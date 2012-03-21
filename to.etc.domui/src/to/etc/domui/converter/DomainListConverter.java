/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
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
	private PropertyMetaModel< ? > m_pmm;

	public DomainListConverter(PropertyMetaModel< ? > pmm) {
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
		return MetaManager.getEnumLabel(m_pmm, in);
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
		if(ar == null)
			throw new IllegalStateException(m_pmm + ": no domainValues");
		for(Object o: ar) {
			String v = MetaManager.getEnumLabel(m_pmm, o);
			if(v.equalsIgnoreCase(in))
				return o;
		}
		throw new ValidationException(Msgs.V_INVALID, in);
	}
}
