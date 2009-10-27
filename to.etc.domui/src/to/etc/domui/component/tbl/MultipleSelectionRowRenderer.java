package to.etc.domui.component.tbl;

import java.util.*;

import to.etc.domui.component.meta.impl.*;

/**
 * Row renderer that is used for MultipleSelectionLookup control.  
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 27 Oct 2009
 */
public class MultipleSelectionRowRenderer extends SimpleRowRenderer {

	public MultipleSelectionRowRenderer(Class< ? > dataClass, String[] cols) {
		super(dataClass, cols);
	}

	public MultipleSelectionRowRenderer(Class< ? > dataClass) {
		super(dataClass);
	}

	/**
	 * Initialize, using the genericized table column set. Reserve some extra space for selection indicator column that is added as first column. 
	 * @param clz
	 * @param xdpl
	 */
	@Override
	protected void initialize(final List<ExpandedDisplayProperty> xdpl) {
		//-- For all properties in the list, use metadata to define'm
		final int[] widths = new int[80];
		m_totwidth = 55;
		int ix = 0;
		addColumns(xdpl, widths);
		ix = 0;
		for(final SimpleColumnDef scd : m_columnList) {
			final int pct = (100 * widths[ix++]) / m_totwidth;
			scd.setWidth(pct + "%");
		}
	}

}
