package to.etc.el;

import java.util.*;

public abstract class EnumeratedMap implements Map {
	private Map m_map;

	public abstract Enumeration enumerateKeys();

	public abstract boolean isMutable();

	public abstract Object getValue(Object pKey);

	public void clear() {
		throw new UnsupportedOperationException();
	}

	public boolean containsKey(Object pKey) {
		return getValue(pKey) != null;
	}

	public boolean containsValue(Object pValue) {
		return getAsMap().containsValue(pValue);
	}

	public Set entrySet() {
		return getAsMap().entrySet();
	}

	public Object get(Object pKey) {
		return getValue(pKey);
	}

	public boolean isEmpty() {
		return !enumerateKeys().hasMoreElements();
	}

	public Set keySet() {
		return getAsMap().keySet();
	}

	public int size() {
		return getAsMap().size();
	}

	public Collection values() {
		return getAsMap().values();
	}

	public Map getAsMap() {
		if(m_map != null)
			return m_map;
		Map m = convertToMap();
		if(!isMutable())
			m_map = m;
		return m;
	}

	Map convertToMap() {
		Map ret = new HashMap();
		for(Enumeration e = enumerateKeys(); e.hasMoreElements();) {
			Object key = e.nextElement();
			Object value = getValue(key);
			ret.put(key, value);
		}
		return ret;
	}

	public Object put(Object pKey, Object pValue) {
		throw new UnsupportedOperationException();
	}

	public void putAll(Map pMap) {
		throw new UnsupportedOperationException();
	}

	public Object remove(Object pKey) {
		throw new UnsupportedOperationException();
	}
}
