package to.etc.domui.component.tbl;

import javax.annotation.*;

/**
 * Used to implement programmable sorting on ITableModels that are sortable.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 23, 2011
 */
public interface ISortHelper<T> {
	/**
	 * Configure the model passed in such a way that it sorts in some specific way. The implementation <i>must</i>
	 * know the specific model type used.
	 * @param model
	 * @param descending
	 * @param <M>
	 */
	<M extends ITableModel< T >> void adjustSort(@Nonnull M model, boolean descending) throws Exception;
}
