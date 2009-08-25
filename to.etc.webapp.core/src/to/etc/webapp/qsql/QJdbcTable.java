package to.etc.webapp.qsql;

import java.lang.annotation.*;

/**
 * Marks a POJO class as a JDBC accessed table or view.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 25, 2009
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface QJdbcTable {
	String table();
}
