package to.etc.webapp.qsql;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.webapp.query.QDataContext;
import to.etc.webapp.query.QSelection;
import to.etc.webapp.query.QSelectionColumn;

import java.sql.ResultSet;

/**
 * Extracts count selection result from result set as first integer type column value.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 26 Apr 2013
 */
public class SelectorColumnsResultMaker implements IInstanceMaker {

	private final @NonNull
	QSelection< ? > m_selection;

	SelectorColumnsResultMaker(@NonNull QSelection< ? > selection) {
		m_selection = selection;
	}

	@Override
	public @NonNull
	Object make(@NonNull QDataContext dc, @NonNull ResultSet rs) throws Exception {
		//has to be returned as array of size 1
		Object[] res = new Object[m_selection.getColumnList().size()];
		int index = 0;
		for(QSelectionColumn selCol : m_selection.getColumnList()) {
			res[index] = rs.getObject(index + 1);
			index++;
		}
		return res;
	}

}
