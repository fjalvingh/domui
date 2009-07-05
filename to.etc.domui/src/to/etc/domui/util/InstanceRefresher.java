package to.etc.domui.util;

import to.etc.domui.component.meta.*;
import to.etc.domui.state.*;
import to.etc.webapp.query.*;

public class InstanceRefresher {
	static public void refresh(Object val) throws Exception {
		if(val == null)
			return;
		ClassMetaModel cmm = MetaManager.findClassMeta(val.getClass());
		if(!cmm.isPersistentClass())
			return;
		QDataContext dc = QContextManager.getContext(PageContext.getCurrentConversation());
		dc.attach(val);
	}
}
