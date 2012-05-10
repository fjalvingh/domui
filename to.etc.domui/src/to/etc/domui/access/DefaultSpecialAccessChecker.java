package to.etc.domui.access;

import java.lang.reflect.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.annotations.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;

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
	 * Maps UrlPage classnames to their AccessChecker. We use names instead of the Class instances
	 * to allow for class reloading.
	 */
	private Map<String, AccessChecker> m_checkerMap = new HashMap<String, AccessChecker>();

	/**
	 * Resolves special access checker for the page. This uses the cache.
	 * @param pageClass
	 * @return
	 */
	private synchronized AccessChecker resolveAccessChecker(final Class< ? extends UrlPage> pageClass) {
		String cn = pageClass.getCanonicalName();
		AccessChecker ach = findAccessChecker(pageClass, cn);
		if (ach != null) {
			return ach;
		}

		ach = calculateAccessChecker(pageClass);
		m_checkerMap.put(cn, ach);
		return ach;
	}

	private AccessChecker findAccessChecker(final Class< ? extends UrlPage> pageClass, String cn) {
		AccessChecker ach = m_checkerMap.get(cn);
		if(ach != null) {
			//-- Hit on name; is the class instance the same? If not this is a reload.
			if((Class< ? >) ach.getPageClass() == pageClass) // Idiotic generics. If the class changed we have a reload of the class and need to recalculate.
				return ach;
		}
		return null;
	}

	private AccessChecker calculateAccessChecker(Class< ? extends UrlPage> pageClass) {
		Method checkMethod = getSpecialAccessCheckMethod(pageClass);
		if (checkMethod == null) {
			return null;
		}
		return new AccessChecker(pageClass, checkMethod);
	}

	@Override
	public UISpecialAccessResult doSpecialAccessCheck(@Nonnull Class< ? extends UrlPage> pageClass, @Nonnull RequestContextImpl ctx) throws Exception {
		AccessChecker ach = resolveAccessChecker(pageClass);
		return ach.checkAccess(PageParameters.createFrom(ctx));
	}

	private Method getSpecialAccessCheckMethod(Class< ? extends UrlPage> clz) {
		Method[] methods = clz.getDeclaredMethods();
		for(Method method : methods) {
			UISpecialAccessCheck upp = method.getAnnotation(UISpecialAccessCheck.class);
			if(upp != null && !Constants.NONE.equals(upp.dataParam()) && //
				Modifier.isStatic(method.getModifiers()) && //
				method.getParameterTypes().length == 1) {
				return method;
			}
		}
		return null;
	}

	@Override
	public boolean hasSpecialAccess(@Nonnull Class< ? extends UrlPage> clz) {
		AccessChecker ach = resolveAccessChecker(clz);
		return ach != null;
	}

}
