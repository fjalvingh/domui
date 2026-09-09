/*
 * Copy the parts of @maxgraph/core that are not Javascript into the module's web
 * resources: the stylesheet and the images it and the graph handles refer to. The
 * layout mirrors the npm package, so common.css's own ../images/ references hold.
 */
import {cp, mkdir, rm} from 'node:fs/promises';

const from = 'node_modules/@maxgraph/core';
const to = 'src/main/resources/META-INF/resources/js/maxgraph';

await rm(`${to}/css`, {recursive: true, force: true});
await rm(`${to}/images`, {recursive: true, force: true});
await mkdir(`${to}/css`, {recursive: true});
await cp(`${from}/css`, `${to}/css`, {recursive: true});
await cp(`${from}/images`, `${to}/images`, {recursive: true});
await cp(`${from}/LICENSE`, `${to}/LICENSE`);
console.log(`copied maxGraph css, images and LICENSE into ${to}`);
