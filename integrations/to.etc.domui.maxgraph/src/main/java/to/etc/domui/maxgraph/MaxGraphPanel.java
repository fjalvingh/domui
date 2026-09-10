package to.etc.domui.maxgraph;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.header.HeaderContributor;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Page;
import to.etc.domui.maxgraph.model.GraphCell;
import to.etc.domui.maxgraph.model.GraphChange;
import to.etc.domui.maxgraph.model.GraphEdge;
import to.etc.domui.maxgraph.model.GraphLayoutDirection;
import to.etc.domui.maxgraph.model.GraphLayoutType;
import to.etc.domui.maxgraph.model.GraphModel;
import to.etc.domui.maxgraph.model.GraphNode;
import to.etc.domui.maxgraph.model.GraphOp;
import to.etc.domui.maxgraph.model.GraphOpType;
import to.etc.domui.maxgraph.model.GraphPaletteItem;
import to.etc.domui.maxgraph.model.IGraphChangeHandler;
import to.etc.domui.maxgraph.model.IGraphCreateHandler;
import to.etc.domui.maxgraph.model.IGraphModelListener;
import to.etc.domui.parts.IComponentJsonProvider;
import to.etc.domui.server.RequestContextImpl;
import to.etc.domui.server.StringBufferDataFactory;
import to.etc.domui.state.IPageParameters;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.javascript.JavascriptStmt;
import to.etc.domui.util.javascript.JsonBuilder;
import to.etc.util.StringTool;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A diagram, drawn in the browser by <a href="https://github.com/maxGraph/maxGraph">maxGraph</a>
 * from a {@link GraphModel} built in Java.
 *
 * <pre>
 * MaxGraphPanel.initialize(this);
 *
 * GraphModel model = new GraphModel();
 * GraphNode start = model.addNode("Start", 40, 20, 120, 40)
 *     .styled(s -&gt; s.shape(GraphShape.Ellipse));
 * GraphNode check = model.addNode("Check the order", 40, 120, 120, 40);
 * model.addEdge(start, check);
 *
 * MaxGraphPanel panel = new MaxGraphPanel();
 * add(panel);
 * panel.size("100%", "400px").setModel(model);
 * </pre>
 *
 * <p>The panel renders one empty div and leaves everything inside it to maxGraph. The
 * drawing itself is not in the page's HTML: the browser asks for the model as JSON once
 * the component is there, and builds it. That also means the drawing must be rebuildable
 * at any time - a {@link #forceRebuild()} of the panel or of anything above it throws the
 * browser-side instance away, and the create call that comes with the next render asks
 * for the model again.</p>
 *
 * <p>Changing the model afterwards does not redraw anything: the panel listens to the
 * model, and what changed during a request is sent at the end of it as a list of changes
 * that the browser applies to the drawing it already has. So a page moves a node by
 * moving it in the model, and nothing else.</p>
 *
 * <p>A drawing is read-only unless {@link #setEditable(boolean)} says otherwise. In an
 * editable one the user can move, resize, rename and delete cells and bend edges; every
 * such change is sent to the server, offered to the page's {@link IGraphChangeHandler},
 * and - unless the handler refuses it - made to the model. A refused change is put back,
 * because the model then sends the browser what the cell really is.</p>
 *
 * <p>Adding to a drawing goes the other way round. The browser never makes a cell, because
 * a cell it invented would have no id the server knows it by; it says what the user did -
 * dropped a {@link GraphPaletteItem}, drew a connection - and the page's
 * {@link IGraphCreateHandler} decides what the model becomes. What it makes then arrives
 * in the browser like any other change.</p>
 *
 * <p>Undo is the model's, and is off until {@link GraphModel#setUndoEnabled(boolean)} says
 * otherwise. Where it is on, ctrl-Z and ctrl-Y in the drawing ask the model to take a step
 * back or forward, exactly as a button calling {@link GraphModel#undo()} would; what that
 * changes arrives in the browser as an ordinary list of changes. The browser keeps no
 * history of its own - it could not put back a cell it deleted, because the object of that
 * cell only exists here.</p>
 *
 * <p>{@link #layout(GraphLayoutType)} arranges the drawing: the browser does the arranging,
 * because that is where the drawing is, and where it puts things comes back as ordinary
 * changes - so the model ends up holding the arranged drawing, and a later render draws it
 * that way.</p>
 *
 * <p>A picture of the drawing is made by the browser, because that is where the drawing
 * is: {@link #download(GraphExportFormat, String)} hands it straight to the user, and
 * {@link #export(GraphExportFormat, IGraphExportHandler)} posts it back here for the
 * application to keep.</p>
 *
 * <p>A page that uses this must call {@link #initialize(NodeContainer)} once, which adds
 * the library to that page only.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class MaxGraphPanel extends Div implements IComponentJsonProvider {
	/** Where the maxGraph resources sit in this module's META-INF/resources. */
	static private final String RESOURCE_ROOT = "js/maxgraph";

	private GraphModel m_model = new GraphModel();

	private boolean m_panning = true;

	private boolean m_editable;

	@Nullable
	private IGraphChangeHandler m_changeHandler;

	@Nullable
	private IGraphCreateHandler m_createHandler;

	private final List<GraphPaletteItem> m_paletteList = new ArrayList<>();

	/** What changed since the browser was last told, in the order it changed. */
	private final List<GraphOp> m_pendingOps = new ArrayList<>();

	/** The model version the browser's drawing is at, as far as this panel knows. */
	private int m_sentVersion;

	private boolean m_listening;

	/**
	 * Set when the model's version moved on without the browser being sent anything - which
	 * is what happens when the browser's own changes are applied here. The browser has to
	 * be told the new version even when there is nothing to correct, or its next change list
	 * will be about a version that no longer exists.
	 */
	private boolean m_versionMoved;

	/** A layout the page asked for and the browser has not been told about yet. */
	@Nullable
	private GraphLayoutType m_pendingLayout;

	private GraphLayoutDirection m_pendingLayoutDirection = GraphLayoutDirection.North;

	/** Pictures the page asked for that the browser has not been told to make yet. */
	private final List<ExportCommand> m_exportList = new ArrayList<>();

	/**
	 * The handlers of the pictures that have been asked for but have not arrived, by the
	 * token they will come back under. A picture takes a request of its own to make, so
	 * more than one can be on its way at a time.
	 */
	private final Map<String, IGraphExportHandler> m_exportHandlerMap = new HashMap<>();

	private int m_exportTokenCounter;

	private final IGraphModelListener m_modelListener = (model, op) -> opped(op);

	public MaxGraphPanel() {
		setCssClass("ui-mxgr");
	}

	/**
	 * Add the maxGraph library to the page this node is on. Header contributors are per
	 * page, so only the pages that show a diagram load the bundle.
	 */
	static public void initialize(NodeContainer what) {
		Page page = what.getPage();
		page.addHeaderContributor(HeaderContributor.loadStylesheet("$" + RESOURCE_ROOT + "/css/common.css"), 10);
		page.addHeaderContributor(HeaderContributor.loadStylesheet("$" + RESOURCE_ROOT + "/domui-maxgraph.css"), 11);
		page.addHeaderContributor(HeaderContributor.loadJavascript("$" + RESOURCE_ROOT + "/domui-maxgraph.js"), 12);
	}

	/**
	 * The create call carries only what is needed to construct the widget: it runs again
	 * on every full page render, so it may not contain any of the model's state.
	 */
	@Override
	public void createContent() throws Exception {
		//-- Everything before now is in the model the browser is about to ask for.
		m_pendingOps.clear();
		m_sentVersion = m_model.getVersion();

		StringBuilder sb = new StringBuilder();
		sb.append("DomUIMaxGraph.create('").append(getActualID()).append("', {imageBase:'")
			.append(DomUtil.getRelativeApplicationResourceURL(RESOURCE_ROOT + "/images"))
			.append("'})");
		appendCreateJS(sb);
	}

	@Override
	public void onAddedToPage(Page p) {
		super.onAddedToPage(p);
		startListening();
	}

	@Override
	public void onRemoveFromPage(Page p) {
		super.onRemoveFromPage(p);
		stopListening();
		appendJavascript("DomUIMaxGraph.destroy('" + getActualID() + "');");
	}

	/**
	 * Called out of band by the browser side, once per created widget, to get the drawing.
	 */
	@NonNull
	@Override
	public Object provideJsonData(@NonNull IPageParameters parameterSource) throws Exception {
		StringBufferDataFactory sb = new StringBufferDataFactory("application/json");
		try(JsonBuilder b = new JsonBuilder(sb)) {
			//-- Connections can only be drawn where there is something to make an edge with.
			new GraphJsonRenderer().render(b, m_model, m_panning, m_editable, null != m_createHandler, m_paletteList);
		}
		//-- Whatever the browser had, it now has this.
		m_pendingOps.clear();
		m_sentVersion = m_model.getVersion();
		return sb;
	}

	/**
	 * Send what changed in the model during this request to the drawing that is already
	 * on the screen, so that it changes instead of being rebuilt.
	 */
	@Override
	protected void renderJavascriptDelta(@NonNull JavascriptStmt b) throws Exception {
		if(!m_pendingOps.isEmpty() || m_versionMoved) {
			m_versionMoved = false;
			StringBuilder json = new StringBuilder();
			try(JsonBuilder jb = new JsonBuilder(json)) {
				new GraphJsonRenderer().renderOps(jb, m_sentVersion, m_model, m_pendingOps);
			}
			m_pendingOps.clear();
			m_sentVersion = m_model.getVersion();

			b.append("DomUIMaxGraph.apply('").append(getActualID()).append("',").append(json.toString()).append(")");
		}
		//-- After the changes, so that a layout asked for in the same request arranges what
		//-- that request added, and a picture of it is a picture of the arranged drawing.
		renderLayout(b);
		renderExports(b);
	}

	/**
	 * A full render creates the widget again, which asks for the model again - so a layout
	 * or a picture that was asked for in the same request has to be emitted here as well,
	 * and the browser does it once the drawing it is about is there.
	 */
	@Override
	protected void renderJavascriptState(@NonNull JavascriptStmt b) throws Exception {
		renderLayout(b);
		renderExports(b);
	}

	private void renderLayout(@NonNull JavascriptStmt b) {
		GraphLayoutType layout = m_pendingLayout;
		if(null == layout) {
			return;
		}
		m_pendingLayout = null;
		b.next();
		b.append("DomUIMaxGraph.layout('").append(getActualID()).append("',{layout:'")
			.append(layout.getName()).append("',direction:'")
			.append(m_pendingLayoutDirection.getName()).append("'})");
	}

	/**
	 * Ask the browser for the pictures the page wants. One that is to be saved says where;
	 * one that is to come here carries the token its answer will name.
	 */
	private void renderExports(@NonNull JavascriptStmt b) {
		for(ExportCommand command : m_exportList) {
			b.next();
			String fileName = command.fileName();
			if(null == fileName) {
				b.append("DomUIMaxGraph.exportImage('").append(getActualID()).append("',{token:'")
					.append(command.token()).append("',format:'").append(command.format().getName())
					.append("',scale:").append(Double.toString(command.scale())).append("})");
			} else {
				b.append("DomUIMaxGraph.download('").append(getActualID()).append("',{format:'")
					.append(command.format().getName()).append("',scale:")
					.append(Double.toString(command.scale())).append(",name:")
					.append(StringTool.strToJavascriptString(fileName, true)).append("})");
			}
		}
		m_exportList.clear();
	}

	/**
	 * The picture the browser was asked for. It arrives in a request of its own, because
	 * making it takes as long as it takes to draw the thing; the token says which of the
	 * pictures that were asked for this is.
	 *
	 * <p>This is an ordinary page action, so the handler may change the page - which is how
	 * a picture that came here is shown, or handed to the user as a download.</p>
	 */
	public void webActionGRAPHEXPORT(@NonNull RequestContextImpl ctx) throws Exception {
		String json = ctx.getPageParameters().getString("json", null);
		if(null == json) {
			throw new IllegalStateException("The graph export request has no json parameter");
		}
		JsonNode root = new ObjectMapper().readTree(json);
		IGraphExportHandler handler = m_exportHandlerMap.remove(root.path("token").asText());
		if(null == handler) {
			return;                                        // Asked for by a panel that is gone, or answered twice.
		}
		GraphExportFormat format = formatOf(root.path("format").asText());
		byte[] data = Base64.getDecoder().decode(root.path("data").asText());
		handler.exported(new GraphExport(format, data, root.path("width").asInt(), root.path("height").asInt()));
	}

	static private GraphExportFormat formatOf(String name) {
		for(GraphExportFormat format : GraphExportFormat.values()) {
			if(format.getName().equals(name)) {
				return format;
			}
		}
		throw new IllegalStateException("Unknown export format '" + name + "' from the browser");
	}

	/** One picture the page asked for: to be saved where the user wants it, or to come here. */
	private record ExportCommand(GraphExportFormat format, double scale, @Nullable String token, @Nullable String fileName) {}

	/*----------------------------------------------------------------------*/
	/*	CODING:	What the browser changed							        */
	/*----------------------------------------------------------------------*/

	/**
	 * The user changed the drawing. The changes arrive as the same operations that go the
	 * other way, and are handled one by one: the page's handler is asked about each, and
	 * what it accepts is made to the model.
	 *
	 * <p>This rides a normal page action, so the answer is the ordinary page delta - a
	 * handler is free to change anything else on the page while it is at it.</p>
	 */
	public void webActionGRAPHCHANGE(@NonNull RequestContextImpl ctx) throws Exception {
		String json = ctx.getPageParameters().getString("json", null);
		if(null == json) {
			throw new IllegalStateException("The graph change request has no json parameter");
		}
		GraphChangeSet set = new GraphChangeParser().parse(m_model, json);

		//-- Whatever else happens, the browser is told where the model ended up.
		m_versionMoved = true;
		changedJavascriptState();

		if(set.getBase() != m_sentVersion) {
			//-- Made against a drawing that no longer exists. Dropping it leaves the version
			//-- mismatched, which is exactly what makes the browser ask for the model again.
			return;
		}

		//-- Everything is decided before anything is done, so that every handler call sees the
		//-- model the user was looking at.
		Set<GraphChange> refusedSet = Collections.newSetFromMap(new IdentityHashMap<>());
		decide(set, refusedSet);

		//-- One gesture is often several changes - deleting a node deletes its edges with it -
		//-- so everything one request does is one step to undo.
		m_model.beginEdit();
		try {
			for(GraphChange change : set.getChanges()) {
				if(!refusedSet.contains(change)) {
					applyWithoutEcho(change);
				}
			}
			putBack(refusedSet);
			handle(set.getRequests());
		} finally {
			m_model.endEdit();
		}
	}

	/**
	 * Do what the user asked for that the browser could not do itself: make the cells it
	 * has no ids for, and move through the history it does not have.
	 *
	 * <p>Nothing is suppressed here. The browser has none of the cells a create handler
	 * makes, because it never makes one of its own, and an undo changes cells it was never
	 * told changed - so both go to it as ordinary changes, which is what the model's
	 * listener turns them into.</p>
	 */
	private void handle(List<GraphRequest> requestList) throws Exception {
		for(GraphRequest request : requestList) {
			switch(request.getType()) {
				default:
					break;

				case RequestUndo:
					m_model.undo();
					break;

				case RequestRedo:
					m_model.redo();
					break;

				case RequestNode:
				case RequestEdge:
					create(request);
					break;
			}
		}
	}

	/**
	 * Make the cell the user asked for - or rather, let the page make it: what a dropped
	 * palette item or a drawn connection becomes is the page's decision, and it may be
	 * nothing at all.
	 */
	private void create(GraphRequest request) throws Exception {
		IGraphCreateHandler handler = m_createHandler;
		if(null == handler) {
			return;
		}
		if(request.getType() == GraphOpType.RequestNode) {
			GraphPaletteItem item = paletteItem(request.getPaletteKey());
			if(null != item) {
				handler.createNode(m_model, item, request.getX(), request.getY());
			}
		} else {
			GraphNode source = request.getSource();
			GraphNode target = request.getTarget();
			if(null != source && null != target) {
				handler.createEdge(m_model, source, target);
			}
		}
	}

	/**
	 * Ask the handler about each change, and collect the ones it refuses.
	 *
	 * <p>Deleting a node deletes the edges that hang on it, because that is what the browser
	 * does with them, so those edges are one gesture with the node and follow its verdict:
	 * they are refused with it, and the handler is not asked about them at all. Which is why
	 * the node removals are asked about first.</p>
	 */
	private void decide(GraphChangeSet set, Set<GraphChange> refusedSet) throws Exception {
		IGraphChangeHandler handler = m_changeHandler;
		if(null == handler) {
			return;
		}
		Set<GraphChange> decidedSet = Collections.newSetFromMap(new IdentityHashMap<>());
		Set<GraphCell> keptSet = Collections.newSetFromMap(new IdentityHashMap<>());
		for(GraphChange change : set.getChanges()) {
			if(change.getType() == GraphOpType.Remove && change.getCell() instanceof GraphNode) {
				decidedSet.add(change);
				if(!handler.acceptChange(change)) {
					refusedSet.add(change);
					keptSet.add(change.getCell());
				}
			}
		}

		for(GraphChange change : set.getChanges()) {
			if(decidedSet.contains(change)) {
				continue;
			}
			if(change.getType() == GraphOpType.Remove && change.getCell() instanceof GraphEdge edge
				&& (keptSet.contains(edge.getSource()) || keptSet.contains(edge.getTarget()))) {
				refusedSet.add(change);                    // Goes with the node that stays.
			} else if(!handler.acceptChange(change)) {
				refusedSet.add(change);
			}
		}
	}

	/**
	 * Make an accepted change to the model without this panel hearing about it: the browser
	 * made it, so it already shows it, and sending it back would be telling it what it just
	 * told us. Any other panel on the same model is still listening, and is told.
	 */
	private void applyWithoutEcho(GraphChange change) {
		boolean listening = m_listening;
		stopListening();
		try {
			change.apply();
		} finally {
			if(listening) {
				startListening();
			}
		}
	}

	/**
	 * Put back what was refused, by sending the browser what the model really holds: the
	 * cell as it is here replaces whatever the user made of it there.
	 *
	 * <p>Nodes go first. A refused removal is sent as the cell itself, and an edge can only
	 * be made once both of its ends are there again.</p>
	 */
	private void putBack(Set<GraphChange> refusedSet) {
		for(GraphChange change : refusedSet) {
			if(change.getCell() instanceof GraphNode) {
				resend(change);
			}
		}
		for(GraphChange change : refusedSet) {
			if(!(change.getCell() instanceof GraphNode)) {
				resend(change);
			}
		}
	}

	private void resend(GraphChange change) {
		GraphCell cell = change.getCell();
		if(m_model.getCell(cell.getId()) != cell) {
			return;                                        // Refused, but gone anyway.
		}
		m_model.resend(undoOf(change));
	}

	/**
	 * What has to be sent to undo a change that was never made: for a removal the cell
	 * itself, for anything else the property the change was about, as the model has it.
	 */
	private GraphOp undoOf(GraphChange change) {
		GraphCell cell = change.getCell();
		switch(change.getType()) {
			default:
			case Remove:
				return GraphOp.add(cell);

			case Label:
				return GraphOp.label(cell);

			case Style:
				return GraphOp.style(cell);

			case Geometry:
				return GraphOp.geometry((GraphNode) cell);

			case Terminal:
				return GraphOp.terminal((GraphEdge) cell);

			case Points:
				return GraphOp.points((GraphEdge) cell);
		}
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Listening to the model								        */
	/*----------------------------------------------------------------------*/

	private void startListening() {
		if(!m_listening) {
			m_model.addChangeListener(m_modelListener);
			m_listening = true;
		}
	}

	private void stopListening() {
		if(m_listening) {
			m_model.removeChangeListener(m_modelListener);
			m_listening = false;
		}
	}

	/**
	 * One change to the model. A change that is already on the list is not added again:
	 * what is sent for it is read from the cell when the list is rendered, so recording it
	 * once is recording the last value.
	 */
	private void opped(GraphOp op) {
		if(!m_pendingOps.contains(op)) {
			m_pendingOps.add(op);
		}
		changedJavascriptState();
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Properties											        */
	/*----------------------------------------------------------------------*/

	public GraphModel getModel() {
		return m_model;
	}

	/**
	 * Show this model. The drawing is rebuilt from it: another model has other cells, so
	 * there is nothing to change one into the other with.
	 */
	public MaxGraphPanel setModel(GraphModel model) {
		if(m_model == model) {
			return this;
		}
		boolean listening = m_listening;
		stopListening();
		m_model = model;
		m_pendingOps.clear();
		if(listening) {
			startListening();
		}
		forceRebuild();
		return this;
	}

	/**
	 * Arrange the drawing, now: the browser lays it out and tells the model where everything
	 * ended up, so the drawing stays arranged when it is built again.
	 *
	 * <p>This is one command, not a property - what a button does, not something a drawing
	 * is. A drawing that is arranged on every render would put a node the user dragged back
	 * where the layout wants it.</p>
	 *
	 * <p>What the layout moves arrives here as ordinary changes, which means a
	 * {@link #setChangeHandler(IGraphChangeHandler) change handler} is asked about them like
	 * any other change - and, where the model keeps a history, one arrangement is one thing
	 * to undo. It works on a read-only drawing too: the user cannot move anything there, but
	 * the page can.</p>
	 */
	public MaxGraphPanel layout(GraphLayoutType type) {
		return layout(type, GraphLayoutDirection.North);
	}

	/**
	 * Arrange the drawing so that it grows this way. Only {@link GraphLayoutType#Hierarchical}
	 * and the tree layouts have a direction; the others ignore it.
	 */
	public MaxGraphPanel layout(GraphLayoutType type, GraphLayoutDirection direction) {
		m_pendingLayout = type;
		m_pendingLayoutDirection = direction;
		changedJavascriptState();
		return this;
	}

	/**
	 * Make a picture of the drawing and bring it here: the browser draws it and posts it
	 * back, and the handler is called with it a moment later, in a request of its own.
	 *
	 * <p>This is what an application asks for when it needs the picture itself - to put in
	 * a report, to mail, to keep. A picture the <i>user</i> is to keep does not need the
	 * detour: {@link #download(GraphExportFormat, String)} saves it without it ever coming
	 * here.</p>
	 *
	 * <p>The picture is made in the browser because that is the only place the drawing
	 * exists: the model says what is drawn, but where an edge runs and how wide a label is
	 * are maxGraph's answers, not the model's. What is made is the whole drawing, whatever
	 * part of it happens to be scrolled into view.</p>
	 *
	 * <p>It arrives over the ordinary page POST, which containers limit the size of -
	 * Tomcat allows two megabytes by default. A drawing large enough to make a picture
	 * bigger than that is one to save in the browser instead.</p>
	 */
	public MaxGraphPanel export(GraphExportFormat format, IGraphExportHandler handler) {
		return export(format, 1.0, handler);
	}

	/**
	 * The same picture, drawn this many times its own size. Only {@link GraphExportFormat#Png}
	 * gains anything by it: it decides how many pixels the picture has, which is what a
	 * printed one wants more of.
	 */
	public MaxGraphPanel export(GraphExportFormat format, double scale, IGraphExportHandler handler) {
		String token = "e" + (++m_exportTokenCounter);
		m_exportHandlerMap.put(token, handler);
		m_exportList.add(new ExportCommand(format, scale, token, null));
		changedJavascriptState();
		return this;
	}

	/**
	 * Save a picture of the drawing under this name, on the machine the browser runs on.
	 * Nothing comes here: the browser makes the picture and hands it to the user, which is
	 * all a "save this drawing" button needs and costs no traffic at all.
	 */
	public MaxGraphPanel download(GraphExportFormat format, String fileName) {
		return download(format, 1.0, fileName);
	}

	/** The same, drawn this many times its own size. */
	public MaxGraphPanel download(GraphExportFormat format, double scale, String fileName) {
		m_exportList.add(new ExportCommand(format, scale, null, fileName));
		changedJavascriptState();
		return this;
	}

	public boolean isEditable() {
		return m_editable;
	}

	/**
	 * Whether the user can change the drawing: move and resize nodes, and delete what is
	 * selected. Off by default - a drawing that explains something is not something to
	 * edit.
	 */
	public MaxGraphPanel setEditable(boolean editable) {
		if(m_editable == editable) {
			return this;
		}
		m_editable = editable;
		forceRebuild();
		return this;
	}

	/**
	 * What the user can drag into the drawing. An item does nothing without a
	 * {@link #setCreateHandler(IGraphCreateHandler) create handler} to say what it becomes.
	 */
	public MaxGraphPanel addPaletteItem(GraphPaletteItem item) {
		m_paletteList.add(item);
		forceRebuild();
		return this;
	}

	public List<GraphPaletteItem> getPaletteItems() {
		return Collections.unmodifiableList(m_paletteList);
	}

	@Nullable
	public GraphPaletteItem paletteItem(@Nullable String key) {
		for(GraphPaletteItem item : m_paletteList) {
			if(item.getKey().equals(key)) {
				return item;
			}
		}
		return null;
	}

	@Nullable
	public IGraphCreateHandler getCreateHandler() {
		return m_createHandler;
	}

	/**
	 * Makes the cells the user asks for. Without one nothing can be added to the drawing:
	 * dropping a palette item and drawing a connection both do nothing.
	 */
	public MaxGraphPanel setCreateHandler(@Nullable IGraphCreateHandler createHandler) {
		if(m_createHandler == createHandler) {
			return this;
		}
		m_createHandler = createHandler;
		forceRebuild();                                    // Whether connections can be drawn changed with it.
		return this;
	}

	@Nullable
	public IGraphChangeHandler getChangeHandler() {
		return m_changeHandler;
	}

	/**
	 * Asked about every change the user makes, before it is made to the model. Without one
	 * every change is accepted.
	 */
	public MaxGraphPanel setChangeHandler(@Nullable IGraphChangeHandler changeHandler) {
		m_changeHandler = changeHandler;
		return this;
	}

	public boolean isPanning() {
		return m_panning;
	}

	/**
	 * Whether the drawing can be moved by dragging its background. On by default.
	 */
	public MaxGraphPanel setPanning(boolean panning) {
		if(m_panning == panning) {
			return this;
		}
		m_panning = panning;
		forceRebuild();
		return this;
	}

	/**
	 * Set both dimensions of the drawing area. maxGraph draws into a container that has
	 * a size of its own; without one there is nothing to see.
	 */
	public MaxGraphPanel size(String width, String height) {
		setWidth(width);
		setHeight(height);
		return this;
	}
}
