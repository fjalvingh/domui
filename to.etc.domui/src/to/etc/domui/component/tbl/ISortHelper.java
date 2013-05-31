package to.etc.domui.component.tbl;

import javax.annotation.*;

/**
 * Used to implement programmable sorting on ITableModels that are sortable.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 23, 2011
 */
public interface ISortHelper {
	/**
	 * Configure the model passed in such a way that it sorts in some specific way. The implementation <i>must</i>
	 * know the specific model type used.
	 * @param <T>
	 * @param model
	 * @param descending
	 */
	<T extends ITableModel< ? > & IProgrammableSortableModel> void adjustSort(@Nonnull T model, boolean descending);
}
