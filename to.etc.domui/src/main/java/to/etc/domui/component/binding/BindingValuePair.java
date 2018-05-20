package to.etc.domui.component.binding;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 12-3-17.
 */
final public class BindingValuePair<MV> {
	@NonNull
	private final IBinding m_binding;

	@Nullable
	private final MV m_controlModelValue;

	public BindingValuePair(@NonNull IBinding binding, @Nullable MV controlModelValue) {
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
