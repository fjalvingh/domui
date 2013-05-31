package to.etc.log;

import java.util.*;

/**
 * MDC implementation done by mirroring code available for logback 0.9.21 implementation.
 * Original author is Ceki G&uuml;lc&uuml;
 * 
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Oct 31, 2012
 */
public class EtcMDCAdapter implements org.slf4j.spi.MDCAdapter {

	/** MDC key that is used to store user login id */
	public static final String	LOGINID	= "loginId";

	/** MDC key that is used to store HTTP session id */
	public static final String	SESSION	= "session";

	/**
	 * This class extends InheritableThreadLocal so that children threads get a copy
	 * of the parent's hashmap.
	 */
	public class CopyOnInheritThreadLocal extends InheritableThreadLocal<HashMap<String, String>> {

		/**
		 * Child threads should get a copy of the parent's hashmap.
		 */
		@Override
		protected HashMap<String, String> childValue(HashMap<String, String> parentValue) {
			if(parentValue == null) {
				return null;
			} else {
				HashMap<String, String> hm = new HashMap<String, String>(parentValue);
				return hm;
			}
		}
	}

	final CopyOnInheritThreadLocal	copyOnInheritThreadLocal	= new CopyOnInheritThreadLocal();

	public EtcMDCAdapter() {
	}

	/**
	 * Put a context value (the <code>val</code> parameter) as identified with the
	 * <code>key</code> parameter into the current thread's context map. Note that
	 * contrary to log4j, the <code>val</code> parameter can be null.
	 * 
	 * <p>
	 * If the current thread does not have a context map it is created as a side
	 * effect of this call.
	 * 
	 * <p>
	 * Each time a value is added, a new instance of the map is created. This is
	 * to be certain that the serialization process will operate on the updated
	 * map and not send a reference to the old map, thus not allowing the remote
	 * logback component to see the latest changes.
	 * 
	 * @throws IllegalArgumentException
	 *           in case the "key" parameter is null
	 */
	@Override
	public void put(String key, String val) throws IllegalArgumentException {
		if(key == null) {
			throw new IllegalArgumentException("key cannot be null");
		}

		HashMap<String, String> oldMap = copyOnInheritThreadLocal.get();

		HashMap<String, String> newMap = new HashMap<String, String>();
		if(oldMap != null) {
			newMap.putAll(oldMap);
		}
		// the newMap replaces the old one for serialisation's sake
		copyOnInheritThreadLocal.set(newMap);
		newMap.put(key, val);
	}

	/**
	 * Get the context identified by the <code>key</code> parameter.
	 * 
	 * <p>
	 * This method has no side effects.
	 */
	@Override
	public String get(String key) {
		HashMap<String, String> hashMap = copyOnInheritThreadLocal.get();

		if((hashMap != null) && (key != null)) {
			return hashMap.get(key);
		} else {
			return null;
		}
	}

	/**
	 * Remove the the context identified by the <code>key</code> parameter.
	 * 
	 * <p>
	 * Each time a value is removed, a new instance of the map is created. This is
	 * to be certain that the serialization process will operate on the updated
	 * map and not send a reference to the old map, thus not allowing the remote
	 * component to see the latest changes.
	 */
	@Override
	public void remove(String key) {
		if(key == null) {
			return;
		}
		HashMap<String, String> oldMap = copyOnInheritThreadLocal.get();

		HashMap<String, String> newMap = new HashMap<String, String>();
		if(oldMap != null) {
			newMap.putAll(oldMap);
		}
		// the newMap replaces the old one for serialisation's sake
		copyOnInheritThreadLocal.set(newMap);
		newMap.remove(key);
	}

	/**
	 * Clear all entries in the MDC.
	 */
	@Override
	public void clear() {
		HashMap<String, String> hashMap = copyOnInheritThreadLocal.get();

		if(hashMap != null) {
			hashMap.clear();
			copyOnInheritThreadLocal.remove();
		}
	}

	/**
	 * Get the current thread's MDC as a map. This method is intended to be used
	 * internally.
	 */
	public Map<String, String> getPropertyMap() {
		return copyOnInheritThreadLocal.get();
	}

	/**
	 * Return a copy of the current thread's context map. Returned value may be
	 * null.
	 */
	@Override
	public Map<String, String> getCopyOfContextMap() {
		HashMap<String, String> hashMap = copyOnInheritThreadLocal.get();
		if(hashMap == null) {
			return null;
		} else {
			return new HashMap<String, String>(hashMap);
		}
	}

	/**
	 * Returns the keys in the MDC as a {@link Set}. The returned value can be
	 * null.
	 */
	public Set<String> getKeys() {
		HashMap<String, String> hashMap = copyOnInheritThreadLocal.get();

		if(hashMap != null) {
			return hashMap.keySet();
		} else {
			return null;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setContextMap(Map contextMap) {
		HashMap<String, String> oldMap = copyOnInheritThreadLocal.get();

		HashMap<String, String> newMap = new HashMap<String, String>();
		newMap.putAll(contextMap);

		// the newMap replaces the old one for serialisation's sake
		copyOnInheritThreadLocal.set(newMap);

		// hints for the garbage collector
		if(oldMap != null) {
			oldMap.clear();
			oldMap = null;
		}
	}
}
