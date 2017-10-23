package to.etc.domui.component.tbl;

import to.etc.domui.dom.html.TR;

import javax.annotation.DefaultNonNull;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 7/29/16.
 */
@DefaultNonNull
final public class DataTableRow<T> extends TR {
	private final TableRowSet<T> m_rowSet;

	public DataTableRow(TableRowSet<T> rowSet) {
		m_rowSet = rowSet;
	}

	public DataTableRow<T> addRowAfter() {
		return m_rowSet.addRowAfter(this);
	}

	public DataTableRow<T> addRowBefore() {
		return m_rowSet.addRowBefore(this);
	}

	public boolean isVisible() {
		return m_rowSet.isVisible();
	}

	public void markEven(boolean even) {
		if(even) {
			addCssClass("ui-even");
			removeCssClass("ui-odd");
		} else {
			addCssClass("ui-odd");
			removeCssClass("ui-even");
		}
	}
}
