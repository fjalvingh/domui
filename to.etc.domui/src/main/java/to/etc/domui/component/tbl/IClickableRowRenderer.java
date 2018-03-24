package to.etc.domui.component.tbl;

import javax.annotation.*;

/**
 * This is a {@link IRowRenderer} that has methods to handle clicks on the row.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 11, 2012
 */
public interface IClickableRowRenderer<T> extends IRowRenderer<T> {
	/**
	 * Set (or clear) the handler to be called when the row is clicked.
	 * @param rowClicked
	 */
	void setRowClicked(@Nonnull ICellClicked<T> rowClicked);

	/**
	 * Set (or clear) the handler to be called when a specific cell in the row is clicked.
	 * @param col
	 * @param cellClicked
	 */
	void setCellClicked(int col, @Nonnull ICellClicked<T> cellClicked);
}
