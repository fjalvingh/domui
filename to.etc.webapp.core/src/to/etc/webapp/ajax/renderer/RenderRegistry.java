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
package to.etc.webapp.ajax.renderer;

import java.lang.reflect.*;
import java.util.*;


/**
 * This is a registry which contains mappings from Class to ClassRenderer.
 * The data gets built dynamically while objects get rendered.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 6, 2006
 */
abstract public class RenderRegistry {
	private final Map<Class< ? >, ItemRenderer> m_map = new HashMap<Class< ? >, ItemRenderer>();

	static private class FixPair {
		private final Class< ? > m_baseclass;

		private final ItemRenderer m_renderer;

		FixPair(final Class< ? > cl, final ItemRenderer r) {
			m_baseclass = cl;
			m_renderer = r;
		}

		public Class< ? > getBaseclass() {
			return m_baseclass;
		}

		public ItemRenderer getRenderer() {
			return m_renderer;
		}
	}

	private final List<FixPair> m_fixedList = new ArrayList<FixPair>();

	/** The list of method names to IGNORE on an object. */
	private final Set<String> m_ignoredMethodSet = new HashSet<String>();

	/** The list of types that is not to be rendered. */
	private final Set<Class< ? >> m_ignoredTypeSet = new HashSet<Class< ? >>();

	private final Set<String> m_ignoredPackageSet = new HashSet<String>();

	/** Maps primitive types to their array renderer. */
	private final Map<Class< ? >, ItemRenderer> m_arrayRendererMap = new HashMap<Class< ? >, ItemRenderer>();

	/**
	 * Default renderer for all non-primitive arrays
	 */
	static private final ItemRenderer ARRAYRENDERER = new ItemRenderer() {
		@Override
		public void render(final ObjectRenderer or, final Object val) throws Exception {
			Class< ? > dt = val.getClass().getComponentType();
			Object[] l = (Object[]) val;
			or.renderArrayStart(l);
			int ix = 0;
			for(Object o : l) {
				or.renderArrayElement(o, dt, ix++);
			}
			or.renderArrayEnd(l);
		}
	};

	/**
	 * Default renderer for all primitive arrays. This is a VERY expensive
	 * implementation because all elements of the array to render are wrapped
	 * into wrapper objects!! To prevent this you should provide your own
	 * array wrappers by adding them using addArrayRenderer().
	 */
	static private final ItemRenderer PRIMITIVE_ARRAYRENDERER = new ItemRenderer() {
		@Override
		public void render(final ObjectRenderer or, final Object val) throws Exception {
			Class< ? > dt = val.getClass().getComponentType();
			or.renderArrayStart(val);
			int len = Array.getLength(val);
			for(int i = 0; i < len; i++) {
				Object o = Array.get(val, i);
				or.renderArrayElement(o, dt, i);
			}
			or.renderArrayEnd(val);
		}
	};

	/**
	 * Does a lookup for a specialised renderer for an array of
	 * primitives.
	 * @param cl		The primitive component of the array
	 * @return
	 */
	public ItemRenderer makePrimitiveArrayRenderer(final Class< ? > cl) {
		ItemRenderer ir = m_arrayRendererMap.get(cl.getComponentType());
		if(ir != null)
			return ir;
		return PRIMITIVE_ARRAYRENDERER;
	}

	public RenderRegistry() {
		addIgnoredMethod("getClass");
		addIgnoredMethod("hashCode");
		addIgnoredMethod("getCallbacks");
		addIgnoredType(Void.TYPE);
		addIgnoredType(Class.class);
		addIgnoredType(Class[].class);
		addIgnoredPackage("org.hibernate.");

		//-- Register renderers for List, Map and other basal Java classes.
		registerBase(Collection.class, new ItemRenderer() {
			@Override
			public void render(final ObjectRenderer or, final Object val) throws Exception {
				Collection< ? > l = (Collection< ? >) val;
				or.renderArrayStart(l);
				int ix = 0;
				for(Object o : l)
					or.renderArrayElement(o, Object.class, ix++);
				or.renderArrayEnd(l);
			}
		});

		registerBase(Map.class, new ItemRenderer() {
			@Override
			public void render(final ObjectRenderer or, final Object val) throws Exception {
				Map< ? , ? > m = (Map< ? , ? >) val;
				or.renderMapStart(m);
				int size = m.size();
				int count = 0;
				for(Iterator< ? > it = m.entrySet().iterator(); it.hasNext();) {
					Map.Entry< ? , ? > me = (Map.Entry< ? , ? >) it.next();
					or.renderMapEntry(me.getKey(), me.getValue(), count, size);
					count++;
				}
				or.renderMapEnd(m);
			}
		});
	}

	public synchronized ItemRenderer findRenderer(final Class< ? > cl) {
		return m_map.get(cl);
	}

	public synchronized void register(final Class< ? > cl, final ItemRenderer r) {
		m_map.put(cl, r);
	}

	public synchronized void registerBase(final Class< ? > bc, final ItemRenderer r) {
		m_fixedList.add(new FixPair(bc, r));
	}

	public synchronized void addIgnoredMethod(final String name) {
		m_ignoredMethodSet.add(name);
	}

	public synchronized void addIgnoredPackage(final String name) {
		m_ignoredPackageSet.add(name);
	}

	public synchronized void addIgnoredType(final Class< ? > cl) {
		m_ignoredTypeSet.add(cl);
	}

	public synchronized void addArrayRenderer(final Class< ? > primitive, final ItemRenderer r) {
		m_arrayRendererMap.put(primitive, r);
	}

	public synchronized boolean isIgnoredType(final Class< ? > c) {
		for(Class< ? > tcl : m_ignoredTypeSet) {
			if(tcl.isAssignableFrom(c))
				return true;
		}
		String cn = c.getName();
		for(String ign : m_ignoredPackageSet) {
			if(cn.startsWith(ign))
				return true;
		}
		return false;
	}

	//	@SuppressWarnings("unchecked")
	private synchronized ItemRenderer findFixed(final Class< ? > bc) {
		for(FixPair p : m_fixedList) {
			if(p.getBaseclass().isAssignableFrom(bc))
				return p.getRenderer();
		}
		return null;
	}

	public synchronized ItemRenderer makeRenderer(final Class< ? > cl) {
		ItemRenderer ir = m_map.get(cl);
		if(ir != null) {
			//			if(cl != null)
			//				System.out.println("Re-using existing class renderer "+ir+" to render "+cl.getName());
			return ir;
		}
		if(cl.isArray()) {
			if(!cl.getComponentType().isPrimitive())
				return ARRAYRENDERER;
			return makePrimitiveArrayRenderer(cl);
		}

		//-- Try any fixed pair
		ir = findFixed(cl);
		if(ir == null)
			ir = makeClassRenderer(cl);
		m_map.put(cl, ir);
		//		System.out.println("Created new class renderer "+ir+" to render "+cl.getName());
		return ir;
	}

	/**
	 * This introspects a class' definition and creates an ItemRenderer which
	 * decodes the class. The ItemRenderer consists of a renderer which renders
	 * a list. We accept all methods that are getters, defined as public parameterless
	 * methods with a name starting with 'get'.
	 * @param cl
	 * @return
	 */
	protected ItemRenderer makeClassRenderer(final Class< ? > cl) {
		List<ClassMemberRenderer> ml = new ArrayList<ClassMemberRenderer>();
		Method[] ar = cl.getMethods();
		for(Method m : ar) {
			if(m.isAnnotationPresent(Unrenderable.class))
				continue;
			int mod = m.getModifiers();
			if(Modifier.isPublic(mod) && !Modifier.isStatic(mod) && m.getParameterTypes().length == 0) {
				String name = m.getName();
				if(!m_ignoredMethodSet.contains(name)) {
					int nlen = name.length();
					int slen = 0;
					if(nlen > 3 && name.startsWith("get"))
						slen = 3;
					else if(nlen > 3 && name.startsWith("has"))
						slen = 3;
					else if(nlen > 2 && name.startsWith("is"))
						slen = 2;
					else
						continue;
					Class< ? > rt = m.getReturnType();
					if(!isIgnoredType(rt)) {

						//-- Something real. Form the actual name
						StringBuilder sb = new StringBuilder();
						sb.append(m.getName(), slen, m.getName().length());
						sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
						ml.add(new ClassMemberRenderer(m, sb.toString()));
					}
				}
			}
		}

		ClassRenderer cr = new ClassRenderer(ml.toArray(new ClassMemberRenderer[ml.size()]));
		return cr;
	}
}
