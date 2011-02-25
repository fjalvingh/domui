package to.etc.domui.state;

import java.util.*;

import to.etc.domui.component.meta.*;
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
	/** When set no data can be changed */
	private boolean m_readOnly = false;

	private Map<String, String> m_parameterMap = new HashMap<String, String>();

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

	public PageParameters getUnlockedCopy() {
		PageParameters clone = new PageParameters();
		for(Map.Entry<String, String> entry : m_parameterMap.entrySet()) {
			clone.addParameter(entry.getKey(), entry.getValue());
		}
		return clone;
	}

	public void setReadOnly() {
		//FIXME nmaksimovic 20110225 change back after Frits is back.
		//m_readOnly = true;
	}

	private void writeable() {
		if(m_readOnly)
			throw new IllegalStateException("This object is readonly and cannot be changed.");
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
		writeable();
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
				m_parameterMap.put(pk, String.valueOf(key));
			}
		}
	}

	/**
	 * Add string, value pair.
	 * @param k
	 * @param object
	 */
	private void internalAdd(String k, Object o) throws Exception {
		writeable();
		if(o == null)
			return;

		if(o instanceof ILongIdentifyable) {
			m_parameterMap.put(k, String.valueOf(((ILongIdentifyable) o).getId()));
			return;
		}
		Object key = o;
		ClassMetaModel cmm = MetaManager.findClassMeta(o.getClass());
		if(cmm.isPersistentClass()) {
			//-- Get the PK attribute of the persistent class;
			PropertyMetaModel pkpm = cmm.getPrimaryKey();
			if(pkpm != null) {
				key = pkpm.getAccessor().getValue(o);
				if(key == null)
					throw new IllegalStateException("The instance of " + o.getClass() + " passed has a null primary key");
			}
		}

		m_parameterMap.put(k, String.valueOf(key));
	}

	public void addParameter(String name, Object value) {
		writeable();

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
		m_parameterMap.put(name, s);
	}

	public boolean hasParameter(String name) {
		return m_parameterMap.containsKey(name);
	}

	public int getInt(String name) {
		String v = m_parameterMap.get(name);
		if(v != null) {
			try {
				return Integer.parseInt(v);
			} catch(Exception x) {}
		}
		throw new MissingParameterException(name);
	}

	public int getInt(String name, int df) {
		String v = m_parameterMap.get(name);
		if(v != null) {
			v = v.trim();
			if(v.length() > 0) {
				try {
					return Integer.parseInt(v);
				} catch(Exception x) {
					throw new MissingParameterException(name);
				}
			}
		}
		return df;
	}

	public long getLong(String name) {
		String v = m_parameterMap.get(name);
		if(v != null) {
			try {
				return Long.parseLong(v);
			} catch(Exception x) {}
		}
		throw new MissingParameterException(name);
	}

	public long getLong(String name, long df) {
		String v = m_parameterMap.get(name);
		if(v != null) {
			v = v.trim();
			if(v.length() > 0) {
				try {
					return Long.parseLong(v);
				} catch(Exception x) {
					throw new MissingParameterException(name);
				}
			}
		}
		return df;
	}

	public Long getLongW(String name) {
		String v = m_parameterMap.get(name);
		if(v != null) {
			try {
				return Long.decode(v);
			} catch(Exception x) {}
		}
		throw new MissingParameterException(name);
	}

	public Long getLongW(String name, long df) {
		return getLongW(name, Long.valueOf(df));
	}

	public Long getLongW(String name, Long df) {
		String v = m_parameterMap.get(name);
		if(v != null) {
			v = v.trim();
			if(v.length() > 0) {
				try {
					return Long.decode(v);
				} catch(Exception x) {
					throw new MissingParameterException(name);
				}
			}
		}
		return df;
	}

	public String getString(String name) {
		String v = m_parameterMap.get(name);
		if(v != null)
			return v;
		throw new MissingParameterException(name);
	}

	public String getString(String name, String df) {
		String v = m_parameterMap.get(name);
		return v == null ? df : v;
	}

	/**
	 * Create this from an actual request. This does not add any parameter that starts with _ or $.
	 * @param c
	 * @return
	 */
	static public PageParameters createFrom(IRequestContext ctx) {
		PageParameters pp = new PageParameters();
		for(String name : ctx.getParameterNames()) {
			if(name.length() > 0) {
				char c = name.charAt(0);
				if(c != '_' && c != '$' && !name.startsWith("webui"))
					pp.addParameter(name, ctx.getParameter(name));
			}
		}
		return pp;
	}

	@Override
	public String toString() {
		return "Parameters: " + m_parameterMap.toString();
	}

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
				String val = m_parameterMap.get(key);
				String aval = a.m_parameterMap.get(key);
				if(aval == null) // Key not found -> not equal.
					return false;
				if(!val.equals(aval))
					return false;
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
