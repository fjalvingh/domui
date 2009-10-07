package to.etc.domui.component.meta;

import java.lang.annotation.*;

/**
 * Marks a property as a search property.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 14, 2008
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SearchProperty {
	/**
	 * When set this defines an "order" when multiple search fields are present; the fields
	 * with the lowest order value are sorted before the fields with higher values in the
	 * lookup form.
	 * @return
	 */
	public int order() default -1;

	/**
	 * This defines the minimal length a user must enter into a search control before it
	 * is allowed to search. This can be used to prevent searches on 'a%' if that would
	 * cause a problematic query.
	 * @return
	 */
	public int minLength() default -1;

	/**
	 * Generate a CI query by default. Unused?
	 * @return
	 */
	public boolean ignoreCase() default true;
}
