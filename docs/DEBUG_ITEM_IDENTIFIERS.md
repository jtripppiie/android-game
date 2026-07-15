# Debug Item Identifiers

Debug APKs show a compact, stable ID beside each gameplay object. Use that ID
when requesting a tweak, ideally with a screenshot and the encounter name from
the upper-left debug panel.

Examples:

```text
SUN-OB01  first Midnight Sun obstacle
RIV-AN02  second visible Salmon Rush animal
MOO-PF03  third Moose Pass route platform
DRK-RG01  first Dark Winter trick ring
BER-PU02  second Bear Country utility pickup
```

Stage prefixes:

- `SUN`: Midnight Sun Run
- `RIV`: Salmon Rush
- `MOO`: Moose Pass
- `DRK`: Dark Winter
- `BER`: Bear Country

Item categories:

- `OB`: gate or grounded obstacle
- `AN`: wildlife or stage hazard
- `PF`: route platform
- `PD`: launch pad
- `BL`: supply block
- `RG`: trick ring
- `WT`: water patch
- `PU`: utility pickup
- `AT`: boss attack
- `PL`: player
- `BOSS`: current stage boss

IDs are assigned once and never renumber while that run is active. A new run
restarts each category at `01`. Ordinary stars, player snowballs, particles,
and score popups intentionally have no badges.

The debug APK enables the overlay automatically. Tap the menu title five times
within the existing gesture window to hide or restore it. Android logcat also
records mappings such as:

```text
DEBUG ITEM BER-AN02 = POLAR
DEBUG ITEM BER-PF03 = BRITTLE PLATFORM
```

Useful feedback format:

```text
Bear Country, encounter bear_country_chain:
BER-AN02 is too close to BER-PF03 and BER-PU01 is too high.
```
