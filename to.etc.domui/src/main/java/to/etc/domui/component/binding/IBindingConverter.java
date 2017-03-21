package to.etc.domui.component.binding;

import javax.annotation.Nullable;

/**
 * Convert some binding value from one type to another type at binding time.
 * <b>Important</b>: the methods herein must ensure that the following rules
 * hold:
 * <ul>
 *     <li>modelToControl(controlToModel(x)) == x</li>
 *     <li>controlToModel(modelToControl(x)) == x</li>
 * </ul>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 19-3-17.
 */
public interface IBindingConverter<C, V> {
	/**
	 * Convert the value V, obtained from the model, to a value type that is expected by the control.
	 * @param value
	 * @return
	 * @throws Exception
	 */
	@Nullable
	C modelToControl(@Nullable V value) throws Exception;

	/**
	 * Control the value C, obtained from the control, into some value type expected by the model.
	 * @param value
	 * @return
	 * @throws Exception
	 */
	@Nullable
	V controlToModel(@Nullable C value) throws Exception;
}
