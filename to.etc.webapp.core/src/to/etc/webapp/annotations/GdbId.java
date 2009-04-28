package to.etc.webapp.annotations;

import java.lang.annotation.*;

/**
 * Defines the ID property of a class.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 28, 2009
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value=ElementType.METHOD)
public @interface GdbId {
	/** The generator type to use. */
	String		generator() default "";

	/** The parameter for the generator; usually a sequence name */
	String		param();
}
