#!/usr/bin/env python3
"""Export in an isolated copy; never put upload credentials in the source tree."""
import argparse
import json
import os
from pathlib import Path
import shutil
import subprocess
import tempfile

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--godot', default='godot')
parser.add_argument('--templates', type=Path, help='Matching Godot template directory')
parser.add_argument('--sign', action='store_true', help='Use MOTO_UPLOAD_KEYSTORE, MOTO_UPLOAD_ALIAS, MOTO_UPLOAD_PASSWORD')
parser.add_argument('--output', type=Path)
args = parser.parse_args()
root = Path(__file__).resolve().parents[1]
output = (args.output or root / 'build' / ('moto-thrash-release.aab' if args.sign else 'moto-thrash-unsigned.aab')).resolve()
env = os.environ.copy()
secrets = {}
if args.sign:
    for key in ('MOTO_UPLOAD_KEYSTORE', 'MOTO_UPLOAD_ALIAS', 'MOTO_UPLOAD_PASSWORD'):
        value = env.pop(key, '')
        if not value:
            parser.error(f'{key} must be set locally; do not paste credentials into chat')
        secrets[key] = value
    keyfile = Path(secrets['MOTO_UPLOAD_KEYSTORE']).expanduser().resolve()
    if not keyfile.is_file():
        parser.error('Upload keystore does not exist')
    secrets['MOTO_UPLOAD_KEYSTORE'] = str(keyfile)
output.parent.mkdir(parents=True, exist_ok=True)
with tempfile.TemporaryDirectory(prefix='moto-release-') as tmp:
    project = Path(tmp) / 'project'
    shutil.copytree(root, project, ignore=shutil.ignore_patterns('.git', '.godot', 'build', 'export', 'android', '*.keystore', '*.jks', '*.p12', '.env*'))
    cfg = (project / 'export_presets.cfg').read_text()
    release = cfg[cfg.index('[preset.1]'):].replace('[preset.1]', '[preset.0]').replace('[preset.1.options]', '[preset.0.options]')
    release = release.replace('package/signed=true', 'package/signed=' + str(args.sign).lower())
    if args.templates:
        templates = args.templates.resolve()
        for kind in ('debug', 'release'):
            release = release.replace(f'custom_template/{kind}=""', f'custom_template/{kind}={json.dumps(str(templates / ("android_" + kind + ".apk")))}')
        release += '\ngradle_build/android_source_template=' + json.dumps(str(templates / 'android_source.zip')) + '\n'
    if args.sign:
        for field, key in [('release', 'MOTO_UPLOAD_KEYSTORE'), ('release_user', 'MOTO_UPLOAD_ALIAS'), ('release_password', 'MOTO_UPLOAD_PASSWORD')]:
            release += f'keystore/{field}={json.dumps(secrets[key])}\n'
    preset = project / 'export_presets.cfg'
    preset.write_text(release)
    preset.chmod(0o600)
    def run(arguments):
        # Capture output to redact any credential unexpectedly printed by tools.
        result = subprocess.run([args.godot, '--headless', '--path', str(project), *arguments], env=env, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
        log = result.stdout
        for value in secrets.values():
            log = log.replace(value, '[REDACTED]')
        print(log, end='')
        if result.returncode or any(word in log for word in ('SCRIPT ERROR', 'Parse Error', 'Export failed')):
            raise SystemExit(result.returncode or 1)
    run(['--editor', '--import', '--quit'])
    # Write to a temporary destination so failed exports never replace a valid artifact.
    artifact = Path(tmp) / 'game.aab'
    run(['--install-android-build-template', '--export-release', 'Android Release', str(artifact)])
    if not artifact.is_file():
        raise SystemExit('Exporter did not produce an AAB')
    shutil.copy2(artifact, output)
print(f'Created {output}')
print('Upload signing applied.' if args.sign else 'UNSIGNED: validate locally; not a Play Console upload artifact.')
