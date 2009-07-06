package to.etc.domui.state;

import java.util.*;

import to.etc.domui.server.*;
import to.etc.domui.trouble.*;

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
	private Map<String, String> m_parameterMap = new HashMap<String, String>();

	public PageParameters() {}

	public PageParameters(Object... list) {
		if((list.length & 0x1) != 0)
			throw new IllegalStateException("Incorrect parameter count: must be an even number of objects, each [string], [object]");
		for(int i = 0; i < list.length; i += 2) {
			Object a = list[i];
			if(!(a instanceof String))
				throw new IllegalStateException("Expecting a 'String' as parameter " + i + ", but got a '" + a + "'");
			Object b = list[i + 1];
			addParameter((String) a, b);
		}
	}

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
