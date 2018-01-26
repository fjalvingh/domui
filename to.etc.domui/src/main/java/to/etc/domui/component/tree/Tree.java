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
package to.etc.domui.component.tree;

import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.tbl.ICellClicked;
import to.etc.domui.dom.html.ClickInfo;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.IClicked2;
import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;
import to.etc.domui.dom.html.TR;
import to.etc.domui.dom.html.Table;
import to.etc.domui.server.DomApplication;
import to.etc.domui.util.IRenderInto;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tree<T> extends Div implements ITreeModelChangedListener<T> {
	private ITreeModel<T> m_model;

	private boolean m_showRoot;

	//	private boolean m_expandOnlyOne;

	private Table m_rootTable;

	private boolean m_expandRoot;

	private Map<Object, VisibleNode<T>> m_openMap = new HashMap<Object, VisibleNode<T>>();

	/** The specified ComboRenderer used. */
	private IRenderInto< ? > m_contentRenderer;

	private IRenderInto<T> m_actualContentRenderer;

	private Class< ? extends IRenderInto<T>> m_contentRendererClass;

	private PropertyMetaModel< ? > m_propertyMetaModel;

	private ICellClicked<T> m_cellClicked;

	private INodePredicate<T> m_nodeSelectablePredicate;

	/**
	 * Represents the internal visible state of the tree.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Oct 20, 2008
	 */
	static class VisibleNode<V> {
		final V data;

		/** The first row of the node's data. */
		TR nodeRow;

		/** If this is an expanded node this contains the expanded children's nodes. */
		VisibleNode<V>[] childNodes;

		boolean expanded;

		boolean unexpandable;

		public VisibleNode(V data) {
			this.data = data;
		}
	}

	public Tree() {
		setCssClass("ui-tree");
	}

	public Tree(ITreeModel<T> model) {
		this();
		setModel(model);
	}

	/**
	 * Main initial renderer.
	 * @see to.etc.domui.dom.html.NodeBase#createContent()
	 */
	@Override
	public void createContent() throws Exception {
		//-- The root node is always expanded, of course
		T root = getModel().getRoot();

		if(isShowRoot()) {
			Table t = m_rootTable = new Table();
			t.setCellSpacing("0");
			t.setCellPadding("0");
			TBody b = new TBody();
			t.add(b);
			VisibleNode<T> n = getVisibleNode(root);
			n.expanded = true;
			renderItem(b, n, true);
//
//			m_rootTable = renderList(root, n);
//
//
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
				//				for(int j = getModel().getChildCount(v); --j >= 0;) {
				//					Object w = getModel().getChild(v, j);
				//					expandNode(w);
				//				}
			}
		}
		add(m_rootTable);
	}

	/**
	 * Get or create a thingy for a visible node.
	 * @param base
	 * @return
	 */
	private VisibleNode<T> getVisibleNode(T base) {
		VisibleNode<T> n = m_openMap.get(base);
		if(n == null) {
			n = new VisibleNode<T>(base);
			m_openMap.put(base, n);
		}
		return n;
	}

	private Table renderList(T base, VisibleNode<T> baseInfo) throws Exception {
		Table t = new Table();
		t.setCellSpacing("0");
		t.setCellPadding("0");
		TBody b = new TBody();
		t.add(b);

		int len = getModel().getChildCount(base); // #of items in this thingy.
		if(len == 0) {
			throw new IllegalStateException("Implement 'expanding node having 0 children': base=" + base);
		}

		//-- Render each child && assign their VisibleNode thingy.
		VisibleNode<T>[] vnar = new VisibleNode[len];
		for(int i = 0; i < len; i++) {
			final T item = getModel().getChild(base, i); // Get ith child
			VisibleNode<T> chvn = getVisibleNode(item);
			vnar[i] = chvn;
			renderItem(b, chvn, i == (len - 1));
		}
		baseInfo.childNodes = vnar;
		return t;
	}

	private void renderItem(TBody b, VisibleNode<T> chvn, boolean last) throws Exception {
		final T item = chvn.data;
		chvn.nodeRow = b.addRow();
		TD td = b.addCell();
		Img img = new Img();
		td.add(img);
		img.setImgBorder(0);
		TD cont = b.addCell(); // Content cell
		cont.setCssClass("ui-tr-val");

		//-- Render content cell data
		renderContent(cont, item);

		if(!getModel().hasChildren(item) || chvn.unexpandable) {
			img.setSrc(last ? "THEME/tree-leaf-last.png" : "THEME/tree-leaf.png");
			chvn.unexpandable = true;
			chvn.expanded = false;
		} else {
			img.setCssClass("ui-tr-act");
			boolean expanded = isExpanded(item); // Expanded?
			if(!expanded) {
				img.setSrc(last ? "THEME/tree-closed-last.png" : "THEME/tree-closed.png");
				img.setClicked(new IClicked<Img>() {
					@Override
					public void clicked(@Nonnull Img bxx) throws Exception {
						expandNode(item);
					}
				});
			} else {
				/*
				 * Expanded node: add the expanded thing here, then expand the data into
				 * a separate cell.
				 */
				img.setSrc(last ? "THEME/tree-opened-last.png" : "THEME/tree-opened.png");

				b.addRow(); // Next row contains the CONTENT of the expanded node,
				td = b.addCell(); // TD at the level of + and -, must contain line-down if this is not the last node
				if(!last)
					td.setBackgroundImage(branchurl()); // Vertical line downwards to next + or -
				td = b.addCell(); // Content area for expanded thingerydoo
				Table tc = renderList(item, chvn); // Render item's expanded thingies
				td.add(tc);

				img.setClicked(new IClicked<Img>() {
					@Override
					public void clicked(@Nonnull Img bxx) throws Exception {
						collapseNode(item);
					}
				});
			}
		}
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
	 *
	 * @param item
	 * @throws Exception
	 */
	public void expandNode(T item) throws Exception {
		getModel().expandChildren(item);
		List<T> path = getTreePath(item); // Calculate a path.
		if(path.size() == 0)
			throw new IllegalStateException("No TREE path found to node=" + item);
		Object root = getModel().getRoot();
		if(root != path.get(0))
			throw new IllegalStateException("Tree path does NOT start with the root node-> model implementation sucks?");

		//		if(! isBuilt()) {
		//			//-- Just mark all visible thingies as EXPANDED...
		//			for(Object o: path) {
		//				VisibleNode vn = getVisibleNode(o);
		//				vn.expanded = true;
		//			}
		//			return;
		//		}

		//-- The thing is visible. We need to re-render where needed.
		for(final T o : path) {
			VisibleNode<T> vn = getVisibleNode(o);
			vn.expanded = true;

			if(vn.childNodes == null && vn.nodeRow != null /* if root is not visible skip */) {
				/*
				 * Not currently visibly expanded.. Do the in-table expansion of this node by replacing
				 * it's TR image with a 'collapse' image OR a LEAF image if we determine here the thing
				 * is a lazy leaf.
				 */
				TR row = vn.nodeRow;
				TD td = (TD) row.getChild(0); // 1st cell containing the image
				Img img = (Img) td.getChild(0); // The image,
				int rowix = row.getParent().findChildIndex(row); // Row's index in it's parent
				boolean last = row.getParent().getChildCount() == rowix + 1;
				int len = getModel().getChildCount(o);
				if(len == 0) {
					/*
					 * This node was lazily-unexpandable: hasChildren has returned T but the #of children is 0. Make it
					 * a leaf and change it's image.
					 */
					img.remove(); // Drop old image
					img = new Img(last ? "THEME/tree-leaf-last.png" : "THEME/tree-leaf.png");
					td.add(img);
					vn.expanded = false; // Cannot expand
					vn.unexpandable = true;
					img.setClicked(null); // Make sure Click handler is discarded FIXME Does this work??
				} else {
					/*
					 * An unexpanded non-leaf node: change it's icon to "closeable" and change the click handler.
					 */
					img.remove();
					img = new Img(last ? "THEME/tree-opened-last.png" : "THEME/tree-opened.png");
					td.add(img);
					img.setCssClass("ui-tr-act");
					img.setClicked(new IClicked<Img>() {
						@Override
						public void clicked(@Nonnull Img bxx) throws Exception {
							collapseNode(o);
						}
					});

					/*
					 * Now add a row AFTER the current row, containing the EXPANDED nodes.
					 */
					TR nr = new TR(); // New row for the expanded thingies
					row.getParent().add(rowix + 1, nr); // Append the row AFTER the current earlier unexpanded row
					td = nr.addCell(); // TD at the level of + and -, must contain line-down if this is not the last node
					if(!last)
						td.setBackgroundImage(branchurl()); // Vertical line downwards to next + or -
					td = nr.addCell(); // Content area for expanded thingerydoo

					//					Table tc = renderList(item, vn); // Render item's expanded thingies jal: should be not ITEM but thing being expanded!!!!

					Table tc = renderList(o, vn); // Render item's expanded thingies
					td.add(tc);

					img.setClicked(new IClicked<Img>() {
						@Override
						public void clicked(@Nonnull Img bxx) throws Exception {
							collapseNode(o);
						}
					});
				}
			}
		}
	}


	/**
	 * Force the specified node to collapse. This also collapses all nodes after it, of course.
	 * @param item
	 * @throws Exception
	 */
	public void collapseNode(final T item) throws Exception {
		VisibleNode<T> vn = m_openMap.get(item);
		if(vn == null || !vn.expanded)
			return;

		//-- We have a node... We must discard all VisibleNodes after this node;
		dropCrud(vn);
		getModel().collapseChildren(item);
		vn.expanded = false;
		vn.childNodes = null;

		//-- Collapse the node. Get the base of the presentation,
		TR row = vn.nodeRow;
		TD td = (TD) row.getChild(0); // 1st cell containing the image
		Img img = (Img) td.getChild(0); // The image,
		int rowix = row.getParent().findChildIndex(row); // Row's index in it's parent
		boolean last = row.getParent().getChildCount() == rowix + 2; // 2: current row == node to collapse, row after it is expanded data; row after THAT would be another node.
		img.remove();
		img = new Img(last ? "THEME/tree-closed-last.png" : "THEME/tree-closed.png");
		td.add(img);
		img.setCssClass("ui-tr-act");
		img.setClicked(new IClicked<Img>() {
			@Override
			public void clicked(@Nonnull Img bxx) throws Exception {
				expandNode(item);
			}
		});

		row.getParent().getChild(rowix + 1).remove(); // Drop the 2nd item
	}

	public void collapseAll() throws Exception {
		T item = getModel().getRoot();

		for(int i = 0; i < getModel().getChildCount(item); i++) {
			T xx = getModel().getChild(item, i);
			collapseNode(xx);
		}
	}

	public void toggleNode(T item) throws Exception {
		if(isExpanded(item))
			collapseNode(item);
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
	 * @param item
	 * @return
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

	private IRenderInto< ? > calculateContentRenderer(Object val) {
		if(m_contentRenderer != null)
			return m_contentRenderer;
		if(m_contentRendererClass != null)
			return DomApplication.get().createInstance(m_contentRendererClass);

		if(val == null)
			throw new IllegalStateException("Cannot calculate content renderer for null value");
		ClassMetaModel cmm = MetaManager.findClassMeta(val.getClass());

		IRenderInto<Object> rr = (IRenderInto<Object>) MetaManager.createDefaultComboRenderer(m_propertyMetaModel, cmm);
		return new IRenderInto<Object>() {
			@Override public void render(@Nonnull NodeContainer node, @Nullable Object object) throws Exception {
				rr.renderOpt(node, object);
			}
		};
	}

	private void renderContent(@Nonnull TD cell, @Nullable T value) throws Exception {
		if(m_actualContentRenderer == null)
			m_actualContentRenderer = (IRenderInto<T>) calculateContentRenderer(value);
		m_actualContentRenderer.renderOpt(cell, value);

		if(isSelectable(value)) {
			cell.addCssClass("ui-tr-sel");
			if(isSelected(value))
				cell.addCssClass("ui-tr-selected");

			cell.setClicked2(new IClicked2<TD>() {
				@Override
				public void clicked(@Nonnull TD node, @Nonnull ClickInfo clinfo) throws Exception {
					// FIXME This means null root nodes cannot be clicked
					if(null != value) {
						cellClicked(cell, value, clinfo);
					}
				}
			});
		}
		updateSelectable(cell, value);
	}

	private void updateSelectable(@Nonnull TD cell, @Nullable T value) throws Exception {
		INodePredicate<T> predicate = m_nodeSelectablePredicate;
		if(null != predicate) {
			boolean isSelectable = predicate.predicate(value);
			if(isSelectable) {
				cell.addCssClass("ui-tr-selectable");
				cell.removeCssClass("ui-tr-unselectable");
			} else {
				cell.addCssClass("ui-tr-unselectable");
				cell.removeCssClass("ui-tr-selectable");
			}
		}
	}

	protected void cellClicked(@Nonnull final TD cell, @Nonnull final T value, @Nonnull ClickInfo clinfo) throws Exception {
		if(getCellClicked() != null)
			getCellClicked().cellClicked(value);
	}

	/**
	 * Returns T if this node is currently expanded (opened).
	 * @param node
	 * @return
	 */
	public boolean isExpanded(T node) {
		VisibleNode<T> vn = m_openMap.get(node);
		if(vn == null)
			return false;
		return vn.expanded;
	}


	protected boolean isSelectable(@Nullable T node) throws Exception {
		if(getCellClicked() == null)
			return false;
		if(m_nodeSelectablePredicate == null)
			return true;
		return m_nodeSelectablePredicate.predicate(node);
	}

	/**
	 * Internal use: set or reset the 'selected' indication on the visible node.
	 * @param node
	 * @param selected
	 * @throws Exception
	 */
	protected void markAsSelected(T node, boolean selected) throws Exception {
		if(null != node)
			expandNode(node);

		VisibleNode<T> vn = m_openMap.get(node);
		if(vn == null)
			return;
		if(vn.nodeRow == null)
			return;
		TD cell = (TD) vn.nodeRow.getChild(1);
		if(selected)
			cell.addCssClass("ui-tr-selected");
		else
			cell.removeCssClass("ui-tr-selected");
		cell.removeAllChildren();
		renderContent(cell, node);
	}

	@Nullable
	public TR locateRowIfExpanded(T node){
		VisibleNode<T> vn = m_openMap.get(node);
		if (null != vn){
			return vn.nodeRow;
		}
		return null;
	}

	protected boolean isSelected(@Nullable T node) {
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
	 * @return
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

	public IRenderInto< ? > getContentRenderer() {
		return m_contentRenderer;
	}

	public void setContentRenderer(IRenderInto< ? > contentRenderer) {
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
