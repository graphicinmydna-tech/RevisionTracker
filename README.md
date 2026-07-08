# Revision Tracker — Offline BCS Study Utility

A local-first Android app (Kotlin) for tracking a 5-round spaced-repetition
revision workflow across the 9 compulsory BPSC syllabus subjects. No internet
permission, no accounts — everything lives in `SharedPreferences` under the
file name `bcs_data`.

## Architecture (3-tier navigation)

```
DashboardActivity            → 9 fixed compulsory subjects (master list)
 └─ TopicListActivity         → user-added custom sub-topics per subject
     └─ MilestoneActivity     → the 5-round SR "control room" for one topic
```

## Spaced repetition rule

Each new topic seeds 5 milestones with fixed offsets:

| Round | Offset from previous checkpoint |
|-------|----------------------------------|
| 1     | +1 day   (from topic creation)   |
| 2     | +3 days  (from Round 1 completion) |
| 3     | +7 days  (from Round 2 completion) |
| 4     | +14 days (from Round 3 completion) |
| 5     | +30 days (from Round 4 completion) |

Checking off a round timestamps it, computes the next round's due date, and
arms an exact `AlarmManager` alarm so the device fires a native, high-priority
(`IMPORTANCE_HIGH`) status-bar notification on that date — plus an immediate
confirmation notification the moment you check the box.

## Where things live

- `data/Models.kt` — `Subject`, `Topic`, `Milestone` + JSON (de)serialization
- `data/PrefsManager.kt` — all reads/writes to the `bcs_data` SharedPreferences file
- `util/SpacedRepetition.kt` — the +1/+3/+7/+14/+30 day scheduling math
- `util/ReminderScheduler.kt` — `AlarmManager` wrapper (arm/cancel)
- `util/NotificationHelper.kt` — creates the `IMPORTANCE_HIGH` channel and posts alerts
- `receiver/ReminderReceiver.kt` — fires the notification when an alarm goes off
- `receiver/BootReceiver.kt` — re-arms pending alarms after a device reboot
- `ui/` — the three activities + their RecyclerView adapters

## Build & run

1. Open this folder (`RevisionTracker/`) directly in **Android Studio** (Koala or newer).
   Let Gradle sync — it will pull all dependencies (AndroidX, Material Components).
2. Connect a device/emulator running **API 23+** and press **Run**.
3. On first launch the app auto-seeds the 9 compulsory subjects with empty topic lists.
4. On Android 13+, grant the notification permission when prompted so revision
   alerts can be shown. On Android 12+, if you want exact-time alarms you may
   also need to grant "Alarms & reminders" access in system settings (the app
   falls back to inexact alarms otherwise).

## Notes / things you may want to extend

- Editing/deleting a topic and reordering subjects aren't implemented yet —
  the spec described creation and progression, so that's what's wired up first.
- Milestone offsets (1/3/7/14/30) are centralized in
  `Milestone.DEFAULT_OFFSETS` if you ever want to tune the curve.
- Storage is a single JSON blob per launch (`subjects_json` key) rather than
  one key per record — simplest correct approach for a syllabus-sized dataset;
  say the word if you'd rather split it into per-subject keys.
