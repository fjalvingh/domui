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
package to.etc.domui.state;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.server.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.util.*;
import to.etc.webapp.qsql.*;

/**
 * Encapsulates parameters for a page. All parameters must be presentable in URL form,
 * i.e. they must be renderable as part of a GET or POST. A page request formed by a
 * Page class and a PageParameters class is bookmarkable.
 * This is a mutable object.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 22, 2008
 */
public class PageParameters {
	/**
	 * Contains either String or String[], maps parameter name to either one or an array of values of that parameter.
	 */
	private Map<String, Object> m_map = new HashMap<String, Object>();

	public PageParameters() {}

	public PageParameters(Object... list) {
		try {
			addParameters(list);
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
		//
		//
		//		if((list.length & 0x1) != 0)
		//			throw new IllegalStateException("Incorrect parameter count: must be an even number of objects, each [string], [object]");
		//		for(int i = 0; i < list.length; i += 2) {
		//			Object a = list[i];
		//			if(!(a instanceof String))
		//				throw new IllegalStateException("Expecting a 'String' as parameter " + i + ", but got a '" + a + "'");
		//			Object b = list[i + 1];
		//			addParameter((String) a, b);
		//		}
	}

	/**
	 * Primitive, only allowing String value.
	 * @param name
	 * @param value
	 */
	private void setParameter(String name, String value) {
		m_map.put(name, value);
	}

	/**
	 * Primitive, only allowing string array.
	 * @param name
	 * @param values
	 */
	private void setParameter(String name, String[] values) {
		m_map.put(name, values);
	}

	/**
	 * Returns a single value for a parameter. The parameter must either be a single
	 * string, or must be a 1-size array.
	 * @param name
	 * @return
	 */
	@Nullable
	private String	getOne(String name) {
		Object v = m_map.get(name);
		if(null == v)
			return null;
		if(v instanceof String)
			return (String) v;
		String[] ar = (String[]) v;
		if(ar.length == 0)		// Questionable: allow 0-size array and treat as empty; rationale: this parameter would not actually occur on the url.
			return null;
		if(ar.length == 1)
			return ar[0];
		throw new MultipleParameterException(name); // There can be only oneeeeee.. </highlander>
	}

	/**
	 * Add parameters. This accepts multiple formats that can all be mixed. Each actual parameter always is a
	 * name, value pair. The simplest way to use this is to specify a list of strings in pairs where the first
	 * string in the pair is the key and the second one is the value. You can also substitute an object instance
	 * for the value; if this object instance represents some persistent entity or implements {@link ILongIdentifyable}
	 * the primary key for the object will be rendered as the value, otherwise it will be rendered as a tostring.
	 * You can also specify a single object in the location for the next key; in this case both key and value will
	 * be determined from this object; it must be some persistent object which knows it's key field.
	 *
	 * @param plist
	 */
	public void addParameters(Object... plist) throws Exception {
		int ix = 0;
		int len = plist.length;
		while(ix < len) {
			Object k = plist[ix++]; // Consume the key
			if(k instanceof String) {
				//-- Must be [key, value] pair. Append it. The value can be array.
				if(ix >= len)
					throw new IllegalStateException("Missing value for key string '" + k + "'");
				internalAdd((String) k, plist[ix++]);
			} else if(k != null) {
				//-- Non-string @ key position: this must be a persistent class.
				ClassMetaModel cmm = MetaManager.findClassMeta(k.getClass());
				if(! cmm.isPersistentClass())
					throw new IllegalStateException("Instance of "+k.getClass()+" is not a persistent class");
				//-- Get the PK attribute of the persistent class;
				PropertyMetaModel pkpm = cmm.getPrimaryKey();
				if(pkpm == null)
					throw new IllegalStateException("The instance of " + k.getClass() + " passed has no primary key defined");
				Object key = pkpm.getAccessor().getValue(k);
				if(key == null)
					throw new IllegalStateException("The instance of " + k.getClass() + " passed has a null primary key");

				String	pk = cmm.getClassBundle().getString("pk.name");
				if(pk == null) {
					pk = k.getClass().getName();
					pk = pk.substring(pk.lastIndexOf('.') + 1);
				}
				setParameter(pk, String.valueOf(key));
			}
		}
	}

	/**
	 * Add string, value pair. Existing parameter is overwritten.
	 * @param k
	 * @param object
	 */
	private void internalAdd(String k, Object o) throws Exception {
		if(o == null)
			return;

		if(o instanceof ILongIdentifyable) {
			setParameter(k, String.valueOf(((ILongIdentifyable) o).getId()));
			return;
		}

		if(o instanceof String[]) {
			setParameter(k, (String[]) o);
			return;
		}

		String keyval = null;
		ClassMetaModel cmm = MetaManager.findClassMeta(o.getClass());
		if(cmm.isPersistentClass()) {
			//-- Get the PK attribute of the persistent class;
			PropertyMetaModel pkpm = cmm.getPrimaryKey();
			if(pkpm != null) {
				Object key = pkpm.getAccessor().getValue(o);
				if(key == null)
					throw new IllegalStateException("The instance of " + o.getClass() + " passed has a null primary key");
				keyval = CompoundKeyConverter.INSTANCE.marshal(key);
			}
		}

		if(keyval == null)
			keyval = String.valueOf(o);
		setParameter(k, keyval);
	}

	/**
	 * Adds a parameter with the specified name. When a parameter with the same name already exists,
	 * the existing value will be overwritten with the specified value.
	 *
	 * @param name, the parameter name.
	 * @param value, the (new) value.
	 */
	public void addParameter(String name, Object value) {
		//-- Convert the value to a string;
		String s;
		if(value == null)
			s = "null";
		else if(value instanceof String)
			s = (String) value;
		else if(value instanceof Number) {
			s = value.toString();
		} else if(value instanceof Boolean)
			s = value.toString();
		else
			throw new IllegalStateException("Cannot convert a " + value.getClass() + " to an URL parameter yet - parameter converters not implemented yet");
		setParameter(name, s);
	}

	/**
	 * Removes the parameter with specified name entirely from the map.
	 *
	 * @param name, the name of the parameter to be removed.
	 */
	public void removeParameter(String name) {
		m_map.remove(name);
	}

	/**
	 * Indicates whether a given parameter name exists in this PageParameters object.
	 *
	 * @param name, the name of the parameter to be checked for.
	 * @return true when the parameter exists, false otherwise.
	 */
	public boolean hasParameter(String name) {
		return m_map.containsKey(name);
	}

	/**
	 * Gets the value for the specified parametername as an int (primitive).
	 * If the parameter does not exists or the value cannot be converted to an int, a MissingParameterException is thrown.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @return the value as an int
	 */
	public int getInt(String name) {
		String v = getOne(name);
		if(null == v)
			throw new MissingParameterException(name);
		try {
			return Integer.parseInt(v);
		} catch(Exception x) {
		}
		throw new UnusableParameterException(name, "int", v);
	}

	/**
	 * Gets the value for the specified parametername as an int (primitive).
	 * If the parameter does cannot be converted to an int, a MissingParameterException is thrown.
	 * When the parameter does not exist, the specified default value is returned.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @param df, the default value to be returned, when the specified parameter does not exist.
	 * @return the value as an int
	 */
	public int getInt(String name, int df) {
		String v = getOne(name);
		if(null != v && (v = v.trim()).length() > 0) {
			try {
				return Integer.parseInt(v);
			} catch(Exception x) {
				throw new UnusableParameterException(name, "int", v);
			}
		}
		return df;
	}

	/**
	 * Gets the value for the specified parametername as a long (primitive).
	 * When multiple value exists for the specified parameter, the first element of the array is returned.
	 * If the parameter does not exists or the value cannot be converted to an int, a MissingParameterException is thrown.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @return the value as a long
	 */
	public long getLong(String name) {
		String v = getOne(name);
		if(null == v)
			throw new MissingParameterException(name);
		try {
			return Long.parseLong(v);
		} catch(Exception x) {
		}
		throw new UnusableParameterException(name, "long", v);
	}

	/**
	 * Gets the value for the specified parametername as a long (primitive).
	 * When multiple value exists for the specified parameter, the first element of the array is returned.
	 * If the parameter does cannot be converted to an int, a MissingParameterException is thrown.
	 * When the parameter does not exist, the specified default value is returned.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @param df, the default value to be returned, when the specified parameter does not exist.
	 * @return the value as a long
	 */
	public long getLong(String name, long df) {
		String v = getOne(name);
		if(null != v && (v = v.trim()).length() > 0) {
			try {
				return Long.parseLong(v);
			} catch(Exception x) {
				throw new UnusableParameterException(name, "long", v);
			}
		}
		return df;
	}

	/**
	 * Gets the value for the specified parametername as a Long object.
	 * When multiple value exists for the specified parameter, the first element of the array is returned.
	 * If the parameter does not exists or the value cannot be converted to an int, a MissingParameterException is thrown.
	 * This method uses decode() so hexadecimal and octal strings can be used as parameter values.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @return the value as a Long
	 */
	public Long getLongW(String name) {
		String v = getOne(name);
		if(null == v)
			throw new MissingParameterException(name);
		try {
			return Long.decode(v);
		} catch(Exception x) {
		}
		throw new UnusableParameterException(name, "long", v);
	}

	/**
	 * Gets the value for the specified parametername as a Long object.
	 * When multiple value exists for the specified parameter, the first element of the array is returned.
	 * If the parameter does cannot be converted to an int, a MissingParameterException is thrown.
	 * When the parameter does not exist, the specified default value is returned.
	 * This method uses decode() so hexadecimal and octal strings can be used as parameter values.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @param df, the default value to be returned, when the specified parameter does not exist.
	 * @return the value as a Long
	 */
	public Long getLongW(String name, long df) {
		return getLongW(name, Long.valueOf(df));
	}

	/**
	 * Gets the value for the specified parametername as a Long object.
	 * When multiple value exists for the specified parameter, the first element of the array is returned.
	 * If the parameter does cannot be converted to an int, a MissingParameterException is thrown.
	 * When the parameter does not exist, the specified default value is returned.
	 * This method uses decode() so hexadecimal and octal strings can be used as parameter values.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @param df, the default value to be returned, when the specified parameter does not exist.
	 * @return the value as a Long
	 */
	public Long getLongW(String name, Long df) {
		String v = getOne(name);
		if(null != v && (v = v.trim()).length() > 0) {
			try {
				return Long.decode(v);
			} catch(Exception x) {
				throw new UnusableParameterException(name, "long", v);
			}
		}
		return df;
	}

	/**
	 * Gets the value for the specified parametername as a String object.
	 * When multiple value exists for the specified parameter, the first element of the array is returned.
	 * When the parameter does not exist, a MissingParameterException is thrown.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @return the value as a String
	 */
	@Nonnull
	public String getString(String name) {
		String v = getOne(name);
		if(v != null)
			return v;
		throw new MissingParameterException(name);
	}

	/**
	 * Gets the value for the specified parametername as a String object.
	 * When multiple value exists for the specified parameter, the first element of the array is returned.
	 * When the parameter does not exist, the specified default value is returned.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @param df, the default value to be returned, when the specified parameter does not exist.
	 * @return the value as a String
	 */
	@Nullable
	public String getString(String name, String df) {
		String v = getOne(name);
		if(null == v || v.length() == 0)
			return df;
		return v;
	}

	/**
	 * Gets the value for the specified parametername as a String array.
	 * When the parameter does not exist, a MissingParameterException is thrown.
	 * This method is provided for legacy reasons only.
	 * The domui framework discourages uses of parameter arrays.
	 *
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @return the value as a String
	 */
	@Nonnull
	public String[] getStringArray(String name) {
		Object var = m_map.get(name);
		if(null != var) {
			if(var instanceof String)
				return new String[]{(String) var};
			String[] ar = (String[]) var;
			if(ar.length > 0)
				return ar;
		}
		throw new MissingParameterException(name);
	}

	/**
	 * Create this from an actual request. This does not add any parameter that starts with _ or $.
	 * @param c
	 * @return
	 */
	@Nonnull
	static public PageParameters createFrom(IRequestContext ctx) {
		PageParameters pp = new PageParameters();
		for(String name : ctx.getParameterNames()) {
			if(name.length() > 0) {
				char c = name.charAt(0);
				if(c != '_' && c != '$' && !name.startsWith("webui")) {
					String[] par = ctx.getParameters(name);
					if(par != null && par.length > 0) {
						if(par.length == 1)
							pp.m_map.put(name, par[0]); // Add as single string
						else
							pp.m_map.put(name, par); // Add as string[]0
					}
				}
			}
		}
		return pp;
	}

	@Override
	public String toString() {
		//-- Must render explicitly now because array toString method does not print members
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, Object> me : m_map.entrySet()) {
			if(me.getValue() instanceof String) {
				if(sb.length() > 0)
					sb.append("&");
				sb.append(me.getKey()).append('=').append(me.getValue());
			} else {
				String[] vals = (String[]) me.getValue();
				for(String s : vals) {
					if(sb.length() > 0)
						sb.append("&");
					sb.append(me.getKey()).append('=').append(s);
				}
			}
		}
		return "Parameters: " + sb.toString();
	}

	/**
	 * Gets all the names of the parameters this object is holding
	 * @return the parameter names in an array
	 */
	@Nonnull
	public String[] getParameterNames() {
		return m_map.keySet().toArray(new String[m_map.size()]);
	}

	/**
	 * Compare this with another instance. Used to see that a new request has different parameters
	 * than an earlier request.
	 * <h2>remark</h2>
	 * <p>We check the size of the maps; if they are equal we ONLY have to check that each key-value
	 * pair in SOURCE exists in TARGET AND is the same. We don't need to check for "thingies in SRC
	 * that do not occur in TGT" because that cannot happen if the map sizes are equal.</p>
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(! (obj instanceof PageParameters))
			return false;

		PageParameters a = (PageParameters) obj;
		if(a.m_map.size() != m_map.size()) // Maps differ -> done
			return false;

		for(String key : m_map.keySet()) {
			Object oval = a.m_map.get(key);
			Object val = m_map.get(key);
			if(!compValues(oval, val))
				return false;
		}
		return true;
	}

	private boolean compValues(Object oval, Object val) {
		if(oval instanceof String && val instanceof String) {
			return oval.equals(val);
		}
		if(oval instanceof String[] && val instanceof String[]) {
			String[] a = (String[]) oval;
			String[] b = (String[]) val;
			if(a.length != b.length)
				return false;
			//-- walk through the entire array, same order of members is not necessary to be equal
			for(String av : a) {
				boolean found = false;
				for(String bv : b) {
					if(DomUtil.isEqual(av, bv)) {
						found = true;
						break;
					}
				}
				if(!found)
					return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		throw new IllegalStateException("missing");
	}
}
