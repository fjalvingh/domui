package to.etc.iocular.def;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import to.etc.iocular.container.FailedAlternative;
import to.etc.iocular.container.MethodInvoker;
import to.etc.iocular.util.ClassUtil;

/**
 * Some kind of method or constructor call builder.
 *
 * @author jal
 * Created on Apr 22, 2007
 */
public class MethodCallBuilder {
	private ComponentBuilder		m_component;

	private Class<?>				m_baseClass;

	private String					m_methodName;

	private List<Class<?>>			m_formals;

	private ParameterDef[]			m_paramDefs;

	private boolean					m_staticOnly;

	/**
	 * When set this locates the method that explicitly matches the formals
	 * specified; when true all formals must be specified and an exact match
	 * must be found.
	 */
	private boolean					m_explicit;

	public MethodCallBuilder(ComponentBuilder component, Class< ? > baseClass, String methodName, Class< ? >[] formals, boolean staticOnly) {
		m_component = component;
		m_baseClass = baseClass;
		m_methodName = methodName;
		m_staticOnly = staticOnly;
		m_formals	= new ArrayList<Class<?>>();
		for(int i = 0; i < formals.length; i++)
			m_formals.add(formals[i]);
	}

	/**
	 * Create an unique invoker for this method. The invoker encapsulates the method
	 * to call plus references to all parameters for the method as obtained from
	 * a container.
	 *
	 * @param stack
	 * @return
	 */
	public MethodInvoker	createInvoker(Stack<ComponentBuilder> stack) {
		List<Method>	mlist = getAcceptableMethods();
		if(mlist.size() == 0)
			throw new IocConfigurationException(m_component, "Cannot find an acceptable method '"+m_methodName+" on "+m_baseClass);
		if(mlist.size() > 1 && m_explicit)
			throw new IllegalStateException("internal: no unique method for explicit method found.");

		/*
		 * Try to create invokers for all applicable methods, then keep the best one.
		 */
		List<FailedAlternative>	aflist = new ArrayList<FailedAlternative>();
		MethodInvoker	best= null;
		for(Method m : mlist) {
			MethodInvoker	miv = tryToMakeAnInvokerIfYouWouldBeSoKind(stack, m, aflist);
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
	 *
	 * @param stack
	 * @param m
	 * @param aflist
	 * @return
	 */
	private MethodInvoker	tryToMakeAnInvokerIfYouWouldBeSoKind(Stack<ComponentBuilder> stack, Method m, List<FailedAlternative> aflist) {
		Class<?>[] 		fpar = m.getParameterTypes();
		Annotation[][]	pannar = m.getParameterAnnotations();
		ComponentRef[]	refar	= new ComponentRef[fpar.length];

		for(int i = 0; i < fpar.length; i++) {	
			Class<?> fp = fpar[i];
			ParameterDef	def = null;
			if(m_paramDefs != null && i < m_paramDefs.length)
				def = m_paramDefs[i];
			ComponentRef	cr	= m_component.getBuilder().findReferenceFor(stack, fp, pannar[i], def);
			if(cr == null) {
				//-- Cannot use this- the parameter passed cannot be filled in.
				aflist.add(new FailedAlternative(m+": Parameter["+i+"] (a "+fp+") cannot be provided"));
				return null;
			}
			refar[i] = cr;
		}
		return new MethodInvoker(m, refar);
	}

	/**
	 * Find all methods that are acceptable by name, modifiers and by formals.
	 * @return
	 */
	private List<Method>	getAcceptableMethods() {
		List<Method>	res = new ArrayList<Method>();
		Method[]	mar = ClassUtil.findMethod(m_baseClass, m_methodName);
		for(Method m : mar) {
			int mod = m.getModifiers();
			if(m_staticOnly && ! Modifier.isStatic(mod))
				continue;
			if(! Modifier.isPublic(mod))
				continue;
			if(! matchFormals(m))
				continue;
			res.add(m);
		}

		return res;
	}

	/**
	 * Checks to see if a method matches it's formals.
	 *
	 * @param m
	 * @return
	 */
	private boolean	matchFormals(Method m) {
		if(m_formals == null)
			return true;									// No formals -> accept all
		Class<?>[]	par = m.getParameterTypes();
		if(m_formals.size() != par.length && m_explicit)		// If explicit parameter count must match formal count
			return false;
		else if(m_formals.size() > par.length)				// More formals specified than parameters on the method?
			return false;

		for(int i = 0; i < par.length; i++) {
			if(i < m_formals.size()) {
				Class<?> fp = m_formals.get(i);
				if(fp != null) {
					if(! par[i].isAssignableFrom(fp))
						return false;
				}
			}
		}

		return true;
	}

}
