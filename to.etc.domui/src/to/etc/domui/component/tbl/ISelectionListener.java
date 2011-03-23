package to.etc.domui.component.tbl;

import javax.annotation.*;

/**
 * A listener for selection events on a {@link ISelectionModel}. Register instances on it with {@link ISelectionModel#addListener(ISelectionListener)}.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 17, 2011
 */
public interface ISelectionListener<T> {
	/**
	 * Notification that the selection of the item changed.
	 *
	 * is sent via this call.
	 * @param row
	 * @param on
	 * @throws Exception
	 */
	void selectionChanged(@Nonnull T row, boolean on) throws Exception;

	/**
	 * Notification that the entire selection on a ISelectionModel is cleared or set: rewrite all shown rows.
	 * @throws Exception
	 */
	void selectionAllChanged() throws Exception;
}
