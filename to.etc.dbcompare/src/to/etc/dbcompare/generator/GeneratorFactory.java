package to.etc.dbcompare.generator;

import java.sql.*;

public interface GeneratorFactory {
	public AbstractGenerator createGenerator(Connection dbc) throws Exception;

	public AbstractGenerator createGenerator(String id) throws Exception;
}
