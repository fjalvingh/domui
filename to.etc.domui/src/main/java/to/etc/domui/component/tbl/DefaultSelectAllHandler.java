package to.etc.domui.component.tbl;

/**
 * A default "select all" handler which delegates to either the model (preferred) or
 * the selection model itself (slow).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 17, 2011
 */
public class DefaultSelectAllHandler implements ISelectionAllHandler {
	@Override
	public <T> void selectAll(ITableModel<T> sourceModel, ISelectionModel<T> selectionModel) throws Exception {
		if(sourceModel instanceof ISelectionAllHandler) {
			((ISelectionAllHandler) sourceModel).selectAll(sourceModel, selectionModel);
			return;
		}

		//-- Inefficient: delegate to selection model itself.
		selectionModel.selectAll(sourceModel);
	}
}
