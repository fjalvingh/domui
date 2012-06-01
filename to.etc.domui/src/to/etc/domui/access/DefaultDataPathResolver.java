package to.etc.domui.access;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;

public class DefaultDataPathResolver implements IDataPathResolver {

	/**
	 * Maps UrlPage classnames to their DataPathResolver. We use names instead of the Class instances
	 * to allow for class reloading.
	 */
	private Map<String, DataPathResolver> m_checkerMap = new HashMap<String, DataPathResolver>();

	/**
	 * Resolves special access checker for the page. This uses the cache.
	 * @param pageClass
	 * @return
	 */
	private synchronized DataPathResolver resolveDataPathResolver(final Class< ? extends UrlPage> pageClass, String target, String dataPath) {
		String cn = pageClass.getCanonicalName();
		DataPathResolver dpr = findDataPathResolver(pageClass, cn);
		if(dpr != null) {
			return dpr;
		}

		dpr = calculateDataPathResolver(pageClass, target, dataPath);
		m_checkerMap.put(cn, dpr);
		return dpr;
	}

	private DataPathResolver findDataPathResolver(final Class< ? extends UrlPage> pageClass, String cn) {
		DataPathResolver dpr = m_checkerMap.get(cn);
		if(dpr != null) {
			//-- Hit on name; is the class instance the same? If not this is a reload.
			if((Class< ? >) dpr.getPageClass() == pageClass) // Idiotic generics. If the class changed we have a reload of the class and need to recalculate.
				return dpr;
		}
		return null;
	}

	private DataPathResolver calculateDataPathResolver(Class< ? extends UrlPage> pageClass, String target, String dataPath) {
		return new DataPathResolver(pageClass, target, dataPath);
	}

	@Override
	public Object resolveDataPath(@Nonnull Class< ? extends UrlPage> pageClass, @Nonnull RequestContextImpl ctx, @Nonnull String target, @Nullable String dataPath) throws Exception {
		DataPathResolver dpr = resolveDataPathResolver(pageClass, target, dataPath);
		return dpr.resolveDataPath(PageParameters.createFrom(ctx));
	}


}
