package to.etc.domui.annotations;

import java.lang.annotation.*;

import to.etc.domui.util.*;

/**
 * Valid for a page property, this defines an URL parameter that is to be filled
 * in when the page is accessed. When the page is constructed the framework will
 * scan for properties having this annotation. It will then inject the value obtained
 * from the URL parameter specified in this property into the page.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 19, 2008
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UIUrlParameter {
	/**
	 * The name of the URL parameter to look for. If this is not present we use the property's name
	 * as the name of the URL parameter.
	 * @return
	 */
	String name() default Constants.NONE;

	/**
	 * By default all parameters defined are mandatory. Set this to false to make the URL parameter an optional
	 * value. When a value is optional it's setter is NOT called when the URL parameter is not present.
	 * @return
	 */
	boolean mandatory() default true;

	/**
	 * When set this defines that the given parameter is the primary key for this entity.
	 * @return
	 */
	Class< ? > entity() default Object.class;
}
