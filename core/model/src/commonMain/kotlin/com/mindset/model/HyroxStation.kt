package com.mindset.model

/** The 8 Hyrox stations. A non-null value on [Exercise.hyroxStation] tags it as a Hyrox station. */
enum class HyroxStation(val text: String) {
    SKI_ERG("Ski Erg"),
    SLED_PUSH("Sled Push"),
    SLED_PULL("Sled Pull"),
    BURPEE_BROAD_JUMP("Burpees"),
    ROWING("Rowing"),
    FARMERS_CARRY("Farmers"),
    SANDBAG_LUNGES("Lunges"),
    WALL_BALLS("Wall Balls"),
}
