/*
 * The browser side of DomUI's maxGraph component. This is the only file in DomUI
 * that knows the maxGraph API: everything the Java side sends is in DomUI's own
 * vocabulary, so a maxGraph upgrade is this file plus a rebuilt bundle.
 *
 * esbuild bundles it into one IIFE which exposes the exports below as the global
 * DomUIMaxGraph; see the module's README for the build.
 */
import { Client, Graph, InternalEvent } from '@maxgraph/core';

export interface CreateOptions {
	/** Server path the maxGraph images are served from, without a trailing slash. */
	imageBase?: string;

	/** Allow the drawing to be panned by dragging its background. */
	panning?: boolean;
}

interface Instance {
	graph: Graph;
}

const instances = new Map<string, Instance>();

/**
 * Create a graph inside the element with this id, replacing whatever instance
 * was there before.
 */
export function create(id: string, options: CreateOptions = {}): void {
	const container = document.getElementById(id);
	if(null == container) {
		console.error("DomUIMaxGraph: no element with id '" + id + "'");
		return;
	}
	destroy(id);

	if(options.imageBase) {
		Client.setImageBasePath(options.imageBase);
	}
	InternalEvent.disableContextMenu(container);

	const graph = new Graph(container);
	graph.setPanning(options.panning !== false);
	instances.set(id, {graph});

	//-- Phase 0: a hard-coded drawing, to prove the bundle. The model arrives from
	//-- the server in phase 1.
	graph.batchUpdate(() => {
		const from = graph.insertVertex({
			id: 'n1', value: 'Hello', position: [40, 40], size: [120, 40]
		});
		const to = graph.insertVertex({
			id: 'n2', value: 'World', position: [260, 160], size: [120, 40],
			style: {shape: 'ellipse', fillColor: '#ddeeff'}
		});
		graph.insertEdge({id: 'e1', source: from, target: to, value: 'edge'});
	});
}

/**
 * Throw away the graph in this element, if there is one. Called when the
 * component leaves the page.
 */
export function destroy(id: string): void {
	const instance = instances.get(id);
	if(undefined === instance) {
		return;
	}
	instance.graph.destroy();
	instances.delete(id);
}

/** The maxGraph instance for this element, for debugging from the console. */
export function graphFor(id: string): Graph | undefined {
	return instances.get(id)?.graph;
}
