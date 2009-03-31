package to.etc.domui.component.meta.impl;

import to.etc.domui.component.meta.*;

/**
 * A default, base implementation of a MetaModel layer. This tries to discover metadata by using
 * base property information plus Hibernate/JPA annotation data.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 16, 2008
 */
public class DefaultDataMetaModel implements DataMetaModel {
	public void updateClassMeta(DefaultClassMetaModel dmm) {
		dmm.initialize();			// FIXME Delegate initialization here?
	}

	/**
	 * 
	 * @see to.etc.domui.component.meta.DataMetaModel#findFieldData(java.lang.Class, java.lang.String)
	 */
	public PropertyMetaModel findFieldData(Class< ? > clz, String fieldname) {
		DefaultClassMetaModel	cm = (DefaultClassMetaModel)MetaManager.findClassMeta(clz);
		if(cm == null)
			return null;
		return cm.findProperty(fieldname);
	}
}
