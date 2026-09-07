package com.mindset.di

import io.kotzilla.generated.monitoring
import org.koin.dsl.KoinAppDeclaration

/**
 * Kotzilla Koin profiler, compiled in ONLY under `-Pmindset.profiler=true`.
 *
 * The counterpart in `src/noProfilerMain` is a no-op and is what every ordinary build gets. The
 * split is at the source-set level rather than a runtime flag because the Kotzilla SDK ships
 * consumer ProGuard rules (`-keep public class io.kotzilla.sdk.KotzillaSDK`, and friends) that make
 * its classes R8 entry points: once the artifact is on the classpath, no amount of dead-code
 * gating can shrink it out of a release build. Keeping it off the classpath is the only lever.
 */
public val appObservability: KoinAppDeclaration = { monitoring() }
