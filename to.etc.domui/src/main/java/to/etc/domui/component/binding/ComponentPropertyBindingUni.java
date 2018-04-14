package to.etc.domui.component.binding;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.util.IValueAccessor;
import to.etc.function.FunctionEx;

/**
 * An unidirectional binding, which only allow moving from model to control. This
 * is the default binding for all bindings to control properties.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 1-4-18.
 */
@NonNullByDefault
public class ComponentPropertyBindingUni<C extends NodeBase, CV, M, MV> extends AbstractComponentPropertyBinding<C, CV, M, MV> {
	@Nullable
	private FunctionEx<MV, CV> m_converter;

	public ComponentPropertyBindingUni(C control, PropertyMetaModel<CV> controlProperty, M modelInstance, IValueAccessor<MV> accessor, @Nullable FunctionEx<MV, CV> converter) {
		super(control, controlProperty, modelInstance, accessor);
		m_converter = converter;
	}

	/**
	 * We never have data from the control.
	 */
	@Nullable @Override public BindingValuePair<MV> getBindingDifference() throws Exception {
		return null;
	}

	@Nullable
	@Override protected CV convertModelToControl(@Nullable MV modelValue) throws Exception {
		FunctionEx<MV, CV> converter = m_converter;
		if(null != converter) {
			return converter.apply(modelValue);
		} else {
			return (CV) modelValue;
		}
	}
}
