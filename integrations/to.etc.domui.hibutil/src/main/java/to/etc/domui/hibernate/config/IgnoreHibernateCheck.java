package to.etc.domui.hibernate.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to flag if certain method should not be checked by HibernateChecker. Please describe the reasoning by setting the value.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreHibernateCheck {
	String value();
}
