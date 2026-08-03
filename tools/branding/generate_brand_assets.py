#!/usr/bin/env python3
"""Generate the MIND[SET] brand assets from a single geometry source.

The icon is a dark rounded tile with two white square brackets `[ ]` framing a
red pulse/heartbeat line. Every output below is derived from the same normalized
geometry (fractions of the icon's side length) so the raster PNGs and the Android
vector drawables stay pixel-consistent.

Outputs
  Android vector drawables (app/src/main/res/drawable/):
    ic_launcher_foreground.xml  glyph only, 108 viewport, adaptive safe-zone
    ic_launcher_background.xml  solid tile fill, 108 viewport
    ic_launcher_monochrome.xml  glyph silhouette, single color (themed icons)
    brand_logo.xml              full icon (tile + glyph) for in-app / splash use
  Android legacy PNG mipmaps (app/src/main/res/mipmap-*/), for API 24-25:
    ic_launcher.png (rounded)   +  ic_launcher_round.png (circle)
  iOS (iosApp/.../AppIcon.appiconset/):
    AppIcon-1024.png / -dark.png / -tinted.png  +  Contents.json
  Source / store (docs/branding/):
    mindset-icon.svg            canonical vector source
    playstore-512.png           Play Store listing icon

Run:  python3 tools/branding/generate_brand_assets.py
Requires Pillow (pip install Pillow).
"""

from __future__ import annotations

import json
import os

from PIL import Image, ImageDraw

# ---------------------------------------------------------------------------
# Brand tokens — keep in sync with app colors.xml and the Compose splash.
# ---------------------------------------------------------------------------
BRAND_BG = "#0F0F11"    # splash / window background
BRAND_TILE = "#1B1C20"  # icon tile / adaptive background
BRAND_INK = "#FFFFFF"   # brackets + wordmark letters
BRAND_RED = "#E5484D"   # pulse line + wordmark brackets + progress

SS = 4  # supersampling factor for anti-aliased PNGs

# ---------------------------------------------------------------------------
# Geometry — all values are fractions of the icon side length S in [0, 1].
# Content stays within the adaptive-icon center safe zone (~0.19..0.81).
# ---------------------------------------------------------------------------
STROKE = 0.030      # bracket bar / nub thickness
BAR_TOP = 0.365
BAR_BOT = 0.635
NUB_LEN = 0.075     # inward horizontal nub length
LEFT_X = 0.335      # left bracket outer edge
RIGHT_X = 0.665     # right bracket outer edge
PULSE_STROKE = 0.032
TILE_RADIUS = 0.2237  # squircle-ish corner radius fraction

# Heartbeat polyline (normalized points), left -> right. Symmetric about the icon center
# (x=0.5, baseline y=0.505): even horizontal arms, then a rise to a peak and an equal drop to a
# valley (same amplitude up as down), mirroring the reference logo.
_PY = 0.505   # baseline
_AMP = 0.10   # peak/valley amplitude (even up and down)
PULSE = [
    (0.410, _PY),          # left arm (flat)
    (0.452, _PY),
    (0.480, _PY - _AMP),   # peak (up)
    (0.520, _PY + _AMP),   # valley (down, equal amplitude)
    (0.548, _PY),
    (0.590, _PY),          # right arm (flat), mirror of the left
]


def _bracket_rects():
    """Return the six rounded-rect specs (x0, y0, x1, y1) for the two brackets."""
    s = STROKE
    return [
        # left: bar, top nub, bottom nub
        (LEFT_X, BAR_TOP, LEFT_X + s, BAR_BOT),
        (LEFT_X, BAR_TOP, LEFT_X + NUB_LEN, BAR_TOP + s),
        (LEFT_X, BAR_BOT - s, LEFT_X + NUB_LEN, BAR_BOT),
        # right: bar, top nub, bottom nub
        (RIGHT_X - s, BAR_TOP, RIGHT_X, BAR_BOT),
        (RIGHT_X - NUB_LEN, BAR_TOP, RIGHT_X, BAR_TOP + s),
        (RIGHT_X - NUB_LEN, BAR_BOT - s, RIGHT_X, BAR_BOT),
    ]


# ---------------------------------------------------------------------------
# Raster rendering (Pillow)
# ---------------------------------------------------------------------------
def render_png(size: int, bg: str | None, ink: str, red: str, tile: str) -> Image.Image:
    """Render the icon at `size` px. tile: 'rounded' | 'circle' | 'square'.

    bg=None makes the background transparent (glyph only).
    """
    n = size * SS
    img = Image.new("RGBA", (n, n), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    def px(v):
        return v * n

    if bg is not None:
        if tile == "circle":
            d.ellipse([0, 0, n - 1, n - 1], fill=bg)
        elif tile == "square":
            d.rectangle([0, 0, n, n], fill=bg)
        else:  # rounded
            d.rounded_rectangle([0, 0, n - 1, n - 1], radius=px(TILE_RADIUS), fill=bg)

    for (x0, y0, x1, y1) in _bracket_rects():
        r = px(STROKE) / 2
        d.rounded_rectangle([px(x0), px(y0), px(x1), px(y1)], radius=r, fill=ink)

    pts = [(px(x), px(y)) for (x, y) in PULSE]
    w = int(round(px(PULSE_STROKE)))
    d.line(pts, fill=red, width=w, joint="curve")
    rr = w / 2
    for (x, y) in (pts[0], pts[-1]):  # round caps
        d.ellipse([x - rr, y - rr, x + rr, y + rr], fill=red)

    return img.resize((size, size), Image.LANCZOS)


# ---------------------------------------------------------------------------
# Vector path builders (shared by SVG + Android <vector>)
# ---------------------------------------------------------------------------
def _round_rect_path(x0, y0, x1, y1, r, v):
    """Rounded-rect path data scaled to viewport `v`."""
    x0, y0, x1, y1, r = x0 * v, y0 * v, x1 * v, y1 * v, r * v
    r = min(r, (x1 - x0) / 2, (y1 - y0) / 2)
    f = lambda n: f"{n:.2f}"
    return (
        f"M{f(x0 + r)},{f(y0)} L{f(x1 - r)},{f(y0)} Q{f(x1)},{f(y0)} {f(x1)},{f(y0 + r)} "
        f"L{f(x1)},{f(y1 - r)} Q{f(x1)},{f(y1)} {f(x1 - r)},{f(y1)} "
        f"L{f(x0 + r)},{f(y1)} Q{f(x0)},{f(y1)} {f(x0)},{f(y1 - r)} "
        f"L{f(x0)},{f(y0 + r)} Q{f(x0)},{f(y0)} {f(x0 + r)},{f(y0)} Z"
    )


def _pulse_path(v):
    f = lambda n: f"{n:.2f}"
    pts = [(x * v, y * v) for (x, y) in PULSE]
    head = f"M{f(pts[0][0])},{f(pts[0][1])}"
    rest = " ".join(f"L{f(x)},{f(y)}" for (x, y) in pts[1:])
    return f"{head} {rest}"


def bracket_paths(v):
    r = STROKE / 2
    return [_round_rect_path(x0, y0, x1, y1, r, v) for (x0, y0, x1, y1) in _bracket_rects()]


def android_vector(include_tile: bool, mono: bool, viewport: int = 108) -> str:
    v = viewport
    ink = "#FF000000" if mono else BRAND_INK.replace("#", "#FF")
    red = ink if mono else BRAND_RED.replace("#", "#FF")
    lines = [
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
        f'    android:width="{v}dp" android:height="{v}dp"',
        f'    android:viewportWidth="{v}" android:viewportHeight="{v}">',
    ]
    if include_tile:
        tile = BRAND_TILE.replace("#", "#FF")
        lines.append(
            f'    <path android:fillColor="{tile}" '
            f'android:pathData="{_round_rect_path(0, 0, 1, 1, TILE_RADIUS, v)}" />'
        )
    for p in bracket_paths(v):
        lines.append(f'    <path android:fillColor="{ink}" android:pathData="{p}" />')
    lines.append(
        f'    <path android:strokeColor="{red}" android:strokeWidth="{PULSE_STROKE * v:.2f}"'
        f' android:strokeLineCap="round" android:strokeLineJoin="round"'
        f' android:pathData="{_pulse_path(v)}" />'
    )
    lines.append("</vector>\n")
    return "\n".join(lines)


def android_background(viewport: int = 108) -> str:
    v = viewport
    tile = BRAND_TILE.replace("#", "#FF")
    return (
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        f'    android:width="{v}dp" android:height="{v}dp"\n'
        f'    android:viewportWidth="{v}" android:viewportHeight="{v}">\n'
        f'    <path android:fillColor="{tile}" android:pathData="M0,0h{v}v{v}h-{v}z" />\n'
        "</vector>\n"
    )


def svg_source(v: int = 108) -> str:
    parts = [
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{v}" height="{v}" viewBox="0 0 {v} {v}">',
        f'  <path fill="{BRAND_TILE}" d="{_round_rect_path(0, 0, 1, 1, TILE_RADIUS, v)}"/>',
    ]
    for p in bracket_paths(v):
        parts.append(f'  <path fill="{BRAND_INK}" d="{p}"/>')
    parts.append(
        f'  <path fill="none" stroke="{BRAND_RED}" stroke-width="{PULSE_STROKE * v:.2f}"'
        f' stroke-linecap="round" stroke-linejoin="round" d="{_pulse_path(v)}"/>'
    )
    parts.append("</svg>\n")
    return "\n".join(parts)


# ---------------------------------------------------------------------------
# Emit
# ---------------------------------------------------------------------------
REPO = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
RES = os.path.join(REPO, "app", "src", "main", "res")
IOS_ICON = os.path.join(
    REPO, "iosApp", "iosApp", "Assets.xcassets", "AppIcon.appiconset"
)
BRANDING = os.path.join(REPO, "docs", "branding")

MIPMAP_DPI = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}


def _write(path: str, text: str):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as fh:
        fh.write(text)
    print("wrote", os.path.relpath(path, REPO))


def _save(img: Image.Image, path: str):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)
    print("wrote", os.path.relpath(path, REPO), f"({img.width}x{img.height})")


def main():
    # Remove the stock Android Studio droid foreground, which lives in drawable-v24 (it uses an
    # inline aapt gradient that needs API 24+). Our vector has no gradient, so it lives in plain
    # drawable/ and applies on every API. Left in place, the -v24 variant would shadow ours on
    # API 24+ and the launcher/system-splash would still show the droid.
    stale = os.path.join(RES, "drawable-v24", "ic_launcher_foreground.xml")
    if os.path.exists(stale):
        os.remove(stale)
        print("removed", os.path.relpath(stale, REPO))
        try:
            os.rmdir(os.path.dirname(stale))
        except OSError:
            pass

    # Android vector drawables
    _write(os.path.join(RES, "drawable", "ic_launcher_foreground.xml"),
           android_vector(include_tile=False, mono=False))
    _write(os.path.join(RES, "drawable", "ic_launcher_background.xml"),
           android_background())
    _write(os.path.join(RES, "drawable", "ic_launcher_monochrome.xml"),
           android_vector(include_tile=False, mono=True))
    _write(os.path.join(RES, "drawable", "brand_logo.xml"),
           android_vector(include_tile=True, mono=False))

    # Android legacy PNG mipmaps (API 24-25)
    for dpi, size in MIPMAP_DPI.items():
        _save(render_png(size, BRAND_TILE, BRAND_INK, BRAND_RED, "rounded"),
              os.path.join(RES, f"mipmap-{dpi}", "ic_launcher.png"))
        _save(render_png(size, BRAND_TILE, BRAND_INK, BRAND_RED, "circle"),
              os.path.join(RES, f"mipmap-{dpi}", "ic_launcher_round.png"))

    # iOS AppIcon (full-bleed square; iOS masks the corners)
    _save(render_png(1024, BRAND_TILE, BRAND_INK, BRAND_RED, "square"),
          os.path.join(IOS_ICON, "AppIcon-1024.png"))
    _save(render_png(1024, BRAND_BG, BRAND_INK, BRAND_RED, "square"),
          os.path.join(IOS_ICON, "AppIcon-1024-dark.png"))
    _save(render_png(1024, BRAND_BG, BRAND_INK, BRAND_INK, "square"),
          os.path.join(IOS_ICON, "AppIcon-1024-tinted.png"))
    contents = {
        "images": [
            {"idiom": "universal", "platform": "ios", "size": "1024x1024",
             "filename": "AppIcon-1024.png"},
            {"appearances": [{"appearance": "luminosity", "value": "dark"}],
             "idiom": "universal", "platform": "ios", "size": "1024x1024",
             "filename": "AppIcon-1024-dark.png"},
            {"appearances": [{"appearance": "luminosity", "value": "tinted"}],
             "idiom": "universal", "platform": "ios", "size": "1024x1024",
             "filename": "AppIcon-1024-tinted.png"},
        ],
        "info": {"author": "xcode", "version": 1},
    }
    _write(os.path.join(IOS_ICON, "Contents.json"), json.dumps(contents, indent=2) + "\n")

    # Source + store
    _write(os.path.join(BRANDING, "mindset-icon.svg"), svg_source())
    _save(render_png(512, BRAND_TILE, BRAND_INK, BRAND_RED, "rounded"),
          os.path.join(BRANDING, "playstore-512.png"))

    print("\nDone.")


if __name__ == "__main__":
    main()
