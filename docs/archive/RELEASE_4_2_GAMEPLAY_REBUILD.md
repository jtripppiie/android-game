# You Rush 4.2 Gameplay Rebuild

Version 4.2 is a systemic correction pass based on an adversarial review of the
complete run, not a cosmetic tuning pass.

## Critical verdict

The previous build had three rules that invalidated otherwise promising play:

- Combo never expired, so score measured time alive more than consecutive skill.
- Bosses took damage outside recovery, so firing continuously beat reading tells.
- A life reset discarded hazards but retained route geometry from the dead
  encounter, producing incoherent and occasionally hostile respawn spacing.

It also overpaid the safest obstacle solution: one ordinary shot deleted a log
and paid more than a clean vault. Late bosses stacked an attack, wildlife, and
falling ice into the same decision window.

## Replacement contracts

- Combo is now a 2.15–2.8 second action chain. Multipliers unlock at 4, 7, and
  10 linked actions and cap at x4, including FLOW and Aurora surges.
- Stage and expedition rewards are banked flat. They cannot be inflated by a
  live combo or score perk.
- Boss armor rejects direct damage outside the early recovery window. Normal
  weak shots deal 1; empowered weak shots deal 2. Tells and recovery windows
  are longer, acceleration is restrained, and the fight timer is 65 seconds.
- Boss beats ask one primary question at a time. Automatic phase-two crossfire
  is removed, snow walls are limited to two readable lanes, and final-phase
  falling ice is attached only to the summon pattern.
- Two animals in an enraged summon are separated by 188 gameplay dp rather than
  62 dp, preventing merged collision silhouettes.
- Ordinary shots crack a log and require a second hit. Empowered shots retain a
  one-hit utility option. Shooting is safer but lower-scoring than vaulting.
- Respawn atomically clears every encounter-owned object and gives longer spawn
  separation. Boss arenas are rebuilt before combat resumes.

## Regression coverage

Pure unit tests lock combo thresholds/window floors, multiplier caps, banked
reward behavior, boss armor damage, log durability, boss timing, and the existing
director, collision, reward, route, and stage rules.
