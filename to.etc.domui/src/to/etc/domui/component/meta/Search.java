package to.etc.domui.component.meta;

import java.lang.annotation.*;

/**
 * Defines the default search criteria for an entity.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 14, 2008
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Search {
	public SearchProperty[]		properties() default {};
}
