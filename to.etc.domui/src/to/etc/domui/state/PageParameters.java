package to.etc.domui.state;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.server.*;
import to.etc.domui.trouble.*;
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
	private Map<String, String[]> m_parameterMap = new HashMap<String, String[]>();

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
				//-- Must be [key, value] pair. Append it.
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
				m_parameterMap.put(pk, new String[] {String.valueOf(key)});
			}
		}
	}

	/**
	 * Add string, value pair.
	 * @param k
	 * @param object
	 */
	private void internalAdd(String k, Object o) throws Exception {
		if(o == null)
			return;

		if(o instanceof ILongIdentifyable) {
			m_parameterMap.put(k, new String[] {String.valueOf(((ILongIdentifyable) o).getId())});
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
		m_parameterMap.put(k, new String[] {keyval});
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
		m_parameterMap.put(name, new String[] {s});
	}

	/**
	 * Removes the parameter with specified name entirely from the map.
	 * 
	 * @param name, the name of the parameter to be removed.
	 */
	public void removeParameter(String name) {
		m_parameterMap.remove(name);
	}

	/**
	 * Indicates whether a given parameter name exists in this PageParameters object.
	 * 
	 * @param name, the name of the parameter to be checked for.
	 * @return true when the parameter exists, false otherwise.
	 */
	public boolean hasParameter(String name) {
		return m_parameterMap.containsKey(name);
	}

	/**
	 * Gets the value for the specified parametername as an int (primitive).
	 * When multiple value exists for the specified parameter, the first element of the array is returned.
	 * If the parameter does not exists or the value cannot be converted to an int, a MissingParameterException is thrown.
	 * 
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @return the value as an int
	 */
	public int getInt(String name) {
		String[] v = m_parameterMap.get(name);
		if(v != null) {
			try {
				return Integer.parseInt(v[0]);
			} catch(Exception x) {}
		}
		throw new MissingParameterException(name);
	}

	/**
	 * Gets the value for the specified parametername as an int (primitive).
	 * When multiple value exists for the specified parameter, the first element of the array is returned.
	 * If the parameter does cannot be converted to an int, a MissingParameterException is thrown.
	 * When the parameter does not exist, the specified default value is returned.
	 * 
	 * @param name, the name of the parameter who's value is to be retrieved.
	 * @param df, the default value to be returned, when the specified parameter does not exist.
	 * @return the value as an int
	 */
	public int getInt(String name, int df) {
		String[] v = m_parameterMap.get(name);
		if(v != null && v.length > 0) {
			v[0] = v[0].trim();
			if(v[0].length() > 0) {
				try {
					return Integer.parseInt(v[0]);
				} catch(Exception x) {
					throw new MissingParameterException(name);
				}
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
		String[] v = m_parameterMap.get(name);
		if(v != null) {
			try {
				return Long.parseLong(v[0]);
			} catch(Exception x) {}
		}
		throw new MissingParameterException(name);
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
		String[] v = m_parameterMap.get(name);
		if(v != null && v.length > 0) {
			v[0] = v[0].trim();
			if(v[0].length() > 0) {
				try {
					return Long.parseLong(v[0]);
				} catch(Exception x) {
					throw new MissingParameterException(name);
				}
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
		String[] v = m_parameterMap.get(name);
		if(v != null) {
			try {
				return Long.decode(v[0]);
			} catch(Exception x) {}
		}
		throw new MissingParameterException(name);
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
		String[] v = m_parameterMap.get(name);
		if(v != null && v.length > 0) {
			v[0] = v[0].trim();
			if(v[0].length() > 0) {
				try {
					return Long.decode(v[0]);
				} catch(Exception x) {
					throw new MissingParameterException(name);
				}
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
		String[] v = m_parameterMap.get(name);
		if(v != null && v.length > 0)
			return v[0];
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
		String[] v = m_parameterMap.get(name);
		return v == null || v.length == 0? df : v[0];
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
		String[] v = m_parameterMap.get(name);
		if(v != null && v.length > 0)
			return v;
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
				if(c != '_' && c != '$' && !name.startsWith("webui"))
					pp.addParameterArray(name, ctx.getParameters(name));
			}
		}
		return pp;
	}

	private void addParameterArray(String name, String[] parameters) {
		m_parameterMap.put(name, parameters);
	}

	@Override
	public String toString() {
		return "Parameters: " + m_parameterMap.toString();
	}

	/**
	 * Gets all the names of the parameters this object is holding
	 * @return the parameter names in an array
	 */
	@Nonnull
	public String[] getParameterNames() {
		return m_parameterMap.keySet().toArray(new String[m_parameterMap.size()]);
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
		if(obj instanceof PageParameters) {
			PageParameters a = (PageParameters) obj;
			if(a.m_parameterMap.size() != m_parameterMap.size()) // Maps differ -> done
				return false;

			for(String key : m_parameterMap.keySet()) {
				String[] vals = m_parameterMap.get(key);
				String[] avals = a.m_parameterMap.get(key);
				if(avals == null) // Key not found -> not equal.
					return false;
				if(vals.length != avals.length) //Different array lengths for parameterName
					return false;
				for (String val : vals) { //check all items in the array
					boolean found = false;
					for (String aval : avals) {//walk through the entire array, same order of members is not necessary to be equal
						if (aval.equals(val)) {
							found = true;
							break;
						}
					}
					if (!found) //this value was not found in other value array, unequal.
						return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		throw new IllegalStateException("missing");
	}
}