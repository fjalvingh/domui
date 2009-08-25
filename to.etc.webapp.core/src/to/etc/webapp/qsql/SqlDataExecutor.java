package to.etc.webapp.qsql;

import java.util.*;

import to.etc.webapp.query.*;

/**
 * This thingy is a poor man's persistence layer (read-only) to do queries on
 * annotated java POJO's that are linked to tables or views. For this code to
 * work it requires a metamodel for the specified classes; in addition the
 * classes must have definitions around their table mapping.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 25, 2009
 */
public class SqlDataExecutor implements IQueryExecutor {
	public <T> List<T> executeQuery(QDataContext dc, QCriteria<T> res) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Object[]> query(QSelection< ? > sel) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
