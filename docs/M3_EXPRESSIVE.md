# M3 Expressive: Design Language Guide

How Google intends Material 3 Expressive apps to **look, feel, and be built**. Read this before designing any screen. [DESIGN_SYSTEM.md](DESIGN_SYSTEM.md) holds the enforceable rules; this file explains the intent behind them.

Sources (checked 2026-11-13):
- [S1] Google Design research article: https://design.google/library/expressive-material-design-google-research
- [S2] Google launch post (May 13, 2025): https://blog.google/products-and-platforms/platforms/android/material-3-expressive-android-wearos-launch/
- [S3] Androidify, Google's reference Compose app (Android Developers Blog): https://android-developers.googleblog.com/2025/05/androidify-building-delightful-ui-with-compose.html · code: https://github.com/android/androidify
- [S4] M3 Expressive design language (Wear OS docs, general principles only): https://developer.android.com/design/ui/wear/guides/get-started/design-language
- [LIB] Inspected directly in `material3-android-1.5.0-rc01.aar` (javap). These are the token names that actually exist.
- [M3] m3.material.io, rendered with headless Chromium on 2026-11-13 (the site is client-rendered):
  - Motion: https://m3.material.io/styles/motion/overview/how-it-works
  - Easing & duration: https://m3.material.io/styles/motion/easing-and-duration/applying-easing-and-duration
  - Shape: https://m3.material.io/styles/shape/overview-principles
  - Typography: https://m3.material.io/styles/typography/overview
  - Color: https://m3.material.io/styles/color/system/overview
  - Components: https://m3.material.io/components
  - Blog "Building with M3 Expressive": https://m3.material.io/blog/building-with-m3-expressive
  - Corner radius scale: https://m3.material.io/styles/shape/corner-radius-scale · Shape morph: https://m3.material.io/styles/shape/shape-morph
  - Type scale tokens: https://m3.material.io/styles/typography/type-scale-tokens · Applying type: https://m3.material.io/styles/typography/applying-type
  - Color roles: https://m3.material.io/styles/color/roles · Dynamic (user-generated): https://m3.material.io/styles/color/dynamic/user-generated-source
  - Elevation: https://m3.material.io/styles/elevation/overview · Icons: https://m3.material.io/styles/icons/overview
  - Breakpoints: https://m3.material.io/foundations/layout/applying-layout/window-size-classes · Canonical layouts: https://m3.material.io/foundations/layout/canonical-layouts/overview
  - States: https://m3.material.io/foundations/interaction/states/overview · Accessibility: https://m3.material.io/foundations/designing/overview
  - ⚠️ Not yet read (rendering timed out twice): https://m3.material.io/styles/motion/transitions/transition-patterns

---

## 1. Intent: what "expressive" means
- "Expressive design makes you feel something." It "inspires emotion, communicates function, and helps users achieve their goals." [S1]
- Built from **five levers: color, shape, size, motion, and containment**, used for "making key actions stand out and grouping like elements together." [S1]
- It is research-backed: 46 studies, more than 18,000 participants. In eye-tracking tests, users spotted key UI elements "up to four times faster", and users over 45 performed on par with younger users. [S1]
- The feel: "more natural, springy animations meant to bring a moment of delight to everyday routines." [S2]
- It builds on Material You's personalization through dynamic color. [S2]

### ⚠️ Restraint matters, especially for a finance app
- "Context still matters… What works in a media player or email app might not be suitable for something like a **banking interface**." [S1]
- "Don't compromise your product's core functionality for visual flourishes." "No amount of emotion can compensate for a lack of clarity." [S1]
- Breaking familiar patterns hurt usability. Removing the vertical list in a playlist redesign and removing text labels from actions both made usability worse. [S1]
- "A strong minority of users preferred calmer, less intense versions." [S1]

**Fiscus rule:** be expressive in **emphasis and delight**: the balance hero, the primary "add" action, positive moments like a budget met, and transitions. Be calm and conventional in **data**: transaction lists, amounts, and forms stay readable, labeled, and familiar. Numbers must never be decorative.

### The 7 expressive tactics (official, [M3] blog)
M3 Expressive "isn't a new version of the system… this isn't 'M4'". It is a set of tactics, each "one axis along which you can make your components, layouts, and products more expressive":
1. **Use a variety of shapes.** Mix classic and abstract shapes. Do: "Break from the surrounding shape style to draw attention to a particular element." Caution: "Smaller shapes can result in essential actions looking less important."
2. **Apply rich and nuanced colors.** Build hierarchy with surface tones, and use contrast between primary, secondary, and tertiary to prioritize actions. Caution: "Without contrast, elements can blend together."
3. **Guide attention with typography.** Use emphasized styles for important headlines and actions. Heavier weight, size, color, and spacing direct attention.
4. **Contain content for emphasis.** Group content logically. Give the most important content "ample space and the brightest surface mapping." Caution: "Ungrouped information can blend together."
5. **Add fluid and natural motion.** Use shape morph, surface effects, and expressive springs.
6. **Leverage component flexibility.** Adapt UI to context and screen size (canonical layouts).
7. **Combine tactics to create hero moments.** "Stick to **one or two hero moments** in your product; too many moments can be overwhelming." Ask: is this interaction emotionally impactful? Is it a key interaction?

**Fiscus hero moments (ADR-010, accepted):** (1) the balance/overview hero on Home, (2) the add-transaction flow (FAB menu, then a satisfying save). Everything else stays standard M3.

Official Expressive component set [M3 blog]: app bars, **button groups** (new), common buttons, extended FAB, **FAB menu** (new), FABs, icon buttons, **loading indicator** (new), navigation bar, navigation rail, progress indicators, sliders, **split button** (new), **toolbars** (new).

## 2. Color
- Use M3's deeper tonal palettes and full role set. Emphasis comes from **role pairs** (container + on-container), not custom hex values. [S4][S1]
- In the research, the key action was made to stand out with a **secondary-color** container and a larger size. [S1]
- Dynamic color is part of the identity. Always support it, with a branded fallback. [S2]
- Contrast: the research "exceed[ed] existing standards for… color contrast." Aim above WCAG AA where you can. [S1]
- Available roles [LIB]: primary/secondary/tertiary (+Container, +Fixed, +FixedDim, +On…), error (+Container), the surface family (`surfaceDim`, `surfaceBright`, `surfaceContainerLowest…Highest`), outline/outlineVariant, inverse*, scrim. `expressiveLightColorScheme()` exists. Use surface-container tiers to build hierarchy without shadows.

### From m3.material.io [M3]
- The system has: accessible color relationships built in, **26+ color roles** mapped to components, a built-in dark theme, a static baseline scheme, and dynamic color (wallpaper-based and **content-based**).
- **Three contrast levels** (standard, medium, high) that users can pick; contrast is tokenized (May 2025). Support all three.
- "Specific colors, such as **semantic colors**, can be set to not dynamically change." Use this for income/expense if their meaning must stay stable.
- **Fixed colors** (primaryFixed etc.) "keep the same color value in light and dark themes."
- Surfaces are **tone-based roles** (surfaceContainer…), not elevation overlays.
- Tool: Material Theme Builder generates the scheme. Design with **roles, not hex values**, because dynamic color changes the actual colors.
- **Role usage** [M3 roles]:
  - **Primary**: the most important actions (FAB, high-emphasis buttons, active states). **Secondary**: less prominent elements (filter chips, tonal buttons, nav selection). **Tertiary**: contrasting accents for small elements that need special attention (badges, input fields), "at the designer's discretion."
  - **Container** roles are fills for components and are never used for text or icons. **On-X** roles are text and icons on X. Only use the intended pairs; improper pairings break under the contrast levels.
  - **Error** is static: it doesn't change with dynamic color, but adapts to light and dark.
  - **Surface** is for backgrounds. Surface containers (lowest to highest) are for cards, sheets, and dialogs, and build hierarchy. Common pattern: body uses `surface`, navigation uses `surfaceContainer`. Keep these mappings the same across breakpoints.
  - Default text color is `onSurface`, or `onSurfaceVariant` for lower emphasis.
  - **Outline** is for important boundaries (text field border). **OutlineVariant** is for dividers and decoration. Don't use outline for dividers or card borders.
  - **Fixed** colors and surface dim/bright are add-ons: "If you aren't sure whether your product should use the add-on roles, it probably shouldn't." Avoid fixed colors where contrast is needed.
  - Paired roles give a minimum 3:1 contrast. Text needs **4.5:1** (small) or **3:1** (large) [M3 applying type].

### Elevation [M3 elevation]
- M3 shows elevation with **tonal surface color instead of shadows**. Use shadows only for extra protection or to invite interaction.
- "Avoid changing the default elevation of Material 3 components." Use a small number of levels.

## 3. Shape
- Shape is "much more expansive and meaningful". Rounding and sharpening of corners supports **shape-morphing** buttons, lists, and states. [S4]
- Corner shapes create "variety, distinction, and relationship between container shapes." [S4]
- **Shape scale** [LIB]: `extraSmall`, `small`, `medium`, `large`, `largeIncreased`, `extraLarge`, `extraLargeIncreased`, `extraExtraLarge`. New in Expressive [M3]: large increased **20dp**, extra large increased **32dp**, extra extra large **48dp**. Fully rounded corners now use "full" instead of 50%.
- **Shape principles** [M3]:
  - Use shapes and text in harmony. M3 shapes echo the roundness of Google Sans Flex.
  - Morph shapes to connect function and feeling: interaction states (selected), actions in progress (loading, typing), environment changes. Morphing "should respond to user interaction."
  - Be bold and embrace tension: mix round and sharp shapes to convey state or draw attention.
  - **Shape is versatile, not semantic**: don't give a single shape a fixed meaning.
  - **Use abstract shapes sparingly**: "Don't compromise clarity for the sake of visual design." "Shapes without clear meaning… can add more visual clutter than delight."
  - Use expressive shapes most freely in decorative, non-interactive spots: imagery, avatar masks, graphics.
  - Shape plus motion can make 2D feel 2.5D (depth).
- **Full corner scale** [M3 corner radius]: none 0 · extra small 4 · small 8 · medium 12 · large 16 · large increased 20 · extra large 28 · extra large increased 32 · extra extra large 48 · full (dp). There are also asymmetric variants (top, start, end) and **inner corners** for tightly grouped items (menus, split buttons).
- Customize shape by remapping a component to a different style on the scale, not with arbitrary dp values. Customizing is "encouraged for hero moments."
- ⚠️ "Be careful not to apply large or full corners to **information-dense components**, such as cards."
- **Optical roundness**: for nested shapes, inner radius = outer radius − padding (for example 48 − 14 = 34dp). Never use the same radius for nested objects.
- The shape library is "for mostly visual elements. Avoid applying unconventional shapes to text-heavy containers."
- Shape morph is built into the **standard button group** and the **loading indicator**, using the expressive motion scheme by default [M3 shape morph].
- **MaterialShapes library** (35 shapes) [LIB]: Arch, Arrow, Boom, Bun, Burst, Circle, ClamShell, Clover4Leaf, Clover8Leaf, Cookie4/6/7/9/12Sided, Diamond, Fan, Flower, Gem, Ghostish, Heart, Oval, Pentagon, Pill, PixelCircle, PixelTriangle, Puffy, PuffyDiamond, SemiCircle, Slanted, SoftBoom, SoftBurst, Square, Sunny, Triangle, VerySunny.
  - Use them for **accents** (avatars and category icons, the hero button, loading, empty states), not for data containers. Apply with `.clip(MaterialShapes.Cookie9Sided.toShape())` [S3]. Requires `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` (see REFERENCES).
- Components morph shape on press or selection by themselves (button groups, toggle buttons, FAB menu). Rely on that instead of hand-animating corners.

## 4. Typography
- Typography is "emphasized": variable font axes such as weight and width are used "for more expression." [S2][S4]
- **Emphasized styles** exist for every role [LIB]: `displayLargeEmphasized` … `labelSmallEmphasized` (30 styles total).
  - Use **Emphasized** for the one thing that matters on a screen (the balance, a screen title, a key total). Use regular styles for everything else. Too much emphasis means no emphasis.
- Variable fonts (Roboto Flex is cited) can animate weight or width as feedback. [S4] Using a custom font is an ADR decision.
- From m3.material.io [M3]:
  - "M3 type scale has 30 type styles: 15 baseline and 15 emphasized." "The emphasized type styles add more expression to highlighted moments" and give "clearer hierarchies and prioritized components."
  - Five roles: display, headline, title, body, label (each small/medium/large).
  - "Roboto Flex can be used on its own… but is **not yet part of the M3 typescale**." Google Sans Flex is the M3 Expressive brand face (six variable axes).
  - Aug 2026: the type scale can adapt line height to the **language script height** (small to extra large). Don't hardcode line heights; use the tokens.
  - Use type **tokens**, not fixed values.
- **Role usage** [M3 applying type]:
  - **Display** is for short, important text or **numerals**, the largest on screen. Good for the balance hero.
  - **Headline** is short, high-emphasis text. **Title** is medium-emphasis text such as section headers and app bars; be cautious with expressive fonts here.
  - **Body** is long text, never in decorative fonts. **Label** is component text and captions; buttons use labelLarge.
- **Emphasized styles** [M3 type tokens]: "Material components don't use emphasized type styles by default." Swap them in for selected list or menu items, primary-action buttons, badges, extended FABs, unread or selected states, and headlines. Baseline and emphasized are "meant to be used together."
- **Tabular numbers**: "Use tabular figures (monospaced numbers)… where values may change often" so values stay aligned and the layout doesn't shift. **Fiscus: all amounts use tabular figures** (`fontFeatureSettings = "tnum"`).
- Line height is about 1.2× for title, headline, and display, and about 1.5× for body and label (the tokens already handle this). Keep text to **40–60 characters per line** across breakpoints.
- Customizing: change the brand and plain typeface tokens, line height, and tracking, but **avoid changing type sizes**, since that breaks component rendering. Brand typeface for display and headline, plain typeface for body and label (default: Roboto for both).
- Links: primary color (tertiary for less prominent) and always underlined.

## 5. Motion
- Motion is a **physics-based (spring) system built into the theme**. Use `MaterialExpressiveTheme(motionScheme = MotionScheme.expressive())`; expressive is the default there. [S3]
- "Wherever possible… leverage the Material 3 Expressive MotionScheme to obtain a themed motion token, creating a consistent motion feeling throughout the app." [S3]
- **Tokens** [LIB]: `MaterialTheme.motionScheme.`
  - **Spatial** (position, size, scale, rotation; may overshoot or bounce): `fastSpatialSpec()`, `defaultSpatialSpec()`, `slowSpatialSpec()`
  - **Effects** (color, alpha; no overshoot): `fastEffectsSpec()`, `defaultEffectsSpec()`, `slowEffectsSpec()`
  - Shared elements: `motionScheme.sharedElementTransitionSpec` [S3]
- Pick the speed by the size of the change: small (fast), component-level (default), full-screen (slow).
- From m3.material.io [M3]:
  - The motion physics system (springs) **replaces** easing and duration. Easing and duration is "no longer maintained", but is "still used for transitions."
  - **Expressive** scheme: "Material's opinionated motion scheme… should be used for most situations, particularly hero moments and key interactions." It overshoots and bounces. **Standard** scheme: "more functional with minimal bounce… for utilitarian products."
  - A spring has three attributes: stiffness (higher means faster), damping (1 means no bounce), and initial velocity.
  - **Spatial** tokens are for x/y, rotation, size, and rounded corners (they overshoot). **Effects** tokens are for color and opacity (no overshoot).
  - Speed: "Most motion should use the default speed". Fast for small components (switches, buttons). Default for partial-screen (bottom sheet, expanded rail). Slow for full-screen animations and refreshes.
  - "All component motion is driven by two tokens: expressive fast spatial and expressive fast effects." In Compose, 21 components use the physics system by default.
  - Customization levels: **(1)** use a default scheme (should be sufficient), **(2)** a custom `MotionScheme`, **(3)** swap the scheme for a specific element by overriding the CompositionLocal.
  - **Fiscus:** expressive app-wide (level 1). Level 3 (standard for dense data views) needs an ADR.
  - Easing and duration (legacy, for transitions): Emphasized 500ms (begin and end on screen), Emphasized decelerate 400ms (enter), Emphasized accelerate 200ms (exit permanently), Standard 300/250/200ms. Exits are shorter than enters, and duration scales with the area covered.
- Motion responds to touch. For example, neighboring notifications "subtly respond to your drag" with haptics. [S2] Pair key interactions with haptics.

## 6. Containment, size, and layout
- Use "high-contrast visual containment" and group related elements together. [S1]
- **Size signals importance.** The research made the primary button bigger and placed it within thumb reach, which made it faster to find "without overwhelming other items." [S1]
- Grouped containers adapt to the space available, distributing it evenly or deliberately to set hierarchy. [S4]
- Keep familiar structures: lists stay lists, and actions keep their text labels. [S1]

## 6b. Adaptive layout [M3 breakpoints, canonical layouts]
- Breakpoints (width): **compact** < 600dp (phone portrait), **medium** 600–839 (tablet or foldable portrait), **expanded** 840–1199 (phone landscape, tablet landscape), **large** 1200–1599, **extra-large** 1600+.
- Panes: one for compact and medium; two recommended for expanded and large; up to three for extra-large. Don't use two panes in medium for dense information.
- Swap equivalents: navigation bar (compact) becomes a collapsed rail (medium/expanded), then an expanded rail (large). Bottom sheet (compact) becomes a menu (medium+). Full-screen dialog (compact) becomes a basic dialog. Only swap components that do the same job.
- Canonical layouts: **feed**, **list-detail**, **supporting pane**. **Fiscus:** transactions use list-detail; insights could use a supporting pane.
- Design for breakpoints, not devices. Ask what to reveal, divide, resize, reposition, or swap.

## 6c. States, icons, accessibility
- Interaction states: enabled, disabled, hover, focused, pressed, dragged. They are shown with state layers and must be applied consistently. M3 components handle this automatically.
- Icons: **Material Symbols** (outlined, rounded, or sharp; variable weight, fill, grade, and optical size). Pick **one style** for the whole app.
- Accessibility: "By using standard platform controls… apps automatically contain the markup… needed to work well with assistive technology." Avoid non-standard elements. This supports our M3-components-only rule. Support TalkBack, keyboard, and switch access.

## 7. Components Google highlights
Official catalog [M3] (m3.material.io/components), grouped by purpose:
- **Buttons**: button groups, buttons, extended FABs, FAB menu, FABs, icon buttons, segmented buttons, split buttons
- **Date & time pickers**: date pickers, time pickers
- **Loading & progress**: loading indicator, progress indicators
- **Navigation**: navigation bar, navigation drawer, navigation rail
- **Sheets**: bottom sheets, side sheets
- **Others**: app bars, badges, cards, carousel, checkbox, chips, dialogs, divider, lists, menus, radio button, search, sliders, snackbar, switch, tabs, text fields, toolbars, tooltips

Google highlights:
Floating toolbar (studied as "modern, clean, and energetic as well as noticeable and usable"), wavy progress indicators (studied for making waiting "feel faster"), larger and shape-morphing buttons, adaptive button groups, shape-morphing lists, FAB menu, flexible app bars, and loading indicators. [S1][S3][S4] See the component map in DESIGN_SYSTEM.md.

## 8. Reference implementation pattern (from Androidify [S3])
```kotlin
MaterialExpressiveTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = shapes,
    motionScheme = MotionScheme.expressive(),
) {
    SharedTransitionLayout { /* provide scope via CompositionLocal */ content() }
}
```
- Press feedback: scale with `MaterialTheme.motionScheme.defaultSpatialSpec<Float>()`.
- Hero shapes: `MaterialShapes.*.toShape()`.
- Screen-to-screen: shared bounds with `motionScheme.sharedElementTransitionSpec`.
- ⚠️ Androidify's `sharedBoundsRevealWithShapeMorph` / `MorphOverlayClip` is **custom code, not a library API**. Under our rules it needs user approval before we use something similar.

## 9. Checklist for every screen
- [ ] One clear hero or primary action, emphasized with size, color role, or an emphasized type style
- [ ] Data (amounts, lists, forms) is calm, labeled, and familiar
- [ ] Every animation uses `motionScheme` tokens, spatial vs effects chosen correctly
- [ ] Shapes come from the shape scale or MaterialShapes; decorative shapes only on accents
- [ ] Color comes from roles only; works with dynamic color on and off, light and dark and in standard, medium, and high contrast
- [ ] Expressive shapes only where they carry meaning or decoration (not on data); motion uses default speed unless the element is small or full-screen
- [ ] Touch targets ≥ 48dp, contrast at AA or better, text labels on actions
- [ ] Is this one of the 1–2 hero moments? If not, keep it standard
- [ ] Amounts use tabular figures; emphasized type only for selected or primary or key items
- [ ] Color roles used only in their intended pairs (container ↔ on-container); outlineVariant for dividers
- [ ] No large or full corners on information-dense cards; nested corners use optical roundness
- [ ] Default component elevation unchanged; hierarchy via surface-container tiers
- [ ] Layout works at compact, medium, and expanded (canonical layout chosen); one icon style
