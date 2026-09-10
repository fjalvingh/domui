/*
 * The browser side of DomUI's maxGraph component. This is the only file in DomUI
 * that knows the maxGraph API: what the server sends is in DomUI's own vocabulary,
 * so a maxGraph upgrade is this file plus a rebuilt bundle.
 *
 * esbuild bundles it into one IIFE which exposes the exports below as the global
 * DomUIMaxGraph; see the module's README for the build.
 */
import {
	Cell, ChildChange, Client, ConnectionHandler, EdgeHandlerConfig, Geometry, GeometryChange, Graph,
	gestureUtils, ImageBox, InternalEvent, KeyHandler, Point, StyleChange, TerminalChange, ValueChange
} from '@maxgraph/core';

/**
 * Show the handle in the middle of a selected edge that bends it. maxGraph hides it by
 * default, which leaves an edge with no waypoints yet impossible to bend at all - the
 * handles it does show are the ones for waypoints that already exist. It is a setting of
 * the library rather than of a graph, and every graph in this bundle is one of ours.
 */
EdgeHandlerConfig.virtualBendsEnabled = true;

/**
 * The dot that appears in the middle of a shape the pointer is over, and that a connection
 * is drawn from. maxGraph has no image for this and without one a connection would start
 * from the middle of a shape with nothing to say so - and dragging a shape to move it would
 * connect it instead.
 */
const CONNECT_ICON = 'data:image/svg+xml;utf8,'
	+ encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14">'
		+ '<circle cx="7" cy="7" r="6" fill="#82b366" stroke="#ffffff" stroke-width="2"/></svg>');

/** DomUI's own Javascript, which is loaded before this and lives in its own global. */
declare const WebUI: {
	jsoncall(id: string, fields: object, callback: (response: any) => void): void;
	sendJsonAction(id: string, action: string, json: object): void;
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

/** One thing the user can drag into the drawing. What it becomes is decided on the server. */
interface PaletteDoc {
	key: string;
	label?: string;
	w: number;
	h: number;
	style?: Record<string, unknown>;
}

interface ModelDoc {
	version: number;
	options?: {panning?: boolean, editable?: boolean, connectable?: boolean, palette?: PaletteDoc[]};
	cells?: CellDoc[];
}

/** One change to the drawing: what happened, plus the fields that kind of change carries. */
interface OpDoc extends CellDoc {
	op: string;
	/** The palette item that was dropped, for a requestNode. */
	key?: string;
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
	/** True while a change list from the server is being applied, so it is not sent back. */
	applying: boolean;
	editable: boolean;
	keyHandler: KeyHandler;
	/** The strip the palette items live in, above the drawing. */
	palette: HTMLElement;
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

	//-- DomUI renders one empty div and never looks inside it, so the two parts of the
	//-- widget are made here: the palette strip, and the canvas maxGraph owns.
	container.innerHTML = '';
	const palette = document.createElement('div');
	palette.className = 'ui-mxgr-palette';
	palette.hidden = true;
	container.appendChild(palette);
	const canvas = document.createElement('div');
	canvas.className = 'ui-mxgr-canvas';
	container.appendChild(canvas);
	InternalEvent.disableContextMenu(canvas);

	const graph = new Graph(canvas);
	editingAllowed(graph, false, false);
	zoomOnCtrlWheel(graph);

	const keyHandler = new KeyHandler(graph, canvas);
	keyHandler.bindKey(46, () => graph.removeCells(graph.getSelectionCells(), true));
	keyHandler.setEnabled(false);

	const instance: Instance = {
		graph, version: -1, loaded: false, queue: [], cellById: new Map(),
		applying: false, editable: false, keyHandler, palette
	};
	instances.set(id, instance);
	undoKeys(id, instance, keyHandler);
	askForEdges(id, instance);

	//-- A key press only reaches the graph when it happens inside its own container, and
	//-- clicking a shape does not put the focus there by itself. It has to be the pointer
	//-- event: maxGraph consumes those, and a consumed pointerdown means no mousedown at all.
	canvas.addEventListener('pointerdown', () => {
		if(instance.editable) {
			canvas.focus();
		}
	});
	graph.getDataModel().addListener(InternalEvent.CHANGE, (_sender: unknown, event: any) => {
		sendChanges(id, instance, event.getProperty('edit'));
	});
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
 * What the user may do to the drawing. A read-only drawing can be selected in, panned and
 * zoomed, and nothing else: it is the server's model that decides what it looks like. An
 * editable one adds moving, resizing, renaming, bending and deleting - each of which is
 * sent to the server, which has the last word on it - and, where the server has something
 * to make an edge with, drawing a connection, which is not a change but a request for one.
 *
 * Connecting is separate because it costs something: the dot that starts a connection sits
 * in the middle of a node, so where connecting is on, that is where it happens instead of
 * dragging the node.
 */
function editingAllowed(graph: Graph, editable: boolean, connectable: boolean): void {
	graph.setDropEnabled(false);

	graph.setCellsMovable(editable);
	graph.setCellsResizable(editable);
	graph.setCellsDeletable(editable);
	graph.setCellsEditable(editable);
	graph.setCellsBendable(editable);
	graph.setConnectable(editable && connectable);

	//-- A connection has to end on a node: one that ends nowhere would be an edge the
	//-- server cannot make sense of.
	graph.setAllowDanglingEdges(false);
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
		build(id, instance, response);
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
function build(id: string, instance: Instance, doc: ModelDoc): void {
	const graph = instance.graph;
	instance.version = doc.version;
	instance.cellById.clear();

	graph.setPanning(doc.options?.panning !== false);
	instance.editable = doc.options?.editable === true;
	editingAllowed(graph, instance.editable, doc.options?.connectable === true);
	instance.keyHandler.setEnabled(instance.editable);
	const canvas = graph.container;
	if(null != canvas) {
		//-- Only a drawing that can be edited belongs in the tab order.
		if(instance.editable) {
			canvas.setAttribute('tabindex', '0');
		} else {
			canvas.removeAttribute('tabindex');
		}
	}
	buildPalette(id, instance, doc.options?.palette ?? []);

	withoutEcho(instance, () => graph.batchUpdate(() => {
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
	}));
}

/**
 * Do something to the drawing without it being reported back to the server: the server is
 * where it came from, and telling it what it just told us would be an echo.
 */
function withoutEcho(instance: Instance, what: () => void): void {
	const was = instance.applying;
	instance.applying = true;
	try {
		what();
	} finally {
		instance.applying = was;
	}
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
	withoutEcho(instance, () => instance.graph.batchUpdate(() => {
		for(const op of delta.ops ?? []) {
			if('reload' === op.op) {
				reload = true;
				return;
			}
			applyOp(instance, op);
		}
	}));
	if(reload) {
		load(id, instance);
	} else {
		instance.version = delta.version;
	}
}

/*----------------------------------------------------------------------*/
/*	CODING: What the user wants made                                    */
/*----------------------------------------------------------------------*/

/**
 * Drawing a connection asks the server for an edge instead of making one.
 *
 * maxGraph would insert the edge itself at the end of the gesture, with an id of its own
 * invention that means nothing here. Taking over the one method that does it leaves the
 * preview, the highlighting and the reset exactly as they were, and no cell is ever made
 * in the browser.
 */
function askForEdges(id: string, instance: Instance): void {
	const handler = instance.graph.getPlugin('ConnectionHandler') as ConnectionHandler | undefined;
	if(undefined === handler) {
		return;
	}
	handler.connectImage = new ImageBox(CONNECT_ICON, 14, 14);
	handler.connect = (source: Cell | null, target: Cell | null) => {
		const from = idOf(source);
		const to = idOf(target);
		if(null !== from && null !== to) {
			send(id, instance, [{op: 'requestEdge', id: from, source: from, target: to}]);
		}
	};
}

/**
 * Fill the strip above the drawing with what can be dragged into it. Dropping an item
 * asks the server for a node at that place, in the drawing's own coordinates.
 */
function buildPalette(id: string, instance: Instance, items: PaletteDoc[]): void {
	const palette = instance.palette;
	palette.innerHTML = '';
	palette.hidden = 0 === items.length || !instance.editable;
	if(palette.hidden) {
		return;
	}
	for(const item of items) {
		const element = document.createElement('div');
		element.className = 'ui-mxgr-pi';
		element.textContent = item.label ?? item.key;
		palette.appendChild(element);

		const preview = document.createElement('div');
		preview.className = 'ui-mxgr-pi-drag';
		preview.style.width = item.w + 'px';
		preview.style.height = item.h + 'px';

		gestureUtils.makeDraggable(element, instance.graph, (_graph, _event, _cell, x, y) => {
			send(id, instance, [{op: 'requestNode', id: item.key, key: item.key,
				x: (x ?? 0) - item.w / 2, y: (y ?? 0) - item.h / 2}]);
		}, preview);
	}
}

/**
 * ctrl-Z and ctrl-Y ask the server to move through the history it keeps; maxGraph's own
 * UndoManager is deliberately not installed anywhere in this file.
 *
 * The model is the server's, and so is everything an undo needs: a cell the user deleted
 * still exists there, with its id and the application's own data on it, while here there
 * is nothing left to put back. So the browser only says which key was pressed, and what
 * comes back is the ordinary list of changes.
 *
 * A keystroke inside the label editor is left alone: KeyHandler ignores everything while
 * the graph is editing, so ctrl-Z there undoes typing, as it should.
 */
function undoKeys(id: string, instance: Instance, keyHandler: KeyHandler): void {
	keyHandler.bindControlKey(90, () => send(id, instance, [{op: 'requestUndo', id: ''}]));
	keyHandler.bindControlKey(89, () => send(id, instance, [{op: 'requestRedo', id: ''}]));
	//-- ctrl-shift-Z is the other redo, and the only one people use on a Mac.
	keyHandler.bindControlShiftKey(90, () => send(id, instance, [{op: 'requestRedo', id: ''}]));
}

/*----------------------------------------------------------------------*/
/*	CODING: What the user changed                                       */
/*----------------------------------------------------------------------*/

/**
 * Called at the end of every transaction on the drawing. What the user did is translated
 * into the same operations the server sends the other way and posted as a normal page
 * action, so the answer is the ordinary page delta - including whatever the server decides
 * to change back.
 */
function sendChanges(id: string, instance: Instance, edit: {changes?: unknown[]} | undefined): void {
	if(instance.applying || !instance.editable || undefined === edit) {
		return;
	}
	const ops: OpDoc[] = [];
	for(const change of edit.changes ?? []) {
		const op = translate(change);
		if(null !== op && !ops.some(o => o.op === op.op && o.id === op.id)) {
			//-- A cell the user threw away is no longer addressable here either. Forgetting it
			//-- is what lets the server put it back: an add for a cell we still knew about
			//-- would be taken for a repeat and ignored.
			if('remove' === op.op) {
				instance.cellById.delete(op.id);
			}
			ops.push(op);
		}
	}
	if(0 === ops.length) {
		return;
	}
	send(id, instance, ops);
}

/** Everything the browser has to say about a drawing goes as one page action. */
function send(id: string, instance: Instance, ops: OpDoc[]): void {
	WebUI.sendJsonAction(id, 'GRAPHCHANGE', {base: instance.version, ops});
}

/**
 * One maxGraph change as one of our operations, or null for a change the server has no
 * word for. What is sent is read from the cell as it is now, not from the change: the
 * change has been executed by the time we see it, and its own fields have been swapped
 * around for the undo stack.
 */
function translate(change: unknown): OpDoc | null {
	if(change instanceof GeometryChange) {
		//-- A node's geometry is where it is; an edge's is the points it is bent through.
		return change.cell.isVertex() ? geometryOp(change.cell) : pointsOp(change.cell);
	}
	if(change instanceof ChildChange) {
		//-- parent is where the cell ended up: nowhere means it was deleted. A cell that
		//-- appeared is not sent - only the server hands out ids.
		return null === change.parent ? opFor('remove', change.child) : null;
	}
	if(change instanceof ValueChange) {
		const doc = opFor('label', change.cell);
		if(null !== doc) {
			doc.label = String(change.cell.getValue() ?? '');
		}
		return doc;
	}
	if(change instanceof TerminalChange) {
		const doc = opFor('terminal', change.cell);
		if(null !== doc) {
			doc.source = idOf(change.cell.getTerminal(true));
			doc.target = idOf(change.cell.getTerminal(false));
		}
		return doc;
	}
	if(change instanceof StyleChange) {
		const doc = opFor('style', change.cell);
		if(null !== doc) {
			doc.style = change.cell.getStyle() as Record<string, unknown>;
		}
		return doc;
	}
	return null;
}

function opFor(name: string, cell: Cell): OpDoc | null {
	const id = cell.getId();
	return null == id ? null : {op: name, id};
}

function geometryOp(cell: Cell): OpDoc | null {
	const doc = opFor('geometry', cell);
	const geometry = cell.getGeometry();
	if(null === doc || null == geometry) {
		return null;
	}
	doc.x = geometry.x;
	doc.y = geometry.y;
	doc.w = geometry.width;
	doc.h = geometry.height;
	return doc;
}

function pointsOp(cell: Cell): OpDoc | null {
	const doc = opFor('points', cell);
	if(null === doc) {
		return null;
	}
	doc.points = (cell.getGeometry()?.points ?? []).map(p => [p.x, p.y]);
	return doc;
}

function idOf(cell: Cell | null): string | null {
	return null == cell ? null : cell.getId();
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

/**
 * Adding a cell that is already there does nothing. The server sends a cell again to put
 * back a change it refused, and what it takes back may be only part of what the browser
 * threw away - so an add has to be as harmless to repeat as every other operation.
 */
function addNode(instance: Instance, doc: CellDoc): void {
	if(instance.cellById.has(doc.id)) {
		return;
	}
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
	if(instance.cellById.has(doc.id)) {
		return;
	}
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
