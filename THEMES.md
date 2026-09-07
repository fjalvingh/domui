# DomUI theming

How theming works, after the removal of the two obsolete theme engines and the Rhino
template machinery. The last section records what was removed and what is still open.

All paths are relative to `domui/to.etc.domui/src/main/java/to/etc/domui/` unless said
otherwise; resources live under `domui/to.etc.domui/src/main/resources/resources/`.

[TOC]

## 1. Summary

There is **one** theme engine: the SCSS one. `DomApplication` sets it as the default
(`server/DomApplication.java:611`), giving the theme name `scss-winter-default-default`,
and it is the only entry in the `THEME_FACTORIES` registry.

A theme is a **search path of directories** holding one `style.scss` plus the images that
stylesheet and the components refer to. Serving it is two things: compiling the SCSS to
CSS on demand, and resolving `THEME/xxx` image references against that search path.

## 2. The two URL forms

There are two distinct spellings, and confusing them is the single most common mistake in
this code.

**`THEME/xxx` (no dollar)** — what application and component code writes. It is a
*logical* reference meaning "a resource from whatever theme is current". It appears in
Java source and in node properties (`Img.setSrc`, `setBackgroundImage`, `Icon.of(...)`),
and survives untranslated in the server-side DOM tree.

**`$THEME/themeName/xxx` (with dollar)** — the *resolved* form. It names one concrete
theme and is both a DomUI resource RURL and a browser-visible URL.

The conversion happens at render time, in `ThemeManager.getThemedResourceRURL()`
(`themes/ThemeManager.java:203-216`):

```java
if(path.startsWith("THEME/"))       path = path.substring(6);
else if(path.startsWith("ICON/"))   throw new IllegalStateException("Bad ROOT: ICON/...");
else                                return path;              // not theme-relative
String newicon = theme.translateResourceName(path);           // icon-name remapping hook
return ThemeResourceFactory.PREFIX + theme.getThemeName() + "/" + newicon;
```

`ICON/` is a third, long-removed form that survives only as this guard clause.

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

`DomApplication.getFactoryFromThemeName()` (`server/DomApplication.java:2748`) splits on
the first `-` and looks the prefix up in `THEME_FACTORIES`. `SassThemeFactory` re-splits
the whole name itself and insists on 4 or 5 segments (`SassThemeFactory.java:70-77`), so a
style, icon or colour name containing a dash is impossible.

`iconName` and `colorName` only do something when they are *not* `default`: each prepends
a directory to the search path, which is how a `_color.scss` or an icon file gets
overridden (see §5). With one shipped style nothing uses that, so the last three segments
are `winter-default-default` everywhere.

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

`SassThemeFactory.INSTANCE` is an anonymous singleton inside the concrete factory class;
the outer class is instantiated per theme construction and thrown away.

### `ITheme` (`themes/ITheme.java`)

```java
String               getThemeName();                       // the name as it appears in URLs
ResourceDependencies getDependencies();                    // for dev-mode reload
IResourceRef         getThemeResource(String name, IResourceDependencyList rdl);
String               translateResourceName(String name);   // icon name remapping hook
String               getStyleSheetName();                  // RURL of the main stylesheet
```

`translateResourceName` is a no-op in `SassTheme` (`SassTheme.java:51-53`). It is kept as
the extension point it is — an `ITheme` of your own can still substitute one icon file for
another — but nothing in the framework uses it any more.

### `IThemeVariant` (`themes/IThemeVariant.java`)

A one-method interface (`getVariantName()`) with one implementation, `DefaultThemeVariant`
("default"). See open issue 1 in §9: the mechanism does not currently do anything.

## 4. `ThemeManager` — instantiation and caching

`themes/ThemeManager.java` is a final class owned by `DomApplication`
(`m_themeManager`, exposed via `internalGetThemeManager()`).

`getTheme(key, rdl)` (line 109-148) is the workhorse:

1. resolve the factory from the name prefix;
2. look `key` up in `m_themeMap`;
3. a hit is returned only if its `IIsModified` says nothing changed — in production the
   dependency object is `null`, so a hit is always returned;
4. otherwise `factory.getTheme(application, key)` builds a fresh `ITheme`;
5. **in development mode only**, the theme's own `ResourceDependencies` are wrapped in a
   `ThemeModifiableResource` with a 3-second throttle (`themes/ThemeModifiableResource.java`)
   so a theme edit is picked up without re-stat-ing every fragment on every request.

## 5. Resource resolution: `$THEME/...` to bytes

`ThemeResourceFactory` (`themes/ThemeResourceFactory.java`) is an `IResourceFactory`
registered at `server/DomApplication.java:616`. Resource factories are scored by
`accept()`; this one returns 30 for anything starting with `$THEME/`, beating
`SimpleResourceFactory` (10 for any `$…`) and `ClassRefResourceFactory` (10 for `$RES/`).

```
$THEME/scss-winter-default-default/btnCancel.png
       └──────── theme name ─────┘ └ file name ┘   (split on the FIRST slash)
```

`getResource()` then asks `DomApplication.getTheme(themeName, rdl)` for the `ITheme` and
delegates to `theme.getThemeResource(fileName, rdl)`, throwing `ThingyNotFoundException`
when the result does not exist.

`SassTheme.getThemeResource` (`SassTheme.java:79-92`) is a **search path walk** — try
`<dir>/<name>` for each directory in order, return the first that exists. The path is
built in `SassThemeFactory.createTheme()`:

```
$themes/scss/<style>/<color>-color     (only when color   != "default")
$themes/scss/<style>/<icon>-icons      (only when icon    != "default")
$themes/scss/<style>
$themes/scss/all
```

The entries are themselves DomUI RURLs, so the walk recurses into `SimpleResourceFactory`
→ `DomApplication.getAppFileOrResource()` (`server/DomApplication.java:1674-1693`), which
tries, in order: a file under the webapp directory, a reloading classpath ref under
`META-INF/resources/` (dev mode only), a servlet-container fragment resource, and finally
a classpath resource under `/resources/`.

This is what makes a theme overridable: dropping `themes/scss/winter/btnCancel.png` into
the webapp shadows the one in the DomUI jar.

## 6. Serving: the request round trip

Requests reach `PartRequestHandler` (priority 80, ahead of `ApplicationRequestHandler` at
50 — `server/DomApplication.java:619-620`), which calls `PartService.render()`.
`PartService.findPart()` (`server/parts/PartService.java:145-155`) tries class-based parts
(`xxx.part/…`) first, then URL matchers **in registration order**
(`server/DomApplication.java:697-700`):

| # | Part factory | Matches | Purpose |
| --- | --- | --- | --- |
| 1 | `SassPartFactory` | `*.scss`, `*.sass` | compile SCSS → CSS |
| 2 | `InternalResourcePart` | path starts with `$` | serve a resource verbatim |

Results are cached by `PartService` in a 16 MB LRU keyed on whatever the factory's
`decodeKey()` returns, and re-checked against `ResourceDependencies` on every hit — in
production too, deliberately (`PartService.java:280-287`).

### 6.1 An icon: `$THEME/scss-winter-default-default/btnCancel.png`

Matcher 1 misses, matcher 2 hits. `InternalResourcePart.decodeKey` refuses extension-less
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
(`themes/sass/SassTheme.java:54-66`) computes that hash by **compiling the sheet right
there**, through `PartService.getData()`, and hashing the result. That warms the part
cache, so the browser's follow-up request is a cache hit (`$hash` is stripped from the key
by `SassPartFactory.decodeKey`, which drops every `$`-prefixed parameter). It reaches into
`UIContext.getRequestContext()` for a browser version it then discards, and is annotated
`FIXME Fugly!!` in the source (open issue 3).

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

There is exactly one channel: the generated `_parameters.scss`
(`sass/AbstractSassResolver.java:243-270`). `style.scss` imports it first
(`resources/themes/scss/winter/style.scss:5`). Its content is built from:

1. every URL parameter of the request that does not start with `__`, as `$name: value;`
   (a name ending in `$` marks the value as a string to be quoted);
2. `DomApplication.getThemeProperties()` — the map fed by `setThemeProperty(name, value)`
   (`server/DomApplication.java:2489-2504`);
3. whatever `IThemeVariablesCalculator.calculate(parameters)` returns
   (`themes/sass/IThemeVariablesCalculator.java`, plugged in via
   `setThemeVariablesCalculator`, default `parameters -> Map.of()`).

A physical `_parameters.scss` also exists in the theme directory but is never read — the
resolver intercepts the name before any lookup. Its content says as much: *"its content
serves as a reminder only"*.

Two further override hooks are empty files in the theme, imported by `style.scss` and
meant to be shadowed by a webapp copy: `_custominit.scss` (imported second, before the
variables) and `_userstyle.scss` (imported after them).

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

`GrayscalerPart` and `MarkerImagePart` are the two parts that load a themed image
server-side, through `PartUtil.loadImage()`: GIF, JPEG and PNG via `ImaTool`, SVG via the
Batik transcoder at the size the SVG itself declares. See open issue 4 — the SVG branch
cannot currently run.

A scan of every `"THEME/…"` literal in the framework and demo (93 distinct paths) against
`resources/themes/scss/winter/` finds two dangling references:

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

`calculateUserTheme` (`server/DomApplication.java:2556`) is the documented hook for a
per-user theme; nothing in this workspace overrides it. `setDefaultThemeName` and
`setDefaultThemeFactory` set the application-wide default; the latter just assigns the
factory's own default name — note it stores **only the name**, so the "default factory" is
not remembered as such, it is re-derived from the name prefix on every lookup.

The theme name is also carried in `IPageParameters`
(`state/IBasicParameterContainer.java:47`, `state/MapParameterContainer.java:179`) and
participates in `equals`/`hashCode` (`MapParameterContainer.java:120-150`), which is what
keeps compiled parts of different themes apart in the part cache.

## 10. Live path, condensed

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

## 11. What was removed

The two pre-2017 engines and the Rhino template machinery they shared. All of it was
unreachable under the SCSS engine — structurally so, because the first thing the template
expander did was call `ITheme.getPropertyScope()`, on which `SassTheme` threw.

**Theme engines**

- `themes/simple/` — `SimpleTheme`, `SimpleThemeFactory` (factory prefix `s`)
- `themes/fragmented/` — `FragmentedThemeFactory`, `FragmentedThemeStore` (prefix
  `fragmented`), including the `.frag.css` concatenation, the `inherit()` callback into
  Java, and the 2012 `.replace("domui.", "orange.")` customer workaround
- their registration in `DomApplication`'s static block

**Rhino template machinery**

- `ThemeManager.getThemeReplacedString()` (both overloads) and the deprecated
  `ThemeManager.getThemeMap()`
- `ITheme.getPropertyScope()`, and the `IScriptScope` field/parameter it forced through
  `SassTheme`'s constructor — plus the 40-line do-nothing `IScriptScope` that
  `SassThemeFactory` had to build to satisfy it
- `DomApplication.augmentThemeMap(IScriptScope)`
- `themes/ThemeCssUtils.java` and `themes/CssColor.java`, which existed only to be called
  from `.color.js` files

**Parts**

- `themes/ThemePartFactory.java` (`*.theme.*`) and `parts/SvgPartFactory.java`
  (`*.png.svg`), with their registrations
- `PartUtil.loadProperties()` (no callers) and `PartUtil`'s URL-parameter splitting.
  `PartUtil.loadSvg()` now takes the resource stream and rasterises it at the size the SVG
  declares, with no theme expansion and no `w`/`h` parameters.

**Other**

- `themes/CleanThemeVariant.java` — named the fragmented engine's `clean` style directory,
  which is gone
- `src/test/.../TestRhino.java`, `extra/TestThemeExpander.java`, `makeunsplit.xml` (an Ant
  script that rebuilt `css-blue` from the now-deleted `css-domui`), and the orphaned
  `extra/*.png.svg` + `extra/defaultbutton.properties`
- every theme resource directory except `scss/`: `blue/`, `blue-colors/`, `css-blue/`,
  `css-domui/`, `css-domui-clean/`, `domui-icons/`, `domui-icons-clean/`, `green-colors/`,
  `orange-colors/` and the four `*.color.js` / `*.icons.js` files — 152 `.frag.css` files
  among them

Net: 693 files, ~28,300 lines. Rhino itself stays — `HtmlFullRenderer.renderTemplatePage()`
and `OopsFrameRenderer` use `RhinoTemplate` for page and error-frame templates, which has
nothing to do with theming.

Verified after the change: full `mvn21 clean install` succeeds, and against a locally run
demo `$THEME/<theme>/style.scss` (200, 472 KB of CSS), `$THEME/<theme>/btnCancel.png`,
`GrayscalerPart` and `MarkerImagePart` all still serve.

## 12. Still open

1. **The variant mechanism is inert.** `UrlPage.setThemeVariant()`
   (`dom/html/UrlPage.java:86`) pushes a variant into the request context and its getter is
   commented out; `RequestContextImpl.getCurrentTheme()` (line 408-417) then calls
   `getTheme(getThemeName(), null)` **without** it. The only caller of the variant-aware
   `ThemeManager.getTheme(name, variant, rdl)` is
   `DomApplication.getDefaultThemeInstance()`, itself unused in the framework. Either wire
   it up or drop `IThemeVariant`.
2. **The theme name is over-parameterised.** With one style and the SCSS `@import`
   override trick doing the work of icon/colour sets, `scss-winter-default-default` carries
   two segments that are always `default`. `scss-winter` would say the same thing.
3. **`getStyleSheetName()` compiles the whole stylesheet** to compute a cache-busting hash,
   from inside a getter, reaching into `UIContext` for a browser version it discards
   (`SassTheme.java:54-66`, its own `FIXME Fugly!!`).
4. **SVG rasterisation cannot run.** `PartUtil.loadSvg()` throws
   `NoClassDefFoundError: org/w3c/dom/svg/SVGDocument`, because `to.etc.domui/pom.xml`
   deliberately excludes `batik-ext` — commit e611a2382 (2019-02-10), *"exclude batik-ext
   because it (partially) duplicates org.w3c.dom package"* for the Java 10 build. Nothing
   else on the classpath provides those interfaces. This predates the cleanup: the old code
   threw earlier (in the template expander) and never reached Batik at all. Fixing it means
   either finding a JPMS-safe source for `org.w3c.dom.svg` or replacing Batik.
5. **`ThemeManager.checkReapThemes()` does nothing.** It sorts a *copy* of the map's values
   and removes entries from that copy; `m_themeMap` is never touched
   (`ThemeManager.java:151-172`). It also skips the last element because of the
   `for(int i = list.size()-1; --i >= 0;)` / `list.remove(i)` combination.
6. **Two dangling icon references** — `THEME/big-accessDenied.png` and `THEME/btnBack.png`
   (§8).
7. **`$themes/scss/all` is on every search path** but does not exist; `scss/` holds only
   `winter/`.
8. **`translateResourceName()` is a no-op** in the only shipped `ITheme`. Kept as an
   extension point; drop it if the rework decides `ITheme` should not carry one.
