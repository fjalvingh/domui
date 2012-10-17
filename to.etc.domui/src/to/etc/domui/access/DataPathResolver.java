package to.etc.domui.access;

import java.util.*;

import to.etc.domui.annotations.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;
import to.etc.util.*;
import to.etc.webapp.query.*;

public class DataPathResolver extends UiParamDataResolver {

	private final String m_target;

	private final String m_dataPath;

	private String m_paramName;

	private Class< ? > m_dataClassType = null;

	protected DataPathResolver(Class< ? extends UrlPage> pageClass, String target, String dataPath) {
		super(pageClass);
		m_target = target;
		m_dataPath = dataPath;
	}

	@Override
	protected Class< ? > resolveDataClassType() {
		return m_dataClassType;
	}

	public Object resolveDataPath(IPageParameters pp) throws Exception {
		QDataContext dc = null;

		try {
			if(m_paramName == null) {
				for(PropertyInfo pi : ClassUtil.getProperties(getPageClass())) {
					if(pi.getName().equals(m_target) && pi.getGetter() != null) {
						UIUrlParameter upp = pi.getGetter().getAnnotation(UIUrlParameter.class);
						if(upp == null) {
							throw new IllegalStateException(getPageClass() + "\nDataPath shows invalid target, no UIUrlParameter annotation defined for getter found for :" + m_target);
						}
						m_paramName = upp.name();
						m_dataClassType = pi.getGetter().getReturnType();
						break;
					}
				}

				if(m_paramName == null) {
					throw new IllegalStateException(getPageClass() + "\nDataPath shows invalid target, no proper getter found for :" + m_target);
				}
			}

			dc = QContextManager.getDataContextFactory().getDataContext();

			Object value = getAccessData(dc, pp, m_paramName);

			Object res = value;

			if(value != null) {
				if(!value.getClass().isAssignableFrom(m_dataClassType)) {
					throw new IllegalStateException(getPageClass() + "\nDataPath shows invalid target, not expected type foung for getter (expected :" + m_dataClassType + ", but found:"
						+ value.getClass().getCanonicalName() + ")");
				}

				if(!DomUtil.isBlank(m_dataPath)) {
					//walk down property path
					List<PropertyMetaModel< ? >> pmmlist = MetaManager.parsePropertyPath(MetaManager.findClassMeta(value.getClass()), m_dataPath);
					for(PropertyMetaModel< ? > pmm : pmmlist) {
						res = pmm.getValue(res);
					}
				}
			}

			return res;
		} finally {
			if(dc != null) {
				dc.close();
			}
		}
	}


}
