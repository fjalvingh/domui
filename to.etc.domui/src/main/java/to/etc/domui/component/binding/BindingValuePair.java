package to.etc.domui.component.binding;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 12-3-17.
 */
final public class BindingValuePair<CV, PV> {
	@Nonnull
	private final IBinding m_binding;

	@Nullable
	private final CV m_controlValue;

	public BindingValuePair(@Nonnull IBinding binding, @Nullable CV controlValue) {
		m_binding = binding;
		m_controlValue = controlValue;
	}

	/**
	 * Move the "control" value to the "property".
	 */
	public void moveControlToModel() {
		m_binding.setModelValue(m_controlValue);
	}
}
