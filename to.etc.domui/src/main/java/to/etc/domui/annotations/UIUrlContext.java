package to.etc.domui.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 15-12-18.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UIUrlContext {
	/**
	 * When not optional, any property marked with this for which no
	 * value is found will throw an exception.
	 */
	boolean optional() default false;
}
