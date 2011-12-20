package to.etc.domui.access;

import java.lang.reflect.*;

import javax.annotation.*;

import to.etc.domui.annotations.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;
import to.etc.webapp.query.*;

/**
 * Does special access check calls defined by annotation {@link UISpecialAccessCheck} using reflection.
 * 
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 15 Dec 2011
 */
public class AccessChecker {

	final private Method m_checkAccessMethod;

	final private Class< ? extends UrlPage> m_pageClass;
	
	public AccessChecker(final Class< ? extends UrlPage> pageClass, final Method checkAccessMethod) {
		m_pageClass = pageClass;
		m_checkAccessMethod = checkAccessMethod;
	}
	
	public Class< ? extends UrlPage> getPageClass() {
		return m_pageClass;
	}

	private Object getAccessData(QDataContext dc, final PageParameters pp) throws Exception {
		UISpecialAccessCheck upp = m_checkAccessMethod.getAnnotation(UISpecialAccessCheck.class);
		if(upp == null || Constants.NONE.equals(upp.dataParam())) {
			return null;
		}

		String pv = pp.getString(upp.dataParam());

		if(pv == null || "NEW".equals(pv))
			return null;

		Class< ? > targetDataClass = resolveDataClassType();
		
		dc = QContextManager.getDataContextFactory().getDataContext();
		
		Object pk = getKeyInstance(dc, upp.dataParam(), pv, targetDataClass);

		if(pk != null) {
			return dc.find(targetDataClass, pk);
		}
		return null;
	}
	
	private Object getKeyInstance(QDataContext dc, String param, String pv, Class< ? > targetDataClass) throws Exception {
		//-- Try to find the PK for this entity
		ClassMetaModel cmm = MetaManager.findClassMeta(targetDataClass);

		PropertyMetaModel< ? > pmm = cmm.getPrimaryKey(); // Find it's PK;
		if(pmm == null)
			throw new RuntimeException("Cannot find the primary key property for entity class '" + targetDataClass + "' for URL parameter=" + param + " of page=" + m_pageClass + ".");

		// if the parametervalue has no value and the type of the key is Number, we treet it like no parameter was filled in
		if(Number.class.isAssignableFrom(pmm.getActualType()) && pv != null && pv.length() == 0)
			return null;

		//-- Convert the URL's value to the TYPE of the primary key, using URL converters.
		Object pk = CompoundKeyConverter.INSTANCE.unmarshal(dc, pmm.getActualType(), pv);

		if(pk == null)
			throw new RuntimeException("URL parameter value='" + pv + "' converted to Null primary key value for entity class '" + targetDataClass + "' for URL parameter=" + param + " of page=" + m_pageClass + ".");
		return pk;
	}

	private Class< ? > resolveDataClassType() {
		if(m_checkAccessMethod.getParameterTypes().length != 1) {
			throw new IllegalStateException("Check access method needs to have exactly 1 parameter!");
		}

		Class< ? > targetDataClass = m_checkAccessMethod.getParameterTypes()[0]; // Allways assumes that single param is target data
		return targetDataClass;
	}

	/**
	 * If possible, does special access check call and returns its result. 
	 * @param pp
	 * @return
	 * @throws Exception
	 */
	public UISpecialAccessResult checkAccess(@Nonnull final PageParameters pp) throws Exception {
		if (m_checkAccessMethod != null) {
			QDataContext dc = null;

			try {
				dc = QContextManager.getDataContextFactory().getDataContext();

				Object value = getAccessData(dc, pp);
				if(value != null) {
					Object res = m_checkAccessMethod.invoke(null, value);

					if(res == null || (!(res instanceof UISpecialAccessResult))) {
						throw new IllegalStateException("Expected non null result of type UISpecialAccessResult but found: " + res);
					}
					return (UISpecialAccessResult) res;
				}
			} finally {
				if(dc != null) {
					dc.close();
				}
			}
		}
		return UISpecialAccessResult.DEFAULT;
	}
}
