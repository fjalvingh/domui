package to.etc.webapp.qsql;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface QJdbcColumn {
	String name();

	int length() default -1;

	int scale() default -1;

	boolean nullable() default false;

	boolean istransient() default false;

	Class< ? extends IJdbcType> columnConverter() default IJdbcType.class;
}
