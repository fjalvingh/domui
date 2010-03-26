package to.etc.webapp.qsql;

public interface IJdbcTypeFactory {
	int accept(JdbcPropertyMeta pm);

	ITypeConverter createType(JdbcPropertyMeta pm) throws Exception;
}
