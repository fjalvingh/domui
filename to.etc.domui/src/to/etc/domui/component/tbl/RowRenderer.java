package to.etc.domui.component.tbl;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.webapp.annotations.*;

/**
 * This is the type-safe replacement for the other row renderers which are now deprecated.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 11, 2013
 */
final public class RowRenderer<T> extends AbstractRowRenderer<T> {
	public RowRenderer(@Nonnull Class<T> data, @Nonnull ClassMetaModel cmm) {
		super(data, cmm);
	}

	public RowRenderer(@Nonnull Class<T> data) {
		super(data);
	}

	@Override
	protected void complete(@Nonnull TableModelTableBase<T> tbl) {
		if(isComplete())
			return;

		//-- If we have no columns at all we use a default column list.
		if(getColumnList().size() == 0)
			addDefaultColumns();

		//-- If we have not yet a default sortable column but the model has it - use the model's one.
		if(getSortColumn() == null) {
			String dsp = model().getDefaultSortProperty();
			getColumnList().setDefaultSortColumn(dsp);
		}

		getColumnList().assignPercentages();				// Calculate widths
		super.complete(tbl);
	}

	/**
	 * Add all of the columns as defined by the metadata to the list.
	 */
	public void addDefaultColumns() {
		getColumnList().addDefaultColumns();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Typesafe definition delegates.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Add and return the column definition for a column on the specified property. Because Java still has no
	 * first-class properties (sigh) you need to pass in the property's type to get a typeful column. If you
	 * do not need a typeful column use {@link #column(String)}.
	 * @param type
	 * @param property
	 * @return
	 */
	@Nonnull
	public <T> SimpleColumnDef<T> column(@Nonnull Class<T> type, @Nonnull @GProperty String property) {
		return getColumnList().column(type, property);
	}

	/**
	 * This adds a column on the specified property, but has no idea about the real type. It can be used as long
	 * as that type is not needed.
	 * @param property
	 * @return
	 */
	@Nonnull
	public SimpleColumnDef< ? > column(@Nonnull String property) {
		return getColumnList().column(property);
	}

	/**
	 * Add a column which gets referred the row element instead of a column element. This is normally used together with
	 * @return
	 */
	@Nonnull
	public SimpleColumnDef<T> column() {
		return getColumnList().column();
	}


}
