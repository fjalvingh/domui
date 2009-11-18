package to.etc.domui.converter;

import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;

/**
 * Converts strings for properties whose domain is a value list (enum, boolean), using the metadata provided for that property.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 17, 2009
 */
public final class DomainListConverter implements IConverter<Object> {
	private PropertyMetaModel m_pmm;

	public DomainListConverter(PropertyMetaModel pmm) {
		m_pmm = pmm;
	}

	/**
	 * Convert the value passed into the label provided for that value.
	 * @see to.etc.domui.converter.IObjectToStringConverter#convertObjectToString(java.util.Locale, java.lang.Object)
	 */
	@Override
	public String convertObjectToString(Locale loc, Object in) throws UIException {
		if(in == null)
			return null;
		String s = m_pmm.getDomainValueLabel(loc, in); // Prefer property label
		if(s == null) {
			s = m_pmm.getClassModel().getDomainLabel(loc, in);
			if(s == null)
				s = in.toString();
		}
		return s;
	}

	/**
	 * Convert the label entered into the value for that label. Not normally used because LOV items are usually represented
	 * by a combobox.
	 * @see to.etc.domui.converter.IStringToObjectConverter#convertStringToObject(java.util.Locale, java.lang.String)
	 */
	@Override
	public Object convertStringToObject(Locale loc, String in) throws UIException {
		if(in == null)
			return null;
		Object[] ar = m_pmm.getDomainValues();
		for(Object o: ar) {
			String v = m_pmm.getDomainValueLabel(loc, o);
			if(v == null) {
				v = m_pmm.getClassModel().getDomainLabel(loc, o);
				if(v == null)
					v = o.toString();
			}
			if(v.equalsIgnoreCase(in))
				return o;
		}
		throw new ValidationException(Msgs.V_INVALID, in);
	}
}
