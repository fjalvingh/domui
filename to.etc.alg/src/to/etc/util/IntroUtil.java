package to.etc.util;

import java.lang.reflect.*;
import java.util.*;

/**
 * Introspection and class-based utilities.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 22, 2007
 */
public class IntroUtil {
	private IntroUtil() {
	}

	/**
	 * Tries to load a class by name; returns null if not found.
	 * @param name
	 * @return
	 */
	static public Class< ? > findClass(String name) {
		try {
			return Class.forName(name);
		} catch(Exception x) {
			return null;
		}
	}

	static public <T> Class<T> findClass(String name, Class<T> base) {
		Class< ? > clz = findClass(name);
		if(clz == null)
			return null;
		if(!base.isAssignableFrom(clz))
			throw new IllegalStateException("The class '" + clz.getName() + "' is not a valid instance of a '" + base.getName() + "'");
		return (Class<T>) clz;
	}

	/**
	 * Retrieves all valid "getter" methods. These are methods isXXX() and getXXX() that are
	 * public nonstatic and not abstract.
	 *
	 * @param clz
	 * @param ignoreNameSet
	 * @return
	 */
	static public Map<String, Method> getGetterMap(Class< ? > clz, Set<String> ignoreNameSet) {
		Map<String, Method> gettermap = new HashMap<String, Method>();
		Method[] mar = clz.getMethods();
		StringBuilder sb = new StringBuilder(40);
		for(Method m : mar) {
			String name = m.getName();
			int nlen = name.length();
			int nindex;
			if(name.startsWith("get") && nlen > 3)
				nindex = 3;
			else if(name.startsWith("is") && nlen > 2)
				nindex = 2;
			else
				continue;

			//-- Name is valid. Check parameterlessity
			if(m.getParameterTypes().length != 0)
				continue;
			int mod = m.getModifiers();
			if(!Modifier.isPublic(mod) || Modifier.isAbstract(mod) || Modifier.isStatic(mod))
				continue;

			//-- Both name and thingy ok... Create property name
			sb.setLength(0);
			sb.append(name, nindex, nlen);
			sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
			name = sb.toString();
			if(ignoreNameSet != null && ignoreNameSet.contains(name)) // Must be ignored?
				continue;
			gettermap.put(name, m);
		}
		return gettermap;
	}

	static public Map<String, Method> getSetterMap(Class< ? > clz, Set<String> ignoreNameSet) {
		Map<String, Method> gettermap = new HashMap<String, Method>();
		Method[] mar = clz.getMethods();
		StringBuilder sb = new StringBuilder(40);
		for(Method m : mar) {
			String name = m.getName();
			int nlen = name.length();
			if(!name.startsWith("set") && nlen > 3)
				continue;

			//-- Name is valid. Must have max 1 parameter
			if(m.getParameterTypes().length != 1)
				continue;
			int mod = m.getModifiers();
			if(!Modifier.isPublic(mod) || Modifier.isAbstract(mod) || Modifier.isStatic(mod))
				continue;

			//-- Both name and thingy ok... Create property name
			sb.setLength(0);
			sb.append(name, 3, nlen);
			sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
			name = sb.toString();
			if(ignoreNameSet != null && ignoreNameSet.contains(name)) // Must be ignored?
				continue;
			gettermap.put(name, m);
		}
		return gettermap;
	}
}
