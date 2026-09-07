package com.mindset.domain

/** The user's subscription entitlement. [isPro] unlocks the Pro-gated surfaces (full History, etc.). */
data class Entitlement(val isPro: Boolean = false)
