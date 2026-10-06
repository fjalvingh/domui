# Dark theme: separate colours and images per variant

Status: **in progress** since 2026-10-06. §7 records progress.

The dark variant looks poor because almost all of its colours are *worked out* from the
light theme rather than *chosen*, and because its images are the light theme's images.
This plan gives each variant its own colour files, in which every visible colour is
stated, and makes every image the theme draws correct on a dark page. The arithmetic that
now produces the dark colours is kept only where it is correct in both variants.

Paths are relative to `to.etc.domui/src/main/resources/resources/themes/scss/winter/`
unless said otherwise. `finished-plans/THEMES.md` §13-§15 describes the current
mechanism; this plan assumes it has been read.

[TOC]

## 1. What there is now

### 1.1 The colour mechanism

| File | Holds | Variant-specific? |
| --- | --- | --- |
| `_variables.scss` | tier 1, the main set: 138 variables, 94 of them colours, *mixed* with fonts, sizes, image names | no |
| `_derived-variables.scss` | tier 2: 285 variables, ~256 colours. Bulma glue (`$text`, `$border`, `$link`, `$colors`…) plus one variable per colour a component paints, each defaulting to a main-set value or an expression over one | no |
| `_color.scss` | forwards both tiers unchanged | **replaced** by `dark/_color.scss` |
| `dark/_color.scss` | `@forward "variables" with (79 values)` and `@forward "derived-variables" with (34 values)` | the whole dark variant |

So the dark variant states **113 of ~350 colour variables**. The other ~240 get their dark
value one of three ways:

1. **By formula from a value dark did set**, using a formula written for a light page.
2. **From the inverted greyscale ramp**: dark sets `$white` to the darkest surface and
   `$black` to near-white, so everything written as "a grey on the ramp" moves.
3. **Not at all**: the light literal is used as it is.

On top of that, the partials still compute some colours themselves: `lighter()`/`darker()`
in `_monthpanel`, `_smallimgbutton`, `_checkboxbutton`, `_radiobutton` and
`bulmaish/_button_common`, plus `findColorHighlight()` for the button colour variants.

### 1.2 Colours, measured

I compiled both variants with the dart-sass CLI, from a scratch copy of the theme, and
paired each colour declaration in the light sheet with the same one in the dark sheet:

| | declarations |
| --- | --- |
| colour declarations in the light sheet | 1159 |
| different in the dark sheet | 808 |
| **identical in the dark sheet** | 351 |
| … of which not `transparent`/`inherit`/`none`/a black shadow | **~90 real colours** |

The identical ones are case 3, light colours left on a dark page. The most visible of them:

- every `.is-primary` button, icon and SVG icon, `.ui-tlf-hdr`, popup menu hover, tree hover,
  cookie bar: `#f69231` (the accent is never toned down for dark)
- ConditionPanel's ten pastel level backgrounds (`#aff8a8`, `#dcd3ff`, …)
- PopInPanel (`#b8e59d`, title `#6af218`), the schedule's items (`#00ffcc`, `#ff4d00`)
- `.ui-f-display` (`#eff7ff`), the read-only form wash
- breadcrumb2 selected item (`#eef5ff`) and crumb link (`#ffff99`)
- GenericHeader/ExpandingHeader title blues (`#297bc0`, `#013686`, `#4c5a6f`), on a dark ground
- Switch "on" (`#2196f3`), the layout resizer feedback colours, the drag-drop table header,
  the progress bar (`red`), DataCellTable's selection (`cyan`, a literal in the partial)
- DataPager2 button hover text `#051cac`: dark navy on a dark page

Cases 1 and 2 are the "ugly" ones, because they change colour but land somewhere nobody
chose. From the same compile:

| Component | Light | Dark, as computed | Why |
| --- | --- | --- | --- |
| TabPanel header bar | grey 48% | **grey 58%**, a light band across a dark page | `$tab-hdr-bg: $grey`, ramp inverted |
| TabPanel tab text | `#fff` on 71% | **`rgba(0,0,0,.7)` on 36%**, dark on dark | `$tab-color: $text-invert` = `findColorInvert($text)`; `$text` is light in dark |
| TabPanel tab separators | 29% | **76%**, bright lines | `$tab-border: $grey-dark` |
| Selected tab text | 71% on white | 36% on 14%, barely readable | `$tab-selected-color: $tab-bg` |
| Every "hard" line: bug badge, progress bar, selected cell, popin title, pager | `#0a0a0a` | **`hsl(0,0%,96%)`, white frames** | `$line-hard: $black`, ramp inverted |
| Tree3 fold button | black | **near-white** with dark text | `$tree3-fbtn-bg: $black` |
| Ace editor bar | 14% | **88%**, a white bar | `$acedit-bar-bg: $black-ter` |
| Tree3, breadcrumb, cookie warning text | white | `rgba(0,0,0,.7)` | all `$text-invert` |
| MonthPanel header, small image button hover | `lighter(x, 12-20%)` | lighter on a dark page means *more* contrast, not less | formula in the partial |

The theme's own documentation of the mechanism calls this a feature ("every rule that
reaches for the light end of the ramp then gets a dark colour without knowing it"). It
works for page grounds and body text. It fails for anything whose light-mode colour
depended on a *relationship*, such as "a bit darker than the panel" or "text inverted
against the body text colour": inverting the ramp turns the relationship around, and the
result has not been designed.

### 1.3 Images

The theme directory holds 164 images (`.png`/`.gif`). An image placed in `dark/` would
already shadow the light one, because image references are resolved through the same
search path as the stylesheets (`$THEME/dark/x.png` → `winter/dark/x.png`, else
`winter/x.png`). Nothing has used that yet: `dark/` holds only `_color.scss`, so every
image is the light one. The dark variant switches off just two of them,
`$title-bg-img` and `$tab-img`.

Images reach the page in three ways:

- `url(...)` in a partial, either literally or through an `$…-img` variable (~60
  images)
- `Img`/`Icon.of("THEME/…")` in Java. The `Theme` enum (`themes/Theme.java`) holds 56
  of these behind `Theme.update()`, which lets one be replaced by any `IIconRef`
  (font icon, SVG). Names are also built up in code: `big-<type>.png` (Explanation),
  `mini-<type>.png` (MessageLine), `btnHeader<state><size>.png` (ExpandHeader),
  `ipt*.png` (InternalParentTree)
- `exceptionTemplate.html`/`exceptionPrdTemplate.html`, the stand-alone error pages

I drew each image on the light page colour and on the dark one (contact sheets made with
ImageMagick) and sorted them by what goes wrong on dark:

| Group | What goes wrong | Images |
| --- | --- | --- |
| **A. Surfaces drawn as images**: gradients, bands, rules | a light band or block on a dark page, whatever the palette says | `bg-ttl-domui` (title bar), `tab-all-domui` (ScrollableTabPanel sprite), `bg-errors` (error panel), `bg-progress` (PercentageProgress), `bg-rounded-left/-middle/-right` (Breadcrumb), `dnd-separator`, `data-pager-icons` (light button faces) |
| **B. One-colour glyphs**: a shape that means "draw this in the text or line colour" | dark or grey ink vanishes on dark; light-grey ones glare | `sort-asc/-desc/-none`, `tree-*` and `bg-tree-*` (tree lines and +/- boxes), `xdt-collapsed/-expanded`, `tab-pnl-close(-hover)`, `22x11_tab-pnl-close`, `tab-scrl-icon`, `resize`, `menuarrow2.gif` (calendar), `btnHeaderMenuActive`, `paw`, `secret`, `btnShowDetails`, `mandatoryField`, `48x16_isct_erase` |
| **C. Colour icons with a light body or dark detail** | the white body glares as a block (calendar, lookup, save, find), or the dark detail is lost | `btn-datein`, `btnToday`, `btn-popuplookup`, `btn-hover-popuplookup`, `btn-hover-ClearLookup`, `btnClearLookup`, `btnSave`, `btnFind`, `btnHideDetails`, `ttlFind`, `isct_empty`, `iptPage`, `dataExpired`, `dpr-select-all/-none/-on`, `dspcb-on/-off`, `btnHeader{Collapsed,Expanded}{NORMAL,SMALL}`, `btnHeaderHamburger`, `72x24_back`, `72x24_close`, `close`, `btnEdit`, `secured` |
| **D. Animations** with a white matte or a light face | a white fringe or light bar; cannot be recoloured | `io-blk-wait.gif`, `progressbar.gif`, `lui-keyword-wait.gif`, `asy-container-busy.gif` |
| **E. Colour icons that read on both grounds** | nothing, verified on the sheet | the message/state icons (`mbx-*`, `mini-*`, `big-info`, `info`, `error`, `warning`, `success`), `btnCancel`, `btnCheckmark`, `btnClear`, `btnClose`, `btnConfirm`, `btnDelete`, `btnNew`, `btnAdd`, `btnPlus`, `btnRedCross`, `btnSkull`, `btnSpecialChar`, `btnClock`, `addToSelection`, `accessDenied`, `nav-overflow`, `pmnu-submenu-*`, `sh-*`, `icon-search`, `ipt{Component,Html,Location,SourceCode}`, `ui-bug-ind/-ovf`, `logo-small` |
| **F. Not themed** | — | the colour picker's 15 images (a self-contained dark widget); `exc-*`, used only by the stand-alone exception page, which is not rendered with the theme |
| **G. Not referenced anywhere** | — | `bg-expl`, `bg-horiz-separator`, `bg-new-ttl1/2`, `blank`, `btnMinus`, `btn-hover-ClearMultipleLookup`, `bupl-cancel`, `lsel-delete`, `defaultButton`, `exception-img-1`, `flare-important`, `hr-caption`, `pan-down/-left/-right/-up`, `paneh/-hc/-v/-vc.gif`, `small-delete`, `tab-blue-header(2)-bg`, `tab-norm-*`, `tab-off-*`. (`big-error`/`big-warning` look unreferenced but are built up by Explanation.) `hr-blue.png` is referenced, in a commented-out rule, and does not exist |

Group E is checked again in Phase 5 against the *new* dark grounds, because whether an
icon reads depends on the ground it sits on.

### 1.4 Other places dark colours are computed or repeated

- `to.etc.domui/.../themes/ThemeColor.java` folds colours computed at runtime
  (`tint`/`ink`/`edge`) into fixed lightness bands for dark. That is correct where it is
  used, for colours that do not exist until runtime, and it **stays**. Its comment says
  stylesheets should use the same bands, so it has to be checked against the new palette.
- The demo's `css/_darkstyle.scss` repeats the dark palette as its own `$dm-*` literals
  (`$dm-surface: hsl(220,13%,11%)` *is* dark's `$white`). It should read the theme instead.

## 2. Target design

### 2.1 Files

```
winter/
  _variables.scss           non-colour only: fonts, sizes, metrics, image names    shared
  _palette.scss             light: the colour roles, every value a literal          ┐ one per
  _component-colors.scss    light: the bulma colour names, and one variable per     ┘ variant
                            colour a component paints
  _derived-variables.scss   non-colour derived values: sizes, metrics               shared
  _color.scss               forwards the four variable modules                       shared
  dark/_palette.scss        dark: the same names, its own literals
  dark/_component-colors.scss  dark: the same names, its own values
  dark/*.png                the dark copies of group C images
  icons/*.svg               the group B glyphs, one-colour, coloured by CSS          shared
```

This relies on a mechanism that already exists: a file in `dark/` shadows the file of the
same name in `winter/`, because every theme resource, including a Sass `@use`/`@forward`
target, is resolved through the theme's search path with the variant directory first
(`DartSassResolver.resolveName` → `ThemeResourceFactory`). The new part is that a variant
**replaces** colour files instead of *configuring* them with `with (...)`. So:

- `dark/_color.scss` and both `with` clauses go away. `_color.scss` is the same for every
  variant.
- The dark files cannot read the light ones (the same name resolves to themselves), so
  **nothing in dark can be computed from a light value**. The structure enforces this.
- A variable that one variant declares and the other does not is a compile error in the
  variant that lacks it ("Undefined variable"), so the two files cannot drift apart
  without anyone noticing.
- `_custominit.scss` still configures the theme exactly as it does now
  (`load-css("theme", $with: …)`). Every palette and component-colour variable keeps
  `!default`, so an application's value still wins over both variants.
- **`_variant-custominit.scss`** is its per-variant sibling. An application that wants
  different values in light and dark puts the light ones in
  `themes/scss/winter/_variant-custominit.scss` and the dark ones in
  `themes/scss/winter/dark/_variant-custominit.scss`. It writes only what it changes, so
  a colour DomUI adds later still comes from DomUI's palette. Where both files set a
  variable, the variant file wins. DomUI ships an empty copy in `winter/` and in every
  variant directory, so the light file cannot reach dark: when dark is compiled,
  DomUI's empty `dark/` copy is found before it.

### 2.2 The palette (tier 1)

Roughly 60-80 roles, named for what they are *for*, never derived across roles:

| Group | Roles (indicative) |
| --- | --- |
| Grounds by elevation | `$ground-0` (page) … `$ground-5`: page, panel, raised panel, popup, header band, strongest |
| Text | `$text-color`, `$text-strong`, `$text-muted`, `$text-disabled`, `$text-on-accent`, `$text-on-strong` |
| Lines | `$line-color`, `$line-strong`, `$line-hard`, `$focus-ring` |
| Accent | `$primary`, `$primary-hover-bg`, `$primary-active-bg`, `$primary-soft-bg` (a wash of the accent) |
| Links | `$link-color`, `$link-visited-color`, `$link-hover-color` |
| Selection and hover | `$selected-bg`, `$selected-color`, `$row-hover-bg`, `$row-hover-outline`, `$highlight-bg` |
| Inputs | `$input-bg`, `$input-color`, `$input-border`, `$input-hover-border`, `$input-focus-border`, `$input-ro-bg`, `$input-disabled-bg` |
| States | `$errors-*`, `$warnings-*`, `$info-*`, `$success-*`, each a `-bg`/`-color`/`-border` triplet |
| Glyphs | `$glyph-color` (sort arrows, tree lines, close crosses), `$glyph-hover-color` |
| Named hues | `$red` … `$purple`, tuned per variant (dark: less saturation, more lightness) |
| Literal greys | `$white` … `$black`, which **mean what they say in both variants** |

**The ramp is no longer inverted.** `$white` is white in the dark variant too, because a
name that means its opposite in one variant is where most of the §1.2 surprises came
from. Everything that used the ramp to mean "a surface" or "a line" moves to a role
(`$ground-*`, `$line-*`, `$text-*`), and `ladder()`, `$grey-ramp` and `$ladder-direction`
are deleted: the one component that walked the ramp, the popup menu, states its levels
per variant instead.

### 2.3 Component colours (tier 2)

There is one file per variant with identical names. Most values are references to that
variant's own palette (`$tab-hdr-bg: $ground-4`). A literal is used where a component
genuinely has its own colour (ConditionPanel levels, the schedule's item colours, the
resizer feedback). Because each variant writes its own mapping, dark can map TabPanel
onto different roles than light, which a shared formula cannot do.

### 2.4 Images

Each group from §1.3 gets the treatment that makes it follow the palette, so a later
palette change does not need new images:

| Group | Becomes |
| --- | --- |
| A. surfaces | **CSS**: gradients, borders and backgrounds from component-colour variables. The image is deleted. The sprites (`tab-all-domui`, `data-pager-icons`) are split: the faces become CSS, the glyphs on them go to B |
| B. glyphs | **One-colour SVG** in `icons/`, shared by both variants. In a stylesheet it is drawn as a CSS mask: `mask: url(icons/x.svg) …; background-color: $…-glyph-color`. From Java it is inlined by `SvgIcon` with `fill: currentColor`, through `Theme.update()` where it is a `Theme` constant. The colour is a component variable, so it is per variant |
| C. colour icons | **A dark copy** with the same name in `dark/`: the white body is dropped to a dark face, the dark detail lifted. Made by a script (ImageMagick) checked in next to the images, so a copy can be regenerated and reviewed, and corrected by hand where the script is not enough |
| D. animations | **CSS animations** in colour variables: the busy spinner, the striped "please wait" bar and the progress bar. The GIFs are deleted |
| E. readable | unchanged, shared |
| F. not themed | unchanged |
| G. unreferenced | deleted, with a list in the log; an application that used one by its `THEME/` name gets a missing image and can copy it |

The rule for the future is the same as for colours: **a new themed image is either a
one-colour SVG coloured by a variable, or it comes with its dark copy in the same
commit.**

### 2.5 Rules (they replace "Three rules the colour sweep produced" in THEMES.md §14)

1. **A partial reads only its own component variables.** This is unchanged, but it now
   also means no `lighter()`, `darker()`, `findColorInvert()` or `findColorHighlight()`
   in a partial. A hover, active or disabled shade that is visible gets its own variable
   (`$mp-hdr-hover-bg`, `$button-primary-hover-bg`).
2. **The dark files hold no colour arithmetic.** The only function allowed in
   `dark/_palette.scss`/`dark/_component-colors.scss` is `rgba($role, alpha)`, for
   something that has to be see-through. A reference to a role *of the same file* is
   fine. The light files keep the expressions (`lighter()`, `findColorInvert()`…) that
   produced the light theme as it was, so the light theme did not change. A new colour
   may be computed in light, but it is stated in dark.
3. **A new colour is added in both variants in the same commit.** The compiler enforces
   this.
4. Contrast helpers (`contrastRatio`, `findColorInvert`) stay in `_functions.scss`. They
   are used by the verification in §3 and may be used for genuinely runtime-only cases,
   but not to produce a theme colour.
5. **Images:** see the end of §2.4.

## 3. Verification tooling (needed by every phase)

- **A compile-and-diff harness.** It compiles the theme for both variants, pairs the
  colour declarations and reports (a) dark identical to light and (b) the diff of each
  variant against a stored baseline. Phase 1 is checked by "both diffs empty". This is
  the experiment from §1.2, kept as a script.
- **A contrast check.** For each listed text/ground pair (body text on each ground, tab
  text on a tab, header text on the DataTable header, state text on a state wash…) it
  asserts WCAG AA in both variants.
- **An image check.** It lists every image the compiled sheets and the Java sources
  reference, with the variant directory it resolves from, and fails on a missing one.
  This catches a deleted image that is still used, and a group C image without its dark
  copy.
- **A demo "theme review" page** that puts every Tier A and B component (§4, Phase 4)
  and every themed image on one page, so one screenshot per variant shows the whole
  palette. Visual review happens on this page with the browser tools, in both variants.

## 4. Phases

### Phase 1: restructure, no colour changes

The goal is to split the files without changing a single compiled colour, so that every
later commit is only a colour change.

1. Split `_variables.scss` into `_variables.scss` (non-colour) and `_palette.scss`
   (colour). Split `_derived-variables.scss` into `_derived-variables.scss` and
   `_component-colors.scss`. Update `_color.scss`, `_functions.scss` (it reads
   `$grey-ramp`/`$ladder-direction`, which move to the palette) and the `@use` lines.
2. Write `dark/_palette.scss` and `dark/_component-colors.scss` as copies of the light
   files with the dark variant's 113 own values in place of the light ones. A value the
   dark variant did not set keeps the light file's *expression* (`$selected-bg: $primary`,
   `findColorInvert($text)`), which now resolves against the dark file. Delete
   `dark/_color.scss`.
3. Verify that both compiled sheets are identical to before.

After this phase nothing in dark is read from the light theme, and every dark colour is
a line in a dark file that can be changed on its own. What is still computed is computed
from dark values with formulas made for light; Phase 2 replaces those.

### Phase 2: roles instead of the inverted ramp

1. Introduce the roles that stand for what the ramp greys were used for, in both
   palettes. The light values are the same ramp expressions as before, so the light
   sheet stays identical. Each dark value is the literal the inverted ramp gave, so
   the dark sheet stays identical too.
2. Repoint every component variable that reads `$white`…`$black` to a role, in both
   component files. A grey that only one component uses keeps the light expression in
   the light file and gets the literal in the dark file.
3. Make `$white`…`$black` literal again in dark. Remove `ladder()`, `$grey-ramp` and
   `$ladder-direction`.
4. Verify: the light sheet is identical, and the dark sheet is identical except for what
   changes because `$white` is white again.

The phase preserves values: the §1.2 defects are still there afterwards, now as
explicit dark values that Phases 3 and 4 replace. The accent and state shades and
`$glyph-*` belong to the phases that need them (4 and 5).

### Phase 3: design the dark palette

This is the visual work, done once in `dark/_palette.scss` rather than component by
component:

- grounds step up in lightness with elevation (roughly +3-4% per step on one cool
  neutral hue); a raised surface is lighter, never darker
- text is not pure white: a strong, a normal and a muted level, all AA on `$ground-0…3`
- lines are low-contrast and quieter than text, and `$line-hard` is the strongest line,
  not white
- the accent and the named hues lose saturation and gain lightness. The orange stays
  recognisably the brand, but a filled `is-primary` button gets an explicitly chosen
  ground and text
- states are a tinted dark ground with light text of the same hue, levelled with each
  other as the light ones are

The result is checked with the contrast test and the review page, and then
`ThemeColor`'s bands (`TINT_*`, `INK_*`, `EDGE_*`) are aligned with it.

### Phase 4: components, by visibility

For each component, both variant files get their values, the in-partial arithmetic is
replaced with variables, and the review page gets a screenshot:

**Tier A**, on nearly every page: page and body, links, panels and captioned panels,
inputs (incl. read-only, disabled, error, focus), the default and `is-*` buttons with
their hover/active/focus/disabled shades, DataTable (header, even rows, hover, selected,
cell highlight), the page title bar and `$header-bg`, TabPanel and ScrollableTabPanel,
popup menus (`pmnu`, `pome2`, hamburger), FloatingWindow and dialogs, MsgBox, the message
line and the error/warning/info panels, breadcrumbs, LookupInput popups, the DateInput2
calendar, form labels and `.ui-f-display`.

**Tier B**, often seen: Tree2/Tree3, DataPager/DataPager2, Switch, CheckboxButton,
RadioButton, GenericHeader/ExpandingHeader, MultipleLookupInput and EnumSetInput labels,
MonthPanel, the layout panes and resizers, PopInPanel, progress bars, ConditionPanel
levels, InfoPanel/ExplanationPanel, SearchAsYouType, the cookie banners.

**Tier C**, stated but not designed: the bug indicator, LogTailer, Ace bar, drag-and-drop,
OddCharacters, the schedule grid. These get a sensible dark value and nothing more.

**Exempt**: `_devmode.scss` (already a dark panel, left alone by earlier sweeps) and the
colour picker's `#f00` sample swatches.

The literals still left in partials are moved into variables as part of their
component: `cyan` (`_datacelltable`), `#999999` (radio button disabled label, small
button disabled), `black` (checkbox switch glyph), `#000` (FloatingWindow hider), and
the `.ui-chkbb` can-toggle parameters computed with `darker`/`lighter`.

### Phase 5: images

Done per group in §2.4, in this order, because each later group depends on the earlier:

1. **G**: delete the unreferenced images. This shrinks the rest of the work.
2. **A**: replace the surface images with CSS. They belong with the component whose
   surface they draw, so where Phase 4 has already reached a component, this is a
   follow-up commit for it.
3. **B**: draw the glyphs as SVG in `icons/`, add the mask mixin to the theme, switch the
   partials and the `Theme`/`Img` call sites. Tree2/Tree3 and the old `Tree` build their
   lines from images in Java and are the largest single item.
4. **D**: replace the four animations with CSS.
5. **C**: generate the dark copies, then review each on the theme review page in the
   *final* dark palette, and correct by hand where needed.
6. **E**: check again on the final grounds. Any that fail move to C.

### Phase 6: applications, demo and documentation

- **Applications.** `_custominit.scss` works unchanged; per-variant values go in
  `_variant-custominit.scss` (§2.1). An application that built its own variant as
  `@forward "variables" with (...)` must convert. Its variant directory
  now holds a `_palette.scss` and a `_component-colors.scss` (copied from `dark/` and
  edited), and a renamed variable is a compile error naming it. A short "variant
  colours and images" section goes on the documentation site's
  `look-and-feel/moving-to-modules` page, alongside a list of the renamed variables and
  the deleted images, as was done for the last rename.
- **Demo.** `css/_darkstyle.scss` drops its `$dm-*` copies and reads the theme's roles
  (`@use "theme" as *`). Most of its rules then disappear into one rule per class that
  is correct in both variants. The demo's own images (`webapp/img`) are checked the same
  way as the theme's.
- **THEMES.md.** §13 (the dark variant) and the variable tables in §14/§15 are rewritten
  for the new files. When this plan is finished it moves to `finished-plans/` or is
  merged into THEMES.md.

## 5. Decisions

Taken as recommended when work started on 2026-10-06. They can still be reversed, at
the cost noted.

1. **The component colours are duplicated per variant**, not only the palette. That
   means ~250 names written twice. It is the only way dark can map a component onto
   *different* roles than light (the TabPanel problem), and the compiler keeps the two
   files in step. Reversing it after Phase 2 would mean writing shared formulas again.
2. **The ramp stops being inverted** (Phase 2).
3. **Applications' own `with`-clause variants break.** Only variants written after the
   September module move are affected, and they get a compile error that names the
   variable: the same reasoning as that move.
4. **Not now: CSS custom properties.** One sheet with `var(--x)` and a
   `[data-theme=dark]` block would make switching variants free and would remove the
   compile per variant. It is easier *after* this plan, because once no colour is
   computed in Sass every colour can become a custom property. It is listed here so that
   nobody does it halfway during Phase 4.

## 6. Out of scope

- `ThemeColor`'s algorithm. Only its constants are re-aligned (Phase 3).
- The two IGNORE items in IMPROVEMENT-PLAN.md (`$dt-hdr-btm-border` is `undefined`; the
  error washes are equal). Phase 1 carries them over as they are.
- The colour picker and the stand-alone exception pages (§1.3, group F).

## 7. Progress

| Phase | State |
| --- | --- |
| §3 compile-and-diff harness, contrast check | done (image check, review page: not yet) |
| 1 | done |
| `_variant-custominit.scss` | done |
| 2 | done |
| 3 | done |
| 4 | done |
| 5-6 | not started |

### 7.1 The harness (2026-10-06)

`to.etc.domui/src/test/java/to/etc/domui/themes/`:

- `ThemeVariantCompiler` compiles `style.scss` for a variant straight from the source
  tree with the dart-sass that DomUI bundles, imitating DomUI's resolver: variant
  directory first, `theme` and `parameters` as virtual names, and optionally an
  application theme directory in front.
- `TestThemeVariants`: both variants compile, and dark differs from light. Both variants
  declare exactly the same names in `_palette.scss` and `_component-colors.scss`. A
  colour set in an application's `_custominit.scss` is used by both variants.
- `ThemeColorReport` (a `main`) writes `default.css`, `dark.css` and
  `same-colours.txt`, the list of colour declarations dark has exactly as light does. Run
  it from `to.etc.domui` with
  `mvn21 -q test-compile exec:java -Dexec.classpathScope=test -Dexec.mainClass=to.etc.domui.themes.ThemeColorReport -Dexec.args=<dir>`.
  Its count differs from §1.2 (1015 colour declarations, 207 the same in dark) because it
  counts `rgba()`/`hsl()` differently from the first experiment. The baseline for the
  later phases is its output.

### 7.2 Phase 1 (2026-10-06)

- `_variables.scss` (138 variables) is now `_variables.scss` (42, not colours) and
  `_palette.scss` (96, colours). `_derived-variables.scss` (285) is now
  `_derived-variables.scss` (26, not colours) and `_component-colors.scss` (259 → 254
  after removing five declarations that were made twice: `$mp-bg` and four `$tab-*`. The
  second one of each was a no-op). The bulma colour names (`$text`, `$border`, `$link`,
  `$colors`, the `-invert`s) went to `_component-colors.scss` rather than staying shared
  as §2.1 first said, because dark sets some of them (`$highlight2-bg`), and because they
  are colours.
- `$title-bg-img` and `$tab-img` are in `_palette.scss`, because the dark variant switches
  them off. Phase 5 removes both images.
- `_functions.scss` reads `palette` instead of `variables`. `_color.scss` forwards all
  four modules and is no longer replaced by a variant. `dark/_color.scss` is gone.
- Comments that described the `with` mechanism are updated: `_index.scss`,
  `style.scss`, `_calendarTheme.scss`, `_datatable.scss`, `_scrollableTable.scss` and
  `SassThemeFactory`.
- Verified: `ThemeColorReport` output for both variants is byte-identical to the
  baseline taken before the change. Under `jetty:run` the demo's
  `$THEME/default/style.scss` and `$THEME/dark/style.scss` are byte-identical to the
  harness output (with the source map removed). `TestThemeVariants` passes.
- **Build trap:** an incremental `mvn21 install` keeps the deleted `dark/_color.scss` in
  `target/classes`, and the dark sheet then fails with "This variable was not declared
  with !default". Build `to.etc.domui` with `clean` once after this change.
- Still describing the old mechanism, to be fixed in Phase 6: `finished-plans/THEMES.md`
  §13/§15 and the documentation site's `look-and-feel/themes` and
  `look-and-feel/moving-to-modules` pages.

### 7.3 `_variant-custominit.scss` (2026-10-06)

- `style.scss` loads `custominit` and `variant-custominit` and configures the theme with
  `map.merge()` of the two, so the variant file wins. Empty copies are in `winter/` and
  `winter/dark/`, and each says what it is for.
- `TestThemeVariants.variantCustomInitIsPerVariant` covers three things: each variant
  takes its own file, the variant file wins over `_custominit.scss`, and an application
  light file does not reach dark when the application has no dark file. The harness
  imitates `SassTheme.getThemeResource()`: search-path directories in order, and within
  one, the webapp file before the jar's (`DomApplication.getAppFileOrResource`).
- Verified live under `jetty:run`, with temporary files in the demo's webapp
  (`themes/scss/winter/`, removed again). Each variant took its own value, and
  without a dark file the dark sheet was byte-identical to the baseline.
- Unchanged sheets: both are still byte-identical to the baseline.

### 7.4 Phase 2 (2026-10-06)

- New roles in both palettes. Each is the ramp grey it replaces in light, and the
  literal the inverted ramp gave in dark:

  | Role | Light | Used for |
  | --- | --- | --- |
  | `$ground-bg` | `$white` | what a control or button is filled with |
  | `$ground-alt-bg` | `$white-ter` | one step off the ground: bulma's `$background` and `$light`, static buttons, tree hover |
  | `$text-color` | `$grey-dark` | bulma's `$text` |
  | `$text-strong-color` | `$grey-darker` | bulma's `$text-strong`/`$dark`, link hover/focus/active, a control's text |
  | `$control-border` | `$grey-lighter` | bulma's `$border`, button and input edges |
  | `$control-hover-border` | `$grey-light` | the same, hovered |
  | `$control-active-border` | `$grey-dark` | the same, pressed |
  | `$fill-muted-bg` | `$grey-lighter` | disabled fills, the log gutter, the resizer, a switch that is off, `<code>` blocks |
  | `$line-soft` | `$grey-lighter` | InfoPanel's edge, GenericHeader's rule, Tree2's shadow |

  These names stand in for §2.2's indicative `$ground-0…5` and `$text-*` set. They are
  named after what was there, and Phase 3 may merge or extend them.
- `$line-strong`, `$line-hard` and `$text-muted` have their dark values as literals.
- One-off greys stay a ramp expression in light and are a literal in dark: TabPanel
  (`$tab-hdr-bg`, `$tab-bg`, `$tab-border`), the cookie warning, PopInPanel's border,
  the Ace bar, Tree3's fold button, MonthPanel's other-month text and bulma's
  `$input-shadow`.
- The popup menu states its levels per variant (`$pmnu-sm1-bg`…`$pmnu-sm3-bg`,
  `$pmnu-disabled-color`, `$pmnu-border`) instead of walking the ramp, and
  `ladder()`, `$grey-ramp` and `$ladder-direction` are gone. `_functions.scss` now reads
  no theme variable at all.
- `<code>` blocks read `$code-block-bg` instead of `$grey-lighter` in `_core.scss`. The
  unused `$shades` map is deleted.
- In dark, `$white`…`$black` are the light theme's literal greys again. The dark files
  use them only in bulma's `$colors` map, where "white" and "black" mean those colours.
- Verified with `ThemeColorReport` against the Phase 1 baseline: the light sheet is
  byte-identical. The dark sheet differs in 72 declarations, all of them the
  `is-white`/`is-black` variants of buttons and icons, which in dark were the dark page
  colour and the light text colour, and are now white and black.
  `TestThemeVariants` passes.

### 7.5 Phase 3 (2026-10-06): the Darcula palette

The dark palette is modelled on IntelliJ's Darcula, as decided for this phase: dark but
not black, neutral greys with a trace of blue-green, muted blue for selection and focus,
and Darcula's orange for the accent.

| Role | Value | Darcula's |
| --- | --- | --- |
| page (`$body-bg`) | `#2B2B2B` | editor ground |
| panel (`$surface-bg`) | `#313335` | gutter |
| window, popup, recessed control (`$window-bg`, `$ground-alt-bg`) | `#3C3F41` | panel grey |
| field, button, band (`$ground-bg`, `$input-bg`, `$surface-alt-bg`) | `#45494A` | text field |
| text / strong / muted | `#BBBBBB` / `#D4D4D4` / `#A0A0A0` | UI text |
| form labels | `#A9B7C6` | editor text |
| lines: soft / normal / strong / hard | `#464646` / `#515151` / `#6B6B6B` / `#9A9A9A` | separator `#515151` |
| control border | `#646464` | text field border |
| accent (`$primary`) | `#CC7832` | keyword orange |
| links, focus | `#589DF6`, `#4A88C7` | hyperlink, focus border |
| selection | `#4B6EAF`, DataTable `#214283` | menu / editor selection |
| hover on menus and selected rows | `#2D4565` | (a muted selection blue) |
| error / warning / info edge | `#BC3F3C` / `#BE9117` / `#4A88C7` | error, warning, focus |
| named hues | `#CC7832`, `#C9A93B`, `#499C54`, `#299999`, `#4A88C7`, `#9876AA`, `#BC3F3C` | syntax colours |

- **Pulled forward from Phase 4:** the 46 literals in `dark/_component-colors.scss` that
  Phases 1-2 had kept from the old bluish palette, re-pointed to Darcula values so that
  nothing on the page is left in the old hue. That covers the popup menu, the DataTable
  palette, TabPanel, the breadcrumb, the jscalendar popup, and the one-offs listed in
  §7.4. TabPanel and the breadcrumb were changed together with their text colours,
  because one does not work without the other. That fixes the §1.2 TabPanel and
  breadcrumb defects, and the white frames (`$line-hard` is `#9A9A9A`).
- **Pulled forward from Phase 6:** the demo's `css/_darkstyle.scss` reads the theme's
  roles (`@use "theme" as t`) instead of keeping its own copy of the old palette, and
  the demo's source viewer (`css/_syntax.scss`) uses Darcula's syntax colours in dark.
  The demo's top bar (`.d-sbc`, `#24292e`) is its own brand bar in both variants and
  is left as it is.
- **`TestThemeContrast`** is the §3 contrast check. A sheet compiled against the theme
  for each variant writes out the channels of 30 text/ground pairs, and the test
  computes the WCAG ratio exactly. The theme's own `contrastRatio()` approximates. Every
  dark pair is at least 4.5:1, or 3:1 for the bold DataTable header and the primary
  button. Three dark values were adjusted to get there: `$text-muted`, `$errors-wash`
  with `$errors-input-bg`, and TabPanel's `$tab-color`. The light variant's existing
  shortfalls are a pinned list in the test: muted text, the page title, error and info
  text, TabPanel and the breadcrumb. The light theme is not changed by this work, and
  the test fails when one of them is fixed, so that the list gets updated.
- **`ThemeColor`'s bands** moved up with the page ground, from 11% to 17% lightness:
  tint 16-48% → 20-44%, ink 50-85% → 60-88%, edge 24-42% → 30-44%. Nothing in this
  repository calls it; applications do.
- Verified with dark-mode screenshots of the demo (headless Chrome with
  `--force-dark-mode`, which the demo's colour-scheme autodetect follows) of TabPanel,
  DataTable, FormBuilder, buttons, the CD shop search pages, notices and error panels,
  and PopupMenu2. The light sheet is unchanged.
- Left for Phase 4, seen on those screenshots: the Explanation panel's glossy gradient
  (`_misc`/`_messageline2`), and the remaining formulas made for light in
  `dark/_component-colors.scss`. Left for Phase 5: ScrollableTabPanel's grey end blocks
  (`tab-scrl-icon.png`) and the other images.

### 7.6 Phase 4 (2026-10-06): components

- **Explanation, redesigned** in both variants, which is the one deliberate change to the
  light theme. It was a 2px border around a gradient that faded to white, with a 48px
  PNG floated left that stuck out of the box. It is now a callout: a 4px bar and a round
  marker ("i", or "!" for a warning or error) in the severity's colour, on that
  severity's wash, with the text in a block of its own (`.ui-expl-txt`) so that the
  html in it flows as one paragraph. The marker is drawn in CSS, so it has no image.
  `Explanation.java` no longer adds the `big-<type>.png` image, so `big-error.png` and
  `big-warning.png` are now unreferenced (§1.3 group G). New variables, per variant:
  `$expl-{info,warning,error}-{bg,accent,marker-color}`. `$expl-border` is gone.
- `_messageline2.scss` (`.ui-msgln2`) is deleted: nothing creates that class.
- **No colour arithmetic in a partial any more.** Each computed colour became a component
  variable, with the old expression as its light value: MonthPanel's band
  (`$mp-band-bg`), SmallImgButton's hover and focus (`$sib-hover-bg`, `$sib-focus-bg`),
  RadioButton's disabled-and-chosen text (`$rbb-disabled-checked-color`), the bulma text
  button's pressed state (`$button-text-active-background-color`), the CheckboxButton
  toggle's seven shades (`$ckb-off-track-bg` … `$ckb-on-knob-hover-color`), and the
  bulma colour buttons. `$button-color-shades` gives each entry of `$colors` its hover,
  active, focus-border and inverted-hover colour. In dark, the focus border of every
  colour button is the theme's focus blue, where the formula rotated the hue: a
  green ring around the orange button.
- **No colour arithmetic in the dark files.** The bulma `-invert` colours, `$text-invert`
  and everything read from it (the cookie banners, the mini DefaultButton), EnumSetInput's
  label border and text, ExpandingTable's expanded row (light green before), and
  RadioButton's borders are stated.
- **Main-set colours read directly by a partial** got component variables where it
  mattered in dark: PopupMenu2 (`$pome2-bg`/`-color`/`-hover-bg`: a raised `#3C3F41`
  popup with the selection-blue hover), DataCellTable's selection (`$dct-selected-bg`,
  the literal `cyan` before), and the jscalendar's month title (`$cal-month-title-color`:
  it was drawn in the body colour, invisible on the dark band).
- **Tier B and C dark values:** the ConditionPanel levels (ten dark tints of the same
  hues), PopInPanel, the schedule's items and hour lines, the layout resizer feedback,
  the Switch, the progress bar (Darcula red), DataPager2's hover text, the header blues
  of GenericHeader and ExpandingHeader, the popup menu title, the cookie banner, the Ace
  bar's rule, the read-only form wash, and the drag-and-drop table's stripes.
- Dark now shares only four colour declarations with light, all deliberate: the
  drag-and-drop table's own dark-green header, the cookie banner's "advanced" button,
  which reads `var(--red)`, a custom property that now has a dark value, and the bug
  badge's yellow count.
- Verified: `TestThemeContrast` has six more pairs (the three explanation texts on their
  washes at 4.5:1, the three markers at 3:1) and passes, as does `TestThemeVariants`.
  Against the baseline, the light sheet differs only in `.ui-expl` and in the deleted
  `.ui-msgln2`. In the running demo, in dark: the Tier B pages by headless screenshot
  (headers, panels, checkbox, radio group, EnumSetInput, MonthPanel, WeekAgenda,
  DateInput2, Text2, the ruler, Tree3, HamburgerMenu, the title bar, BreadCrumb2), and
  in Chrome the states that need a click: PopupMenu2 open, the DateInput2 calendar, a
  floating window, a modal MsgBox with its overlay, and the HamburgerMenu open. The new
  Explanation in both variants.
