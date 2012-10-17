package to.etc.domui.access;

import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;
import to.etc.webapp.query.*;

public abstract class UiParamDataResolver {

	final private Class< ? extends UrlPage> m_pageClass;

	protected UiParamDataResolver(Class< ? extends UrlPage> pageClass) {
		m_pageClass = pageClass;
	}

	protected Object getAccessData(final QDataContext dc, final IPageParameters pp, final String param) throws Exception {
		String pv = pp.getString(param);

		if(pv == null || "NEW".equals(pv))
			return null;

		Class< ? > targetDataClass = resolveDataClassType();

		Object pk = getKeyInstance(dc, param, pv, targetDataClass);

		if(pk != null) {
			return dc.find(targetDataClass, pk);
		}
		return null;
	}

	protected abstract Class< ? > resolveDataClassType();

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

	public Class< ? extends UrlPage> getPageClass() {
		return m_pageClass;
	}
}
