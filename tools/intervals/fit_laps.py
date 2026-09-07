#!/usr/bin/env python3
"""
Minimal, dependency-free FIT decoder — counts laps and dumps their key fields.

Purpose: verify whether a COROS (or any) .FIT export preserves the per-segment
lap structure that the vendor->intervals.icu sync flattens. Runs entirely
locally; nothing is uploaded.

Usage:  python3 tools/intervals/fit_laps.py /path/to/activity.fit

We decode just enough of the FIT binary spec to walk record/definition messages
and pull the lap (global msg #19) and session (#18) messages. Field numbers per
the FIT Profile: lap.total_elapsed_time=7, total_timer_time=8, total_distance=9,
avg_heart_rate=15, max_heart_rate=16, sport=25, sub_sport=39, start_time=2.
"""
import sys, struct, datetime

FIT_EPOCH = 631065600  # seconds between Unix epoch and FIT epoch (1989-12-31 UTC)

# global message numbers we care about
G_SESSION, G_LAP, G_RECORD = 18, 19, 20

# lap field defs (field_def_num -> (name, scale, kind)); kind: 't'=time,'d'=dist,'i'=int,'ts'=timestamp
LAP_FIELDS = {
    2:  ("start_time", 1, "ts"),
    7:  ("elapsed_s", 1000.0, "t"),
    8:  ("timer_s", 1000.0, "t"),
    9:  ("distance_m", 100.0, "d"),
    15: ("avg_hr", 1, "i"),
    16: ("max_hr", 1, "i"),
    25: ("sport", 1, "i"),
    39: ("sub_sport", 1, "i"),
    254:("idx", 1, "i"),
}

def read_uint(b, big):
    return int.from_bytes(b, "big" if big else "little", signed=False)

def main(path):
    data = open(path, "rb").read()
    hdr_size = data[0]
    data_size = struct.unpack("<I", data[4:8])[0]   # bytes 4-7; 8-11 is the ".FIT" magic
    body = data[hdr_size: hdr_size + data_size]

    defs = {}   # local_type -> dict(global, arch, fields=[(num,size)], dev_bytes)
    laps, sessions, record_count = [], 0, 0
    i = 0
    n = len(body)
    while i < n:
        h = body[i]; i += 1
        if h & 0x80:  # compressed timestamp header -> data message, local type in bits 5-6
            local = (h >> 5) & 0x03
            d = defs.get(local)
            if not d: break
            rec, i = read_data(body, i, d)
            sessions, record_count = tally(d, rec, laps, sessions, record_count)
        elif h & 0x40:  # definition message
            local = h & 0x0F
            has_dev = bool(h & 0x20)
            i += 1  # reserved
            arch = body[i]; i += 1
            gnum = read_uint(body[i:i+2], arch == 1); i += 2
            nfields = body[i]; i += 1
            fields = []
            for _ in range(nfields):
                fnum = body[i]; fsize = body[i+1]; i += 3  # (num,size,base_type)
                fields.append((fnum, fsize))
            dev_bytes = 0
            if has_dev:
                ndev = body[i]; i += 1
                for _ in range(ndev):
                    dsize = body[i+1]; i += 3
                    dev_bytes += dsize
            defs[local] = {"global": gnum, "arch": arch, "fields": fields, "dev_bytes": dev_bytes}
        else:  # normal data message
            local = h & 0x0F
            d = defs.get(local)
            if not d: break
            rec, i = read_data(body, i, d)
            sessions, record_count = tally(d, rec, laps, sessions, record_count)
    report(path, laps, sessions, record_count)

def read_data(body, i, d):
    big = d["arch"] == 1
    rec = {}
    for fnum, fsize in d["fields"]:
        raw = body[i:i+fsize]; i += fsize
        rec[fnum] = raw if not all(x == 0xFF for x in raw) else None
    i += d["dev_bytes"]  # skip developer fields
    return rec, i

def tally(d, rec, laps, sessions, record_count):
    g = d["global"]; big = d["arch"] == 1
    if g == G_RECORD:
        record_count += 1
    elif g == G_SESSION:
        sessions += 1
    elif g == G_LAP:
        parsed = {}
        for fnum, (name, scale, kind) in LAP_FIELDS.items():
            raw = rec.get(fnum)
            if raw is None:
                parsed[name] = None; continue
            v = read_uint(raw, big)
            if kind == "ts":
                parsed[name] = datetime.datetime.utcfromtimestamp(v + FIT_EPOCH).strftime("%H:%M:%S")
            elif kind in ("t", "d"):
                parsed[name] = round(v / scale, 1)
            else:
                parsed[name] = v
        laps.append(parsed)
    return sessions, record_count

def report(path, laps, sessions, record_count):
    print(f"\nFile: {path}")
    print(f"sessions={sessions}  record_samples={record_count}  LAPS={len(laps)}\n")
    if not laps:
        print("No lap messages found — this export has NO per-segment structure.")
        return
    hdr = f"{'#':>2}  {'start':>8}  {'elapsed_s':>9}  {'dist_m':>8}  {'avgHR':>5}  {'maxHR':>5}  {'sub':>3}"
    print(hdr); print("-"*len(hdr))
    for k, lp in enumerate(laps, 1):
        print(f"{k:>2}  {str(lp.get('start_time')):>8}  {str(lp.get('elapsed_s')):>9}  "
              f"{str(lp.get('distance_m')):>8}  {str(lp.get('avg_hr')):>5}  "
              f"{str(lp.get('max_hr')):>5}  {str(lp.get('sub_sport')):>3}")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("usage: python3 fit_laps.py /path/to/activity.fit"); sys.exit(2)
    main(sys.argv[1])
