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
package to.etc.el.node;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import javax.servlet.jsp.el.*;

import to.etc.el.*;
import to.etc.javabean.*;
import to.etc.util.*;

public class NdLookup extends NdBase {
	/** The object that we need to find the item in */
	private NdBase m_object;

	/** The value to lookup with */
	private NdBase m_key;

	public NdLookup(NdBase obj, NdBase key) {
		m_object = obj;
		m_key = key;
	}

	public NdBase getBase() {
		return m_object;
	}

	public NdBase getKey() {
		return m_key;
	}

	/**
	 * @see to.etc.el.node.NdBase#dump(to.etc.el.node.IndentWriter)
	 */
	@Override
	public void dump(IndentWriter w) throws IOException {
		w.println(getNodeName());
		w.print("Base=");
		w.inc();
		m_object.dump(w);
		w.dec();
		w.forceNewline();
		w.print("key=");
		w.inc();
		m_key.dump(w);
		w.dec();
		w.forceNewline();
	}

	/**
	 * Handle the map operation as per [jsp 2.0] JSP 2.3.4
	 * @see to.etc.el.node.NdBase#evaluate(javax.servlet.jsp.el.VariableResolver, javax.servlet.jsp.el.FunctionMapper)
	 */
	@Override
	public Object evaluate(VariableResolver vr) throws ELException {
		//-- If either expr-a or expr-b is null then return null
		Object a = m_object.evaluate(vr);
		if(a == null)
			return null;
		Object b = m_key.evaluate(vr);
		if(b == null)
			return null;

		/*
		 * If a is a map try to find an item using a lookup; return
		 * null if not found.
		 */
		if(a instanceof Map)
			return ((Map) a).get(b);

		/*
		 * For List and Array, coerce b to integer, then get the value.
		 */
		if(a instanceof List) {
			long ix = ElUtil.coerceToLong(b);
			List l = (List) a;
			if(ix >= l.size() || ix < 0) // No index out of bounds allowed: return null instead
				return null;
			return l.get((int) ix);
		}
		Class cla = a.getClass();
		if(cla.isArray()) {
			long ix = ElUtil.coerceToLong(b);
			int al = Array.getLength(a);
			if(ix >= al || ix < 0) // No index out of bounds allowed: return null instead
				return null;
			return Array.get(a, (int) ix); // Return the array value
		}

		//-- We must do a bean lookup.. Coerce to string
		String v = ElUtil.coerceToString(b); // Coerce to string
		if(v.length() == 0)
			throw new ELException("The key for a Javabean lookup in object " + a + " is empty");

		//-- Get a descriptor for this
		BeanPropertyDescriptor pd = BeanEvaluator.findProperty(cla, v);
		if(pd == null)
			throw new ELPropertyException(cla, v);

		//-- We need to evaluate using a getter.
		try {
			return pd.callGetter(a);
		} catch(ELException x) {
			throw x;
		} catch(RuntimeException x) {
			throw x;
		} catch(Exception x) {
			throw new ELException(x.toString(), x);
		}
	}

	@Override
	public void getExpression(Appendable a) throws IOException {
		m_object.getExpression(a);
		a.append('.');
		m_key.getExpression(a);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Utility functions.									*/
	/*--------------------------------------------------------------*/
	//	/**
	//	 * Returns null if the base expression at least resolves to something
	//	 * that we can get properties from, else it returns the property name.
	//	 */
	//	private String	isPropertyable(VariableResolver vr) throws ELException
	//	{
	//		//-- Get both the key and the base; if either is null we're readonly.
	//		Object	a	= m_object.evaluate(vr);
	//		if(a == null)
	//			return null;
	//		Object b = m_key.evaluate(vr);
	//		if(b == null)
	//			return null;
	//
	//		/*
	//		 * If the object is a list, map or array then it cannot be set.
	//		 */
	//		if(a instanceof Map || a instanceof List || a.getClass().isArray())
	//			return null;
	//
	//		//-- This IS a bean property lookup. Find the descriptor
	//		String	v = ElUtil.coerceToString(b);	// Coerce to string
	//		if(v.length() == 0)
	//			throw new ELException("The key for a Javabean lookup in object "+a+" is empty");
	//		return v;
	//	}
	/**
	 * Determines if the target of this thingy is writeable. To know
	 * this we need to evaluate the base expression to an object. Then,
	 * if the object is a map, list or array we try to assign to the appropriate
	 * item. If not we try to find a setter in the object.
	 */
	public boolean isReadOnly(VariableResolver vr) throws ELException {
		//-- Get both the key and the base; if either is null we're readonly.
		Object a = m_object.evaluate(vr);
		if(a == null)
			return true;
		Object b = m_key.evaluate(vr);
		if(b == null)
			return true;

		/*
		 * If the object is a list, map or array then we can set it,
		 */
		if(a instanceof Map || a instanceof List)
			return false; // 20050803 jal allow maps, arrays and lists as lvalues
		Class cla = a.getClass();
		if(cla.isArray())
			return false; // 20050803 jal allow maps, arrays and lists as lvalues

		//-- This IS a bean property lookup. Find the descriptor
		String v = ElUtil.coerceToString(b); // Coerce to string
		if(v.length() == 0)
			throw new ELException("The key for a Javabean lookup in object " + a + " is empty");

		//-- Get a descriptor for this
		BeanPropertyDescriptor pd = BeanEvaluator.findProperty(cla, v);
		if(pd == null)
			throw new ELPropertyException(cla, v);

		//-- If the thingy has a writer..
		return pd.isReadOnly();
	}

	/**
	 * Determines the type of this expression as good as it gets.
	 * @param vr
	 * @return
	 * @throws ELException
	 */
	public Class getType(VariableResolver vr) throws ELException {
		//-- Get both the key and the base; if either is null we're readonly.
		Object a = m_object.evaluate(vr);
		if(a == null)
			return null; // If empty expression use null
		Object b = m_key.evaluate(vr);
		if(b == null)
			return null;

		/*
		 * If a is a map try to find an item using a lookup; return
		 * null if not found.
		 */
		if(a instanceof Map) {
			//-- Can we get the thingy?
			Object v = ((Map) a).get(b);
			if(v == null)
				return null;
			return v.getClass(); // Return the type,
		}

		/*
		 * For List and Array, coerce b to integer, then get the value.
		 */
		if(a instanceof List) {
			long ix = ElUtil.coerceToLong(b);
			List l = (List) a;
			if(ix >= l.size() || ix < 0) // No index out of bounds allowed: return null instead
				return null;
			Object v = l.get((int) ix);
			if(v == null)
				return null;
			return v.getClass();
		}

		//-- Check for array,
		Class cla = a.getClass();
		if(cla.isArray())
			return cla.getComponentType(); // For arrays the type IS the component type.

		//-- We must do a bean lookup.. Coerce to string
		String v = ElUtil.coerceToString(b); // Coerce to string
		if(v.length() == 0)
			throw new ELException("The key for a Javabean lookup in object " + a + " is empty");

		//-- Get a descriptor for this
		BeanPropertyDescriptor pd = BeanEvaluator.findProperty(cla, v);
		if(pd == null)
			throw new ELPropertyException(cla, v);

		return pd.getSetterType();
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Setter code.										*/
	/*--------------------------------------------------------------*/
	private void setMapValue(Map m, Object key, Object value) throws Exception {
		/*
		 * Since the morons that designed java generics designed a
		 * stupid mechanism (again) we cannot get type information
		 * on parameterized objects at all. Fuck. So we just put
		 * the objects in the map and hope for the fucking best.
		 * What an idiots. I think they THEMSELVES like to make
		 * .net c# win.
		 */
		m.put(key, value);
	}

	private void setListValue(List m, Object key, Object value) throws Exception {
		//-- For this to work the key must be coercable to a numeric value
		int i = RuntimeConversions.convertToInt(key);
		if(m.size() == i)
			m.add(value);
		else if(i < m.size() && i >= 0)
			m.set(i, value);
		else
			throw new ELException("List index out of range: index is " + i + ", the list size is " + m.size());
	}

	private void setArrayValue(Class acl, Object ar, Object key, Object value) throws Exception {
		//-- Coerce the value to whatever type the array expects.
		Class cl = acl.getComponentType();
		Object nv = RuntimeConversions.convertTo(value, cl); // Convert to requested type

		//-- For this to work the key must be coercable to a numeric value
		int i = RuntimeConversions.convertToInt(key);
		int arl = Array.getLength(ar);
		if(i >= arl || i < 0)
			throw new ELException("Array index out of range: index is " + i + ", the array size is " + arl);
		Array.set(ar, i, nv);
	}

	/**
	 * Set the value for a property.
	 * @param vr
	 * @param value
	 */
	public void setValue(VariableResolver vr, Object value) throws Exception {
		//-- Get both the key and the base; if either is null we're readonly.
		Object a = m_object.evaluate(vr);
		if(a == null)
			throw new ELException("The expression " + m_object.getExpression() + " returns null: cannot set a value");
		Object b = m_key.evaluate(vr);
		if(b == null)
			throw new ELException("The expression " + m_key.getExpression() + " returns null: cannot set a value");

		/*
		 * Handle setting maps, lists and arrays.
		 */
		if(a instanceof Map) {
			setMapValue((Map) a, b, value);
			return;
		}
		if(a instanceof List) {
			setListValue((List) a, b, value);
			return;
		}
		Class cla = a.getClass();
		if(cla.isArray()) {
			setArrayValue(cla, a, b, value);
			return;
		}

		//-- This IS a bean property lookup. Find the descriptor
		String v = ElUtil.coerceToString(b); // Coerce to string
		if(v.length() == 0)
			throw new ELException("The key for a Javabean lookup in object " + a + " is empty");

		//-- Get a descriptor for this
		BeanPropertyDescriptor pd = BeanEvaluator.findProperty(cla, v);
		if(pd == null)
			throw new ELPropertyException(cla, v);

		if(pd.isReadOnly())
			throw new ELException("The expression '" + getExpression() + "' is not writable");
		pd.callSetter(a, value);
	}

	public MethodInvocator getMethodInvocator(VariableResolver vr) throws ELException {
		//-- Get both the key and the base; if either is null we're readonly.
		Object a = m_object.evaluate(vr);
		if(a == null)
			throw new ELException("The expression " + m_object.getExpression() + " returns null: cannot set a value");
		Object b = m_key.evaluate(vr);
		if(b == null)
			throw new ELException("The expression " + m_key.getExpression() + " returns null: cannot set a value");

		/*
		 * If the object is a list, map or array then it cannot be set.
		 */
		if(a instanceof Map || a instanceof List)
			throw new ELException("The expression " + m_object.getExpression() + " does not return a valid bean type");
		Class cla = a.getClass();
		if(cla.isArray())
			throw new ELException("The expression " + m_object.getExpression() + " does not return a valid bean type");

		//-- This IS a bean property lookup. Find the descriptor
		String v = ElUtil.coerceToString(b); // Coerce to string
		if(v.length() == 0)
			throw new ELException("The key for a Javabean lookup in object " + a + " is empty");

		//-- Now get a single unique method for this thingy to call
		Method[] mar = cla.getMethods();
		Method sel = null;
		for(int i = mar.length; --i >= 0;) {
			Method m = mar[i];
			if(Modifier.isPublic(m.getModifiers())) {
				if(m.getName().equals(v)) {
					if(sel != null)
						throw new ELException("The expression '" + getExpression() + "' resolves to two methods: " + sel + " and " + m);
					sel = m;
				}
			}
		}
		if(sel == null)
			throw new ELException("The expression '" + getExpression() + "' does not resolve to a public method called '" + v + "' in class " + cla.getName());

		return new MethodInvocatorImpl(a, sel);
	}
}
