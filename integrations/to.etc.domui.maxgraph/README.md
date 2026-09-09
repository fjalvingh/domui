# DomUI maxGraph integration

A DomUI component that draws diagrams with [maxGraph](https://github.com/maxGraph/maxGraph),
plus the maxGraph bundle it needs.

## The bundle is committed

maxGraph ships as ES modules only - it has had no UMD build since 0.5.0 - so it has to
be bundled before a browser can load it. That bundle lives in
`src/main/resources/META-INF/resources/js/maxgraph` and **is committed**, which is why
this module builds with plain Maven and needs no node.

Rebuild it after changing `src/main/frontend/domui-maxgraph.ts` or the maxGraph version:

```bash
$ npm install
$ npm run bundle
```

`npm run bundle` type-checks the wrapper (esbuild itself does not), writes
`domui-maxgraph.js` and `domui-maxgraph-min.js`, and copies maxGraph's own `css` and
`images` next to them. Commit what changes.

The committed bundles carry **no source maps**: they would be four megabytes of git
history that only someone editing `domui-maxgraph.ts` can use, and that person can
rebuild. Add `--sourcemap` to the `bundle-js` command for as long as you need one.

The maxGraph version is pinned exactly in `package.json`: maxGraph breaks API on nearly
every minor release, and `src/main/frontend/domui-maxgraph.ts` is the only file that
touches that API. Last built with **node v22.22.1** against **@maxgraph/core 0.24.0**.

## What is served

| Path | What |
| --- | --- |
| `$js/maxgraph/domui-maxgraph.js` | the bundle; production gets the `-min` sibling |
| `$js/maxgraph/domui-maxgraph.css` | the rules for the container DomUI renders |
| `$js/maxgraph/css/common.css` | maxGraph's own stylesheet, as it ships it |
| `js/maxgraph/images/` | maxGraph's images, which `Client.setImageBasePath()` is pointed at |

maxGraph is Apache-2.0; its LICENSE is copied next to the bundle.
