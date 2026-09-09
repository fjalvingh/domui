/*
 * The browser side of DomUI's maxGraph component. This is the only file in DomUI
 * that knows the maxGraph API: what the server sends is in DomUI's own vocabulary,
 * so a maxGraph upgrade is this file plus a rebuilt bundle.
 *
 * esbuild bundles it into one IIFE which exposes the exports below as the global
 * DomUIMaxGraph; see the module's README for the build.
 */
import { Cell, Client, Geometry, Graph, InternalEvent, Point } from '@maxgraph/core';

/** DomUI's own Javascript, which is loaded before this and lives in its own global. */
declare const WebUI: {
	jsoncall(id: string, fields: object, callback: (response: any) => void): void;
};

export interface CreateOptions {
	/** Server path the maxGraph images are served from, without a trailing slash. */
	imageBase?: string;
}

/** One cell of the model document the server sends, and the payload of a change to one. */
interface CellDoc {
	id: string;
	kind?: 'node' | 'edge';
	label?: string;
	style?: Record<string, unknown>;

	/** Nodes. */
	parent?: string;
	x?: number;
	y?: number;
	w?: number;
	h?: number;

	/** Edges. A terminal that is sent as null is one that was taken away. */
	source?: string | null;
	target?: string | null;
	points?: number[][];
}

interface ModelDoc {
	version: number;
	options?: {panning?: boolean};
	cells?: CellDoc[];
}

/** One change to the drawing: what happened, plus the fields that kind of change carries. */
interface OpDoc extends CellDoc {
	op: string;
}

/**
 * A list of changes, and the model version it turns into. It only applies to a drawing
 * that is at "base"; anything else means we saw a different history than the server did.
 */
interface DeltaDoc {
	base: number;
	version: number;
	ops?: OpDoc[];
}

interface Instance {
	graph: Graph;
	/** The model version this drawing shows, or -1 while there is no drawing yet. */
	version: number;
	/** False while we are waiting for a model document; changes that arrive then wait too. */
	loaded: boolean;
	queue: DeltaDoc[];
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
	zoomOnCtrlWheel(graph);
	const instance: Instance = {graph, version: -1, loaded: false, queue: [], cellById: new Map()};
	instances.set(id, instance);
	load(id, instance);
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

/**
 * Apply the changes the server made to its model to the drawing that is already there,
 * so that what did not change is not touched.
 *
 * A change list that arrives before the drawing does is kept until it has: the document
 * that is on its way is newer than the list, and the list then finds itself already
 * applied and does nothing.
 */
export function apply(id: string, delta: DeltaDoc): void {
	const instance = instances.get(id);
	if(undefined === instance) {
		return;
	}
	if(!instance.loaded) {
		instance.queue.push(delta);
		return;
	}
	applyDelta(id, instance, delta);
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
 * Ask the server for the model of this component and draw it, replacing whatever is
 * drawn now. Changes that come in while we wait are queued by {@link apply}.
 */
function load(id: string, instance: Instance): void {
	instance.loaded = false;
	WebUI.jsoncall(id, {}, (response: ModelDoc) => {
		//-- The panel can have gone away, or been recreated, while we were asking.
		if(instances.get(id) !== instance) {
			return;
		}
		build(instance, response);
		instance.loaded = true;

		const queue = instance.queue;
		instance.queue = [];
		for(const delta of queue) {
			applyDelta(id, instance, delta);
		}
	});
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

	graph.batchUpdate(() => {
		for(const child of graph.getChildCells(graph.getDefaultParent(), true, true)) {
			graph.getDataModel().remove(child);
		}
		for(const cell of doc.cells ?? []) {
			if('edge' === cell.kind) {
				addEdge(instance, cell);
			} else {
				addNode(instance, cell);
			}
		}
	});
}

/**
 * Walk a change list. A list that was made against another version of the model than
 * this drawing shows is either one we already have - the drawing was rebuilt from a
 * document that is newer than the list - or a sign that we missed one, and then the only
 * cure is to ask for the whole model again.
 */
function applyDelta(id: string, instance: Instance, delta: DeltaDoc): void {
	if(delta.base !== instance.version) {
		if(delta.version <= instance.version) {
			return;
		}
		load(id, instance);
		return;
	}

	let reload = false;
	instance.graph.batchUpdate(() => {
		for(const op of delta.ops ?? []) {
			if('reload' === op.op) {
				reload = true;
				return;
			}
			applyOp(instance, op);
		}
	});
	if(reload) {
		load(id, instance);
	} else {
		instance.version = delta.version;
	}
}

function applyOp(instance: Instance, op: OpDoc): void {
	if('addNode' === op.op) {
		addNode(instance, op);
		return;
	}
	if('addEdge' === op.op) {
		addEdge(instance, op);
		return;
	}

	const cell = instance.cellById.get(op.id);
	if(undefined === cell) {
		return;                                            // Gone already; every change is about a cell that may not be there.
	}
	const model = instance.graph.getDataModel();
	switch(op.op) {
		default:
			console.error("DomUIMaxGraph: unknown operation '" + op.op + "'");
			break;

		case 'remove':
			//-- Through the model, not through the graph: the graph refuses to delete what it
			//-- was told is not deletable, and everything here is not deletable by the user.
			model.remove(cell);
			instance.cellById.delete(op.id);
			break;

		case 'label':
			model.setValue(cell, op.label ?? '');
			break;

		case 'style':
			model.setStyle(cell, op.style ?? {});
			break;

		case 'geometry':
			model.setGeometry(cell, movedGeometry(cell, op));
			break;

		case 'terminal':
			model.setTerminal(cell, terminal(instance, op.source), true);
			model.setTerminal(cell, terminal(instance, op.target), false);
			break;

		case 'points':
			model.setGeometry(cell, routedGeometry(cell, op));
			break;
	}
}

function terminal(instance: Instance, id: string | null | undefined): Cell | null {
	return null == id ? null : instance.cellById.get(id) ?? null;
}

/** The cell's geometry with the bounds the change carries; a geometry is replaced, not edited. */
function movedGeometry(cell: Cell, op: CellDoc): Geometry {
	const geometry = cell.getGeometry()?.clone() ?? new Geometry();
	geometry.x = op.x ?? 0;
	geometry.y = op.y ?? 0;
	geometry.width = op.w ?? 0;
	geometry.height = op.h ?? 0;
	return geometry;
}

function routedGeometry(cell: Cell, op: CellDoc): Geometry {
	const geometry = cell.getGeometry()?.clone() ?? new Geometry();
	geometry.points = (op.points ?? []).map(p => new Point(p[0], p[1]));
	return geometry;
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
		source: terminal(instance, doc.source),
		target: terminal(instance, doc.target),
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
