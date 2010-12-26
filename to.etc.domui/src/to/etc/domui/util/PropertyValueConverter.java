package to.etc.domui.util;

import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.trouble.*;
import to.etc.util.*;

/**
 * This converts an object to a string by creating a string from a list of
 * property values off that object.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 16, 2010
 */
final public class PropertyValueConverter<T> implements IConverter<T> {
	final private String[] m_properties;

	public PropertyValueConverter(String... properties) {
		m_properties = properties;
	}

	@Override
	public T convertStringToObject(Locale loc, String in) throws UIException {
		throw new IllegalStateException("This converter cannot be used for input");
	}

	/**
	 * Use the property list to get a value for each property, and append to the string to return.
	 * @see to.etc.domui.converter.IObjectToStringConverter#convertObjectToString(java.util.Locale, java.lang.Object)
	 */
	@Override
	public String convertObjectToString(Locale loc, T in) throws UIException {
		if(null == in)
			return "";
		Class< ? > clz = in.getClass();
		ClassMetaModel cmm = MetaManager.findClassMeta(clz);
		StringBuilder sb = new StringBuilder();
		for(String pname : m_properties) {
			PropertyMetaModel< ? > pmm = cmm.findProperty(pname);
			if(null == pmm)
				throw new IllegalArgumentException("The property '"+pname+"' is unknown on class "+clz.getName()+" ("+this+")");
			Object value;
			try {
				value = pmm.getValue(in);
			} catch(Exception x) {
				throw WrappedException.wrap(x); // Oh, the joys of checked exceptions... Bah.
			}

			if(value != null) {
				if(sb.length() > 0)
					sb.append(" ");
				sb.append(String.valueOf(value));
			}
		}
		return sb.toString();
	}
}
