/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.dom.html;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.component.binding.OldBindingHandler;
import to.etc.domui.component.layout.FloatingDiv;
import to.etc.domui.component.misc.WindowParameters;
import to.etc.domui.dom.errors.IErrorFence;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.header.HeaderContributor;
import to.etc.domui.dom.header.HeaderContributorEntry;
import to.etc.domui.server.DomApplication;
import to.etc.domui.state.ConversationContext;
import to.etc.domui.state.IPageParameters;
import to.etc.domui.state.PageParameters;
import to.etc.domui.state.SubConversationContext;
import to.etc.domui.state.UIContext;
import to.etc.domui.state.UIGotoContext;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.javascript.JavascriptStmt;
import to.etc.domui.util.resources.IResourceRef;
import to.etc.function.IExecute;
import to.etc.util.WrappedException;
import to.etc.webapp.core.IRunnable;
import to.etc.webapp.nls.NlsContext;
import to.etc.webapp.query.IQContextContainer;
import to.etc.webapp.query.QContextContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This is the main owner of all nodes; this represents all that is needed for a
 * page to render. All nodes that are (indirectly) attached to the page directly
 * connect here. The page maintains a full ident map to all components currently
 * reachable on the page. In addition the page assigns IDs to nodes that have no
 * ID (or a duplicate ID).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 18, 2007
 */
@NonNullByDefault
final public class Page implements IQContextContainer {
	static private final Logger LOG = LoggerFactory.getLogger(Page.class);

	static private final int MAX_DOMUI_NODES_PER_PAGE = 100_000;

	/** Next ID# for unidded nodes. */
	private int m_nextID = 1;

	/** The unique, random page ID generated to check for session expired problems */
	private final int m_pageTag;

	/** Temp work buffer to prevent lots of allocations. */
	@Nullable
	private StringBuilder m_sb;

	/**
	 * The set of parameters that was used at page creation time.
	 */
	@Nullable
	private IPageParameters m_pageParameters;

	@Nullable
	private ConversationContext m_cc;

	//	private boolean					m_built;

	@NonNull
	private final Map<String, NodeBase> m_nodeMap = new HashMap<String, NodeBase>(127);

	@Nullable
	private Map<String, NodeBase> m_beforeMap;

	/**
	 * Contains the header contributors in the order that they were added.
	 */
	@NonNull
	private List<HeaderContributorEntry> m_orderedContributorList = Collections.EMPTY_LIST;

	/**
	 * As soon as header contributor are rendered to the browser this gets set to the
	 * length of the list rendered out. When new contributors are added by components
	 * this gets seen because the list is bigger than this index; we need to render
	 * from this index till end-of-list to include the contributors.
	 */
	private int m_lastContributorIndex;

	/**
	 * Set containing the same header contributors, but in a fast-lookup format.
	 */
	@Nullable
	private Set<HeaderContributor> m_headerContributorSet;

	@Nullable
	private StringBuilder m_appendJS;

	/** Temp for checking shelve order. */
	private boolean m_shelved;

	@Nullable
	private NodeBase m_defaultFocusSource;

	/** When a (sub)tree validation has started this holds the validation's start point, so that the validation can be repeated. */
	@Nullable
	private NodeBase m_validationSource;

	/** When a (sub)tree validation has started this holds the action to run at the end of successful validation. */
	@Nullable
	private IRunnable m_validationAction;

	/** Number of exceptions in-a-row on a full render of this page. */
	private int m_pageExceptionCount;

	/** Set to T if an initial full render of the page completed OK. */
	private boolean m_fullRenderCompleted;

	@NonNull
	private final UrlPage m_rootContent;

	/** The component that needs to be focused. This is null if no explicit focus request was done. */
	@Nullable
	private NodeBase m_focusComponent;

	/**
	 * If a "pop-in" is present this contains the reference to it.
	 */
	@Nullable
	private NodeContainer m_currentPopIn;

	private Map<String, Object> m_pageData = Collections.EMPTY_MAP;

	private boolean m_allowVectorGraphics;

	/**
	 * Contains all nodes that were added or marked as forceRebuild <i>during</i> the BUILD
	 * phase. If this set is non-empty after a build loop then the loop needs to repeat the
	 * build for the nodes and their subtrees in here. See bug 688.
	 */
	private Set<NodeBase> m_pendingBuildSet = new HashSet<NodeBase>();

	///**
	// * All subpages that were added to the tree this request.
	// */
	//private Set<SubPage> m_addedSubPages = new HashSet<>();
	//
	///**
	// * All SubPages currently present on the page.
	// */
	//private Set<SubPage> m_subPageSet = new HashSet<>();

	/**
	 * All subpages that have been deleted during this request.
	 */
	private Set<SubPage> m_removedSubPages = new HashSet<>();

	/**
	 * When calling user handlers on nodes this will keep track of the node the handler was
	 * called on. If that node becomes somehow removed it will move upward to it's parent,
	 * so that it points to an on-screen node always. This is needed for error handling.
	 */
	@Nullable
	private NodeBase m_theCurrentNode;

	/**
	 * When set, the page will be rendered as XHTML. This is experimental, and used to implement
	 * the SVG/VML graphic flow editor.
	 */
	private boolean m_renderAsXHTML;

	/**
	 * EXPERIMENTAL Render the page through a HTML template.
	 */
	@Nullable
	private IResourceRef m_renderTemplate;

	/**
	 * If the page has gotten it's values injected this is set to true. This prevents injecting
	 * a value twice which causes trouble for NEW objects (it creates two separate instances of
	 * a new object).
	 */
	private boolean m_injected;

	/**
	 * The stack of floating windows on top of the main canvas, in ZIndex order.
	 */
	@Nullable
	private List<FloatingDiv> m_floatingWindowStack;

	/**
	 * This gets incremented for every request that is handled.
	 */
	private int m_requestCounter;

	/** The current handler phase in handling requests. */
	@NonNull
	private PagePhase m_phase = PagePhase.NULL;

	/** The last node that was clicked, for doubleclick detection */
	@Nullable
	private NodeBase m_lastClickTarget;

	private long m_lastClickTime;

	private boolean m_allowTooManyNodes;

	/**
	 * Nodes that are added to a render and that are removed by the Javascript framework are added here; this
	 * will force them to be removed from the tree after any render without causing a delta.
	 */
	@NonNull
	private List<NodeBase> m_removeAfterRenderList = Collections.EMPTY_LIST;

	@NonNull
	private List<IExecute> m_afterRequestListenerList = Collections.EMPTY_LIST;

	@NonNull
	private List<IExecute> m_beforeRequestListenerList = Collections.EMPTY_LIST;

	@NonNull
	private List<IExecute> m_destroyListenerList = new CopyOnWriteArrayList<>();

	@NonNull
	private List<IExecute> m_afterRenderList = Collections.EMPTY_LIST;

	@Deprecated
	private List<Object> m_pageMessageList = new ArrayList<>();

	private List<IExecute> m_pageOnCallbackList = new ArrayList<>();

	///**
	// * Contains all http headers that need to be sent for this page. When the Page
	// * is created this is filled with the headers set in {@link DomApplication#getDefaultHTTPHeaderMap()},
	// * and it can after be manipulated by a page before being used.
	// */
	//private Map<String, String> m_HTTPHeaderMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	public Page(@NonNull final UrlPage pageContent) throws Exception {
		m_pageTag = DomApplication.internalNextPageTag(); // Unique page ID.
		m_rootContent = pageContent;
		registerNode(pageContent); // First node.
		pageContent.internalSetTag("body"); // Override it's tagname
		pageContent.setErrorFence(); // The body ALWAYS accepts ANY errors.

		//-- Localize calendar resources
		DomApplication app = DomApplication.get();
		String res = app.findLocalizedResourceName("$js/calendarnls", ".js", NlsContext.getLocale());
		if(res == null)
			throw new IllegalStateException("internal: missing calendar NLS resource $js/calendarnls{nls}.js");
		addHeaderContributor(HeaderContributor.loadJavascript(res), -760);

		//-- Localize DomUI resources
		res = app.findLocalizedResourceName("$js/domuinls", ".js", NlsContext.getLocale());
		if(res == null)
			throw new IllegalStateException("internal: missing domui NLS resource $js/domuinls{nls}.js");
		addHeaderContributor(HeaderContributor.loadJavascript(res), -760);
		//m_HTTPHeaderMap.putAll(app.applyPageHeaderTransformations(pageContent.getClass().getName(), app.getDefaultHTTPHeaderMap()));
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Phase handling (debug internals)					*/
	/*--------------------------------------------------------------*/

	public void internalSetPhase(@NonNull PagePhase phase) {
		m_phase = phase;
	}

	public void inNull() {
		if(m_phase == PagePhase.NULL)
			return;
		throw new IllegalStateException("DomUI: not allowed in state " + m_phase);
	}

	public void inRender() {
		if(m_phase == PagePhase.FULLRENDER || m_phase == PagePhase.DELTARENDER)
			return;
		throw new IllegalStateException("DomUI: not allowed in state " + m_phase);
	}

	public void inBuild() {
		if(m_phase == PagePhase.BUILD)
			return;
		throw new IllegalStateException("DomUI: not allowed in state " + m_phase);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Initialization.										*/
	/*--------------------------------------------------------------*/

	/**
	 * Assign required data to the page.
	 */
	final public void internalInitialize(@NonNull IPageParameters pp, @NonNull final ConversationContext cc) {
		if(pp == null)
			throw new IllegalStateException("Internal: Page parameters cannot be null here");
		if(cc == null)
			throw new IllegalStateException("Internal: ConversationContext cannot be null here");

		m_cc = cc;
		if(!pp.isReadOnly()) {
			if(pp instanceof PageParameters) {
				((PageParameters) pp).setReadOnly();
			} else {
				PageParameters rpp = pp.getUnlockedCopy();
				rpp.setReadOnly();
				pp = rpp;
			}
		}

		m_pageParameters = pp;
	}

	public final void internalOnDestroy() throws Exception {
		for(IExecute listener : m_destroyListenerList) {
			try {
				listener.execute();
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
		m_asyncLink.m_page = null;
		UrlPage body = getBody();
		body.internalOnDestroy();
	}

	public void setTheCurrentNode(@Nullable NodeBase b) {
		if(b != null && b.getPage() != this)
			throw new IllegalStateException("The node is not part of this page!");
		m_theCurrentNode = b;
	}

	@Nullable
	public NodeBase getTheCurrentNode() {
		return m_theCurrentNode;
	}

	/**
	 * This tries to locate the control that the "theCurrentNode" is associated with. If no control
	 * can be found it returns the node verbatim.
	 */
	@Nullable
	public NodeBase getTheCurrentControl() {
		//-- Locate the best encapsulating control if possible.
		NodeBase nb = getTheCurrentNode();
		while(nb != null && !(nb instanceof IControl<?>) && nb.hasParent()) {
			nb = nb.getParent();
		}
		return nb != null ? nb : getTheCurrentNode();
	}

	@NonNull
	public Map<String, NodeBase> internalNodeMap() {
		return m_nodeMap;
	}

	@NonNull
	private StringBuilder sb() {
		StringBuilder sb = m_sb;
		if(sb == null)
			sb = m_sb = new StringBuilder(64);
		else
			sb.setLength(0);
		return sb;
	}

	@NonNull
	public DomApplication getApplication() {
		return DomApplication.get();
	}

	/**
	 * Return the <b>readonly</b> copy of the parameters for this page.
	 */
	@NonNull
	public IPageParameters getPageParameters() {
		if(null != m_pageParameters)
			return m_pageParameters;
		throw new IllegalStateException("The page is not initialized??");
	}

	public int getRequestCounter() {
		return m_requestCounter;
	}

	public void internalIncrementRequestCounter() {
		m_requestCounter++;
	}

	public final int getPageTag() {
		return m_pageTag;
	}

	/**
	 * Calculates a new ID for a node.
	 */
	@NonNull
	final String nextID() {
		StringBuilder sb = sb();
		sb.append("_");
		int id = m_nextID++;
		while(id != 0) {
			int d = id % 36;
			if(d <= 9)
				d = d + '0';
			else
				d = ('A' + (d - 10));
			sb.append((char) d);
			id = id / 36;
		}
		return sb.toString();
	}

	/**
	 * Registers the node with this page. If the node has no ID or the ID is invalid then
	 * a new ID is assigned.
	 */
	final void registerNode(@NonNull final NodeBase n) {
		if(n.isAttached())
			throw new IllegalStateException("Node still attached to other page");

		/*
		 * jal 20081211 If a node already has an ID reuse that if not currently assigned. This should fix
		 * the following bug in drag and drop: a dropped node is REMOVED, then ADDED to another node (2 ops).
		 * This would reassign an ID, causing the delta to be rendered with a delete of the NEW ID instead
		 * of the old ID.
		 */
		String id = n.internalGetID();
		if(id != null) {
			if(m_nodeMap.containsKey(id)) {            // Duplicate key?
				id = nextID();                            // Assign new ID
				n.setActualID(id);                        // Save in node.
			}
		} else {
			//-- Assign new ID
			id = nextID();
			n.setActualID(id);
		}
		if(null != m_nodeMap.put(id, n))
			throw new IllegalStateException("Duplicate node ID '" + id + "'!?!?");
		if(! m_allowTooManyNodes && m_nodeMap.size() > MAX_DOMUI_NODES_PER_PAGE)
			throw new IllegalStateException("The page you are using is too big (it creates too many DOM nodes). Ask the developer to fix this issue.");
		n.setPage(this);
		n.onHeaderContributors(this);                // Ask the node for it's header contributors.
		n.internalOnAddedToPage(this);
		if(n.isFocusRequested()) {
			setFocusComponent(n);
			n.clearFocusRequested();
		}
		internalAddPendingBuild(n);

		if(n instanceof SubPage) {
			SubPage sp = (SubPage) n;                    // This is not dumb at all, sigh.
			getConversation().addSubConversation(sp.getConversation());
			m_removedSubPages.remove(sp);                // If we removed it earlier- unremove it (keeping its conversation state)

			try {
				DomApplication.get().getSubPageInjector().inject(sp);
			} catch(Exception x) {
				throw WrappedException.wrap(x);
			}
		}

		//-- Fix for bug# 787: cannot locate error fence. Allow errors to be posted on disconnected nodes.
		UIMessage message = n.getMessage();
		if(message != null) {
			IErrorFence fence = DomUtil.getMessageFence(n);        // Get the fence that'll handle the message by looking UPWARDS in the tree
			fence.addMessage(message);
		}
	}

	/**
	 * Removes this node from the IDmap.
	 */
	final void unregisterNode(@NonNull final NodeBase n) {
		if(n.getPage() != this)
			throw new IllegalStateException("This node does not belong to this page!?");
		if(n.getActualID() == null)
			throw new IllegalStateException("This-node's actual ID has gone!?");
		if(m_theCurrentNode == n)
			m_theCurrentNode = n.getParent();
		n.internalOnRemoveFromPage(this);
		n.setPage(null);
		if(m_nodeMap.remove(n.getActualID()) == null)
			throw new IllegalStateException("The node with ID=" + n.getActualID() + " was not found!?");
		m_pendingBuildSet.remove(n);

		if(n instanceof SubPage) {
			SubPage sp = (SubPage) n;                    // Sigh
			m_removedSubPages.add(sp);
			//m_addedSubPages.remove(sp);					// If it was added before but removed again -> nothing happened...
		}
	}

	@Nullable
	public NodeBase findNodeByID(@NonNull final String id) {
		return m_nodeMap.get(id);
	}

	/*
	 * When components itself are changed at application-invocation time we
	 * have two different possibilities:
	 * 1. 	component attributes are changed. The change is detected by the renderer
	 * 		for the component which keeps the "previous" values of the properties. At
	 * 		tree render time only the changed attributes are sent as a change list.
	 * 2.	The tree structure changes because components are moved, added or deleted.
	 *
	 * This code handles case 2. To prevent us from always having to create a before
	 * image all calls that change the tree (removeComponent, addComponent) call
	 * a signal function here. Only when that function gets called (the 1st time) will
	 * a copy of the before-tree be made. This copy only encapsulates the structure;
	 * the components themselves are not copied.
	 * The existence of the before-structure will indicate that a full tree delta is
	 * to be done at response time.
	 */

	/**
	 * Called by all methods that change this tree. As soon as this gets called
	 * it checks to see if a before-image is present. If not then it gets created
	 * so that the structure before the changes is maintained.
	 */
	final protected void copyIdMap() {
		if(m_beforeMap != null)
			return;
		m_beforeMap = new HashMap<>(m_nodeMap);
	}

	@Nullable
	final public Map<String, NodeBase> getBeforeMap() {
		return m_beforeMap;
	}

	public void internalClearDeltaFully() {
		for(NodeBase nb : m_removeAfterRenderList) {
			nb.remove();
		}
		m_removeAfterRenderList.clear();

		getBody().internalClearDeltaFully();
		m_beforeMap = null;
		m_sb = null;
	}


	public void addRemoveAfterRenderNode(@NonNull NodeBase node) {
		if(m_removeAfterRenderList == Collections.EMPTY_LIST) {
			m_removeAfterRenderList = new ArrayList<>();
		}
		m_removeAfterRenderList.add(node);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Automatic Test ID management.						*/
	/*--------------------------------------------------------------*/

	private static class IntRef {
		public int m_value;
	}

	@NonNull
	private final Map<String, IntRef> m_testIdMap = new HashMap<String, Page.IntRef>();

	@NonNull
	public String allocateTestID(@NonNull String initial) {
		IntRef ir = m_testIdMap.get(initial);
		if(null == ir) {
			ir = new IntRef();
			m_testIdMap.put(initial, ir);
			return initial;
		}
		int v = ++ir.m_value;
		return initial + "_" + v;
	}

	public boolean isTestIDAllocated(@NonNull String id) {
		return m_testIdMap.get(id) != null;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Header contributors									*/
	/*--------------------------------------------------------------*/

	/**
	 * Call from within the onHeaderContributor call on a node to register any header
	 * contributors needed by a node.
	 */
	final public void addHeaderContributor(@NonNull final HeaderContributor hc, int order) {
		Set<HeaderContributor> set = m_headerContributorSet;
		List<HeaderContributorEntry> list = m_orderedContributorList;
		if(set == null || list == null) {
			m_headerContributorSet = set = new HashSet<>(30);
			list = m_orderedContributorList = new ArrayList<>(30);
		} else if(set.contains(hc))                            // Already registered?
			return;
		set.add(hc);
		list.add(new HeaderContributorEntry(hc, order));
	}

	public synchronized void internalAddContributors(@NonNull List<HeaderContributorEntry> full) {
		full.addAll(m_orderedContributorList);
	}

	@NonNull
	public List<HeaderContributorEntry> getHeaderContributorList() {
		return new ArrayList<>(m_orderedContributorList);
	}

	@NonNull
	public List<HeaderContributorEntry> getAddedContributors() {
		if(m_orderedContributorList == null || m_lastContributorIndex >= m_orderedContributorList.size())
			return Collections.EMPTY_LIST;
		return new ArrayList<>(m_orderedContributorList.subList(m_lastContributorIndex, m_orderedContributorList.size()));
	}

	public void internalContributorsRendered() {
		m_lastContributorIndex = m_orderedContributorList == null ? 0 : m_orderedContributorList.size();
	}

	/**
	 * Return the BODY component for this page.
	 */
	@NonNull
	public UrlPage getBody() {
		return m_rootContent;
	}

	public <T> void setData(@NonNull final T inst) {
		if(m_pageData == Collections.EMPTY_MAP)
			m_pageData = new HashMap<String, Object>();
		m_pageData.put(inst.getClass().getName(), inst);
	}

	@Nullable
	public <T> T getData(@NonNull final Class<T> clz) {
		return (T) m_pageData.get(clz.getName());
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Simple component binding.							*/
	/*--------------------------------------------------------------*/

	public void modelToControl() throws Exception {
		internalSetPhase(PagePhase.bindModelToControl);
		try {
			OldBindingHandler.modelToControl(getBody());
		} finally {
			internalSetPhase(PagePhase.NULL);
		}
	}

	public void controlToModel() throws Exception {
		internalSetPhase(PagePhase.bindControlToModel);
		try {
			OldBindingHandler.controlToModel(getBody());
		} finally {
			internalSetPhase(PagePhase.NULL);
		}
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	http protocol headers										*/
	/*----------------------------------------------------------------------*/

	//public void addHTTPHeader(@NonNull String header, @Nullable String value) {
	//	if(null == value)
	//		m_HTTPHeaderMap.remove(header);
	//	else
	//		m_HTTPHeaderMap.put(header, value);
	//}
	//
	//@NonNull
	//public Map<String, String> getHTTPHeaderMap() {
	//	return m_HTTPHeaderMap;
	//}

	/*--------------------------------------------------------------*/
	/*	CODING:	Handle the floating window stack.					*/
	/*--------------------------------------------------------------*/


	/**
	 * Add a floating thing to the floater stack.
	 */
	void internalAddFloater(@NonNull NodeContainer originalParent, @NonNull FloatingDiv in) {
		//-- Sanity checks.
		if(!(in instanceof FloatingDiv))
			throw new IllegalStateException("Floaters can only be FloatingDiv-derived, and " + in + " is not.");
		if(getBody() == null)
			throw new IllegalStateException("Ehm- I have no body?"); // Existential problems are the hardest...

		//-- Be very sure it's not already in the stack
		final FloatingDiv window = in;
		for(FloatingDiv fr : getFloatingStack()) {
			if(fr == window)
				return;
		}

		//-- It needs to be added. Calculate the zIndex to use; calculate a Z-Index that is higher than the current "topmost" window.
		int zindex = 100;
		for(FloatingDiv fr : getFloatingStack()) {
			if(fr.getZIndex() >= zindex)
				zindex = fr.getZIndex() + 100;
		}
		window.setZIndex(zindex);
		//		System.out.println("New floater got zIndex=" + zindex);

		//-- If this is MODAL create a hider for it.
		if(window.isModal()) {
			Div hider = new Div();
			getBody().add(hider);
			hider.setCssClass("ui-flw-hider");
			hider.setZIndex(zindex - 1); // Just below the new floater.
			window.internalSetHider(hider);

			//-- Add a click handler which will close the floater when the hider div is clicked.
			hider.setClicked(new IClicked<NodeBase>() {
				@Override
				public void clicked(@NonNull NodeBase clickednode) throws Exception {
					if(window.isAutoClose())
						window.closePressed();
				}
			});
		}
		getFloatingStack().add(window); // Add on top (defines order)

		//-- Add the floater to the body,
		getBody().internalAdd(Integer.MAX_VALUE, window); // Add to body,
	}

	/**
	 * Callback called by a floating window when it is removed from the page.
	 */
	public void internalRemoveFloater(@NonNull FloatingDiv floater) {
		if(!getFloatingStack().remove(floater)) // If already removed exit
			return;
		Div h = floater.internalGetHider();
		if(h != null) {
			h.remove();
			floater.internalSetHider(null);
		}
	}

	@NonNull
	private List<FloatingDiv> getFloatingStack() {
		List<FloatingDiv> ws = m_floatingWindowStack;
		if(ws == null)
			m_floatingWindowStack = ws = new ArrayList<FloatingDiv>();
		return ws;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	BUILD phase coding (see bug 688).					*/
	/*--------------------------------------------------------------*/

	void internalAddPendingBuild(@NonNull NodeBase n) {
		m_pendingBuildSet.add(n);
	}

	/**
	 * This handles the BUILD phase for a FULL page render.
	 */
	public void internalFullBuild() throws Exception {
		m_phase = PagePhase.BUILD;
		m_pendingBuildSet.clear();
		buildSubTree(getBody());
		rebuildLoop();
	}

	/**
	 * This handles the BUILD phase for the DELTA build. It walks only the nodes that
	 * are marked as changed initially and does not descend subtrees that are unchanged.
	 */
	public void internalDeltaBuild() throws Exception {
		m_phase = PagePhase.BUILD;
		m_pendingBuildSet.clear();
		buildChangedTree(getBody());
		rebuildLoop();
	}

	/**
	 * Loop over the changed-nodeset until it stays empty.
	 */
	private void rebuildLoop() throws Exception {
		int tries = 0;
		modelToControl();
		while(m_pendingBuildSet.size() > 0) {
			if(tries++ > 10)
				throw new IllegalStateException("Internal: building the tree failed after " + tries + " attempts: the tree keeps changing every build....");
			NodeBase[] todo = m_pendingBuildSet.toArray(new NodeBase[m_pendingBuildSet.size()]); // Dup todolist,
			m_pendingBuildSet.clear();
			for(NodeBase nd : todo) {
				buildSubTree(nd);
				modelToControl();
			}
		}
	}

	/**
	 * Call 'build' on all subtree nodes and reset all rebuild markers in the set code.
	 */
	private void buildSubTree(@NonNull NodeBase nd) throws Exception {
		nd.build();
		m_pendingBuildSet.remove(nd); // We're building this dude.
		if(!(nd instanceof NodeContainer))
			return;
		NodeContainer nc = (NodeContainer) nd;
		List<NodeBase> ichl = nc.internalGetChildren();
		for(int i = 0, len = ichl.size(); i < len; i++) {
			buildSubTree(ichl.get(i));
		}
	}

	private void buildChangedTree(@NonNull NodeBase nd) throws Exception {
		m_pendingBuildSet.remove(nd);
		if(!(nd instanceof NodeContainer)) {
			//-- NodeBase only- simple; always rebuild.
			nd.build();
			return;
		}
		NodeContainer nc = (NodeContainer) nd;
		if(nc.childHasUpdates() && nc.internalGetOldChildren() == null) {
			nc.build();

			List<NodeBase> ichl = nc.internalGetChildren();
			for(int i = 0, len = ichl.size(); i < len; i++) {
				buildChangedTree(ichl.get(i));
			}
		}
		if(nc.internalGetOldChildren() != null || nc.childHasUpdates() || nc.mustRenderChildrenFully()) {
			buildSubTree(nc);
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Per-request Javascript handling.					*/
	/*--------------------------------------------------------------*/
	/*
	 * The code below allows Javascript to be added for every event roundtrip. All of the
	 * javascript added gets executed /after/ all of the DOM delta has been executed by
	 * the browser.
	 */

	/**
	 * Add a Javascript statement (MUST be a valid, semicolon-terminated statement or statement list) to
	 * execute on return to the browser (once).
	 */
	public void appendJS(@NonNull final CharSequence sq) {
		internalGetAppendJS().append(sq);
	}

	@Nullable
	public StringBuilder internalFlushAppendJS() {
		if(internalCanLeaveCurrentPageByBrowser()) {
			if(m_rootContent instanceof IPageWithNavigationCheck) {
				m_rootContent.appendJavascript("WebUI.setCheckLeavePage(false);");
			}
		} else {
			m_rootContent.appendJavascript("WebUI.setCheckLeavePage(true);");
		}

		StringBuilder sb = m_appendJS;
		m_appendJS = null;
		return sb;
	}

	@NonNull
	public StringBuilder internalGetAppendJS() {
		StringBuilder sb = m_appendJS;
		if(null == sb) {
			sb = m_appendJS = new StringBuilder(2048);
		}
		return sb;
	}


	/**
	 * Force the browser to open a new window with a user-specified URL. The new window does NOT
	 * inherit any DomUI session data, of course, and has no WindowSession. After creation the
	 * window cannot be manipulated by DomUI code.
	 *
	 * @param windowURL    The url to open. If this is a relative path it will get the webapp
	 * 					context appended to it.
	 */
	public void openWindow(@NonNull String windowURL, @Nullable WindowParameters wp) {
		if(windowURL == null || windowURL.length() == 0)
			throw new IllegalArgumentException("Empty window URL");
		String js = DomUtil.createOpenWindowJS(DomUtil.calculateURL(UIContext.getRequestContext(), windowURL), wp);
		appendJS(js);
	}

	/**
	 * DEPRECATED: Should use DomUtil#createOpenWindowJS(Class, PageParameters, WindowParameters).
	 * Open a DomUI page in a separate browser popup window. This window will create it's own WindowSession.
	 * FIXME URGENT This code needs to CREATE the window session BEFORE referring to it!!!!
	 */
	@Deprecated
	public void openWindow(@NonNull Class<? extends UrlPage> clz, @Nullable IPageParameters pp, @Nullable WindowParameters wp) {
		String js = DomUtil.createOpenWindowJS(clz, pp, wp);
		appendJS(js);
	}


	/**
	 * Registers a clicked node, and returns TRUE if we have a double click event.
	 */
	public boolean registerClick(NodeBase clicked) {
		long cts = System.currentTimeMillis();
		if(m_lastClickTarget == clicked) {
			//-- within DBLCLICKTIME?
			long dly = cts - m_lastClickTime;
			m_lastClickTime = cts;
			return dly <= DomApplication.get().getDblClickTime();
		} else {
			m_lastClickTarget = clicked;
			m_lastClickTime = cts;
			return false;
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Component focus handling.							*/
	/*--------------------------------------------------------------*/

	/**
	 * Return the component that currently has a focus request.
	 */
	@Nullable
	public NodeBase getFocusComponent() {
		return m_focusComponent;
	}

	public void setFocusComponent(@Nullable final NodeBase focusComponent) {
		m_focusComponent = focusComponent;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Context handling code.								*/
	/*--------------------------------------------------------------*/
	@NonNull
	public ConversationContext getConversation() {
		ConversationContext cc = m_cc;
		if(cc == null)
			throw new IllegalStateException("The conversational context is null??????");
		cc.checkAttached();
		return cc;
	}

	@Nullable
	public ConversationContext internalGetConversation() {
		return m_cc;
	}

	public int getPageExceptionCount() {
		return m_pageExceptionCount;
	}

	public void setPageExceptionCount(final int pageExceptionCount) {
		m_pageExceptionCount = pageExceptionCount;
	}

	public boolean isFullRenderCompleted() {
		return m_fullRenderCompleted;
	}

	public void setFullRenderCompleted(final boolean fullRenderCompleted) {
		m_fullRenderCompleted = fullRenderCompleted;
	}

	/**
	 * Call all onShelve() handlers on all attached components.
	 */
	public void internalShelve() throws Exception {
		if(m_shelved)
			throw new IllegalStateException("Calling SHELVE on already-shelved page " + this);
		m_shelved = true;
		getBody().internalShelve();
	}

	/**
	 * Call all unshelve handlers on all attached components.
	 */
	public void internalUnshelve() throws Exception {
		if(!m_shelved)
			throw new IllegalStateException("Calling UNSHELVE on already-unshelved page " + this);
		m_shelved = false;
		getBody().internalUnshelve();
	}

	public boolean isShelved() {
		return m_shelved;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Javascript component state registration.			*/
	/*--------------------------------------------------------------*/
	@NonNull
	final private Set<NodeBase> m_javaScriptStateChangedSet = new HashSet<NodeBase>();

	/**
	 * Registers the node specified as needing a callback at delta render time.
	 * @param nodeBase
	 */
	void registerJavascriptStateChanged(@NonNull NodeBase nodeBase) {
		m_javaScriptStateChangedSet.add(nodeBase);
	}

	@NonNull
	public Set<NodeBase> internalGetJavaScriptStateChangedSet() {
		return m_javaScriptStateChangedSet;
	}

	/**
	 * For all nodes that registered a "javascript delta", this calls that node's {@link NodeBase#renderJavascriptDelta(JavascriptStmt)}
	 * method, then it will reset the state for the node. Because calls might cause other nodes to become invalid this
	 * code loops max 10 times checking the set of delta nodes.
	 * @throws Exception
	 */
	@Nullable
	public StringBuilder internalFlushJavascriptStateChanges() throws Exception {
		if(m_javaScriptStateChangedSet.size() == 0)
			return null;

		ArrayList<NodeBase> todo = new ArrayList<NodeBase>(m_javaScriptStateChangedSet);
		StringBuilder sb = new StringBuilder(8192);
		JavascriptStmt stmt = new JavascriptStmt(sb);
		for(int count = 0; count < 10; count++) {
			m_javaScriptStateChangedSet.clear();
			for(NodeBase nb : todo) {
				nb.renderJavascriptDelta(stmt);
				stmt.next();
			}

			if(m_javaScriptStateChangedSet.size() == 0) {
				return sb;
			}

			todo.clear();
			todo.addAll(m_javaScriptStateChangedSet);
			m_javaScriptStateChangedSet.clear();
		}
		throw new IllegalStateException("Javascript state keeps changing: set is " + todo);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Pop-in support.										*/
	/*--------------------------------------------------------------*/

	/**
	 * This sets a new pop-in. This does NOT add the popin to the tree, that
	 * must be done manually.
	 */
	public void setPopIn(@Nullable final NodeContainer pin) {
		if(m_currentPopIn != null && m_currentPopIn != pin) {
			NodeContainer old = m_currentPopIn;
			m_currentPopIn = null;
			old.remove();
		}
		m_currentPopIn = pin;
	}

	/**
	 * Remove any pending pop-in.
	 */
	public void clearPopIn() {
		if(m_currentPopIn != null) {
			NodeContainer old = m_currentPopIn;
			m_currentPopIn = null;
			old.remove();
		}
	}

	@Nullable
	public NodeContainer getPopIn() {
		return m_currentPopIn;
	}

	@Override
	public String toString() {
		return "Page[" + getBody().getClass().getName() + "]";
	}

	public boolean isDestroyed() {
		return m_cc != null && !m_cc.isValid();
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	IQContextContainer implementation.					*/
	/*--------------------------------------------------------------*/

	public boolean isInjected() {
		return m_injected;
	}

	public void setInjected(boolean injected) {
		m_injected = injected;
	}


	@NonNull
	@Override
	public List<QContextContainer> getAllContextContainers() {
		return getConversation().getAllContextContainers();
	}

	@Override
	@NonNull
	public QContextContainer getContextContainer(@NonNull String key) {
		return getConversation().getContextContainer(key);
	}

	public boolean isAllowVectorGraphics() {
		return m_allowVectorGraphics;
	}

	public void setAllowVectorGraphics(boolean allowVectorGraphics) {
		m_allowVectorGraphics = allowVectorGraphics;
	}

	public boolean isRenderAsXHTML() {
		return m_renderAsXHTML;
	}

	public void setRenderAsXHTML(boolean renderAsXHTML) {
		m_renderAsXHTML = renderAsXHTML;
	}

	public void setRenderTemplate(IResourceRef tmp) {
		m_renderTemplate = tmp;
	}

	/**
	 * Experimental: render the page through a template.
	 */
	@Nullable
	public IResourceRef getRenderTemplate() {
		return m_renderTemplate;
	}

	public void setDefaultFocusSource(@Nullable NodeBase node) {
		m_defaultFocusSource = node;
	}

	@Nullable
	public NodeBase getDefaultFocusSource() {
		return m_defaultFocusSource;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Page action events.									*/
	/*--------------------------------------------------------------*/
	public void addAfterRequestListener(@NonNull IExecute x) {
		if(m_afterRequestListenerList.size() == 0)
			m_afterRequestListenerList = new ArrayList<IExecute>();
		m_afterRequestListenerList.add(x);
	}

	public void addBeforeRequestListener(@NonNull IExecute x) {
		if(m_beforeRequestListenerList.size() == 0)
			m_beforeRequestListenerList = new ArrayList<IExecute>();
		m_beforeRequestListenerList.add(x);
	}

	public void removeBeforeRequestListener(@NonNull IExecute x) {
		if(!m_beforeRequestListenerList.remove(x))
			LOG.error("PAGE: removal of beforeRequestListener failed (" + x + ")");
	}

	public void addDestroyListener(@NonNull IExecute listener) {
		m_destroyListenerList.add(listener);
	}

	public void removeDestroyListener(@NonNull IExecute listener) {
		if(!m_destroyListenerList.remove(listener))
			LOG.error("PAGE: removal of destroyListener failed (" + listener + ")");
	}

	public void callRequestFinished() throws Exception {
		for(IExecute x : new ArrayList<>(m_afterRequestListenerList)) {
			x.execute();
		}
	}

	public void callRequestStarted() throws Exception {
		for(IExecute x : new ArrayList<>(m_beforeRequestListenerList)) {
			x.execute();
		}
	}

	public void addAfterRenderListener(@NonNull IExecute x) {
		if(m_afterRenderList == Collections.EMPTY_LIST)
			m_afterRenderList = new ArrayList<>();
		m_afterRenderList.add(x);
	}

	public void removeAfterRenderListener(@NonNull IExecute x) {
		m_afterRenderList.remove(x);
	}

	public void callAfterRenderListeners() throws Exception {
		for(IExecute listener : new ArrayList<>(m_afterRenderList)) {
			listener.execute();
		}
	}

	/**
	 * Internal: add a new message for the page.
	 */
	public <T> void addPageMessage(T message) {
		synchronized(this) {
			m_pageMessageList.add(message);
		}
	}

	/**
	 * Get all messages for the page, and clear that list (empty the postbox).
	 */
	public List<?> getPageMessagesAndClear() {
		synchronized(this) {
			List<Object> list = m_pageMessageList;
			m_pageMessageList = new ArrayList<>();
			return list;
		}
	}


	public Set<SubPage> getRemovedSubPages() {
		return m_removedSubPages;
	}

	public void discardRemovedSubPages() {
		for(SubPage subPage : getRemovedSubPages()) {
			SubConversationContext scs = subPage.getConversation();
			try {
				getConversation().removeAndDestroySubConversation(scs);
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
		getRemovedSubPages().clear();
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Notifications												*/
	/*----------------------------------------------------------------------*/

	private List<NotificationListener<?>> m_notificationListenerList = new ArrayList<>();

	<T> void addNotificationListener(NotificationListener<T> newl) {
		for(NotificationListener<?> nl : m_notificationListenerList) {
			if(nl.getWhom() == newl.getWhom() && nl.getEventClass() == newl.getEventClass()) {
				return;
			}
		}
		m_notificationListenerList.add(newl);
	}

	<T> void removeNotificationListener(NotificationListener<T> oldl) {
		m_notificationListenerList.remove(oldl);
	}

	<T> void notifyPage(@NonNull T eventClass) throws Exception {
		buildSubTree(getBody());

		Class<?> clz = eventClass.getClass();
		for(NotificationListener<?> nl : m_notificationListenerList) {
			if(nl.getEventClass().isAssignableFrom(clz)) {
				INotificationListener<T> listener = (INotificationListener<T>) nl.getListener();
				listener.notify(eventClass);
			}
		}
	}

	@NonNullByDefault
	public static class NotificationListener<T> {
		final private Class<T> m_eventClass;

		final private NodeBase m_whom;

		final private INotificationListener<T> m_listener;

		public NotificationListener(Class<T> eventClass, NodeBase whom, INotificationListener<T> listener) {
			m_eventClass = eventClass;
			m_whom = whom;
			m_listener = listener;
		}

		public Class<T> getEventClass() {
			return m_eventClass;
		}

		public NodeBase getWhom() {
			return m_whom;
		}

		public INotificationListener<T> getListener() {
			return m_listener;
		}
	}


	final private AsyncMessageLink m_asyncLink = new AsyncMessageLink(this);

	/**
	 * Returns an asynchronous link that can be used to signal the page when some event occurs,
	 * but which will disappear if the page is destroyed before that happens.
	 */
	public AsyncMessageLink postbox() {
		return m_asyncLink;
	}

	/**
	 * Called from AsyncManager when a polled request comes in (i.e. auto page updates).
	 */
	public void internalPolledEntry() throws Exception {
		List<IExecute> runList;
		synchronized(this) {
			if(m_pageOnCallbackList.size() == 0)
				return;
			runList = m_pageOnCallbackList;
			m_pageOnCallbackList = new ArrayList<>();
		}

		List<Exception> errorList = new ArrayList<>(runList.size());
		for(IExecute run : runList) {
			try {
				run.execute();
			} catch(Exception x) {
				errorList.add(x);
			}
		}
		if(errorList.size() == 0)
			return;
		for(int i = 0; i < errorList.size(); i++) {
			errorList.get(i).printStackTrace();
		}

		throw errorList.get(0);
	}

	private synchronized void addDelayedExecution(IExecute execute) {
		m_pageOnCallbackList.add(execute);
	}

	static public final class AsyncMessageLink {
		@Nullable
		volatile private Page m_page;

		public AsyncMessageLink(Page up) {
			m_page = up;
		}

		public <T> void post(@NonNull T message) {
			Page page = m_page;
			if(null == page) {
				//System.err.println("Dropping post: " + message);
				return;
			}
			page.addPageMessage(message);
		}

		public void execute(@NonNull IExecute code) {
			Page page = m_page;
			if(null != page)
				page.addDelayedExecution(code);
		}
	}

	/**
	 * Checks if page can be left caused by browser navigation.
	 */
	public boolean internalCanLeaveCurrentPageByBrowser() {
		if(m_rootContent instanceof IPageWithNavigationCheck) {
			IPageWithNavigationCheck pageWithNavigationCheck = (IPageWithNavigationCheck) m_rootContent;
			boolean hasModification = pageWithNavigationCheck.hasModification();
			return !hasModification;
		} else {
			return true;
		}
	}

	/**
	 * Checks if page can be left caused by domui navigation.
	 */
	public boolean internalCanLeaveCurrentPageByDomui(UIGotoContext gotoCtx) throws Exception {
		if(m_rootContent instanceof IPageWithNavigationCheck) {
			IPageWithNavigationCheck pageWithNavigationCheck = (IPageWithNavigationCheck) m_rootContent;
			boolean hasModification = pageWithNavigationCheck.hasModification();
			if(!hasModification) {
				return true;
			}
			if(m_rootContent instanceof IPageWithNavigationHandler) {
				((IPageWithNavigationHandler) m_rootContent).handleNavigationOnModified(gotoCtx);
			} else {
				DomApplication.get().handleNavigationOnModified(gotoCtx, this.getBody());
			}
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Do not use, you will OOM the server just like that!!
	 */
	public void internalSetAllowTooManyNodes(boolean allowTooManyNodes) {
		m_allowTooManyNodes = allowTooManyNodes;
	}
}
