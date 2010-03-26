package to.etc.webapp.qsql;

import java.lang.annotation.*;

/**
 * Marks a type as a compound (embedded) type.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 25, 2010
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface QJdbcCompound {
}
