# DomUI theming: how it works today

An investigation of the theme subsystem as it stands in `to.etc.domui`, written as the
basis for the planned removal of obsolete functionality. It describes what is there, what
actually runs, and what is dead — with file/line references so every claim can be checked.

All paths are relative to `domui/to.etc.domui/src/main/java/to/etc/domui/` unless said
otherwise; resources live under `domui/to.etc.domui/src/main/resources/resources/`.

[TOC]

## 1. Summary

Theming is three separate mechanisms grown on top of each other, of which **only the
newest one is used**:

| Engine | Factory name | Introduced | Status |
| --- | --- | --- | --- |
| Fragmented | `fragmented` | 2011 | dead — nothing selects it, resources still shipped |
| Simple | `s` | 2011 | dead — nothing selects it, resources still shipped, has a URL bug |
| Sass/SCSS | `scss` | 2017 | **the one in use**; the framework default |

`DomApplication` hard-codes `setDefaultThemeFactory(SassThemeFactory.INSTANCE)`
(`server/DomApplication.java:614`), giving the default theme name
`scss-winter-default-default`; both the demo (`to.etc.domui.demo/.../Application.java:53`)
and the skeleton re-assert exactly that. All three factories are nevertheless registered
in a static block (`server/DomApplication.java:2908-2911`) and all their machinery — Rhino
JavaScript property scopes, `.frag.css` concatenation, `.color.js` / `icon.props.js`
inheritance, style variants — is still compiled and shipped.

Verified live against https://demo.domui.org/ :

```
GET /$THEME/scss-winter-default-default/style.scss     -> 200 text/css  472185 bytes
GET /$THEME/scss-winter-default-default/btnCancel.png  -> 200 image/png     700 bytes
GET /$THEME/scss-winter-default-default/nonexistent.png-> 404
```

## 2. The two URL forms

There are two distinct spellings, and confusing them is the single most common mistake in
this code.

**`THEME/xxx` (no dollar)** — what application and component code writes. It is a
*logical* reference meaning "a resource from whatever theme is current". It only ever
appears in Java source and in node properties (`Img.setSrc`, `setBackgroundImage`,
`Icon.of(...)`).

**`$THEME/themeName/xxx` (with dollar)** — the *resolved* form. It names one concrete
theme and is both a DomUI resource RURL and a browser-visible URL.

The conversion happens in `ThemeManager.getThemedResourceRURL()`
(`themes/ThemeManager.java:258-291`):

```java
if(path.startsWith("THEME/"))       path = path.substring(6);
else if(path.startsWith("ICON/"))   throw new IllegalStateException("Bad ROOT: ICON/...");
else                                return path;              // not theme-relative
String newicon = theme.translateResourceName(path);           // icon-name remapping
return ThemeResourceFactory.PREFIX + theme.getThemeName() + "/" + newicon;
```

`ICON/` is a third, fully removed form that survives only as this guard clause.

The entry point used by components is `NodeBase.getThemedResourceRURL()`
(`dom/html/NodeBase.java:1943`), which short-circuits absolute URLs and otherwise
delegates to the above using `UIContext.getRequestContext()`. `DomUtil.calculateImageURL()`
(`util/DomUtil.java:2337`) does the same for non-node code.

Call sites that perform the translation during rendering:

- `dom/HtmlTagRenderer.java:1234` — `<img src=...>`
- `dom/HtmlTagRenderer.java:283` — `background-image: url(...)`
- `dom/HtmlFullRenderer.java:507`, `dom/html/OptimalDeltaRenderer.java:298,305`,
  `dom/HtmlFileRenderer.java:466,522` — CSS/JS header contributors
- `parts/MarkerImagePartKey.java:75-77` — the marker-image part's icon parameter

After translation `IRequestContext.getRelativePath()` (`server/RequestContextImpl.java:486`)
prefixes the webapp URL, giving e.g. `/demo/$THEME/scss-winter-default-default/btnCancel.png`.

### Theme name grammar

A theme name is dash-separated and the **first segment is the factory name**:

```
factory - styleName - iconName - colorName [ - variant ]
scss    - winter    - default  - default
```

`DomApplication.getFactoryFromThemeName()` (`server/DomApplication.java:2763`) splits on
the first `-` and looks the prefix up in the static `THEME_FACTORIES` map. Every factory
re-splits the whole name itself and insists on 4 or 5 segments
(`SassThemeFactory.java:76-83`, `SimpleThemeFactory.java:128-135`,
`FragmentedThemeFactory.java:193-201`). A style, icon or colour name containing a dash is
therefore impossible.

## 3. The interfaces

### `IThemeFactory` (`themes/IThemeFactory.java`)

```java
String getFactoryName();                                  // URL prefix, e.g. "scss"
ITheme getTheme(DomApplication da, String themeName);     // must NOT cache; caller caches
String getDefaultThemeName();                             // INCLUDING the factory name
default String appendThemeVariant(String themeName, IThemeVariant variant) {
    return themeName + "-" + variant.getVariantName();
}
```

All three implementations are anonymous `IThemeFactory` singletons living inside the
concrete factory class (`SassThemeFactory.INSTANCE`, `SimpleThemeFactory.INSTANCE`,
`FragmentedThemeFactory.getInstance()`); the outer class is instantiated per theme
construction and thrown away. `appendThemeVariant` is never overridden.

### `ITheme` (`themes/ITheme.java`)

```java
String               getThemeName();                       // the name as it appears in URLs
ResourceDependencies getDependencies();                    // for dev-mode reload
IResourceRef         getThemeResource(String name, IResourceDependencyList rdl);
IScriptScope         getPropertyScope();                   // Rhino scope — legacy only
String               translateResourceName(String name);   // icon name remapping
String               getStyleSheetName();                  // RURL of the main stylesheet
```

Two of these six are legacy:

- `getPropertyScope()` returns the JavaScript variable scope built from `.color.js` /
  `.props.js` files. `SassTheme` cannot supply one and **throws**
  (`themes/sass/SassTheme.java:53-55`). It is consumed only by
  `ThemeManager.getThemeReplacedString()` and the deprecated `getThemeMap()`.
- `translateResourceName()` maps an icon file name to another one through the JS `icon`
  object. `SassTheme` returns the name unchanged (`SassTheme.java:57-59`);
  `SimpleTheme.java:127-138` and `FragmentedThemeStore.java:95-106` share an identical
  copy-pasted implementation.

### `IThemeVariant` (`themes/IThemeVariant.java`)

A one-method interface (`getVariantName()`) with two implementations:
`DefaultThemeVariant` ("default") and `CleanThemeVariant` ("clean").
`CleanThemeVariant` is referenced by **nothing** outside its own file.

Variants exist in three places that do not agree with each other:

- `UrlPage.setThemeVariant()` (`dom/html/UrlPage.java:86`) pushes it into the request
  context; the matching getter is commented out (`UrlPage.java:90-92`).
- `RequestContextImpl` stores it (`server/RequestContextImpl.java:132,443-450`) — and then
  `getCurrentTheme()` (line 408-417) calls `m_application.getTheme(getThemeName(), null)`,
  **without the variant**. So setting a variant on a page changes nothing.
- `ThemeManager.getTheme(name, variant, rdl)` (line 115-119) does apply it, but its only
  callers are `DomApplication.getDefaultThemeInstance()` (itself unused inside the
  framework) and the deprecated `getThemeMap()`.

The variant mechanism is effectively inert.

## 4. `ThemeManager` — instantiation and caching

`themes/ThemeManager.java` is a final class owned by `DomApplication`
(`m_themeManager`, exposed via `internalGetThemeManager()`).

`getTheme(key, rdl)` (line 121-160) is the workhorse:

1. resolve the factory from the name prefix;
2. look `key` up in `m_themeMap`;
3. a hit is returned only if its `IIsModified` says nothing changed — in production the
   dependency object is `null`, so a hit is always returned;
4. otherwise `factory.getTheme(application, key)` builds a fresh `ITheme`;
5. **in development mode only**, the theme's own `ResourceDependencies` are wrapped in a
   `ThemeModifiableResource` with a 3-second throttle (`themes/ThemeModifiableResource.java`)
   so a theme edit is picked up without re-stat-ing every fragment on every request.

`checkReapThemes()` (line 166-185) is meant to drop themes unused for five minutes. It
sorts a *copy* of the map's values and removes entries from that copy — `m_themeMap` is
never touched. **The reaper does nothing.** It also skips index `size()-1` because of the
`for(int i = list.size(); --i >= 0;)` / `list.remove(i)` combination.

`getThemeReplacedString()` (line 199-238) is the Rhino template expander: load a resource
as UTF-8, take the theme's property scope, add `browser` and whatever
`DomApplication.augmentThemeMap()` contributes, run `RhinoTemplateCompiler` over it,
return the string. It requires a `$THEME/...` URL because it calls
`ThemeResourceFactory.splitThemeResourceURL()` on it.

It has four call sites, and **none of them can be reached under the SCSS engine**:

| Caller | Fires for | Why it never runs today |
| --- | --- | --- |
| `themes/ThemePartFactory.java:149` | `*.theme.*` URLs | `themes/scss/winter/` contains no `.theme.` resource at all |
| `parts/SvgPartFactory.java:112` | `*.png.svg` URLs | no `.png.svg` resource ships (they sit in the non-packaged `extra/`) |
| `parts/PartUtil.java:143` (`loadSvg`, from `GrayscalerPart` and `MarkerImagePart`) | image names ending in `svg` | the live theme contains no `.svg` file, and `ImageIconRef` routes `.svg` to the inlining `SvgIcon`, not to `<img>` |
| `parts/PartUtil.java:69` (`loadProperties`) | — | no callers anywhere |

The guarantee is structural rather than accidental: the first thing
`getThemeReplacedString` does with the resolved theme is call
`theme.getPropertyScope()` (line 209), and `SassTheme` **throws**
`IllegalStateException("Cannot do this as I'm not javascript based.")` on that
(`themes/sass/SassTheme.java:53-55`). Any of the four paths reached with an SCSS theme
would blow up rather than render — which is exactly the evidence that they are not
reached. (Reached with a non-`$THEME/` URL, `splitThemeResourceURL` throws first.)

What the Rhino scope did for the old themes — inject theme variables into a text resource
before serving it — is done for SCSS by the synthesised `_parameters.scss` (§7) instead.

## 5. Resource resolution: `$THEME/...` to bytes

`ThemeResourceFactory` (`themes/ThemeResourceFactory.java`) is an `IResourceFactory`
registered at `server/DomApplication.java:619`. Resource factories are scored by
`accept()`; this one returns 30 for anything starting with `$THEME/`, beating
`SimpleResourceFactory` (10 for any `$…`) and `ClassRefResourceFactory` (10 for `$RES/`).

```
$THEME/scss-winter-default-default/btnCancel.png
       └──────── theme name ─────┘ └ file name ┘   (split on the FIRST slash)
```

`getResource()` then asks `DomApplication.getTheme(themeName, rdl)` for the `ITheme` and
delegates to `theme.getThemeResource(fileName, rdl)`, throwing `ThingyNotFoundException`
when the result does not exist.

Each `ITheme` implements `getThemeResource` as a **search path walk** — try
`<dir>/<name>` for each directory in order, return the first that exists:

- `SassTheme.java:88-101`, forward order over `m_searchPath`
- `SimpleTheme.java:100-125`, forward order, returns a bespoke non-existent ref instead of
  the `IResourceRef.NONEXISTENT` constant that already exists
- `FragmentedThemeStore.java:108-137`, **reverse** order over the inheritance stack, plus a
  special case returning the pre-built stylesheet bytes for `style.theme.css`; also builds
  its own non-existent ref

The search-path entries are themselves DomUI RURLs (`$themes/scss/winter`), so the walk
recurses into `SimpleResourceFactory` → `DomApplication.getAppFileOrResource()`
(`server/DomApplication.java:1677-1696`), which tries, in order: a file under the webapp
directory, a reloading classpath ref under `META-INF/resources/` (dev mode only), a
servlet-container fragment resource, and finally a classpath resource under `/resources/`.

This is what makes a theme overridable: dropping `themes/scss/winter/btnCancel.png` into
the webapp shadows the one in the DomUI jar.

## 6. Serving: the request round trip

Requests reach `PartRequestHandler` (priority 80, ahead of `ApplicationRequestHandler` at
50 — `server/DomApplication.java:622-623`), which calls `PartService.render()`.
`PartService.findPart()` (`server/parts/PartService.java:145-155`) tries class-based parts
(`xxx.part/…`) first, then URL matchers **in registration order**
(`server/DomApplication.java:703-707`):

| # | Part factory | Matches | Purpose |
| --- | --- | --- | --- |
| 1 | `SassPartFactory` | `*.scss`, `*.sass` | compile SCSS → CSS |
| 2 | `ThemePartFactory` | `*.theme.*` | Rhino-expand a themed text resource |
| 3 | `SvgPartFactory` | `*.png.svg` | Batik-rasterise a themed SVG to PNG |
| 4 | `InternalResourcePart` | path starts with `$` | serve a resource verbatim |

Results are cached by `PartService` in a 16 MB LRU keyed on whatever the factory's
`decodeKey()` returns, and re-checked against `ResourceDependencies` on every hit — in
production too, deliberately (`PartService.java:280-287`).

### 6.1 An icon: `$THEME/scss-winter-default-default/btnCancel.png`

Matchers 1-3 miss, matcher 4 hits. `InternalResourcePart.decodeKey` refuses extension-less
paths (403) and `.class` (404), then `generate()` calls `da.getResource(rurl, …)` — which
lands in `ThemeResourceFactory` as described above, resolving to
`resources/themes/scss/winter/btnCancel.png` in the DomUI jar. MIME from the extension,
`Expires` set from `getDefaultExpiryTime()` in production only.

### 6.2 The stylesheet: `$THEME/scss-winter-default-default/style.scss`

`HtmlFullRenderer.renderThemeCSS()` (`dom/HtmlFullRenderer.java:478-491`) emits

```html
<link rel="stylesheet" type="text/css" href="<ctx>/$THEME/<theme>/style.scss?$hash=<hex>">
```

from `ITheme.getStyleSheetName()`. `SassTheme.getStyleSheetName()`
(`themes/sass/SassTheme.java:63-75`) computes that hash by **compiling the sheet right
there**, through `PartService.getData()`, and hashing the result. That warms the part
cache, so the browser's follow-up request is a cache hit (`$hash` is stripped from the key
by `SassPartFactory.decodeKey`, which drops every `$`-prefixed parameter). It reaches into
`UIContext.getRequestContext()` for the browser version and is annotated `FIXME Fugly!!`
in the source.

The compile itself: `SassPartFactory.generate()` → `SassCompilerFactory.createCompiler()`
→ `JSassCompiler` (libsass via jsass). Imports are resolved by `JSassResolver` /
`AbstractSassResolver` (`sass/AbstractSassResolver.java`), which:

- resolves relative to the importing file's directory, honouring `.` and `..`;
- tries the SCSS partial form (`_name.scss`) before the plain one;
- resolves through `DomApplication.getResource()`, so an import inside
  `$THEME/<theme>/style.scss` searches the theme's search path — this is how a colour or
  icon directory earlier in the path overrides `_color.scss`;
- **synthesises `_parameters.scss`** rather than reading it from disk (see §7).

`?__nomap=true` disables the embedded source map (`JSassCompiler.java:60-64`); the offline
renderer sets it (`dom/HtmlFileRenderer.java:412`), the normal one does not.

### 6.3 The offline renderer differs

`HtmlFileRenderer` (`dom/HtmlFileRenderer.java:402-425`) inlines the compiled CSS in a
`<style>` block instead of linking it, and takes the theme from
`DomApplication.getDefaultThemeName()` rather than from the request context — so a
per-user theme is ignored there. `renderLoadCSS` (line 465-501) similarly inlines
contributor stylesheets, falling back to the webapp file and then a classpath resource.

## 7. Getting values *into* a stylesheet

Under SCSS there is exactly one channel: the generated `_parameters.scss`
(`sass/AbstractSassResolver.java:243-270`). `style.scss` imports it first
(`resources/themes/scss/winter/style.scss:5`). Its content is built from:

1. every URL parameter of the request that does not start with `__`, as `$name: value;`
   (a name ending in `$` marks the value as a string to be quoted);
2. `DomApplication.getThemeProperties()` — the map fed by `setThemeProperty(name, value)`
   (`server/DomApplication.java:2508-2523`);
3. whatever `IThemeVariablesCalculator.calculate(parameters)` returns
   (`themes/sass/IThemeVariablesCalculator.java`, plugged in via
   `setThemeVariablesCalculator`, default `parameters -> Map.of()`).

A physical `_parameters.scss` also exists in the theme directory but is never read — the
resolver intercepts the name before any lookup. Its content says as much: *"its content
serves as a reminder only"*.

Two further override hooks are plain empty files in the theme, imported by `style.scss`
and meant to be shadowed by a webapp copy: `_custominit.scss` (imported second, before the
variables) and `_userstyle.scss` (imported after them).

The legacy channel is `DomApplication.augmentThemeMap(IScriptScope)`
(`server/DomApplication.java:2479-2484`), which injects a `ThemeCssUtils` helper as `util`
plus the theme properties into the Rhino scope. `ThemeCssUtils` (`themes/ThemeCssUtils.java`)
and the 541-line `themes/CssColor.java` exist purely to be called from `.color.js` files;
no Java code constructs either.

## 8. Icons

`themes/Theme.java` is an enum of ~60 framework icon slots (`BTN_CANCEL`, `ICON_MBX_ERROR`,
…) implementing `IIconRef`, backed by a static `ConcurrentHashMap` that a static
initialiser fills with `Icon.of("THEME/xxx.png")` and that `Theme.update(key, icon)` lets
an application re-point.

`Icon.of(String)` (`component/misc/Icon.java:732`) returns an `ImageIconRef`, whose
`createNode()` (`component/misc/ImageIconRef.java:45-54`) branches on the extension:

- `.svg` → `SvgIcon`, which fetches the resource and **inlines the SVG markup** into the
  page (`component/misc/SvgIcon.java`);
- no extension → `FontIcon`, i.e. a CSS class on a `<span>` (FontAwesome-style);
- anything else → `ImgIcon`, a `<span>` wrapping an `<img src="THEME/…">` that
  `HtmlTagRenderer.visitImg` translates at render time.

So the `THEME/` prefix in an icon path survives untranslated in the DOM tree and is only
resolved during rendering. `translateResourceName()` gets its chance at that same moment —
under SCSS it is a no-op, under the legacy engines it consulted the JS `icon` object, which
is what let an icon set substitute one file for another.

A scan of every `"THEME/…"` literal in the framework and demo (93 distinct paths) against
`resources/themes/scss/winter/` finds two genuinely dangling references:

- `THEME/big-accessDenied.png` — `themes/Theme.java:140` (`BIG_ACCESS_DENIED`)
- `THEME/btnBack.png` — `component/misc/InternalParentTree.java:219`

(The apparent misses `big-`, `mini-`, `btnHeaderCollapsed`, `btnHeaderExpanded` are
prefixes concatenated at runtime and do resolve.)

## 9. Where the theme name comes from

```
RequestContextImpl.getThemeName()            server/RequestContextImpl.java:423-434
  ├─ field m_themeName, if already computed
  ├─ session attribute "ctx$themename", set by setThemeName()
  └─ DomApplication.calculateUserTheme(ctx)  -> getDefaultThemeName()  (override point)
```

`calculateUserTheme` (`server/DomApplication.java:2575`) is the documented hook for a
per-user theme; it is not overridden anywhere in this workspace. `setDefaultThemeName` and
`setDefaultThemeFactory` (line 2494 / 2534) set the application-wide default; the latter
just assigns the factory's own default name — note it stores **only the name**, so the
"default factory" is not remembered as such, it is re-derived from the name prefix on
every lookup.

The theme name is also carried in `IPageParameters`
(`state/IBasicParameterContainer.java:47`, `state/MapParameterContainer.java:179`) and
participates in `equals`/`hashCode` (`MapParameterContainer.java:120-150`), which is what
keeps compiled parts of different themes apart in the part cache.

## 10. The two legacy engines, briefly

### Simple (`themes/simple/`, factory `s`, default `s-blue-blue-blue`)

Name `s-<style>-<icon>-<color>`. Loads three JavaScript property files into one Rhino
scope (`<color>.color.js`, `<icon>.icons.js`, `css-<style>/style.props.js`) and builds a
four-entry search path: `<icon>-icons`, `<color>-colors`, `css-<style>`, `all`. The
stylesheet is `style.theme.css`, served by `ThemePartFactory`.

Its `getStyleSheetName()` has a **double-slash bug**:
`PREFIX + m_themeName + "/" + "/style.theme.css"` (`themes/simple/SimpleTheme.java:81`).

Of its default `s-blue-blue-blue`, `themes/blue.icons.js` is a zero-byte file,
`themes/blue-icons/` does not exist, and neither does `themes/all/`.

### Fragmented (`themes/fragmented/`, factory `fragmented`, default `fragmented-domui-orange-domui`)

The most elaborate of the three, and the only one supporting variants meaningfully. Name
`fragmented-<style>-<icon>-<color>[-<variant>]`. It:

- loads colours from `themes/<color>.color.js`, icons from `themes/<icon>-icons/icon.props.js`,
  style from `themes/css-<style>/style.props.js`, each of which may call `inherit('name')`
  to pull in a parent set — implemented by evaluating
  `function inherit(s) { collector.internalInheritXxx(s); }` in the Rhino scope and
  calling back into Java (`FragmentedThemeFactory.java:346-352`);
- builds the master stylesheet at theme-construction time by concatenating **all
  `*.frag.css` files** found across the inheritance stack, sorted by file name, from both
  the classpath (`ClasspathInventory.getPackageInventory`) and the webapp directory
  (`FragmentedThemeFactory.java:452-500`);
- appends `-<variant>` to the style directory name, which is what `css-domui-clean/` and
  `domui-icons-clean/` are for;
- carries a hard-coded customer workaround: `.replace("domui.", "orange.")` on the colour
  file name, commented *"jsavic 20121107: reported workaround - temporary"*
  (`FragmentedThemeFactory.java:319`).

`Check.CHECK` in `appendFragment` compiles each fragment as a Rhino template purely to
validate it, then appends the **unexpanded** source — the expanded result is discarded and
`Check.NONE` is never used.

## 11. Resources currently shipped

`resources/themes/`:

| Path | Files | Belongs to | Live? |
| --- | --- | --- | --- |
| `scss/winter/` (+ `input/`, `bulmaish/`) | 280 (114 `.scss`, 164 images) | sass | **yes** |
| `css-blue/` | 166 | simple / fragmented | no |
| `blue/` | 153 | simple (orphan: no factory builds this path) | no |
| `domui-icons/` | 152 | fragmented | no |
| `css-domui/` | 88 | fragmented | no |
| `css-domui-clean/` | 88 | fragmented, `clean` variant | no |
| `blue-colors/`, `green-colors/`, `orange-colors/` | 3 each | simple / fragmented | no |
| `domui-icons-clean/` | 2 | fragmented, `clean` variant | no |
| `blue.color.js`, `green.color.js`, `orange.color.js`, `blue.icons.js` (0 bytes) | 4 | simple / fragmented | no |

152 `.frag.css` files in total. Only `scss/winter` is reachable from the default theme,
and `scss/` contains no `all/` directory even though `SassThemeFactory` puts
`$themes/scss/all` on the search path.

## 12. Dead, broken and questionable — the cleanup list

**Certainly dead**

1. `themes/simple/` (2 classes) and its resources — no configuration selects factory `s`.
2. `themes/fragmented/` (2 classes, ~730 lines) and its resources — likewise for
   `fragmented`. It is `ClasspathInventory.getPackageInventory`'s only production caller
   (the other is that class's own `main`), so that method becomes dead with it.
3. `themes/ThemePartFactory.java` — matches `*.theme.*`; the only such resources are the
   two legacy stylesheets. Its `Key` still carries a `BrowserVersion` and an `iv`
   cache-buster.
4. `parts/SvgPartFactory.java` — matches `*.png.svg`; the only `.png.svg` files in the
   repo are in `to.etc.domui/extra/`, which is not a source or resource root, so none are
   ever served. (This does **not** free the Batik dependency: `parts/PartUtil.java`
   still uses the transcoder from `GrayscalerPart` and `MarkerImagePart`.)
5. `ThemeManager.getThemeReplacedString()` (both overloads) — its four call sites are 3, 4
   and the two in `parts/PartUtil.java`; none is reachable under SCSS (§4). Note this also
   makes `PartUtil.loadProperties()` (no callers) dead, and reduces `PartUtil.loadSvg()`
   to a plain resource read — or removes SVG support from `GrayscalerPart` /
   `MarkerImagePart` altogether, which is a decision to take rather than assume.
6. `ThemeManager.getThemeMap()` — already `@Deprecated`, no callers.
7. `themes/CleanThemeVariant.java` — referenced nowhere.
8. `themes/ThemeCssUtils.java` and `themes/CssColor.java` — reachable only from `.color.js`
   files, which only the legacy engines load.
9. `ITheme.getPropertyScope()` — `SassTheme` throws on it; after 3-6 nothing calls it.
10. `ITheme.translateResourceName()` — a no-op under SCSS; after 1-2 the only remaining
    implementation is `return name`.
11. `DomApplication.augmentThemeMap()` — after 9 there is no scope to augment.
    (`ThemeProperty` values reach SCSS through `_parameters.scss` instead, which is a
    separate path and must stay.)
12. `themes/ITheme.getDependencies()` — `SassTheme` always returns an empty
    `ResourceDependencies` (`SassThemeFactory.java:127`), so the dev-mode reload it feeds
    is a no-op for the only live engine. Note the SCSS reload people actually rely on comes
    from `PartService`'s own dependency check, not from here.

**Broken**

13. `ThemeManager.checkReapThemes()` mutates a copy — themes are never reaped (§4).
14. `SimpleTheme.getStyleSheetName()` emits a double slash (§10).
15. `resources/css/jquery-ui.theme.css` matches `ThemePartFactory.MATCHER`; requesting it
    would throw `IllegalArgumentException` from `splitThemeResourceURL` because it is not a
    `$THEME/` URL. Nothing references it today, so it is latent — and it disappears with 3.
16. `THEME/big-accessDenied.png` and `THEME/btnBack.png` do not exist in the live theme (§8).
17. `SassPartFactory.decodeKey` and `SassTheme.getStyleSheetName` pin
    `BrowserVersion.INSTANCE` (a hard-coded Chrome 60 UA string,
    `server/BrowserVersion.java:46`) while `ThemePartFactory` keys on the real one. Browser
    conditionals in stylesheets are therefore already effectively gone; `BrowserVersion` in
    the theme path is vestigial.

**Design questions for the rework**

18. The variant mechanism is dead in the water (§3): `RequestContextImpl.getCurrentTheme()`
    drops the variant that `UrlPage.setThemeVariant()` sets, and the getter is commented
    out. Either wire it up or drop `IThemeVariant` entirely.
19. The 4-segment theme name is over-parameterised for SCSS: `iconName` and `colorName` are
    almost always `default`, and the SCSS `@import` override trick makes the search path a
    better fit for them than a URL segment. With one style there is no reason for the name
    to be more than `scss-winter`.
20. `getStyleSheetName()` compiling the whole sheet to compute a cache-busting hash, from
    inside a getter, reaching into `UIContext` for a browser version it then discards, is
    the single ugliest part of the live path (its own `FIXME Fugly!!`).
21. `SimpleTheme` and `FragmentedThemeStore` each carry a private copy of a non-existent
    `IResourceRef` although `IResourceRef.NONEXISTENT` exists — moot once they are removed,
    but the same pattern should not reappear.
22. `IThemeFactory.getTheme()` is documented as "must not cache", yet `ThemeManager` is the
    only caller and the only cache — worth stating in the interface as a contract rather
    than a comment.
23. Rhino (`org.mozilla:rhino`, `to.etc.domui/pom.xml:92-95`) **cannot** be dropped with the
    themes: `dom/HtmlFullRenderer.java:134-164` and `server/OopsFrameRenderer.java` use
    `RhinoTemplate` for the page body and error frame templates. Only the theme-side uses
    (`RhinoExecutor`, `IScriptScope`, `RhinoScriptScope`) go away.
24. `src/test/java/to/etc/domui/test/theme/TestRhino.java` tests the icon-remapping
    behaviour of item 10 and goes with it.

## 13. Live path, condensed

Everything that actually runs today, end to end:

```
page render
  HtmlFullRenderer.renderThemeCSS()
    RequestContextImpl.getCurrentTheme()          session / calculateUserTheme -> "scss-winter-default-default"
      ThemeManager.getTheme(name, null)           cache; SassThemeFactory.INSTANCE.getTheme(...)
        SassThemeFactory.createTheme()            search path: [$themes/scss/winter, $themes/scss/all]
    SassTheme.getStyleSheetName()
      PartService.getData($THEME/<theme>/style.scss)   -> compiles + caches, returns hash
    <link href=".../$THEME/<theme>/style.scss?$hash=...">

browser GET /$THEME/<theme>/style.scss?$hash=...
  PartRequestHandler -> PartService.render -> SassPartFactory (cache hit)
    JSassCompiler + JSassResolver
      imports resolve via DomApplication.getResource -> ThemeResourceFactory
        -> SassTheme.getThemeResource -> search path -> classpath /resources/themes/scss/winter/...
      "parameters" import -> synthesised from URL params + setThemeProperty + IThemeVariablesCalculator

browser GET /$THEME/<theme>/btnCancel.png
  PartService.render -> InternalResourcePart ("$" prefix)
    DomApplication.getResource -> ThemeResourceFactory -> SassTheme.getThemeResource
      -> /resources/themes/scss/winter/btnCancel.png
```

Nine of the classes under `themes/` and one under `parts/` play no part in that diagram.
