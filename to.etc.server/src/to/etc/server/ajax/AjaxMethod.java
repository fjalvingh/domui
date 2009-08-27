package to.etc.server.ajax;

import java.lang.annotation.*;

/**
 * Annotates a method as an Ajax-callable method
 * 
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 5, 2006
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AjaxMethod {
	/**
	 * Defines a list of rolenames that the user must have 
	 * to be allowed access to this method. If the list is
	 * empty no security check takes place. The presence of
	 * roles also forces the user to login.
	 * @return
	 */
	String roles() default "";

	/**
	 * Defines the generated output format.
	 * @return
	 */
	ResponseFormat response() default ResponseFormat.UNDEFINED;
}
