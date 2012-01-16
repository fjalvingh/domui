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
package to.etc.javabean;

import java.beans.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * Javabean method resolver for common Javabean methods. This tries
 * to resolve bean access paths as fast as possible and can cache
 * the results for fast lookup. Caching is the default; call the
 * uncached functions for -ah- uncached results.
 *
 * @author jal
 * Created on May 18, 2005
 */
public class BeanEvaluator {
	static private final Class< ? >[] NO_PARAMS = new Class< ? >[0];

	static private final Class< ? >[] PARAM_1_STRING = new Class[]{String.class};

	static private final Class< ? >[] PARAM_2_STR_OBJ = new Class[]{String.class, Object.class};

	/** Signalling instance. */
	static private final BeanPropertyDescriptor NO_SUCH_PROP = new BeanPropertyDescriptor();

	private BeanEvaluator() {}

	static private class PropRef {
		/** The class this is a property reference for */
		Class m_cl;

		/** The property name */
		String m_propname;

		int m_hash;

		/** The cached descriptor. */
		BeanPropertyDescriptor m_pd;

		/** The next ref in the bucket. */
		PropRef m_next;

		public PropRef() {}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Simple helper functions.							*/
	/*--------------------------------------------------------------*/
	/**
	 * Tries to locate a BeanInfo class.
	 * @param cl
	 * @return
	 */
	static private BeanInfo findBeanInfoFor(Class cl) {
		String biname = cl.getName() + "BeanInfo";
		try {
			Class bcl = Class.forName(biname);
			Object o = bcl.newInstance();
			if(o instanceof BeanInfo)
				return (BeanInfo) o;
		} catch(Exception x) {}
		return null;
	}

	static private Method findMethod(Class cl, String name, Class[] args) {
		try {
			return cl.getMethod(name, args);
		} catch(Exception x) {
			return null;
		}
	}

	static public String makeMethodName(String prefix, String property) {
		if(property.length() == 0)
			throw new IllegalStateException("Empty property name");
		StringBuffer sb = new StringBuffer(prefix.length() + property.length());
		sb.append(prefix);
		sb.append(Character.toUpperCase(property.charAt(0)));
		sb.append(property, 1, property.length());
		return sb.toString();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Uncached bean introspectors.						*/
	/*--------------------------------------------------------------*/
	/**
	 * This recursively walks all BeanInfo's for a class to find a given
	 * property.
	 * @param bi
	 * @param name
	 * @return
	 */
	static private BeanPropertyDescriptor findPropertyInBeanInfo(BeanInfo bi, String name) {
		PropertyDescriptor[] par = bi.getPropertyDescriptors();
		for(int i = par.length; --i >= 0;) {
			PropertyDescriptor pd = par[i];
			if(pd.getName().equals(name)) {
				//-- Found a match!
				return new BeanPropertyDescriptor(pd.getReadMethod(), pd.getWriteMethod(), pd.getPropertyEditorClass(), name);
			}
		}

		//-- Not found in this bean. Walk all derived beans,
		for(BeanInfo baba : bi.getAdditionalBeanInfo()) {
			BeanPropertyDescriptor p = findPropertyInBeanInfo(baba, name);
			if(p != null)
				return p;
		}
		return null;
	}

	/**
	 * Returns a property descriptor for the specified bean class. This uses an uncached
	 * variant. It obeys the Java bean access rules: it starts to get info using a BeanInfo
	 * class and falls back to generic introspection if that fails.
	 *
	 * @param beancl
	 * @param name
	 * @return
	 */
	static BeanPropertyDescriptor uncachedGetPropertyDescriptor(Class beancl, String name) {
		//-- 1. Does the class have a BeanInfo available?
		BeanInfo bi = findBeanInfoFor(beancl);
		if(bi != null)
			return findPropertyInBeanInfo(bi, name);

		/*
		 * 2. Thank ghodt, there is no BeanInfo. Let's find it out by ourselves. We
		 * walk all methods to find both the best getter and all setters for the simple
		 * property. We do this all at once to prevent is from having to scan multiple
		 * times.
		 *
		 * The getters all must start with get; all setters must start with set (rule 1).
		 * The best getter is a normal method specifying the normal name (getXxxx). If no
		 * such method is found it tests to see if DynamicBean is implemented and checks
		 * the methods from that. If DynaBean also does not exist it checks for a generic
		 * get(String) method.
		 */
		String getname = makeMethodName("get", name);
		String isname = makeMethodName("is", name);
		Method getm = findMethod(beancl, getname, NO_PARAMS);
		if(getm == null)
			getm = findMethod(beancl, isname, NO_PARAMS);
		if(getm == null) {
			if(DynamicBean.class.isAssignableFrom(beancl)) // Bean class implements Dynamic beans?
			{
				/*
				 * If the dynabean
				 */
				getm = findMethod(beancl, "getDynamicProperty", PARAM_1_STRING);
				Method setm = findMethod(beancl, "setDynamicProperty", PARAM_2_STR_OBJ);
				if(getm == null || setm == null)
					throw new IllegalStateException("!?");
				return new BeanPropertyDescriptor(getm, setm, null, name);
			}
		}
		if(getm == null) {
			//-- NEMA Extension: allow "generic get(String)" as a getter. In that case we also use a generic setter.
			getm = findMethod(beancl, "get", PARAM_1_STRING);
			Method setm = findMethod(beancl, "set", PARAM_2_STR_OBJ);
			if(getm != null)
				return new BeanPropertyDescriptor(getm, setm, null, name);
		}
		if(getm == null) {
			/*
			 * If there's no getter now then the property does not exist.
			 */
			return null;
		}
		Class proptype = getm.getReturnType();

		/*
		 * Find a setter. We try to find a setter with a parameter type
		 * that is the same as the getter.
		 */
		String setname = makeMethodName("set", name);
		List<Method> setal = new ArrayList<Method>(2);
		Method defsetter = null;

		Method[] mar = beancl.getMethods(); // Get all methods,
		for(Method m : mar) {
			if(Modifier.isPublic(m.getModifiers())) // Only accept public dudes
			{
				if(m.getName().equals(setname)) {
					//-- Name is correct. How about the arguments?
					Class[] par = m.getParameterTypes();
					if(par.length == 1) {
						//-- Is the parameter value assignment-compatible with the setter?
						if(par[0].isAssignableFrom(proptype))
							defsetter = m;
						else {
							setal.add(m);
						}
					}
				}
			}
		}

		if(defsetter != null)
			return new BeanPropertyDescriptor(getm, defsetter, null, name);

		//-- If no setter was found the object is read-only.
		//		if(defsetter == null && setal.size() == 0)
		return new BeanPropertyDescriptor(getm, null, null, name);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Fast cache implementation.							*/
	/*--------------------------------------------------------------*/
	/**
	 * The bucket table for the hash implementation.
	 */
	static private PropRef[] m_hash_ar = new PropRef[511];

	/** #of cached items. */
	static private int m_size;

	/**
	 * Gets or creates a hashing item.
	 * @param cl
	 * @param propname
	 * @param m
	 */
	static private synchronized void put(Class cl, String propname, BeanPropertyDescriptor pd) {
		propname = propname.intern();
		int hash = ((cl.hashCode() + 4 << propname.hashCode()) & 0x7fffffff);
		int bucket = hash % m_hash_ar.length;

		//-- Can we find the thingy?
		for(PropRef r = m_hash_ar[bucket]; r != null; r = r.m_next) {
			if(r.m_hash == hash) {
				if(r.m_cl == cl && r.m_propname == propname) {
					//-- Item found. Replace data
					r.m_pd = pd;
					return;
				}
			}
		}

		//-- Add the item
		PropRef r = new PropRef();
		r.m_next = m_hash_ar[bucket];
		m_hash_ar[bucket] = r;
		r.m_cl = cl;
		r.m_hash = hash;
		r.m_pd = pd;
		r.m_propname = propname;
		m_size++;

		//-- Do we need to rebalance?
		if(m_size / 3 > m_hash_ar.length)
			rebalance();
	}

	static private synchronized BeanPropertyDescriptor find(Class cl, String propname) {
		propname = propname.intern();
		int hash = ((cl.hashCode() + 4 << propname.hashCode()) & 0x7fffffff);
		int bucket = hash % m_hash_ar.length;

		//-- Can we find the thingy?
		for(PropRef r = m_hash_ar[bucket]; r != null; r = r.m_next) {
			if(r.m_hash == hash) {
				if(r.m_cl == cl && r.m_propname == propname) {
					return r.m_pd;
				}
			}
		}
		return null;
	}

	static synchronized private final void rebalance() {
		//-- Find the first higher power of 2 for the table size
		int size = 512;
		int cursz = m_hash_ar.length + 1; // For ease.
		while(size <= cursz) {
			size <<= 2;
		}
		size--;

		PropRef[] ar = new PropRef[size]; // Create the new table

		//-- Now walk the old table and reassign all to the new table,
		for(int i = m_hash_ar.length; --i >= 0;) // All buckets,
		{
			PropRef r = m_hash_ar[i];
			while(r != null) {
				PropRef next = r.m_next; // Get current next

				//-- Link into chain of new array
				int bucket = r.m_hash % size; // Get new bucket,
				r.m_next = ar[bucket];
				ar[bucket] = r; // Link in
				r = next; // And move forward
			}
		}
		m_hash_ar = ar;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Cached property retrieval.							*/
	/*--------------------------------------------------------------*/
	/**
	 * Get a property descriptor for a bean. Uses the cache to quickly
	 * locate such a beast.
	 */
	static public BeanPropertyDescriptor findProperty(Class cl, String propname) {
		/*
		 * The cache is racey to prevent long lock times. This may mean that >1 task
		 * registers a descriptor; the last one wins ;-)
		 */
		propname = propname.intern();
		BeanPropertyDescriptor pd = find(cl, propname);
		if(pd != null)
			return pd == NO_SUCH_PROP ? null : pd;

		//-- Calculate a descriptor
		pd = uncachedGetPropertyDescriptor(cl, propname);
		if(pd == null) {
			//-- Save a signalling instance to cache "not found" too
			put(cl, propname, NO_SUCH_PROP); // Cache "not found" indicator
			return null; // No such property.
		}
		put(cl, propname, pd); // Cache descriptor
		return pd;
	}


}
