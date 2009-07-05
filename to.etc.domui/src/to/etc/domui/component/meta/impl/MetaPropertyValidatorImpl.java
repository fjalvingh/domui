package to.etc.domui.component.meta.impl;

import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;

/**
 * A validator definition.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 24, 2008
 */
public class MetaPropertyValidatorImpl implements PropertyMetaValidator {
	private Class< ? extends IValueValidator< ? >> m_vclass;

	private String[] m_parameters;

	public MetaPropertyValidatorImpl(Class< ? extends IValueValidator< ? >> vclass) {
		m_vclass = vclass;
	}

	public MetaPropertyValidatorImpl(Class< ? extends IValueValidator< ? >> vclass, String[] parameters) {
		m_vclass = vclass;
		m_parameters = parameters;
	}

	public Class< ? extends IValueValidator< ? >> getValidatorClass() {
		return m_vclass;
	}

	public void setValidatorClass(Class< ? extends IValueValidator< ? >> vclass) {
		m_vclass = vclass;
	}

	public String[] getParameters() {
		return m_parameters;
	}

	public void setParameters(String[] parameters) {
		m_parameters = parameters;
	}
}
