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

import java.lang.reflect.*;

import javax.servlet.jsp.el.*;

import to.etc.util.*;

/**
 * A property descriptor class which describes a simple
 * property.
 *
 * Created on May 18, 2005
 * @author jal
 */
public class BeanPropertyDescriptor {
	static final Object[] NO_PARAM = new Object[0];

	String m_name;

	Method m_write_m;

	Method m_read_m;

	/** The type of the setter */
	private Class m_setval_cl;

	//	private Class<? extends PropertyEditor>	m_editor_cl;

	private interface GetInvoker {
		public Object invoke(BeanPropertyDescriptor pd, Object bean) throws Exception;
	}
	private interface SetInvoker {
		public void invoke(BeanPropertyDescriptor pd, Object bean, Object value) throws Exception;
	}

	static private final GetInvoker GET_NORMAL = new GetInvoker() {
		public Object invoke(BeanPropertyDescriptor pd, Object bean) throws Exception {
			return pd.m_read_m.invoke(bean, NO_PARAM);
		}
	};

	static private final GetInvoker GET_PARAM = new GetInvoker() {
		public Object invoke(BeanPropertyDescriptor pd, Object bean) throws Exception {
			return pd.m_read_m.invoke(bean, new Object[]{pd.m_name});
		}
	};

	static private final SetInvoker SET_NORMAL = new SetInvoker() {
		public void invoke(BeanPropertyDescriptor pd, Object bean, Object value) throws Exception {
			//			System.out.println("Calling "+pd.m_write_m+" using a "+bean.getClass()+" of "+bean);
			pd.m_write_m.invoke(bean, new Object[]{value});
		}
	};

	static private final SetInvoker SET_PARAM = new SetInvoker() {
		public void invoke(BeanPropertyDescriptor pd, Object bean, Object value) throws Exception {
			//			System.out.println("Calling "+pd.m_write_m+" using a "+bean.getClass());
			pd.m_write_m.invoke(bean, new Object[]{pd.m_name, value});
		}
	};

	private GetInvoker m_get_i;

	private SetInvoker m_set_i;

	BeanPropertyDescriptor() {}

	public BeanPropertyDescriptor(Method readm, Method writem, Class editorclass, String name) {
		m_name = name;
		//		m_editor_cl	= editorclass;
		if(writem != null)
			setWriteMethod(writem);
		if(readm != null)
			setReadMethod(readm);
		else
			throw new IllegalStateException("Attempt to set null getter method");
	}

	private void setReadMethod(Method m) {
		m_read_m = m;
		Class[] par = m.getParameterTypes();
		if(par.length == 0)
			m_get_i = GET_NORMAL;
		else
			m_get_i = GET_PARAM;
	}

	private void setWriteMethod(Method m) {
		m_write_m = m;
		Class[] par = m.getParameterTypes();
		if(par.length == 1) {
			m_set_i = SET_NORMAL;
			m_setval_cl = par[0];
		} else {
			m_set_i = SET_PARAM;
			m_setval_cl = par[1];
		}
	}

	public String getName() {
		return m_name;
	}

	public Method getWriteMethod() {
		return m_write_m;
	}

	public Method getReadMethod() {
		return m_read_m;
	}

	public boolean isReadOnly() {
		return m_write_m == null;
	}

	public Object callGetter(Object bean) throws Exception {
		try {
			return m_get_i.invoke(this, bean);
		} catch(InvocationTargetException x) {
			if(x.getCause() instanceof Exception)
				throw (Exception) x.getCause();
			else if(x.getCause() instanceof Error)
				throw (Error) x.getCause();
			else
				throw x;
		}
	}

	public void callSetter(Object bean, Object in) throws Exception {
		Exception y = null;
		Object val = null;
		try {
			//-- Convert the object to the setter type using complex conversions
			val = RuntimeConversions.convertToComplex(in, m_setval_cl);

			//			System.out.println("el: setting using method "+m_write_m.toGenericString()+" with a "+(in == null ? "null" : (in+" t="+in.getClass().getName())));
			//			if(in != val)
			//				System.out.println("  : converted to "+val+" t="+val.getClass().getCanonicalName() );
			m_set_i.invoke(this, bean, val);
			return;
		} catch(InvocationTargetException x) {
			// FIXME Replace with more reasonable message describing the class and method and actual parameters causing the error
			if(!(x.getCause() instanceof Exception))
				throw x;
			y = (Exception) x.getCause();
		} catch(Exception x) {
			y = x;
		}

		//-- Handle the exception.
		y.printStackTrace(); // Print because ELException does not fcking nest
		StringBuffer sb = new StringBuffer();
		sb.append("\nEL Set expression failed with ");
		sb.append(y.toString());
		sb.append("\n");
		sb.append("Attempted to call method ");
		sb.append(m_write_m.toGenericString());
		sb.append("\nOn bean ");
		sb.append(bean == null ? "null" : bean.getClass().getName());
		sb.append("\nWith set value ");
		if(val == null)
			sb.append("null");
		else {
			sb.append(val.toString());
			sb.append(" (type=");
			sb.append(val.getClass().getName());
			sb.append(")");
		}

		throw new ELException(sb.toString(), y);
	}

	public Class getSetterType() {
		return m_setval_cl;
	}
}
