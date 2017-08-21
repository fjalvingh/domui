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
package to.etc.domui.component.tree2;

import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.tbl.ICellClicked;
import to.etc.domui.component.tree.INodePredicate;
import to.etc.domui.component.tree.ITreeModel;
import to.etc.domui.component.tree.ITreeModelChangedListener;
import to.etc.domui.dom.Animations;
import to.etc.domui.dom.html.ATag;
import to.etc.domui.dom.html.ClickInfo;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.IClicked2;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Ul;
import to.etc.domui.server.DomApplication;
import to.etc.domui.util.INodeContentRenderer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Replacement for the Tree component, not using tables.
 */
public class Tree2<T> extends Div implements ITreeModelChangedListener<T> {
	private ITreeModel<T> m_model;

	private boolean m_showRoot;

	private Ul m_rootTable;

	private boolean m_expandRoot;

	private Map<Object, VisibleNode<T>> m_openMap = new HashMap<Object, VisibleNode<T>>();

	/** The specified ComboRenderer used. */
	private INodeContentRenderer< ? > m_contentRenderer;

	private INodeContentRenderer<T> m_actualContentRenderer;

	private Class< ? extends INodeContentRenderer<T>> m_contentRendererClass;

	private PropertyMetaModel< ? > m_propertyMetaModel;

	private ICellClicked<T> m_cellClicked;

	private INodePredicate<T> m_nodeSelectablePredicate;

	/**
	 * Represents the internal visible state of the tree.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Oct 20, 2008
	 */
	static private class VisibleNode<V> {
		final V data;

		/** The first row of the node's data. */
		Tree2Node<V> nodeRow;

		//ATag content;
		//
		//TreeIcon icon;
		//
		//Ul childRoot;

		/** If this is an expanded node this contains the expanded children's nodes. */
		VisibleNode<V>[] childNodes;

		boolean expanded;

		boolean unexpandable;

		public VisibleNode(V data) {
			this.data = data;
		}
	}

	public Tree2() {
		setCssClass("ui-tree2");
	}

	public Tree2(ITreeModel<T> model) {
		this();
		setModel(model);
	}

	/**
	 * Main initial renderer.
	 */
	@Override
	public void createContent() throws Exception {
		//-- The root node is always expanded, of course
		T root = getModel().getRoot();

		if(isShowRoot()) {
			Ul ul = m_rootTable = new Ul("ui-tree2-rootlist");
			VisibleNode<T> n = getVisibleNode(root);
			n.expanded = true;
			renderItem(ul, n, true);
		} else {
			//-- Render the root thingy && create the 1st visibleNode
			VisibleNode<T> n = getVisibleNode(root);
			n.expanded = true;
			m_rootTable = renderList(root, n);
		}

		if(m_expandRoot) {
			for(int i = getModel().getChildCount(root); --i >= 0;) {
				T v = getModel().getChild(root, i);
				expandNode(v);
			}
		}
		add(m_rootTable);
	}

	/**
	 * Get or create a thingy for a visible node.
	 */
	private VisibleNode<T> getVisibleNode(T base) {
		VisibleNode<T> n = m_openMap.get(base);
		if(n == null) {
			n = new VisibleNode<T>(base);
			m_openMap.put(base, n);
		}
		return n;
	}

	private Ul renderList(T parentValue, VisibleNode<T> parentInfo) throws Exception {
		Ul ul = new Ul("ui-tree2-list");

		int len = getModel().getChildCount(parentValue); // #of items in this thingy.
		if(len == 0) {
			throw new IllegalStateException("Implement 'expanding node having 0 children': parentValue=" + parentValue);
		}

		//-- Render each child && assign their VisibleNode thingy.
		VisibleNode<T>[] vnar = new VisibleNode[len];
		for(int i = 0; i < len; i++) {
			final T item = getModel().getChild(parentValue, i); // Get ith child
			VisibleNode<T> chvn = getVisibleNode(item);
			vnar[i] = chvn;
			renderItem(ul, chvn, i == (len - 1));
		}
		parentInfo.childNodes = vnar;
		return ul;
	}

	private void renderItem(Ul parentNode, VisibleNode<T> chvn, boolean last) throws Exception {
		final T item = chvn.data;

		Tree2Node<T> li = new Tree2Node<>(item);
		parentNode.add(li);
		chvn.nodeRow = li;

		//-- Render content cell data
		renderContent(li.getContent(), item);

		if(!getModel().hasChildren(item) || chvn.unexpandable) {
			li.setType(last ? TreeNodeType.LEAF_LAST : TreeNodeType.LEAF);
			chvn.unexpandable = true;
			chvn.expanded = false;
		} else {
			//img.setCssClass("ui-tree2-act");
			boolean expanded = isExpanded(item); // Expanded?
			if(expanded) {
				/*
				 * Expanded node: add the expanded thing here, then expand the data into
				 * a separate cell.
				 */
				li.setType(last ? TreeNodeType.OPENED_LAST : TreeNodeType.OPENED);
				Ul childUl = renderList(item, chvn);
				li.add(childUl);
				li.setChildRoot(childUl);
				li.getIcon().setClicked((IClicked<NodeContainer>) bxx -> collapseNode(item, true));
			} else {
				li.setType(last ? TreeNodeType.CLOSED_LAST : TreeNodeType.CLOSED);
				li.getIcon().setClicked((IClicked<NodeContainer>) bxx -> expandNode(item, true));
			}
		}
	}

	public void expandNode(T item) throws Exception {
		expandNode(item, false);
	}

	/**
	 * Force the specified node to expand. If the node is a leaf then the entire
	 * path up to that leaf is expanded. The inner logic is as follows:
	 * <ul>
	 *	<li>Determine the full TreePath to the node. This defines all nodes that need to be expanded
	 *		to display the target node.</li>
	 *	<li>For each node in the path check expansion state; if the node is expanded we're done, but if
	 *		not we need to expand it, so render the expanded state in the tree.
	 * </ul>
	 */
	public void expandNode(T item, boolean animate) throws Exception {
		getModel().expandChildren(item);
		List<T> path = getTreePath(item); // Calculate a path.
		if(path.size() == 0)
			throw new IllegalStateException("No TREE path found to node=" + item);
		Object root = getModel().getRoot();
		if(root != path.get(0))
			throw new IllegalStateException("Tree path does NOT start with the root node-> model implementation sucks?");

		//-- The thing is visible. We need to re-render where needed.
		for(final T pathValue : path) {
			VisibleNode<T> vn = getVisibleNode(pathValue);
			vn.expanded = true;

			if(vn.childNodes == null && vn.nodeRow != null /* if root is not visible skip */) {
				/*
				 * Not currently visibly expanded.. Do the in-table expansion of this node by replacing
				 * it's TR image with a 'collapse' image OR a LEAF image if we determine here the thing
				 * is a lazy leaf.
				 */
				Tree2Node<T> row = vn.nodeRow;
				int rowix = row.getParent().findChildIndex(row); // Row's index in it's parent
				boolean last = row.getParent().getChildCount() == rowix + 1;
				int len = getModel().getChildCount(pathValue);
				if(len == 0) {
					/*
					 * This node was lazily-unexpandable: hasChildren has returned T but the #of children is 0. Make it
					 * a leaf and change it's image.
					 */
					row.setType(last ? TreeNodeType.LEAF_LAST : TreeNodeType.LEAF);
					vn.expanded = false; 				// Cannot expand
					vn.unexpandable = true;
					row.getIcon().setClicked(null);		// Make sure Click handler is discarded
				} else {
					/*
					 * An unexpanded non-leaf node: change its icon to "closeable" and change the click handler.
					 */
					row.setType(last ? TreeNodeType.OPENED_LAST : TreeNodeType.OPENED);
					//img.addCssClass("ui-tree2-act");
					row.getIcon().setClicked((IClicked<NodeContainer>) bxx -> collapseNode(pathValue, true));
					Ul childUl = renderList(item, vn);
					row.setChildRoot(childUl);
					row.add(childUl);

					if(animate) {
						Animations.slideDown(childUl);
					}
				}
			}
		}
	}


	/**
	 * Force the specified node to collapse. This also collapses all nodes after it, of course.
	 */
	public void collapseNode(final T item, boolean animate) throws Exception {
		VisibleNode<T> vn = m_openMap.get(item);
		if(vn == null || !vn.expanded)
			return;

		//-- We have a node... We must discard all VisibleNodes after this node;
		dropCrud(vn);
		getModel().collapseChildren(item);
		vn.expanded = false;
		vn.childNodes = null;

		//-- Collapse the node. Get the base of the presentation,
		Tree2Node<T> row = vn.nodeRow;

		int rowix = row.getParent().findChildIndex(row); // Row's index in it's parent
		boolean last = row.getParent().getChildCount() == rowix + 1;

		row.setType(last ? TreeNodeType.CLOSED_LAST : TreeNodeType.CLOSED);
		//img.addCssClass("ui-tree2-act");
		row.getIcon().setClicked((IClicked<NodeContainer>) bxx -> expandNode(item, true));

		Ul ul = row.getChildRoot();
		if(null == ul)
			return;

		if(animate) {
			Animations.slideUpAndRemove(ul);
		} else {
			ul.remove();
		}
		row.setChildRoot(null);
	}

	public void collapseAll() throws Exception {
		T item = getModel().getRoot();

		for(int i = 0; i < getModel().getChildCount(item); i++) {
			T xx = getModel().getChild(item, i);
			collapseNode(xx, false);
		}
	}

	public void toggleNode(T item) throws Exception {
		if(isExpanded(item))
			collapseNode(item, false);
		else
			expandNode(item);
	}

	private void dropCrud(VisibleNode<T> vnbase) throws Exception {
		if(vnbase.childNodes == null)
			return;
		int ix = 0;
		for(VisibleNode<T> vn : vnbase.childNodes) {
			if(vn == null)
				throw new IllegalStateException("?? Element " + ix + " of parent=" + vnbase.data + " is null???");
			m_openMap.remove(vn.data);
			dropCrud(vn);
			if(vn.expanded)
				getModel().collapseChildren(vn.data);
			ix++;
		}
	}

	private String branchurl() {
		return getThemedResourceRURL("THEME/tree-branch.png");
	}

	/**
	 * Calculates a tree path for a given node, as a set of nodes that walk to the item. The
	 * root element is always the 1st element in the treepath
	 */
	public List<T> getTreePath(T item) throws Exception {
		List<T> path = new ArrayList<T>();
		addParentPath(path, item);
		return path;
	}

	private void addParentPath(List<T> path, T item) throws Exception {
		T parent = getModel().getParent(item);
		/*
		 * jal 20081127 The explicit compare with the root node is needed because we allow the root
		 * node to be null. In that case the path to the item MUST start with null (representing the
		 * root node).
		 */
		if(parent == getModel().getRoot()) {
			path.add(parent);
		} else if(parent != null) {
			addParentPath(path, parent);
		}
		path.add(item);
	}

	private INodeContentRenderer< ? > calculateContentRenderer(Object val) {
		if(m_contentRenderer != null)
			return m_contentRenderer;
		if(m_contentRendererClass != null)
			return DomApplication.get().createInstance(m_contentRendererClass);

		if(val == null)
			throw new IllegalStateException("Cannot calculate content renderer for null value");
		ClassMetaModel cmm = MetaManager.findClassMeta(val.getClass());
		return MetaManager.createDefaultComboRenderer(m_propertyMetaModel, cmm);
	}

	private void renderContent(final NodeContainer cell, final T value) throws Exception {
		if(m_actualContentRenderer == null)
			m_actualContentRenderer = (INodeContentRenderer<T>) calculateContentRenderer(value);
		m_actualContentRenderer.renderNodeContent(this, cell, value, this);

		if(isSelectable(value)) {
			cell.addCssClass("ui-tree2-selectable");
			if(isSelected(value))
				cell.addCssClass("ui-tree2-selected");

			cell.setClicked(new IClicked2<NodeContainer>() {
				@Override
				public void clicked(@Nonnull NodeContainer node, @Nonnull ClickInfo clinfo) throws Exception {
					cellClicked(value, clinfo);
				}
			});
		}
		updateSelectable(cell, value);
	}

	private void updateSelectable(@Nonnull NodeContainer cell, @Nonnull T value) throws Exception {
		INodePredicate<T> predicate = m_nodeSelectablePredicate;
		if(null != predicate) {
			boolean isSelectable = predicate.predicate(value);
			if(isSelectable) {
				cell.addCssClass("ui-tree2-selectable");
				cell.removeCssClass("ui-tree2-unselectable");
			} else {
				cell.addCssClass("ui-tree2-unselectable");
				cell.removeCssClass("ui-tree2-selectable");
			}
		}
	}

	protected void cellClicked(@Nonnull final T value, @Nonnull ClickInfo clinfo) throws Exception {
		if(getCellClicked() != null)
			((ICellClicked<Object>) getCellClicked()).cellClicked(value);
	}

	/**
	 * Returns T if this node is currently expanded (opened).
	 */
	public boolean isExpanded(T node) {
		VisibleNode<T> vn = m_openMap.get(node);
		if(vn == null)
			return false;
		return vn.expanded;
	}


	protected boolean isSelectable(@Nonnull T node) throws Exception {
		if(getCellClicked() == null)
			return false;
		if(m_nodeSelectablePredicate == null)
			return true;
		return m_nodeSelectablePredicate.predicate(node);
	}

	/**
	 * Internal use: set or reset the 'selected' indication on the visible nodeValue.
	 */
	protected void markAsSelected(T nodeValue, boolean selected) throws Exception {
		if(null != nodeValue)
			expandNode(nodeValue);

		VisibleNode<T> vn = m_openMap.get(nodeValue);
		if(vn == null)
			return;
		Tree2Node<T> row = vn.nodeRow;
		if(row == null)
			return;
		row.internalSetSelected(selected);
		ATag content = row.getContent();
		content.removeAllChildren();
		renderContent(content, nodeValue);
	}

	@Nullable
	public Tree2Node<T> locateRowIfExpanded(T node){
		VisibleNode<T> vn = m_openMap.get(node);
		if (null != vn){
			return vn.nodeRow;
		}
		return null;
	}

	protected boolean isSelected(@Nonnull T node) {
		return false;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Model updates.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Set a new model for this table. This discards the entire presentation
	 * and causes a full build at render time.
	 */
	public void setModel(ITreeModel<T> model) {
		ITreeModel<T> itm = model; // Stupid Java Generics need cast here
		if(m_model == itm) // If the model did not change at all begone
			return;
		//		ITreeModel<?>	old = m_model;
		if(m_model != null)
			m_model.removeChangeListener(this); // Remove myself from listening to my old model
		m_model = itm;
		m_openMap.clear();
		if(itm != null)
			itm.addChangeListener(this); // Listen for changes on the new model
		forceRebuild(); // Force a rebuild of all my nodes
		//		fireModelChanged(old, model);
	}

	public void setExpandRoot(boolean x) throws Exception {
		if(m_expandRoot == x)
			return;
		m_expandRoot = x;
		if(!x || !isBuilt())
			return;
		T root = getModel().getRoot();
		if(!isExpanded(root))
			expandNode(root);
	}

	public boolean getExpandRoot() {
		return m_expandRoot;
	}

	/**
	 * Get the currently used model.
	 */
	public ITreeModel<T> getModel() {
		return m_model;
	}

	public boolean isShowRoot() {
		return m_showRoot;
	}

	public void setShowRoot(boolean showRoot) {
		m_showRoot = showRoot;
	}

	public INodeContentRenderer< ? > getContentRenderer() {
		return m_contentRenderer;
	}

	public void setContentRenderer(INodeContentRenderer< ? > contentRenderer) {
		m_contentRenderer = contentRenderer;
	}

	public Class< ? extends INodeContentRenderer< ? >> getContentRendererClass() {
		return m_contentRendererClass;
	}

	public void setContentRendererClass(Class< ? extends INodeContentRenderer<T>> contentRendererClass) {
		m_contentRendererClass = contentRendererClass;
	}

	public ICellClicked<T> getCellClicked() {
		return m_cellClicked;
	}

	public void setCellClicked(ICellClicked<T> cellClicked) {
		m_cellClicked = cellClicked;
	}

	public PropertyMetaModel< ? > getPropertyMetaModel() {
		return m_propertyMetaModel;
	}

	public void setPropertyMetaModel(PropertyMetaModel< ? > propertyMetaModel) {
		m_propertyMetaModel = propertyMetaModel;
	}

	public INodePredicate<T> getNodeSelectablePredicate() {
		return m_nodeSelectablePredicate;
	}

	public void setNodeSelectablePredicate(INodePredicate<T> nodeSelectablePredicate) {
		m_nodeSelectablePredicate = nodeSelectablePredicate;
	}

	@Override
	public void onNodeAdded(T parent, int index, T node) {
		throw new IllegalStateException("Not implemented");
	}

	@Override
	public void onNodeUpdated(T node) {
		throw new IllegalStateException("Not implemented");
	}

	@Override
	public void onNodeRemoved(T oldParent, int oldIndex, T deletedNode) {
		throw new IllegalStateException("Not implemented");
	}
}
