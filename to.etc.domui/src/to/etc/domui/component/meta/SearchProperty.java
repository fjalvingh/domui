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
	public int			order() default -1;
	public String		name() default "$*$";
	public int			minLength() default -1;
	public boolean		ignoreCase() default true;
}
