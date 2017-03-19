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

	@Nullable
	private final PV m_propertyValue;

	public BindingValuePair(@Nonnull IBinding binding, @Nullable CV controlValue, @Nullable PV propertyValue) {
		m_binding = binding;
		m_controlValue = controlValue;
		m_propertyValue = propertyValue;
	}

	/**
	 * Move the "control" value to the "property".
	 */
	public void moveControlToModel() {
		m_binding.setModelValue(m_controlValue);
	}
}
