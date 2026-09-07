# DomUI theming

How theming works, after the removal of the two obsolete theme engines and the Rhino
template machinery, and the reduction to one theme with per-session variants. The last
sections record what was removed and what is still open.

All paths are relative to `domui/to.etc.domui/src/main/java/to/etc/domui/` unless said
otherwise; resources live under `domui/to.etc.domui/src/main/resources/resources/`.

[TOC]

## 1. Summary

An application has **one** theme, fixed at initialization time:

```java
setThemeFactory(SassThemeFactory.INSTANCE);      // DomApplication's own default
```

There is no registry and no theme name: `DomApplication` holds a single
`IThemeFactory` field. The one thing that varies is the **theme variant**, which is
per user session and is what a dark/light switch is built from.

A theme is a **search path of directories** holding one `style.scss` plus the images that
stylesheet and the components refer to; the variant, when it is not `default`, puts one
more directory in front of that path. Serving it is two things: compiling the SCSS to CSS
on demand, and resolving `THEME/xxx` image references against the search path.

## 2. The two URL forms

There are two distinct spellings, and confusing them is the single most common mistake in
this code.

**`THEME/xxx` (no dollar)** — what application and component code writes. It is a
*logical* reference meaning "a resource from whatever theme is current". It appears in
Java source and in node properties (`Img.setSrc`, `setBackgroundImage`, `Icon.of(...)`),
and survives untranslated in the server-side DOM tree.

**`$THEME/variantName/xxx` (with dollar)** — the *resolved* form. Its first segment is the
theme variant the reference was resolved in, and it is both a DomUI resource RURL and a
browser-visible URL.

The conversion happens at render time, in `ThemeManager.getThemedResourceRURL()`
(`themes/ThemeManager.java:203-216`):

```java
if(path.startsWith("THEME/"))       path = path.substring(6);
else if(path.startsWith("ICON/"))   throw new IllegalStateException("Bad ROOT: ICON/...");
else                                return path;              // not theme-relative
String newicon = theme.translateResourceName(path);           // icon-name remapping hook
return ThemeResourceFactory.PREFIX + theme.getVariantName() + "/" + newicon;
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
prefixes the webapp URL, giving e.g. `/demo/$THEME/default/btnCancel.png`.

### There is no theme name

The URL's first segment used to be a four- or five-part theme name whose leading word
picked a factory out of a registry (`scss-winter-default-default`). That is gone: the
factory is a field on `DomApplication`, and the segment is simply the variant name.
`$THEME/default/btnCancel.png` and `$THEME/dark/btnCancel.png` are the same file in two
variants.

Which style directory the factory reads is now the factory's own business —
`SassThemeFactory` takes it as a constructor argument, defaulting to `winter` through
`SassThemeFactory.INSTANCE`.

## 3. The interfaces

### `IThemeFactory` (`themes/IThemeFactory.java`)

```java
ITheme getTheme(DomApplication da, IThemeVariant variant);  // must NOT cache; caller caches
default IThemeVariant getDefaultVariant();                  // DefaultThemeVariant.INSTANCE
```

One instance is held by `DomApplication` and asked for one `ITheme` per variant.
`SassThemeFactory` is a normal class taking the style directory name;
`SassThemeFactory.INSTANCE` is the `winter` one DomUI ships.

### `ITheme` (`themes/ITheme.java`)

```java
String               getVariantName();                     // first URL segment: the variant
ResourceDependencies getDependencies();                    // for dev-mode reload
IResourceRef         getThemeResource(String name, IResourceDependencyList rdl);
String               translateResourceName(String name);   // icon name remapping hook
String               getStyleSheetName();                  // RURL of the main stylesheet
```

`translateResourceName` is a no-op in `SassTheme` (`SassTheme.java:51-53`). It is kept as
the extension point it is — an `ITheme` of your own can still substitute one icon file for
another — but nothing in the framework uses it any more.

### `IThemeVariant` (`themes/IThemeVariant.java`)

A name, and nothing more. `IThemeVariant.of("dark")` makes one (`DefaultThemeVariant.INSTANCE`
is "default"); the name must survive in a URL path segment, which `of()` checks.

An application declares its variants as constants and switches with
`IRequestContext.setThemeVariant()`, which stores the name in the **session**, so the
choice holds for every following request. `DomApplication.calculateUserThemeVariant()`
is the override point for deciding it per user instead.

## 4. `ThemeManager` — instantiation and caching

`themes/ThemeManager.java` is a final class owned by `DomApplication`
(`m_themeManager`, exposed via `internalGetThemeManager()`).

`getTheme(variant, rdl)` is the workhorse; the map is keyed on the variant name, so it
holds one entry per variant in use:

1. look the variant name up in `m_themeMap`;
2. a hit is returned only if its `IIsModified` says nothing changed — in production the
   dependency object is `null`, so a hit is always returned;
3. otherwise the application's one `IThemeFactory` builds a fresh `ITheme` for the variant;
4. **in development mode only**, the theme's own `ResourceDependencies` are wrapped in a
   `ThemeModifiableResource` with a 3-second throttle (`themes/ThemeModifiableResource.java`)
   so a theme edit is picked up without re-stat-ing every file on every request.

`getTheme(String variantName, rdl)` is the same thing for a name taken out of a URL.

## 5. Resource resolution: `$THEME/...` to bytes

`ThemeResourceFactory` (`themes/ThemeResourceFactory.java`) is an `IResourceFactory`
registered at `server/DomApplication.java:616`. Resource factories are scored by
`accept()`; this one returns 30 for anything starting with `$THEME/`, beating
`SimpleResourceFactory` (10 for any `$…`) and `ClassRefResourceFactory` (10 for `$RES/`).

```
$THEME/dark/btnCancel.png
       └──┘ └ file name ┘   (split on the FIRST slash; [0] is the variant)
```

`getResource()` then asks `DomApplication.getTheme(variantName, rdl)` for the `ITheme` and
delegates to `theme.getThemeResource(fileName, rdl)`, throwing `ThingyNotFoundException`
when the result does not exist.

`SassTheme.getThemeResource` is a **search path walk** — try `<dir>/<name>` for each
directory in order, return the first that exists. `SassThemeFactory.getTheme()` builds
the path:

```
$themes/scss/<style>/<variant>     (only when the variant is not "default")
$themes/scss/<style>
$themes/scss/all
```

This is the whole of the variant mechanism: a variant directory is consulted first, so it
overrides the files it contains and inherits every file it does not. A dark theme is a
`dark/` directory holding its own `_color.scss` plus whatever images must differ;
`style.scss` keeps its plain `@import 'color'`.

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

### 6.1 An icon: `$THEME/default/btnCancel.png`

Matcher 1 misses, matcher 2 hits. `InternalResourcePart.decodeKey` refuses extension-less
paths (403) and `.class` (404), then `generate()` calls `da.getResource(rurl, …)` — which
lands in `ThemeResourceFactory` as described above, resolving to
`resources/themes/scss/winter/btnCancel.png` in the DomUI jar (a `dark` request would
have looked in `winter/dark/` first). MIME from the extension,
`Expires` set from `getDefaultExpiryTime()` in production only.

### 6.2 The stylesheet: `$THEME/default/style.scss`

`HtmlFullRenderer.renderThemeCSS()` (`dom/HtmlFullRenderer.java:478-491`) emits

```html
<link rel="stylesheet" type="text/css" href="<ctx>/$THEME/<variant>/style.scss?$hash=<hex>">
```

from `ITheme.getStyleSheetName()`. `SassTheme.getStyleSheetName()` computes that hash by **compiling the sheet right
there**, through `PartService.getData()`, and hashing the result. That warms the part
cache, so the browser's follow-up request is a cache hit (`$hash` is stripped from the key
by `SassPartFactory.decodeKey`, which drops every `$`-prefixed parameter). It reaches into
`UIContext.getRequestContext()` for a browser version it then discards, and is annotated
`FIXME Fugly!!` in the source (open issue 3).

`SassPartFactory.decodeKey` takes the variant for the cache key **from the URL**, not from
the requesting session: the URL is what decides which sheet this is, and two sessions in
different variants asking for the same sheet must share one cache entry.

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
`<style>` block instead of linking it, and renders the factory's default variant rather
than the session's — so a per-session variant is ignored there. `renderLoadCSS` (line 465-501) similarly inlines
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

On top of those, `$themeVariant` is always defined, holding the name of the variant the
sheet is being compiled for. That is the alternative to a directory per variant: one file
can `@if $themeVariant == "dark"` instead.

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

## 9. Where the variant comes from

```
RequestContextImpl.getThemeVariant()
  ├─ field m_themeVariant, if already computed for this request
  ├─ session attribute "ctx$themevariant", set by setThemeVariant()
  └─ DomApplication.calculateUserThemeVariant(ctx)  -> factory's default   (override point)
```

`setThemeVariant()` writes the session attribute, so a variant chosen on one page holds
for every request after it — which is what makes a dark/light toggle a one-liner:

```java
UIContext.getRequestContext().setThemeVariant(DARK);
```

It also clears the request's cached `ITheme`, so the switch takes effect on the page that
performs it. `UrlPage.setThemeVariant()` is the same call from inside a page.

The variant name is carried in `IPageParameters`
(`state/IBasicParameterContainer.java`, `state/MapParameterContainer.java`) and
participates in `equals`/`hashCode`, which is what keeps compiled parts of different
variants apart in the part cache.

## 10. Live path, condensed

```
page render
  HtmlFullRenderer.renderThemeCSS()
    RequestContextImpl.getCurrentTheme()          session variant / calculateUserThemeVariant -> "dark"
      ThemeManager.getTheme(variant, null)        cache by variant name; the app's one IThemeFactory
        SassThemeFactory.getTheme()               search path: [.../winter/dark, .../winter, .../all]
    SassTheme.getStyleSheetName()
      PartService.getData($THEME/dark/style.scss)      -> compiles + caches, returns hash
    <link href=".../$THEME/dark/style.scss?$hash=...">

browser GET /$THEME/dark/style.scss?$hash=...
  PartRequestHandler -> PartService.render -> SassPartFactory (cache hit; key's variant from the URL)
    JSassCompiler + JSassResolver
      imports resolve via DomApplication.getResource -> ThemeResourceFactory
        -> SassTheme.getThemeResource -> search path -> winter/dark/_color.scss, else winter/_color.scss
      "parameters" import -> $themeVariant + URL params + setThemeProperty + IThemeVariablesCalculator

browser GET /$THEME/dark/btnCancel.png
  PartService.render -> InternalResourcePart ("$" prefix)
    DomApplication.getResource -> ThemeResourceFactory -> SassTheme.getThemeResource
      -> winter/dark/btnCancel.png if present, else /resources/themes/scss/winter/btnCancel.png
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

## 12. One theme, and real variants

The second change: an application no longer has a *set* of themes selected by a name in
the URL, but exactly one, with variants.

**Gone**

- the `THEME_FACTORIES` registry, `DomApplication.register(IThemeFactory)` and
  `getFactoryFromThemeName()` — the leading word of a theme name no longer picks a factory
- the theme name itself: `setDefaultThemeName()`, `getDefaultThemeName()`, the
  `m_defaultTheme` field, and the four/five-part `factory-style-icon-color-variant` grammar
- `IThemeFactory.getFactoryName()`, `getDefaultThemeName()` and `appendThemeVariant()`
- `IRequestContext.getThemeName()` / `setThemeName()`, and
  `DomApplication.calculateUserTheme()`
- `DomApplication.getDefaultThemeInstance()`
- the per-theme icon-set and colour-set search path entries; a variant directory replaces
  both

**New or changed**

- `DomApplication.setThemeFactory(IThemeFactory)` / `getThemeFactory()` — one field, set
  during initialization. `SassThemeFactory` is a normal class taking the style directory
  name; `SassThemeFactory.INSTANCE` is the `winter` one.
- `IThemeFactory.getTheme(DomApplication, IThemeVariant)` and `getDefaultVariant()`.
- `IThemeVariant.of(String)` builds a variant; `ITheme.getThemeName()` became
  `getVariantName()`.
- `IRequestContext.setThemeVariant()` stores the name in the **session**, so the choice
  holds across requests, and clears the request's cached `ITheme` so it takes effect
  immediately. `DomApplication.calculateUserThemeVariant()` is the per-user override point.
- The `themeName` carried by `IPageParameters` became `themeVariantName`, and
  `SassPartFactory.decodeKey` now takes it **from the URL** rather than from the requesting
  session, so two sessions in different variants share one cache entry per sheet.
- The stylesheet gets `$themeVariant` as an scss variable, so a single file can branch on
  the variant instead of needing a directory of its own.

Verified against a locally run demo, with a `dark` variant dropped into the demo webapp as
`themes/scss/winter/dark/_color.scss` (removed again afterwards):

| Request | Result |
| --- | --- |
| `$THEME/default/style.scss` | 200, `$themeVariant` = `default`, framework colours |
| `$THEME/dark/style.scss` | 200, `$themeVariant` = `dark`, the overridden colour present |
| `$THEME/default/btnCancel.png`, `$THEME/dark/btnCancel.png` | both 200 — the variant inherits the image it does not replace |

## 13. Still open

1. **`UrlPage.getThemeVariant()` is still commented out** (`dom/html/UrlPage.java:90-92`),
   though the setter next to it now works. Uncomment it, or drop the pair in favour of
   `UIContext.getRequestContext()`.
2. **DomUI ships no variant of its own.** The mechanism works, but `winter` has no `dark/`
   directory, so an application that wants dark has to write the whole colour set. A
   framework-supplied dark variant is the obvious next step, and is a design job rather
   than a plumbing one.
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
