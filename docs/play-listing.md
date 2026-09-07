# Play Store listing copy — MIND[SET]

Source of truth for the Play Console listing. Update here first, then paste across, so the
listing and the app stay in step.

Positioning note: MIND[SET] is a tracker for **fitness racing**, not a HYROX app. The data layer
already treats a race format as reference data (`event_format` / `event_segment` /
`event_division` / `event_segment_standard`, keyed by `formatKey`), so a new event is seed rows
rather than a new app. The listing says that, with HYROX named as the format shipped today. This
keeps the copy true as DEKA and others land, and keeps the trademark used descriptively rather
than as the product's identity.

---

## App name (max 30)

```
MIND[SET]
```
9 characters. No trademark in the title — see the trademark note below.

---

## Short description (max 80)

```
Offline training tracker for HYROX and fitness racing: clock, splits, PBs.
```
74 characters. Keeps HYROX for search — it is what athletes actually type — while "and fitness
racing" stops the app reading as HYROX-only.

Alternatives, same constraint:

```
Race-day training tracker: live clock, station splits, PBs. HYROX built in.
```
```
Train for fitness racing: live race clock, station splits, PBs. Fully offline.
```

---

## Full description (max 4000)

```
MIND[SET] is a training tracker for fitness racing — the kind of event where you run, hit a
station, and run again, and where your split on each station decides your day.

It works entirely offline. There is no account to create, no sign-in, and nothing to sync. Your
training data lives on your phone and stays there.


RACE FORMATS ARE DATA, NOT GUESSWORK

MIND[SET] does not hardcode one event. A race format is reference data: its ordered sequence of
runs, stations and transitions, its divisions, and the exact loads, distances and rep counts each
division has to hit.

HYROX is the format available today — the full 16-segment sequence, Women's and Men's, Open and
Pro, checked against the rulebook rather than typed from memory. Further formats are added as
data, so supporting your event does not mean waiting for the app to be rebuilt around it.

• Division-correct weights, distances and rep counts on every screen
• Full, half and split-race variants
• Personal bests calculated per station, against your own division


LIVE RACE CLOCK

Run a full simulation with a clock that keeps time whether or not the screen is on. Total elapsed
time and per-segment splits are recorded as you move, so you get the same numbers you would get
on race day.

• Runs, stations and transitions timed separately
• Keeps running with the app in the background or the screen off
• Pause, resume and advance from the notification, without unlocking
• Survives being swiped out of Recents — the clock does not stop because Android did


TEMPLATE-FIRST LOGGING

Build a session once and reuse it. Templates cover both race simulations and strength work, so
the logging screen is filled in before you start and you are entering numbers, not building a
workout mid-set.

• Reusable race and strength templates
• Weight, reps, time, distance and calories
• Warm-up, main, accessory, conditioning and core sections


TRAINING HISTORY AND PERSONAL BESTS

Every session is kept, searchable and broken down segment by segment. Personal bests are tracked
across estimated 1RM, max weight, max reps, best time and max calories.


EXERCISE LIBRARY

Hundreds of exercises with muscle diagrams and instructions, drawn from open datasets and
credited in the app.


BUILT WITH PRIVACY AS THE DEFAULT

• No account, no sign-in, no profile on anyone's server
• No advertising and no advertising identifiers
• No usage or behavioural analytics
• Crash reports only, so bugs can be fixed


HONEST ABOUT WHERE IT IS

MIND[SET] is in active development, built by one developer training for a race. Today it ships
one race format, HYROX, and Singles standards — Doubles and Relay are not in yet. The interface
is dark only. Cloud sync is planned but not built, so back your phone up.

Feedback goes straight to the developer and shapes what gets built next.


---
Not affiliated with, endorsed by, or connected to HYROX or Upsolut Sports AG. HYROX is a
trademark of its respective owner and is used here only to describe a race format the app
supports.
```

Roughly 2,300 characters — well inside the 4,000 limit.

---

## Graphics

| Asset | File | Spec |
|---|---|---|
| App icon | `docs/branding/playstore-512.png` | 512×512 PNG, 32-bit, opaque |
| Feature graphic | `docs/branding/playstore-feature.png` | 1024×500 PNG, no alpha |
| Phone screenshots | **still needed** | min 2, max 8 · 16:9 or 9:16 · each side 320–3840 px |

Regenerate the first two with `python3 tools/branding/generate_brand_assets.py`.

---

## Trademark note

HYROX is a registered trademark. The app name carries no trademark, the description names it as
one supported race format rather than as the app's identity, and the listing ends with an explicit
non-affiliation disclaimer. That is the ordinary posture for an unaffiliated third-party app, but
the holder can still object — if a takedown ever arrives, the fix is to describe the format
generically ("8 stations and 8 runs") rather than by name. Generalised positioning makes that a
copy edit instead of a repositioning.

---

## Foreground service declaration (FOREGROUND_SERVICE_SPECIAL_USE)

Play Console asks: *"Describe your app's use of this permission, including why the task must start
immediately and cannot be paused or restarted."* Paste the following.

```
MIND[SET] is a training tracker for fitness racing. This foreground service runs a live race
clock, started only when the athlete explicitly taps "Start race" in the app.

A race is one continuous timed effort — in the format shipped today, 8 runs and 8 stations. The
service records total elapsed time and every individual run, station and transition split as the
athlete moves through the race. It shows an ongoing notification with the current segment and
running clock, plus Pause, Resume, Next and Finish controls, so the athlete can advance between
stations without unlocking their phone mid-effort.

Why the task must start immediately: the clock IS the athlete's result. Timing has to begin on
the exact moment they tap Start and be accurate from that first second. A deferred or batched
start would mean the recorded race time does not match the effort actually performed, which makes
the result worthless.

Why it cannot be paused or restarted: the athlete is physically racing with the phone pocketed
and the screen off, typically for 60 to 90 minutes. If the system deferred, paused or restarted
this task, the elapsed time and per-segment splits would be lost or wrong. Those timings are the
entire output of the feature and cannot be reconstructed afterwards — there is no record of when
each station started or ended other than this running clock.

The service is started only by direct user action, remains visible for its entire lifetime
through the ongoing notification, and stops as soon as the athlete taps Finish or cancels the
race. It performs no work outside an active race.
```

### If the reviewer asks why not a defined FGS type

```
No defined foreground service type fits. The closest is "health", but that type additionally
requires a runtime sensor permission (BODY_SENSORS, ACTIVITY_RECOGNITION or
HIGH_SAMPLING_RATE_SENSORS). MIND[SET] reads no sensors at all — it is a stopwatch and a split
recorder. Requesting a sensor permission the app never uses would mean asking users for more
access than the feature needs, purely to satisfy a type gate. "specialUse" is declared instead,
with an android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE property in the manifest describing exactly
this use.
```

### If asked about persistence after swiping the app away

```
The service declares stopWithTask="false" so that removing the app from Recents does not stop a
race in progress. An athlete mid-race should not lose their timing because the app was swiped
away. The race still ends on Finish or cancel, and the ongoing notification is visible the whole
time, so the user always has a one-tap way to stop it.
```

---

## Demonstration video (required for the FGS declaration)

Play wants a link to a video showing the permission actually being used. It must be viewable
**without signing in** — an *unlisted* YouTube video works, a *private* one does not. Keep it
under about 90 seconds and let the link stay live: reviewers revisit it on later submissions.

### Shot list

Each beat proves one claim in the written justification. Do them in this order and the video
answers the reviewer's questions before they ask.

1. **App open on Home.** Establishes it is MIND[SET] and the race has not started.
2. **Tap "Start race".** Proves the service is user-initiated, not started in the background.
3. **The clock running in-app**, a few seconds on the current segment.
4. **Press the power button to lock the phone.** The ongoing notification is visible on the lock
   screen with the clock still advancing. This is the core of the whole declaration.
5. **Tap Next on the lock-screen notification** to advance a station — without unlocking. Show
   the segment name change and the split being taken.
6. **Wait ten seconds with the screen off**, then wake it: the clock has kept correct time. This
   is the "cannot be paused or restarted" claim, shown rather than asserted.
7. **Unlock and open the app**: the recorded split for the finished station is there.
8. **Tap Finish.** The notification disappears and the service stops — proving it is bounded.

### How to record it

A screen recording cannot show a screen that is off, and secure lock screens often capture as
black. **Film the phone with a second camera.** It is lower effort and it demonstrates screen-off
behaviour in a way a screen capture physically cannot.

If you want a clean capture of the in-app portions to cut in, `adb` can record the unlocked parts:

    adb shell screenrecord --time-limit 60 /sdcard/fgs-demo.mp4
    adb pull /sdcard/fgs-demo.mp4 docs/screenshots/

Narration is not needed. If you add on-screen captions, label beats 4 and 6 explicitly
("screen locked — clock still running", "10 seconds later — time is correct"), because those are
the two the reviewer is checking for.
