package to.etc.domui.logic;

import javax.annotation.*;
import javax.annotation.concurrent.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.errors.*;

/**
 * EXPERIMENTAL - do not use.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 1, 2014
 */
@Immutable
final public class LogicError {
	@Nonnull
	final private UIMessage m_message;

	@Nonnull
	final private Object m_modelObject;

	@Nullable
	final private PropertyMetaModel< ? > m_property;

	public LogicError(@Nonnull UIMessage message, @Nonnull Object modelObject, @Nullable PropertyMetaModel< ? > property) {
		m_message = message;
		m_modelObject = modelObject;
		m_property = property;
	}

	@Nonnull
	public UIMessage getMessage() {
		return m_message;
	}

	@Nonnull
	public Object getModelObject() {
		return m_modelObject;
	}

	@Nullable
	public PropertyMetaModel< ? > getProperty() {
		return m_property;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_message == null) ? 0 : m_message.hashCode());
		result = prime * result + ((m_modelObject == null) ? 0 : m_modelObject.hashCode());
		PropertyMetaModel< ? > property = m_property;
		result = prime * result + ((property == null) ? 0 : property.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		LogicError other = (LogicError) obj;
		if(m_message == null) {
			if(other.m_message != null)
				return false;
		} else if(!m_message.equals(other.m_message))
			return false;
		if(m_modelObject == null) {
			if(other.m_modelObject != null)
				return false;
		} else if(!m_modelObject.equals(other.m_modelObject))
			return false;
		PropertyMetaModel< ? > property = m_property;
		if(property == null) {
			if(other.m_property != null)
				return false;
		} else if(!property.equals(other.m_property))
			return false;
		return true;
	}


}
