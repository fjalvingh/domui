# maxGraph as a DomUI component

Plan for wrapping the [maxGraph](https://github.com/maxGraph/maxGraph) TypeScript
diagramming library in a DomUI component with a Java-side "content model" for the
drawing, growing from "render a drawing" to "the Java model and the Javascript
instance stay in sync in both directions".

Nothing of this exists yet; this file is the plan and the record of what the
investigation found.

## 1. What is being built

A component, `MaxGraphPanel`, that renders a diagram described by a Java model
(`GraphModel`, holding `GraphNode` and `GraphEdge` cells), and - in later phases -
keeps that Java model and the browser-side maxGraph instance synchronised:

- changes made to the Java model during a server round trip are pushed to the
  browser as a change list, without re-rendering the drawing;
- changes made by the user in the browser (moving a node, drawing an edge,
  renaming, deleting) are sent to the server and applied to the Java model, where
  page code can react to them, and refuse them.

The Java model is the thing application code programs against. maxGraph's own API
is reachable from nowhere but the component's own Typescript file - see the
versioning risk in §9.

## 2. What maxGraph is, and what wrapping it costs

- `@maxgraph/core`, currently **0.24.0** (2026-07-08), Apache-2.0, the maintained
  successor of the archived mxGraph. Written in TypeScript, renders to SVG + HTML.
- Core API: `Graph` (the widget, takes a container element), `GraphDataModel`
  (`graph.getDataModel()`), `Cell` (vertex or edge, has an `id`, a `value`, a
  `Geometry` and a `CellStyle`), `graph.insertVertex()/insertEdge()`,
  `graph.batchUpdate(fn)` for transactions.
- Change notification: the model fires `InternalEvent.CHANGE` at the end of an
  undoable edit, carrying an `edit` whose `changes` array holds typed change
  objects - `ChildChange` (add/remove), `GeometryChange`, `StyleChange`,
  `ValueChange`, `TerminalChange`, `CollapseChange`, `VisibleChange`. `EXECUTE`
  fires per atomic change. **This is the hook the browser -> server direction is
  built on**, and it is rich enough that we never have to diff whole models.
- **It ships no UMD or IIFE bundle**: the UMD build was removed from the npm
  package in 0.5.0, and the README states that using it in a web page without a
  build tool is unsupported. The package is ESM + CJS plus type definitions, and
  additionally ships `css`, `images`, `i18n` and `xsd` directories. `Client.setBasePath()`
  and `Client.setImageBasePath()` tell it where the images live.
  **So we must bundle it ourselves** (§6). This is the single largest piece of new
  infrastructure in this plan.
- It moves fast and breaks its API on nearly every minor release (0.20 removed all
  enums, 0.21 moved `fit` into a plugin, 0.23 and 0.24 moved tooltip and image-bundle
  handling into plugins). Pin the version, and keep every maxGraph type inside the
  wrapper's own Typescript.

## 3. What DomUI already gives us

Everything the sync needs exists; nothing in the framework has to be extended for
phases 1-3. Verified against the source:

| Need | Mechanism |
| --- | --- |
| Load the library on pages that use it | `HeaderContributor.loadJavascript("$js/...")`, added by a static `initialize(NodeContainer)` on the component, as `PlotlyGraph` does |
| Create the widget when the node is rendered | `NodeBase.appendCreateJS()` - runs on create *and* on every full page refresh, and may not carry state |
| Restore browser state after a full page refresh | Nothing, when the create JS is state-free: a full render re-emits it. `NodeBase.renderJavascriptState(JavascriptStmt)` is for state that only the browser has (`CKEditor`, `AceEditor` do that) |
| Push a change list to an existing widget | `NodeBase.changedJavascriptState()` marks the node; `renderJavascriptDelta(JavascriptStmt)` then emits the Javascript into the delta response. `AceEditor` markers are the working example |
| Fetch the model as JSON from the browser | `WebUI.jsoncall(id, fields, cb)` -> `IComponentJsonProvider.provideJsonData()`; the response may be a `StringBufferDataFactory` filled by `JsonBuilder`, which is how `PlotlyGraph` ships its dataset |
| Send structured data to the server as a normal page action | `WebUI.sendJsonAction(id, action, json)` - posts `json` as a string field, dispatches to `NodeBase.componentHandleWebAction(ctx, action)`, and returns the normal page delta. Nothing used it before this component |
| Send structured data out of band (no UI state change, JSON reply) | action prefixed `#` -> `componentHandleWebDataRequest()` |
| Build/parse JSON | `to.etc.domui.util.javascript.JsonBuilder` (streaming, used by plotly), `to.etc.json.JSON` (bean mapping), and Jackson 2.21 is already a dependency |

Two framework rules shape the design:

- **DomUI must never own the DOM maxGraph draws into.** The component renders one
  empty `div`; maxGraph fills it. As long as the component has no server-side
  children and is not rebuilt, the delta renderer leaves that subtree alone. A
  `forceRebuild()` (of it or any ancestor) throws the widget away - which is why
  the whole drawing must be restorable from the Java model at any time.
- **Components are never kept in fields** (workspace rule). `GraphModel` is state
  and belongs in a page field; `MaxGraphPanel` is a local variable of
  `createContent()`.

## 4. Where the code goes

The module **`integrations/to.etc.domui.maxgraph`** exists, modelled on
`integrations/fontawesome6free`:

```
integrations/to.etc.domui.maxgraph/
  pom.xml                                  depends on to.etc.domui; runs no node
  README.md                                how to rebuild the bundle, and with what
  package.json  tsconfig.json  copy-assets.mjs      the bundle build (§6.3)
  .settings/org.eclipse.jdt.core.prefs     ecj's per-module settings, as every module has
  src/main/frontend/domui-maxgraph.ts      the wrapper, the only file that imports @maxgraph/core
  src/main/java/to/etc/domui/maxgraph/     the component and (from phase 1) the model
  src/main/resources/META-INF/web-fragment.xml
  src/main/resources/META-INF/resources/js/maxgraph/
        domui-maxgraph.js  domui-maxgraph-min.js  (+ source maps)
        domui-maxgraph.css                 the rules for the container DomUI renders
        css/common.css  images/*  LICENSE  maxGraph's own, copied as it ships them
```

Why a separate module and not `to.etc.domui/component/maxgraph`:

- the bundle is roughly a megabyte of third-party Javascript; it has no business in
  the core jar that every DomUI application loads;
- it is Apache-2.0 next to an LGPL core - cleaner kept apart;
- it needs a modern node toolchain, which the core module's frontend build cannot
  provide (§6);
- `fontawesome*` already establishes the pattern for "an integration module that
  ships web resources", including how they are found: `getAppFileOrResource()`
  resolves through servlet-3 web fragments (`META-INF/resources/...`) and through
  `/resources/...` on the classpath, so `$js/maxgraph/domui-maxgraph.js` resolves
  from the module's jar with no registration at all. Note that
  `VersionedJsResourceFactory` prefers a `-min` sibling outside development mode,
  so emit both files.

The demo module gains a dependency on it; `to.etc.domui` itself does not.

## 5. The Java content model

Package `to.etc.domui.maxgraph`, model in `to.etc.domui.maxgraph.model`.

```
GraphModel                  the drawing: cells by id, insertion order, version counter, listeners
 +- GraphCell (abstract)    id, label, style, parent, userObject (server-side only, never sent)
     +- GraphNode           geometry (x, y, w, h), collapsed, visible, children (grouping)
     +- GraphEdge           source, target, waypoints
GraphStyle                  typed subset of maxGraph's CellStyle + raw(name, value) escape hatch
GraphGeometry               x, y, width, height
IGraphModelListener         onModelChanged(List<GraphOp>)
GraphOp / GraphOpType       one change, the unit of the wire protocol (§7)
IGraphChangeHandler         page callback for browser-originated changes; may reject
GraphEdit                   one undoable step: the ops it made, and the ops that take them back
```

Decisions that matter:

- **Ids are server-assigned, stable, opaque strings** and are used verbatim as
  maxGraph `Cell` ids. They are the only correlation key between the two sides.
  `GraphModel` allocates them (`n1`, `e1`, ...) unless the caller supplies one.
- **`userObject` never crosses the wire.** Application code hangs its own entity on
  a cell; only `label` and the typed properties are serialised. This keeps the
  protocol small and stops entity graphs leaking into the browser.
- **`GraphStyle` is a typed subset** (`shape`, `fillColor`, `strokeColor`,
  `strokeWidth`, `rounded`, `fontSize`, `fontColor`, `edgeStyle`, `startArrow`,
  `endArrow`, `dashed`, ...) with a `raw()` escape hatch, exactly as
  `to.etc.domui.component.plotly.layout` wraps plotly's layout options. Typed
  where it pays, open where it does not.
- **The model carries a version counter**, bumped on every applied change. It is
  what makes conflict handling trivial (§7.3).
- The model is **usable standalone**: building one and rendering it to JSON has no
  dependency on a page, which makes it unit-testable without a browser.

## 6. The Javascript build: upgrade the existing pipeline first

The core module's Typescript pipeline cannot bundle maxGraph, and it should not be
left as it is either. What is there today:

- `to.etc.domui/pom.xml` runs `frontend-maven-plugin` 1.11.0, which downloads and
  pins **node v8.11.1 and npm 5.6.0** - a 2018 runtime, end-of-life since 2019 -
  and runs `npm install` + `tsc` on every build.
- `tsconfig.json` compiles with `"module": "system"` and `"outFile"` into one
  `domui-combined.js` from a hand-maintained, order-sensitive `files` list, because
  the code is one `WebUI` namespace spread over 16 files. There are no ES modules
  and no npm dependencies in the source at all.
- Installed: typescript 4.3.2, and `@types/jquery` **2.0.56** while the framework
  actually serves jQuery 3.7.1 - the type definitions describe a different jQuery
  than the one that runs.
- `domui-combined.js` (166KB, unminified) is gitignored and built; it is served as
  `$ts/domui-combined.js?v=2`, which goes through `SimpleResourceFactory` - so,
  unlike `$js/`, it has **no `-min` resolution**: every page loads the full
  unminified bundle.
- The per-file `.js` and `.js.map` outputs *are* committed (about 40 files) even
  though nothing serves them. They are stale IDE build products.

This splits into two upgrades of very different size.

### 6.1 The toolchain upgrade - DONE

Node 8 cannot run esbuild, or any other modern bundler, so this had to happen before
any maxGraph work; otherwise the build would carry two node installations. What was
done:

1. `frontend-maven-plugin` 1.11.0 -> 1.15.1, node **v8.11.1 -> v22.22.1**, and the
   separate `npmVersion` pin dropped so the npm bundled with node is used.
2. `typescript` 4.3 -> 5.9, `@types/jquery` ^2.0.56 -> ^3.5.32 (the jQuery that is
   actually served), `@types/jqueryui` -> ^1.12.24, all moved to `devDependencies`.
3. `esbuild` added; `npm run compile-typescript` is now `tsc` followed by `minify`.
   The core bundle went from 163KB to **74KB** minified, with a source map.
4. `VersionedJsResourceFactory` now accepts `$ts/` as well as `$js/`, so
   `$ts/domui-combined.js` resolves to `domui-combined-min.js` outside development
   mode and to the full bundle inside it - one URL, no change to `DomApplication`.
   Header contributors are registered in the constructor, before development mode is
   known, so the choice has to be made at resource-resolution time.
5. An explicit `"target": "es2017"` in `tsconfig.json`. There was none, so the output
   was downlevelled ES3/ES5 - which meant `class BodyTooLargeException extends Error`
   emitted the `__extends` shim whose constructor returns the `Error` it built, so
   **`x instanceof BodyTooLargeException` in `domui.fileupload.ts` was always false**.
   ES2017 emits native classes and the check now works.
6. Two errors the new types and compiler found, both fixed: `expr: any` in
   `domui.jquery.d.ts` conflicting with the real `JQueryStatic.expr`, and
   `maxSize === NaN` in `domui.fileupload.ts` - always false, so a garbage
   `fumaxsize` attribute silently disabled the upload size check. Now `isNaN()`.
7. The ~40 committed per-file `.js`/`.js.map` outputs and the duplicate
   `resources/ts/package.json` (which was being packaged into the jar) removed, and
   `.gitignore` extended to keep them out.

*Verified*: `mvn21 clean install -pl to.etc.domui -am` downloads node 22, runs npm
install, tsc and esbuild, and produces both bundles plus their maps in the jar; the
demo served from jetty returns the 75825-byte minified bundle with
`-Ddeveloper.properties=false` and the 162903-byte full bundle in development mode,
at the same URL; the demo unit and Selenium integration suites pass.

### 6.2 The source modernization - `WebUI` namespace to ES modules

This is the expensive one, and **maxGraph does not need it**. Measured:

- 16 files declare `namespace WebUI`, exporting **203 functions**, with **130
  internal `WebUI.x()` cross-file calls** and 198 `this.` uses - and a `this.` inside
  a namespace function silently means something else once the file becomes a module.
- The external contract is harder: **72 distinct `WebUI.*` functions are emitted as
  Javascript from 41 Java classes**, a good number of them as inline HTML attributes
  from `HtmlTagRenderer` (`onclick="return WebUI.clicked(this, 'id', event)"`,
  `onchange`, `onkeypress`, `onunload`). Those globals - `WebUI` and its alias
  `DomUI` - must survive the conversion exactly.

So it is doable but it is a project: a mechanical conversion plus a generated
`window.WebUI = {...}` facade, and it wants an IT test that exercises the 72
functions before anyone trusts it.

**Recommendation: do 6.1 now, and leave 6.2 out of the maxGraph work entirely.**
The one thing that would force 6.2 is a decision that new component Typescript in
the *core* module must be able to `import` npm packages - which the namespace +
`outFile` setup cannot do at all. maxGraph does not force it, because it lives in
its own module with its own bundle and its own global.

### 6.3 What the maxGraph module then does

With 6.1 in place:

- the module has its own `package.json` pinning `@maxgraph/core` exactly and using the
  same esbuild, and an `npm run bundle` script producing an **IIFE** bundle exposing
  one global, `window.DomUIMaxGraph`, plus a minified sibling and source maps. esbuild
  does not type-check, so `bundle` runs `tsc --noEmit` over the wrapper first;
- **the built bundle is committed to git.** The core's generated bundle is *not*
  committed, but it is regenerated by every Maven build from sources in the same
  module; a megabyte of third-party Javascript is a different thing - committing it
  keeps node off the critical path of a normal `mvn21 clean install` and makes every
  maxGraph upgrade an explicit, reviewable commit. (The alternative - a `node`
  profile that rebuilds it - is a reasonable second choice if you would rather have
  no generated artifacts in git at all.)
- `npm run bundle` also copies `@maxgraph/core`'s `css/common.css` and its `images/`
  into `META-INF/resources/js/maxgraph/`, and the create-JS calls
  `Client.setImageBasePath()` at that URL. Forgetting this is the classic
  mxGraph-family failure: handles and folding icons silently disappear;
- the node version used for a bundle is recorded in the module's README so a rebuild
  is reproducible.

## 7. The wire protocol

One vocabulary of operations serves the initial load, the server -> browser delta
and the browser -> server delta. This is the heart of the design; get it right
once and the three phases below are mechanical.

### 7.1 The model document (initial load and after a full refresh)

`GET`-equivalent via `WebUI.jsoncall` -> `provideJsonData()`, rendered with
`JsonBuilder`:

```json
{ "version": 12,
  "options": { "editable": true, "grid": true, "gridSize": 10, "rubberband": true },
  "cells": [
    { "id": "n1", "kind": "node", "parent": null, "label": "Start",
      "x": 20, "y": 20, "w": 120, "h": 40, "style": { "shape": "ellipse", "fillColor": "#ddeeff" } },
    { "id": "e1", "kind": "edge", "source": "n1", "target": "n2", "label": "yes",
      "style": { "edgeStyle": "orthogonalEdgeStyle" }, "points": [[80, 140]] }
  ] }
```

### 7.2 The operation list (both directions)

```json
{ "base": 12, "version": 13, "ops": [
  { "op": "addNode",  "id": "n7", "parent": null, "label": "x", "x": 0, "y": 0, "w": 80, "h": 40, "style": {} },
  { "op": "addEdge",  "id": "e3", "source": "n1", "target": "n7" },
  { "op": "remove",   "id": "n3" },
  { "op": "geometry", "id": "n1", "x": 40, "y": 60, "w": 120, "h": 40 },
  { "op": "style",    "id": "n1", "style": { "fillColor": "#f88" } },
  { "op": "label",    "id": "n1", "label": "Begin" },
  { "op": "terminal", "id": "e1", "source": "n2", "target": null },
  { "op": "points",   "id": "e1", "points": [[80, 140]] },
  { "op": "reload" }
] }
```

Four more travel from the browser only. None of them is a change: the first two ask for a
cell that does not exist yet, because ids are the server's, and the last two ask the model's
history to make one (phase 5).

```json
{ "op": "requestNode", "key": "task", "x": 120, "y": 40 }
{ "op": "requestEdge", "source": "n1", "target": "n2" }
{ "op": "requestUndo" }
{ "op": "requestRedo" }
```

Every op is **id-addressed and idempotent**, so applying the same list twice is
harmless and order within a list is the only thing that matters.

### 7.3 Conflicts

The browser stamps its op list with `base` - the model version it was at. If that
does not equal the server's current version, the server discards the list and
answers with a single `{"op":"reload"}`, which makes the browser re-fetch the model
document. Given DomUI serialises requests within a window session this is a rare
path; making it correct is one `if`, and it removes every merge question.

### 7.4 Echo suppression

Both sides set a flag while applying a received op list, so applying it does not
generate a change list back. On the browser this means applying inside
`graph.batchUpdate()` with the model listener suppressed.

## 8. Phases

### Phase -1 - upgrade the existing Typescript build (§6.1) - DONE

Node 22, typescript 5.9, jQuery 3 types, esbuild in the build, the core bundle
minified and served as such outside development mode, two latent Javascript bugs
fixed, stale committed build artifacts gone.

### Phase 0 - toolchain spike - DONE

The module above, a `MaxGraphPanel` that renders one empty div and a create-JS call,
and a wrapper whose `create()` draws a hard-coded two-node graph. `BasicGraphPage`
in the demo (`pages/components/graph/`, linked from `ComponentListPage` under
"Diagrams") shows it.

**The bundle measured**: 971KB, **401KB minified**, 111KB over the wire once the
container gzips it. maxGraph's stylesheet and images are 4KB and 6KB next to that.
Neither committed bundle carries a source map: that would be four megabytes of git
history usable only by someone editing the wrapper, who can rebuild with
`--sourcemap` for as long as they need one.

*Verified*: the page draws the two vertices and the edge, with the styled ellipse, on
first load and again after a full page refresh, with no console errors, **in both
development mode and production mode** - production serving the 401KB minified bundle
from the same `$js/maxgraph/domui-maxgraph.js` reference. The four assets
(`$js/maxgraph/domui-maxgraph.js`, `$js/maxgraph/css/common.css`,
`$js/maxgraph/domui-maxgraph.css`, `js/maxgraph/images/expanded.gif`) all serve.
`mvn21 clean install` builds the whole reactor green **without node being involved in
this module at all** - the bundle is committed, and only `to.etc.domui`'s own
Typescript still runs node, as it did before. The demo's 9 unit tests and 55 Selenium
ITs are green with the module and its demo page added.

### Phase 1 - render a drawing - DONE

The Java content model of §5, the document of §7.1, and a panel that fetches it.

- `to.etc.domui.maxgraph.model` holds `GraphModel`, `GraphCell` with `GraphNode` and
  `GraphEdge`, `GraphGeometry`, `GraphPoint`, `GraphStyle` and the `GraphShape` /
  `GraphEdgeStyle` enums. It knows nothing of maxGraph and nothing of DomUI, so a
  drawing can be built and tested without either.
- `MaxGraphPanel implements IComponentJsonProvider`. `createContent()` emits only
  `DomUIMaxGraph.create('<id>', {imageBase})`; `provideJsonData()` renders the model
  with `GraphJsonRenderer` into a `JsonBuilder`.
- The wrapper's `create()` builds the `Graph`, switches every editing affordance off,
  asks for the model with `WebUI.jsoncall`, and builds all cells in one
  `batchUpdate()`. Read-only interaction: selection, panning, and zoom on ctrl+wheel
  (a plain wheel keeps scrolling the page).
- `BasicGraphPage` in the demo builds a five-node flow chart in Java, and
  `ITMaxGraphPanel` drives it.

**No `renderJavascriptState()`, contrary to what this plan said.** A full render
re-emits a node's `appendCreateJS` buffer - which is why the phase 0 drawing already
survived a refresh - so adding `renderJavascriptState` would create the widget twice.
The create call carries no state, and the model comes from the follow-up json call, so
the refresh path needs nothing of its own.

Two smaller deviations: cell ids are allocated by the model and cannot be supplied by
the caller (an application correlates through `GraphCell.setUserObject()`, which stays
on the server), and there are no tooltips, because the model has nothing to put in
them. `GraphStyle` is handed out mutable, so a style edited after the drawing has been
sent does not reach the browser by itself - that is what phase 2 is for.

*Verified*: the demo page draws the model - shapes, fill and stroke colours, the
labelled and dashed edges, the orthogonal routing - with no console errors, and comes
back identically after both a browser reload and DomUI's own full re-render.
`ITMaxGraphPanel` asserts the seven labels of the model are in the SVG, and again after
a refresh; it and the rest of the demo suite are green.

### Phase 2 - server -> browser sync - DONE

Changing the model changes the drawing, without redrawing it. A page moves a node by
moving it in the model and does nothing else - no `forceRebuild()`, no redraw.

- Every mutation in `to.etc.domui.maxgraph.model` now arrives at
  `GraphModel.changed(GraphOp)`, which bumps the version and tells whatever listens.
  `MaxGraphPanel` listens while it is on a page, collects the operations, and calls
  `changedJavascriptState()`; `renderJavascriptDelta()` then emits
  `DomUIMaxGraph.apply('<id>', {base, version, ops})`, rendered by
  `GraphJsonRenderer.renderOps()` in the vocabulary of §7.2.
- **An operation names a cell, it does not copy it.** What is sent is read from the cell
  when the change list is rendered, so a cell changed three times in a round trip is sent
  once, in the state it ended up in - and the panel's buffer needs no coalescing beyond
  "an operation already on the list is not added again" (`GraphOp.equals` is type plus
  cell identity).
- **`GraphStyle` and `GraphGeometry` report to their cell**, which closes phase 1's gap:
  `cell.style().fillColor(...)` and `node.getGeometry().setPosition(...)` are changes to
  the model like any other. A style or geometry made on its own, not part of a drawing,
  reports to nobody.
- The Typescript `apply()` walks the operations inside one `batchUpdate()`. Two things
  the plan did not foresee:
  - **A change list can arrive before the drawing does.** The create JS and the change
    list are in the same response, but the model document is fetched asynchronously after
    it. So an instance carries a `loaded` flag and a queue: lists that arrive early wait,
    and when the document lands - newer than they are - they find themselves already
    applied and do nothing.
  - **A list whose `base` is not the version the drawing is at** is either one we already
    have (`delta.version <= instance.version`: ignore it) or a sign that we missed one,
    and then the drawing is loaded again from the model document. That is §7.3's reload
    path, in the server -> browser direction, working before phase 3 needs it.
- `graph.removeCells()` **obeys `cellsDeletable`**, which the read-only setup turns off,
  so removals went through silently doing nothing. Applying a change goes through
  `graph.getDataModel()` throughout: it is the server that decided, and the browser's
  editing policy has no say over it.
- A full render still goes through phase 1's path - `createContent()` drops whatever was
  pending, because the browser is about to ask for the whole model again.

No echo suppression, contrary to what the plan said: nothing on the browser side listens
to model changes yet, so applying a change list cannot echo. That flag belongs to phase 3
and is written when there is something to suppress.

`ChangingGraphPage` in the demo (linked from `ComponentListPage` next to
`BasicGraphPage`) has five buttons - add a satellite, move the hub, recolour it, rename
it, remove a satellite - each of which changes only the model.

*Verified*: driven in a browser, every button changes the drawing, and the console shows
nothing but the jQuery-migrate warnings the demo always has. Held onto in Javascript
across the clicks: the `<svg>` is the same element afterwards, and so is the `<text>` of a
node that was not touched, while the ellipse of the node that was recoloured and moved is
a new one - which is the phase's claim, measured rather than looked at. Removing a
satellite takes its edge with it. `BasicGraphPage` still draws phase 1's flow chart
unchanged. `ITMaxGraphPanel` grew four tests around the new page - changed in place rather
than rebuilt, a removed cell disappearing while its neighbour stays, a relabelled cell, a
restyled cell - and `mvn21 verify -pl to.etc.domui.demo` is green: 9 unit tests, 61
Selenium ITs, no failures.

### Phase 3 - browser -> server sync - DONE

The user may now change the drawing, and the server has the last word on it. A drawing
is read-only until `setEditable(true)`; an editable one can be moved, resized and deleted
in, and every such change goes to the server before it counts.

- The Typescript listens on the data model's `InternalEvent.CHANGE`, translates the
  `edit.changes` of the transaction into our operations, coalesces them per transaction,
  and calls `WebUI.sendJsonAction(id, 'GRAPHCHANGE', {base, ops})` - the framework call
  the plan found unused. `MaxGraphPanel.webActionGRAPHCHANGE()` picks it up through
  `SimpleWebActionFactory`, and `GraphChangeParser` reads it in the vocabulary
  `GraphJsonRenderer` writes - Jackson, because `to.etc.json` has no mapping for a
  `double` and a geometry is four of them.
- **Rejection is a veto, not an undo.** The plan said to apply a change and let the
  handler change it back, but a delete cannot be changed back: the model removes what
  cannot exist without the cell, and nothing rebuilds that. So `IGraphChangeHandler` is
  asked *before* anything is made to the model, and returns false to refuse. Nothing then
  has to be undone - the correction is `GraphModel.resend()`, recording an operation
  without changing anything so that what the model holds is sent again. Because the
  operations are id-addressed and read at render time, that is all it takes: phase 2's
  delta carries it in the same response.
- `GraphChange` is the mirror of `GraphOp`: an operation names a cell and is read from it,
  a change carries what is proposed for it. That is what lets a handler decide.
- **A node deleted in the browser takes its edges with it**, so those edges are one
  gesture with the node: they follow its verdict, and the handler is not asked about them.
  Which is why the node deletions are put to the handler first.
- **The browser is always told the new version**, even when there is nothing to correct.
  Applying the user's changes moves the model on without anything being sent, and the next
  change list would then be about a version that no longer exists.
- **A change list made against another version needs no `reload` operation.** Dropping it
  leaves the two versions apart, and phase 2's guard in the browser already turns that into
  a fetch of the whole model. §7.3 costs one `if` and no protocol.
- Editing a label in place, bending an edge and drawing a new one stay off: a cell the
  browser invents has no id the server knows it by, and ids are the server's (§5). That is
  what phase 4's palette is for. The server side parses the whole vocabulary anyway -
  label, terminal, style, points - so the browser is the only side phase 4 has to grow.

Three things about maxGraph and the browser cost real time here, and are worth knowing:

- `graph.removeCells()` **obeys `cellsDeletable`**, which is off in a read-only drawing, so
  applying a removal has to go through `graph.getDataModel()`. The server decided; the
  browser's editing policy has no say over it.
- a `KeyHandler` only sees a key press that happens **inside the graph's container**, and
  clicking a shape does not put the focus there - the container needs a `tabindex` and has
  to be focused by hand;
- and it must be focused on **`pointerdown`**: maxGraph consumes those, and a consumed
  pointer event means the compatibility `mousedown` never fires at all. A `mousedown`
  listener on the container is simply never called.

One protocol bug worth recording: the browser has to **forget a cell the user removed**.
It was still in the id map, so when the server put a refused deletion back the add was
taken for a repeat of a cell already there and ignored - the node stayed gone, in a
drawing that otherwise looked right.

`EditableGraphPage` in the demo has a hub that refuses to be deleted and three nodes to
drag, and writes what the model was told under the drawing.

*Verified*: driven in a browser - dragging a node reports where the model put it,
deleting a node deletes it and its edge, and deleting the hub brings it and all three of
its edges straight back, with nothing in the console but the demo's usual jQuery-migrate
warnings. `ITMaxGraphPanel` drives all three with Selenium (a real drag, and click +
Delete), and `mvn21 verify -pl to.etc.domui.demo` is green: 9 unit tests, 64 Selenium ITs,
no failures.

### Phase 4 - the user builds the drawing - DONE

Everything phase 3 left off that needs a cell the server has not named yet: a palette to
drag nodes from, connections drawn between nodes, in-place label editing, and edge
bending. What is still open is in phase 5 below.

- **The browser never makes a cell.** Ids are the server's (§5), so instead of inventing
  one the browser says what the user did: two new operations, `requestNode` (a palette
  item was dropped here) and `requestEdge` (a connection was drawn from this node to that
  one). They are the only two that travel one way only. The page's new
  `IGraphCreateHandler` answers with the cell it wants, and that arrives in the browser
  through phase 2's delta like any other change - with the id the model gave it. **One
  round trip, no temporary ids, and nothing in the protocol that has to be renamed
  afterwards.**
- **Returning null is how a drawing says what may not be drawn in it.** There is nothing
  to take back, because nothing was made: the constraint costs no protocol at all, which
  is why `isValidSource/Target` is not needed on the browser side.
- **The panel renders one empty div still.** The wrapper makes two elements inside it -
  the palette strip and the canvas maxGraph owns - so DomUI still has no server-side
  children to re-render and the rule of §3 holds.
- A `GraphPaletteItem` is what the user drags: a key, a label, a size and a style. Only
  its look goes to the browser; `item.create(model, x, y)` is the one-liner a handler
  usually answers with.
- Connections are only offered where the panel has a create handler, because the dot that
  starts one sits in the middle of a node - exactly where dragging the node would
  otherwise move it. A drawing that cannot gain edges should not lose that.

Four things about maxGraph, each of which cost time:

- `ConnectionHandler.connect()` is the one method that inserts the edge, and replacing it
  on the instance leaves the preview, the highlighting and the reset exactly as they were.
  There is no cleaner hook, and no need for one.
- **Without a `connectImage` a connection starts from the middle of a node with nothing to
  say so**, and dragging a node to move it connects it instead
  (`isImmediateConnectSource` is `!isCellMovable`). maxGraph ships no image for it, so the
  wrapper draws the dot itself as an inline SVG data URI.
- **A virtual bend is off by default** (`EdgeHandlerConfig.virtualBendsEnabled`), which
  leaves an edge that has no waypoints yet impossible to bend: the handles maxGraph shows
  are for waypoints that already exist. It is a setting of the library rather than of a
  graph, and every graph in this bundle is one of ours.
- `gestureUtils.makeDraggable(element, graph, dropHandler, preview)` gives the palette its
  drag, and hands the drop point in the drawing's own coordinates - which is what
  `requestNode` carries.

`GraphEditorPage` in the demo has a three-item palette, connects what is drawn, and
refuses a connection out of a Done - deciding that on the node's `userObject`, which is the
server's own data and never goes to the browser. It writes what the model was told under
the drawing.

*Verified*: driven in a browser - a dropped palette item becomes the node the page makes, a
connection drawn between two shapes becomes the edge the page makes, one the page refuses
is not drawn at all, a double click renames a shape, and the middle handle of an edge bends
it. Each of those reports what the model was told, and the console shows nothing but the
demo's usual jQuery-migrate warnings. `ITMaxGraphPanel` drives the palette drop, the
connection, the refusal, the rename and the bend with real Selenium gestures; `mvn21 verify
-pl to.etc.domui.demo` is green: 9 unit tests, 69 Selenium ITs, no failures.

One thing that was tried and turned out not to be reachable: a node cannot be connected to
itself - maxGraph does not offer the connection at all - so a demo rule about it would have
been a rule nobody can hit.

### Phase 5 - what an editor still wants

The three items are independent of each other. The first is done; the other two are not
started.

#### Undo/redo - the server's - DONE

**Undo/redo is the server's** (decision 4, reversed). The browser's `UndoManager` is never
installed; `GraphModel` keeps the history, and an undo is an ordinary change list on its way
to the browser. Two things settle it: the model is the only side that still has the *object*
of a deleted cell - its `userObject` included - and maxGraph's editing API changes with every
minor release, so a history built on it is a history that breaks on upgrade (§9). Keeping it
in Java leaves the wrapper with two keystrokes to forward and nothing to remember.

- **An undo step is an op list, and it travels the path phase 2 already built.** Every op
  in §7.2 has an inverse in the same vocabulary: `addNode`/`addEdge` invert to `remove`,
  `remove` inverts to the add of *that same `GraphCell` object* under its old id, and
  `geometry`, `style`, `label`, `terminal` and `points` invert to themselves carrying what
  the cell held before. So undo needs no operation the protocol does not have, and the
  browser needs no code beyond what applies a delta today.
- **What is recorded is the change that reverses it**, made where the original was made -
  the mutators of the model, the only places that see both the old value and the new one.
  A `GraphOp` names a cell and does not copy it (§7.2), so an inverse cannot be an op with
  old values in it; it is the mutation that puts the old value back, and the op it produces
  on its way out is the ordinary one.
- **A step is a list, because one gesture is several changes.** Removing a node removes its
  children and the edges that end on it, and a palette drop can be answered with a node
  *and* an edge. One `GraphEdit` holds the changes of one boundary, and undoing it runs
  their inverses **in reverse order** - which puts a node back before the things that
  cannot exist without it, with no ordering rule of its own.
- **The boundaries are the round trip and the cascade.** Everything one `GraphChangeSet`
  causes, the cells its handler creates included, is one step; so is one
  `GraphModel.remove()`, however many cells it takes with it. Page code groups its own with
  `model.edit(() -> ...)`, and anything outside a boundary is a step by itself.
- **History is off until it is asked for**, so building the drawing in the page's
  constructor does not fill the stack; `clearHistory()` is what loading another drawing
  calls.
- **Triggered from either side.** `model.canUndo()/undo()/redo()` is all a server-side
  toolbar button needs. In the drawing, ctrl-Z and ctrl-Y (and ctrl-shift-Z) send
  `requestUndo`/`requestRedo` (§7.2) - one-way ops in the same family as `requestNode`: the
  browser says what the user did, the server decides whether anything happens. maxGraph's
  own `UndoManager` is installed nowhere, and `KeyHandler` ignores keystrokes while a label
  is being edited, so ctrl-Z in the editor still undoes typing.
- **The version counter moves forward, never back.** An undo is a change like any other, so
  §7.3's conflict rule and §7.4's echo suppression are untouched.
- **A refused change is not history.** `IGraphChangeHandler` rejects before the model is
  touched, and `resend()` changes nothing, so neither can be undone.
- **The stack is bounded** (`setUndoLimit`, default 50) and a new change clears the redo
  side. It pins the `userObject` of every deleted cell until that step falls off the end,
  which is why there is a limit at all.

What it cost: `GraphEdit` and the history in `GraphModel`, a recorded inverse in each
mutator (`GraphCell`, `GraphGeometry`, `GraphStyle`, `GraphEdge`, and the model's own
register/remove), `GraphEdge.setWaypoints()` so that a bend is one change rather than one
per point, two enum values and their parsing, and eleven lines of Typescript for the
keystrokes. Nothing in the delta protocol, and nothing in the browser's side of it.

*Verified*: `TestGraphUndo` in the module drives the model without a browser - the ten
cases include a removed node coming back as the same object with its `userObject`, its
children and its edges, in an order in which nothing arrives before what it needs. In a
browser, `GraphEditorPage` has Undo and Redo buttons and takes ctrl-Z; `ITMaxGraphPanel`
drives all three paths, the deleted-node-and-its-edge one being the point of the exercise.
`mvn21 verify -pl to.etc.domui.demo` is green: 9 unit tests, 72 Selenium ITs, no failures,
and 10 unit tests in the maxgraph module itself, which had none before.

#### The other two

- **Automatic layout** (`HierarchicalLayout`). Cheap: run it in the browser and the
  geometry changes it makes travel to the model through phase 3 by themselves.
- **SVG/PNG export**, which wants a `#`-action to get the drawing back out of band.

### Phase 6 - demo and documentation

- Demo pages under `to.etc.domui.demo/.../pages/components/graph/`, linked from
  `ComponentListPage`, following the demo conventions (`HTag(1)` title, content in a
  `ContentPanel`, `form4` for any form, no component in a field).
- A documentation section `domui.github.io/site/content/components/125-diagrams/`
  (between `120-charts` and `130-async`), written show-first, with `!demo()` tags
  pointing at those demo pages - which therefore have to be deployed to
  https://demo.domui.org/ before the docs build is meaningful.
- The component groups list in the docs' components index gains this group.

## 9. Risks and how they are handled

| Risk | Handling |
| --- | --- |
| maxGraph breaks its API on every minor release | Pin the version. All maxGraph types stay inside `domui-maxgraph.ts`; the Java API and the wire protocol are ours, so an upgrade is one file and one bundle rebuild |
| Bundle size (measured: 401KB minified, 111KB gzipped) | Loaded only by pages that call `MaxGraphPanel.initialize()`; the minified variant is served outside development mode by the existing `$js` resolution |
| `forceRebuild()` destroys the widget | The Java model is authoritative and the drawing is always rebuildable from it; the create JS carries no state, so a full render re-emits it and the browser asks for the model again |
| Large models over the page POST | The op protocol keeps steady-state traffic tiny; the model document is fetched out of band. If an initial model ever gets big, `#`-actions (`componentHandleWebDataRequest`) are the escape hatch. Container POST size limits are worth a note in the docs |
| Server-side memory | The model lives in the page/conversation like any other page state; document that a huge drawing is a per-conversation cost. The undo stack adds a bounded multiple of it and pins the `userObject` of every deleted cell until that step falls off the end - hence the depth limit |
| Node toolchain drift | §6.1 brings the build to a current node before anything else; the maxGraph bundle is committed and reproducible from a recorded node version, so a normal Maven build needs no node |

## 10. Decisions to confirm before starting

1. **Separate module** `integrations/to.etc.domui.maxgraph` rather than a package in
   `to.etc.domui` (§4). Recommended, but it is the one structural choice that is
   awkward to reverse later.
2. **Component name** `MaxGraphPanel`, model names neutral (`GraphModel`,
   `GraphNode`, `GraphEdge`) so the Java API does not advertise the library.
3. **Committed bundle** rather than a Maven-driven node build (§6).
4. **Undo/redo is the server's** (2026-09-10), reversing the original "the browser's
   `UndoManager` does it, the server follows". Phase 4 found the hole: undoing a deletion
   needs a cell the model no longer has an object for, let alone an id. The library
   settles the rest - maxGraph's editing API moves on every minor release, and history
   built on it is history to be rewritten at every upgrade. Built as phase 5's first item;
   it cost the browser eleven lines and the protocol nothing.
5. **Version-mismatch means reload**, not merge (§7.3).
6. **§6.1 (toolchain) happens first; §6.2 (namespace -> ES modules) is not part of
   this work.** Reopen only if new core-module Typescript must be able to import npm
   packages.
