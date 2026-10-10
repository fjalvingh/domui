# Colour roles: a theme vocabulary designed first, components mapped onto it

Status: **phases 1 to 5 done**, and phase 6 except the documentation's old names (2026-10-10,
see 6.1 to 6.3); the role set was decided in section 7. The light values in section 3 are a first proposal, to be judged on the review
page; the light theme is allowed to change.

## 1. Why

Three times now we tried to find the theme's colour vocabulary by collecting what the
components paint and giving each colour a name. Every time it ended the same way: many
colours, one user each, and names that describe a place instead of a meaning
(`$struct-strip`, `$fill-strong-bg`, `$header-band-bg`, `$ground-alt-bg`, ...). Light still
has 114 exceptions, and they cannot be removed because there is no role they could
become.

This plan goes the other way:

1. Design a **role set** in the language of design: text levels, surfaces and their
   elevation, lines, the intents of actions and messages (as Bulma's `is-primary`,
   `is-info`, ...), interaction states. Do this without looking at which component needs
   what.
2. Give every role a value in the **light** scheme.
3. **Map** every component colour in `_component-colors.scss` to a role. Where no role
   fits, the component changes its look. The role set does not grow for one component.

The set is small and regular. It is a grid of families × steps, so a name can be guessed,
and there are no one-off entries to remember.

## 2. What it is modelled on

| System | What we take from it |
| --- | --- |
| GitHub Primer | `fg`/`bg`/`border` per role, and each role in an *emphasis* (solid) and a *muted* (wash) form. Intent names `accent`, `success`, `attention`, `danger`. |
| Atlassian | **Role** (what it means: neutral, brand, information, success, warning, danger) is separate from **emphasis** (subtlest → bold). "Selected" and "focused" are *states*, not roles. An **accent** set of hues that have no meaning, for things that only need to be told apart. |
| IBM Carbon | Text levels (primary / secondary / helper / placeholder / on-color / inverse / disabled). Surfaces as stacked **layers**. Separate `field` colours for inputs. `focus`, `highlight`, `overlay` as roles of their own. |
| Material 3 | `on-X`: every fill has its own text colour. Elevation as surface containers. `inverse-surface`, `scrim`, `outline` and `outline-variant`. |
| Bulma | The six intents DomUI's buttons already use (`primary`, `link`, `info`, `success`, `warning`, `danger`), each with a light and a dark form and an invert. |

What they all share is what this set follows:
- A name never contains a component.
- A name says what the colour is *for*: text, surface, border. It never says what it looks
  like ("grey", "blue").
- Every coloured fill has a text colour that goes on it.
- Intent and emphasis are two separate axes.

## 3. The role set

### 3.1 Grammar

```
$<family>-<step>
```

- Neutral families: `surface`, `text`, `border`, `field`, `link`.
- Colour families (the intents and `chrome`) all have the same six steps:

  | Step | Meaning | Contrast rule |
  | --- | --- | --- |
  | `-wash` | the lightest tint: the ground of a message, an input in error, a notice | the family's `-text` and `$text-default` reach 4.5:1 on it |
  | `-tint` | a clearly coloured but still light fill: a tag, a label, a highlighted day | `$text-strong` reaches 4.5:1 on it |
  | `-solid` | the full colour: a button, a flare, a badge, a title bar | — |
  | `-on-solid` | text and icons on `-solid` | 4.5:1 on `-solid` |
  | `-text` | text in this intent on an ordinary surface: an error message, a heading | 4.5:1 on `$surface-page` and on the family's `-wash` |
  | `-border` | a border, an accent bar or a marker in this intent | none: it is decoration next to text that says the same. A line that alone carries the meaning uses `-solid` |

"Heavy" and "light", as asked for, are `-solid` and `-wash`. `-tint` sits between them;
the components need it often (mixes of 28-45% of a hue into the page).

Hover and pressed shades of a `-solid` are **not** roles. They are worked out from it in
the mapping layer, by one function per nature, as `findColorHighlight()` does now. A dark
nature works them out differently.

### 3.2 Surfaces: what things stand on

| Role | For | Light |
| --- | --- | --- |
| `$surface-page` | the page canvas | `#ffffff` |
| `$surface-raised` | a panel, card or tab content on the page | `#ffffff` |
| `$surface-overlay` | what floats: a popup, menu, dialog or floating window | `#f5f6f7` |
| `$surface-sunken` | recessed: a gutter, a code block, a well | `#f5f5f5` |
| `$surface-band` | a strip one step off its surface: a table header band, an alternate row, a schedule's hour band | `#eeeeee` |
| `$surface-inverse` | a dark spot in a light theme: a tooltip, a toast | `#363636` |

### 3.3 Text

| Role | For | Light | on page |
| --- | --- | --- | --- |
| `$text-default` | body text | `#4a4a4a` | 8.9 |
| `$text-strong` | emphasis, labels, a control's own text, text on a `-tint` | `#363636` | 12.1 |
| `$text-subtle` | secondary: hints, helper text, week numbers | `#6b6b6b` | 5.3 |
| `$text-faint` | out of the way: placeholder, another month's day (exempt from AA) | `#b5b5b5` | 2.1 |
| `$text-inverse` | on `$surface-inverse` | `#ffffff` | — |

### 3.4 Lines

| Role | For | Light |
| --- | --- | --- |
| `$border-subtle` | dividers, the rule under a header, a table's inner lines | `#dbdbdb` |
| `$border-default` | the edge of a panel, window, menu or pane | `#aaaaaa` |
| `$border-strong` | a line that carries weight: under an h1, between header cells, a popup's frame | `#7a7a7a` |
| `$border-bold` | a hard frame: a selected cell, a badge, an outline | `#0a0a0a` |

### 3.5 Fields (inputs)

| Role | For | Light |
| --- | --- | --- |
| `$field-surface` | an input's ground | `#ffffff` |
| `$field-surface-readonly` | a read-only input or display value | `#f2f9fe` |
| `$field-border` | an input's or a plain button's edge | `#dbdbdb` |
| `$field-border-hover` | ... while hovered | `#b5b5b5` |

Focus is `$focus-ring` (3.7). An input in error is `$danger-wash` and `$danger-border`.

### 3.6 Links

| Role | Light |
| --- | --- |
| `$link-text` | `#2200cc` |
| `$link-text-visited` | `#551a8b` |
| `$link-text-hover` | `#363636` |

### 3.7 States (they apply to anything, in any family)

| Role | For | Light |
| --- | --- | --- |
| `$hover-wash` | under the pointer: a row, a menu item, a tree item | `#fff4d3` |
| `$hover-border` | the outline drawn with it | `#ffd55a` |
| `$selected-wash` | a selected row, cell or day | `#f5d0ea` |
| `$selected-solid` | the one chosen item, loudly: a selected tab or menu entry | `#b0177d` |
| `$selected-on-solid` | text on it | `#ffffff` |
| `$selected-border` | the frame of a selected cell or item | `#b0177d` |
| `$highlight` | marked or found: a search hit, a marked day | `#ffbb43` |
| `$focus-ring` | keyboard focus, on anything | `#5794bf` |
| `$disabled-surface` | a disabled control's ground | `#f5f5f5` |
| `$disabled-text` | its text (3:1 on `$disabled-surface`: exempt from AA, but readable) | `#858585` |
| `$disabled-border` | its edge | `#dbdbdb` |
| `$scrim` | what dims the page behind a modal (alpha at the point of use) | `#000000` |
| `$shadow` | what shadows are made of (alpha at the point of use) | `#000000` |

Selection has a hue of its own, **magenta** (about 320°), and no other role may use it.
That hue is the largest gap between all the others: the blues of `chrome`, `info` and
links (205-220°), the green of `success` (141°), and the reds, oranges and yellows of
`danger`, `primary`, `hover` and `highlight` (0-48°). So a selected row can never be
mistaken for a hovered, highlighted, informative or erroneous one. On `$selected-wash`,
`$text-default` has 6.4:1 and links 7.8:1; white on `$selected-solid` has 6.4:1.

### 3.8 Intents (Bulma's `is-*`): actions and messages

Eight families, each with the six steps of 3.1.

| Family | Means | `-wash` | `-tint` | `-solid` | `-on-solid` | `-text` | `-border` |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `primary` | the theme's accent: the default action | `#fef3e7` | `#fbd9a8` | `#f69231` | `#1e1e1e` | `#a65400` | `#f69231` |
| `control` | the normal colour of a control in use: a toggle that is on, a chosen item, a chip, a pager's buttons | `#eaf1ff` | `#c7dafd` | `#2b6de8` | `#ffffff` | `#1d4ed8` | `#2b6de8` |
| `neutral` | an ordinary action, a plain title bar | `#f5f5f5` | `#dbdbdb` | `#6b6b6b` | `#ffffff` | `#4a4a4a` | `#b5b5b5` |
| `info` | information, guidance (cyan-teal, apart from `control`) | `#e3f4fa` | `#b5e3f2` | `#0b7285` | `#ffffff` | `#0a6f91` | `#1a9bc0` |
| `success` | it worked, it is allowed | `#e9f7ee` | `#d4ee9f` | `#1a7f45` | `#ffffff` | `#1e7b43` | `#43b761` |
| `warning` | caution, before a mistake | `#fff7dd` | `#fff2ab` | `#f2b01e` | `#1e1e1e` | `#8a6100` | `#daa520` |
| `danger` | an error, a destructive action | `#ffe5e5` | `#f4b6b6` | `#d32f2f` | `#ffffff` | `#c81e1e` | `#e02424` |
| `chrome` | the application's frame: tab strips, tabs, header bands, headings | `#e4ecf5` | `#c9d8e8` | `#3d6a99` | `#ffffff` | `#1f6aa8` | `#a9c1db` |

All the pairs the contrast rules ask for were checked: every `-text` on the page and on its
`-wash` is ≥ 4.7 (chrome's 4.78 the lowest), every `-on-solid` on its `-solid` is ≥ 4.9, and `$text-strong` on every
`-tint` is ≥ 6.5.

`chrome` is winter's blue frame (`$struct-strip`, `-tab`, `-band`, the heading blue). It is
the family that makes the theme look like itself. That is why it is a family and not part of
`neutral`. Material would call it `secondary`, but "chrome" says what it is for.

Bulma's `link` colour button maps to `$link-text`. `white`, `black`, `light` and `dark`
map to surfaces and text. Those six do not need families of their own.

### 3.9 Categories: colours with no meaning

Some things only have to be told apart: ConditionPanel's nesting levels, the kinds of
appointment in the schedule, the layout resizer's feedback, the drag-and-drop table header.

These colours are `$category-<n>-solid` and `$category-<n>-wash`, for n = 1 to 7. They are
numbered, not named after a hue: "orange" would be a name that describes how a colour looks,
and a scheme picks its own hues. The order is such that neighbours differ most, so a
component that needs three of them takes 1, 2 and 3. No category is the selection's magenta.

They have two steps each, no more. Anything in between uses the intent family that matches
instead. In light, 1 to 7 are blue, orange, green, blue-violet, cyan, red and yellow:
Bulma's hues as now, except that purple moved to a blue-violet (about 265°), away from the
selection. The washes are each hue at about 25% on the page.

### 3.10 Count

6 surfaces + 5 text + 4 lines + 4 fields + 3 links + 13 states + 8×6 intents + 7×2 categories
= **97 roles**.

## 4. What changes in the light theme if this is followed

Visible changes, all on purpose:

- `$text-subtle` `#6b6b6b` (was `$text-muted` `#7a7a7a`, 4.3:1, below AA).
- The error red for text is `#c81e1e` (was `#ff0000`, 4.0:1).
- The grey title bar `$neutral-solid` is `#6b6b6b` (was `#7c7c7c`: white on it was 4.2:1).
- `is-success` is a deep green `#1a7f45` with white text (was Bulma's bright `#23d160`).
- Bands and tabs that had white text on light blue (`#93afcf`, 2.3:1) get `$text-strong`
  on a `chrome-tint`, or white on `chrome-solid`.
- Selection is magenta instead of blue (rows, cells, days) and orange (the chosen item).
- The heading blue `#1f6aa8` (was `#297bc0`, 4.48:1).
- About 40 near-identical colours become the same colour, such as the many greys and the
  three slightly different hover yellows.

## 5. How it fits the scheme architecture

**The role set is the scheme's tokens** (decided). A scheme's `_scheme.scss`
(`light/winter/`, `dark/nord/`, ...) declares exactly the 97 roles, which replace today's
`$page`, `$struct-strip`, `$raised2` and the rest.

- `_component-colors.scss` maps every component colour to `r.$<role>`. Hover and pressed
  shades of a `-solid` come from one function per nature.
- **No old names are kept in a release** (decided). During the work, aliases may live
  for a few commits (section 6). The version of DomUI that brings the roles is the
  version in which an application changes its names: not a release later. That covers:
  - the main set (`$surface-bg`, `$line-color`, `$errors-color`, `$primary`, ...);
  - the Bulma names the theme's own mixins read (`$info`, `$text`, `$link`, `$background`,
    ...). The `bulmaish` partials read roles instead. Only Bulma's mechanics stay, such
    as the `$colors` map that the `is-*` classes are generated from, now built from
    the intent families.
  
  Sass helps here: an application whose `_custominit.scss` still sets a name that is gone
  gets a compile error naming it, not a silently different colour.
- The nature palettes (`light/_palette.scss`, `dark/_palette.scss`) shrink to what a nature
  really decides: the shade functions and `$color-scheme`. Whatever is left may become a
  smaller `_nature.scss`.
- Exceptions: the goal is **none** for light. Each of the 114 becomes a mapping to a role,
  or a deliberate change of look (section 4). The same goes for dark's nature exceptions.

## 6. Phases

Light first, dark later. Between the phases, the old names may live on as **aliases**
for as many commits as the work needs. This lets each nature be done on its own. What is
released to the world has no aliases (decision 5).

1. **Decide the role set** (this document): names, families, steps, the light values.
   Add a swatch table of the roles to the theme review page, so the light values can be
   judged on the page itself rather than as hex codes.
2. **Light declares the roles.** `light/winter/_scheme.scss` declares the 97 roles in place
   of its old tokens. `light/_palette.scss` defines the old palette names as aliases of
   the roles, so that the components and the shared `_component-colors.scss` still
   compile.
   - The dark schemes and `dark/_palette.scss` are not touched.
   - Verify: every variant compiles, dark byte for byte as before. The swatches show.
3. **Map the components to roles**, one group of the review page at a time (text, buttons,
   toggles, inputs, notices, panels, headers, popups, navigation, calendar, tree, tables).
   For each group:
   - its component colours in `_component-colors.scss` become roles, and so do the
     `bulmaish` partials;
   - light's exceptions for the group are removed, and the review page is checked in light;
   - **dark keeps compiling through aliases the other way round.** Until phase 5,
     `dark/_palette.scss` defines each role the components now read from its old names,
     for example `$surface-raised: $surface-bg`. Dark should therefore look the same.
     Where it does not, the difference is noted for phase 5 and not fixed in this phase.
4. **Light contrast tests.** `TestThemeContrast` checks the rules of 3.1 on light's roles.
   That is about 30 pairs, instead of a list per component.
5. **Dark declares the roles.** Every dark scheme's `_scheme.scss` gets the 97 roles,
   chosen and judged on the review page, not just derived.
   - `dark/_palette.scss` loses its aliases, and so do dark's nature exceptions.
   - SCHEMES.md §6 left open whether the dark palette's mixes stay. That is decided
     here, per step: `-wash` and `-tint` become tokens or mixes.
   - The contrast tests of phase 4 run on every dark scheme too.
6. **Remove every alias**: light's of phase 2, dark's of phase 3, the main set, and the
   Bulma names.
   - A search for each old name in DomUI, the demo and the site must come up empty.
   - **This phase must be done before a release.**
7. **Documentation**:
   - the site's `look-and-feel/themes` page on the roles;
   - in `moving-to-modules`, a complete table from every removed name to its role, since
     applications have to change at the upgrade;
   - IMPROVEMENT-LOG.

### 6.1 Phases 1 and 2, as done (2026-10-09)

- `light/winter/_scheme.scss` holds the 91 roles, and only them. Its old tokens are gone.
  `light/_palette.scss` writes their values out where it used to read them, so for now
  every old name keeps exactly the value it had.
  - This is a small change from the plan, where phase 2 made the old names aliases of
    the roles. Done that way, the light theme would have changed all at once. Instead it
    changes group by group in phase 3, as the components move to roles.
- Both palettes end with the 91 roles, so the theme module offers them (`t.$surface-page`)
  and an application can set them.
  - Light passes on its scheme's roles.
  - Dark works them out roughly from its old names and tokens (`$surface-raised:
    $surface-bg`, washes as mixes). These are the reverse aliases of phase 3, put in now,
    because the review page reads the roles in every scheme.
- Two roles existed already, with the same meaning and the same light value, and simply
  became roles:
  - `$info-border`, a palette name;
  - `$text-strong`, Bulma's name in `_component-colors.scss`, which is gone from there.
- Review page: the swatch strip is now a table of the roles. Each role is shown with what
  goes on it: a surface with body text, a fill with its text colour, a line as a frame. The
  intents and chrome are a grid of family × step. The demo sheet (`_themereview.scss`)
  reads the roles from the theme module by name, so a missing role is a compile error.
- **Verified:**
  - all six compiled sheets are byte for byte the sheets of before;
  - the theme tests pass;
  - the review page renders in light-winter and dark-midnight.

### 6.2 Phase 3, as done

How it goes: per group of the review page, the group's component colours in
`_component-colors.scss` become roles, and so do the colours its partials read directly
from the old palette. Since the roles replace that palette, a partial reads a role directly
where it used to read a palette name. Light's exceptions for the group go. Every sheet is
compiled before and after, and the declarations that changed are read one by one. Light
is checked on the review page, and so is Midnight.

**Text and headings, buttons** (2026-10-09):
- `_core.scss`:
  - body text is `$text-default` (was `inherit`: the browser's black);
  - links, h1's rule, `.listtbl`, `select` and the code block read roles. The code block
    is `$surface-sunken` with a `$border-default` dotted frame.
- Buttons:
  - `$colors`, the Bulma colour map, is built from the roles: `primary` ... `danger` are
    the intents' `-solid` and `-on-solid`, `link` is `$link-text`, `white` / `black` /
    `light` / `dark` are surfaces and `neutral`.
  - `$button-color-shades` is worked out from `$colors` by one function for both natures:
    hover and pressed are the fill made lighter or darker, whichever stands out (5%, 20%),
    and focus is `$focus-ring` for every colour. Light used to rotate the hue for focus
    (a green focus on the orange button); dark had literals. Section 3.1 said one function
    per nature; one turned out to be enough.
  - The plain, text, disabled and static buttons read roles.
  - Gone: Bulma's `$info` / `$success` / `$warning` / `$danger` / `$light` / `$dark`, every
    `*-invert`, `$link-invert`, `$button-link-color` / `-invert`. Light's exceptions for
    `$colors`, `$button-color-shades`, `$sib-focus-bg` and `$button-text-active-background-color`
    went with them, and so did 15 of dark's nature exceptions.
- **Dark, for phase 5:**
  - The colour buttons now take each scheme's own hues with `s.$page` text, instead of the
    light theme's hues. They read well in Midnight, but there "link" and "info" are the
    same blue.
  - The code block is darker than the page (`$surface-sunken`), where it was lighter.
  - The text in a `select` is `$text-strong`.

**Toggles and choices** (2026-10-09):
- Whatever is switched on or chosen is the `primary` family: CheckboxButton's knob and track
  while on, SwitchButton's track, the chosen item of `RadioGroup.asButtons()` and
  `.ui-boolean-btn`. These were the alternative-button blue (`#428efe`). Off is `neutral`.
  An unchosen item is a plain button.
- RadioButton's component colours were renamed. `$rbb-common-bg` and `$rbb-common-color`
  each served as both ground and text, swapped between chosen and not. They are now
  `$rbb-bg`, `$rbb-color`, `$rbb-chosen-bg`, `$rbb-chosen-color` and the rest, and
  `$rbb-disabled-color` (a ground) is `$rbb-disabled-bg`.
- CheckboxButton has three new component colours: the knob's ground while hovered off
  (it was the on-track at 90%), the knob on, and the knob's text on.
- `$disabled-text` is `#858585` (was `#999999`): 3:1 on `$disabled-surface`, which the
  contrast test asks of a disabled label. Light's "switch's other label, off" and "disabled
  radio button" are off the known list of `TestThemeContrast`.
- **Dark, for phase 5:** the toggles are each scheme's `$primary`, the light theme's orange,
  instead of its blue. Dark's rough `$disabled-text` is its `$text-muted`.

**Inputs, lookup and search** (2026-10-09):
- Bulma's input mixins (`bulmaish/_core_defs.scss`) no longer read palette names (`$border`,
  `$link`, `$input-bg`, `$input-color`, `$input-ro-bg-*`, `$bevel-*`). They read the form's
  component colours, which are field roles:
  - a read-only input is the flat `$field-surface-readonly`, where it was a two-colour
    gradient;
  - focus is `$focus-ring`;
  - the value typed in an input is `$text-strong` (was `inherit`);
  - a combo's arrow is `$text-subtle` (was Bulma's link blue).
- New component colours: `$input-value-color`, `$input-border`, `$input-ro-bg`,
  `$input-error-bg`. Every partial that paints an input's error, read-only or border state
  reads them (LookupInput, ErrorPanel, FileUpload2, ImageUpload, LabelSelector,
  SearchAsYouType, the HtmlEditor's frame).
- `$list-selected-bg` (was `$highlight2-bg`, a light exception of a pale orange) is the
  selected item of any list: a table row, a lookup popup's row, SelectOnePanel, Tree3. It is
  `$selected-wash`.
- A chosen value shown as a tag (MultipleLookupInput, EnumSetInput, LabelSelector) is one set
  of component colours, `$tag-*`, in the primary tint. They were yellow and lime.
- LookupInput's popup saying how many results there are is `info` (was green); the one
  saying none or too many is `warning` (was a loud red-orange). The popups of LookupInput,
  SearchAsYouType and SelectOnePanel are `$surface-overlay` with a `$border-strong` frame.
- Gone: Bulma's `$link`, `$link-*`, `$background`, `$border`, `$border-hover`,
  `$text-invert`, `$text-light`, `$code`, `$pre` and their grounds, and light's `$link`
  exception. Gone from both palettes: `$lui-result-*`, `$lui-warning-*`, `$mli-label-*`,
  `$esic-label-bg`, `$sayt-ok-color`, which are component colours now.
- **Dark, for phase 5:** the input shadow is 0.2, which dark's nature exception
  `$input-shadow` always asked for. Before, the mixins wrote 0.1 themselves and never read it.

**Notices** (2026-10-09):
- Every severity is its intent family:
  - ErrorMessageDiv: new `$emd-*` component colours, each severity's `-wash` and `-text`;
  - Explanation: its bar and marker are the family's `-solid`, and the "i" or "!" on the
    marker is `-on-solid`, so its contrast follows from the roles;
  - the question MsgBox's "?" marker is `info`;
  - ErrorPanel, a TabPanel tab with an error, and the Ace editor's error line share
    `$error-text-color` and `$ace-error-border`;
  - the dialog of a failed server call (`$ioe-*`) is `danger`, and its title bar has
    `-on-solid` text, where it had none.
- `.ui-hor-divider` reads `$hor-divider-bg` (`$border-subtle`) and `$hor-divider-border`
  (`$border-strong`).
- `TestThemeContrast` measures the message pairs on the new component colours, and the muted
  text on `$text-subtle`. Five pairs are off light's known list: muted text twice, the error
  text, the error message and the info message. Three are left on it: the page title and
  the two breadcrumb pairs.

**Panels and headers** (2026-10-09):
- A plain title bar (Caption, Caption2, CaptionedPanel, CaptionedHeader) reads `$caption-bg`
  and `$caption-color`: `$neutral-solid` (`#6b6b6b`, was `#7c7c7c`) with white.
- Panel and CaptionedPanel are `$surface-raised`. A Panel in light now has an edge
  (`$border-default`) and a bar (`$chrome-border`), where both were transparent: a raised
  surface the colour of the page shows by its edge.
- GenericHeader: level 2 and BLUE are `$chrome-text`, level 3 is `$text-strong` (was a navy
  of its own); ExpandHeader's bar is `$chrome-text`.
- jquery-layout's resizer is the intents: may drag `success`, at its limit `danger`, closed
  `warning`, all `-tint`. Its panes read `$layout-pane-*` and `$layout-border`.
- **Dark, for phase 5:** the title bars are each scheme's rough `$neutral-solid` (its
  `$fill-strong-bg`), where they were its `$struct` blue.

**Popups, navigation, calendar, tree, tabs, tables and editors** (2026-10-09). With these,
**light has no exceptions left**: `light/winter/_scheme-exceptions.scss` is gone, and
light's known list in `TestThemeContrast` is empty.
- Every menu floats on `$surface-overlay`, with `$hover-wash` under the pointer: PopupMenu2,
  HamburgerMenu and SimplePopupMenu. SimplePopupMenu was an inverted dark menu in light;
  it is now like the other two. A FloatingWindow is `$surface-overlay` round
  `$surface-raised` content. Modal veils (`$flw-hider-bg`, `$io-blk-bg`) are `$scrim`.
- MessageFlare is each intent's `-solid` and `-on-solid`; light's flare colours were these
  values already. The bug badge is `danger`. PopInPanel is an overlay with a `$chrome-tint`
  title (was green), and the waiting spinner is `chrome`.
- BreadCrumb2 is `chrome`: the crumbs are `$chrome-solid` with white, the current one
  `$chrome-wash` with `$chrome-text`. It was the alternative-button blue, at 2.3:1.
- Calendar:
  - the jscalendar popup lost its brown palette for the theme's: overlay, a `chrome`
    band, `$hover-wash`, `$selected-wash`, today in `primary`, the weekend in
    `$danger-text`;
  - MonthPanel's band is `$chrome-wash`, a marked day `$highlight`;
  - in the schedule, an appointment is `$category-1-wash` and a note `$category-2-wash`.
- PercentageCompleteRuler2's bar is `$chrome-solid` (was red).
- Trees:
  - Tree3's fold button is `$surface-inverse` with `$text-inverse`;
  - Tree2's hover is `$neutral-wash` with `$primary-text`;
  - the old Tree's selected item is `$selected-solid` (new `$tree-*` component colours).
- TabPanel stands on `$chrome-wash`; a tab is `$chrome-tint` with `$text-strong`, the
  selected tab the content's `$surface-raised`. ScrollableTabPanel's scroll buttons are
  `$chrome-tint`.
- DataTable:
  - the header is `$chrome-tint` with `$text-strong`, edged in `$chrome-border`;
  - the lines are `$border-subtle` and the even rows `$surface-band`;
  - a selected cell is `$selected-solid` (was a purple), a selected row `$selected-wash`;
  - hover is `$hover-wash` and `$hover-border`, through new `$dt-hover-*` and
    `$dt-selected-hover-*` component colours instead of the palette's `$row-*` names.
- DataCellTable's selected cell is `$selected-wash` (was cyan). DataPager2's buttons are
  `$neutral-wash`.
- HtmlEditor is a field. Ace's bar is `$surface-inverse`. The cookie banners are
  `neutral` and `$surface-inverse`.
- ConditionPanel's levels are the seven `$category-*-wash`. Light had ten pastels of
  its own.
- `TestThemeContrast` reads the title bar as `$caption-*`. `TestThemeVariants` checks that an
  application's custominit configures the roles (`$link-text`, `$primary-solid`), not the old
  names.
- Found on the way: `mvn install` without `clean` keeps a deleted resource in
  `target/classes` and so in the jar. The demo kept serving light's deleted exceptions
  until the stale copy was removed by hand.
- **Dark, for phase 5:**
  - Dark's rough aliases had to be adjusted for the contrast test:
    - each intent's `-on-solid` is whichever of the page and white reads best on its
      `-solid`;
    - `$chrome-wash` and `$chrome-tint` are mixed towards the page.
  - The flares keep their dark literals as nature exceptions, because the dark intents'
    solids are mid-light hues on which neither text colour reaches 4.5:1.
  - Selection in dark is the rough magenta of its palette.

**The rest** (2026-10-09):
- The last component colours that read old names are roles:
  - LogTailer, EmbeddedCode, FileUpload2, ColorPicker, OddCharacters, drag and drop;
  - the drag-drop table, whose header was a literal dark green and is now
    `$surface-inverse`;
  - CookieWarning's link and accept button, the HtmlEditor's iframe sheet, LabelSelector.
- The development-mode popup (`_devmode.scss`) was all literals. It is `$ipt-*` component
  colours on `$surface-inverse`.
- Bulma's `$text`, the last Bulma colour name, is gone.
- The demo's dark sheets (`_darkstyle.scss`, `_syntax.scss`) read roles. The syntax diff
  washes mix the intents' solids into the page instead of Bulma's hues.
- **Result:**
  - No component colour, partial, demo sheet or integration reads an old palette name.
    The old names live only in the two palettes: in light as the values they had, in dark
    as what the rough roles are made from.
  - The literals left in partials are ColorPicker's hue samples, which are samples.
  - Light has no exceptions; dark has its nature exceptions (input shadow, the
    drag-drop table's stripe, the flares).
  - Every sheet compiles and the theme tests pass, with light's known contrast list empty.

### 6.3 Phases 4 and 5, and the palettes' part of 6, as done (2026-10-10)

- **Every dark scheme declares the 97 roles** in its `_scheme.scss`, as plain values like
  light's. Its old tokens are gone (`$page`, `$struct-strip`, `$hint-wash` ...). The values
  were worked out once, by a script, from each scheme's old tokens:
  - surfaces, text, lines, fields and links are the scheme's own tokens;
  - each family's `-wash` and `-tint` are its hue mixed into the page, at most 16% and 35%,
    and less where the text on them needs it;
  - `-on-solid` is whichever of the page and white reads best, with the `-solid` moved
    until that reaches 4.5:1;
  - `-text` is lifted until it reaches 4.5:1 on the page and on its wash;
  - `control` is the scheme's link colour, which is the alternative-button colour dark
    always had. `info` is whichever of its cyan and blue is farther in hue from that.
    `chrome` is its structure blue, `neutral` its greys;
  - selection is one magenta (`#e070c0`), with a wash that keeps both text and links at
    4.5:1. The categories are the scheme's hues.

  This settles SCHEMES.md's open question about the dark palette's mixes: **a wash is a
  token**. The scheme states it, and nothing works it out at compile time.
- **Both palettes are what a nature decides:** `$color-scheme` and the roles passed on from
  the scheme. Every old name is gone from them (`$surface-bg`, `$line-color`, `$primary`,
  `$errors-*`, the greyscale ramp ...). Light's sheet compiled byte for byte the same
  without them, which shows nothing read them.
- **No exceptions are left in either nature.** `dark/_nature-exceptions.scss` is gone:
  - the flares come from the dark intents, which now pass the contrast rules;
  - the drag-drop table's stripe is `$surface-band`;
  - the input shadow is a component colour that is 0.2 on a dark nature and 0.1 on a
    light one.

  The empty default files stay, for a scheme or an application that wants an exception.
- **`TestThemeContrast` checks the roles themselves** (phase 4), in every scheme:
  - the three kinds of text on the page, a panel, an overlay and a band;
  - inverse text, input values, links;
  - text and links on the hover, selection and highlight states, and the disabled text;
  - for each of the eight families, its `-text` on the page and on its wash, text on the
    wash, strong text on the tint, and `-on-solid` on its solid.

  Next to those are 36 pairs that components put together, which guard the mapping.
  Light's list of known failures is gone: every scheme passes every pair.
- Checked on the review page: Midnight, Darcula and Nord.
- **Left for phase 6/7:** the site's look-and-feel pages still name old variables in five
  pages (34 mentions). They are rewritten with the documentation phase, together with a
  table from every removed name to its role.

## 7. Decisions (2026-10-09)

1. **Step words** `wash / tint / solid / on-solid / text / border`.
2. **`chrome`** is a family of its own, for the application's frame.
3. **Selection** must be visually far from everything else, and must not be bluish. It
   does not have to be orange. It became magenta, a hue reserved for it (3.7).
4. **Categories** (first called accents) have two steps, `-solid` and `-wash`. There are
   already many colours. They are numbered, not named after a hue (decided after phase 2).
5. **The roles are the scheme files' tokens.** No old names are kept in a release, not even
   as deprecated aliases: an application renames when it takes the new version (section 5).
   During the work, aliases may live for a few commits, so that light can be done first
   and dark later (section 6).
6. **`control`** (2026-10-10): an eighth family, the normal colour of a control in use. It is
   winter's hard blue of the alternative buttons, made dark enough for white text
   (`#2b6de8`). Toggles that are on, chosen items, chips of chosen values and DataPager2's
   buttons are `control`, not `primary`. Orange `primary` is the default action only, and
   `neutral` stays the grey. Phase 3 had put those controls in `primary`, which made
   them orange; the name `control` says what the family is for, where `secondary` would
   rank it. `info` moved to a cyan-teal (`#0b7285`), because the control blue and the
   old info blue were the same colour.
   BreadCrumb2 is `control` too (2026-10-10): its crumbs are clickable steps, so they are
   `$control-solid` with `$control-on-solid`, and the current page is `$control-wash` with
   `$control-text`. They had been `chrome` since phase 3. The old BreadCrumb keeps white text
   on its own background images.
