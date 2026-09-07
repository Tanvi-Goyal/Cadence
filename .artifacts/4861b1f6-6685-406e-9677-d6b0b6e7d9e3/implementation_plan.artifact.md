# Add Spotless for Code Formatting

This plan integrates Spotless into the MindSet project to enforce consistent code style across all modules (Kotlin, Gradle, Markdown, etc.).

## User Review Required

> [!NOTE]
> Spotless will be configured at the root level to cover all subprojects. This ensures that `./gradlew spotlessApply` works globally.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///Users/tanvigoyal/AndroidStudioProjects/Cadence/gradle/libs.versions.toml)
- Add `spotless` version.
- Add `spotless` plugin definition.

#### [MODIFY] [build.gradle.kts](file:///Users/tanvigoyal/AndroidStudioProjects/Cadence/build.gradle.kts)
- Apply Spotless plugin.
- Configure Kotlin formatting using `ktlint`.
- Configure Kotlin Script (`.gradle.kts`) formatting.
- Configure Markdown formatting.

### Style Configuration

#### [NEW] [.editorconfig](file:///Users/tanvigoyal/AndroidStudioProjects/Cadence/.editorconfig)
- Define standard Kotlin style rules (indent size, max line length, etc.).
- Ktlint uses this file for configuration.

## Verification Plan

### Automated Tests
- Run `./gradlew spotlessCheck` to verify compliance.
- Run `./gradlew spotlessApply` to automatically format all files.

### Manual Verification
- Verify that a purposefully misformatted file is corrected by `spotlessApply`.
