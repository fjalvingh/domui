package to.etc.domui.component.meta.impl;

import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.util.*;

/**
 * This collects whatever extra data is needed during the parse of this object.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 6, 2012
 */
class DefaultJavaClassInfo implements IMetaModelInfo {
	final private DefaultClassMetaModel m_cmm;

	final private Map<PropertyInfo, DefaultPropertyMetaModel< ? >> m_map = new HashMap<PropertyInfo, DefaultPropertyMetaModel< ? >>();

	final private List<SearchPropertyMetaModel> m_searchList = new ArrayList<SearchPropertyMetaModel>();

	final private List<SearchPropertyMetaModel> m_keySearchList = new ArrayList<SearchPropertyMetaModel>();

	public DefaultJavaClassInfo(DefaultClassMetaModel defaultClassMetaModel) {
		m_cmm = defaultClassMetaModel;
	}

	public DefaultClassMetaModel getModel() {
		return m_cmm;
	}

	public Map<PropertyInfo, DefaultPropertyMetaModel< ? >> getMap() {
		return m_map;
	}

	public List<SearchPropertyMetaModel> getSearchList() {
		return m_searchList;
	}

	public List<SearchPropertyMetaModel> getKeySearchList() {
		return m_keySearchList;
	}

	public Class< ? > getTypeClass() {
		return m_cmm.getActualClass();
	}
}