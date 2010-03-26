package to.etc.webapp.qsql;

public interface IJdbcTypeFactory {
	int accept(JdbcPropertyMeta pm);

	IJdbcType createType(JdbcPropertyMeta pm) throws Exception;
}
