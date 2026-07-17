#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
GODOT_BIN="${GODOT_BIN:-godot}"
OUTPUT_DIR="${1:-$ROOT/test-results/android-gameplay/after/audits}"
AUDIT_HOME="${AUDIT_HOME:-/tmp/you-rush-godot-home}"

mkdir -p "$OUTPUT_DIR"
mkdir -p "$AUDIT_HOME/.config/godot" "$AUDIT_HOME/.local/share/godot" "$AUDIT_HOME/.cache"

export HOME="$AUDIT_HOME"
export XDG_CONFIG_HOME="$AUDIT_HOME/.config"
export XDG_DATA_HOME="$AUDIT_HOME/.local/share"
export XDG_CACHE_HOME="$AUDIT_HOME/.cache"

run_audit() {
	local name="$1"
	shift
	"$GODOT_BIN" --headless --path "$ROOT/godot" -- "$@" 2>&1 | tee "$OUTPUT_DIR/$name.log"
	if rg -q "SCRIPT ERROR:|Parse Error:|Compile Error:|Failed to load script" "$OUTPUT_DIR/$name.log"; then
		echo "Audit $name contained a Godot script failure" >&2
		return 1
	fi
}

run_audit touch --touch-audit
run_audit system --system-audit
run_audit lifecycle --lifecycle-audit
run_audit pause --pause-audit
run_audit mechanics --mechanics-audit

for stage in 0 1 2 3 4; do
	run_audit "stage-$stage-geometry" "--geometry-audit=$stage"
	run_audit "stage-$stage-debug-overlay" "--debug-overlay-audit=$stage"
	run_audit "stage-$stage-autoplay" "--autoplay-audit=$stage"
done

echo "All gameplay audits completed without script errors."
