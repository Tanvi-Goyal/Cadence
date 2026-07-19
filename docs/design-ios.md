---
name: Kinetic Precision iOS
## colors:
  - surface: '#131313'
  - surface-dim: '#131313'
  - surface-bright: '#393939'
  - surface-container-lowest: '#0e0e0e'
  - surface-container-low: '#1b1b1b'
  - surface-container: '#1f1f1f'
  - surface-container-high: '#2a2a2a'
  - surface-container-highest: '#353535'
  - on-surface: '#e2e2e2'
  - on-surface-variant: '#bccbb9'
  - inverse-surface: '#e2e2e2'
  - inverse-on-surface: '#303030'
  - outline: '#869585'
  - outline-variant: '#3d4a3d'
  - surface-tint: '#4ae176'
  - primary: '#4be277'
  - on-primary: '#003915'
  - primary-container: '#22c55e'
  - on-primary-container: '#004b1e'
  - inverse-primary: '#006e2f'
  - secondary: '#4edea3'
  - on-secondary: '#003824'
  - secondary-container: '#00a572'
  - on-secondary-container: '#00311f'
  - tertiary: '#afc7ff'
  - on-tertiary: '#002e6a'
  - tertiary-container: '#82abff'
  - on-tertiary-container: '#003d88'
  - error: '#ffb4ab'
  - on-error: '#690005'
  - error-container: '#93000a'
  - on-error-container: '#ffdad6'
  - primary-fixed: '#6bff8f'
  - primary-fixed-dim: '#4ae176'
  - on-primary-fixed: '#002109'
  - on-primary-fixed-variant: '#005321'
  - secondary-fixed: '#6ffbbe'
  - secondary-fixed-dim: '#4edea3'
  - on-secondary-fixed: '#002113'
  - on-secondary-fixed-variant: '#005236'
  - tertiary-fixed: '#d8e2ff'
  - tertiary-fixed-dim: '#adc6ff'
  - on-tertiary-fixed: '#001a42'
  - on-tertiary-fixed-variant: '#004395'
  - background: '#131313'
  - on-background: '#e2e2e2'
  - surface-variant: '#353535'
## Typography
  ## large-title:
    fontFamily: SF Pro Display
    fontSize: 34px
    fontWeight: '700'
    lineHeight: 41px
    letterSpacing: 0.37px
  ## title-1:
    fontFamily: SF Pro Display
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 34px
    letterSpacing: 0.36px
  ## title-2:
    fontFamily: SF Pro Display
    fontSize: 22px
    fontWeight: '700'
    lineHeight: 28px
    letterSpacing: 0.35px
  ## headline:
    fontFamily: SF Pro Text
    fontSize: 17px
    fontWeight: '600'
    lineHeight: 22px
    letterSpacing: -0.41px
  ## body:
    fontFamily: SF Pro Text
    fontSize: 17px
    fontWeight: '400'
    lineHeight: 22px
    letterSpacing: -0.41px
  ## callout:
    fontFamily: SF Pro Text
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 21px
    letterSpacing: -0.32px
  ## subheadline:
    fontFamily: SF Pro Text
    fontSize: 15px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: -0.24px
  ## footnote:
    fontFamily: SF Pro Text
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 18px
    letterSpacing: -0.08px
  ## caption-1:
    fontFamily: SF Pro Text
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
    letterSpacing: 0px
  ## monospaced-data:
    fontFamily: SF Mono
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 18px
    letterSpacing: -0.1px
## rounded:
  - sm: 0.25rem
  - DEFAULT: 0.5rem
  - md: 0.75rem
  - lg: 1rem
  - xl: 1.5rem
  - full: 9999px
## spacing:
  - margin-horizontal: 16px
  - stack-gap: 8px
  - section-spacing: 24px
  - touch-target-min: 44px
---

## Brand & Style
The design system bridges high-performance technical aesthetics with the refined structure of the Apple Human Interface Guidelines. The personality is precise, urgent, and sophisticated, targeting professional users who require data density without sacrificing clarity. 

The style utilizes **Dark Mode Minimalism** as its foundation, enhanced by **Glassmorphism** for structural depth. It adheres to iOS-native metaphors—respecting safe areas and standard interaction patterns—while maintaining a "technical-grade" feel through vibrant accents and rigorous alignment. The goal is a UI that feels like a precision instrument integrated directly into the OS.

## Colors
The palette is rooted in a deep, absolute black (`#000000`) base to maximize the efficiency of OLED displays and provide a high-contrast backdrop for technical data.

- **Primary (Kinetic Green):** Used for primary actions, progress indicators, and active states. It functions as the system tint color.
- **Secondary/Tertiary:** Used for data visualization and distinguishing different data streams (e.g., performance metrics vs. connectivity).
- **Surface Colors:** Instead of grays, the design system uses iOS system materials (Thin, Regular, Thick) to create hierarchical depth through translucency.
- **Support Colors:** System Red for destructive actions and System Amber for warnings, consistent with HIG standards.

## Typography
Typography follows the Apple San Francisco (SF) scale to ensure legibility and platform consistency. 

- **SF Pro Display** is used for Title levels and above to provide a clean, high-tech impact.
- **SF Pro Text** handles all body and interactive labels, utilizing dynamic type scaling.
- **SF Mono** is introduced specifically for numerical data, coordinates, and timestamps to emphasize the "precision" aspect of the brand.
- **Tracking:** Use tighter tracking for larger display sizes and standard tracking for body text as per Apple's official specifications.

## Layout & Spacing
The layout relies on the **Inset Grouped** model. Content is organized into cards or list rows with rounded corners that do not touch the screen edges, creating a clear distinction between the background and interactive content.

- **Grid:** Use a flexible column system that adheres to a 16pt side margin on iPhone and 24pt on iPad.
- **Vertical Rhythm:** Follow an 8pt grid for vertical spacing between elements within a group, and 24pt-32pt between distinct sections.
- **Navigation:** Standard iOS Navigation Bar (Large Title transitions to Inline Title on scroll) and a Bottom Tab Bar for primary application destinations.

## Elevation & Depth
Depth is communicated through **Z-axis stacking and Materiality** rather than traditional drop shadows.

- **Base Layer:** Pure black background (`#000000`).
- **Surface Layer:** Inset Grouped cells use a "Secondary System Background" or a custom dark gray (`#1C1C1E`).
- **Floating Elements:** Modals and action sheets use the `systemUltraThinMaterial` blur effect, allowing the background colors to softly bleed through, maintaining context.
- **Borders:** Use 0.5pt hair-line strokes (System Separator color) for list dividers and card boundaries to emphasize the "precision" aesthetic.

## Shapes
Shapes follow the "Continuous Corner" (squircle) geometry native to iOS.

- **Containers:** Standard Inset Grouped rows use a 10pt-12pt corner radius.
- **Buttons:** Large action buttons use a 12pt radius or are fully pill-shaped if used as floating elements.
- **Inputs:** Text fields use a 10pt radius to match the container language.
- **Selection:** Use rounded rectangles for selection states in menus, never sharp corners.

## Components
- **Buttons:** Use `UIButton.Configuration.filled()` for primary actions with Kinetic Green backgrounds and black text. Use `gray()` or `tinted()` configurations for secondary actions.
- **Lists:** Adhere strictly to Inset Grouped style. Row height should be a minimum of 44pt. Use chevron-right accessories for drill-down navigation.
- **Inputs:** Standard iOS text fields with a clear button. Place descriptive labels above the field in `footnote` size or use the inline label style.
- **Chips/Segments:** Use the native `UISegmentedControl` for toggling views. For filtering, use small rounded-pill chips with a 1px border.
- **Cards:** For data dashboards, use custom cards with the `systemBackground` color and a subtle 0.5pt stroke.
- **Navigation Bars:** Translucent with Kinetic Green for interactive elements (Back buttons, Edit, Done).