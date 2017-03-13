package to.etc.domui.component.binding;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 12-3-17.
 */
public class BindingPair<CV, PV> {
	private final IBinding m_binding;

	private final CV m_controlValue;

	private final PV m_propertyValue;

	public BindingPair(IBinding binding, CV controlValue, PV propertyValue) {
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
