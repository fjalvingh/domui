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

	static private final Map<Class< ? >, ClassInfo>	m_classMap	= new HashMap<Class< ? >, ClassInfo>();

	/**
	 * Get introspected bean information for the class. This info is cached so access will be fast after the 1st try.
	 * @param clz
	 * @return
	 */
	static synchronized public ClassInfo getClassInfo(Class< ? > clz) {
		ClassInfo ci = m_classMap.get(clz);
		if(ci == null) {
			List<PropertyInfo> proplist = calculateProperties(clz);
			ci = new ClassInfo(clz, proplist);
			m_classMap.put(clz, ci);
		}
		return ci;
	}

	static public PropertyInfo findPropertyInfo(Class< ? > clz, String property) {
		return getClassInfo(clz).findProperty(property);
	}

	static public List<PropertyInfo> getProperties(final Class< ? > cl) {
		ClassInfo ci = getClassInfo(cl);
		return ci.getProperties();
	}

	/**
	 * DO NOT USE - uncached calculation of a class's properties.
	 * @param cl
	 * @return
	 */
	static public List<PropertyInfo> calculateProperties(final Class< ? > cl) {
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
			else {
				//-- The stupid generics impl will generate Object-returning property methods also, but we need the actual typed one..
				if(i.getter == null)
					i.getter = m;
				else {
					if(i.getter.getReturnType().isAssignableFrom(m.getReturnType()))
						i.getter = m;
				}
			}
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
			//			if(setter == null) jal 20100205 read-only properties should be allowed explicitly.
			//				continue;
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

	static public void propertyNameToJava(StringBuilder sb, String in) {
		if(in.length() == 0)
			return;
		int len = sb.length();
		sb.append(in);
		sb.setCharAt(len, Character.toUpperCase(sb.charAt(len)));
	}

	static public String propertyNameToJava(String in) {
		StringBuilder sb = new StringBuilder();
		propertyNameToJava(sb, in);
		return sb.toString();
	}

	/**
	 * This tries to determine the value class for a property defined as some kind
	 * of Collection&lt;T&gt; or T[]. If the type cannot be determined this returns
	 * null.
	 *
	 * @param genericType
	 * @return
	 */
	static public Class< ? > findCollectionType(Type genericType) {
		if(genericType instanceof Class< ? >) {
			Class< ? > cl = (Class< ? >) genericType;
			if(cl.isArray()) {
				return cl.getComponentType();
			}
		}
		if(genericType instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) genericType;
			Type raw = pt.getRawType();

			//-- This must be a collection type of class.
			if(raw instanceof Class< ? >) {
				Class< ? > cl = (Class< ? >) raw;
				if(Collection.class.isAssignableFrom(cl)) {
					Type[] tar = pt.getActualTypeArguments();
					if(tar != null && tar.length == 1) { // Collection<T> required
						return (Class< ? >) tar[0];
					}
				}
			}
		}
		return null;
	}

	static public boolean isCollectionOrArrayType(Class< ? > clz) {
		return clz.isArray() || Collection.class.isAssignableFrom(clz);
	}


}
