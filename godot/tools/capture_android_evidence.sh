#!/usr/bin/env bash
set -euo pipefail

ADB="${ADB:-adb}"
APK="${1:?usage: capture_android_evidence.sh APK [OUTPUT_DIR] [BUILD_ID]}"
OUTPUT_DIR="${2:-test-results/android-gameplay/after}"
BUILD_ID="${3:-working-tree}"
PACKAGE="com.jtripppiie.mooserush"
ACTIVITY="com.godot.game.GodotAppLauncher"
STAGE="${VERIFICATION_STAGE:-0}"
RECORDS="$OUTPUT_DIR/evidence.tsv"

mkdir -p "$OUTPUT_DIR" "$OUTPUT_DIR/../videos"
printf 'file\tbuild\tstage\tstarting_state\tinput\texpected\tobserved\tstatus\tlog\ttester\n' > "$RECORDS"

SCENARIOS=(
	"01-launch.png:launch"
	"02-main-menu.png:main-menu"
	"03-stage-map.png:stage-map"
	"04-stage-start.png:stage-start"
	"05-running.png:running"
	"06-short-jump.png:short-jump"
	"07-full-jump.png:full-jump"
	"08-air-jump.png:air-jump"
	"09-dash.png:dash"
	"10-stomp.png:stomp"
	"11-snowball.png:snowball"
	"12-key-collected.png:key-collected"
	"13-first-rescue.png:first-rescue"
	"14-second-rescue.png:second-rescue"
	"15-checkpoint.png:checkpoint"
	"16-enemy-encounter.png:enemy-encounter"
	"17-player-damaged.png:player-damaged"
	"18-boss-tell.png:boss-tell"
	"19-boss-attack.png:boss-attack"
	"20-boss-defeated.png:boss-defeated"
	"21-stage-goal.png:stage-goal"
	"22-stage-complete.png:stage-complete"
	"23-next-stage-unlocked.png:next-stage-unlocked"
	"24-pause-menu.png:pause-menu"
	"25-return-to-map.png:return-to-map"
	"26-game-over.png:game-over"
	"27-restart.png:restart"
	"28-review-mode.png:review-mode"
	"29-large-text.png:large-text"
	"30-high-contrast.png:high-contrast"
	"31-touch-controls.png:touch-controls"
	"32-background-resume.png:background-resume"
)

wait_for_result() {
	local marker=""
	for _attempt in $(seq 1 45); do
		marker="$("$ADB" logcat -d -s godot:I godot:E '*:S' | rg 'VERIFICATION READY|VERIFICATION FAIL' | tail -1 || true)"
		if [[ -n "$marker" ]]; then
			printf '%s' "$marker"
			return 0
		fi
		if "$ADB" logcat -d | rg -q 'uniform vectors exceed|FATAL EXCEPTION|Fatal signal'; then
			printf 'runtime failed before verification-ready marker'
			return 1
		fi
		sleep 1
	done
	printf 'verification-ready timeout'
	return 1
}

capture_scenario() {
	local file="$1"
	local scenario="$2"
	local observed=""
	local status="FAIL"
	"$ADB" shell am force-stop "$PACKAGE"
	"$ADB" logcat -c
	"$ADB" shell am start -n "$PACKAGE/$ACTIVITY" \
		--es verification_scenario "$scenario" \
		--ei verification_stage "$STAGE" >/dev/null
	if observed="$(wait_for_result)"; then
		status="PASS"
	fi
	"$ADB" exec-out screencap -p > "$OUTPUT_DIR/$file"
	"$ADB" logcat -d -v threadtime > "$OUTPUT_DIR/${file%.png}.logcat.txt"
	printf '%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n' \
		"$file" "$BUILD_ID" "$STAGE" "clean debug launch" \
		"debug scenario: $scenario" "production systems reach requested state" \
		"$observed" "$status" "${file%.png}.logcat.txt" "emulator automation" \
		>> "$RECORDS"
}

"$ADB" install -r "$APK"
for entry in "${SCENARIOS[@]}"; do
	capture_scenario "${entry%%:*}" "${entry#*:}"
done

VIDEO_SCENARIOS=(
	"movement-responsiveness:running"
	"short-jump:short-jump"
	"full-jump:full-jump"
	"air-jump:air-jump"
	"dash:dash"
	"stomp:stomp"
	"simultaneous-movement-jump:simultaneous-input"
	"enemy-collision:player-damaged"
	"boss-attack-timing:boss-attack"
	"stage-completion:stage-complete"
	"pause-resume:pause-menu"
	"repeated-restart:restart"
)

for entry in "${VIDEO_SCENARIOS[@]}"; do
	name="${entry%%:*}"
	scenario="${entry#*:}"
	remote="/sdcard/$name.mp4"
	"$ADB" shell rm -f "$remote"
	"$ADB" shell screenrecord --time-limit 8 "$remote" &
	recorder_pid=$!
	sleep 1
	"$ADB" shell am force-stop "$PACKAGE"
	"$ADB" shell am start -n "$PACKAGE/$ACTIVITY" \
		--es verification_scenario "$scenario" \
		--ei verification_stage "$STAGE" >/dev/null
	wait "$recorder_pid" || true
	"$ADB" pull "$remote" "$OUTPUT_DIR/../videos/$name.mp4"
	"$ADB" shell rm -f "$remote"
done

echo "Android evidence capture complete: $OUTPUT_DIR"
