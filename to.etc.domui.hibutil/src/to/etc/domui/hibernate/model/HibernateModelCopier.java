package to.etc.domui.hibernate.model;

import org.hibernate.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.util.db.*;

public class HibernateModelCopier extends QBasicModelCopier {
	@Override
	protected <T> boolean isUnloadedChildList(T source, PropertyMetaModel pmm) throws Exception {
		Object value = pmm.getAccessor().getValue(source);
		if(value == null)
			return false;
		return !Hibernate.isInitialized(value);
	}

	@Override
	protected <T> boolean isUnloadedParent(T source, PropertyMetaModel pmm) throws Exception {
		Object value = pmm.getAccessor().getValue(source);
		if(value == null)
			return false;
		return !Hibernate.isInitialized(value);
	}
}
