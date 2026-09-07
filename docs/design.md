---
name: Kinetic Precision
colors:
  - surface: '#12140e'
  - surface-dim: '#12140e'
  - surface-bright: '#383a33'
  - surface-container-lowest: '#0d0f09'
  - surface-container-low: '#1a1c16'
  - surface-container: '#1e201a'
  - surface-container-high: '#292b24'
  - surface-container-highest: '#34352f'
  - on-surface: '#e3e3d9'
  - on-surface-variant: '#c4c8b7'
  - inverse-surface: '#e3e3d9'
  - inverse-on-surface: '#2f312b'
  - outline: '#8e9383'
  - outline-variant: '#44483b'
  - surface-tint: '#afd27d'
  - primary: '#cff39a'
  - on-primary: '#213600'
  - primary-container: '#b3d681'
  - on-primary-container: '#415d17'
  - inverse-primary: '#4a671f'
  - secondary: '#c2caaf'
  - on-secondary: '#2c3320'
  - secondary-container: '#444c37'
  - on-secondary-container: '#b4bca1'
  - tertiary: '#c0f0eb'
  - on-tertiary: '#003734'
  - tertiary-container: '#a4d4cf'
  - on-tertiary-container: '#2e5d59'
  - error: '#ffb4ab'
  - on-error: '#690005'
  - error-container: '#93000a'
  - on-error-container: '#ffdad6'
  - primary-fixed: '#cbef97'
  - primary-fixed-dim: '#afd27d'
  - on-primary-fixed: '#111f00'
  - on-primary-fixed-variant: '#334e07'
  - secondary-fixed: '#dee6ca'
  - secondary-fixed-dim: '#c2caaf'
  - on-secondary-fixed: '#171e0c'
  - on-secondary-fixed-variant: '#424935'
  - tertiary-fixed: '#bbece7'
  - tertiary-fixed-dim: '#a0d0cb'
  - on-tertiary-fixed: '#00201e'
  - on-tertiary-fixed-variant: '#1e4e4b'
  - background: '#12140e'
  - on-background: '#e3e3d9'
  - surface-variant: '#34352f'
## typography:
  ## display-lg:
    fontFamily: Inter
    fontSize: 57px
    fontWeight: '400'
    lineHeight: 64px
    letterSpacing: -0.25px
  ## display-md:
    fontFamily: Inter
    fontSize: 45px
    fontWeight: '400'
    lineHeight: 52px
    letterSpacing: 0px
  ## display-sm:
    fontFamily: Inter
    fontSize: 36px
    fontWeight: '400'
    lineHeight: 44px
    letterSpacing: 0px
  ## headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '400'
    lineHeight: 40px
    letterSpacing: 0px
  ## headline-md:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '400'
    lineHeight: 36px
    letterSpacing: 0px
  ## headline-sm:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '400'
    lineHeight: 32px
    letterSpacing: 0px
  ## title-lg:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '400'
    lineHeight: 28px
    letterSpacing: 0px
  ## title-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '500'
    lineHeight: 24px
    letterSpacing: 0.15px
  ## title-sm:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.1px
  ## body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0.5px
  ## body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0.25px
  ## body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
    letterSpacing: 0.4px
  ## label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.1px
  ## label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.5px
  ## label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.5px
## rounded:
  - sm: 0.25rem
  - DEFAULT: 0.5rem
  - md: 0.75rem
  - lg: 1rem
  - xl: 1.5rem
  - full: 9999px
## spacing:
  - none: 0dp
  - xs: 4dp
  - sm: 8dp
  - md: 16dp
  - lg: 24dp
  - xl: 32dp
  - xxl: 48dp
  - xxxl: 64dp
---

## Brand & Style
The design system is engineered for high-performance environments where speed and legibility are paramount. Centered on the "Kinetic Precision" personality, the aesthetic is dark, technical, and strictly utilitarian, drawing inspiration from professional aeronautics and developer-centric interfaces.

The style is a disciplined execution of **Material 3 (M3) Modernism**. It prioritizes high-contrast focal points against a deep obsidian backdrop, ensuring that critical data and actions remain readable under high cognitive load or physical stress. Visual flourishes are stripped back to favor functional clarity, structural integrity, and the systematic application of the Material 3 Compose framework.

## Colors
The color palette utilizes the Material 3 Dark Theme specification, mapped to the "Kinetic Green" core. Primary roles use a vibrant, desaturated green to ensure AAA accessibility against the "Deep Obsidian" surface.

- **Primary Roles:** Used for high-emphasis components and key action states.
- **Surface Roles:** The background is an deep obsidian neutral, reducing eye strain and providing a high-contrast base for technical data.
- **Tertiary Roles:** Used for contrasting accents or status-neutral information to prevent primary-color fatigue.
- **States:** Hover, focus, and pressed states should follow M3 state layer opacities (8%, 12%, and 12% respectively) applied as an overlay of the 'On' color.

## Typography
The typography is driven by **Inter**, chosen for its tall x-height and exceptional readability in digital interfaces. This design system maps directly to the standard Material 3 Type Scale.

- **Scale Usage:** For mobile devices, `display-lg` and `display-md` should be avoided for primary UI content, substituting with `headline-lg`.
- **Readability:** Body text leverages `body-lg` for primary content to ensure high visibility under stress.
- **Functional Labels:** `label-sm` is reserved for technical metadata or secondary captions that require minimal space without sacrificing font weight.

## Layout & Spacing
This design system employs a strict **8dp grid system** for all spacing and structural alignment.

- **Layout Model:** A fluid grid system is used. On mobile, use 4 columns with 16dp margins. On tablet, 8 columns with 24dp margins. On desktop, 12 columns with 24dp margins.
- **Gutters:** Gutters are fixed at 16dp across all breakpoints to maintain a dense, technical density.
- **Alignment:** All components must align their bounding boxes to the 8dp grid. Internal padding may use the 4dp (xs) increment for tighter component grouping (e.g., text within buttons).

## Elevation & Depth
Depth is expressed through **Tonal Layers** rather than physical shadows, following M3 Dark Theme best practices.

- **Surface Levels:** Higher elevation is represented by lighter surface overlays (Surface Container, Surface Container High, Surface Container Highest).
- **Shadows:** Avoid drop shadows for standard UI elements. Use a subtle `outline` color role (1dp stroke) for components like Cards and Text Fields to define boundaries against the deep obsidian background.
- **Elevation Overlay:** Use a primary-colored semi-transparent overlay on surfaces to indicate elevation levels 1 through 5, as per Compose `Surface` defaults.

## Shapes
The shape language follows the Material 3 scale to balance precision with modern interface trends.

- **Extra Small:** 4dp (Checkboxes, Text Fields bottom corners).
- **Small:** 8dp (Chips, Tooltips).
- **Medium:** 12dp (Cards, Small Dialogs).
- **Large:** 16dp (Extended FABs, Navigation Drawers).
- **Extra Large:** 28dp (Top App Bars, Large Dialogs).
- **Full:** Rounded/Pill (Buttons, Search Bars).

## Components
Components are styled to emphasize the "Kinetic Precision" theme:

- **Buttons:** Use the Filled Button for primary actions (Primary/On-Primary) and Outlined Button for secondary actions (Outline/Primary). All buttons are fully rounded (pill-shaped).
- **Input Fields:** Filled text fields with a 1dp bottom stroke. Use `surface_variant` for the container to ensure it stands out from the main `surface`.
- **Cards:** Use `surface_container_low` for Elevated Cards. For technical data lists, use Outlined Cards with a 1dp `outline` border to maintain a flat, structured appearance.
- **Chips:** Small (8dp) corner radius. Use for filtering and metadata tags.
- **Lists:** Clean, 1px `outline_variant` separators between list items. Use `body-lg` for headlines and `body-sm` for secondary supporting text.
- **Status Indicators:** Use `error` for critical alerts and `tertiary` for non-critical status updates to differentiate from primary action flows.
