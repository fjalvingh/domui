/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
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

	public boolean calculate(PropertyMetaModel< ? > pmm) {
		IRequestContext rq = PageContext.getRequestContext();
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
