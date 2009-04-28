package to.etc.webapp.annotations;

import java.lang.annotation.*;

/**
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 28, 2009
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value=ElementType.METHOD)
public @interface GdbColumn {
	/** Sets the base name of this column. If a table prefix is defined it is used in addition to form the full name. */
	String			name() default "";

	/** Sets the full name of this column. If this is set any table prefix is ignored. */
	String			fullName() default "";

	String			type() default "";

	/** The length of a column. For strings this is the max size <i>in characters</i>, for numerics this is the precision. */
	int				length();

	/** For numerics, the scale field. */
	int				scale() default -1;

	/** Whether the field can be nullable. Defaults to false. */
	boolean			nullable() default false;
}
