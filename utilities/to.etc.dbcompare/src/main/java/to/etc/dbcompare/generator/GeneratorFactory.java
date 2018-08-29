package to.etc.dbcompare.generator;

import java.sql.*;

public interface GeneratorFactory {
	AbstractGenerator createGenerator(Connection dbc) throws Exception;

	AbstractGenerator createGenerator(String id) throws Exception;
}
