package to.etc.domui.component.meta;

import to.etc.domui.converter.*;

/**
 * Annotation to define a parameterized value validator.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 24, 2008
 */
public @interface MetaValueValidator {
	public Class<? extends IValueValidator<?>>	validator();
	public String[]	parameters() default {};
}
