# Android export and release checklist

The Godot project is landscape and fixed to a 16:9 480×270 pixel-art internal canvas. Android export uses Godot's standard tooling rather than a checked-in generated Gradle project.

1. Open in a recent Godot 4 editor and install matching **Android export templates**.
2. In Godot Editor Settings, configure Android SDK and Java SDK (JDK 17 is a commonly supported choice; verify against your Godot version).
3. Project → Export → Add → Android; use package ID such as `com.tripperdeelabs.dirtrush` and an appropriate target SDK.
4. Generate a debug APK, install on a physical Android device, and check touch behavior, both landscape directions, edge cutouts, framerate, and suspend/resume.
5. Add an original launcher icon, sound effects, intro polish, and store screenshots.
6. Before release, test each track and the unlock/save system; verify no stuck boost after interruptions and no loss of progress.
7. Configure your **own** upload keystore using a secret outside GitHub, and export a signed `.aab` for Google Play.
8. On Google Play, select a **paid app** with a $0.99 price where supported. Pricing and availability vary by country/tax; a paid app cannot be converted to free and back freely in all store flows.

No ad/analytics/network SDK exists in the game source. Sound and haptics are not implemented in the initial code drop.
