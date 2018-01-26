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
package to.etc.domui.converter;

import java.math.*;
import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.util.*;
import to.etc.webapp.query.*;

/**
 * URL Converter class which converts a (compound) primary key into a string and v.v.
 * The converter will follow object references on each (embedded) object in the key until
 * it reaches a renderable value for a property; these are added by walking the object tree
 * by following all properties in alphabetic order.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 15, 2010
 */
public class CompoundKeyConverter {
	static public final CompoundKeyConverter INSTANCE = new CompoundKeyConverter();

	static private class Scanner extends TextScanner {
		private QDataContext m_dc;

		private Class< ? > m_root;

		private String m_input;

		public Scanner(QDataContext dc, Class< ? > root, String s) {
			super(s);
			m_dc = dc;
			m_root = root;
			m_input = s;
		}

		/**
		 * Marshalled string scanner. These are just strings where comma and backslash are escaped.
		 * @param ts
		 * @return
		 */
		public String scanString() {
			clear();
			int c;
			while(!eof()) {
				c = LA();
				if(c == '\\') {
					accept();
					copy();
				} else if(c == ',') {
					accept();
					return getCopied();
				} else
					copy();
			}
			return getCopied();
		}


		/**
		 * Scans the next part at the current location, and returns the object
		 * properly filled in from the input string.
		 * @param ts
		 * @param type
		 * @return
		 */
		public Object scanAnything(Class< ? > type) throws Exception {
			if(isRenderable(type)) {
				//-- Simple type: decode from parameter string.
				if(type == String.class) {
					return scanString();
				} else if(DomUtil.isIntegerOrWrapper(type) || DomUtil.isLongOrWrapper(type) || DomUtil.isDoubleOrWrapper(type) || DomUtil.isFloatOrWrapper(type) || DomUtil.isBooleanOrWrapper(type)) {
					String s = scanString();
					return RuntimeConversions.convertTo(s, type);
				} else
					throw new IllegalStateException("?? Unexpected type in unmarshaller: " + type);
			}

			//-- If the type is a persistent class we need it's PK, then we instantiate a proxied version
			ClassMetaModel cmm = MetaManager.findClassMeta(type);
			if(cmm.isPersistentClass())
				return scanPersistentClass(type, cmm);

			//-- Scan as an object having properties, and assign those properties.
			return scanObject(type, cmm);
		}

		/**
		 * Scan a persistent class by scanning it's PK, then loading a proxied instance.
		 * @param type
		 * @param cmm
		 * @return
		 * @throws Exception
		 */
		private Object scanPersistentClass(Class< ? > type, ClassMetaModel cmm) throws Exception {
			//-- Not renderable: only acceptable item is another persistent class, in which case we need to render it's PK too
			if(!cmm.isPersistentClass())
				throw new IllegalStateException("Unexpected: PK entry is not a persistent class: " + cmm + ", in root PK " + m_root.getClass());

			//-- Obtain it's PK and render it as well
			PropertyMetaModel< ? > pkpm = cmm.getPrimaryKey();
			if(pkpm == null)
				throw new IllegalStateException("Unexpected: persistent class " + cmm + " has an undefined PK property");
			Object pk = scanAnything(pkpm.getActualType());
			if(pk == null)
				throw new IllegalStateException("Primary key " + pkpm + " is null in [[" + m_input + "]]");

			//-- Now load a proxied instance.
			return m_dc.getInstance(cmm.getActualClass(), pk);
		}

		/**
		 * Scan the object type passed by creating an instance, then filling all of its
		 * properties.
		 * @param ts
		 * @param type
		 * @param cmm
		 * @return
		 */
		private Object scanObject(Class< ? > type, ClassMetaModel cmm) throws Exception {
			Object inst;
			try {
				inst = type.newInstance();
			} catch(Exception x) {
				throw new IllegalStateException("Cannot create instance of " + type + " for PK=" + m_root + ": " + x, x);
			}

			for(PropertyMetaModel< ? > pmm : cmm.getProperties()) {
				Object pvalue = scanAnything(pmm.getActualType());
				if(pvalue == null)
					throw new IllegalStateException("Null value for property " + pmm + " in pk " + m_root);
				((IValueAccessor<Object>) pmm).setValue(inst, pvalue);
			}
			return inst;
		}
	}

	/**
	 *
	 * @param pkclass
	 * @param in
	 * @return
	 * @throws UIException
	 */
	public Object unmarshal(QDataContext dc, Class< ? > pkclass, String in) throws Exception {
		if(in.trim().length() == 0)
			return null;
		Scanner ts = new Scanner(dc, pkclass, in);
		return ts.scanAnything(pkclass);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Marshaller part..									*/
	/*--------------------------------------------------------------*/
	/**
	 * Marshalling a PK object into a string.
	 * @param in
	 * @return
	 * @throws UIException
	 */
	public String marshal(Object in) throws Exception {
		if(in == null)
			return "$$null$$";
		StringBuilder sb = new StringBuilder();
		renderAnything(sb, in, in);
		return sb.toString();
	}

	private void renderRenderable(StringBuilder sb, Object pvalue) throws Exception {
		if(sb.length() != 0)
			sb.append(",");
		if(pvalue instanceof String)
			sb.append(((String) pvalue).replace(",", "\\,").replace("\\", "\\\\"));
		else
			sb.append(String.valueOf(pvalue));
	}

	private void renderAnything(StringBuilder sb, Object in, Object root) throws Exception {
		if(in == null)
			throw new IllegalStateException("Unexpected: null value in PK " + root + " of class " + root.getClass());
		Class< ? > clz = in.getClass();

		//-- Primitives are rendered immediately and exited.
		if(isRenderable(clz)) {
			renderRenderable(sb, in);
			return;
		}

		//-- Depending on the type of object.....
		ClassMetaModel cmm = MetaManager.findClassMeta(in.getClass());
		if(cmm.isPersistentClass()) {
			renderPersistentClass(sb, in, root, cmm);
			return;
		}

		//-- Seems to be some other object... Render it.
		renderObject(sb, in, root, cmm);
	}

	private void renderPersistentClass(StringBuilder sb, Object in, Object root, ClassMetaModel cmm) throws Exception {
		//-- Not renderable: only acceptable item is another persistent class, in which case we need to render it's PK too
		if(!cmm.isPersistentClass())
			throw new IllegalStateException("Unexpected: PK entry is not a persistent class: " + cmm + ", in root PK " + root.getClass());

		//-- Obtain it's PK and render it as well
		PropertyMetaModel< ? > pkpm = cmm.getPrimaryKey();
		if(pkpm == null)
			throw new IllegalStateException("Unexpected: persistent class " + cmm + " has an undefined PK property");
		Object pkval = pkpm.getValue(in);
		renderAnything(sb, pkval, root);
	}

	private void renderObject(StringBuilder sb, Object in, Object root, ClassMetaModel cmm) throws Exception {
		for(PropertyMetaModel< ? > pmm : cmm.getProperties()) {
			Object pvalue = pmm.getValue(in);
			renderAnything(sb, pvalue, root);
		}
	}

	/**
	 * FIXME Replace with DomUtil.isBasicType later?
	 * Returns T if the type can be rendered as a PK component.
	 * @param actualType
	 * @return
	 */
	static boolean isRenderable(Class< ? > t) {
		if(t.isPrimitive())
			return true;
		return t == Integer.class || t == Long.class || t == Short.class || t == String.class || t == Byte.class || t == BigDecimal.class || t == BigInteger.class || t == Double.class
			|| t == Float.class
			|| t == Date.class;
	}
}
