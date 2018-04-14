package to.etc.domui.component.controlfactory;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.util.IReadOnlyModel;

/**
 * This is a base class to bind an input control (an IControl) to some property in a class. The control and
 * the class can have different value types; this base class has methods to convert those types when the
 * value(s) are moved. The basic implementation used most is {@link SimpleComponentPropertyBinding}.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 15, 2012
 */
@Deprecated
//@Immutable
abstract public class ComponentPropertyBinding<T, C> implements IModelBinding {
	@NonNull
	final IControl<C> m_control;

	@NonNull
	final private PropertyMetaModel<T> m_propertyMeta;

	@NonNull
	final private IReadOnlyModel< ? > m_model;

	/**
	 * Convert the data value of type T to the control value of type C.
	 * @param zheValue
	 * @return
	 * @throws Exception
	 */
	@Nullable
	abstract protected C convertValueToControl(@Nullable T zheValue) throws Exception;

	/**
	 * Convert the control's value of type C to a data value of type T.
	 * @param zheValue
	 * @return
	 * @throws Exception
	 */
	abstract protected T convertControlToValue(@Nullable C zheValue) throws Exception;

	public ComponentPropertyBinding(@NonNull IReadOnlyModel< ? > model, @NonNull PropertyMetaModel<T> propertyMeta, @NonNull IControl<C> control) {
		m_model = model;
		m_propertyMeta = propertyMeta;
		m_control = control;
	}

	@Override
	public void moveControlToModel() throws Exception {
		C val = m_control.getValue();
		T nval = convertControlToValue(val);
		Object base = m_model.getValue();
		m_propertyMeta.setValue(base, nval);
	}

	@Override
	public void moveModelToControl() throws Exception {
		Object base = m_model.getValue();
		T pval = m_propertyMeta.getValue(base);
		C cval = convertValueToControl(pval);
		m_control.setValue(cval);
	}

	@Override
	public void setControlsEnabled(boolean on) {
		m_control.setReadOnly(!on);
	}

	@NonNull
	public IControl<C> getControl() {
		return m_control;
	}

	@NonNull
	public PropertyMetaModel<T> getPropertyMeta() {
		return m_propertyMeta;
	}

	@NonNull
	public IReadOnlyModel< ? > getModel() {
		return m_model;
	}
}
