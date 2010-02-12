package to.etc.domui.dom.html;

import java.util.*;

import to.etc.domui.component.form.*;
import to.etc.domui.component.input.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;
import to.etc.util.*;

/**
 * Base node for all non-container html dom nodes.
 *
 * <h2>Delta tree calculation</h2>
 * <p>We need to be able to calculate a delta tree <b>very</b> fast. The speed of the delta calculation is a large factor in response time
 * and server CPU utilization. Delta calculation works by defining a strict way in which the DOM is manipulated (conceptual):
 * <ol>
 *	<li>We define the BEFORE state of the tree. This is the state of the tree when a new request ENTERS the server. The before state is conceptually
 *		maintained by copying all "parent" pointers and all "child" lists of all nodes (in a special way). This means that a single node has both a
 *		"current" state (the normal parent and childList properties) and an "old" state (the state of it's parent and children properties before
 *		code that possibly changes the tree starts).</li>
 *	<li>In the 'application-phase' we start executing code that can change the tree. We run all input handlers, then we handle all events. When all code
 *		ran we have the AFTER state of the tree, contained in the actual tree properties (parent and childList). In addition, every change in the
 *		structure has left a mark on every container node. This mark "percolates" upwards, so we can quickly see which container(s) have changes. If the
 *		tree is unchanged we see it immediately because the root node has no "childHasUpdates" indication.</li>
 *	<li>We now have a valid BEFORE and a valid AFTER image. By comparing the nodes in OLD and ACTUAL we can easily determine tree ownership everywhere.</li>
 * </ol>
 * </p>
 *
 * <h3>Special problems</h3>
 * <p>For the mechanism to work properly it is very important that the BEFORE state is proper. This means that the BEFORE state may NOT be changed when,
 * during the application-phase, nodes are removed and possibly re-added to the tree. For this event it is important that <i>even</i> when the node is
 * added and removed the BEFORE state is kept as ATTACHED; it may not change as the result of the moves executed in between.</p>
 * <p>When nodes are removed from the tree and in a different request/response cycle are added again we must ensure that any state from the previous
 * delta run leaks to the new delta run; if this happens it usually results in a "Hell Freezeth over" exception. Because nodes removed from the tree
 * in a phase are not reachable anymore it is hard to clear this state <i>after</i> the delta run.</p>
 *
 * <h3>Maintaining and clearing the old state - quickly</h3>
 * <p>for the delta mechanism to work we need to ensure that the old state properly represents the state of each node in the tree. The "easiest" way
 * to do that is to do a full tree traversal to define the BEFORE state. This marks all nodes's parents. Then, after the delta has been fully calculates
 * the state must be deleted from ALL nodes, including those that were DELETED(!). This requires that all deleted nodes are kept on a list, and are fully
 * cleaned after the delta.</p>
 *
 * <h3>Update numbers' basics</h3>
 * <p>Although reasonably simple this is slow as molasses: it requires lots of treewalks which are expensive. So we use a different, more complex but way
 * faster method. This method is based around "update numbers". Update numbers are a method used to quickly purge large structures of state, without traversing
 * the entire state. They work as follows:
 * <ul>
 *	<li>Each node gets a field "currentUpdateNumber".</li>
 *	<li>The page gets a field "currentUpdateRun". For every request cycle this number gets <i>incremented</i> BEFORE any other action takes place.</li>
 *	<li>We assume the pointers valid (set in this run) <i>only</i> when the update number in the page equals the update number in the node.</li>\
 *	<li>When any of the protected fields are set in this run the update number for the node is set to the update number in the page. This identifies the
 *		field as set to a value</li>
 *	<li>The simple act of incrementing the page's currentUpdateRun field invalidates (clears) all of the states in the child nodes, without
 *		walking past all of them!!</li>
 * </ul>
 * We will use this mechanism to identify nodes that have changed <i>this run</i> and to discover nodes that have old state and are added again.
 * </p>
 *
 * <h3>Implementation details and logic decomposition</h3>
 * <p>The oldParent field is the field protected by the updateNumber. Normal nodes all have "outdated" updatenumbers meaning their fields are UNSET. Since only
 * changed nodes have fields set an outdated updateNumber also implies that the node is unchanged.
 * </p>
 * <p>If a node is PRESENT in a page, and it is REMOVED then we SET it's update# and it's oldParent. This means the removed node has valid ptrs for THIS run indicating
 * both it's change and the fact that it's old parent was node xxxx.
 * </p>
 * <p>If a node is not present in the page and it gets ADDED the add sees that the node has UNSET pointers (because it's update # is wrong wrt the page it gets added
 * to). This means that regardless of the new node's old pointer it's old pointer is invalid, so it gets nulled (indicating it is a NEWLY ATTACHED node) and the update#
 * gets updated to current indicating the null is valid/set.</p>
 * <p>If this node that was just attached is then removed again the oldparent is not changed (because it is already SET in this run). If it gets added yet again
 * (the programmer is probably drunk, but with all the monthly drinks at Itris we'll need to take that into consideration too) the add notices that its' OLD is
 * VALID so it will not be set.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 18, 2007
 */
abstract public class NodeBase extends CssBase implements INodeErrorDelegate, IModelBinding {
	/** The owner page. If set then this node IS attached to the parent in some way; if null it is not attached. */
	private Page m_page;

	private String m_tag;

	private String m_cssClass;

	/** This is the actual ID of the node IF the framework decided to override the specified ID (or if no ID was assigned). */
	private String m_actualID;

	private String m_testID;

	private NodeContainer m_parent;

	private IClicked< ? > m_clicked;

	private boolean m_built;

	/** T when this node's html/style attributes changed. */
	private boolean m_attributesChanged;

	private NodeContainer m_oldParent;

	/** Helper variable containing this-node's index in the output tree while calculating the delta */
	int m_oldNodeIndex;

	int m_newNodeIndex;

	int m_origNewIndex;

	private Object m_userObject;

	private String m_title;

	private String m_onClickJS;

	private String m_onMouseDownJS;

	private StringBuilder m_appendJS;

	private StringBuilder m_createJS;

	private List<String> m_specialAttributes;

	private boolean m_focusRequested;

	/**
	 * This must visit the appropriate method in the node visitor. It should NOT recurse it's children.
	 * @param v
	 * @throws Exception
	 */
	abstract public void visit(INodeVisitor v) throws Exception;

	protected NodeBase(final String tag) {
		m_tag = tag;
	}

	public void internalSetTagName(final String s) {
		m_tag = s;
	}

	public IClicked< ? > getClicked() {
		return m_clicked;
	}

	public void setClicked(final IClicked< ? > clicked) {
		m_clicked = clicked;
	}

	public boolean internalNeedClickHandler() {
		return getClicked() != null;
	}

	public boolean hasChangedAttributes() {
		return m_attributesChanged;
	}

	public void setHasChangedAttributes(final boolean d) {
		m_attributesChanged = d;
	}

	public void setHasChangedAttributes() {
		setHasChangedAttributes(true);
	}

	public void internalCheckNotDirty() {
		if(hasChangedAttributes())
			throw new IllegalStateException("The node " + this + " has DIRTY ATTRIBUTES set");
	}

	public void internalOnClicked() throws Exception {
		IClicked<NodeBase> c = (IClicked<NodeBase>) getClicked();
		if(c == null)
			throw new IllegalStateException("? Node " + this.getActualID() + " does not have a click handler??");
		c.clicked(this);
	}

	/**
	 * Called on stylesheet changes. Clears the style cache.
	 *
	 * @see to.etc.domui.dom.css.CssBase#changed()
	 */
	@Override
	protected void changed() {
		setCachedStyle(null);
		setHasChangedAttributes();
		if(getParent() != null)
			getParent().childChanged(); // Indicate child has changed
		super.changed();
	}

	public String getCssClass() {
		return m_cssClass;
	}

	public void setCssClass(final String cssClass) {
		//		System.out.println("--- id="+m_actualID+", css="+cssClass);
		if(!DomUtil.isEqual(cssClass, m_cssClass))
			changed();
		m_cssClass = cssClass;
	}

	/**
	 * Removes the specified CSS class. Returns T if the class was actually present.
	 * @param name
	 * @return
	 */
	public boolean removeCssClass(final String name) {
		if(getCssClass() == null)
			return false;
		StringTokenizer st = new StringTokenizer(getCssClass(), " \t");
		StringBuilder sb = new StringBuilder(getCssClass().length());
		boolean fnd = false;
		while(st.hasMoreTokens()) {
			String s = st.nextToken();
			if(name.equals(s)) {
				fnd = true;
			} else {
				if(sb.length() > 0)
					sb.append(' ');
				sb.append(s);
			}
		}
		if(!fnd)
			return false;
		setCssClass(sb.toString());
		return true;
	}

	public void addCssClass(final String name) {
		if(getCssClass() == null) {
			setCssClass(name);
			return;
		}
		StringTokenizer st = new StringTokenizer(getCssClass(), " \t");
		while(st.hasMoreTokens()) {
			String s = st.nextToken();
			if(name.equals(s)) // Already present?
				return;
		}
		setCssClass(getCssClass() + " " + name);
	}

	public boolean hasCssClass(final String cls) {
		if(getCssClass() == null)
			return false;
		int pos = getCssClass().indexOf(cls);
		if(pos == -1)
			return false;
		if(pos != 0 && getCssClass().charAt(pos - 1) != ' ')
			return false;
		return true;
	}

	final public String getActualID() {
		if(null == m_actualID)
			throw new IllegalStateException("Missing ID on " + this);
		return m_actualID;
	}

	final String internalGetID() {
		return m_actualID;
	}

	final void setActualID(final String actualID) {
		m_actualID = actualID;
	}

	/**
	 * Return the node's tag name (the html tag this node represents).
	 * @return
	 */
	final public String getTag() {
		return m_tag;
	}

	/**
	 * INTERNAL USE ONLY, FOR SPECIAL CASES!!!! Node tags may NEVER change once rendered to the browser.
	 * @param tag
	 */
	final protected void setTag(final String tag) {
		m_tag = tag;
	}


	/**
	 * Return the current actual parent of this node.
	 * @return
	 */
	public NodeContainer getParent() {
		return m_parent;
	}

	/**
	 * Find the nth upward parent of this node. When n == 1 this is the
	 * same as getParent(): it returns the direct parent. For n == 2  this
	 * returns the parent of the parent etc. As soon as a null parent is
	 * encountered this will return null.
	 *
	 * @param up
	 * @return
	 */
	public NodeContainer getParent(int up) {
		NodeContainer c = m_parent;
		while(--up > 0) {
			if(c == null)
				return null;
			c = c.getParent();
		}
		return c;
	}

	/**
	 * Walk the parents upwards to find the closest parent of the given class. The class can be a base class (it is
	 * not a literal match but an instanceof match).
	 * @param <T>
	 * @param clz
	 * @return
	 */
	public <T> T getParent(final Class<T> clz) {
		NodeContainer c = getParent();
		for(;;) {
			if(c == null)
				return null;
			if(clz.isAssignableFrom(c.getClass()))
				return (T) c;
			c = c.getParent();
		}
	}

	/**
	 * INTERNAL USE ONLY Changes the OLD PARENT pointer. THIS FORCES A "set", and validates the pointer
	 * by setting the updateNumber equal to the page's update#.
	 * @param c
	 */
	void setOldParent(final NodeContainer c) {
		m_oldParent = c;
	}

	public NodeContainer getOldParent() {
		return m_oldParent;
	}

	public void clearDelta() {
		m_oldParent = null;
		setHasChangedAttributes(false);
	}

	public void clearDeltaFully() {
		clearDelta();
	}

	/**
	 * FIXME NEED TO BE CHANGED - LOGIC MUST MOVE TO CONTAINER.
	 * @param parent
	 */
	void setParent(final NodeContainer parent) {
		if(m_oldParent == null) // jal 20090115 Was !=, seems very wrong and the cause of the "Hell Freezeth over" exception..
			m_oldParent = m_parent;
		m_parent = parent;
	}

	/**
	 * Disconnect this node from it's parent. The node can be reconnected to another parent
	 * afterwards.
	 */
	public void remove() {
		if(getParent() != null) {
			getParent().removeChild(this);
		}
	}

	public void replaceWith(final NodeBase nw) {
		getParent().replaceChild(this, nw);
	}

	void unregisterFromPage() {
		if(getPage() == null)
			return;
		clearMessage(); // jal 20091015 Remove any pending messages for removed nodes.
		getPage().unregisterNode(this);
	}

	public void appendAfterMe(final NodeBase item) {
		if(getParent() == null)
			throw new IllegalStateException("No parent node is known");
		int ix = getParent().findChildIndex(this);
		if(ix == -1)
			throw new IllegalStateException("!@?! Cannot find myself!?");
		getParent().add(ix + 1, item);
	}

	public void appendBeforeMe(final NodeBase item) {
		if(getParent() == null)
			throw new IllegalStateException("No parent node is known");
		int ix = getParent().findChildIndex(this);
		if(ix == -1)
			throw new IllegalStateException("!@?! Cannot find myself!?");
		getParent().add(ix, item);
	}

	void registerWithPage(final Page p) {
		p.registerNode(this);
	}

	final public Page getPage() {
		return m_page;
	}

	final void setPage(final Page page) {
		m_page = page;
	}

	final public void build() throws Exception {
		if(!m_built) {
			internalCreateContent();
			m_built = true;
		}
	}

	protected void clearBuilt() {
		m_built = false;
	}

	/**
	 * Returns T if the node's content has been built.
	 * @return
	 */
	public boolean isBuilt() {
		return m_built;
	}

	private final void internalCreateContent() throws Exception {
		createContent();
		afterCreateContent();
	}

	/**
	 * Return the <i>literal</i> text, with tilde replacement done. If the value set was
	 * a resource key (a string starting with ~) this resolves the key into a string and
	 * returns that. To obtain the key instead of the translated value use getTitle().
	 *
	 * @return
	 */
	public String getLiteralTitle() {
		return DomUtil.replaceTilded(this, m_title); // FIXME Performance?
	}

	/**
	 * Set the title attribute, using tilde replacement. If the string starts with a ~ it is
	 * assumed to be a key into the page's resource bundle.
	 *
	 * @param title
	 */
	public void setTitle(final String title) {
		if(!DomUtil.isEqual(title, m_title))
			changed();
		m_title = title;
	}

	/**
	 * Returns the title <i>as set</i> verbatim; if it was set using a tilde key this returns the <i>key</i> without resource bundle replacement.
	 * @return
	 */
	public String getTitle() {
		return m_title;
	}

	@Override
	public String toString() {
		String n = getClass().getName();
		int pos = n.lastIndexOf('.');
		return n.substring(pos + 1) + ":" + m_actualID + (m_title == null ? "" : "/" + m_title);
	}

	public boolean acceptRequestParameter(final String[] values) throws Exception {
		throw new IllegalStateException("?? The '" + getTag() + "' component (" + this.getClass() + ") with id=" + m_actualID + " does NOT accept input!");
	}

	void internalOnAddedToPage(final Page p) {
		onAddedToPage(p);
		if(m_appendJS != null) {
			getPage().appendJS(m_appendJS);
			m_appendJS = null;
		}
	}

	void internalOnRemoveFromPage(final Page p) {
		onRemoveFromPage(p);
	}

	public void createContent() throws Exception {}

	protected void afterCreateContent() throws Exception {}

	public void onAddedToPage(final Page p) {}

	public void onRemoveFromPage(final Page p) {}

	public void onHeaderContributors(final Page page) {}

	//	public boolean validate() {
	//		return true;
	//	}

	public Object getUserObject() {
		return m_userObject;
	}

	public void setUserObject(final Object userObject) {
		m_userObject = userObject;
	}

	/**
	 * When set this causes a "testid" attribute to be rendered on the node. This ID can then be used for selenium tests et al.
	 * @return
	 */
	public String getTestID() {
		return m_testID;
	}

	public void setTestID(String testID) {
		if(DomUtil.isEqual(testID, m_testID))
			return;
		m_testID = testID;
		changed();
	}

	public String getOnClickJS() {
		return m_onClickJS;
	}

	public void setOnClickJS(final String onClickJS) {
		if(DomUtil.isEqual(onClickJS, m_onClickJS))
			changed();
		m_onClickJS = onClickJS;
	}

	public String getOnMouseDownJS() {
		return m_onMouseDownJS;
	}

	public void setOnMouseDownJS(final String onMouseDownJS) {
		m_onMouseDownJS = onMouseDownJS;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Javascript handling.								*/
	/*--------------------------------------------------------------*/
	/**
	 * This adds a Javascript segment to be executed <b>one time</b>, as soon as the
	 * current request returns. <b>The code is rendered only once</b>. This should
	 * only be used in "event" based code; if you need javascript to <i>create</i> a component
	 * you need to call {@link #appendCreateJS(CharSequence)}. This method can
	 * be called from all code to add a Javascript to execute on the browser. This
	 * Javascript should <i>only</i> reference global state or this specific component
	 * <b>because the order of execution for multiple components is explicitly undefined</b>.
	 *
	 * @param js
	 */
	public void appendJavascript(final CharSequence js) {
		if(getPage() != null)
			getPage().appendJS(js);
		else {
			if(m_appendJS == null)
				m_appendJS = new StringBuilder(js.length() + 100);
			m_appendJS.append(';');
			m_appendJS.append(js);
		}
	}

	/**
	 * This adds a Javascript segment to be executed when the component is (re)constructed. It
	 * gets added to the page's onload() code every time this object is constructed. It gets
	 * rendered <i>only</i> when the component is initially created <i>or</i> when the page
	 * is fully refreshed. The latter means that this string <b>may not contain</b> state
	 * information because this means that the running Javascript state of the component will
	 * be reset when the page is refreshed.
	 * This Javascript should <i>only</i> reference global state or this specific component
	 * <b>because the order of execution for multiple components is explicitly undefined</b>.
	 *
	 * @param js
	 */
	public void appendCreateJS(final CharSequence js) {
		if(m_createJS == null)
			m_createJS = new StringBuilder();
		m_createJS.append(js);
		m_createJS.append(';');
	}

	public StringBuilder getCreateJS() {
		return m_createJS;
	}

	/**
	 * This gets called when a component is re-rendered fully because of a full page
	 * refresh. It should only be used for components that maintain a lot of state
	 * in Javascript on the browser. These components need to add Javascript commands
	 * to that browser to restore/initialize the state to whatever is present in the
	 * server's data store. It must do that by adding the needed Javascript to the buffer
	 * passed.
	 *
	 * @param sb
	 * @throws Exception
	 */
	public void renderJavascriptState(StringBuilder sb) throws Exception {

	}

	public void onBeforeFullRender() throws Exception {
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Generic attribute and event handling.				*/
	/*--------------------------------------------------------------*/
	/**
	 * This is a generic method to add tag add attributes to a tag. It can be used to add
	 * attributes that are not defined on the HTML class for the node, like "onblur", "testid"
	 * and the like. There are no limitations to what can be generated with it but since it
	 * is expensive it should be used little. If a given attribute is used many times it
	 * must be created as a field proper.
	 * @param name
	 * @param value
	 */
	public void setSpecialAttribute(final String name, final String value) {
		if(m_specialAttributes == null) {
			m_specialAttributes = new ArrayList<String>(5);
		} else {
			for(int i = 0; i < m_specialAttributes.size(); i += 2) {
				if(m_specialAttributes.get(i).equals(name)) {
					if(value == null) {
						m_specialAttributes.remove(i);
						m_specialAttributes.remove(i);
						return;
					}
					m_specialAttributes.set(i + 1, value);
					changed();
					return;
				}
			}
		}
		m_specialAttributes.add(name);
		m_specialAttributes.add(value);
		changed();
	}

	/**
	 * Return the list of special attributes and their value. The even index retrieves
	 * the name, the odd index it's value. See {@link #setSpecialAttribute(String, String)} for
	 * details.
	 *
	 * @return
	 */
	public List<String> getSpecialAttributeList() {
		return m_specialAttributes;
	}

	public String getSpecialAttribute(final String name) {
		if(m_specialAttributes != null) {
			for(int i = 0; i < m_specialAttributes.size(); i += 2) {
				if(m_specialAttributes.get(i).equals(name))
					return m_specialAttributes.get(i + 1);
			}
		}
		return null;
	}

	/**
	 * Default handling for webui AJAX actions to a component.
	 * @param ctx
	 * @param action
	 * @throws Exception
	 */
	public void componentHandleWebAction(final RequestContextImpl ctx, final String action) throws Exception {
		if("WEBUIDROP".equals(action)) {
			handleDrop(ctx);
			return;
		}
		throw new IllegalStateException("The component " + this + " does not accept the web action " + action);
	}

	/**
	 * Claim the focus for this component. Only reasonable for input type and action
	 * components (links, buttons). For now this can only be called for components that
	 * are already attached to a page. If this is a proble I'll fix it. Only one component
	 * in a page can claim focus for itself.
	 */
	public void setFocus() {
		if(getPage() == null) {
			//-- Mark this as a component wanting the focus.
			m_focusRequested = true;
		} else
			getPage().setFocusComponent(this);
	}

	public boolean isFocusRequested() {
		return m_focusRequested;
	}

	public void clearFocusRequested() {
		m_focusRequested = false;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Error message handling code.						*/
	/*--------------------------------------------------------------*/
	/*
	 * Explanatory blurb.
	 */
	/**
	 * When set this component has an error/warning/info message. A control can have only one
	 * message associated with it; the most severe error of all message types gets used.
	 */
	private UIMessage m_message;

	/**
	 * When set this contains a user-understandable tekst indicating which control has the error. It usually contains
	 * the "label" associated with the control, and is set automatically by form builders if possible.
	 */
	private String m_errorLocation;

	private INodeErrorDelegate m_errorDelegate;

	/**
	 * When set this contains a user-understandable tekst indicating which control has the error. It usually contains
	 * the "label" associated with the control, and is set automatically by form builders if possible.
	 * @param errorLocation
	 */
	public void setErrorLocation(String errorLocation) {
		m_errorLocation = errorLocation;
	}

	/**
	 * When set this contains a user-understandable tekst indicating which control has the error. It usually contains
	 * the "label" associated with the control, and is set automatically by form builders if possible.
	 * @return
	 */
	public String getErrorLocation() {
		return m_errorLocation;
	}

	/**
	 * This sets a message (an error, warning or info message) on this control. If the
	 * control already has an error then we check if the severity of the new error is
	 * higher than the severity of the existing one; only in that case will the error
	 * be removed. To clear the error message call clearMessage().
	 *
	 * @param mt
	 * @param code
	 * @param param
	 */
	public UIMessage setMessage(final UIMessage msg) {
		if(m_errorDelegate != null)
			return m_errorDelegate.setMessage(msg);

		//-- If this (new) message has a LOWER severity than the EXISTING message ignore this call and return the EXISTING message
		if(m_message != null) {
			if(m_message.getType().getOrder() > msg.getType().getOrder()) {
				return m_message;
			}

			//-- If code, type and parameters are all equal just leave the existing message in-place
			if(m_message == msg || m_message.equals(msg))
				return m_message;

			//-- The current message is to be replaced. For that we need to clear it first
			clearMessage(); // Discard existing message
		}

		//-- Now add the message
		m_message = msg;
		if(msg.getErrorLocation() == null)
			msg.setErrorLocation(m_errorLocation);
		msg.setErrorNode(this);

		IErrorFence fence = DomUtil.getMessageFence(this); // Get the fence that'll handle the message by looking UPWARDS in the tree
		fence.addMessage(this, m_message);
		return m_message;
	}

	/**
	 * Remove this-component's "current" error message, if present.
	 */
	public void clearMessage() {
		if(m_errorDelegate != null) {
			m_errorDelegate.clearMessage();
			return;
		}
		if(getMessage() == null)
			return;
		IErrorFence fence = DomUtil.getMessageFence(this); // Get the fence that'll handle the message by looking UPWARDS in the tree
		UIMessage msg = m_message;
		m_message = null;
		fence.removeMessage(this, msg);
	}

	public UIMessage getMessage() {
		if(m_errorDelegate != null)
			return m_errorDelegate.getMessage();
		return m_message;
	}

	/**
	 * Return T if this node currently has an error associated with it.
	 * @return
	 */
	public boolean hasError() {
		return getMessage() != null && getMessage().getType() == MsgType.ERROR;
	}

	public void setErrorDelegate(final INodeErrorDelegate errorDelegate) {
		m_errorDelegate = errorDelegate;
	}

	public INodeErrorDelegate getErrorDelegate() {
		return m_errorDelegate;
	}

	/**
	 * This adds a message to the "global" message list. The message "percolates" upwards to the first parent that acts
	 * as an error message fence. That component will be responsible for rendering the error message at an appropriate
	 * location.
	 * @param mt
	 * @param code
	 * @param param
	 */
	public UIMessage addGlobalMessage(UIMessage m) {
		IErrorFence fence = DomUtil.getMessageFence(this); // Get the fence that'll handle the message by looking UPWARDS in the tree
		fence.addMessage(this, m);
		return m;
	}

	public void clearGlobalMessage() {
		IErrorFence fence = DomUtil.getMessageFence(this); // Get the fence that'll handle the message by looking UPWARDS in the tree
		fence.clearGlobalMessages(this, null);
	}

	public void clearGlobalMessage(UIMessage m) {
		IErrorFence fence = DomUtil.getMessageFence(this); // Get the fence that'll handle the message by looking UPWARDS in the tree
		fence.removeMessage(null, m);
	}

	public void clearGlobalMessage(final String code) {
		IErrorFence fence = DomUtil.getMessageFence(this); // Get the fence that'll handle the message by looking UPWARDS in the tree
		fence.clearGlobalMessages(this, code);
	}

	protected void internalShelve() throws Exception {
		onShelve();
	}

	protected void internalUnshelve() throws Exception {
		onUnshelve();
	}

	protected void onShelve() throws Exception {}

	protected void onUnshelve() throws Exception {}


	/*--------------------------------------------------------------*/
	/*	CODING:	Handle dropping of dnd nodes.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Called when a drop is done on a DropTarget node. This calls the appropriate handlers on both the
	 * drop node AND the draggable that was dropped.
	 * @param ctx
	 */
	protected void handleDrop(final RequestContextImpl ctx) throws Exception {
		//-- Get the drop handler,
		if(!(this instanceof IDropTargetable))
			throw new IllegalStateException("?? Got a DROP action but I am not able to receive droppings?? " + this);
		IDropHandler droph = ((IDropTargetable) this).getDropHandler();

		//-- Find the dragged node and it's DragHandler
		String dragid = ctx.getParameter("_dragid");
		if(dragid == null)
			throw new IllegalStateException("No _dragid in drop request to node=" + this);
		NodeBase dragnode = getPage().findNodeByID(dragid);
		if(dragnode == null)
			throw new IllegalStateException("Unknown dragged node " + dragid + " in drop request to node=" + this);
		if(!(dragnode instanceof IDraggable))
			throw new IllegalStateException("The supposedly dragged node " + dragnode + " does not implement IDraggable!?");
		IDragHandler dragh = ((IDraggable) dragnode).getDragHandler();

		//-- First call the drag handler's DROPPED thingy
		int index = 0;
		String s = ctx.getParameter("_index");
		if(s != null) {
			try {
				index = Integer.parseInt(s.trim());
			} catch(Exception x) {
				throw new IllegalStateException("Bad _index parameter in DROP request: " + s);
			}
		}
		DropEvent dx = new DropEvent((NodeContainer) this, dragnode, index);
		dragh.onDropped(dx);
		droph.onDropped(dx);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IModelBinding implementation.						*/
	/*--------------------------------------------------------------*/
	/**
	 * EXPERIMENTAL - DO NOT USE.
	 * For non-input non-container nodes this does exactly nothing.
	 * @see to.etc.domui.component.form.IModelBinding#moveControlToModel()
	 */
	public void moveControlToModel() throws Exception {
		build();
		Object v = this; // Silly: Eclipse compiler has bug - it does not allow this in instanceof because it incorrecly assumes 'this' is ALWAYS of type NodeBase - and it it not.
		if(v instanceof IBindable) {
			IBindable b = (IBindable) v;
			if(b.isBound())
				b.bind().moveControlToModel();
		}
	}

	/**
	 * EXPERIMENTAL - DO NOT USE.
	 * For non-input non-container nodes this does exactly nothing.
	 * @see to.etc.domui.component.form.IModelBinding#moveModelToControl()
	 */
	public void moveModelToControl() throws Exception {
		build();
		Object v = this; // Silly: Eclipse compiler has bug - it does not allow this in instanceof because it incorrecly assumes 'this' is ALWAYS of type NodeBase - and it it not.
		if(v instanceof IBindable) {
			IBindable b = (IBindable) v;
			if(b.isBound())
				b.bind().moveModelToControl();
		}
	}

	/**
	 * EXPERIMENTAL - DO NOT USE.
	 * For non-input non-container nodes this does exactly nothing.
	 *
	 * @see to.etc.domui.component.form.IModelBinding#setControlsEnabled(boolean)
	 */
	public void setControlsEnabled(boolean on) {
		try {
			build();
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
		Object v = this; // Silly: Eclipse compiler has bug - it does not allow this in instanceof because it incorrecly assumes 'this' is ALWAYS of type NodeBase - and it it not.
		if(v instanceof IBindable) {
			IBindable b = (IBindable) v;
			if(b.isBound())
				b.bind().setControlsEnabled(on);
		}
	}
}
