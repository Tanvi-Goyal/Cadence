package com.mindset.di

import org.koin.dsl.KoinAppDeclaration

/**
 * No-op observability — the default for every build. Swap in the real Kotzilla profiler for a
 * session with `-Pmindset.profiler=true`, which puts `src/profilerMain` on the source path and the
 * SDK on the classpath. See that file for why this is a compile-time and not a runtime switch.
 */
public val appObservability: KoinAppDeclaration = { }
