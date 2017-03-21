package to.etc.webapp.qsql;

import java.sql.*;

import javax.annotation.*;

import to.etc.webapp.query.*;

/**
 * Extracts count selection result from result set as first integer type column value.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 26 Apr 2013
 */
public class SelectorColumnsResultMaker implements IInstanceMaker {

	private final @Nonnull
	QSelection< ? > m_selection;

	SelectorColumnsResultMaker(@Nonnull QSelection< ? > selection) {
		m_selection = selection;
	}

	@Override
	public @Nonnull
	Object make(@Nonnull QDataContext dc, @Nonnull ResultSet rs) throws Exception {
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
