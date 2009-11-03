package to.etc.domui.component.tbl;

import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;

/**
 * Renders rows from a datamodel; this tries to use the metadata for all
 * parts not explicitly specified.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 18, 2008
 */
public class SimpleRowRenderer extends AbstractRowRenderer implements IRowRenderer {
	private int m_totwidth;

	protected void setTotalWidth(int w) {
		m_totwidth = w;
	}

	protected int getTotalWidth() {
		return m_totwidth;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Simple renderer initialization && parameterisation	*/
	/*--------------------------------------------------------------*/
	/**
	 * Create a renderer by handling the specified class and a list of properties off it.
	 * @param dataClass
	 * @param cols
	 */
	public SimpleRowRenderer(final Class< ? > dataClass, final String... cols) {
		super(dataClass);
		final ClassMetaModel cmm = MetaManager.findClassMeta(dataClass);
		if(cols.length != 0)
			initializeExplicitColumns(cmm, cols);
		else
			initializeDefaultColumns(cmm);

		//-- Is there a default sort thingy? Is that column present?
		final String sort = cmm.getDefaultSortProperty();
		if(sort != null) {
			for(final SimpleColumnDef scd : m_columnList) {
				if(scd.getPropertyName().equals(sort)) {
					setSortColumn(scd);
					break;
				}
			}
		}
	}

	/**
	 * This initializes the ColumnList by auto-decoding all required data from the class and the
	 * list of columns specified. It uses metamodel info if present.
	 *
	 * @param clz
	 * @param cols
	 */
	protected void initializeExplicitColumns(final ClassMetaModel cmm, final String[] cols) {
		if(cols == null || cols.length == 0)
			throw new IllegalStateException("The list-of-columns is empty or null; I need at least one column to continue.");

		final List<ExpandedDisplayProperty> xdpl = ExpandedDisplayProperty.expandProperties(cmm, cols);
		initialize(xdpl);
	}

	/**
	 * Initialize, using the genericized table column set.
	 * @param clz
	 * @param xdpl
	 */
	protected void initialize(final List<ExpandedDisplayProperty> xdpl) {
		//-- For all properties in the list, use metadata to define'm
		final int[] widths = new int[80];
		m_totwidth = 0;
		int ix = 0;
		addColumns(xdpl, widths);
		ix = 0;
		for(final SimpleColumnDef scd : m_columnList) {
			final int pct = (100 * widths[ix++]) / m_totwidth;
			scd.setWidth(pct + "%");
		}
	}

	protected void addColumns(final List<ExpandedDisplayProperty> xdpl, final int[] widths) {
		for(final ExpandedDisplayProperty xdp : xdpl) {
			if(xdp instanceof ExpandedDisplayPropertyList) {
				//-- Flatten: call for subs recursively.
				final ExpandedDisplayPropertyList xdl = (ExpandedDisplayPropertyList) xdp;
				addColumns(xdl.getChildren(), widths);
				continue;
			}

			//-- Create a column def from the metadata
			final SimpleColumnDef scd = new SimpleColumnDef(xdp);
			int dl = xdp.getDisplayLength();
			if(dl <= 0)
				dl = 10;
			//			System.out.println("XDPL: property " + xdp.getName() + " size=" + dl);
			widths[m_columnList.size()] = dl;
			m_totwidth += dl;
			m_columnList.add(scd); // ORDER!

			if(scd.getNumericPresentation() != null && scd.getNumericPresentation() != NumericPresentation.UNKNOWN) {
				scd.setCssClass("ui-numeric");
				scd.setHeaderCssClass("ui-numeric");
			}
		}
	}

	/**
	 * Called for an empty column list, this uses table metadata to create a column list. If
	 * the metadata does not contain stuff this aborts.
	 * @param clz
	 */
	private void initializeDefaultColumns(final ClassMetaModel cmm) {
		final List<DisplayPropertyMetaModel> dpl = cmm.getTableDisplayProperties();
		if(dpl.size() == 0)
			throw new IllegalStateException("The list-of-columns to show is empty, and the class has no metadata (@MetaObject) defining a set of columns as default table columns, so there.");
		final List<ExpandedDisplayProperty> xdpl = ExpandedDisplayProperty.expandDisplayProperties(dpl, cmm, null);
		initialize(xdpl);
	}

	//	public boolean isSortableModel() {
	//		return m_sortableModel;
	//	}

}
