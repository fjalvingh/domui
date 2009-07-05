package to.etc.domui.dom.html;

import java.util.*;

import to.etc.domui.dom.errors.*;
import to.etc.webapp.nls.*;

/**
 * Base node for tags that can contain other nodes.
 *
 * A description on the deltaing mechanism used can be found in the header for {@link NodeBase}
 * @see NodeBase
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 17, 2007
 */
abstract public class NodeContainer extends NodeBase implements Iterable<NodeBase> {
	private List<NodeBase> m_children = Collections.EMPTY_LIST;

	//	private boolean			m_treeChanged;	jal 20090116 Seems to be unused

	/**
	 * Indicator that a child of this node has changes. NOT SET when THIS node has changes: that
	 * can be seen because this node is dirty (attribute changes) or it's before tree is not-null
	 * (tree changes).
	 */
	private boolean m_childHasUpdates;

	/**
	 * When an update knows that a full rerender is best it sets this hint. It will cause a full
	 * re-render of the children IN this node, not this container itself. The only reason his
	 * gets set currently is when an embedded text changes because we cannot address a text
	 * node.
	 */
	private boolean m_mustRenderChildrenFully;

	private NodeBase[] m_oldChildren;

	private IErrorFence m_errorFence;

	public NodeContainer(final String tag) {
		super(tag);
	}

	boolean mustRenderChildrenFully() {
		return m_mustRenderChildrenFully;
	}

	void setMustRenderChildrenFully(final boolean mustRenderChildrenFully) {
		//		if(mustRenderChildrenFully) {
		//			StringTool.dumpLocation("mustRenderFully");
		//		}
		//
		m_mustRenderChildrenFully = mustRenderChildrenFully;
	}

	void setMustRenderChildrenFully() {
		setMustRenderChildrenFully(true);
	}


	protected boolean canContain(final NodeBase node) {
		return true;
	}

	void childChanged() {
		NodeContainer c = this;
		do {
			if(c.m_childHasUpdates)
				return;
			c.m_childHasUpdates = true;
			c = c.getParent();
		} while(c != null);
	}

	boolean childHasUpdates() {
		return m_childHasUpdates;
	}

	void setChildHasUpdates(final boolean childHasUpdates) {
		m_childHasUpdates = childHasUpdates;
	}

	// jal 20070818 Do not expose this (hide impl & prevent changes without proper before-tree management)!!!
	//	public List<NodeBase>	getChildren() {
	//		return m_children;
	//	}

	/**
	 * Used by delta-builder.
	 */
	List<NodeBase> internalGetChildren() {
		return m_children;
	}

	public Iterator<NodeBase> iterator() {
		return m_children.iterator();
	}

	public int getChildCount() {
		return m_children.size();
	}

	public NodeBase getChild(final int i) {
		return m_children.get(i);
	}

	/**
	 * Used for unit tests.
	 */
	@Override
	public void internalCheckNotDirty() {
		super.internalCheckNotDirty();
		if(childHasUpdates())
			throw new IllegalStateException("The node " + this + " has 'childHasUpdates' set");
		if(getOldParent() != null)
			throw new IllegalStateException("The node " + this + " has an 'oldParent' set");
		if(getOldChildren() != null)
			throw new IllegalStateException("The node " + this + " has 'oldChildren' set");
		//		if(m_treeChanged)
		//			throw new IllegalStateException("The node "+this+" has 'treeChanged' set");
	}

	@Override
	public void clearDelta() {
		super.clearDelta();
		setMustRenderChildrenFully(false);
		m_oldChildren = null;
		m_childHasUpdates = false;
	}

	@Override
	final public void clearDeltaFully() {
		clearDelta();
		for(int i = m_children.size(); --i >= 0;)
			m_children.get(i).clearDeltaFully();
	}

	public NodeBase[] getOldChildren() {
		return m_oldChildren;
	}

	/**
	 * Most of the logic to properly indicate that this node's children have changed.
	 */
	void treeChanging() {
		if(m_oldChildren != null) // Already have a copy?
			return;
		if(getParent() != null)
			getParent().childChanged();

		//-- Copy all of my children and save me as their current parent
		if(getPage() != null)
			getPage().copyIdMap(); // Tell my parent I've changed.

		m_oldChildren = m_children.toArray(new NodeBase[m_children.size()]);
		for(int i = m_oldChildren.length; --i >= 0;) {
			m_oldChildren[i].setOldParent(this);
		}
	}

	/**
	 * Add the child at the end of the list.
	 * @param nd
	 */
	public void add(final NodeBase nd) {
		if(!canContain(nd))
			throw new IllegalStateException("This node " + this + " cannot contain a " + nd);
		if(m_children == Collections.EMPTY_LIST)
			m_children = new ArrayList<NodeBase>();
		nd.remove(); // Make sure it is removed from wherever it came from,
		if(nd instanceof TextNode)
			setMustRenderChildrenFully();
		treeChanging();
		//		registerWithPage(nd);			// jal 20080929 Moved downwards to allow tree to be visible at onAddedToPage() event time
		m_children.add(nd); // Then add to this list
		nd.setParent(this);
		registerWithPage(nd); // ORDERED Must be AFTER hanging this into the tree
		childChanged();
	}

	public void add(final int index, final NodeBase nd) {
		if(!canContain(nd))
			throw new IllegalStateException("This node " + this + " cannot contain a " + nd);
		if(m_children == Collections.EMPTY_LIST)
			m_children = new ArrayList<NodeBase>();
		if(index > m_children.size())
			throw new IllegalStateException("Adding a child at index=" + index + ", but childlist size is " + m_children.size());
		if(nd instanceof TextNode)
			setMustRenderChildrenFully();
		treeChanging();
		//		registerWithPage(nd);			// jal 20080929 Moved downwards to allow tree to be visible at onAddedToPage() event time
		m_children.add(index, nd);
		nd.setParent(this);
		registerWithPage(nd); // ORDERED Must be AFTER hanging this into the tree
		childChanged();
	}

	public void addLiteral(final String txt) {
		if(txt != null)
			add(new TextNode(txt));
	}

	public void add(final BundleRef ref, final String k) {
		addLiteral(ref.getString(k));
	}

	void registerChildren() {
		for(NodeBase ch : m_children) {
			ch.registerWithPage(getPage());
		}
	}

	private void registerWithPage(final NodeBase child) {
		if(getPage() == null) // No page-> cannot register
			return;
		child.registerWithPage(getPage());
	}

	@Override
	void registerWithPage(final Page p) {
		super.registerWithPage(p); // Base registration of *this*
		registerChildren();
	}

	/**
	 * Remove a child node from me. This also removes ALL descendants from the current page's view.
	 * @param child
	 */
	public void removeChild(final NodeBase child) {
		if(child.getParent() != this)
			throw new IllegalStateException("Child " + child + " is not a child of container " + this);
		int ix = m_children.indexOf(child);
		if(ix == -1)
			throw new IllegalStateException("Child " + child + " was not in list!? " + this);
		treeChanging();
		m_children.remove(ix);
		child.setParent(null);
		child.unregisterFromPage();
		childChanged();
	}

	public NodeBase removeChild(final int index) {
		if(index < 0 || index >= m_children.size())
			throw new IllegalStateException("Bad delete index " + index + " on node " + this + " with " + m_children.size() + " children");
		treeChanging();
		NodeBase child = m_children.remove(index);
		child.setParent(null);
		child.unregisterFromPage();
		childChanged();
		return child;
	}

	public void replaceChild(final NodeBase child, final NodeBase nw) {
		//-- Find old child's index.
		if(child.getParent() != this)
			throw new IllegalStateException("Child " + child + " is not a child of container " + this);
		int ix = m_children.indexOf(child);
		if(ix == -1)
			throw new IllegalStateException("Child " + child + " was not in list!? " + this);
		treeChanging();
		m_children.set(ix, nw); // Replace inline
		child.setParent(null);
		child.unregisterFromPage();
		nw.setParent(this);
		registerWithPage(nw); // ORDERED Must be AFTER hanging this into the tree
		childChanged();
	}

	/**
	 * Discard all children.
	 */
	public void removeAllChildren() {
		if(m_children.size() == 0)
			return;
		treeChanging();
		m_childHasUpdates = false; // They're gone.... No changes I guess.
		m_mustRenderChildrenFully = true; // Just render all my children again
		for(NodeBase b : m_children) {
			b.setParent(null);
			b.unregisterFromPage();
		}
		m_children.clear();
	}

	public int findChildIndex(final NodeBase b) {
		if(b.getParent() != this)
			return -1;
		return m_children.indexOf(b);
	}

	@Override
	void unregisterFromPage() {
		for(NodeBase b : m_children)
			b.unregisterFromPage();
		super.unregisterFromPage();
	}

	/**
	 * This destroys all existing nodes and causes this node to be rebuilt the next time the
	 * tree is rendered.
	 */
	public void forceRebuild() {
		//-- If we have nodes destroy 'm all
		removeAllChildren(); // Remove all old crap
		treeChanging();
		clearBuilt();
	}

	/**
	 * ONLY USE FOR CALCULATED DATA - Use setText(BundleRef, String) to make the application
	 * language-independent!
	 * Convenience method to change the text content of this node. This ensures
	 * that only one textnode remains as the child, and that node contains the
	 * specified text.
	 * @param txt
	 */
	public void setButtonText(final String txt) {
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
		TextNode t = new TextNode(txt);
		add(t);
	}

	/**
	 * Convenience method to change the text content of this node. This ensures
	 * that only one textnode remains as the child, and that node contains the
	 * specified text.
	 *
	 * @param ref		The bundle containing the message.
	 * @param key		The key to use.
	 */
	public void setText(final BundleRef ref, final String key) {
		setButtonText(ref.getString(key));
	}

	@Override
	public boolean validate() {
		boolean ok = true;
		for(NodeBase nb : m_children) {
			if(!nb.validate())
				ok = false;
		}
		return ok;
	}

	public IErrorFence getErrorFence() {
		return m_errorFence;
	}

	public void setErrorFence(final IErrorFence errorFence) {
		m_errorFence = errorFence;
	}

	public void setErrorFence() {
		if(m_errorFence == null)
			m_errorFence = new ErrorFenceHandler(this);
	}

	@Override
	protected void internalShelve() throws Exception {
		onShelve();
		for(int i = m_children.size(); --i >= 0;)
			m_children.get(i).internalShelve();
	}

	@Override
	protected void internalUnshelve() throws Exception {
		onUnshelve();
		for(int i = m_children.size(); --i >= 0;)
			m_children.get(i).internalUnshelve();
	}
}
