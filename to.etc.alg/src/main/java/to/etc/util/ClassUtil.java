/*
 * DomUI Java User Interface - shared code
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
package to.etc.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final public class ClassUtil {
	private ClassUtil() {
	}

	/**
	 * Calls the given method with the given parameters in a given class instance. Used to access
	 * classes whose definition are not to be linked to the code.
	 *
	 * @throws NoSuchMethodException if no suitable method can be found in the object.
	 */
	@Nullable
	static public Object callMethod(@Nonnull final Object on, @Nonnull final String name, @Nonnull final Object... param) throws Exception {
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

	/**
	 * Calls the given method with the given parameters in a given class instance. Used to access
	 * classes whose definition are not to be linked to the code.
	 *
	 * @throws NoSuchMethodException if no suitable method can be found in the object.
	 */
	@Nullable
	static public Object callMethod(@Nonnull final Object on, @Nonnull final String name, @Nonnull Class[] formals, @Nonnull final Object... instances) throws Exception {
		Method m = findMethod(on.getClass(), name, formals);
		if(m == null)
			throw new NoSuchMethodException("A suitable method " + name + " cannot be found");
		try {
			return m.invoke(on, instances);
		} catch(InvocationTargetException itx) {
			if(itx.getCause() instanceof Exception)
				throw (Exception) itx.getCause();
			throw itx;
		}
	}

	@Nullable
	static public Method findMethod(@Nonnull final Class< ? > clz, @Nonnull final String name, @Nonnull final Class< ? >... param) {
		try {
			return clz.getMethod(name, param);
		} catch(Exception x) {
		}
		try {
			return clz.getDeclaredMethod(name, param);
		} catch(Exception x) {
			return null;
		}
	}

	/**
	 * Tries to find a method that can be called using the specified parameters.
	 */
	@Nullable
	static public Method findMethod(@Nonnull final Class< ? > clz, @Nonnull final String name, @Nonnull final Object... param) {
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

		public boolean isPrivate;

		public Method		getter;

		public List<Method>	setterList	= new ArrayList<Method>();
	}

	static private final Map<Class< ? >, ClassInfo>	m_classMap	= new HashMap<Class< ? >, ClassInfo>();

	/**
	 * Get introspected bean information for the class. This info is cached so access will be fast after the 1st try.
	 */
	@Nonnull
	static synchronized public ClassInfo getClassInfo(@Nonnull Class< ? > clz) {
		ClassInfo ci = m_classMap.get(clz);
		if(ci == null) {
			List<PropertyInfo> proplist = calculateProperties(clz);
			ci = new ClassInfo(clz, proplist);
			m_classMap.put(clz, ci);
		}
		return ci;
	}

	@Nullable
	static public PropertyInfo findPropertyInfo(@Nonnull Class< ? > clz, @Nonnull String property) {
		return getClassInfo(clz).findProperty(property);
	}

	@Nonnull
	static public List<PropertyInfo> getProperties(@Nonnull final Class< ? > cl) {
		ClassInfo ci = getClassInfo(cl);
		return ci.getProperties();
	}

	/**
	 * DO NOT USE - uncached calculation of a class's properties.
	 * @param cl
	 * @return
	 */
	@Nonnull
	static public List<PropertyInfo> calculateProperties(@Nonnull final Class< ? > cl) {
		return calculateProperties(cl, true);
	}

	/**
	 * DO NOT USE - uncached calculation of a class's properties.
	 */
	@Nonnull
	static public List<PropertyInfo> calculateProperties(@Nonnull final Class< ? > cl, boolean publicOnly) {
		Map<String, Info> map = new HashMap<String, Info>();

		//-- First handle private properties
		if(! publicOnly) {
			for(Method m : cl.getDeclaredMethods()) {
				checkPropertyMethod(map, m, publicOnly);
			}
		}

		//-- And let those be overridden by public ones
		for(Method m : cl.getMethods()) {
			checkPropertyMethod(map, m, publicOnly);
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

	private static void checkPropertyMethod(Map<String, Info> map, Method m, boolean publicOnly) {
		//-- Check if this is a valid getter,
		int mod = m.getModifiers();
		if(Modifier.isStatic(mod) || (publicOnly && !Modifier.isPublic(mod)) )
			return;
		String name = m.getName();
		boolean setter = false;
		StringBuilder sb = new StringBuilder(40);
		if(name.startsWith("get")) {
			sb.append(name, 3, name.length());
		} else if(name.startsWith("is")) {
			sb.append(name, 2, name.length());
		} else if(name.startsWith("set")) {
			sb.append(name, 3, name.length());
			setter = true;
		} else
			return;
		if(sb.length() == 0) // just "is", "get" or "set".
			return;

		//-- Check parameters
		Class< ? >[] param = m.getParameterTypes();
		if(setter) {
			if(param.length != 1)
				return;
		} else {
			if(param.length != 0)
				return;
		}

		//-- Construct name.
		if(sb.length() == 1)
			sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
		else {
			if(!Character.isUpperCase(sb.charAt(1))) {
				sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
			}
		}

		boolean pvt = Modifier.isPrivate(mod);
		name = sb.toString();
		Info i = map.get(name);
		if(i == null) {
			i = new Info();
			map.put(name, i);
		}

		//-- Private rules: a property is private only if the getter is private.
		if(pvt) {
			if(! setter) {
				i.isPrivate = true;
			}
		} else if(i.isPrivate && ! setter) {
			i.isPrivate = false;
			//i.getter = null;
			//i.setterList.clear();
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

	@Nonnull
	static public String getMethodName(@Nonnull String prefix, @Nonnull String property) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix);
		if(property.length() > 0) {
			sb.append(Character.toUpperCase(property.charAt(0)));
			sb.append(property, 1, property.length());
		}
		return sb.toString();
	}


	/**
	 * Generic caller of a method using reflection. This prevents us from having
	 * to link to the stupid Oracle driver.
	 */
	@Nullable
	static public Object callObjectMethod(@Nonnull final Object src, @Nonnull final String name, @Nonnull final Class< ? >[] types, @Nonnull final Object... parameters) throws SQLException {
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

	@Nonnull
	static public final <T> T loadInstance(@Nonnull final ClassLoader cl, @Nonnull Class<T> clz, @Nonnull final String className) throws Exception {
		Class< ? > acl;
		try {
			acl = cl.loadClass(className);
		} catch(Exception x) {
			throw new RuntimeException("The class " + className + " cannot be found/loaded: " + x);
		}

		if(!clz.isAssignableFrom(acl))
			throw new IllegalArgumentException("The class " + className + " is not a/does not implement " + clz.getName());

		try {
			return (T) acl.newInstance();
		} catch(Exception x) {
			throw new RuntimeException("Cannot instantiate " + acl + ": " + x);
		}
	}

	/**
	 * Locates an annotation in an array of 'm, returns null if not found.
	 */
	@Nullable
	static public <T extends Annotation> T findAnnotation(@Nonnull final Annotation[] ar, @Nonnull final Class<T> clz) {
		for(Annotation a : ar) {
			if(a.annotationType() == clz)
				return (T) a;
		}
		return null;
	}

	static public void propertyNameToJava(@Nonnull StringBuilder sb, @Nonnull String in) {
		if(in.length() == 0)
			return;
		int len = sb.length();
		sb.append(in);
		sb.setCharAt(len, Character.toUpperCase(sb.charAt(len)));
	}

	@Nonnull
	static public String propertyNameToJava(@Nonnull String in) {
		StringBuilder sb = new StringBuilder();
		propertyNameToJava(sb, in);
		return sb.toString();
	}

	/**
	 * This tries to determine the value class for a property defined as some kind
	 * of Collection&lt;T&gt; or T[]. If the type cannot be determined this returns
	 * null.
	 */
	@Nullable
	static public Class< ? > findCollectionType(@Nonnull Type genericType) {
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

	static public boolean isCollectionOrArrayType(@Nonnull Class< ? > clz) {
		return clz.isArray() || Collection.class.isAssignableFrom(clz);
	}

	/*
	 * Walk the class hierarchy and create a list that goes from base class to derived class. This includes both classes
	 * and interfaces, where interfaces have "multiple bases".
	 */
	@Nonnull
	static public List<Class< ? >> getClassHierarchy(@Nonnull Class< ? > clzin) {
		List<Class< ? >> res = new ArrayList<Class< ? >>();
		appendClassHierarchy(res, clzin);
		return res;
	}

	static public void appendClassHierarchy(@Nonnull List<Class< ? >> res, @Nonnull Class< ? > clzin) {
		if(res.contains(clzin))
			return;

		//-- If this class has a superclass (it is not an interface and not Object) then add that 1st
		Class< ? > sclz = clzin.getSuperclass();
		if(null != sclz) {
			appendClassHierarchy(res, sclz); // First add parts upside the hierarchy.
		}

		//-- Get all implemented interfaces as bases and add them 1st also
		Class< ? >[] ifar = clzin.getInterfaces();
		for(Class< ? > iclz : ifar) {
			appendClassHierarchy(res, iclz);
		}
		if(res.contains(clzin))
			return;
		res.add(clzin);
	}

	/**
	 * Scan the classloader hierarchy and find all urls.
	 */
	@Nonnull
	static public URL[] findUrlsFor(@Nonnull ClassLoader loader) {
		List<URL> res = new ArrayList<URL>();
		findUrlsFor(res, loader);
		return res.toArray(new URL[res.size()]);
	}

	/**
	 * Checks to see what kind of classloader this is, and add all paths to my list.
	 */
	static private void findUrlsFor(@Nonnull List<URL> result, @Nullable ClassLoader loader) {
		//		System.out.println(".. loader="+loader);
		if(loader == null)
			return;
		if(loader instanceof URLClassLoader) {
			URLClassLoader ucl = (URLClassLoader) loader;
			for(URL u : ucl.getURLs()) {
				result.add(u);
			}
		}
		ClassLoader parent = loader.getParent();
		if(null != parent)
			findUrlsFor(result, parent);
	}

	@Nullable
	public static <T> Constructor<T> findConstructor(@Nonnull Class<T> clz, @Nonnull Class< ? >... formals) {
		try {
			return clz.getConstructor(formals);
		} catch(Exception x) {
			return null;
		}
	}

	public static <T> T callConstructor(Class<T> clz, Class< ? >[] formals, Object... args) throws Exception {
		Constructor<T> c = findConstructor(clz, formals);
		if(c == null)
			throw new IllegalStateException("Cannot find constructor in " + clz + " with args " + Arrays.toString(formals));
		return c.newInstance(args);
	}

	/**
	 * Finds sources for classes in the same project. Not meant for jar searching
	 */
	public static @Nullable
	File findSrcForModification(@Nonnull String className) throws Exception {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		final String rel = className.replace(".", "/") + ".class";
		URL resource = classLoader.getResource(rel);
		if(resource == null) {
			return null;
		}
		final String path = resource.getFile();
		if(resource.getProtocol().equals("jar")) {
			throw new Exception("Finding sources in jars for modifications is not supported.");
		}
		String srcRel = rel.substring(0, rel.length() - 5) + "java";

		File root = new File(path.substring(0, path.length() - rel.length()));
		File[] files = new File[1];
		find(root.getParentFile(), srcRel, files);
		return files[0];
	}

	/**
	 * Finds source folder for package in the same project. Not meant for jar searching
	 */
	public static @Nullable
	File findSrcFolderForModification(@Nonnull String packageName) throws Exception {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		final String rel = packageName.replace(".", "/");
		URL resource = classLoader.getResource(rel);
		if(resource == null) {
			return null;
		}
		final String path = resource.getFile();
		if(resource.getProtocol().equals("jar")) {
			throw new Exception("Finding sources in jars for modifications is not supported.");
		}
		File root = new File(path.substring(0, path.length() - rel.length()));
		File[] files = new File[1];
		find(root.getParentFile(), rel, files);
		return files[0];
	}

	/**
	 * Searches for a file in a root recursively upwards and one level down every time.
	 */
	private static void find(@Nullable File root, @Nonnull String srcRel, @Nonnull File[] files) {
		if(root == null) {
			return;
		}
		File[] listFiles = root.listFiles();
		if(listFiles != null) {
			for(int i = 0; i < listFiles.length; i++) {
				File child = listFiles[i];
				File src = new File(child, srcRel);
				if(src.exists()) {
					files[0] = src;
					return;
				}
			}
		}
		find(root.getParentFile(), srcRel, files);
	}

	/**
	 * Retrieves a value from an object using introspection. The name is the direct
	 * name of a method that *must* exist; it does not add a "get". If the method
	 * does not exist this throws an exception.
	 */
	static public final Object getClassValue(@Nonnull final Object inst, @Nonnull final String name) throws Exception {
		if(inst == null)
			throw new IllegalStateException("The input object is null");
		Class< ? > clz = inst.getClass();
		Method m;
		try {
			m = clz.getMethod(name);
		} catch(NoSuchMethodException x) {
			throw new IllegalStateException("Unknown method '" + name + "()' on class=" + clz);
		}
		try {
			return m.invoke(inst);
		} catch(IllegalAccessException iax) {
			throw new IllegalStateException("Cannot call method '" + name + "()' on class=" + clz + ": " + iax);
		} catch(InvocationTargetException itx) {
			Throwable c = itx.getCause();
			if(c instanceof Exception)
				throw (Exception) c;
			else if(c instanceof Error)
				throw (Error) c;
			else
				throw itx;
		}
	}

	/**
	 * Since annotations are not inherited, we do the extends search on super classed in order to be able to work also with annotations on inherited properties.
	 */
	@Nullable
	public static <T extends Annotation> T findAnnotationIncludingSuperClasses(@Nonnull Method annotatedMethod, @Nonnull Class<T> annotationType) {
		T annotation = annotatedMethod.getAnnotation(annotationType);
		if(annotation != null) {
			return annotation;
		}
		Class< ? > parent = annotatedMethod.getDeclaringClass().getSuperclass();
		if(parent != null) {
			Method superMethod;
			try {
				superMethod = parent.getDeclaredMethod(annotatedMethod.getName());
				if(superMethod != null) {
					return findAnnotationIncludingSuperClasses(superMethod, annotationType);
				}
			} catch(NoSuchMethodException | SecurityException e) {
				// no method
			}
		}
		return null;
	}

	/**
	 * Get all annotations of a given type on a method or its base methods.
	 */
	@Nonnull
	static public <T extends Annotation> List<T> getMethodAnnotations(Method m, Class<T> annotationType) {
		List<Class<?>> hierarchy = getClassHierarchy(m.getDeclaringClass());			// Full class hierarchy including interfaces

		List<T> result = new ArrayList<>();
		for(Class<?> clz : hierarchy) {
			Method macc = findMethodInClass(m, clz);

			if(macc != null)
				addAnnotationIf(result, annotationType, macc);
		}
		return result;
	}

	@Nullable private static Method findMethodInClass(Method m, Class<?> clz) {
		Method macc;
		if(clz == m.getDeclaringClass()) {
			macc = m;
		} else {
			try {
				macc = clz.getDeclaredMethod(m.getName(), m.getParameterTypes());
			} catch(NoSuchMethodException | SecurityException x) {
				macc = null;
			}
		}
		return macc;
	}

	@Nullable
	static public <T extends Annotation> T getMethodAnnotation(Method m, Class<T> annotationType) {
		T annotation = m.getAnnotation(annotationType);
		if(null != annotation)
			return annotation;

		List<Class<?>> hierarchy = getClassHierarchy(m.getDeclaringClass());			// Full class hierarchy including interfaces
		for(Class<?> clz : hierarchy) {
			Method macc = findMethodInClass(m, clz);

			if(macc != null) {
				annotation = macc.getAnnotation(annotationType);
				if(null != annotation)
					return annotation;
			}
		}
		return null;
	}

	static private <T extends Annotation> void addAnnotationIf(List<T> list, Class<T> annotationType, Method m) {
		T annotation = m.getAnnotation(annotationType);
		if(null != annotation)
			list.add(annotation);
	}

	static public <T extends Annotation> T getClassAnnotation(Class<?> clzIn, Class<T> annotationType) {
		List<Class<?>> hierarchy = getClassHierarchy(clzIn);			// Full class hierarchy including interfaces
		for(Class<?> clz : hierarchy) {
			T annotation = clz.getAnnotation(annotationType);
			if(null != annotation)
				return annotation;

		}
		return null;
	}

}
