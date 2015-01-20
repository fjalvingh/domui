package to.etc.domui.component.meta.impl;

import to.etc.domui.component.meta.*;

import javax.annotation.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 1/20/15.
 */
@DefaultNonNull
final public class PropertyValueInvalidException extends RuntimeException {
	final private Object m_targetObject;

	final private PropertyMetaModel<?> m_property;

	final private Object m_value;

	public PropertyValueInvalidException(@Nullable Object value, Object targetObject, PropertyMetaModel<?> property) {
		m_value = value;
		m_targetObject = targetObject;
		m_property = property;
	}

	@Override public String getMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append("Value of type ").append(m_value.getClass().getName()).append(" cannot be assigned to property ").append(m_property).append(" of type ").append(m_property.getActualType().getName());
		return sb.toString();
	}
}
