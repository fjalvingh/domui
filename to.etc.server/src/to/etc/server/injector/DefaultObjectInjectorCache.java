package to.etc.server.injector;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * This default version of an injector cache walks all of the property setters
 * and tries to provide them with a value.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 12, 2006
 */
public class DefaultObjectInjectorCache extends ObjectInjectorCache {
	private Injector	m_injector;

	public DefaultObjectInjectorCache(Injector inj) {
		m_injector = inj;
	}

	/**
	 * Called when an object is not yet known. This walks all of the
	 * methods in the todolist and tries to calculate a setter for
	 * them. If a setter is found it is added to the donemap.
	 */
	@Override
	protected InjectorSet calcInjectors(Class< ? extends Object> sourcecl, Class< ? extends Object> targetcl, Map<String, Method> donemap) throws Exception {
		List<SetterInjector> list = new ArrayList<SetterInjector>();
		Map<String, Method> todolist = Injector.getObjectSetterMap(targetcl);
		for(Iterator<String> it = todolist.keySet().iterator(); it.hasNext();) {
			String name = it.next();
			if(donemap.containsKey(name))
				continue;
			Method m = todolist.get(name);
			Annotation[][] paar = m.getParameterAnnotations();
			Annotation[] pann = null;
			if(paar != null && paar.length >= 1) {
				pann = paar[0];
			}
			SetterInjector si = calcSetterFor(sourcecl, m, name, m.getParameterTypes()[0], pann); // Try to calculate a setter thingy.
			if(si != null) {
				list.add(si);
				donemap.put(name, m);
			}
			//			else if(m_allMandatory)
			//				throw new ParameterException("No parameter provider for property '"+name+"' of class '"+cl.getCanonicalName()+"' (type "+far[0].getCanonicalName()+")")
			//				.setHandlerClass(cl).setParameterName(name)
			//			;
		}
		return new InjectorSet(list, sourcecl);
	}

	/**
	 * Tries to calculate a setter which sets the property type specified. If
	 * no provider can be found this returns null.
	 *
	 * @param sourcecl          The type of the "source" object
	 * @param m                 The setter method which needs a value
	 * @param propertyName
	 * @param type
	 * @return
	 */
	protected SetterInjector calcSetterFor(Class< ? > sourcecl, Method m, String propertyName, Class< ? > totype, Annotation[] paramann) throws Exception {
		//-- 1. Find a retriever,
		Retriever r = m_injector.findRetriever(sourcecl, totype, propertyName, paramann);
		if(r == null)
			return null;

		//-- 2. Check to see if a converter is needed to convert from source to target,
		Class< ? > fromtype = r.getType();
		InjectorConverter p;
		if(totype.isAssignableFrom(fromtype)) // Can be assigned directly?
			p = null;
		else {
			p = m_injector.findParameterConverter(totype, fromtype, paramann);
			if(p == null)
				return null;
		}
		return new SetterInjector(m, r, p);
	}
}
