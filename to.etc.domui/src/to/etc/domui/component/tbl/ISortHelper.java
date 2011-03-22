package to.etc.domui.component.tbl;

import to.etc.webapp.query.*;

public interface ISortHelper {
	void adjustSort(String propertyName, QCriteria< ? > criteria, boolean descending);
}
