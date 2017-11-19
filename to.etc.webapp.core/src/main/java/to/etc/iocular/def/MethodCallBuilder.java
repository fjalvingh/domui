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
package to.etc.iocular.def;

import to.etc.iocular.ioccontainer.FailedAlternative;
import to.etc.iocular.ioccontainer.MethodInvoker;
import to.etc.iocular.util.ClassUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Some kind of method or constructor call builder.
 *
 * @author jal
 * Created on Apr 22, 2007
 */
public class MethodCallBuilder {
	private final ComponentBuilder m_component;

	private final Class< ? > m_baseClass;

	private final String m_methodName;

	private enum ParamMode {
		UNKNOWN, NUMBERED, UNNUMBERED
	}

	private ParamMode m_paramMode = ParamMode.UNKNOWN;

	private boolean m_thisIsSelf;

	/**
	 * The defined parameters for this method as set by the builder. The order is undefined. If parameters are set by number
	 * isNumbered() is true; in that case each parameter's number has an assignment.
	 *
	 */
	private final List<MethodParameterSpec> m_actuals = new ArrayList<MethodParameterSpec>();

	//	private List<Class<?>>			m_formals;

	//	private ParameterDef[]			m_paramDefs;

	private boolean m_staticOnly;

	//	/**
	//	 * When set this locates the method that explicitly matches the formals
	//	 * specified; when true all formals must be specified and an exact match
	//	 * must be found.
	//	 */
	//	private boolean					m_explicit;

	public MethodCallBuilder(final ComponentBuilder component, final Class< ? > baseClass, final String methodName, final Class< ? >[] actuals, final boolean staticOnly) {
		m_component = component;
		m_baseClass = baseClass;
		m_methodName = methodName;
		m_staticOnly = staticOnly;
		if(actuals != null) {
			for(Class< ? > ac : actuals) {
				setParameter(ac);
			}
		}
	}

	public MethodCallBuilder(final ComponentBuilder component, final Class< ? > baseClass, final String methodName) {
		m_component = component;
		m_baseClass = baseClass;
		m_methodName = methodName;
	}

	public void setStaticOnly(final boolean staticOnly) {
		m_staticOnly = staticOnly;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Method parameter definition.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Return a def for a numbered parameter. This contains the check for numbered/unnumbered parameter matching.
	 * @param ix
	 * @return
	 */
	private MethodParameterSpec makeNumberedParam(final int ix) {
		switch(m_paramMode){
			default:
				throw new IllegalStateException("Unexpected parameter mode " + m_paramMode);
			case UNKNOWN:
				m_paramMode = ParamMode.NUMBERED;
				break;
			case NUMBERED:
				break;
			case UNNUMBERED:
				throw new IocConfigurationException(m_component, "You cannot mix NUMBERED and UNNUMBERED parameters.");
		}
		for(MethodParameterSpec msp : m_actuals) {
			if(msp.getParameterNumber() == ix) {
				throw new IocConfigurationException(m_component, "Numbered parameter " + ix + " is already defined.");
			}
		}
		MethodParameterSpec msp = new MethodParameterSpec();
		m_actuals.add(msp);
		msp.setParameterNumber(ix);
		return msp;
	}

	/**
	 * Return a def for an unnumbered parameter. This contains the check for numbered/unnumbered parameter matching.
	 *
	 * @return
	 */
	private MethodParameterSpec makeUnnumberedParam() {
		switch(m_paramMode){
			default:
				throw new IllegalStateException("Unexpected parameter mode " + m_paramMode);
			case UNKNOWN:
				m_paramMode = ParamMode.UNNUMBERED;
				break;
			case UNNUMBERED:
				break;
			case NUMBERED:
				throw new IocConfigurationException(m_component, "You cannot mix NUMBERED and UNNUMBERED parameters.");
		}
		MethodParameterSpec msp = new MethodParameterSpec();
		msp.setParameterNumber(m_actuals.size());
		m_actuals.add(msp);
		return msp;
	}

	/**
	 * Set a numbered parameter from a container object identified by the specified type.
	 *
	 * @param index
	 * @param type
	 */
	public void setParameter(final int index, final Class< ? > type) {
		MethodParameterSpec msp = makeNumberedParam(index);
		msp.setSourceType(type);
	}

	/**
	 * Set a numbered parameter from a container object identified by the specified name.
	 * @param index
	 * @param name
	 */
	public void setParameter(final int index, final String name) {
		MethodParameterSpec msp = makeNumberedParam(index);
		msp.setSourceName(name);
	}

	/**
	 * Define a numbered parameter as the actual object being built by the current definition.
	 * @param index
	 */
	public void setParameterSelf(final int index) {
		MethodParameterSpec msp = makeNumberedParam(index);
		msp.setSelf(true);
	}

	/**
	 * Set an unnumbered/unordered parameter from a container object identified by the specified type.
	 * @param type
	 */
	public void setParameter(final Class< ? > type) {
		MethodParameterSpec msp = makeUnnumberedParam();
		msp.setSourceType(type);
	}

	/**
	 * Set an unnumbered/unordered parameter from a container object identified by the specified name.
	 * @param name
	 */
	public void setParameter(final String name) {
		MethodParameterSpec msp = makeUnnumberedParam();
		msp.setSourceName(name);
	}

	/**
	 * Set an unnumbered/unordered parameter from the actual object being built by the current definition.
	 */
	public void setParameterSelf() {
		MethodParameterSpec msp = makeUnnumberedParam();
		msp.setSelf(true);
	}

	/**
	 * Create an unique invoker for this method. The invoker encapsulates the method
	 * to call plus references to all parameters for the method as obtained from
	 * a container.
	 *
	 * @param self
	 * @param stack
	 * @return
	 */
	public MethodInvoker createInvoker(final ISelfDef self, final Stack<ComponentBuilder> stack) {
		List<Method> mlist = getAcceptableMethods();
		if(mlist.size() == 0)
			throw new IocConfigurationException(m_component, "Cannot find an acceptable method '" + m_methodName + " on " + m_baseClass);
		//		if(mlist.size() > 1 && m_explicit)			// jal 20090525 Should be resolved in resolution pass below.
		//			throw new IllegalStateException("internal: no unique method for explicit method found.");

		/*
		 * Try to create invokers for all applicable methods, then keep the best one.
		 */
		List<FailedAlternative> aflist = new ArrayList<FailedAlternative>();
		MethodInvoker best = null;
		for(Method m : mlist) {
			MethodInvoker miv = tryToMakeAnInvokerIfYouWouldBeSoKind(self, stack, m, aflist);
			if(miv != null) {
				//-- We can invoke this one. Is it the best choice so far?
				if(best == null || best.getScore() < miv.getScore())
					best = miv;
			}
		}
		if(best == null)
			throw new BuildPlanFailedException(m_component, "Can't call any of the available methods", aflist);

		return best;
	}

	/**
	 * Tries to make an invoker for the method passed by creating refs for all arguments; if
	 * succesful this returns the thingy with a score based on the #of parameters provided. The
	 * thingy with the highest score wins.
	 * @param self
	 *
	 * @param stack
	 * @param m
	 * @param aflist
	 * @return
	 */
	private MethodInvoker tryToMakeAnInvokerIfYouWouldBeSoKind(final ISelfDef self, final Stack<ComponentBuilder> stack, final Method m, final List<FailedAlternative> aflist) {
		Class< ? >[] fpar = m.getParameterTypes();
		Annotation[][] pannar = m.getParameterAnnotations();
		ComponentRef[] refar = new ComponentRef[fpar.length];

		/*
		 * Must find an acceptable match for each formal parameter.
		 */
		if(m_paramMode == ParamMode.NUMBERED || m_paramMode == ParamMode.UNKNOWN) {
			//-- Each method parameter *must* correspond with the exact defintion at that index.
			if(m_actuals.size() != fpar.length) // If parameter counts mismatch we do not use this
				return null;
			for(int i = 0; i < fpar.length; i++) {
				MethodParameterSpec msp = m_actuals.get(i); // Actual parameter spec: this MUST match the specified parameter;
				if(msp == null)
					return null;
				Class< ? > fp = fpar[i];

				//-- Find a component reference for the specified parameter def
				ComponentRef cr = m_component.getBuilder().findReferenceFor(self, stack, fp, pannar[i], msp);
				if(cr == null) {
					//-- Cannot use this- the parameter passed cannot be filled in.
					aflist.add(new FailedAlternative(m + ": Parameter[" + i + "] (a " + fp + ") cannot be provided using the definition " + msp));
					return null;
				}
				refar[i] = cr;
			}

			if(Modifier.isStatic(m.getModifiers())) {
				return new MethodInvoker(m, null, refar); // Invoker without "this" reference,
			}

			//-- Must be called on some instance.
			if(m_thisIsSelf) {
				//-- Use "SELF" as the base.
				return new MethodInvoker(m, new ComponentRef(self), refar); // Invoker without "this" reference,
			}
		}

		//-- Each parameter must be provided for, but parameter locations need not match and when not explicit values may be inferred for unspecified parameters
		throw new IllegalStateException("Not implemented yet");
	}

	/**
	 * Find all methods that are acceptable by name, modifiers and by formals.
	 * @return
	 */
	private List<Method> getAcceptableMethods() {
		List<Method> res = new ArrayList<Method>();
		Method[] mar = ClassUtil.findMethod(m_baseClass, m_methodName);
		for(Method m : mar) {
			int mod = m.getModifiers();
			if(m_staticOnly && !Modifier.isStatic(mod))
				continue;
			if(!Modifier.isPublic(mod))
				continue;
			if(!matchFormals(m))
				continue;
			res.add(m);
		}

		return res;
	}

	/**
	 * Quick match to check for methods matching the method parameter spec. This is only to limit the actual
	 * set of methods tried.
	 *
	 * @param m
	 * @return
	 */
	private boolean matchFormals(final Method m) {
		if(m_actuals.size() == 0)
			return true; // No formals -> accept all
		Class< ? >[] par = m.getParameterTypes();
		if(m_paramMode == ParamMode.NUMBERED && par.length != m_actuals.size()) // Numbered parameters must all match
			return false;
		return m_actuals.size() <= par.length;
	}

	public void setThisIsSelf() {
		m_thisIsSelf = true;
	}


}
