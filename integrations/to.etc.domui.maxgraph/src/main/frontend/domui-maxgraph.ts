/*
 * The browser side of DomUI's maxGraph component. This is the only file in DomUI
 * that knows the maxGraph API: what the server sends is in DomUI's own vocabulary,
 * so a maxGraph upgrade is this file plus a rebuilt bundle.
 *
 * esbuild bundles it into one IIFE which exposes the exports below as the global
 * DomUIMaxGraph; see the module's README for the build.
 */
import { Cell, Client, Graph, InternalEvent, Point } from '@maxgraph/core';

/** DomUI's own Javascript, which is loaded before this and lives in its own global. */
declare const WebUI: {
	jsoncall(id: string, fields: object, callback: (response: any) => void): void;
};

export interface CreateOptions {
	/** Server path the maxGraph images are served from, without a trailing slash. */
	imageBase?: string;
}

/** One cell of the model document the server sends. */
interface CellDoc {
	id: string;
	kind: 'node' | 'edge';
	label?: string;
	style?: Record<string, unknown>;

	/** Nodes. */
	parent?: string;
	x?: number;
	y?: number;
	w?: number;
	h?: number;

	/** Edges. */
	source?: string;
	target?: string;
	points?: number[][];
}

interface ModelDoc {
	version: number;
	options?: {panning?: boolean};
	cells?: CellDoc[];
}

interface Instance {
	graph: Graph;
	version: number;
	cellById: Map<string, Cell>;
}

const instances = new Map<string, Instance>();

/**
 * Create a graph inside the element with this id and fill it with the model the
 * server has for that component, replacing whatever instance was there before.
 *
 * The call carries no state of its own: it runs again on every full page render, and
 * asks the server for the drawing each time.
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
	readOnly(graph);
	const instance: Instance = {graph, version: -1, cellById: new Map()};
	instances.set(id, instance);

	WebUI.jsoncall(id, {}, (response: ModelDoc) => {
		//-- The panel can have gone away while we were asking.
		if(instances.get(id) !== instance) {
			return;
		}
		build(instance, response);
	});
}

/**
 * Throw away the graph in this element, if there is one. Called when the component
 * leaves the page.
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

/**
 * Nothing in the drawing can be changed from the browser: it is the server's model
 * that decides what it looks like. Selecting, panning and zooming stay.
 */
function readOnly(graph: Graph): void {
	graph.setCellsEditable(false);
	graph.setCellsMovable(false);
	graph.setCellsResizable(false);
	graph.setCellsDeletable(false);
	graph.setCellsBendable(false);
	graph.setConnectable(false);
	graph.setDropEnabled(false);
}

/**
 * Build the whole drawing from a model document, inside one transaction so the browser
 * lays out and paints once.
 */
function build(instance: Instance, doc: ModelDoc): void {
	const graph = instance.graph;
	instance.version = doc.version;
	instance.cellById.clear();

	graph.setPanning(doc.options?.panning !== false);
	zoomOnCtrlWheel(graph);

	graph.batchUpdate(() => {
		for(const cell of doc.cells ?? []) {
			if('node' === cell.kind) {
				addNode(instance, cell);
			} else {
				addEdge(instance, cell);
			}
		}
	});
}

function addNode(instance: Instance, doc: CellDoc): void {
	const parent = undefined === doc.parent ? undefined : instance.cellById.get(doc.parent);
	const cell = instance.graph.insertVertex({
		id: doc.id,
		parent: parent,
		value: doc.label ?? '',
		position: [doc.x ?? 0, doc.y ?? 0],
		size: [doc.w ?? 0, doc.h ?? 0],
		style: doc.style ?? {}
	});
	instance.cellById.set(doc.id, cell);
}

function addEdge(instance: Instance, doc: CellDoc): void {
	const cell = instance.graph.insertEdge({
		id: doc.id,
		source: undefined === doc.source ? null : instance.cellById.get(doc.source),
		target: undefined === doc.target ? null : instance.cellById.get(doc.target),
		value: doc.label ?? '',
		style: doc.style ?? {}
	});
	const points = doc.points;
	if(undefined !== points && points.length > 0) {
		const geometry = cell.getGeometry();
		if(null != geometry) {
			geometry.points = points.map(p => new Point(p[0], p[1]));
		}
	}
	instance.cellById.set(doc.id, cell);
}

/**
 * Zoom with ctrl+wheel. A plain wheel keeps scrolling the page, which is what a reader
 * of a page that happens to contain a drawing expects.
 */
function zoomOnCtrlWheel(graph: Graph): void {
	const container = graph.container;
	if(null == container) {
		return;
	}
	InternalEvent.addMouseWheelListener((event: Event, up: boolean) => {
		const wheel = event as WheelEvent;
		if(!wheel.ctrlKey && !wheel.metaKey) {
			return;
		}
		if(!container.contains(wheel.target as Node)) {
			return;
		}
		if(up) {
			graph.zoomIn();
		} else {
			graph.zoomOut();
		}
		InternalEvent.consume(event);
	}, container);
}
