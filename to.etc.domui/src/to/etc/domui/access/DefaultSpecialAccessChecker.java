package to.etc.domui.access;

import java.lang.reflect.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.annotations.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;

/**
 * This is the default DomUI page special access checker. It is responsible for providing (injecting) data values into
 * special access check methods where required. This default version only accepts the @{@link UISpecialAccessCheck} annotation
 * and uses it to inject entities loaded from an URL parameter into check method.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 15 Dec 2011
 */
public class DefaultSpecialAccessChecker implements ISpecialAccessChecker {

	/**
	 * Maps UrlPage classnames to their PageInjectors. We use names instead of the Class instances
	 * to allow for class reloading.
	 */
	private Map<String, AccessChecker> m_checkerMap = new HashMap<String, AccessChecker>();

	/**
	 * Find the page injectors to use for the page. This uses the cache.
	 * @param page
	 * @return
	 */
	private synchronized AccessChecker findAccessChecker(final Class< ? extends UrlPage> pageClass) {
		String cn = pageClass.getCanonicalName();
		AccessChecker ach = m_checkerMap.get(cn);
		if(ach != null) {
			//-- Hit on name; is the class instance the same? If not this is a reload.
			if((Class< ? >) ach.getPageClass() == pageClass) // Idiotic generics. If the class changed we have a reload of the class and need to recalculate.
				return ach;
		}

		ach = calculateAccessChecker(pageClass);
		m_checkerMap.put(cn, ach);
		return ach;
	}

	private AccessChecker calculateAccessChecker(Class< ? extends UrlPage> pageClass) {
		String checksAccessMethodName = resolveSpecialAccessCheckMethodName(pageClass);

		Method[] methods = pageClass.getDeclaredMethods();
		Method checkMethod = null;
		for(Method method : methods) {
			if(method.getName().equals(checksAccessMethodName) && //
				Modifier.isStatic(method.getModifiers()) && //
				method.getParameterTypes().length == 1) {
				checkMethod = method;
				break;
			}
		}

		if(checkMethod == null) {
			throw new IllegalStateException("Missing expected static method returning boolean with name " + checksAccessMethodName + " with 1 parameters in class " + pageClass);
		}

		return new AccessChecker(pageClass, checkMethod);
	}

	private String resolveSpecialAccessCheckMethodName(Class< ? extends UrlPage> pageClass) {
		UIRights rann = pageClass.getAnnotation(UIRights.class);
		if(rann == null) {
			return null;
		}
		return rann.specialCheckMethod();
	}

	@Override
	public UISpecialAccessResult specialRightsCheck(@Nonnull Class< ? extends UrlPage> pageClass, @Nonnull RequestContextImpl ctx) throws Exception {
		AccessChecker ach = findAccessChecker(pageClass);
		return ach.checkAccess(PageParameters.createFrom(ctx));
	}

}
