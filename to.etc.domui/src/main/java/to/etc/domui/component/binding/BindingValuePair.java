package to.etc.domui.component.binding;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 12-3-17.
 */
final public class BindingValuePair<MV> {
	@Nonnull
	private final IBinding m_binding;

	@Nullable
	private final MV m_controlModelValue;

	public BindingValuePair(@Nonnull IBinding binding, @Nullable MV controlModelValue) {
		m_binding = binding;
		m_controlModelValue = controlModelValue;
	}

	/**
	 * Move the "control" value to the "property".
	 */
	public void moveControlToModel() {
		m_binding.setModelValue(m_controlModelValue);
	}
}
