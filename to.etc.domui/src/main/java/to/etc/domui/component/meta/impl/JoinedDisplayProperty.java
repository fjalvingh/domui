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
package to.etc.domui.component.meta.impl;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

/**
 * A special property consisting of a list of joined properties.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 28, 2008
 */
public class JoinedDisplayProperty extends ExpandedDisplayProperty<String> implements IValueAccessor<String> {
	private List<DisplayPropertyMetaModel> m_displayList;

	private List<PropertyMetaModel< ? >> m_propertyList;

	public JoinedDisplayProperty(List<DisplayPropertyMetaModel> list, List<PropertyMetaModel< ? >> plist, IValueAccessor< ? > rootAccessor) {
		super(String.class, null, rootAccessor);
		m_displayList = list;
		m_propertyList = plist;
		if(list.size() < 2)
			throw new IllegalStateException("?? Expecting at least 2 properties in a joined display property.");

		//-- Calculate basics..
		DisplayPropertyMetaModel dpm = list.get(0); // 1st thingy contains sizes,
		setDisplayLength(dpm.getDisplayLength());
		setSortable(SortableType.UNSORTABLE);
		setName(dpm.getProperty().getName());
	}

	//	jal 20101226 This compiles in Eclipse 3.6 even though the overridden method is final!!?!?!
	//	@Override
	//	public void setValue(Object target, String value) throws Exception {
	//		throw new IllegalStateException("You cannot set a joined display property.");
	//	}

	/**
	 * This creates the joined value of the items in the set.
	 * @see to.etc.domui.util.IValueTransformer#getValue(java.lang.Object)
	 */
	@Override
	public String getValue(Object in) throws Exception {
		Object root = super.getValue(in); // Obtain the root object
		if(root == null)
			return null;

		//-- Now access using the root value
		StringBuilder sb = new StringBuilder();
		String join = null;
		for(int i = 0; i < m_displayList.size(); i++) {
			DisplayPropertyMetaModel dpm = m_displayList.get(i);
			PropertyMetaModel< ? > pm = m_propertyList.get(i);
			Object value = pm.getValue(root);
			if(value == null)
				continue;
			String s;
			IConverter< ? > converter = pm.getConverter();
			if(converter != null)
				s = ((IConverter<Object>) converter).convertObjectToString(NlsContext.getLocale(), value);
			else
				s = ConverterRegistry.convertToString((PropertyMetaModel<Object>) pm, value);
			if(s == null || s.length() == 0)
				continue;
			if(join != null)
				sb.append(join);
			join = dpm.getJoin();
			sb.append(s);
		}
		return sb.toString();
	}

	@Override
	@Nonnull
	public String getDefaultLabel() {
		DisplayPropertyMetaModel dm = m_displayList.get(0);
		String lbl = dm.getLabel(); // Is the label overridden in the DisplayProperty?
		if(lbl != null)
			return lbl;
		return m_propertyList.get(0).getDefaultLabel();
	}
}
