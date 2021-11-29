package to.etc.domui.component.binding;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.YesNoType;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.IControl;
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

	private final boolean m_updateAlways;

	@Nullable
	private CV m_lastValueFromControlAsModelValue;

	public CalculatedBinding(NodeBase control, PropertyMetaModel<CV> controlProperty, SupplierEx<Object> acceptor) {
		m_control = control;
		m_controlProperty = controlProperty;
		m_acceptor = acceptor;
		m_updateAlways = false;
	}

	public CalculatedBinding(NodeBase control, PropertyMetaModel<CV> controlProperty, SupplierEx<Object> acceptor, boolean updateAlways) {
		m_control = control;
		m_controlProperty = controlProperty;
		m_acceptor = acceptor;
		m_updateAlways = updateAlways;
	}

	@Nullable
	@Override
	public BindingValuePair<?> getBindingDifference() throws Exception {
		return null;
	}

	@Override
	public void moveModelToControl() throws Exception {
		CV modelValue = (CV) m_acceptor.get();

		if(m_updateAlways || !MetaManager.areObjectsEqual(modelValue, m_lastValueFromControlAsModelValue)) {
			//-- Value in instance differs from control's
			m_lastValueFromControlAsModelValue = modelValue;
			if(m_controlProperty.getReadOnly() != YesNoType.YES) {
				m_controlProperty.setValue(m_control, modelValue);
				if(m_updateAlways) {
					m_control.forceRebuild();
				}
			}
		}
	}

	@Nullable
	@Override
	public UIMessage getBindError() {
		return null;
	}

	@Override
	public <T> void setModelValue(@Nullable T value) {
		//-- Data is never moved back to the model.
	}

	public static <C extends NodeBase & IControl<X>, X, T> void bind(C control, SupplierEx<Object> acceptor) throws Exception {
		PropertyMetaModel<?> cpmm = MetaManager.findPropertyMeta(control.getClass(), "bindValue");
		if(null == cpmm) {
			cpmm = MetaManager.findPropertyMeta(control.getClass(), "value");
			if(null == cpmm)
				throw new IllegalStateException("No binding value property found");
		}
		CalculatedBinding<?> cb = new CalculatedBinding<>(control, cpmm, acceptor);
		control.addBinding(cb);
	}

	public static <C extends NodeBase & IControl<X>, X, T> void bindForced(C control, SupplierEx<Object> acceptor) throws Exception {
		PropertyMetaModel<?> cpmm = MetaManager.findPropertyMeta(control.getClass(), "bindValue");
		if(null == cpmm) {
			cpmm = MetaManager.findPropertyMeta(control.getClass(), "value");
			if(null == cpmm)
				throw new IllegalStateException("No binding value property found");
		}
		CalculatedBinding<?> cb = new CalculatedBinding<>(control, cpmm, acceptor, true);
		control.addBinding(cb);
	}
}
