/*
 * The browser side of DomUI's maxGraph component. This is the only file in DomUI
 * that knows the maxGraph API: what the server sends is in DomUI's own vocabulary,
 * so a maxGraph upgrade is this file plus a rebuilt bundle.
 *
 * esbuild bundles it into one IIFE which exposes the exports below as the global
 * DomUIMaxGraph; see the module's README for the build.
 */
import {
	Cell, ChildChange, CircleLayout, Client, CompactTreeLayout, ConnectionHandler, DirectionValue,
	EdgeHandlerConfig, FastOrganicLayout, Geometry, GeometryChange, Graph, GraphLayout,
	gestureUtils, HierarchicalLayout, ImageBox, ImageExport, InternalEvent, KeyHandler,
	ParallelEdgeLayout, Point, RadialTreeLayout, StyleChange, SvgCanvas2D, TerminalChange, ValueChange
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

/** Arrange the drawing: which layout, and which way it is to grow. */
interface LayoutDoc {
	layout: string;
	direction: DirectionValue;
}

/** A picture of the drawing: in what format, at what size, and where it is to go. */
interface ExportDoc {
	format: string;
	scale: number;
	/** Set when the server wants the picture: the token its answer has to name. */
	token?: string;
	/** Set when the user is to keep the picture: the name to save it under. */
	name?: string;
}

/** A picture, as base64 - which is what both the server and a download want. */
interface Picture {
	data: string;
	width: number;
	height: number;
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
	/** True while a layout the server asked for is running: what it moves is reported even in a read-only drawing. */
	layingOut: boolean;
	/** A layout that arrived before the drawing did, waiting for it. */
	pendingLayout: LayoutDoc | null;
	/** Pictures that were asked for before the drawing was there, waiting for it. */
	pendingPictures: (() => void)[];
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
		applying: false, layingOut: false, pendingLayout: null, pendingPictures: [],
		editable: false, keyHandler, palette
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

/**
 * Arrange the drawing the way the server asked for. A layout that arrives before the
 * drawing does waits for it: it is about the model that is on its way.
 */
export function layout(id: string, request: LayoutDoc): void {
	const instance = instances.get(id);
	if(undefined === instance) {
		return;
	}
	if(!instance.loaded) {
		instance.pendingLayout = request;
		return;
	}
	runLayout(id, instance, request);
}

/**
 * Make a picture of the drawing and post it back to the server, under the token it was
 * asked for with. Nothing is shown to the user; the page decides what happens to it.
 */
export function exportImage(id: string, request: ExportDoc): void {
	whenDrawn(id, instance => picture(instance, request)
		.then(made => WebUI.sendJsonAction(id, 'GRAPHEXPORT', {
			token: request.token, format: request.format,
			width: made.width, height: made.height, data: made.data
		}))
		.catch(x => console.error('DomUIMaxGraph: the picture could not be made', x)));
}

/**
 * Make a picture of the drawing and hand it to the user under the name given. The server
 * never sees it: the drawing is here, and so is the file.
 */
export function download(id: string, request: ExportDoc): void {
	whenDrawn(id, instance => picture(instance, request)
		.then(made => save(request.name ?? 'drawing', mimeOf(request.format), made.data))
		.catch(x => console.error('DomUIMaxGraph: the picture could not be made', x)));
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
		const waiting = instance.pendingLayout;
		if(null !== waiting) {
			instance.pendingLayout = null;
			runLayout(id, instance, waiting);
		}
		//-- After the layout, so that a picture asked for with it is one of the arranged drawing.
		const pictures = instance.pendingPictures;
		instance.pendingPictures = [];
		for(const make of pictures) {
			make();
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
/*	CODING: Arranging the drawing                                       */
/*----------------------------------------------------------------------*/

/**
 * Run a layout over the whole drawing and let the server know where everything ended up.
 *
 * The arranging is done here because this is where the drawing is - maxGraph's layouts
 * work on the cells it has - but what they move is model data, so it goes back the way a
 * user's own drag does: as geometry changes, through the same listener. That is why the
 * echo is *not* suppressed here, and why a read-only drawing reports these changes even
 * though the user could not have made them.
 */
function runLayout(id: string, instance: Instance, request: LayoutDoc): void {
	const graph = instance.graph;
	const layout = layoutFor(graph, request);
	if(null === layout) {
		console.error("DomUIMaxGraph: unknown layout '" + request.layout + "'");
		return;
	}
	const was = instance.layingOut;
	instance.layingOut = true;
	try {
		graph.batchUpdate(() => layout.execute(graph.getDefaultParent()));
	} finally {
		instance.layingOut = was;
	}
}

/**
 * The layout to run. Every maxGraph layout type stays in this file, so the server's
 * vocabulary is ours and an upgrade that renames one is this function.
 */
function layoutFor(graph: Graph, request: LayoutDoc): GraphLayout | null {
	//-- A tree grows sideways when its roots are to the east or west, and is inverted when
	//-- it grows towards them - which is the same thing the hierarchical layout's direction says.
	const horizontal = 'east' === request.direction || 'west' === request.direction;
	const inverted = 'east' === request.direction || 'south' === request.direction;
	switch(request.layout) {
		default:
			return null;

		case 'hierarchical':
			return new HierarchicalLayout(graph, request.direction);

		case 'organic':
			return new FastOrganicLayout(graph);

		case 'circle':
			return new CircleLayout(graph);

		case 'tree':
			return new CompactTreeLayout(graph, horizontal, inverted);

		case 'radialTree':
			return new RadialTreeLayout(graph);

		case 'parallelEdges':
			return new ParallelEdgeLayout(graph);
	}
}

/*----------------------------------------------------------------------*/
/*	CODING: A picture of the drawing                                    */
/*----------------------------------------------------------------------*/

/** How much room is left around the drawing in a picture of it. */
const PICTURE_BORDER = 4;

/**
 * Do something with a drawing once there is one. A picture that is asked for before the
 * model has arrived is a picture of the model that is on its way.
 */
function whenDrawn(id: string, what: (instance: Instance) => void): void {
	const instance = instances.get(id);
	if(undefined === instance) {
		return;
	}
	if(instance.loaded) {
		what(instance);
	} else {
		instance.pendingPictures.push(() => what(instance));
	}
}

/**
 * A picture of the whole drawing, whatever part of it is scrolled into view and whatever
 * the user has zoomed to. It is not the svg that is on the screen: that one is clipped by
 * its container, carries the handles and the selection, and leans on the page's
 * stylesheets. This is a document of its own, drawn again from the states maxGraph
 * computed - so the edges run and the labels sit where the screen has them, and nothing
 * else of the page is in it.
 */
function picture(instance: Instance, request: ExportDoc): Promise<Picture> {
	const drawn = drawing(instance.graph, request.scale);
	const document = '<?xml version="1.0" encoding="UTF-8"?>\n' + new XMLSerializer().serializeToString(drawn.root);
	const data = base64(new TextEncoder().encode(document));
	if('png' !== request.format) {
		return Promise.resolve({data, width: drawn.width, height: drawn.height});
	}
	return rasterize(data, drawn.width, drawn.height);
}

/** Draw the graph into an svg document of its own, at this many times its own size. */
function drawing(graph: Graph, scale: number): {root: SVGElement, width: number, height: number} {
	const bounds = graph.getGraphBounds();
	//-- What is on the screen is zoomed by the view's scale; a picture is not, so it is divided out.
	const zoom = graph.getView().scale;
	const width = Math.max(1, Math.ceil(bounds.width * scale / zoom) + 2 * PICTURE_BORDER);
	const height = Math.max(1, Math.ceil(bounds.height * scale / zoom) + 2 * PICTURE_BORDER);

	const root = window.document.createElementNS('http://www.w3.org/2000/svg', 'svg');
	root.setAttribute('xmlns', 'http://www.w3.org/2000/svg');
	root.setAttribute('version', '1.1');
	root.setAttribute('width', String(width));
	root.setAttribute('height', String(height));

	const canvas = new SvgCanvas2D(root, false);
	//-- Labels as <text> and not as html in a foreignObject: a browser rasterizes the first
	//-- and gives up on the second, and a png is made by rasterizing this very document.
	canvas.foEnabled = false;
	canvas.translate(Math.floor((PICTURE_BORDER / scale - bounds.x) / zoom), Math.floor((PICTURE_BORDER / scale - bounds.y) / zoom));
	canvas.scale(scale / zoom);

	const cell = graph.getDataModel().getRoot();
	const state = null === cell ? null : graph.getView().getState(cell);
	if(null !== state) {
		new ImageExport().drawState(state, canvas);
	}
	return {root, width, height};
}

/**
 * The png, made by drawing the svg one onto a canvas. It is the only way to a png in a
 * browser, and it is why the svg has to be a document that stands on its own: what is
 * loaded from a data url may not fetch anything else, so an image in the drawing - or a
 * font it does not carry - is not in the picture.
 */
function rasterize(svg: string, width: number, height: number): Promise<Picture> {
	return new Promise((resolve, reject) => {
		const image = new Image();
		image.onload = () => {
			const canvas = window.document.createElement('canvas');
			canvas.width = width;
			canvas.height = height;
			const context = canvas.getContext('2d');
			if(null === context) {
				reject(new Error('this browser has no 2d canvas'));
				return;
			}
			//-- A drawing on the screen sits on the page's white; a png without this one sits on
			//-- nothing at all, and is then printed on whatever it lands on.
			context.fillStyle = '#ffffff';
			context.fillRect(0, 0, width, height);
			context.drawImage(image, 0, 0, width, height);
			const url = canvas.toDataURL('image/png');
			resolve({data: url.substring(url.indexOf(',') + 1), width, height});
		};
		image.onerror = () => reject(new Error('the drawing could not be rasterized'));
		image.src = 'data:image/svg+xml;base64,' + svg;
	});
}

/** Hand the picture to the user under this name. */
function save(name: string, mime: string, data: string): void {
	const url = URL.createObjectURL(new Blob([bytes(data)], {type: mime}));
	const link = window.document.createElement('a');
	link.href = url;
	link.download = name;
	window.document.body.appendChild(link);
	link.click();
	link.remove();
	//-- The url has to still be there while the click is being handled, which is the next tick.
	setTimeout(() => URL.revokeObjectURL(url), 0);
}

function mimeOf(format: string): string {
	return 'png' === format ? 'image/png' : 'image/svg+xml';
}

function base64(data: Uint8Array): string {
	let text = '';
	for(const byte of data) {
		text += String.fromCharCode(byte);
	}
	return btoa(text);
}

function bytes(data: string): Uint8Array<ArrayBuffer> {
	const text = atob(data);
	const out = new Uint8Array(new ArrayBuffer(text.length));
	for(let i = 0; i < text.length; i++) {
		out[i] = text.charCodeAt(i);
	}
	return out;
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
	if(instance.applying || undefined === edit) {
		return;
	}
	//-- A read-only drawing reports nothing the user did, because the user can do nothing to
	//-- it - but a layout the server asked for is not the user, and the model has to hear
	//-- where it put things.
	if(!instance.editable && !instance.layingOut) {
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
