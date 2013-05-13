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
public class CountSelectorResultMaker implements IInstanceMaker {

	@Override
	public Object make(@Nonnull QDataContext dc, @Nonnull ResultSet rs) throws Exception {
		//has to be returned as array of size 1
		Object[] res = new Object[1];
		res[0] = Integer.valueOf(rs.getInt(1));
		return res;
	}

}
