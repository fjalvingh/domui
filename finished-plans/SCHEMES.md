# Colour schemes: every theme variant is a scheme of a nature

Status: **finished** 2026-10-07 - all five phases (see 5.1-5.4). What is left open is in
section 6. Follows `finished-plans/darktheme.md` §8,
which made a theme review page and four dark schemes and then put those schemes into the
framework as `DarkSchemeVariant`s, next to the old light and dark variants.

## 1. Why

That result is lopsided. Light is the theme itself, the "empty" variant `default`. Dark is
an override directory with its own literal colour files. The schemes are overrides of dark,
which compile through a template that is a copy of dark's files. So there are three ways of
being a variant, and the template has to follow the dark files whenever they change.

The goal is one way: every selectable variant is a **scheme**, and every scheme belongs to a
**nature**, light or dark. The directory a scheme sits in, and its name, say which nature it
has.

## 2. Decisions

1. **Three concepts, one place each.**

   | Concept | Decides | Lives in |
   | --- | --- | --- |
   | theme (`winter`) | structure: component sheets, layout, fonts, `_component-colors.scss` | `winter/` |
   | nature (`light`, `dark`) | how the theme's colours are worked out from a scheme's tokens (`_palette.scss`), and the images that differ | `winter/light/`, `winter/dark/` |
   | scheme | the base tokens, `_scheme.scss` | `winter/<nature>/<scheme>/` |

2. **The variant name is `<nature>-<scheme>`**, for instance `light-winter`, `dark-midnight`,
   `dark-nord`. It is what themed resource URLs carry (`$THEME/dark-nord/style.scss`), and
   it is enough by itself to know the nature: no registry is needed to restore a variant.
3. **Every variant has the same search path:**
   `winter/<nature>/<scheme>`, `winter/<nature>`, `winter`, `all`.
4. **An unrecognised variant name** - in the cookie, the session, a URL or anywhere else -
   **becomes the first light scheme** of the application's list. This also covers the old
   names `default`, `dark` and `scheme-*`: no aliases are kept.
5. **Midnight is the first dark scheme**, so it is what colour-scheme detection picks for a
   browser that prefers dark. The detection picks the first scheme of the wanted nature in
   `DomApplication.getThemeVariants()`.
6. **`_component-colors.scss` is one nature-neutral file**: it maps every component colour to
   a role of the palette. A colour that a nature or a scheme wants different is an
   **exception** (decided 2026-10-07, option 1 of three): plain declarations, configured into
   the theme below the application's custominit files, in `_nature-exceptions.scss` (every
   scheme of a nature) and `_scheme-exceptions.scss` (one scheme, outranks the nature). The
   exceptions are visible debt: each one removed is a choice for the role instead.
   (Expressing light's per-component colours as palette roles instead would have taken ~139
   new palette colours with one user each; see 5.1.)
7. **Light stays faithful** - for now byte for byte, through its exceptions. **Darcula may
   change**: it is an ordinary scheme of the dark palette, with no exceptions of its own.

## 3. The intended layout

```
winter/                            style.scss, component sheets, _component-colors.scss, neutral images
winter/light/                      _palette.scss (the light template), light images
winter/light/winter/_scheme.scss   today's light colours, as tokens
winter/dark/                       _palette.scss (the dark template), dark images
winter/dark/midnight/_scheme.scss
winter/dark/darcula/_scheme.scss
winter/dark/lagoon/_scheme.scss
winter/dark/violet/_scheme.scss
winter/dark/nord/_scheme.scss
```

An application's own files follow the same layout in its webapp: `winter/_custominit.scss`
for every variant, `winter/<nature>/_variant-custominit.scss` for a nature,
`winter/<nature>/<scheme>/` for a scheme of its own or for overriding one of DomUI's.

## 4. Java

- One variant class (replacing `DefaultThemeVariant`, `DarkThemeVariant` and
  `DarkSchemeVariant`): nature, scheme name, label. Constants for DomUI's schemes.
- `IThemeVariant.of(name)` parses `<nature>-<scheme>`; `DomApplication.findThemeVariant`
  returns the listed instance (for its label), and the first light scheme for a name that is
  not listed.
- `SassThemeFactory` builds the one search path of 2.3; `IThemeFactory.getVariants()` and
  `DomApplication.getThemeVariants()` stay. The default variant is the first light scheme.
- `getThemeVariantForColorScheme(nature)`: the first listed scheme of that nature.
- `ThemeColor`, the colour-scheme meta tag and the demo's moon/sun switch already go by the
  variant's colour scheme, which now always comes from the name.

## 5. Phases

1. **Nature-neutral component colours.** Move the literals of `winter/_component-colors.scss`
   and `dark/_component-colors.scss` into the two palettes; one `_component-colors.scss` is
   left. Verify: both compiled sheets are identical to before.
2. **The light palette as a template.** The light palette reads a light scheme's tokens, the
   way the dark one does; the light sheet stays identical. (Darcula became a scheme in phase 1,
   and may change, so the dark tokens did not need extending.)
3. **Layout, names and Java.** Move the files into the layout of section 3 (the images with a
   dark copy move into `light/`), the Java model of section 4, the search path, unrecognised
   names. Update `TestThemeVariants`, `TestThemeContrast`, `ThemeVariantCompiler`,
   `ThemeColorReport`.
4. **The demo.** Review page bar from `getThemeVariants()`, `?scheme=dark-nord`, the switch.
5. **Documentation.** The theming pages on the site (`look-and-feel/themes`), `THEMES.md` and
   `darktheme.md` §8 pointing here, `IMPROVEMENT-LOG.md`.

### 5.1 Phase 1, as done (2026-10-07)

- `winter/_component-colors.scss` is the one component file: the role mapping the scheme
  template had (`s.$token` → the palette role), with light's own formula where the template had
  a literal for every dark scheme. `dark/_component-colors.scss` and the `scheme/` directory are
  gone. One variable branches on the nature: `$button-color-shades`
  (`if($color-scheme == dark, ...)`), because the dark nature gives every colour button one
  focus colour where light computes one per colour - that depends on scheme colours, so it
  cannot be a literal exception. `$colors`' link entry reads two new component colours,
  `$button-link-color` / `$button-link-invert`.
- Twelve new palette roles, in both palettes: `$fill-strong-bg`, `$stripe-bg`,
  `$text-bright-color`, `$text-dim`, `$header-strip-bg`, `$header-tab-bg`, `$header-band-bg`,
  `$header-edge-color`, `$header-bar-color`, `$heading-color`, `$heading2-color`,
  `$selection-bg`. Light's values were taken from its own component colours, so that as few
  as possible need an exception.
- `dark/_palette.scss` is the former template; `dark/_scheme.scss` is Darcula's scheme (its
  tokens are the values the old dark files used most for each), so the variant `dark` is now
  a scheme like the others.
- `winter/_scheme-exceptions.scss`: light's **109** exceptions, as the values they compiled
  to (an exception cannot read theme variables, so the 32 colours light calculated are frozen
  there). `dark/_nature-exceptions.scss`: **20**, the literals every dark scheme shares (the
  light theme's button hues and their inverts, the flares). Empty defaults for the others.
- `_theme-configuration.scss` gathers the configuration (exceptions, then the application's
  files); `style.scss` uses it, and so does the test compiler's new `compileConfigured`, so
  `TestThemeContrast` measures the colours a page really gets.
- **Verified:** the compiled sheets of light and of all four schemes are byte for byte the
  sheets of before (`ThemeColorReport` now writes every variant); all six variants compile in
  the running demo; the module's tests pass.
- **Darcula changed:** everything the template decides for every dark scheme (the light
  theme's orange accent, buttons and hints, mixed washes); and three values, for contrast:
  red `#E06C6C` (was `#BC3F3C`), error text `#FF8A87` (was `#FF6B68`), marked-row wash 20%
  instead of 30%.
- `TestThemeContrast` now checks every dark scheme. That found **Nord**'s selected-row text at
  4.31: its `$selection` is now `#48658C` (was `#4C6A92`).
- `TestThemeVariants`: the two palettes declare the same colours; every exception names a
  component colour that exists.

### 5.2 Phase 2, as done (2026-10-07)

- `winter/_palette.scss` is the light nature's palette: it reads `winter/_scheme.scss`, the
  light scheme "winter", through `@use "scheme" as s`. 49 of its colours read a token; the
  others are the light theme's own choices for every light scheme (accent, button gradient,
  state washes, greyscale ramp), or colours that share a token in the dark palette but have a
  value of their own in light (`$errors-border` `#ff0000` next to `$red`, for instance).
- The light scheme has the dark schemes' tokens but one: `hint-strong`. Light's selected item
  follows the accent (`$selected-bg: $primary`), so that an application that sets `$primary`
  still moves it.
- Token values are written out: where light read the greyscale ramp (`$text-color:
  $grey-dark`), the token is the ramp's value (`$text: hsl(0, 0%, 29%)`). An application that
  sets `$grey-dark` no longer moves `$text-color`; one that sets `$text-color` still does.
- `TestThemeVariants.schemesDeclareTheirNaturesTokens`: every scheme declares exactly the
  tokens its nature's palette reads.
- **Verified:** the light sheet and Midnight, Lagoon and Violet are byte for byte the sheets
  of phase 1; Darcula and Nord differ only by their phase-1 contrast fixes; the theme tests
  pass; all variants compile in the running demo.

### 5.3 Phase 3, as done (2026-10-07)

- **Layout** as in section 3: `light/_palette.scss`, `light/winter/` (`_scheme.scss`, light's
  `_scheme-exceptions.scss`), `dark/_palette.scss`, `dark/_nature-exceptions.scss`,
  `dark/{midnight,darcula,lagoon,violet,nord}/_scheme.scss`. The 58 images that have a dark copy
  moved their light original into `light/`; `winter/` keeps the neutral images, the component
  colours, `_theme-configuration.scss`, `_custominit.scss` and empty default exceptions.
  `_variant-custominit.scss` is per nature (`light/`, `dark/`), or per scheme in a scheme's
  directory, instead of the nature's.
- **Java:** `SchemeVariant` (nature, scheme, label; constants `WINTER`, `MIDNIGHT`, `DARCULA`,
  `LAGOON`, `VIOLET`, `NORD`) and the enum `ThemeNature` replace `DefaultThemeVariant`,
  `DarkThemeVariant` and `DarkSchemeVariant`. `IThemeVariant.of()` is gone (`SchemeVariant.parse`
  reads a name), and so is `IThemeFactory.getDefaultVariant()`: the default is
  `DomApplication.getDefaultThemeVariant()`, the first light variant of `getThemeVariants()`.
  `findThemeVariant(name)` returns the listed variant or that default. `getThemeVariantForColorScheme`
  returns the first listed variant of that colour scheme. `SassThemeFactory` has one constructor,
  `(style, variants)`; `INSTANCE` lists winter, Midnight, Darcula, Lagoon, Violet, Nord. The
  labels are the schemes' names (the `ui.theme.*` messages are gone).
- The parameters module also has `$themeNature` and `$themeScheme`.
- Demo: the review page's bar lists `getThemeVariants()`, `?scheme=` takes a variant name
  (`dark-nord`); the moon/sun switch goes to the first scheme of the other nature.
- **Verified:** all six sheets byte for byte the phase-2 sheets under their new names; the
  module's tests pass; in the running demo all six compile, a browser that prefers dark gets
  `dark-midnight` and one that prefers light `light-winter`, an old `dark` cookie gets
  `light-winter`, and the old URL `$THEME/dark/style.scss` serves the default's sheet.

### 5.4 Phases 4 and 5, as done (2026-10-07)

- Demo: the review page's bar and `?scheme=`, and the moon/sun switch, were done with
  phase 3; the demo's sheets' comments followed.
- Site (`domui.github.io`): `look-and-feel/themes` rewritten around colour schemes - the
  schemes, `getThemeVariants()` and its order, the search path, a scheme's tokens, the
  roles, exceptions, images; `the-winter-theme` (`style.scss`, the files);
  `overriding-the-theme` (`_variant-custominit.scss` per nature or scheme, `$themeNature`);
  `sass-scss-support` (the three variant parameters); `moving-to-modules` ("A colour
  scheme of your own", and "After the colour schemes": what an application has to
  change); `styling-your-component`, BreadCrumb2, Explanation, UrlPage. The site
  generates without errors or warnings.
- In this repository: `finished-plans/THEMES.md` points here for variants, and
  `darktheme.md` §8 closes into this plan; `IMPROVEMENT-LOG.md` has the work and the
  decision.

## 6. Open

Fixed since (2026-10-08):

- *Application sheets did not see the exceptions* - nor an application's own custominit
  files: an application sheet's `@use "theme"` was the first load of the theme module,
  unconfigured. `DartSassCompiler` now compiles a sheet that is not the theme's own through a
  small root that loads the theme configured with `_theme-configuration.scss` first (as
  `style.scss` does), and then the sheet; its `theme` is that configured instance. Only when
  the theme has a `_theme-configuration.scss`. Verified in the demo: a sheet reading
  `t.$cal-bg` gets light's exception `#ffd` (was the role's `#f5f6f7`), and Nord's window
  colour in a Nord session. The two documentation items that stood here were done in
  phase 5.
- *The three component defects* of `darktheme.md` §8.3, found to be four:
  - the HtmlEditor's editing area: the iframe's sheet was a plain `minieditor.css` with no
    colours, so the browser's white page. It is `minieditor.scss` now, a root sheet reading
    the theme (input ground, text, links, code, `color-scheme`), which `DartSassCompiler`
    configures like an application sheet - only the theme's `style.scss` configures itself.
  - its toolbar and frame: `js/jquery.wysiwyg.css` is loaded after the theme and outranked
    the theme's `.wysiwyg`. `_htmleditor.scss` repeats its selectors with `body` in front and
    sets only colours (`$htmled-border`, `$htmled-toolbar-border`); in a dark scheme it drops
    the editor's light gradient and inverts the toolbar icons (`filter: invert(1)
    hue-rotate(180deg)`: lightness turned, hue kept).
  - CheckboxButton: the other state's label was the knob's ground at 50% (off) and black
    (on) - mixin parameters, not colours a scheme could set. Now `$ckb-off-idle-color`
    (`$text-muted`) and `$ckb-on-idle-color` (`$text-color`), 3.1-9.2 in the dark schemes.
    And the real cause of "hardly readable": the switch was a fixed width, so a label like
    "Sold out" wrapped and spilled out below it, in light too. The switch is now a grid of
    two equal columns as wide as the longer label, never narrower than its size class; the
    knob slides by its own width.
  - RadioGroup `asButtons()`, disabled: the text read the palette's `$button-disabled-color`
    directly (a line colour, 1.2-1.35 on its ground in dark). Now `$rbb-disabled-label-color`
    (`$text-muted`), 3.1-4.9.
  Light keeps its values through five more exceptions (114 now). `TestThemeContrast` has the
  three label pairs, at 3:1 (disabled and inactive labels are exempt from WCAG, but must be
  readable); light's two faint ones are on its known list. Checked with screenshots of the
  review page in Midnight and winter.

Still open:


- **Light's 114 exceptions** are the debt decision 6 describes: each one removed is a choice
  for the component's role, to make on the review page.
- `ThemeColor`'s dark bands and the demo's syntax colours were tuned for Darcula (see
  `darktheme.md` §7.5); check them against Midnight, now the first dark scheme.
- Whether the dark template's mixes (forbidden in the old dark files by `darktheme.md` §2.5
  rule 2) stay. For Darcula they cannot: phase 2 needs its literals, as tokens or as the
  default of a token.
