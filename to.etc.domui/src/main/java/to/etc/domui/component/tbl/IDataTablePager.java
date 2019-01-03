package to.etc.domui.component.tbl;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.buttons.SmallImgButton;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-1-19.
 */
public interface IDataTablePager extends IDataTableChangeListener {
	boolean isShowSelection();

	void setShowSelection(boolean selection);

	void addButton(@NonNull SmallImgButton sib);
}
