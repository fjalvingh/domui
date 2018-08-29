package to.etc.dbutil.reverse;

import org.eclipse.jdt.annotation.NonNull;

import java.sql.Connection;

public interface IExec {
	void exec(@NonNull Connection dbc) throws Exception;
}
