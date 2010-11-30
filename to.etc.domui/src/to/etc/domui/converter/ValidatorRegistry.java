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

import java.util.*;

/**
 * Stuff to handle validation.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 24, 2008
 */
public class ValidatorRegistry {
	static private Map<Class< ? extends IValueValidator< ? >>, IValueValidator< ? >> m_validatorMap = new HashMap<Class< ? extends IValueValidator< ? >>, IValueValidator< ? >>();

	/**
	 * Retrieves a validator instance.
	 * @param <T>
	 * @param clz
	 * @return
	 */
	static synchronized public <T> IValueValidator<T> getValueValidator(Class< ? extends IValueValidator<T>> clz, String[] parameters) {
		try {
			if(parameters != null && parameters.length > 0) {
				//-- Uncacheable.
				IValueValidator<T> v = clz.newInstance();
				if(!(v instanceof IParameterizedValidator< ? >))
					throw new IllegalStateException("The validator " + clz + " does not accept parameters");
				((IParameterizedValidator<T>) v).setParameters(parameters);
				return v;
			}

			IValueValidator<T> v = (IValueValidator<T>) m_validatorMap.get(clz);
			if(v == null) {
				v = clz.newInstance();
				m_validatorMap.put(clz, v);
			}
			return v;
		} catch(Exception x) {
			throw new IllegalStateException("Cannot instantiate IValueValidator " + clz + ": " + x, x);
		}
	}

	//	static public <T> void validate(T object, Class< ? extends IValueValidator<T>> clz, Object[] parameters) throws Exception {
	//		IValueValidator<T> v = getValueValidator(clz);
	//		v.validate(object, parameters);
	//	}
	//
	//	static public <T> void validate(T object, Class< ? extends IValueValidator<T>>[] clzar) throws Exception {
	//		for(Class< ? extends IValueValidator<T>> clz : clzar) {
	//			IValueValidator<T> v = getValueValidator(clz);
	//			v.validate(object, NOPE);
	//		}
	//	}
	//
	//	static public <T> void validate(T object, PropertyMetaValidator[] valar) throws Exception {
	//		for(PropertyMetaValidator pv : valar) {
	//			IValueValidator<T> va = getValueValidator((Class< ? extends IValueValidator<T>>) pv.getValidatorClass());
	//			va.validate(object, pv.getParameters());
	//		}
	//	}
	//
	//	static public <T> void validate(T object, List<PropertyMetaValidator> valar) throws Exception {
	//		for(PropertyMetaValidator pv : valar) {
	//			IValueValidator<T> va = getValueValidator((Class< ? extends IValueValidator<T>>) pv.getValidatorClass());
	//			va.validate(object, pv.getParameters());
	//		}
	//	}
}
