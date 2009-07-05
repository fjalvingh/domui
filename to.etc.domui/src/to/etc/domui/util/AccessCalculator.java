package to.etc.domui.util;

import to.etc.domui.component.meta.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;

/**
 * Helps with calculating access rights to fields and classes, depending on the roles defined
 * for the user and the access rights masks set in the metadata.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 19, 2008
 */
public class AccessCalculator {
	private boolean m_editable;

	private boolean m_viewable;

	/**
	 * Returns T if the component is to be made editable.
	 * @return
	 */
	public boolean isEditable() {
		return m_editable;
	}

	public boolean isViewable() {
		return m_viewable;
	}

	public boolean calculate(PropertyMetaModel pmm) {
		RequestContext rq = PageContext.getRequestContext();
		m_viewable = MetaManager.isAccessAllowed(pmm.getViewRoles(), rq);
		m_editable = MetaManager.isAccessAllowed(pmm.getEditRoles(), rq);
		if(!m_viewable) {
			//-- Check edit stuff: 
			if(pmm.getEditRoles() == null) // No edit roles at all -> exit
				return false;
			if(!m_editable)
				return false;

			m_viewable = true; // Force it to be viewable too since we have to edit it
		}
		if(pmm.getReadOnly() == YesNoType.YES) {
			m_editable = false;
		}
		return true;
	}
}
