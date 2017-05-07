package to.etc.dbutil.reverse;

import java.sql.*;

import javax.annotation.*;

public interface IExec {
	public void exec(@Nonnull Connection dbc) throws Exception;
}
