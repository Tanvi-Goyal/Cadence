package com.mindset

/*
 * In-code icon set — the code-based alternative to the vector drawables in res/drawable/ic_*.xml.
 * Both are built from the SAME path geometry exported from the Figma design, so they render
 * identically; this set exists so we can compare an in-code ImageVector approach against
 * painterResource(R.drawable.ic_*). Icons are authored in white and tinted at the call site via
 * Icon(tint = ...).
 *
 * The object is just the namespace: each icon is an extension property on it, declared lazily in
 * its own file under `com.mindset.icons` so unused icons cost nothing.
 */
object MindSetIcons
