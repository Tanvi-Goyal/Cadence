package com.mindset.model

/**
 * The four bottom-nav destinations.
 *
 * [Log] is an ACTION tab, not a place: it starts a workout and pushes the logging screen, so it never
 * becomes the selected tab. History moved off the bar entirely — it is reached from Home's "See all".
 */
enum class BottomNavTab(val label: String) {
    Home("Home"),
    Log("Log"),
    Stations("Stations"),
    Profile("Profile"),
}
