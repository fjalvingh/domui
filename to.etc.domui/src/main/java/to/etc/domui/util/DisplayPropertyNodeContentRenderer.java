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

import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.impl.ExpandedDisplayProperty;
import to.etc.domui.dom.html.NodeContainer;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * This INodeRenderer implementation renders a content node by using a list of DisplayPropertyMetaModel data
 * from the metamodel, rendering a single string formed by concatenating all display properties and getting
 * their string representation from the original source object (passed in as 'object').
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 21, 2008
 */
public class DisplayPropertyNodeContentRenderer implements IRenderInto<Object> {
	//	private ClassMetaModel m_targetClassModel;

	private List<ExpandedDisplayProperty< ? >> m_list;

	private List<ExpandedDisplayProperty< ? >> m_flat;

	public DisplayPropertyNodeContentRenderer(ClassMetaModel cmm, List<ExpandedDisplayProperty< ? >> list) {
		//		m_targetClassModel = cmm;
		m_list = list;
	}

	private void prepare() {
		if(m_flat != null)
			return;
		m_flat = ExpandedDisplayProperty.flatten(m_list);
	}

	@Override public void render(@Nonnull NodeContainer node, @Nonnull Object object) throws Exception {
		prepare();
		StringBuilder sb = new StringBuilder();

		for(ExpandedDisplayProperty< ? > xdp : m_flat) {
			if(sb.length() > 0)
				sb.append(' ');
			String s = xdp.getPresentationString(object);
			sb.append(s);
		}
		node.add(sb.toString());
	}
}
