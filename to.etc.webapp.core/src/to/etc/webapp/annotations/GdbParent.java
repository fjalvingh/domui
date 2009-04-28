package to.etc.webapp.annotations;

import java.lang.annotation.*;

/**
 * Referral to a PARENT (master) in a master-child relation. This is present in the child
 * object, and indicates the property which refers to the parent object for the child.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 28, 2009
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value=ElementType.METHOD)
public @interface GdbParent {
	GdbFetch	fetch() default GdbFetch.LAZY;
	String		joinColumns();
	String		constraintName();
	boolean		optional() default false;
}
