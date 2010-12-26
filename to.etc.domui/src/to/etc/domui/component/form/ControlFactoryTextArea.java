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
package to.etc.domui.component.form;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

public class ControlFactoryTextArea implements ControlFactory {
	/**
	 * Accept if the componentHint says textarea.
	 * @see to.etc.domui.component.form.ControlFactory#accepts(to.etc.domui.component.meta.PropertyMetaModel, boolean)
	 */
	@Override
	public int accepts(PropertyMetaModel< ? > pmm, boolean editable, Class< ? > controlClass, Object context) {
		if(controlClass != null && !controlClass.isAssignableFrom(TextArea.class))
			return -1;
		if(pmm.getComponentTypeHint() != null) {
			if(pmm.getComponentTypeHint().toLowerCase().contains(MetaUtils.TEXT_AREA))
				return 10;
		}
		return 0;
	}

	@Override
	public <T> ControlFactoryResult createControl(IReadOnlyModel< ? > model, PropertyMetaModel<T> pmm, boolean editable, Class< ? > controlClass, Object context) {
		TextArea ta = new TextArea();
		if(!editable)
			ta.setReadOnly(true);
		String hint = pmm.getComponentTypeHint().toLowerCase();
		ta.setCols(MetaUtils.parseIntParam(hint, MetaUtils.COL, 80));
		ta.setRows(MetaUtils.parseIntParam(hint, MetaUtils.ROW, 4));
		if(pmm.isRequired())
			ta.setMandatory(true);
		String s = pmm.getDefaultHint();
		if(s != null)
			ta.setTitle(s);
		return new ControlFactoryResult(ta, model, (PropertyMetaModel<String>) pmm);
	}
}
