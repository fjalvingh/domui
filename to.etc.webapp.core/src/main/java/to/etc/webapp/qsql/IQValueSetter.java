package to.etc.webapp.qsql;

import java.sql.*;

import javax.annotation.*;

public interface IQValueSetter {
	void assign(@Nonnull PreparedStatement ps) throws Exception;
}
