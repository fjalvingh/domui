package to.etc.webapp.annotations;

import java.lang.annotation.*;

/**
 * This compile-time annotation specifies that a given String parameter actually
 * represents a property of the class specified by the nearest type parameter. It
 * is used by the Eclipse plugin; it marks string parameters that actually represent
 * some property.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 3, 2010
 */
@Target(value = ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface GProperty {
	int parameter() default -1;
}
