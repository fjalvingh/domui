package to.etc.domui.access;

import java.lang.reflect.*;

import javax.annotation.*;

import to.etc.domui.annotations.*;
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
public class AccessChecker extends UiParamDataResolver {

	final private Method m_checkAccessMethod;

	public AccessChecker(final Class< ? extends UrlPage> pageClass, final Method checkAccessMethod) {
		super(pageClass);
		m_checkAccessMethod = checkAccessMethod;
	}
	
	@Override
	protected Class< ? > resolveDataClassType() {
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
	public UISpecialAccessResult checkAccess(@Nonnull final IPageParameters pp) throws Exception {
		if (m_checkAccessMethod != null) {
			QDataContext dc = null;

			try {
				dc = QContextManager.getDataContextFactory().getDataContext();

				UISpecialAccessCheck upp = m_checkAccessMethod.getAnnotation(UISpecialAccessCheck.class);
				if(upp == null || Constants.NONE.equals(upp.dataParam())) {
					return null;
				}
				Object value = getAccessData(dc, pp, upp.dataParam());

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
