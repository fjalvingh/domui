package to.etc.webapp.annotations;

import java.lang.annotation.*;

/**
 * Indicates the collection property containing the children of a one-to-many collection.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 28, 2009
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface GdbChildren {
	GdbFetch fetch() default GdbFetch.DEFAULT;

	/** If the child class has a property that refers to it's parent name that property here. */
	String mappedBy() default "";

	//	/** If the child class has no parent property you can specify the join columns in it's table here. */
	//	String[]	joinColumns() default {};
}
