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

import to.etc.domui.component.layout.FloatingDiv;
import to.etc.domui.converter.ConverterRegistry;
import to.etc.domui.converter.IConverter;
import to.etc.domui.dom.errors.ErrorFenceHandler;
import to.etc.domui.dom.errors.IErrorFence;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.logic.errors.ProblemModel;
import to.etc.domui.logic.errors.ProblemReporter;
import to.etc.domui.util.DomUtil;
import to.etc.webapp.ProgrammerErrorException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

/**
 * Base node for tags that can contain other nodes.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 17, 2007
 */
abstract public class NodeContainer extends NodeBase implements Iterable<NodeBase> {
	private List<NodeBase> m_children = Collections.EMPTY_LIST;

	/**
	 * Indicator that a child of this node has changes. NOT SET when THIS node has changes: that
	 * can be seen because this node is dirty (attribute changes) or it's before tree is not-null
	 * (tree changes).
	 */
	private boolean m_childHasUpdates;

	/**
	 * When an update knows that a full re-render is best it sets this hint. It will cause a full
	 * re-render of the children IN this node, not this container itself. The only reason his
	 * gets set currently is when an embedded text changes because we cannot address a text
	 * node.
	 */
	private boolean m_mustRenderChildrenFully;

	private NodeBase[] m_oldChildren;

	private IErrorFence m_errorFence;

	private NodeContainer m_delegate;

	/**
	 * Create a container with the specified tag name.
	 * @param tag
	 */
	public NodeContainer(@Nonnull final String tag) {
		super(tag);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Internal state & delta indicators.					*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @return
	 */
	final boolean mustRenderChildrenFully() {
		return m_mustRenderChildrenFully;
	}

	final void setMustRenderChildrenFully(final boolean mustRenderChildrenFully) {
		//		if(mustRenderChildrenFully) {
		//			StringTool.dumpLocation("mustRenderFully");
		//		}
		//
		m_mustRenderChildrenFully = mustRenderChildrenFully;
	}

	final void setMustRenderChildrenFully() {
		setMustRenderChildrenFully(true);
	}

	final void childChanged() {
		NodeContainer c = this;
		for(;;) {
			if(c.m_childHasUpdates)
				return;
			c.m_childHasUpdates = true;
			if(!c.hasParent())
				break;
			c = c.getParent();
		}
	}

	final boolean childHasUpdates() {
		return m_childHasUpdates;
	}

	final void setChildHasUpdates(final boolean childHasUpdates) {
		m_childHasUpdates = childHasUpdates;
	}

	// jal 20070818 Do not expose this (hide impl & prevent changes without proper before-tree management)!!!
	//	public List<NodeBase>	getChildren() {
	//		return m_children;
	//	}

	/**
	 * Used by delta-builder.
	 */
	final public List<NodeBase> internalGetChildren() {
		return m_children;
	}

	/**
	 * Used for unit tests.
	 */
	@Override
	final public void internalCheckNotDirty() {
		super.internalCheckNotDirty();
		if(childHasUpdates())
			throw new IllegalStateException("The node " + this + " has 'childHasUpdates' set");
		if(internalGetOldParent() != null)
			throw new IllegalStateException("The node " + this + " has an 'oldParent' set");
		if(internalGetOldChildren() != null)
			throw new IllegalStateException("The node " + this + " has 'oldChildren' set");
		//		if(m_treeChanged)
		//			throw new IllegalStateException("The node "+this+" has 'treeChanged' set");
	}

	/**
	 * DO NOT USE.
	 * Internal: clear all delta information.
	 * @see to.etc.domui.dom.html.NodeBase#internalClearDelta()
	 */
	@Override
	final public void internalClearDelta() {
		super.internalClearDelta();
		setMustRenderChildrenFully(false);
		m_oldChildren = null;
		m_childHasUpdates = false;
	}

	/**
	 * DO NOT USE.
	 * Internal: clear delta including children's delta.
	 * @see to.etc.domui.dom.html.NodeBase#internalClearDeltaFully()
	 */
	@Override
	final public void internalClearDeltaFully() {
		internalClearDelta();
		for(int i = m_children.size(); --i >= 0;)
			m_children.get(i).internalClearDeltaFully();
	}

	/**
	 * Internal: delta renderer old children set if this node changed. Null if this node has not seen changes.
	 * @return
	 */
	final public NodeBase[] internalGetOldChildren() {
		return m_oldChildren;
	}

	/**
	 * Count the #of nodes in this tree, recursively until the given depth.
	 * @see to.etc.domui.dom.html.NodeBase#internalGetNodeCount(int)
	 */
	@Override
	protected int internalGetNodeCount(int depth) {
		if(depth <= 0)
			return 0;
		depth--;
		int count = 0;
		for(NodeBase b : m_children) {
			count += b.internalGetNodeCount(depth);
		}
		return count;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Tree accessors.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Return an iterator that iterates over all children, in order.
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	@Nonnull
	final public Iterator<NodeBase> iterator() {
		if(m_delegate != null)
			return m_delegate.iterator();

		return m_children.iterator();
	}

	/**
	 * Return the #of children of this container.
	 * @return
	 */
	final public int getChildCount() {
		if(m_delegate != null)
			return m_delegate.getChildCount();

		return m_children.size();
	}

	/**
	 * Return the index of the specified child, if present. Returns -1 if not found.
	 * @param b
	 * @return
	 */
	final public int findChildIndex(@Nonnull final NodeBase b) {
		if(m_delegate != null) {
			int ix = m_delegate.findChildIndex(b);
			if(ix != -1)
				return ix;
		}

		if(!b.hasParent())
			return -1;
		if(b.getParent() != this)
			return -1;
		return m_children.indexOf(b);
	}

	/**
	 * Get the nth child.
	 * @param i
	 * @return
	 */
	@Nonnull
	final public NodeBase getChild(final int i) {
		if(m_delegate != null)
			return m_delegate.getChild(i);

		return m_children.get(i);
	}

	final public NodeBase undelegatedGetChild(final int i) {
		return m_children.get(i);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Tree delta manipulation, internals.					*/
	/*--------------------------------------------------------------*/
	/**
	 * Most of the logic to properly indicate that this node's children have changed.
	 */
	final void treeChanging() {
		if(m_oldChildren != null) // Already have a copy?
			return;
		if(hasParent())
			getParent().childChanged();

		//-- Copy all of my children and save me as their current parent
		if(isAttached())
			getPage().copyIdMap(); // Tell my parent I've changed.

		m_oldChildren = m_children.toArray(new NodeBase[m_children.size()]);
		for(int i = m_oldChildren.length; --i >= 0;) {
			m_oldChildren[i].internalSetOldParent(this);
		}
	}


	/**
	 * Registers all children of a registered parent node. Since this calls onAddedToPage() while
	 * traversing, and since onAddedToPage can <i>add</i> nodes the loop may encounter a {@link ConcurrentModificationException};
	 * in that case we simply try again.
	 */
	final void registerChildren() {
		for(int i = 0; i < 50; i++) {
			try {
				for(NodeBase ch : m_children) {
					if(!ch.isAttached())
						ch.registerWithPage(getPage());
				}
				return;
			} catch(ConcurrentModificationException cmx) {}
		}
		throw new IllegalStateException("registerChildren() keeps dying with ConcurrentModificationException!?");
	}

	/**
	 * Main function to register new nodes with the page they now belong to. This causes
	 * a recursive descend of all children of the added node, so that not only this child
	 * but also all of it's children are registered to the page.
	 * [--NO LONGER TRUE--
	 * While registering this will generate a list of registered nodes. This list is then
	 * used to call the onAddedToPage() handler <i>after</i> all nodes have been added, to
	 * prevent concurrent modification exceptions.
	 * --]
	 * @param child
	 */
	final private void registerWithPage(@Nonnull final NodeBase child) {
		if(!isAttached()) // No page-> cannot register
			return;
		child.registerWithPage(getPage());
	}

	/**
	 * Internal use only: register with a page, causing IDs to be assigned and mapped
	 * if possible.
	 *
	 * @see to.etc.domui.dom.html.NodeBase#registerWithPage(to.etc.domui.dom.html.Page)
	 */
	@Override
	final void registerWithPage(@Nonnull final Page p) {
		super.registerWithPage(p); // Base registration of *this*
		registerChildren();
	}

	/**
	 * The NodeContainer version of this call unregisters itself AND all it's children, recursively.
	 * @see to.etc.domui.dom.html.NodeBase#unregisterFromPage()
	 */
	@Override
	final void unregisterFromPage() {
		for(int i = 0; i < 50; i++) {
			try {
				for(NodeBase b : m_children) {
					if(this == b)
						throw new IllegalStateException("Internal: somehow I (the parent) is also present in my own list-of-children!?");
					b.unregisterFromPage(); // This one already checks if the thing is registered so it can be retried.
				}
				super.unregisterFromPage();
				return;
			} catch(ConcurrentModificationException x) {}
		}
		throw new IllegalStateException("unregisterFromPage: keeps throwing ConcurrentModificationExceptions!??");
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Tree manipulation.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Override to check if special node types can be contained in this.
	 * @param node
	 * @return
	 */
	@OverridingMethodsMustInvokeSuper
	protected void canContain(@Nonnull final NodeBase node) {}

	/**
	 * Add the child at the end of the list.
	 * @param nd
	 */
	@Nonnull
	final public NodeContainer add(@Nonnull final NodeBase nd) {
		add(Integer.MAX_VALUE, nd);
		return this;
	}

	/**
	 * Add the child at the specified index in the child list.
	 * @param index
	 * @param nd
	 */
	final public void add(final int index, @Nonnull final NodeBase nd) {
		/*
		 * Nodes that *must* be added to the body should delegate there immediately.
		 */
		if(nd instanceof FloatingDiv) {
			//-- This *must* be added to the BODY node, and this node must be attached for that to work.. Is it?
			if(!isAttached())
				throw new ProgrammerErrorException("The component " + nd.getClass() + " is defined as 'must be added to the body' but the node it is added to " + this + " is not yet added to the page.");
			getPage().internalAddFloater(this, (FloatingDiv) nd);
			return;
		}

		//-- Is delegation active? Then delegate to wherever.
		if(m_delegate != null) {
			m_delegate.add(index, nd);
			return;
		}
		internalAdd(index, nd);
	}

	final public void undelegatedAdd(final int index, @Nonnull final NodeBase nd) {
		/*
		 * Nodes that *must* be added to the body should delegate there immediately.
		 */
		if(nd instanceof FloatingDiv) {
			//-- This *must* be added to the BODY node, and this node must be attached for that to work.. Is it?
			if(!isAttached())
				throw new ProgrammerErrorException("The component " + nd.getClass() + " is defined as 'must be added to the body' but the node it is added to " + this
					+ " is not yet added to the page.");
			getPage().internalAddFloater(this, (FloatingDiv) nd);
			return;
		}
		internalAdd(index, nd);
	}

	final protected void internalAdd(final int index, @Nonnull final NodeBase nd) {
		if(nd == this)
			throw new IllegalStateException("Attempt to add a node " + nd + " to itself as a child.");

		canContain(nd);
		if(m_children == Collections.EMPTY_LIST)
			m_children = new ArrayList<NodeBase>();
		nd.remove(); // Make sure it is removed from wherever it came from,
		if(nd instanceof TextNode)
			setMustRenderChildrenFully();
		treeChanging();
		//		registerWithPage(nd);			// jal 20080929 Moved downwards to allow tree to be visible at onAddedToPage() event time
		if(index >= m_children.size())
			m_children.add(nd);
		else
			m_children.add(index, nd);
		nd.setParent(this);
		registerWithPage(nd); // ORDERED Must be AFTER hanging this into the tree
		childChanged();
	}


	/**
	 * Add a #text node.
	 * @param txt
	 */
	@Nonnull
	final public NodeContainer add(@Nullable final String txt) {
		if(txt != null && txt.length() > 0)
			add(new TextNode(txt));
		return this;
	}

	/**
	 * Remove a child node from me. This also removes ALL descendants from the current page's view.
	 * @param child
	 */
	final public void removeChild(@Nonnull final NodeBase child) {
		//child can be direct child or child of delegate
		int ix = m_children.indexOf(child);
		//we first try to find child in direct children
		if(ix == -1) {
			//if not found in direct children, we look into delegate if exists
			if(m_delegate != null) {
				m_delegate.removeChild(child);
				return;
			}

			if(child.getParent() != this)
				throw new IllegalStateException("Child " + child + " is not a child of container " + this);

			throw new IllegalStateException("Child " + child + " was not in list!? " + this);
		}
		treeChanging();
		m_children.remove(ix);
		child.unregisterFromPage();
		child.setParent(null); // jal 20091015 moved after unregister to allow nodes to clear their error state
		childChanged();
	}

	/**
	 * Remove the nth child. The removed child is returned and can be reused (added) somewhere else.
	 * @param index
	 * @return
	 */
	@Nonnull
	final public NodeBase removeChild(final int index) {
		if(m_delegate != null) {
			return m_delegate.removeChild(index);
		}
		if(index < 0 || index >= m_children.size())
			throw new IllegalStateException("Bad delete index " + index + " on node " + this + " with " + m_children.size() + " children");
		treeChanging();
		NodeBase child = m_children.remove(index);
		child.unregisterFromPage();
		child.setParent(null); // jal 20091015 moved after unregister to allow nodes to clear their error state
		childChanged();
		return child;
	}

	/**
	 * Swap two children: the "child" (1st) parameter gets removed, and the "nw" (2nd) parameter
	 * is put in it's place, at it's position.
	 *
	 * @param child
	 * @param nw
	 */
	final public void replaceChild(@Nonnull final NodeBase child, @Nonnull final NodeBase nw) {
		//child can be direct child or child of delegate
		int ix = m_children.indexOf(child);
		//we first try to find child in direct children
		if(ix == -1) {
			//if not found in direct children, we look into delegate if exists
			if(m_delegate != null) {
				m_delegate.replaceChild(child, nw);
				return;
			}

			if(child.getParent() != this)
				throw new IllegalStateException("Child " + child + " is not a child of container " + this);

			throw new IllegalStateException("Child " + child + " was not in list!? " + this);
		}

		treeChanging();
		m_children.set(ix, nw); // Replace inline
		child.unregisterFromPage();
		child.setParent(null); // jal 20091015 moved after unregister to allow nodes to clear their error state
		nw.remove();
		nw.setParent(this);
		registerWithPage(nw); // ORDERED Must be AFTER hanging this into the tree
		childChanged();
	}

	/**
	 * Discard all children.
	 */
	final public void removeAllChildren() {
		if(m_delegate != null) {
			m_delegate.removeAllChildren();
			return;
		}

		if(m_children.size() == 0)
			return;
		treeChanging();
		m_childHasUpdates = false; // They're gone.... No changes I guess.
		m_mustRenderChildrenFully = true; // Just render all my children again
		for(NodeBase b : m_children) {
			b.unregisterFromPage();
			b.setParent(null); // jal 20091015 moved after unregister to allow nodes to clear their error state
		}
		m_children.clear();
	}

	/**
	 * This destroys all existing nodes and causes this node to be rebuilt the next time the
	 * tree is rendered.
	 */
	@Override
	@OverridingMethodsMustInvokeSuper
	final public void forceRebuild() {
		//-- If we have nodes destroy 'm all
		m_delegate = null; // FIXME URGENT Wrong!!!!!
		removeAllChildren(); // Remove all old crap
		treeChanging();
		super.forceRebuild();
	}

	/**
	 * Set the text <i>contained in</i> this node, using tilde replacement. Before
	 * the new text node is added to the container the container will first be <b>fully emptied</b>, i.e.
	 * any contained node will be deleted. Setting a null or empty string text will just clear the
	 * node's contents without a {@link TextNode} being added.
	 *
	 * FIXME This must be renamed and made final.
	 *
	 * @param txt
	 */
	public void setText(@Nullable final String txt) {
		if(m_delegate != null) {
			m_delegate.setText(txt);
			return;
		}

		setMustRenderChildrenFully();
		if(getChildCount() == 1) {
			if(getChild(0) instanceof TextNode) {
				//-- Replace this single node's text
				((TextNode) getChild(0)).setText(txt);
				childChanged();
				treeChanging();
				return;
			}
		}

		//-- Drop all children
		while(getChildCount() > 0)
			removeChild(getChild(getChildCount() - 1));
		if(null != txt && txt.length() > 0) {
			TextNode t = new TextNode(txt);
			add(t);
		}
	}

	/**
	 * If this node contains {@link TextNode}'s only this creates the text string represented by those nodes. If
	 * other nodes are found this returns null.
	 * @return
	 */
	@Nullable
	public String getTextContents() {
		StringBuilder sb = new StringBuilder();
		for(NodeBase nb: this) {
			if(nb instanceof TextNode) {
				TextNode tn = (TextNode) nb;
				sb.append(tn.getText());
			} else
				return null;
		}
		return sb.toString();
	}

	@Override
	final protected void internalShelve() throws Exception {
		onShelve();
		for(int i = m_children.size(); --i >= 0;)
			m_children.get(i).internalShelve();
	}

	@Override
	final protected void internalUnshelve() throws Exception {
		onUnshelve();
		for(int i = m_children.size(); --i >= 0;)
			m_children.get(i).internalUnshelve();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Utility functions.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Utility method to add a table; it returns the TBody.
	 * @param headers	When not null this is set as the css class for the TABLE tag.
	 * @return
	 */
	public TBody addTable(String... headers) {
		Table t = new Table();
		t.setCellPadding("0");
		t.setCellSpacing("0");
		add(t);
		if(headers != null && headers.length > 0)
			t.getHead().setHeaders(headers);
		TBody b = new TBody();
		t.add(b);
		return b;
	}

	@Nonnull
	public TBody addTableForLayout(String css) {
		Table t = new Table();
		t.setCssClass(css);
		t.addCssClass("ui-layout");
		t.setCellPadding("0");
		t.setCellSpacing("0");
		t.setTableWidth("100%");
		add(t);
		TBody b = new TBody();
		t.add(b);
		return b;
	}

	@Nonnull
	public TBody addTableForLayout() {
		return addTableForLayout(null);
	}

	/**
	 * Locate all <i>direct</i> children of this container that are instancesof [ofClass].
	 * @param <T>
	 * @param ofClass
	 * @return
	 */
	final public <T> List<T> getChildren(@Nonnull Class<T> ofClass) {
		if(m_delegate != null)
			return m_delegate.getChildren(ofClass);

		List<T> res = null;
		for(NodeBase b : m_children) {
			if(ofClass.isAssignableFrom(b.getClass())) {
				if(res == null)
					res = new ArrayList<T>();
				res.add((T) b);
			}
		}
		return res == null ? Collections.EMPTY_LIST : res;
	}

	/**
	 * Get a list of all children in the <i>entire subtree</i> that are an instance of the specified class.
	 * @param <T>
	 * @param ofClass
	 * @return
	 */
	@Nonnull
	final public <T> List<T> getDeepChildren(@Nonnull Class<T> ofClass) {
		if(m_delegate != null)
			return m_delegate.getDeepChildren(ofClass);

		List<T> res = new ArrayList<T>();
		internalDeepChildren(res, ofClass);
		return res;
	}

	final private <T> void internalDeepChildren(List<T> res, Class<T> ofClass) {
		for(NodeBase b : m_children) {
			if(ofClass.isAssignableFrom(b.getClass())) {
				res.add((T) b);
			} else if(b instanceof NodeContainer) {
				((NodeContainer) b).internalDeepChildren(res, ofClass);
			}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Miscellaneous										*/
	/*--------------------------------------------------------------*/

	/**
	 * Default onRefresh for a container will call refresh on all children.
	 *
	 * @see to.etc.domui.dom.html.NodeBase#onRefresh()
	 */
	@Override
	protected void onRefresh() throws Exception {
		if(m_delegate != null) {
			m_delegate.onRefresh();
			return;
		}

		for(int i = 0; i < m_children.size(); i++)
			m_children.get(i).onRefresh();
	}

	/**
	 * Put a converted value in this cell's text.
	 * @param <T>
	 * @param <C>
	 * @param conv
	 * @param value
	 * @throws Exception
	 */
	public <T, C extends IConverter<T>> void setValue(@Nonnull Class<C> conv, @Nullable T value) throws Exception {
		setText(ConverterRegistry.convertValueToString(conv, value));
	}

	@Nullable
	final public IErrorFence getErrorFence() {
		if(m_delegate != null) {
			IErrorFence f = m_delegate.getErrorFence();
			if(null != f)
				return f;
		}
		return m_errorFence;
	}

	final public void setErrorFence(@Nullable final IErrorFence errorFence) {
		//		StringTool.dumpLocation("setErrorFence(...): called on " + this);
		if(m_delegate != null)
			m_delegate.setErrorFence(errorFence);
		else
			m_errorFence = errorFence;
	}

	final public void setErrorFence() {
		//		StringTool.dumpLocation("setErrorFence(): called on " + this);
		if(m_delegate != null)
			m_delegate.setErrorFence();
		else if(m_errorFence == null)
			m_errorFence = new ErrorFenceHandler(this);
	}

	@Override
	final public void internalOnBeforeRender() throws Exception {
		onBeforeRender();
		for(int i = m_children.size(); --i >= 0;) {
			m_children.get(i).internalOnBeforeRender();
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Content delegation and framed nodes handling.		*/
	/*--------------------------------------------------------------*/
	/**
	 * EXPERIMENTAL Set delegation to another node. This causes all "child" operations to delegate to the "to" node,
	 * it means that all nodes added to this node will actually be added to the "to" node. This is used to "delegate"
	 * content rendering for framed controls, so the content model of the control can be treated as the control itself.
	 *
	 * @param c
	 */
	final public void delegateTo(@Nullable NodeContainer c) {
		if(c == this)
			throw new IllegalStateException("Cannot delegate to self: this would nicely loop..");

		//-- Check to make sure there are not too many levels of delegation present.
		NodeContainer nc = c;
		int dc = 0;
		while(nc != null) {
			dc++;
			if(dc > 10)
				throw new ProgrammerErrorException("Too many delegation levels: can be a delegation loop");
			nc = nc.m_delegate;
		}
		m_delegate = c;
	}

	/**
	 * If this node delegates it's stuff to another, this returns that other node. See {@link #delegateTo(NodeContainer)} for
	 * details.
	 * @return
	 */
	public NodeContainer getDelegate() {
		return m_delegate;
	}

	@Override
	final protected void internalCreateFrame() throws Exception {
		//		NodeContainer old = m_delegate;
		m_delegate = null;
		createFrame();
		//		m_delegate = old;
	}

	/**
	 * EXPERIMENTAL This can be overridden to handle nodes that have an explicit "frame".
	 */
	@OverridingMethodsMustInvokeSuper
	protected void createFrame() throws Exception {
	}


	@Override
	public void appendTreeErrors(@Nonnull List<UIMessage> errorList) {
		super.appendTreeErrors(errorList);
		for(NodeBase nb : this) {
			nb.appendTreeErrors(errorList);
		}
	}

	@Override
	public boolean hasError() {
		for(NodeBase nb : this) {
			if(nb.hasError())
				return true;
		}

		return false;
	}

	/**
	 * Finds all IControl and IActionControl children and set it to readonly/disabled.
	 */
	public void disableAllChildControlsDeep() throws Exception {
		for(IControl<?> ctrl : getDeepChildren(IControl.class)) {
			ctrl.setReadOnly(true);
			ctrl.setDisabled(true);
			if(ctrl instanceof NodeContainer) {
				DomUtil.buildTree((NodeContainer) ctrl);
				((NodeContainer) ctrl).disableAllChildControlsDeep();
			}
		}

		for(IActionControl ctrl : getDeepChildren(IActionControl.class)) {
			ctrl.setDisabled(true);
			if(ctrl instanceof NodeContainer) {
				DomUtil.buildTree((NodeContainer) ctrl);
				((NodeContainer) ctrl).disableAllChildControlsDeep();
			}
		}
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Validation framework.										*/
	/*----------------------------------------------------------------------*/

	/**
	 * When called this asks all underlying nodes to validate. Anything having some
	 * validation error will report it. Before validation all existing validation
	 * errors are cleared.
	 *
	 * @return T if there are validation errors.
	 */
	public boolean validationErrors() throws Exception {
		//validationClear();
		ProblemModel pm = new ProblemModel();
		validateComponents(pm);
		new ProblemReporter(this, pm).report();
		return pm.hasErrors();
	}

	protected void validateComponents(ProblemModel pm) throws Exception {
	}
}

