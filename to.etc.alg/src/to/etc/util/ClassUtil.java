package to.etc.util;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

final public class ClassUtil {
	private ClassUtil() {
	}


	/**
	 * Calls the given method with the given parameters in a given class instance. Used to access
	 * classes whose definition are not to be linked to the code.
	 *
	 * @param on
	 * @param name
	 * @param objects
	 * @return
	 * @throws NoSuchMethodException if no suitable method can be found in the object.
	 */
	static public Object callMethod(final Object on, final String name, final Object... param) throws Exception {
		Method m = findMethod(on.getClass(), name, param);
		if(m == null)
			throw new NoSuchMethodException("A suitable method " + name + " cannot be found");
		try {
			return m.invoke(on, param);
		} catch(InvocationTargetException itx) {
			if(itx.getCause() instanceof Exception)
				throw (Exception) itx.getCause();
			throw itx;
		}
	}

	static public Method findMethod(final Class< ? > clz, final String name, final Class< ? >[] param) {
		try {
			return clz.getMethod(name, param);
		} catch(Exception x) {
			return null;
		}
	}

	/**
	 * Tries to find a method that can be called using the specified parameters.
	 *
	 * @param clz
	 * @param name
	 * @param param
	 * @return
	 */
	static public Method findMethod(final Class< ? > clz, final String name, final Object[] param) {
		boolean hard = false;
		Class< ? >[] par = new Class< ? >[param.length];
		for(int i = param.length; --i >= 0;) {
			Object v = param[i];
			if(v == null) {
				hard = true;
				par[i] = null;
			} else {
				par[i] = v.getClass();
			}
		}

		//-- If not hard get method by types
		if(!hard)
			return findMethod(clz, name, par);

		//-- Find the best fit
		Method[] mar = clz.getMethods();
		Method res = null;
		for(Method m : mar) {
			if(!m.getName().equals(name))
				continue;
			Class< ? >[] far = m.getParameterTypes();
			if(far.length != par.length)
				continue;
			boolean ok = true;
			for(int j = far.length; --j >= 0;) {
				if(!far[j].isAssignableFrom(par[j])) {
					ok = false;
					break;
				}
			}
			if(!ok)
				continue;
			//-- This is a candidate -
			if(res != null)
				throw new IllegalStateException("Ambiguous method to call: " + res + " or " + m);
			res = m;
		}
		return res;
	}

	private static class Info {
		public Info() {
		}

		public String		name;

		public Method		getter;

		public List<Method>	setterList	= new ArrayList<Method>();
	};

	static public List<PropertyInfo> getProperties(final Class< ? > cl) {
		Map<String, Info> map = new HashMap<String, Info>();

		StringBuilder sb = new StringBuilder(40);
		for(Method m : cl.getMethods()) {
			//-- Check if this is a valid getter,
			int mod = m.getModifiers();
			if(!Modifier.isPublic(mod) || Modifier.isStatic(mod))
				continue;
			String name = m.getName();
			boolean setter = false;
			sb.setLength(0);
			if(name.startsWith("get")) {
				sb.append(name, 3, name.length());
			} else if(name.startsWith("is")) {
				sb.append(name, 2, name.length());
			} else if(name.startsWith("set")) {
				sb.append(name, 3, name.length());
				setter = true;
			} else
				continue;
			if(sb.length() == 0) // just "is", "get" or "set".
				continue;

			//-- Check parameters
			Class< ? >[] param = m.getParameterTypes();
			if(setter) {
				if(param.length != 1)
					continue;
			} else {
				if(param.length != 0)
					continue;
			}

			//-- Construct name.
			if(sb.length() == 1)
				sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
			else {
				if(!Character.isUpperCase(sb.charAt(1))) {
					sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
				}
			}
			name = sb.toString();
			Info i = map.get(name);
			if(i == null) {
				i = new Info();
				i.name = name;
				map.put(name, i);
			}
			if(setter)
				i.setterList.add(m);
			else
				i.getter = m;
		}

		//-- Construct actual list
		List<PropertyInfo> res = new ArrayList<PropertyInfo>();
		for(String name : map.keySet()) {
			Info i = map.get(name);
			if(i.getter == null)
				continue;
			Method setter = null;
			for(Method m : i.setterList) {
				if(m.getParameterTypes()[0] == i.getter.getReturnType()) {
					setter = m;
					break;
				}
			}
			if(setter == null)
				continue;
			res.add(new PropertyInfo(name, i.getter, setter));
		}
		return res;
	}

	/**
	 * Generic caller of a method using reflection. This prevents us from having
	 * to link to the stupid Oracle driver.
	 * @param src
	 * @param name
	 * @return
	 * @throws Exception
	 */
	static public Object callObjectMethod(final Object src, final String name, final Class< ? >[] types, final Object... parameters) throws SQLException {
		try {
			Method m = src.getClass().getMethod(name, types);
			return m.invoke(src, parameters);
		} catch(InvocationTargetException itx) {
			if(itx.getCause() instanceof SQLException)
				throw (SQLException) itx.getCause();
			throw new RuntimeException(itx.getCause().toString(), itx.getCause());
		} catch(Exception x) {
			throw new RuntimeException("Exception calling " + name + " on " + src + ": " + x, x);
		}
	}

	static public final Class< ? > loadClass(final ClassLoader cl, final String cname) {
		try {
			return cl.loadClass(cname);
		} catch(Exception x) {}
		return null;
	}

	/**
	 * Locates an annotation in an array of 'm, returns null if not found.
	 * @param <T>
	 * @param ar
	 * @param clz
	 */
	static public <T extends Annotation> T findAnnotation(final Annotation[] ar, final Class<T> clz) {
		for(Annotation a : ar) {
			if(a.annotationType() == clz)
				return (T) a;
		}
		return null;
	}
}
