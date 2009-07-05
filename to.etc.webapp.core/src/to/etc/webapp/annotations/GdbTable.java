package to.etc.webapp.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
public @interface GdbTable {
	/** The full database name for this table. */
	String name();

	/** If column names should be prefixed by default, this is the table's prefix or 'shorthand' */
	String prefix() default "";
}
