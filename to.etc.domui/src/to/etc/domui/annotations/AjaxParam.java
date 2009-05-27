package to.etc.domui.annotations;

import java.lang.annotation.*;

/**
 * Annotation for function parameters to an Ajax-callable method. This
 * defines the name of the parameter within the request and any possible
 * defaults.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 6, 2006
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface AjaxParam {
	/**
	 * The default attribute defines the request parameter's name of this parameter in a GET URL.
	 * @return
	 */
	String value();

	/**
	 * An optional default parameter value.
	 * @return
	 */
	String dflt() default "";

	boolean trim() default true;

	boolean json() default false;
}
