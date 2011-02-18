package to.etc.domui.component.tbl;

/**
 * "Select all" for an {@link ISelectionModel} can be problematic, because all can be
 * quite a lot. It can also mean different things like "all 1000 visible" or "really
 * all, even though you truncated the result, you clod".
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 17, 2011
 */
public interface ISelectionAllHandler {
	<T> void selectAll(ITableModel<T> sourcemodel, ISelectionModel<T> selectionModel) throws Exception;
}
