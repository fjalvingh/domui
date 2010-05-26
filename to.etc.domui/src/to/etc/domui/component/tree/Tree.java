package to.etc.domui.component.tree;

import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;

public class Tree extends Div implements ITreeModelChangedListener<Object> {
	private ITreeModel<Object> m_model;

	private boolean m_showRoot;

	private boolean m_expandOnlyOne;

	private Table m_rootTable;

	private boolean m_expandRoot;

	private Map<Object, VisibleNode> m_openMap = new HashMap<Object, VisibleNode>();

	/** The specified ComboRenderer used. */
	private INodeContentRenderer< ? > m_contentRenderer;

	private INodeContentRenderer<Object> m_actualContentRenderer;

	private Class< ? extends INodeContentRenderer< ? >> m_contentRendererClass;

	private PropertyMetaModel m_propertyMetaModel;

	private ICellClicked< ? > m_cellClicked;

	/**
	 * Represents the internal visible state of the tree.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Oct 20, 2008
	 */
	static class VisibleNode {
		final Object data;

		/** The first row of the node's data. */
		TR nodeRow;

		/** If this is an expanded node this contains the expanded children's nodes. */
		VisibleNode[] childNodes;

		boolean expanded;

		boolean unexpandable;

		public VisibleNode(Object data) {
			this.data = data;
		}
	}

	public Tree() {
		setCssClass("ui-tree");
	}

	/**
	 * Main initial renderer.
	 * @see to.etc.domui.dom.html.NodeBase#createContent()
	 */
	@Override
	public void createContent() throws Exception {
		//-- The root node is always expanded, of course
		Object root = getModel().getRoot();

		//-- Render the root thingy && create the 1st visibleNode
		VisibleNode n = getVisibleNode(root);
		n.expanded = true;
		m_rootTable = renderList(root, n);
		if(m_expandRoot) {
			for(int i = getModel().getChildCount(root); --i >= 0;) {
				Object v = getModel().getChild(root, i);
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
	private VisibleNode getVisibleNode(Object base) {
		VisibleNode n = m_openMap.get(base);
		if(n == null) {
			n = new VisibleNode(base);
			m_openMap.put(base, n);
		}
		return n;
	}

	private Table renderList(Object base, VisibleNode baseInfo) throws Exception {
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
		VisibleNode[] vnar = new VisibleNode[len];
		for(int i = 0; i < len; i++) {
			final Object item = getModel().getChild(base, i); // Get ith child
			VisibleNode chvn = getVisibleNode(item);
			vnar[i] = chvn;
			boolean last = i + 1 == len; // T if this is the last child being rendered
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
						public void clicked(Img bxx) throws Exception {
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
						public void clicked(Img bxx) throws Exception {
							collapseNode(item);
						}
					});
				}
			}
		}
		baseInfo.childNodes = vnar;
		return t;
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
	public void expandNode(Object item) throws Exception {
		getModel().expandChildren(item);
		List<Object> path = getTreePath(item); // Calculate a path.
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

		//-- The damn thing is visible. We need to re-render where needed.
		for(final Object o : path) {
			VisibleNode vn = getVisibleNode(o);
			vn.expanded = true;

			if(vn.childNodes == null) {
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
						public void clicked(Img bxx) throws Exception {
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
					Table tc = renderList(item, vn); // Render item's expanded thingies
					td.add(tc);

					img.setClicked(new IClicked<Img>() {
						public void clicked(Img bxx) throws Exception {
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
	public void collapseNode(final Object item) throws Exception {
		VisibleNode vn = m_openMap.get(item);
		if(vn == null)
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
			public void clicked(Img bxx) throws Exception {
				expandNode(item);
			}
		});

		row.getParent().getChild(rowix + 1).remove(); // Drop the 2nd item
	}

	private void dropCrud(VisibleNode vnbase) throws Exception {
		if(vnbase.childNodes == null)
			return;
		int ix = 0;
		for(VisibleNode vn : vnbase.childNodes) {
			if(vn == null)
				throw new IllegalStateException("?? Element " + ix + " of parent=" + vnbase.data + " is null???");
			m_openMap.remove(vn.data);
			dropCrud(vn);
			if(vn.expanded)
				getModel().collapseChildren(vn.data);
			ix++;
		}
	}

	static private String branchurl() {
		return PageContext.getRequestContext().getRelativeThemePath("tree-branch.png");
	}

	/**
	 * Calculates a tree path for a given node, as a set of nodes that walk to the item. The
	 * root element is always the 1st element in the treepath
	 * @param item
	 * @return
	 */
	public List<Object> getTreePath(Object item) throws Exception {
		List<Object> path = new ArrayList<Object>();
		addParentPath(path, item);
		return path;
	}

	private void addParentPath(List<Object> path, Object item) throws Exception {
		Object parent = getModel().getParent(item);
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

	private void renderContent(final TD cell, final Object value) throws Exception {
		if(m_actualContentRenderer == null)
			m_actualContentRenderer = (INodeContentRenderer<Object>) calculateContentRenderer(value);
		m_actualContentRenderer.renderNodeContent(this, cell, value, this);

		if(getCellClicked() != null) { // Is a cell clicked thing attached?
			cell.addCssClass("ui-tr-sel");
			cell.setClicked(new IClicked<TD>() {
				public void clicked(TD b) throws Exception {
					((ICellClicked<Object>) getCellClicked()).cellClicked(getPage(), cell, value);
				}
			});
		}
	}

	/**
	 * Returns T if this node is currently expanded (opened).
	 * @param node
	 * @return
	 */
	public boolean isExpanded(Object node) {
		VisibleNode vn = m_openMap.get(node);
		if(vn == null)
			return false;
		return vn.expanded;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Model updates.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Set a new model for this table. This discards the entire presentation
	 * and causes a full build at render time.
	 */
	public void setModel(ITreeModel< ? > model) {
		ITreeModel<Object> itm = (ITreeModel<Object>) model; // Stupid Java Generics need cast here
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
		Object root = getModel().getRoot();
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
	public ITreeModel<Object> getModel() {
		return m_model;
	}

	public boolean isShowRoot() {
		return m_showRoot;
	}

	public void setShowRoot(boolean showRoot) {
		m_showRoot = showRoot;
	}

	public boolean isExpandOnlyOne() {
		return m_expandOnlyOne;
	}

	public void setExpandOnlyOne(boolean expandOnlyOne) {
		m_expandOnlyOne = expandOnlyOne;
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

	public void setContentRendererClass(Class< ? extends INodeContentRenderer< ? >> contentRendererClass) {
		m_contentRendererClass = contentRendererClass;
	}

	public ICellClicked< ? > getCellClicked() {
		return m_cellClicked;
	}

	public void setCellClicked(ICellClicked< ? > cellClicked) {
		m_cellClicked = cellClicked;
	}

	public PropertyMetaModel getPropertyMetaModel() {
		return m_propertyMetaModel;
	}

	public void setPropertyMetaModel(PropertyMetaModel propertyMetaModel) {
		m_propertyMetaModel = propertyMetaModel;
	}
}
