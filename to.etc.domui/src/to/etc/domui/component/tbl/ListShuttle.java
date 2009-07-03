package to.etc.domui.component.tbl;

import java.util.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;

/**
 * The ListShuttle component contains a SOURCE list and a TARGET list, and allows the user to
 * "shuttle" elements from the SOURCE list to the TARGET list, and v.v. In addition the target
 * list can be ordered, if necessary.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 5, 2008
 */
public class ListShuttle extends Div implements ITableModelListener<Object> {
	private TBody			m_body;

	private boolean			m_orderable;
	private TD				m_sourceCell;
	private TD				m_targetCell;
	private TD				m_orderCell;
	private TBody			m_sourceBody;
	private TBody			m_targetBody;
	private Div				m_sourceDiv, m_targetDiv;

	private IShuttleModel<Object,Object>	m_model;

	private ITableModel<Object>				m_sourceModel;
	private ITableModel<Object>				m_targetModel;
//	private ITableModelListener<Object>		m_targetChangeListener;

	private Class<? extends INodeContentRenderer<?>>	m_sourceRendererClass;
	private Class<? extends INodeContentRenderer<?>>	m_targetRendererClass;
	private INodeContentRenderer<Object>	m_sourceRenderer;
	private INodeContentRenderer<Object>	m_targetRenderer;
	private final int								m_innerHeight = 400;

	static private IClicked<TD>		C_CLICK = new IClicked<TD>() {
		public void clicked(final TD b) throws Exception {
			if(b.hasCssClass("selected"))
				b.removeCssClass("selected");
			else
				b.addCssClass("selected");
		}
	};

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-sh");
		Table	t	= new Table();						// Main 3 or 4 column layout.
		add(t);
		m_body= new TBody();
		t.add(m_body);
		m_body.addRow();								// Zhe one row
		m_sourceCell	= m_body.addCell();
		TD	midcell		= m_body.addCell();				// Cell for the buttons in zhe middle
		m_targetCell	= m_body.addCell();

		m_sourceCell.setCssClass("ui-sh-sc");
		m_targetCell.setCssClass("ui-sh-tc");

		m_sourceCell.add(m_sourceDiv = new Div());
		m_targetCell.add(m_targetDiv = new Div());
		m_sourceDiv.setCssClass("ui-sh-scd");
		m_targetDiv.setCssClass("ui-sh-tcd");
		m_sourceDiv.setHeight(m_innerHeight+"px");
		m_targetDiv.setHeight(m_innerHeight+"px");
		m_sourceDiv.setOverflow(Overflow.AUTO);
		m_targetDiv.setOverflow(Overflow.AUTO);
		m_sourceCell.setValign(TableVAlign.TOP);
		m_targetCell.setValign(TableVAlign.TOP);

		createMiddle(midcell);

		if(needOrderButtons()) {
			createOrderPane();
		}

		renderSource();
		renderTarget();
	}

	/**
	 * Create the last panel containing the UP and DOWN order buttons.
	 */
	private void	createOrderPane() {
		m_orderCell = m_body.addCell();				// Order buttons cell

		m_orderCell.setCssClass("ui-sh-oc");
		Table	t	= new Table();
		m_orderCell.add(t);
		TBody	b	= new TBody();
		t.add(b);

		SmallImgButton	ib	= new SmallImgButton("THEME/sh-up.png", new IClicked<SmallImgButton>() {
			public void clicked(final SmallImgButton xb) throws Exception {
				moveUp();
			}
		});
		b.addRowAndCell().add(ib);

		ib	= new SmallImgButton("THEME/sh-down.png", new IClicked<SmallImgButton>() {
			public void clicked(final SmallImgButton xb) throws Exception {
				moveDown();
			}
		});
		b.addRowAndCell().add(ib);
	}

	/**
	 * Create the middle pane containing the move buttons.
	 * @param mid
	 */
	private void	createMiddle(final TD mid) {
		mid.setCssClass("ui-sh-bc");
		Table	t	= new Table();
		mid.add(t);
		TBody	b	= new TBody();
		t.add(b);

		SmallImgButton	ib	= new SmallImgButton("THEME/sh-1ar-right.png", new IClicked<SmallImgButton>() {
			public void clicked(final SmallImgButton xb) throws Exception {
				moveRight();
			}
		});
		b.addRowAndCell().add(ib);

		ib	= new SmallImgButton("THEME/sh-1ar-left.png", new IClicked<SmallImgButton>() {
			public void clicked(final SmallImgButton xb) throws Exception {
				moveLeft();
			}
		});
		b.addRowAndCell().add(ib);

		ib	= new SmallImgButton("THEME/sh-2ar-right.png");
		b.addRowAndCell().add(ib);

		ib	= new SmallImgButton("THEME/sh-2ar-left.png");
		b.addRowAndCell().add(ib);
	}

	/**
	 * Render the contents of the source part of the shuttle.
	 */
	private void	renderSource() throws Exception {
		if(m_sourceBody == null) {
			Table t = new Table();
			m_sourceDiv.add(t);
			t.add(m_sourceBody = new TBody());
		} else
			m_sourceBody.removeAllChildren();
		if(m_sourceModel == null)
			return;
		int	count = m_sourceModel.getRows();
		if(count == 0)
			return;

		List<Object>	list = m_sourceModel.getItems(0, count);

		INodeContentRenderer<Object>	r = null;
		for(int i = 0; i < count; i++) {
			TD td = m_sourceBody.addRowAndCell();
			Object	value = list.get(i);
			if(r == null)
				r = calculateSourceRenderer(value);
			r.renderNodeContent(this, td, value, null);

			td.setClicked(C_CLICK);
		}
	}

	/**
	 * Render the contents of the target part of the shuttle.
	 */
	private void	renderTarget() throws Exception {
		if(m_targetBody == null) {
			Table t = new Table();
			m_targetDiv.add(t);
			t.add(m_targetBody = new TBody());
		} else
			m_targetBody.removeAllChildren();
		if(m_targetModel == null)
			return;
		int	count = m_targetModel.getRows();
		if(count == 0)
			return;

		List<Object>	list = m_targetModel.getItems(0, count);
		INodeContentRenderer<Object>	r = null;
		for(int i = 0; i < count; i++) {
			TD td = m_targetBody.addRowAndCell();
			Object	value = list.get(i);
			if(r == null)
				r = calculateTargetRenderer(value);
			r.renderNodeContent(this, td, value, null);
			td.setClicked(C_CLICK);
		}
	}

	private boolean	needOrderButtons() {
		return isOrderable() && getModel() instanceof IMovableShuttleModel<?,?>;
	}

	private INodeContentRenderer<Object> calculateSourceRenderer(final Object val) {
		if(m_sourceRenderer != null)
			return m_sourceRenderer;
		if(m_sourceRendererClass != null)
			return (INodeContentRenderer<Object>)DomApplication.get().createInstance(m_sourceRendererClass);

		if(val == null)
			throw new IllegalStateException("Cannot calculate content renderer for null value");
		ClassMetaModel	cmm = MetaManager.findClassMeta(val.getClass());
		return (INodeContentRenderer<Object>)MetaManager.createDefaultComboRenderer(null /* m_propertyMetaModel */, cmm);
	}

	private INodeContentRenderer<Object> calculateTargetRenderer(final Object val) {
		if(m_targetRenderer != null)
			return m_targetRenderer;
		if(m_targetRendererClass != null)
			return (INodeContentRenderer<Object>)DomApplication.get().createInstance(m_targetRendererClass);

		if(val == null)
			throw new IllegalStateException("Cannot calculate content renderer for null value");
		ClassMetaModel	cmm = MetaManager.findClassMeta(val.getClass());
		return (INodeContentRenderer<Object>)MetaManager.createDefaultComboRenderer(null /* m_propertyMetaModel */, cmm);
	}

	/**
	 * Move all selected thingies to the right. This scans all TD's having a class of SELECTED, then moves
	 * the items found to the TARGET model.
	 * @throws Exception
	 */
	void	moveRight() throws Exception {
		for(int ix = m_sourceBody.getChildCount(); --ix >= 0;) {
			NodeBase tb = m_sourceBody.getChild(ix);
			if(! (tb instanceof TR))
				throw new IllegalStateException("?? Unexpected node: "+tb);
			TR	tr	= (TR) tb;
			TD	td	= (TD) tr.getChild(0);				// The row containing the rendition for the value
			if(td.hasCssClass("selected")) {
				m_model.moveSourceToTarget(ix, 9999);	// Since this changes both models
			}
		}
	}

	/**
	 * Move all selected thingies to the right. This scans all TD's having a class of SELECTED, then moves
	 * the items found to the TARGET model.
	 * @throws Exception
	 */
	void	moveLeft() throws Exception {
		for(int ix = m_targetBody.getChildCount(); --ix >= 0;) {
			NodeBase tb = m_targetBody.getChild(ix);
			if(! (tb instanceof TR))
				throw new IllegalStateException("?? Unexpected node: "+tb);
			TR	tr	= (TR) tb;
			TD	td	= (TD) tr.getChild(0);				// The row containing the rendition for the value
			if(td.hasCssClass("selected")) {
				m_model.moveTargetToSource(ix);			// Since this changes both models
			}
		}
	}

	/**
	 * Move all selected items in the TARGET table UP.
	 * @throws Exception
	 */
	void moveUp() throws Exception {
		for(int ix = 0; ix < m_targetBody.getChildCount(); ix++) {
			NodeBase tb = m_targetBody.getChild(ix);
			if(! (tb instanceof TR))
				throw new IllegalStateException("?? Unexpected node: "+tb);
			TR	tr	= (TR) tb;
			TD	td	= (TD) tr.getChild(0);				// The row containing the rendition for the value
			if(td.hasCssClass("selected")) {
				//-- This must move up.. If we're at index 0 we cannot move up!
				if(ix == 0)
					return;
				((IMovableShuttleModel<Object, Object>)m_model).moveTargetItem(ix, ix-1);
				reselect(ix-1);
			}
		}
	}

	/**
	 * Moves all selected items in the TARGET downwards.
	 * @throws Exception
	 */
	void moveDown() throws Exception {
		for(int ix = m_targetBody.getChildCount(); --ix >= 0;) {
			NodeBase tb = m_targetBody.getChild(ix);
			if(! (tb instanceof TR))
				throw new IllegalStateException("?? Unexpected node: "+tb);
			TR	tr	= (TR) tb;
			TD	td	= (TD) tr.getChild(0);					// The row containing the rendition for the value
			if(td.hasCssClass("selected")) {
				if(ix >= m_targetBody.getChildCount()-1)	// At the end already?
					return;									// Cannot move down!
				((IMovableShuttleModel<Object, Object>)m_model).moveTargetItem(ix, ix+1);
				reselect(ix+1);
			}
		}
	}
	private void	reselect(final int ix) {
//		if(ix < 0 || ix >= m_targetBody.getChildCount())
//			return;
		NodeBase	tb	= m_targetBody.getChild(ix);
		if(! (tb instanceof TR))
			throw new IllegalStateException("?? Unexpected node: "+tb);
		TR	tr	= (TR) tb;
		TD	td	= (TD) tr.getChild(0);				// The row containing the rendition for the value
		td.setCssClass("selected");
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Model based code.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Get the model for this thingy.
	 * @return
	 */
	public IShuttleModel<?, ?> getModel() {
		return m_model;
	}

	/**
	 * Set the model to use for the shuttle. If the model changes or one of the component models has
	 * changed this will cause a redraw.
	 *
	 * @param model
	 */
	public void setModel(final IShuttleModel<?, ?> model) {
		if(m_model == model)
			return;
		m_model = (IShuttleModel<Object, Object>)model;

		//-- Replace source model.
		ITableModel<Object>	tm = m_model.getSourceModel();
		if(m_sourceModel != tm) {							// Source model has changed?
			if(m_sourceModel != null)
				m_sourceModel.removeChangeListener(this);	// Remove myself from that model's listener chain
			m_sourceModel = tm;
			if(m_sourceModel != null)
				m_sourceModel.addChangeListener(this);		// Add myself to the new model
			forceRebuild();
		}

		//-- Replace target model
		tm	= m_model.getTargetModel();
		if(m_targetModel != tm) {
			if(m_targetModel != null)
				m_targetModel.removeChangeListener(this);
			m_targetModel = tm;
			if(m_targetModel != null)
				m_targetModel.addChangeListener(this);
			forceRebuild();
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Source table ITableModelChanged implementation.		*/
	/*--------------------------------------------------------------*/
	/**
	 * The source model has changed. Rebuild the entire component.
	 * @see to.etc.domui.component.tbl.ITableModelListener#modelChanged(to.etc.domui.component.tbl.ITableModel)
	 */
	public void modelChanged(final ITableModel<Object> model) {
		forceRebuild();
	}

	/**
	 * A row was moved (back) to the source model: change it.
	 * @see to.etc.domui.component.tbl.ITableModelListener#rowAdded(to.etc.domui.component.tbl.ITableModel, int, java.lang.Object)
	 */
	public void rowAdded(final ITableModel<Object> model, final int index, final Object value) throws Exception {
		boolean issrc = model == m_sourceModel;
		TBody	b = issrc ? m_sourceBody : m_targetBody;

		//-- Create a new node to insert here,
		TR	tr	= new TR();
		TD	td	= new TD();
		tr.add(td);
		td.setClicked(C_CLICK);
		INodeContentRenderer<Object>	r	= issrc ? calculateSourceRenderer(value) : calculateTargetRenderer(value);
		r.renderNodeContent(this, td, value, null);
		b.add(index, tr);
	}

	public void rowDeleted(final ITableModel<Object> model, final int index, final Object value) throws Exception {
		boolean issrc = model == m_sourceModel;
		TBody	b = issrc ? m_sourceBody : m_targetBody;
		b.removeChild(index);							// Discard this one;
	}

	public void rowModified(final ITableModel<Object> model, final int index, final Object value) throws Exception {
		boolean issrc = model == m_sourceModel;
		TBody	b = issrc ? m_sourceBody : m_targetBody;

		//-- Locate the TD containing the changed thingy.
		TR	tr	= (TR) b.getChild(index);
		TD	td	= (TD)tr.getChild(0);
		td.removeAllChildren();							// Clear it's contents,
		INodeContentRenderer<Object>	r	= issrc ? calculateSourceRenderer(value) : calculateTargetRenderer(value);
		r.renderNodeContent(this, td, value, null);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Simple Setters and getters.							*/
	/*--------------------------------------------------------------*/

	public INodeContentRenderer<Object> getSourceRenderer() {
		return m_sourceRenderer;
	}

	public void setSourceRenderer(final INodeContentRenderer<Object> sourceRenderer) {
		m_sourceRenderer = sourceRenderer;
	}

	public INodeContentRenderer<Object> getTargetRenderer() {
		return m_targetRenderer;
	}

	public void setTargetRenderer(final INodeContentRenderer<Object> targetRenderer) {
		m_targetRenderer = targetRenderer;
	}
	/**
	 * Is this set to be orderable?
	 * @return
	 */
	public boolean isOrderable() {
		return m_orderable;
	}

	public Class< ? extends INodeContentRenderer< ? >> getSourceRendererClass() {
		return m_sourceRendererClass;
	}

	public void setSourceRendererClass(final Class< ? extends INodeContentRenderer< ? >> sourceRendererClass) {
		m_sourceRendererClass = sourceRendererClass;
	}

	public Class< ? extends INodeContentRenderer< ? >> getTargetRendererClass() {
		return m_targetRendererClass;
	}

	public void setTargetRendererClass(final Class< ? extends INodeContentRenderer< ? >> targetRendererClass) {
		m_targetRendererClass = targetRendererClass;
	}

	/**
	 * When set (and the model supports it) this shows ordering buttons at the right of the
	 * target shuttle, allowing the content to be moved up- and downwards.
	 *
	 * @param orderable
	 */
	public void setOrderable(final boolean orderable) {
		m_orderable = orderable;
	}

}
