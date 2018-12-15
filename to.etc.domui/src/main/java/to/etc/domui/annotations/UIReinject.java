package to.etc.domui.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * EXPERIMENTAL When set on a property or a (private) field in a SubPage this
 * causes that field to be re-injected with a new value that is reloaded from
 * the data context associated with the subpage.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 1-12-18.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIReinject {
	boolean value() default true;
}
