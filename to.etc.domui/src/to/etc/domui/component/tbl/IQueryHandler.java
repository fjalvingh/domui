package to.etc.domui.component.tbl;

import java.util.*;

import to.etc.webapp.query.*;

public interface IQueryHandler<T> {
	public List<T> query(QCriteria<T> q) throws Exception;
}
