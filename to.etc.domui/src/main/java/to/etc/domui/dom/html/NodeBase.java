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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.component.binding.ComponentPropertyBinding;
import to.etc.domui.component.binding.IBinding;
import to.etc.domui.component.binding.OldBindingHandler;
import to.etc.domui.component.event.INotify;
import to.etc.domui.component.image.Dimension;
import to.etc.domui.component.layout.Rect;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.dom.HtmlTagRenderer;
import to.etc.domui.dom.css.CssBase;
import to.etc.domui.dom.css.PositionType;
import to.etc.domui.dom.errors.IErrorFence;
import to.etc.domui.dom.errors.INodeErrorDelegate;
import to.etc.domui.dom.errors.MsgType;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.Page.NotificationListener;
import to.etc.domui.dom.webaction.IWebActionHandler;
import to.etc.domui.logic.ILogicContext;
import to.etc.domui.parts.IComponentJsonProvider;
import to.etc.domui.parts.IComponentUrlDataProvider;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.RequestContextImpl;
import to.etc.domui.state.IPageParameters;
import to.etc.domui.state.UIContext;
import to.etc.domui.trouble.UIException;
import to.etc.domui.util.Constants;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.DropEvent;
import to.etc.domui.util.IDragArea;
import to.etc.domui.util.IDragHandler;
import to.etc.domui.util.IDraggable;
import to.etc.domui.util.IDropHandler;
import to.etc.domui.util.IDropTargetable;
import to.etc.domui.util.IExecute;
import to.etc.domui.util.javascript.JavascriptStmt;
import to.etc.webapp.nls.BundleStack;
import to.etc.webapp.nls.IBundle;
import to.etc.webapp.query.QDataContext;
import to.etc.webapp.query.QDataContextFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 18, 2007
 */
abstract public class NodeBase extends CssBase implements INodeErrorDelegate {
	private static final Logger LOG = LoggerFactory.getLogger(NodeBase.class);

	static private boolean m_logAllocations;

	static private int m_nextID;

	/** The owner page. If set then this node IS attached to the parent in some way; if null it is not attached. */
	@Nullable
	private Page m_page;

	@Nonnull
	private String m_tag;

	@Nullable
	private String m_cssClass;

	/** This is the actual ID of the node IF the framework decided to override the specified ID (or if no ID was assigned). */
	@Nullable
	private String m_actualID;

	private String m_testID;

	@Nullable
	private NodeContainer m_parent;

	@Nullable
	private IClickBase< ? > m_clicked;

	private boolean m_built;

	/** T when this node's html/style attributes changed. */
	private boolean m_attributesChanged;

	@Nullable
	private NodeContainer m_oldParent;

	/** Helper variable containing this-node's index in the output tree while calculating the delta */
	int m_oldNodeIndex;

	int m_newNodeIndex;

	int m_origNewIndex;

	@Nullable
	private Object m_userObject;

	@Nullable
	private String m_title;

	@Nullable
	private String m_onClickJS;

	@Nullable
	private String m_onMouseDownJS;

	@Nullable
	private StringBuilder m_appendJS;

	@Nullable
	private StringBuilder m_createJS;

	@Nullable
	private List<String> m_specialAttributes;

	static private final byte F_FOCUSREQUESTED = 0x01;

	static private final byte F_BUNDLEFOUND = 0x02;

	static private final byte F_BUNDLEUSED = 0x04;

	private byte m_flags;

	private StackTraceElement[] m_allocationTracepoint;

	/**
	 * If marked as stretched, element gets attribute stretched. It would be used on client side to adjust its height to all available space in parent element (what is left when other siblings take their pieces)
	 */
	private boolean m_stretchHeight;

	private String m_calculatedTestIdBase;

	private String m_testFullRepeatID;

	private String m_testRepeatId;

	@Nullable
	private List<IBinding> m_bindingList;

	@Nullable
	private String m_overrideTitle;

	@Nullable
	private INotify<NodeBase> m_onSizeAndPositionChange;

	@Nullable
	private Rect m_clientBounds;

	@Nullable
	private Dimension m_browserWindowSize;

	private byte m_disableChanged;

	/**
	 * This must visit the appropriate method in the node visitor. It should NOT recurse it's children.
	 * @param v
	 * @throws Exception
	 */
	abstract public void visit(@Nonnull INodeVisitor v) throws Exception;

	protected NodeBase(@Nonnull final String tag) {
		m_tag = tag;
		if(m_logAllocations) {
			m_allocationTracepoint = DomUtil.getTracepoint();
		}
	}

	/**
	 * Internal use only. Explicitly unsynchronized.
	 * @param la
	 */
	static public void internalSetLogAllocations(boolean la) {
		m_logAllocations = la;
	}

	public StackTraceElement[] getAllocationTracepoint() {
		return m_allocationTracepoint;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Private interfaces and code.						*/
	/*--------------------------------------------------------------*/
	/**
	 * INTERNAL ONLY Set when this node has changed attributes. Does not include child changes.
	 * @return
	 */
	final public boolean internalHasChangedAttributes() {
		return m_attributesChanged;
	}

	/**
	 * Internal only.
	 * @param d
	 */
	final public void internalSetHasChangedAttributes(final boolean d) {
		m_attributesChanged = d;
	}

	/**
	 * Internal use only.
	 */
	final public void internalSetHasChangedAttributes() {
		internalSetHasChangedAttributes(true);
	}

	/**
	 * Internal, throws an exception if we have changed attributes.
	 */
	void internalCheckNotDirty() {
		if(internalHasChangedAttributes())
			throw new IllegalStateException("The node " + this + " has DIRTY ATTRIBUTES set");
	}

	/**
	 * Internal, do the proper run sequence for a clicked event.
	 * @throws Exception
	 */
	public void internalOnClicked(@Nonnull ClickInfo cli) throws Exception {
		IClickBase<NodeBase> c = (IClickBase<NodeBase>) getClicked();
		if(c instanceof IClicked< ? >) {
			((IClicked<NodeBase>) c).clicked(this);
		} else if(c instanceof IClicked2< ? >) {
			((IClicked2<NodeBase>) c).clicked(this, cli);
		} else
			throw new IllegalStateException("? Node " + this.getActualID() + " does not have a (valid) click handler??");
	}

	/**
	 * Called on stylesheet changes. Clears the style cache.
	 *
	 * @see to.etc.domui.dom.css.CssBase#changed()
	 */
	@Override
	final protected void changed() {
		if(m_disableChanged > 0)
			return;
		setCachedStyle(null);
		internalSetHasChangedAttributes();
		NodeContainer p = m_parent;
		if(p != null)
			p.childChanged(); 									// Indicate child has changed
		super.changed();
	}

	/**
	 * Call anything inside the thingy, and force the node to remain unchanged.
	 * @param exec
	 */
	final public void unchanged(IExecute exec) throws Exception {
		m_disableChanged++;
		exec.execute();
		m_disableChanged--;
	}


	/**
	 * INTERNAL USE ONLY Changes the OLD PARENT pointer. THIS FORCES A "set", and validates the pointer
	 * by setting the updateNumber equal to the page's update#.
	 * @param c
	 */
	final void internalSetOldParent(final NodeContainer c) {
		m_oldParent = c;
	}

	final public NodeContainer internalGetOldParent() {
		return m_oldParent;
	}

	public void internalClearDelta() {
		m_oldParent = null;
		internalSetHasChangedAttributes(false);
	}

	public void internalClearDeltaFully() {
		internalClearDelta();
	}

	protected int internalGetNodeCount(int depth) {
		return 1;
	}

	/**
	 * Calculates a new ID for a node.
	 * @return
	 */
	@Nonnull
	final String nextUniqID() {
		StringBuilder sb = new StringBuilder();
		sb.append("__");								// Id MUST start with _, and use __ to ensure id does not overlap with Page#nextId
		int id = nextIdNumber();
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

	private synchronized int nextIdNumber() {
		return m_nextID++;
	}

	/**
	 * When the node is attached to a page this returns the ID assigned to it. To call it before
	 * is an error and throws IllegalStateException.
	 * @return
	 */
	@Nonnull
	final public String getActualID() {
		String id = m_actualID;
		if(null == id) {
			id = m_actualID = nextUniqID();
		}
		return id;
	}

	@Nullable
	final String internalGetID() {
		return m_actualID;
	}

	/**
	 * Internal use only: set the assigned id.
	 * @param actualID
	 */
	final void setActualID(@Nonnull final String actualID) {
		m_actualID = actualID;
	}

	/**
	 * Return the node's tag name (the html tag this node represents).
	 * @return
	 */
	@Nonnull
	final public String getTag() {
		return m_tag;
	}

	/**
	 * INTERNAL USE ONLY, FOR SPECIAL CASES!!!! Node tags may NEVER change once rendered to the browser.
	 * @param tag
	 */
	final protected void internalSetTag(@Nonnull final String tag) {
		m_tag = tag;
	}

	/**
	 * Return the Page for this node, if attached, or null otherwise.
	 * @return
	 */
	@Nonnull
	final public Page getPage() {
		if(null != m_page)
			return m_page;
		throw new IllegalStateException(getTag() + " with title " + getTitle() + " and id " + getActualID() + " not attached to a page yet. Use isAttached() to check if a node is attached or not.");
	}

	/**
	 * Returns T if this node is attached to a real page.
	 * @return
	 */
	final public boolean isAttached() {
		return null != m_page;
	}

	/**
	 * Internal use only.
	 * @param page
	 */
	final void setPage(final Page page) {
		m_page = page;
	}

	/**
	 * Internal use: remove registration.
	 */
	void unregisterFromPage() {
		if(!isAttached())
			return;
		clearMessage(); // jal 20091015 Remove any pending messages for removed nodes.
		getPage().unregisterNode(this);
	}

	/**
	 * Internal: register this node with the page.
	 * @param p
	 */
	void registerWithPage(@Nonnull final Page p) {
		p.registerNode(this);
	}

	void internalOnAddedToPage(final Page p) {
		onAddedToPage(p);
		List<NotificationListener<?>> list = m_notificationListenerList;
		if(null != list) {
			list.forEach(a -> p.addNotificationListener(a));
		}
		StringBuilder appendJS = m_appendJS;
		if(appendJS != null) {
			getPage().appendJS(appendJS);
			m_appendJS = null;
		}
	}

	void internalOnRemoveFromPage(final Page p) {
		onRemoveFromPage(p);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Utility functions to work with the 'class' attr.	*/
	/*--------------------------------------------------------------*/
	/**
	 * Return the full "class" (css class) attribute for this node.
	 */
	@Nullable
	final public String getCssClass() {
		return m_cssClass;
	}

	/**
	 * Set the value for the "class" (css class) attribute. This can be null, or one or
	 * more class names separated by space.
	 * @param cssClass
	 */
	public void setCssClass(@Nullable final String cssClass) {
		//		System.out.println("--- id="+m_actualID+", css="+cssClass);
		if(!DomUtil.isEqual(cssClass, m_cssClass))
			changed();
		m_cssClass = cssClass;
	}

	/**
	 * Removes the specified CSS class. This looks in the space delimited list and removes all 'words' there
	 * that match this name. Returns T if the class was actually present.
	 * @param name
	 * @return
	 */
	final public boolean removeCssClass(@Nonnull final String name) {
		String cssClass = getCssClass();
		if(cssClass == null)
			return false;
		String[] split = cssClass.split("\\s+");
		StringBuilder sb = new StringBuilder(cssClass.length());
		boolean fnd = false;
		for(String s: split) {
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

	/**
	 * Add the class passed as <i>another</i> CSS class to the "class" attribute. If the class already
	 * contains class names this one is added separated by space.
	 * @param name
	 */
	final public void addCssClass(@Nonnull final String nameList) {
		String cssClass = getCssClass();
		if(cssClass == null) {
			setCssClass(nameList);
			return;
		}

		String[] names = nameList.split("\\s+");

		String[] split = cssClass.split("\\s+");
		for(String s: split) {
			for(int i = 0; i < names.length; i++) {
				String name = names[i];
				if(s.equals(name)) {
					names[i] = null;				// Already there
				}
			}
		}
		StringBuilder sb = new StringBuilder(cssClass);
		for(int i = 0; i < names.length; i++) {
			String name = names[i];
			if(null != name) {
				if(sb.length() > 0)
					sb.append(' ');
				sb.append(name);
			}
		}
		setCssClass(sb.toString());
	}

	/**
	 * Returns T if the css class passed is present in the current cssclass.
	 * @param cls
	 * @return
	 */
	final public boolean hasCssClass(@Nonnull final String cls) {
		String cssClass = getCssClass();
		if(cssClass == null)
			return false;
		int pos = cssClass.indexOf(cls);
		if(pos == -1)
			return false;
		return pos == 0 || cssClass.charAt(pos - 1) == ' ';
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Parent node handling.								*/
	/*--------------------------------------------------------------*/
	/**
	 * FIXME NEED TO BE CHANGED - LOGIC MUST MOVE TO CONTAINER.
	 * @param parent
	 */
	final void setParent(final NodeContainer parent) {
		if(m_oldParent == null) // jal 20090115 Was !=, seems very wrong and the cause of the "Hell Freezeth over" exception..
			m_oldParent = m_parent;
		m_parent = parent;
	}

	/**
	 * Return the current actual parent of this node. Throws exception if not attached.
	 * @return
	 */
	@Nonnull
	final public NodeContainer getParent() {
		if(null != m_parent)
			return m_parent;
		if(m_page != null && m_page.getBody() == this)
			throw new IllegalStateException("Calling getParent() on the body tag is an indication of a problem...");
		throw new IllegalStateException("The node is not attached to a page, call isAttached() to test for attachment");
	}

	@Nullable
	public NodeContainer internalGetParent() {
		return m_parent;
	}

	public final boolean hasParent() {
		return m_parent != null;
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
	@Nullable
	final public NodeContainer getParent(int up) {
		NodeBase c = m_parent;
		while(--up > 0) {
			if(c == null)
				return null;
			c = c.m_parent;
		}
		return (NodeContainer) c;
	}

	/**
	 * Walk the parents upwards to find the closest parent of the given class. The class can be a base class (it is
	 * not a literal match but an instanceof match).
	 * @param <T>
	 * @param clz
	 * @return
	 */
	@Nullable
	final public <T> T findParent(final Class<T> clz) {
		if(!hasParent())
			return null;
		NodeBase c = this;
		for(;;) {
			if(!c.hasParent())
				return null;
			c = c.getParent();
			if(clz.isAssignableFrom(c.getClass()))
				return (T) c;
		}
	}

	/**
	 * Walk the parents upwards to find the closest parent of the given class. The class can be a base class (it is
	 * not a literal match but an instanceof match). This throws an exception when the parent cannot be found(!).
	 * @param <T>
	 * @param clz
	 * @return
	 */
	@Nonnull
	final public <T> T getParent(final Class<T> clz) {
		T res = findParent(clz);
		if(null == res)
			throw new IllegalStateException("This node " + this + " does not have a parent of type=" + clz);
		return res;
	}

	/**
	 * Walk the parent chain upwards, and find the first parent that implements <i>any</i> of
	 * the classes passed.
	 * @param clzar
	 * @return
	 */
	@Nullable
	final public NodeBase getParentOfTypes(final Class< ? extends NodeBase>... clzar) {
		NodeBase c = this;
		for(;;) {
			if(!c.hasParent())
				return null;
			c = c.getParent();

			for(Class< ? > clz : clzar) {
				if(clz.isAssignableFrom(c.getClass()))
					return c;
			}
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Tree manipulation.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Disconnect this node from it's parent. The node can be reconnected to another parent
	 * afterwards.
	 */
	final public void remove() {
		if(m_parent != null)
			m_parent.removeChild(this);
	}

	/**
	 * Replace <i>this<i> node in it's parent with the node passed. This node becomes
	 * unattached to the tree and can be reused. The new node takes the exact position
	 * of this node in the tree.
	 * @param nw
	 */
	final public void replaceWith(final NodeBase nw) {
		getParent().replaceChild(this, nw);
	}

	/**
	 * Add the node passed <i>immediately after</i> this node in the tree.
	 * @param item
	 */
	final public void appendAfterMe(@Nonnull final NodeBase item) {
		int ix = getParent().findChildIndex(this);
		if(ix == -1) {
			throw new IllegalStateException("!@?! Cannot find myself!?");
		}
		getParent().undelegatedAdd(ix + 1, item);
	}

	/**
	 * Add the node passed <i>immediately before</i> this node in the tree.
	 * @param item
	 */
	final public void appendBeforeMe(@Nonnull final NodeBase item) {
		int ix = getParent().findChildIndex(this);
		if(ix == -1)
			throw new IllegalStateException("!@?! Cannot find myself!?");
		getParent().undelegatedAdd(ix, item);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Building the node's content.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Not normally called from outside, this forces the node to call createContent if needed (if unbuilt).
	 * FIXME Should probably become internal.
	 * @throws Exception
	 */
	final public void build() throws Exception {
		//		Page pg = m_page;
		//		if(pg != null)
		//			pg.inBuild();									// jal 20131206 Test checked phase handling

		if(!m_built) {
			m_built = true;
			boolean ok = false;
			try {
				internalCreateContent();
				ok = true;
			} finally {
				m_built = ok;
			}

			// 20100504 jal If createContent throws an exception the node is not built but can contain data, causing duplicates in the page if rebuilt.
			if(!ok)
				forceRebuild();
		}
	}

	/**
	 * Force this node to be rebuilt by fully clearing all it's content (removing all children). Use this to force
	 * a component to redraw itself fully, for instance after it's state or content changes.
	 */
	@OverridingMethodsMustInvokeSuper
	public void forceRebuild() {
		onForceRebuild(); // Call event handler.
		clearBuilt();
	}

	/**
	 * Internal.
	 */
	final private void clearBuilt() {
		m_built = false;
		if(m_page != null)
			m_page.internalAddPendingBuild(this);
	}

	/**
	 * Returns T if the node's content has been built (createContent() has been called).
	 * @return
	 */
	public boolean isBuilt() {
		return m_built;
	}

	private final void internalCreateContent() throws Exception {
		beforeCreateContent();
		internalCreateFrame();
		createContent();
		afterCreateContent();
	}

	/**
	 * This method is a placeholder for NodeContainer which allows it to handle
	 * framed windows somehow.
	 */
	protected void internalCreateFrame() throws Exception {}

	/*--------------------------------------------------------------*/
	/*	CODING:	Simple other getter and setter like stuff.			*/
	/*--------------------------------------------------------------*/
	/**
	 * Set the title attribute, using tilde replacement. If the string starts with a ~ it is
	 * assumed to be a key into the page's resource bundle.
	 *
	 * @param title
	 */
	public void setTitle(@Nullable final String title) {
		if(!DomUtil.isEqual(title, m_title))
			changed();
		m_title = title;
	}

	/**
	 * Returns the title <i>as set</i> verbatim; if it was set using a tilde key this returns the <i>key</i> without resource bundle replacement.
	 * @return
	 */
	@Nullable
	public String getTitle() {
		return m_overrideTitle == null ? m_title : m_overrideTitle;
	}

	public void setOverrideTitle(@Nullable String overrideTitle) {
		if(Objects.equals(overrideTitle, m_overrideTitle))
			return;
		m_overrideTitle = overrideTitle;
		changed();
	}


	/**
	 * Return the click handler for this node, or null if none is associated with it.
	 * @return
	 */
	@Nullable
	public IClickBase< ? > getClicked() {
		return m_clicked;
	}

	/**
	 * Set a click handler for this node. This will be attached to the Javascript "onclick" handler for
	 * this node and will fire when the node is clicked. If more information around the click is needed
	 * use {@link #setClicked2(IClicked2)}.
	 * @param clicked
	 */
	public void setClicked(@Nullable final IClicked< ? > clicked) {
		m_clicked = clicked;
	}

	/**
	 * Set an advanced click handler for this node, which gets passed a {@link ClickInfo}
	 * structure containing the location of the click and the other modifier buttons pressed
	 * at click time. Only one of setClicked / setClicked2 can be active at any one time.
	 */
	public void setClicked2(IClicked2<?> clicked) {
		m_clicked = clicked;
	}

	/**
	 * Mostly internal only: override when this component has a clicked handler which must <i>not</i> be
	 * rendered as a Javascript "onclick". For instance the LookupForm returns false for this, so
	 * that it can override the "clicked" property to be called when the lookupform's SEARCH button
	 * is pressed.
	 * @return
	 */
	public boolean internalNeedClickHandler() {
		return getClicked() != null;
	}

	/**
	 * Get whatever user object is set into this node as set by {@link #setUserObject(Object)}.
	 * @return
	 */
	@Nullable
	public Object getUserObject() {
		return m_userObject;
	}

	/**
	 * Set some user object into this node.
	 * @param userObject
	 */
	public void setUserObject(@Nullable final Object userObject) {
		m_userObject = userObject;
	}

	/*----------- Test IDs -------------*/
	/**
	 * When set this causes a "testid" attribute to be rendered on the node. This ID can then be used for selenium tests et al.
	 * @return
	 */
	public String getTestID() {
		return m_testID;
	}

	/**
	 * Set an ID that can be used for finding this node in the HTML using test software. The test id is rendered as a "testid" attribute.
	 * @param testID
	 */
	public void setTestID(String testID) {
		if(DomUtil.isEqual(testID, m_testID))
			return;
		m_testID = testID;
		changed();
	}

	public void setCalculcatedId(@Nonnull String calcid) {
		m_calculatedTestIdBase = DomUtil.convertToID(calcid);
	}

	public void setCalculcatedId(@Nonnull String calcid, @Nullable String parentId) {
		try {
			String base = getTestRepeatId();
			Page page = m_page;
			if(parentId != null && page != null) {
				String nid = base + "/" + calcid;
				if(page.isTestIDAllocated(nid)) {
					setCalculcatedId(parentId + "_" + calcid);
				} else {
					setCalculcatedId(calcid);
				}
			} else {
				setCalculcatedId(calcid);
			}
		} catch(Exception ex) {
			//FIXME: 20130926 vmijic : for now try less intrusive error reporting in problems with generating calculated testid.
			LOG.error("Error in setCalculcatedId", ex);
			setCalculcatedId("ERROR_IN_setCalculcatedId_" + calcid);
		}
	}


	/**
	 * This can be overridden for items that are supposed to be found for testing.
	 * @return
	 */
	@Nullable
	protected String getCalculatedTestID() {
		return m_calculatedTestIdBase;
	}

	@Nullable
	public String calcTestID() {
		Page page = m_page;
		if(null == page)
			return null;
		String baseName = getTestID();
		if(null != baseName)
			return baseName;
		baseName = getCalculatedTestID();
		if(null == baseName)
			return null;
		String repeatId = getTestRepeatId();
		return m_testID = page.allocateTestID(repeatId + baseName);
	}

	/**
	 * EXPERIMENTAL: If this is part of some "repeating" structure this must hold a repeat ID, which is a
	 * page-unique id for the repeating thing.
	 * @return
	 */
	@Nonnull
	public String getTestRepeatId() {
		if(m_testFullRepeatID == null) {
			NodeContainer parent = m_parent;
			if(parent == null) {
				throw new IllegalStateException("?? " + getClass().getName() + " null parent");
			}
			String ptrid = parent.getTestRepeatId();
			if(m_testRepeatId == null) {
				m_testFullRepeatID = ptrid;
			} else {
				m_testFullRepeatID = ptrid + "/" + m_testRepeatId + "/";
			}
		}
		return m_testFullRepeatID;
	}

	public void setTestRepeatID(@Nonnull String trid) {
		m_testRepeatId = trid;
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
	public void appendJavascript(@Nonnull final CharSequence js) {
		StringBuilder sb = getAppendJavascriptBuffer();
		sb.append(';');
		sb.append(js);
	}

	@Nonnull
	public JavascriptStmt appendStatement() {
		return new JavascriptStmt(getAppendJavascriptBuffer());
	}

	@Nonnull
	private StringBuilder getAppendJavascriptBuffer() {
		if(isAttached())
			return getPage().internalGetAppendJS();
		StringBuilder sb = m_appendJS;
		if(sb == null)
			sb = m_appendJS = new StringBuilder(128);
		return sb;
	}

	/**
	 * This js general custom updates contributor callback (custom updares are triggered after any page load or ajax response handling), function gets wrapped around specified js.
	 * @param jsCallback
	 */
	public void appendJsCustomUpdatesContributor(@Nonnull String jsCallback){
		appendJavascript("WebUI.registerCustomUpdatesContributor(function(){" + jsCallback + "});");
	}

	/**
	 * This is js CustomUpdatesContributor callback used to execute WebUI.showOverflowTextAsTitle, function that decorates title on elements selected by specified selector, under all children of an element.
	 * NOTE: This node element needs to be added on page (has to have actualID assigned).
	 * @param cssSelector
	 */
	public void appendShowOverflowTextAsTitleJs(@Nonnull String cssSelector){
		appendJsCustomUpdatesContributor("WebUI.showOverflowTextAsTitle('" + getActualID() + "', '" + cssSelector + "')");
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
	public void appendCreateJS(@Nonnull final CharSequence js) {
		int len = js.length();
		if(len == 0)
			return;
		StringBuilder sb = getCreateJavascriptBuffer();
		sb.append(js);
		if(js.charAt(len - 1) != ';')
			sb.append(';');
	}

	@Nonnull
	private StringBuilder getCreateJavascriptBuffer() {
		StringBuilder sb = m_createJS;
		if(sb == null)
			sb = m_createJS = new StringBuilder();
		else {
			JavascriptStmt st = m_createStmt;
			if(null != st)
				st.next();
		}
		return sb;
	}

	@Nullable
	private JavascriptStmt m_createStmt;

	@Nonnull
	public JavascriptStmt createStatement() {
		JavascriptStmt st = m_createStmt;
		if(null == st) {
			st = m_createStmt = new JavascriptStmt(getCreateJavascriptBuffer());
		}
		return st;
	}

	@Nullable
	public StringBuilder getCreateJS() {
		JavascriptStmt st = m_createStmt;
		if(null != st)
			st.next();
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
	 * @param b
	 * @throws Exception
	 */
	protected void renderJavascriptState(@Nonnull JavascriptStmt b) throws Exception {
	}

	final public void internalRenderJavascriptState(@Nonnull JavascriptStmt stmt) throws Exception {
		renderJavascriptState(stmt);
		stmt.next();
	}


	/**
	 * This marks this component as having "changed" javascript state. It will
	 * cause the node's
	 */
	final public void changedJavascriptState() {
		Page page = m_page;
		if(null != page)
			page.registerJavascriptStateChanged(this);
	}

	protected void renderJavascriptDelta(@Nonnull JavascriptStmt b) throws Exception {

	}

	final public void internalRenderJavascriptDelta(@Nonnull JavascriptStmt stmt) throws Exception {
		renderJavascriptDelta(stmt);
		stmt.next();
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
	public void setSpecialAttribute(@Nonnull final String name, @Nullable final String value) {
		List<String> sa = m_specialAttributes;
		if(sa == null) {
			sa = m_specialAttributes = new ArrayList<String>(5);
		} else {
			for(int i = 0; i < sa.size(); i += 2) {
				if(sa.get(i).equals(name)) {
					if(value == null) {
						sa.remove(i);
						sa.remove(i);
						return;
					}
					sa.set(i + 1, value);
					changed();
					return;
				}
			}
		}
		sa.add(name);
		sa.add(value);
		changed();
	}

	/**
	 * Return the list of special attributes and their value. The even index retrieves
	 * the name, the odd index it's value. See {@link #setSpecialAttribute(String, String)} for
	 * details.
	 * FIXME This should return a copy, not the actual attributes.
	 *
	 * @return
	 */
	@Nullable
	public List<String> getSpecialAttributeList() {
		return m_specialAttributes;
	}

	/**
	 * Return the value for the "special" attribute with the specified name, if present.
	 * @param name
	 * @return
	 */
	@Nullable
	public String getSpecialAttribute(@Nonnull final String name) {
		List<String> attributes = m_specialAttributes;
		if(attributes != null) {
			for(int i = 0; i < attributes.size(); i += 2) {
				if(attributes.get(i).equals(name))
					return attributes.get(i + 1);
			}
		}
		return null;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Getting data from a component from Javascript.		*/
	/*--------------------------------------------------------------*/
	/**
	 * Return an URL to a data call on this node. The call must be found by the {@link #componentHandleWebDataRequest(RequestContextImpl, String)}
	 * method, so there should be a handler.
	 * @param pp
	 * @return
	 */
	@Nonnull
	public String getComponentDataURL(@Nonnull String action, @Nullable IPageParameters pp) {
		NodeBase nb = this;
		return DomUtil.getAdjustedComponentUrl(this, "#" + action, pp);
	}

	/**
	 * Default handling for webui AJAX actions to a component.
	 * Handles {@link Constants#ACMD_NOTIFY_CLIENT_POSITION_AND_SIZE} as command from client (browser).
	 * @param ctx
	 * @param action
	 * @throws Exception
	 */
	public void componentHandleWebAction(@Nonnull final RequestContextImpl ctx, @Nonnull String action) throws Exception {
		if("WEBUIDROP".equals(action)) {
			handleDrop(ctx);
			return;
		} else if(Constants.ACMD_NOTIFY_CLIENT_POSITION_AND_SIZE.equals(action)) {
			handleClientPositionAndSizeChange(ctx);
			return;
		}
		if(action.endsWith("?"))
			action = action.substring(0, action.length() - 1);
		action = "webAction" + action;

		IWebActionHandler handler = ctx.getApplication().getWebActionRegistry().findActionHandler(getClass(), action);
		if(null != handler) {
			handler.handleWebAction(this, ctx, false);
			return;
		}
		throw new IllegalStateException("The component " + this + " does not accept the web action " + action);
	}

	/**
	 * Out-of-bound data request for a component. This is not allowed to change the state of the tree as no delta
	 * response will be returned. The action itself must decide on a response.
	 * @param ctx
	 * @param action
	 * @throws Exception
	 */
	public void componentHandleWebDataRequest(@Nonnull final RequestContextImpl ctx, @Nonnull String action) throws Exception {
		action = "webData" + action;

		IWebActionHandler handler = ctx.getApplication().getWebActionRegistry().findActionHandler(getClass(), action);
		if(null != handler) {
			handler.handleWebAction(this, ctx, true);
			return;
		}
		throw new IllegalStateException("The component " + this + " does not accept the web data request #" + action);
	}

	public boolean acceptRequestParameter(@Nonnull final String[] values) throws Exception {
		throw new IllegalStateException("?? The '" + getTag() + "' component (" + this.getClass() + ") with id=" + m_actualID + " does NOT accept input!");
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Getting data from a component.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Return an URL to a data stream generator for this component. The component must implement
	 * {@link IComponentUrlDataProvider} to handle the data request.
	 * @param pp
	 * @return
	 */
	@Nonnull
	public String getComponentDataURL(@Nullable IPageParameters pp) {
		NodeBase nb = this;
		if(!(nb instanceof IComponentUrlDataProvider))
			throw new IllegalStateException("This component (" + this + ") does not implement " + IComponentUrlDataProvider.class.getName());
		return DomUtil.getAdjustedComponentUrl(this, Constants.ACMD_PAGEDATA, pp);
	}

	@Nonnull
	public String getComponentJSONURL(@Nullable IPageParameters pp) {
		NodeBase nb = this;
		if(!(nb instanceof IComponentJsonProvider))
			throw new IllegalStateException("This component (" + this + ") does not implement " + IComponentJsonProvider.class.getName());
		return DomUtil.getAdjustedComponentUrl(this, Constants.ACMD_PAGEJSON, pp);
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
	@Nullable
	private UIMessage m_message;

	/**
	 * When set this contains a user-understandable tekst indicating which control has the error. It usually contains
	 * the "label" associated with the control, and is set automatically by form builders if possible.
	 */
	@Nullable
	private String m_errorLocation;

	/**
	 * When set this contains a user-understandable tekst indicating which control has the error. It usually contains
	 * the "label" associated with the control, and is set automatically by form builders if possible.
	 * @param errorLocation
	 */
	public void setErrorLocation(@Nullable String errorLocation) {
		m_errorLocation = errorLocation;
	}

	/**
	 * When set this contains a user-understandable tekst indicating which control has the error. It usually contains
	 * the "label" associated with the control, and is set automatically by form builders if possible.
	 * @return
	 */
	@Nullable
	public String getErrorLocation() {
		return m_errorLocation;
	}

	public String	getComponentInfo() {
		StringBuilder sb = new StringBuilder();
		String s = getClass().getName();
		s = s.substring(s.lastIndexOf('.')+1);
		sb.append(s);

		String el = getErrorLocation();
		if(null != el) {
			sb.append(":").append(el);
		}
		ComponentPropertyBinding binding = OldBindingHandler.findBinding(this, "value");
		if(binding != null) {
			sb.append(" ").append(binding);
		} else {
			binding = OldBindingHandler.findBinding(this, "bindValue");
			if(binding != null) {
				sb.append(" ").append(binding);
			}
		}

		if(this instanceof NodeContainer) {
			String txt = DomUtil.calcNodeText((NodeContainer) this);
			if(txt.length() > 0)
				sb.append("/").append(txt);
		}

		return sb.toString();
	}

	/**
	 * This sets a message (an error, warning or info message) on this control. If the
	 * control already has an error then we check if the severity of the new error is
	 * higher than the severity of the existing one; only in that case will the error
	 * be removed. To clear the error message call clearMessage().
	 *
	 * @see to.etc.domui.dom.errors.INodeErrorDelegate#setMessage(to.etc.domui.dom.errors.UIMessage)
	 */
	@Override
	@Nullable
	public UIMessage setMessage(@Nullable final UIMessage msg) {
		//System.out.println("    setMessage '"+getComponentInfo() +"' ("+getActualID()+") to '"+msg+"'");

		//-- If this (new) message has a LOWER severity than the EXISTING message ignore this call and return the EXISTING message
		UIMessage old = m_message;
		if(old == msg)
			return old;

		if(msg != null) {
			if(old != null) {
				if(old.getType().getOrder() > msg.getType().getOrder()) {
					return m_message;
				}

				//-- If code, type and parameters are all equal just leave the existing message in-place
				if(old.equals(msg))
					return old;
			}

			//-- Update any error location.
			if(msg.getErrorLocation() == null)
				msg.setErrorLocation(m_errorLocation);
			msg.setErrorNode(this);
		}
		m_message = msg;										// ORDERED: important!

		//-- Broadcast the error through the tree
		if(m_page != null) {									// Fix for bug# 787: cannot locate error fence. Allow errors to be posted on disconnected nodes.
			IErrorFence fence = DomUtil.getMessageFence(this);	// Get the fence that'll handle the message by looking UPWARDS in the tree
			if(null != old)
				fence.removeMessage(old);
			if(null != msg)
				fence.addMessage(msg);
		}

		//-- Fire a change event
		return msg;
	}

	/**
	 * Deprecated: use {@link #setMessage(UIMessage)} with a null parameter.
	 * Remove this-component's "current" error message, if present.
	 */
	@Deprecated
	@Override
	public void clearMessage() {
		setMessage(null);
	}

	@Nullable
	@Override
	public UIMessage getMessage() {
		return m_message;
	}

	/**
	 * Return T if this node currently has an error associated with it.
	 * @return
	 */
	public boolean hasError() {
		UIMessage message = getMessage();
		return message != null && message.getType() == MsgType.ERROR;
	}

	public void appendTreeErrors(@Nonnull List<UIMessage> errorList) {
		UIMessage message = getMessage();
		if(null != message && message.getType() == MsgType.ERROR)
			errorList.add(message);
	}

	@Nonnull
	public List<UIMessage> getErrorList() {
		List<UIMessage> res = new ArrayList<UIMessage>();
		appendTreeErrors(res);
		return res;
	}

	/**
	 * This adds a message to the "global" message list. The message "percolates" upwards to the first parent that acts
	 * as an error message fence. That component will be responsible for rendering the error message at an appropriate
	 * location.
	 * @param m
	 */
	public UIMessage addGlobalMessage(UIMessage m) {
		IErrorFence fence = DomUtil.getMessageFence(this); // Get the fence that'll handle the message by looking UPWARDS in the tree
		fence.addMessage(m);
		return m;
	}

	public void clearGlobalMessage() {
		IErrorFence fence = DomUtil.getMessageFence(this); // Get the fence that'll handle the message by looking UPWARDS in the tree
		fence.clearGlobalMessages(null);
	}

	public void clearGlobalMessage(UIMessage m) {
		IErrorFence fence = DomUtil.getMessageFence(this); // Get the fence that'll handle the message by looking UPWARDS in the tree
		fence.removeMessage(m);
	}

	/**
	 * Delete all messages with the specified code (deprecated) or group name (see {@link UIMessage#getGroup()}).
	 * @param code
	 */
	public void clearGlobalMessage(final String code) {
		IErrorFence fence = DomUtil.getMessageFence(this); // Get the fence that'll handle the message by looking UPWARDS in the tree
		fence.clearGlobalMessages(code);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Internationalization helper methods.				*/
	/*--------------------------------------------------------------*/

	/**
	 * When known, this contains the bundle stack containing all message bundles for this class <b>and all it's
	 * parents</b>. It is looked up only once, if the F_BUNDLEFOUND flag is not yet set.
	 */
	private IBundle m_componentBundle;

	/**
	 * Set a message bundle for this component. This overrides any and all auto-lookup mechanism, and can
	 * only be used <i>before</i> the message bundle is ever used (any call to $(), findComponentBundle()
	 * and whatnot). Explicitly setting the bundle to null prevents any bundle lookup, and makes all bundle
	 * related calls fail.
	 *
	 * @param bundle
	 */
	final public void setComponentBundle(@Nullable IBundle bundle) {
		if(0 != (m_flags & F_BUNDLEUSED))
			throw new IllegalStateException("The component bundle can only be set BEFORE it is used.");
		m_componentBundle = bundle;
		m_flags |= F_BUNDLEFOUND; // Set the 'found' flag to prevent the bundle from being looked up.
	}

	/**
	 * Returns the component's message bundle stack. This is either explicitly set using {@link #setComponentBundle(IBundle)}, or
	 * the component searches for a stack of message bundles by locating [classname] and "message" bundles for itself and all
	 * it's parent classes. If no message bundles are found at all this will return null.
	 *
	 * @return
	 */
	@Nullable
	final public IBundle findComponentBundle() {
		if((m_flags & F_BUNDLEFOUND) == 0) { // Not looked up yet?
			m_componentBundle = BundleStack.createStack(getClass()); // Create the bundle stack for this component.
			m_flags |= F_BUNDLEFOUND;
		}
		m_flags |= F_BUNDLEUSED;
		return m_componentBundle;
	}

	/**
	 * Returns the component's message bundle stack. This is either explicitly set using {@link #setComponentBundle(IBundle)}, or
	 * the component searches for a stack of message bundles by locating [classname] and "message" bundles for itself and all
	 * it's parent classes. If no message bundles are found at all this throws an IllegalStateException.
	 *
	 * @return
	 */
	@Nonnull
	final public IBundle getComponentBundle() {
		IBundle b = findComponentBundle();
		if(null == b)
			throw new IllegalStateException("The component " + this.getClass() + " does not have any message bundle.");
		return b;
	}

	/**
	 * Translate the key passed into a message string, using the component's message bundle. See {@link #setComponentBundle(IBundle)} and
	 * {@link #findComponentBundle()} for details on how a component find it's messages.
	 * @param key
	 * @param param
	 * @return
	 */
	@Nonnull
	public String $(@Nonnull String key, Object... param) {
		IBundle br = getComponentBundle();
		if(key.startsWith("~")) // Prevent silly bugs.
			key = key.substring(1);
		return br.formatMessage(key, param);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Overridable event methods.							*/
	/*--------------------------------------------------------------*/

	protected void internalShelve() throws Exception {
		onShelve();
	}

	protected void internalUnshelve() throws Exception {
		onUnshelve();
	}

	/**
	 * Default handling of change messages.
	 * @throws Exception
	 */
	public void internalOnValueChanged() throws Exception {
		if(this instanceof IHasChangeListener) {
			IHasChangeListener chb = (IHasChangeListener) this;
			IValueChanged<NodeBase> vc = (IValueChanged<NodeBase>) chb.getOnValueChanged();
			if(vc != null) { // Well, other listeners *could* have changed this one, you know
				vc.onValueChanged(this);
			}
		}
	}

	/**
	 * Called when forceRebuild is done on this node.
	 */
	protected void onForceRebuild() {}

	protected void onShelve() throws Exception {}

	protected void onUnshelve() throws Exception {}

	protected void onRefresh() throws Exception {}

	/**
	 * Will be called just before "full render" starts. It gets called INSIDE the rendering
	 * loop, so only changes "below" this node will have an effect.. Better said: DO NOT CHANGE THE
	 * TREE, this should be an internal interface 8-/
	 * @throws Exception
	 */
	public void renderJavascriptState(StringBuilder sb) throws Exception {

	}

	/**
	 * Will be called just before "full render" starts. It gets called INSIDE the rendering
	 * loop, so only changes "below" this node will have an effect.. Better said: DO NOT CHANGE THE
	 * TREE, this should be an internal interface 8-/
	 * @throws Exception
	 */
	public void onBeforeFullRender() throws Exception {}

	/**
	 * Called before rendering starts. All "actions" have executed. This executes before {@link #onBeforeFullRender()} and
	 * is safe to use.
	 * @throws Exception
	 */
	public void onBeforeRender() throws Exception {}

	@OverridingMethodsMustInvokeSuper
	protected void beforeCreateContent() {}

	public void createContent() throws Exception {}

	protected void afterCreateContent() throws Exception {}

	@OverridingMethodsMustInvokeSuper
	public void onAddedToPage(final Page p) {}

	@OverridingMethodsMustInvokeSuper
	public void onRemoveFromPage(final Page p) {}

	public void onHeaderContributors(final Page page) {}

	public void internalOnBeforeRender() throws Exception {
		onBeforeRender();
	}

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

		IDragHandler dragh;
		if(dragnode instanceof IDragArea)
			dragh = ((IDragArea) dragnode).getDragHandle().getDragHandler();
		else if(!(dragnode instanceof IDraggable))
			throw new IllegalStateException("The supposedly dragged node " + dragnode + " does not implement IDraggable!?");
		else
			dragh = ((IDraggable) dragnode).getDragHandler();

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
		int colIndex = 0;
		s = ctx.getParameter("_colIndex");
		if(s != null) {
			try {
				colIndex = Integer.parseInt(s.trim());
			} catch(Exception x) {
				throw new IllegalStateException("Bad _colIndex parameter in DROP request: " + s);
			}
		}
		String nextSiblingId = ctx.getParameter("_siblingId");
		String dropContainerId = ctx.getParameter("_dropContainerId");
		String mode = ctx.getParameter("_mode");

		DropEvent dx = null;
		if(DropMode.DIV.name().equals(mode)) {
			if(dropContainerId == null) {
				throw new IllegalStateException("Missing drop container is (_dropContainerId) in DIV drop request to node=" + this);
			}
			NodeBase dropContainer = getPage().findNodeByID(dropContainerId);
			if(dropContainer == null) {
				throw new IllegalStateException("Unknown drop container node " + dropContainerId + " in drop request to node=" + this);
			}
			dx = new DropEvent((NodeContainer) dropContainer, dragnode, nextSiblingId);
		} else {
			dx = new DropEvent((NodeContainer) this, dragnode, index, colIndex);
		}
		dragh.onDropped(dx);
		droph.onDropped(dx);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Miscellaneous.										*/
	/*--------------------------------------------------------------*/
	/**
	 * This returns the default "shared context" for database access.
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	public QDataContext getSharedContext() throws Exception {
		return getParent().getSharedContext();								// Delegate getting the "default context" to the parent node.
	}

	@Nonnull
	public QDataContextFactory getSharedContextFactory() {
		return getParent().getSharedContextFactory();
	}

	/**
	 * Get the context.
	 * @return
	 */
	@Nonnull
	public ILogicContext lc() throws Exception {
		return getPage().getBody().lc();
	}

	/**
	 * Claim the focus for this component. Only reasonable for input type and action
	 * components (links, buttons). For now this can only be called for components that
	 * are already attached to a page. If this is a proble I'll fix it. Only one component
	 * in a page can claim focus for itself.
	 */
	public void setFocus() {
		if(!isAttached()) {
			//-- Mark this as a component wanting the focus.
			m_flags |= F_FOCUSREQUESTED;
		} else
			getPage().setFocusComponent(this);
	}

	/**
	 * Returns T if this control has requested the focus.
	 * @return
	 */
	final public boolean isFocusRequested() {
		return (m_flags & F_FOCUSREQUESTED) != 0;
	}

	/**
	 * Reset the "request focus" flag.
	 */
	final public void clearFocusRequested() {
		m_flags &= F_FOCUSREQUESTED;
	}

	public final void refresh() throws Exception {
		onRefresh();
	}

	public boolean isFocusable() {
		NodeBase n = this;
		if(n instanceof IHasChangeListener) { // FIXME Why this 'if'?
			if(n instanceof IControl< ? >) {
				IControl< ? > in = (IControl< ? >) n;
				return !in.isDisabled() && !in.isReadOnly();
			} else
				return true;
		}
		return false;
	}

	/**
	 * If this node "wraps" focusable components this call should return the ID of
	 * a HTML node that can receive focus when the component itself is focused.
	 * @return
	 */
	@Nullable
	protected String getFocusID() {
		return getActualID();
	}

	/**
	 * Appends the jQuery "selector" code for this node as:
	 * {@code $('#_a01')}
	 *
	 * @param sb
	 */
	final public void appendJQuerySelector(@Nonnull StringBuilder sb) {
		sb.append("$(\"#").append(getActualID()).append("\")");
	}

	/**
	 * Returns if node has set stretchHeight
	 * @return
	 */
	public boolean isStretchHeight() {
		return m_stretchHeight;
	}

	/**
	 * Method can be used to stretch height of element to take all available free space in parent container.
	 * <UL>
	 * <LI>NOTE: in order to stretchHeight can work, parent container needs to have height defined in some way (works out of box for all FloatingWindow based containers).</LI>
	 * <LI>In case that stretched node needs to be added directly in (non floating) page, to define page as stretch height applicable, call {@link UrlPage#fixStretchBody()} on it.</LI>
	 * <LI>In case of page resizing stretch code evaluation is triggered automatically.</LI>
	 * <LI>NOTE: if you need to trigger it due to page internal layout changes, you need to add inline javascript call to start resizing by {@link Div#getCustomUpdatesCallJS()} .
	 * </LI>
	 */
	public void setStretchHeight(boolean value) {
		if(m_stretchHeight == value) {
			return;
		}
		m_stretchHeight = value;
		changed();
	}

	@Override
	public String toString() {
		String n = getClass().getName();
		int pos = n.lastIndexOf('.');
		return n.substring(pos + 1) + ":" + m_actualID + (m_title == null ? "" : "/" + m_title);
	}

	/**
	 * Returns if node would have always rendered end tag in {@link HtmlTagRenderer} visitor for node.
	 * @return
	 */
	public boolean isRendersOwnClose() {
		return false;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Theme/theme variants.								*/
	/*--------------------------------------------------------------*/

	/**
	 * This checks to see if the RURL passed is a theme-relative URL. These URLs start
	 * with THEME/. If not the RURL is returned as-is; otherwise the URL is translated
	 * to a path containing the current theme string:
	 * <pre>
	 * 	$THEME/[currentThemeString]/[name]
	 * </pre>
	 * where [name] is the rest of the path string after THEME/ has been removed from it.
	 * @param path
	 * @return
	 */
	@Nonnull
	final public String getThemedResourceRURL(@Nonnull String path) {
		return DomApplication.get().internalGetThemeManager().getThemedResourceRURL(UIContext.getRequestContext(), path);
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Notifications												*/
	/*----------------------------------------------------------------------*/

	/**
	 * Send a self-defined event through the whole page. All nodes that registered
	 * for that event using {@link #addNotificationListener(Class, INotificationListener)} will
	 * receive the event in their listeners.
	 *
	 * @param eventClass
	 * @param <T>
	 * @throws Exception
	 */
	public final <T> void notify(T eventClass) throws Exception {
		getPage().notifyPage(eventClass);
	}

	public final <T> void addNotificationListener(Class<T> eventClass, INotificationListener<T> listener) {
		NotificationListener<T> nl = new Page.NotificationListener<>(eventClass, this, listener);
		if(isAttached())
			getPage().addNotificationListener(nl);
		else {
			List<NotificationListener<?>> list = m_notificationListenerList;
			if(null == list) {
				list = m_notificationListenerList = new ArrayList<>(4);
			}
			list.add(nl);
		}
	}

	@Nullable
	private List<NotificationListener<?>> m_notificationListenerList;

	/*--------------------------------------------------------------*/
	/*	CODING:	Soft binding support.								*/
	/*--------------------------------------------------------------*/

	@Nonnull
	public List<UIMessage> getBindingErrors() throws Exception {
		return OldBindingHandler.getBindingErrors(this);
	}

	/**
	 * Checks the tree starting at this component for binding errors; all of those will be reported
	 * by sending them to the error listeners. In case of errors this will return true.
	 * @return
	 */
	public boolean bindErrors() throws Exception {
		return OldBindingHandler.reportBindingErrors(this);
	}

	/**
	 * If present, return all bindings on this node.
	 * @return
	 */
	@Nullable
	final public List<IBinding> getBindingList() {
		return m_bindingList;
	}

	/**
	 * Add a binding to the binding list.
	 * @param binding
	 */
	final public void addBinding(@Nonnull IBinding binding) {
		List<IBinding> list = m_bindingList;
		if(list == null)
			list = m_bindingList = new ArrayList<>(1);
		list.add(binding);
	}

	final public void removeBinding(@Nonnull IBinding binding) {
		List<IBinding> list = m_bindingList;
		if(null != list)
			list.remove(binding);
	}

	@Nonnull final public ComponentPropertyBinding bind() {
		ClassMetaModel cmm = MetaManager.findClassMeta(getClass());
		PropertyMetaModel<?> p = cmm.findProperty("bindValue");
		if(null != p)
			return bind("bindValue");
		p = cmm.findProperty("value");
		if(null != p)
			return bind("value");
		throw new IllegalStateException("This control (" + getClass() + ") does not have a 'value' nor a 'bindValue' property");
	}


	@Nonnull final public ComponentPropertyBinding bind(@Nonnull String componentProperty) {
		ComponentPropertyBinding binder = new ComponentPropertyBinding(this, componentProperty);
		addBinding(binder);
		return binder;
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Misc														*/
	/*----------------------------------------------------------------------*/

	/**
	 * FIXME Should not exist?
	 * @param result
	 */
	protected void clearValidationFailure(UIException result) {
		/*
		 * Questionable place, but: if validation works we're sure any message related to
		 * the VALIDATION should be gone. So check here to see if the last "validation"
		 * failure is also in the message and if so clear that message.
		 */
		UIMessage msg = getMessage();
		if(result != null && msg != null) {
			//-- Moving from invalid -> valid -check message.
			if(result.getCode().equals(msg.getCode())) { // && result.getBundle().equals(msg.getBundle())) { // INCO Urgent: BundleRef needs equals, defining package+file as key.
				setMessage(null);
			}
		}
	}

	public void notifyParentOrOpenerPage(@Nullable String command) {
		appendJavascript("try { window.parent.WebUI.notifyPage('" + command + "'); } catch (err) {}");
		appendJavascript("try { window.opener.WebUI.notifyPage('" + command + "'); } catch (err) {}");
	}

	private enum AlignmentType {Top, TopToBottom, Left, Right, Middle}

	/**
	 * Adds javascript that aligns node top to top of specified node, with applying y offset.
	 *
	 * @param node
	 * @param yOffset
	 * @param appendAsCreateJs When T, renders javascript into appendCreateJS buffer, otherwise adds it as appendJavascript.
	 */
	public void alignToTop(@Nonnull NodeBase node, int yOffset, boolean appendAsCreateJs){
		alignToTop(node, yOffset, appendAsCreateJs, false);
	}

	/**
	 * Adds javascript that aligns node top to top of specified node, with applying y offset.
	 *
	 * @param node
	 * @param yOffset
	 * @param appendAsCreateJs When T, renders javascript into appendCreateJS buffer, otherwise adds it as appendJavascript.
	 * @param addServerPositionCallback When T, it also causes server round-trip once position is calculated to store calculate top position. This in needed as workaround for fact that, once node update is re-rendered it looses top value inside style attribute. 
	 */
	public void alignToTop(@Nonnull NodeBase node, int yOffset, boolean appendAsCreateJs, boolean addServerPositionCallback){
		alignTo(AlignmentType.Top, "WebUI.alignToTop", node, yOffset, appendAsCreateJs, addServerPositionCallback);
	}

	/**
	 * Adds javascript that aligns node top to bottom of specified node, with applying y offset.
	 *
	 * @param node
	 * @param yOffset
	 * @param appendAsCreateJs When T, renders javascript into appendCreateJS buffer, otherwise adds it as appendJavascript.
	 */
	public void alignTopToBottom(@Nonnull NodeBase node, int yOffset, boolean appendAsCreateJs){
		alignTopToBottom(node, yOffset, appendAsCreateJs, false);
	}

	/**
	 * Adds javascript that aligns node top to bottom of specified node, with applying y offset.
	 *
	 * @param node
	 * @param yOffset
	 * @param appendAsCreateJs When T, renders javascript into appendCreateJS buffer, otherwise adds it as appendJavascript.
	 * @param addServerPositionCallback When T, it also causes server round-trip once position is calculated to store calculate top position. This in needed as workaround for fact that, once node update is re-rendered it looses top value inside style attribute. 
	 */
	public void alignTopToBottom(@Nonnull NodeBase node, int yOffset, boolean appendAsCreateJs, boolean addServerPositionCallback){
		alignTo(AlignmentType.TopToBottom, "WebUI.alignTopToBottom", node, yOffset, appendAsCreateJs, addServerPositionCallback);
	}

	/**
	 * Adds javascript that aligns node left to left position of specified node, with applying offset.
	 *
	 * @param node
	 * @param xOffset
	 * @param appendAsCreateJs When T, renders javascript into appendCreateJS buffer, otherwise adds it as appendJavascript.
	 */
	public void alignToLeft(@Nonnull NodeBase node, int xOffset, boolean appendAsCreateJs){
		alignToLeft(node, xOffset, appendAsCreateJs, false);
	}

	/**
	 * Adds javascript that aligns node left to left position of specified node, with applying offset.
	 *
	 * @param node
	 * @param xOffset
	 * @param appendAsCreateJs When T, renders javascript into appendCreateJS buffer, otherwise adds it as appendJavascript.
	 * @param addServerPositionCallback When T, it also causes server round-trip once position is calculated to store calculate left position. This in needed as workaround for fact that, once node update is re-rendered it looses left value inside style attribute. 
	 */
	public void alignToLeft(@Nonnull NodeBase node, int xOffset, boolean appendAsCreateJs, boolean addServerPositionCallback){
		alignTo(AlignmentType.Left, "WebUI.alignToLeft", node, xOffset, appendAsCreateJs, addServerPositionCallback);
	}

	/**
	 * Adds javascript that aligns node right to right position of specified node, with applying offset.
	 *
	 * @param node
	 * @param xOffset
	 * @param appendAsCreateJs When T, renders javascript into appendCreateJS buffer, otherwise adds it as appendJavascript.
	 */
	public void alignToRight(@Nonnull NodeBase node, int xOffset, boolean appendAsCreateJs){
		alignToRight(node, xOffset, appendAsCreateJs, false);
	}

	/**
	 * Adds javascript that aligns node right to right position of specified node, with applying offset.
	 *
	 * @param node
	 * @param xOffset
	 * @param appendAsCreateJs When T, renders javascript into appendCreateJS buffer, otherwise adds it as appendJavascript.
	 * @param addServerPositionCallback When T, it also causes server round-trip once position is calculated to store calculate left position. This in needed as workaround for fact that, once node update is re-rendered it looses left value inside style attribute. 
	 */
	public void alignToRight(@Nonnull NodeBase node, int xOffset, boolean appendAsCreateJs, boolean addServerPositionCallback){
		alignTo(AlignmentType.Right, "WebUI.alignToRight", node, xOffset, appendAsCreateJs, addServerPositionCallback);
	}

	/**
	 * Adds javascript that aligns node horizontal middle to middle position of specified node, with applying offset.
	 *
	 * @param node
	 * @param xOffset
	 * @param appendAsCreateJs When T, renders javascript into appendCreateJS buffer, otherwise adds it as appendJavascript.
	 */
	public void alignToMiddle(@Nonnull NodeBase node, int xOffset, boolean appendAsCreateJs){
		alignToMiddle(node, xOffset, appendAsCreateJs, false);
	}

	/**
	 * Adds javascript that aligns node horizontal middle to middle position of specified node, with applying offset.
	 *
	 * @param node
	 * @param xOffset
	 * @param appendAsCreateJs When T, renders javascript into appendCreateJS buffer, otherwise adds it as appendJavascript.
	 * @param addServerPositionCallback When T, it also causes server round-trip once position is calculated to store calculate left position. This in needed as workaround for fact that, once node update is re-rendered it looses left value inside style attribute. 
	 */
	public void alignToMiddle(@Nonnull NodeBase node, int xOffset, boolean appendAsCreateJs, boolean addServerPositionCallback){
		alignTo(AlignmentType.Middle, "WebUI.alignToMiddle", node, xOffset, appendAsCreateJs, addServerPositionCallback);
	}

	private void alignTo(final @Nonnull AlignmentType alignment, @Nonnull String jsFunction, @Nonnull NodeBase node, int offset, boolean appendAsCreateJs, boolean addServerPositionCallback){
		setPosition(PositionType.ABSOLUTE);
		String id = getActualID();
		String callbackParam = addServerPositionCallback ? "true" : "false";
		String js = jsFunction + "('" + id + "', '" + node.getActualID() + "', " + offset + ", " + callbackParam + ");";
		if (appendAsCreateJs){
			appendCreateJS(js);
		}else{
			appendJavascript(js);
		}
		if (addServerPositionCallback){
			setOnSizeAndPositionChange(new INotify<NodeBase>(){

				@Override
				public void onNotify(NodeBase sender) throws Exception {
					Rect clientBounds = getClientBounds();
					if (null != clientBounds){
						switch (alignment){
							case Top:
							case TopToBottom:
								setTop(clientBounds.getTop());
								break;
							default: //other are horizontal alignments, so we set left position
								setLeft(clientBounds.getLeft());
								break;
						}
					}
				}
			});
		}
	}

	@Nullable
	protected Rect getClientBounds() {
		return m_clientBounds;
	}

	protected void setClientBounds(@Nonnull Rect clientBound) {
		m_clientBounds = clientBound;
	}

	@Nullable
	protected Dimension getBrowserWindowSize() {
		return m_browserWindowSize;
	}

	protected void setBrowserWindowSize(@Nonnull Dimension browserWindowSize) {
		m_browserWindowSize = browserWindowSize;
	}

	@Nullable
	protected INotify<NodeBase> getOnSizeAndPositionChange() {
		return m_onSizeAndPositionChange;
	}

	protected void setOnSizeAndPositionChange(@Nonnull INotify<NodeBase> onSizeAndPositionChange) {
		m_onSizeAndPositionChange = onSizeAndPositionChange;
	}

	private void handleClientPositionAndSizeChange(@Nonnull RequestContextImpl ctx) throws Exception {
		String valueRect = ctx.getParameter(getActualID() + "_rect");
		String valueBrowserWindowSize = ctx.getParameter("window_size");
		if(null != valueRect && null != valueBrowserWindowSize) {
			String[] values = valueRect.split(",");
			try {
				int left = Math.round(Float.parseFloat(values[0]));
				int top = Math.round(Float.parseFloat(values[1]));
				int width = Math.round(Float.parseFloat(values[2])); //when browser is zoomed, it gets decimal values for size 8-/
				int height = Math.round(Float.parseFloat(values[3]));
				setClientBounds(new Rect(left, top, width + left, height + top));
			} catch(Exception ex) {
				throw new IllegalArgumentException("Unrecognized " + Constants.ACMD_NOTIFY_CLIENT_POSITION_AND_SIZE + " valueRect (id='" + getActualID() + "'):" + valueRect, ex);
			}
			values = valueBrowserWindowSize.split(",");
			try {
				int width = Math.round(Float.parseFloat(values[0]));
				int height = Math.round(Float.parseFloat(values[1]));
				setBrowserWindowSize(new Dimension(width, height));
			} catch(Exception ex) {
				throw new IllegalArgumentException("Unrecognized " + Constants.ACMD_NOTIFY_CLIENT_POSITION_AND_SIZE + " valueBrowserWindowSize (id='" + getActualID() + "'):" + valueBrowserWindowSize, ex);
			}
			INotify<NodeBase> listener = getOnSizeAndPositionChange();
			if(listener != null) {
				listener.onNotify(this);
			}
		}
	}

	/**
	 * Add the specified css class(es).
	 */
	@Nonnull
	public NodeBase css(@Nonnull String ...classNames) {
		for(String cn : classNames) {
			addCssClass(cn);
		}
		return this;
	}
}
