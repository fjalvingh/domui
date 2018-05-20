package to.etc.webapp.qsql;

import org.eclipse.jdt.annotation.NonNull;

import java.sql.PreparedStatement;

public interface IQValueSetter {
	void assign(@NonNull PreparedStatement ps) throws Exception;
}
