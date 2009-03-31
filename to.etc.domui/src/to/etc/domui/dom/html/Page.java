package to.etc.domui.dom.html;

import java.util.*;

import to.etc.domui.dom.header.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;

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
final public class Page {
	/** Next ID# for unidded nodes. */
	private int						m_nextID = 1;

	/** The unique, random page ID generated to check for session expired problems */
	private int						m_pageTag;

	/** Temp work buffer to prevent lots of allocations. */
	private StringBuilder			m_sb;

	private String					m_title;

	/**
	 * The set of parameters that was used at page creation time.
	 */
	private PageParameters			m_pageParameters;

	private ConversationContext		m_cc;

//	private boolean					m_built;

	private Map<String, NodeBase>	m_nodeMap = new HashMap<String, NodeBase>(127);

	private Map<String, NodeBase>	m_beforeMap;

	/**
	 * Contains the header contributors in the order that they were added.
	 */
	private List<HeaderContributor>	m_orderedContributorList = Collections.EMPTY_LIST;

	/**
	 * Set containing the same header contributors, but in a fast-lookup format.
	 */
	private Set<HeaderContributor>	m_headerContributorSet;

	private StringBuilder				m_appendJS;

	/** Number of exceptions in-a-row on a full render of this page. */
	private int							m_pageExceptionCount;

	/** Set to T if an initial full render of the page completed OK. */
	private boolean						m_fullRenderCompleted;

	private UrlPage						m_rootContent;

	/** The component that needs to be focused. This is null if no explicit focus request was done. */
	private NodeBase					m_focusComponent;

	/**
	 * If a "pop-in" is present this contains the reference to it.
	 */
	private NodeContainer				m_currentPopIn;

	private Map<String, Object>			m_pageData = Collections.EMPTY_MAP;

	public Page(UrlPage pageContent) {
		m_pageTag		= DomApplication.internalNextPageTag();				// Unique page ID.
		m_rootContent = pageContent;
		registerNode(pageContent);				// First node.
		pageContent.internalSetTagName("body");	// Override it's tagname
		pageContent.setErrorFence();			// The body ALWAYS accepts ANY errors.

		addHeaderContributor(HeaderContributor.loadJavascript("$js/jquery-1.2.6.js"));
        addHeaderContributor(HeaderContributor.loadJavascript("$js/ui.core.js"));
        addHeaderContributor(HeaderContributor.loadJavascript("$js/ui.draggable.js"));
        addHeaderContributor(HeaderContributor.loadJavascript("$js/jquery.blockUI.js"));
        addHeaderContributor(HeaderContributor.loadJavascript("$js/domui.js"));
        addHeaderContributor(HeaderContributor.loadJavascript("$js/weekagenda.js"));

        /*
         * FIXME: Delayed construction of components causes problems with components
         * that are delayed and that contribute. Example: tab pabel containing a
         * DateInput. The TabPanel gets built when header contributions have already
         * been handled... For now we add all JS files here 8-(
         */
        addHeaderContributor(HeaderContributor.loadJavascript("$js/calendar.js"));
        addHeaderContributor(HeaderContributor.loadJavascript("$js/calendar-setup.js"));
//      Locale  loc = NlsContext.getLocale();                           // FIXME Use the locale to decide on which calendar to use.
        addHeaderContributor(HeaderContributor.loadJavascript("$js/calendarnls.js"));

        /*
         * FIXME Same as above, this is for loading the FCKEditor.
         */
        addHeaderContributor(HeaderContributor.loadJavascript("$fckeditor/fckeditor.js"));
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Initialization.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Assign required data to the page.
	 * @param pp
	 * @param cc
	 */
	final public void	internalInitialize(PageParameters pp, ConversationContext cc) {
		if(pp == null)
			throw new IllegalStateException("Internal: Page parameters cannot be null here");
		if(cc == null)
			throw new IllegalStateException("Internal: ConversationContext cannot be null here");

		m_cc = cc;
		m_pageParameters = pp;
	}
	public Map<String, NodeBase>	internalNodeMap() {
		return m_nodeMap;
	}

	private StringBuilder	sb() {
		if(m_sb == null)
			m_sb = new StringBuilder(64);
		else
			m_sb.setLength(0);
		return m_sb;
	}

	public DomApplication	getApplication() {
		return DomApplication.get();
	}
	public PageParameters getPageParameters() {
		return m_pageParameters;
	}
	public final int	getPageTag() {
		return m_pageTag;
	}

	/**
	 * Calculates a new ID for a node.
	 * @return
	 */
	final String	nextID() {
		StringBuilder sb = sb();
		sb.append("_");
		int id = m_nextID++;
		while(id != 0) {
			int d = id % 36;
			if(d <= 9)
				d = d + '0';
			else
				d = ('A'+(d-10));
			sb.append((char)d);
			id = id / 36;
		}
		return sb.toString();
	}

	/**
	 * Registers the node with this page. If the node has no ID or the ID is invalid then
	 * a new ID is assigned.
	 * @param n
	 */
	final void		registerNode(NodeBase n) {
		if(n.getPage() != null)
			throw new IllegalStateException("Node still attached to other page");
		/*
		 * jal 20081211 If a node already has an ID reuse that if not currently assigned. This should fix
		 * the following bug in drag and drop: a dropped node is REMOVED, then ADDED to another node (2 ops).
		 * This would reassign an ID, causing the delta to be rendered with a delete of the NEW ID instead
		 * of the old ID.
		 */
		String id = n.internalGetID();
		if(id != null) {
			if(m_nodeMap.containsKey(id)) {		// Duplicate key?
				id = nextID();					// Assign new ID
				n.setActualID(id);				// Save in node.
			}
		} else {
			//-- Assign new ID
			id = nextID();					// Assign new ID
			n.setActualID(id);				// Save in node.
		}
		if(null != m_nodeMap.put(id, n))
			throw new IllegalStateException("Duplicate node ID '"+id+"'!?!?");
		n.setPage(this);
		n.onHeaderContributors(this);		// Ask the node for it's header contributors.
		n.internalOnAddedToPage(this);
		if(n.isFocusRequested()) {
			setFocusComponent(n);
			n.clearFocusRequested();
		}
	}

	/**
	 * Removes this node from the IDmap.
	 * @param n
	 */
	final void	unregisterNode(NodeBase n) {
		if(n.getPage() != this)
			throw new IllegalStateException("This node does not belong to this page!?");
		if(n.getActualID() == null)
			throw new IllegalStateException("This-node's actual ID has gone!?");
		n.internalOnRemoveFromPage(this);
		n.setPage(null);
		if(m_nodeMap.remove(n.getActualID()) == null)
			throw new IllegalStateException("The node with ID="+n.getActualID()+" was not found!?");
	}
	
	public NodeBase findNodeByID(String id) {
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
		m_beforeMap = new HashMap<String, NodeBase>(m_nodeMap);
	}
	final public Map<String, NodeBase> getBeforeMap() {
		return m_beforeMap;
	}

	public void clearDeltaFully() {
		getBody().clearDeltaFully();
		m_beforeMap = null;
		m_sb = null;
	}

	/**
	 * Call from within the onHeaderContributor call on a node to register any header
	 * contributors needed by a node.
	 * @param hc
	 */
	final public void	addHeaderContributor(HeaderContributor hc) {
		if(m_headerContributorSet == null) {
			m_headerContributorSet = new HashSet<HeaderContributor>(30);
			m_orderedContributorList = new ArrayList<HeaderContributor>(30);
			m_headerContributorSet.add(hc);
			m_orderedContributorList.add(hc);
			return;
		}
		if(m_headerContributorSet.contains(hc))			// Already registered?
			return;
		m_headerContributorSet.add(hc);
		m_orderedContributorList.add(hc);
	}
	public List<HeaderContributor> getHeaderContributorList() {
		return m_orderedContributorList;
	}

	public UrlPage getBody() {
		return m_rootContent;
	}
	public void	build() throws Exception {
		getBody().build();
	}

	public String getTitle() {
		return m_title;
	}

	public void setTitle(String title) {
		m_title = title;
	}

//	public void	add(NodeBase node) {
//		getBody().add(node);
//	}

	public void	appendJS(CharSequence sq) {
		if(m_appendJS == null)
			m_appendJS = new StringBuilder(sq.length()+100);
		m_appendJS.append(sq);
	}
	public StringBuilder getAppendedJS() {
		StringBuilder	sb = m_appendJS;
		m_appendJS = null;
		return sb;
	}

	public <T> void		setData(T inst) {
		if(m_pageData == Collections.EMPTY_MAP)
			m_pageData = new HashMap<String, Object>();
		m_pageData.put(inst.getClass().getName(), inst);
	}

	public <T> T		getData(Class<T> clz) {
		return (T)m_pageData.get(clz.getName());
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Component focus handling.							*/
	/*--------------------------------------------------------------*/
	/**
	 * Return the component that currently has a focus request.
	 * @return
	 */
	public NodeBase getFocusComponent() {
		return m_focusComponent;
	}

	public void setFocusComponent(NodeBase focusComponent) {
		m_focusComponent = focusComponent;
	}

//	/**
//	 * Walks the component tree, and makes the first input component hold the focus.
//	 */
//	public void	focusFirstInput() {
//		focusFirstInput(getBody());
//	}
//	private boolean focusFirstInput(NodeBase b) {
//		System.out.println("FFN: "+b);
//		if(b instanceof IInputNode) {
//			b.setFocus();
//			return true;
//		}
//
//		if(b instanceof NodeContainer) {
//			NodeContainer nc = (NodeContainer) b;
//			for(NodeBase ch: nc) {
//				if(focusFirstInput(ch))
//					return true;
//			}
//		}
//		return false;
//	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Context handling code.								*/
	/*--------------------------------------------------------------*/
	public ConversationContext	getConversation() {
		if(m_cc == null)
			throw new IllegalStateException("The conversational context is null??????");
		m_cc.checkAttached();
		return m_cc;
	}
	public ConversationContext	internalGetConversation() {
		return m_cc;
	}

	public int getPageExceptionCount() {
		return m_pageExceptionCount;
	}

	public void setPageExceptionCount(int pageExceptionCount) {
		m_pageExceptionCount = pageExceptionCount;
	}

	public boolean isFullRenderCompleted() {
		return m_fullRenderCompleted;
	}

	public void setFullRenderCompleted(boolean fullRenderCompleted) {
		m_fullRenderCompleted = fullRenderCompleted;
	}

	/** Temp for checking shelve order. */
	private boolean			m_shelved;

	/**
	 * Call all onShelve() handlers on all attached components.
	 * @throws Exception
	 */
	public void	internalShelve() throws Exception {
		if(m_shelved)
			throw new IllegalStateException("Calling SHELVE on already-shelved page "+this);
		m_shelved = true;
		getBody().internalShelve();
	}

	/**
	 * Call all unshelve handlers on all attached components.
	 *
	 * @throws Exception
	 */
	public void	internalUnshelve() throws Exception {
		if(! m_shelved)
			throw new IllegalStateException("Calling UNSHELVE on already-unshelved page "+this);
		m_shelved = false;
		getBody().internalUnshelve();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Pop-in support.										*/
	/*--------------------------------------------------------------*/
	/**
	 * This sets a new pop-in. This does NOT add the popin to the tree, that
	 * must be done manually.
	 * 
	 * @param pin
	 */
	public void		setPopIn(NodeContainer pin) {
		if(m_currentPopIn != null && m_currentPopIn != pin) {
			m_currentPopIn.remove();
			m_currentPopIn = null;
		}
		m_currentPopIn = pin;
	}

	/**
	 * Remove any pending pop-in.
	 */
	public void		clearPopIn() {
		if(m_currentPopIn != null) {
			m_currentPopIn.remove();
			m_currentPopIn = null;
		}
	}

	@Override
	public String toString() {
		return "Page["+getBody().getClass().getName()+"]";
	}
}
