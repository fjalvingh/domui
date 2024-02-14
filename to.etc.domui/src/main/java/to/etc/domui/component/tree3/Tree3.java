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
package to.etc.domui.component.tree3;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.tbl.ICellClicked;
import to.etc.domui.component.tbl.ICellClicked2;
import to.etc.domui.component.tree.INodePredicate;
import to.etc.domui.component.tree.ITreeModel;
import to.etc.domui.component.tree.ITreeModelChangedListener;
import to.etc.domui.dom.Animations;
import to.etc.domui.dom.html.ClickInfo;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.IClicked2;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Ul;
import to.etc.domui.server.DomApplication;
import to.etc.domui.util.IRenderInto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a Tree component. It uses the {@link ITreeModel} to represent the tree's data, and is
 * a replacement for the old Tree component. Improvements are:
 * <ul>
 *     <li>The new tree does not use tables. This prevents odd browser rendering behavior.</li>
 *     <li>The code has been greatly cleaned up.</li>
 *     <li>The component uses jquery effects to slide up and down expanded nodes.</li>
 * </ul>
 */
public class Tree3<T> extends Div implements ITreeModelChangedListener<T> {
	private ITreeModel<T> m_model;

	private boolean m_showRoot;

	private Ul m_rootDisplayNode;

	private boolean m_expandRoot;

	private Map<Object, Tree3Node<T>> m_openMap = new HashMap<>();

	/** The specified ComboRenderer used. */
	private IRenderInto<T> m_contentRenderer;

	private IRenderInto<T> m_actualContentRenderer;

	private Class< ? extends IRenderInto<T>> m_contentRendererClass;

	private PropertyMetaModel< ? > m_propertyMetaModel;

	private ICellClicked<T> m_cellClicked;

	private ICellClicked2<T> m_cellClicked2;

	private INodePredicate<T> m_nodeSelectablePredicate;

	@Nullable
	private T m_selectedValue;

	private boolean m_enableDoubleclickExpand = true;

	public Tree3() {
		setCssClass("ui-tree3");
	}

	public Tree3(ITreeModel<T> model) {
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

		Ul ul = m_rootDisplayNode = new Ul("ui-tree3-list ui-tree3-rootlist");
		if(isShowRoot()) {
			Tree3Node<T> n = gettree3Node(root);		// Pre-create the node
			n.setExpanded(true);							// and set it to expanded
			renderItem(ul, root, true);
		} else {
			//-- Render the root thingy && create the 1st visibleNode
			Tree3Node<T> n = gettree3Node(root);
			n.setExpanded(true);
			renderList(ul, n);
		}

		if(m_expandRoot) {
			for(int i = getModel().getChildCount(root); --i >= 0;) {
				T v = getModel().getChild(root, i);
				expandNode(v);
			}
		}
		add(m_rootDisplayNode);
	}

	/**
	 * Get or create a thingy for a visible node.
	 */
	private Tree3Node<T> gettree3Node(T base) throws Exception {

		Tree3Node<T> n = m_openMap.get(base);
		if(n == null) {
			n = new Tree3Node<>(base, getModel().getRoot() == base);
			m_openMap.put(base, n);
		}
		return n;
	}

	@Nullable
	public NodeContainer getNodeContent(T base) {
		Tree3Node<T> n = m_openMap.get(base);
		if(null == n)
			return null;
		return n.getContent();
	}

	private void renderList(Ul into, Tree3Node<T> parent) throws Exception {
		into.removeAllChildren();
		T parentValue = parent.getValue();
		parent.setChildRoot(into);

		int len = getModel().getChildCount(parentValue);// #of items in this thingy.
		if(len == 0) {
			return;										// If the root node has no children this happens.
		}

		//-- Render each child && assign their tree3Node thingy.
		for(int i = 0; i < len; i++) {
			final T item = getModel().getChild(parentValue, i); // Get ith child
			renderItem(into, item, i == (len - 1));
		}
	}

	private Tree3Node<T> renderItem(Ul parentNode, T item, boolean last) throws Exception {
		Tree3Node<T> li = gettree3Node(item);			// Get/create the node
		parentNode.add(li);								// Add the UI node

		//-- Render content cell data
		renderContent(li.getContent(), item);

		if(!getModel().hasChildren(item) || li.isUnExpandable()) {
			li.setType(last ? Tree3NodeType.LEAF_LAST : Tree3NodeType.LEAF);
			li.setUnExpandable(true);
			li.setExpanded(false);
		} else {
			//img.setCssClass("ui-tree3-act");
			boolean expanded = isExpanded(item); // Expanded?
			if(expanded) {
				/*
				 * Expanded node: add the expanded thing here, then expand the data into
				 * a separate cell.
				 */
				li.setType(last ? Tree3NodeType.OPENED_LAST : Tree3NodeType.OPENED);
				Ul childUl = new Ul("ui-tree3-list");
				renderList(childUl, li);
				li.add(childUl);
				li.setChildRoot(childUl);
				li.setFoldingClicked((IClicked<NodeContainer>) bxx -> collapseNode(item, true));
			} else {
				li.setType(last ? Tree3NodeType.CLOSED_LAST : Tree3NodeType.CLOSED);
				li.setFoldingClicked((IClicked<NodeContainer>) bxx -> expandNode(item, true));
			}
		}
		return li;
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
		if(path.isEmpty())
			throw new IllegalStateException("No TREE path found to node=" + item);
		Object root = getModel().getRoot();
		if(root != path.get(0))
			throw new IllegalStateException("Tree path does NOT start with the root node-> model implementation sucks?");

		//-- The thing is visible. We need to re-render where needed.
		for(final T pathValue : path) {
			Tree3Node<T> vn = gettree3Node(pathValue);
			vn.setExpanded(true);

			if(vn.getChildRoot() == null && vn.isAttached() /* if root is not visible skip */) {
				/*
				 * Not currently visibly expanded.. Do the in-table expansion of this node by replacing
				 * it's TR image with a 'collapse' image OR a LEAF image if we determine here the thing
				 * is a lazy leaf.
				 */
				int rowix = vn.getParent().findChildIndex(vn); // Row's index in it's parent
				boolean last = vn.getParent().getChildCount() == rowix + 1;
				int len = getModel().getChildCount(pathValue);
				if(len == 0) {
					/*
					 * This node was lazily-unexpandable: hasChildren has returned T but the #of children is 0. Make it
					 * a leaf and change it's image.
					 */
					vn.setType(last ? Tree3NodeType.LEAF_LAST : Tree3NodeType.LEAF);
					vn.setExpanded(false); 				// Cannot expand
					vn.setUnExpandable(true);
					vn.setFoldingClicked(null);		// Make sure Click handler is discarded
				} else {
					/*
					 * An unexpanded non-leaf node: change its icon to "closeable" and change the click handler.
					 */
					vn.setType(last ? Tree3NodeType.OPENED_LAST : Tree3NodeType.OPENED);
					//img.addCssClass("ui-tree3-act");
					vn.setFoldingClicked((IClicked<NodeContainer>) bxx -> collapseNode(pathValue, true));
					Ul childUl = new Ul("ui-tree3-list");
					renderList(childUl, vn);
					vn.setChildRoot(childUl);
					vn.add(childUl);

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
		Tree3Node<T> vn = m_openMap.get(item);
		if(vn == null || !vn.isExpanded())
			return;

		//-- We have a node... We must discard all tree3Nodes after this node;
		removeAllChildrenFromMap(vn);
		getModel().collapseChildren(item);
		vn.setExpanded(false);

		//-- Collapse the node. Get the base of the presentation,

		int rowix = vn.getParent().findChildIndex(vn); // Row's index in it's parent
		boolean last = vn.getParent().getChildCount() == rowix + 1;

		vn.setType(last ? Tree3NodeType.CLOSED_LAST : Tree3NodeType.CLOSED);
		//img.addCssClass("ui-tree3-act");
		vn.setFoldingClicked((IClicked<NodeContainer>) bxx -> expandNode(item, true));

		Ul ul = vn.getChildRoot();
		if(null == ul)
			return;

		if(animate) {
			Animations.slideUpAndRemove(ul);
		} else {
			ul.remove();
		}
		vn.setChildRoot(null);
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

	private void removeAllChildrenFromMap(Tree3Node<T> vnbase) throws Exception {
		Ul childRoot = vnbase.getChildRoot();
		if(childRoot == null)
			return;
		int ix = 0;
		for(NodeBase nodeBase : childRoot) {
			Tree3Node<T> vn = (Tree3Node<T>) nodeBase;
			if(vn == null)
				throw new IllegalStateException("?? Element " + ix + " of parent=" + vnbase.getValue() + " is null???");
			m_openMap.remove(vn.getValue());
			removeAllChildrenFromMap(vn);
			if(vn.isExpanded())
				getModel().collapseChildren(vn.getValue());
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
		return getModel().getTreePath(item);
	}

	private IRenderInto<T> calculateContentRenderer(Object val) {
		if(m_contentRenderer != null)
			return m_contentRenderer;
		if(m_contentRendererClass != null)
			return DomApplication.get().createInstance(m_contentRendererClass);

		if(val == null)
			throw new IllegalStateException("Cannot calculate content renderer for null value");
		ClassMetaModel cmm = MetaManager.findClassMeta(val.getClass());
		IRenderInto<Object> rr = (IRenderInto<Object>) MetaManager.createDefaultComboRenderer(m_propertyMetaModel, cmm);
		return (node, object) -> rr.render(node, object);
	}

	private void renderContent(@NonNull final NodeContainer cell, @Nullable final T value) throws Exception {
		cell.removeAllChildren();
		if(m_actualContentRenderer == null)
			m_actualContentRenderer = calculateContentRenderer(value);
		m_actualContentRenderer.renderOpt(cell, value);

		if(isSelectable(value)) {
			cell.addCssClass("ui-tree3-selectable");
			if(isSelected(value))
				cell.addCssClass("ui-tree3-selected");

			cell.setClicked2(new IClicked2<NodeContainer>() {
				@Override
				public void clicked(@NonNull NodeContainer node, @NonNull ClickInfo clinfo) throws Exception {
					// FIXME This means null root nodes cannot be clicked
					if(null != value) {
						cellClicked(value, clinfo);
					}
					if(m_enableDoubleclickExpand && clinfo.isDoubleClick()) {
						toggleNode(value);
					}
				}
			});
		}
		updateSelectable(cell, value);
	}

	private void updateSelectable(@NonNull NodeContainer cell, @Nullable T value) throws Exception {
		INodePredicate<T> predicate = m_nodeSelectablePredicate;
		if(null != predicate) {
			boolean isSelectable = predicate.predicate(value);
			if(isSelectable) {
				cell.addCssClass("ui-tree3-selectable");
				cell.removeCssClass("ui-tree3-unselectable");
			} else {
				cell.addCssClass("ui-tree3-unselectable");
				cell.removeCssClass("ui-tree3-selectable");
			}
		}
	}

	protected void cellClicked(@NonNull final T value, @NonNull ClickInfo clinfo) throws Exception {
		if(isSelectable(value)) {
			T selected = m_selectedValue;
			if(selected != value) {

				m_selectedValue = value;
				if(null != selected) {
					markNewSelection(selected, false);
				}
				markNewSelection(value, true);
			}
		}

		ICellClicked2<T> c2 = getCellClicked2();
		if(null != c2)
			c2.cellClicked(value, clinfo);
		ICellClicked<T> cl = getCellClicked();
		if(cl != null)
			cl.cellClicked(value);
	}

	/**
	 * Returns T if this node is currently expanded (opened).
	 */
	public boolean isExpanded(T node) {
		Tree3Node<T> vn = m_openMap.get(node);
		if(vn == null)
			return false;
		return vn.isExpanded();
	}


	protected boolean isSelectable(@Nullable T node) throws Exception {
		if(getCellClicked() == null && getCellClicked2() == null)
			return false;
		if(m_nodeSelectablePredicate == null)
			return true;
		return m_nodeSelectablePredicate.predicate(node);
	}

	/**
	 * Internal use: set or reset the 'selected' indication on the visible nodeValue.
	 */
	//protected void markAsSelected(T nodeValue, boolean selected) throws Exception {
	//	if(null != nodeValue)
	//		expandNode(nodeValue);
	//
	//	markNewSelection(nodeValue, selected);
	//}

	private void markNewSelection(T nodeValue, boolean selected) throws Exception {
		Tree3Node<T> vn = m_openMap.get(nodeValue);
		if(vn == null)
			return;
		vn.internalSetSelected(selected);
		NodeContainer content = vn.getContent();
		content.removeAllChildren();
		renderContent(content, nodeValue);
	}

	@Nullable
	public Tree3Node<T> locateRowIfExpanded(T node){
		Tree3Node<T> vn = m_openMap.get(node);
		if (null != vn){
			return vn;
		}
		return null;
	}

	protected boolean isSelected(T node) {
		return node == m_selectedValue;
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

	public IRenderInto<T> getActualContentRenderer() {
		return m_actualContentRenderer;
	}

	IRenderInto < ? > getContentRenderer() {
		return m_contentRenderer;
	}

	public void setContentRenderer(IRenderInto<T> contentRenderer) {
		m_contentRenderer = contentRenderer;
	}

	public Class< ? extends IRenderInto< ? >> getContentRendererClass() {
		return m_contentRendererClass;
	}

	public void setContentRendererClass(Class< ? extends IRenderInto<T>> contentRendererClass) {
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
	public void onNodeAdded(@Nullable T parent, int index, T node) throws Exception {
		Tree3Node<T> parentVn = locateRowIfExpanded(parent);
		if(null == parentVn)
			return;

		//parentVn.forceRebuild();
		Ul ul = parentVn.getChildRoot();
		if(null == ul) {
			expandNode(parent);
			ul = parentVn.getChildRoot();
			if(null == ul)
				throw new IllegalStateException("There is no UI container for parent node " + parent);
		}
		renderList(ul, parentVn);
	}

	@Override
	public void onNodeUpdated(T node) throws Exception {
		Tree3Node<T> vn = locateRowIfExpanded(node);
		if(null == vn)
			return;
		renderContent(vn.getContent(), node);
	}

	@Override
	public void onNodeRemoved(@Nullable T oldParent, int oldIndex, T deletedNode) throws Exception {
		Tree3Node<T> parentVn = locateRowIfExpanded(oldParent);
		if(null == parentVn)
			return;
		Ul ul = parentVn.getChildRoot();
		if(null != ul) {
			renderList(ul, parentVn);
		}
	}

	@Nullable
	public T getSelectedValue() {
		return m_selectedValue;
	}

	public void setSelectedValue(@Nullable T selectedValue) throws Exception {
		T old = m_selectedValue;
		if(old == selectedValue)
			return;

		m_selectedValue = selectedValue;
		if(null != old) {
			markNewSelection(old, false);
		}
		if(null != selectedValue) {
			T parent = getModel().getParent(selectedValue);
			if(null != parent) {
				expandNode(parent);
			}
			markNewSelection(selectedValue, true);
		}
	}

	public ICellClicked2<T> getCellClicked2() {
		return m_cellClicked2;
	}

	public void setCellClicked2(ICellClicked2<T> cellClicked2) {
		m_cellClicked2 = cellClicked2;
	}

	public boolean isEnableDoubleclickExpand() {
		return m_enableDoubleclickExpand;
	}

	public void setEnableDoubleclickExpand(boolean enableDoubleclickExpand) {
		m_enableDoubleclickExpand = enableDoubleclickExpand;
	}
}
