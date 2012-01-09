package to.etc.server.injector;

import java.lang.annotation.*;
import java.lang.reflect.*;

import to.etc.server.ajax.*;

/**
 * A default implementation of a method injector cache which uses the
 * standard Retriever / Converter framework of an Injector to resolve
 * method parameters.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 20, 2006
 */
public class DefaultParameterInjectorCache extends ParameterInjectorCache {
	/** The injector which knows the rules about injecting into these parameters. */
	private Injector	m_injector;

	public DefaultParameterInjectorCache(Injector inj) {
		m_injector = inj;
	}

	protected Retriever calculateRetriever(int pix, Class sourcecl, Class paramty, Annotation[] pannar) throws Exception {
		//-- No known annotations. Try for a default retriever
		Retriever dp = m_injector.findRetriever(sourcecl, paramty, null, pannar);
		if(dp != null)
			return dp;
		throw new AjaxHandlerException("Don't know how to retrieve a value for parameter " + (pix + 1) + ", (a " + paramty.toString() + ") of method " + this);
	}

	@SuppressWarnings("null")
	@Override
	protected ParameterInjectorSet calcInjectors(Class sourcecl, Method m, boolean[] done) throws Exception {
		Annotation[][] annar = m.getParameterAnnotations();
		Class[] formalar = m.getParameterTypes();
		InjectorConverter[] converters = null;
		Retriever[] retrievers = null;
		for(int pix = formalar.length; --pix >= 0;) {
			if(done[pix])
				continue;

			//-- Try to find a fix for this parameter.
			Annotation[] pann = annar[pix];
			Retriever re = m_injector.findRetriever(sourcecl, formalar[pix], null, pann);
			InjectorConverter co = null;
			if(re != null) {
				//-- Retriever found.... We may provide for this one after all..
				if(!formalar[pix].isAssignableFrom(re.getType())) {
					//-- Cannot directly assign; need a converter
					co = m_injector.findParameterConverter(formalar[pix], re.getType());
					if(co == null)
						throw new ParameterException("Cannot convert a " + re.getType().getName() + " to the type of parameter " + (pix + 1) + ", (a " + formalar[pix] + ") of method " + m)
							.setHandlerMethod(m).setHandlerClass(m.getDeclaringClass()).setParameterIndex(pix);
				}

				//-- If we're still willing then add this as a valid response
				if(re != null) {
					if(retrievers == null) {
						converters = new InjectorConverter[formalar.length];
						retrievers = new Retriever[formalar.length];
					}
					retrievers[pix] = re;
					converters[pix] = co;
					done[pix] = true;
				}
			}
		}
		if(retrievers == null)
			return null;
		return new ParameterInjectorSet(sourcecl, retrievers, converters);
	}
}
