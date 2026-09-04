#!/usr/bin/env bash
#
# intervals.icu API probe / smoke test.
#
# Phase-0 tool: prove the integration end-to-end with curl before any Kotlin,
# and re-run later as a health check when something looks off.
#
# Auth is HTTP Basic: username is the literal "API_KEY", password is your key.
# The key is read from $ICU_KEY so it never lands in shell history or in args.
#
#   export ICU_KEY="your_key_here"      # get it: intervals.icu -> Settings -> Developer
#   ./tools/intervals/probe.sh                 # full probe: whoami -> activities -> newest activity+intervals+streams
#   ./tools/intervals/probe.sh whoami          # just auth + athlete profile
#   ./tools/intervals/probe.sh activities 30   # activities in the last 30 days (default 30)
#   ./tools/intervals/probe.sh activity iXXXXX # one activity with lap/interval breakdown
#   ./tools/intervals/probe.sh streams  iXXXXX # per-sample time series for one activity
#
# Raw JSON for every call is saved under tools/intervals/samples/ so you can
# read the ACTUAL field names and write your Kotlin data classes from reality,
# not from any doc (including this one).

set -euo pipefail

BASE="https://intervals.icu/api/v1"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUT="$SCRIPT_DIR/samples"
mkdir -p "$OUT"

if [[ -z "${ICU_KEY:-}" ]]; then
  echo "ERROR: ICU_KEY is not set. Run:  export ICU_KEY=\"your_key_here\"" >&2
  exit 1
fi

# curl wrapper: basic auth, fail loudly on HTTP >=400, capture status.
# $1 = url, $2 = output file. Echoes the HTTP status line to stderr.
icu() {
  local url="$1" out="$2" code
  code=$(curl -sS -u "API_KEY:${ICU_KEY}" \
              -w '%{http_code}' -o "$out" \
              "$url") || { echo "curl failed for $url" >&2; return 1; }
  echo "  HTTP $code  ->  $out  ($(wc -c <"$out" | tr -d ' ') bytes)" >&2
  if [[ "$code" == "429" ]]; then
    echo "  RATE LIMITED (429). Back off and retry." >&2
  fi
  [[ "$code" -lt 400 ]]
}

whoami() {
  echo "== whoami: athlete profile (also proves auth works) ==" >&2
  icu "$BASE/athlete/0" "$OUT/athlete.json"
  jq '{id, name, sportSettings: (.sportSettings // "n/a"), icu_activated: (.icu_activated // "n/a")}' \
     "$OUT/athlete.json" 2>/dev/null || cat "$OUT/athlete.json"
}

# The "0" alias resolves to the key owner on /athlete/{id} but NOT reliably on
# every sub-endpoint (activities is one that wants the concrete id). So we read
# the real athlete id from the profile once and use it everywhere.
athlete_id() {
  [[ -s "$OUT/athlete.json" ]] || icu "$BASE/athlete/0" "$OUT/athlete.json" >/dev/null
  jq -r '.id // "0"' "$OUT/athlete.json" 2>/dev/null || echo 0
}

activities() {
  local days="${1:-30}"
  # newest/oldest are ISO dates. We derive them with `date` (macOS BSD syntax).
  local newest oldest aid
  newest=$(date -u +%Y-%m-%d)
  oldest=$(date -u -v-"${days}"d +%Y-%m-%d)
  aid=$(athlete_id)
  echo "== activities for $aid: $oldest .. $newest ==" >&2
  icu "$BASE/athlete/$aid/activities?oldest=$oldest&newest=$newest" "$OUT/activities.json"
  local kind
  kind=$(jq -r 'type' "$OUT/activities.json" 2>/dev/null || echo unknown)
  if [[ "$kind" != "array" ]]; then
    echo "  Unexpected response (not a list). Raw body below — paste this to me:" >&2
    head -c 400 "$OUT/activities.json" >&2; echo >&2
    return 0
  fi
  local n
  n=$(jq 'length' "$OUT/activities.json")
  if [[ "$n" == "0" ]]; then
    echo "  EMPTY for $oldest..$newest. If the dashboard shows workouts in this" >&2
    echo "  range, this is a date/param issue (tell me) — not a sync issue." >&2
    return 0
  fi
  echo "  $n activities. Most recent first:" >&2
  jq -r '.[] | "  \(.id)\t\(.start_date_local // .start_date)\t\(.type)\t\(.name)"' \
     "$OUT/activities.json" | head -20
}

activity() {
  local id="$1"
  echo "== activity $id (with intervals=true — THE important one) ==" >&2
  icu "$BASE/activity/$id?intervals=true" "$OUT/activity-$id.json"
  echo "  --- lap/interval count & field names present on each interval: ---" >&2
  jq '{
        interval_count: (.icu_intervals | length? // 0),
        interval_field_names: (.icu_intervals[0] | keys? // []),
        activity_field_names: (. | keys)
      }' "$OUT/activity-$id.json" 2>/dev/null \
    || echo "  (could not parse intervals — open $OUT/activity-$id.json and read it)"
}

streams() {
  local id="$1"
  local types="heartrate,cadence,velocity_smooth,distance,time"
  echo "== streams $id ($types) ==" >&2
  icu "$BASE/activity/$id/streams.json?types=$types" "$OUT/streams-$id.json"
  jq -r 'if type=="array" then (.[] | "  stream: \(.type)\tpoints: \(.data|length)")
         else "  (unexpected shape — read the file)" end' \
     "$OUT/streams-$id.json" 2>/dev/null || cat "$OUT/streams-$id.json"
}

case "${1:-full}" in
  whoami)     whoami ;;
  activities) activities "${2:-30}" ;;
  activity)   activity "${2:?need an activity id, e.g. iXXXXXXXX}" ;;
  streams)    streams  "${2:?need an activity id, e.g. iXXXXXXXX}" ;;
  full)
    whoami
    echo >&2
    activities 30
    # auto-pick the most recent activity id and drill into it
    newest_id=$(jq -r '.[0].id // empty' "$OUT/activities.json" 2>/dev/null || true)
    if [[ -n "${newest_id:-}" ]]; then
      echo >&2
      activity "$newest_id"
      echo >&2
      streams "$newest_id"
    else
      echo >&2
      echo "No activity to drill into. Sync a workout, then re-run." >&2
    fi
    echo >&2
    echo "Done. Raw JSON in $OUT/ — read it to write your data classes." >&2
    ;;
  *) echo "unknown command: $1 (try: whoami | activities | activity <id> | streams <id> | full)" >&2; exit 2 ;;
esac
