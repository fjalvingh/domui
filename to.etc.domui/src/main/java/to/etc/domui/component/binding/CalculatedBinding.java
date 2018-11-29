package to.etc.domui.component.binding;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.YesNoType;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.NodeBase;
import to.etc.function.SupplierEx;

/**
 * This is a single-sided binding which moves data TO a control only, from
 * some calculation.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 7-11-18.
 */
@NonNullByDefault
public class CalculatedBinding<CV> implements IBinding {
	private final NodeBase m_control;

	private final PropertyMetaModel<CV> m_controlProperty;

	private final SupplierEx<Object> m_acceptor;

	@Nullable
	private CV m_lastValueFromControlAsModelValue;

	public CalculatedBinding(NodeBase control, PropertyMetaModel<CV> controlProperty, SupplierEx<Object> acceptor) {
		m_control = control;
		m_controlProperty = controlProperty;
		m_acceptor = acceptor;
	}

	@Nullable @Override public BindingValuePair<?> getBindingDifference() throws Exception {
		return null;
	}

	@Override public void moveModelToControl() throws Exception {
		CV modelValue = (CV) m_acceptor.get();

		if(!MetaManager.areObjectsEqual(modelValue, m_lastValueFromControlAsModelValue)) {
			//-- Value in instance differs from control's
			m_lastValueFromControlAsModelValue = modelValue;
			if(m_controlProperty.getReadOnly() != YesNoType.YES) {
				m_controlProperty.setValue(m_control, modelValue);
//					System.out.println(this + ": m2c " + controlValue);
			}
		}
	}

	@Nullable @Override public UIMessage getBindError() {
		return null;
	}

	@Override public <T> void setModelValue(@Nullable T value) {
		//-- Data is never moved back to the model.
	}
}
