package to.etc.domui.component.tbl;

import org.eclipse.jdt.annotation.NonNull;

public interface IPageableComponent {
	int getCurrentPage();

	void setCurrentPage(int page) throws Exception;

	int getPageCount() throws Exception;

	void addChangeListener(@NonNull IDataTableChangeListener listener);
}
