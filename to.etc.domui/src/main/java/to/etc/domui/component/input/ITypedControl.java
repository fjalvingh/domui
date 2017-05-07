package to.etc.domui.component.input;

import javax.annotation.Nonnull;

/**
 * When present on controls, this should help with determining the actual type
 * of a control when the Java Generics jokefest erased the type.
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 20-3-17.
 */
public interface ITypedControl<T> {
	@Nonnull
	Class<T> getActualType();
}
