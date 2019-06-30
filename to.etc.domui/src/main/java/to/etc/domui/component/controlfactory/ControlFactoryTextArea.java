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
package to.etc.domui.component.controlfactory;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.MetaUtils;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.misc.UIControlUtil;
import to.etc.domui.dom.html.TextArea;
import to.etc.domui.server.DomApplication;

public class ControlFactoryTextArea implements PropertyControlFactory {
	/**
	 * Accept if the componentHint says textarea.
	 */
	@Override
	public int accepts(@NonNull PropertyMetaModel< ? > pmm, boolean editable, @Nullable Class< ? > controlClass) {
		if(controlClass != null && !controlClass.isAssignableFrom(TextArea.class))
			return -1;
		String cth = pmm.getComponentTypeHint();
		if(cth != null) {
			if(cth.toLowerCase().contains(MetaUtils.TEXT_AREA))
				return 10;
		}
		return 0;
	}

	@Override
	public @NonNull <T> ControlFactoryResult createControl(@NonNull PropertyMetaModel<T> pmm, boolean editable, @Nullable Class< ? > controlClass) {
		TextArea ta = editable ? new TextArea() : new TextDisplayArea();
		if(!editable)
			ta.setReadOnly(true);
		String cth = pmm.getComponentTypeHint();
		if(cth != null) {
			String hint = cth.toLowerCase();
			ta.setCols(MetaUtils.parseIntParam(hint, MetaUtils.COL, 80));
			ta.setRows(MetaUtils.parseIntParam(hint, MetaUtils.ROW, 4));
		}
		UIControlUtil.configure(ta, pmm, editable);

		int length = pmm.getLength();
		if(length > 0) {
			ta.setMaxLength(length);
			ta.setMaxByteLength(DomApplication.getPlatformVarcharByteLimit());
		}

		return new ControlFactoryResult(ta);
	}
}
