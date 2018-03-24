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
package to.etc.domui.component.menu;

import to.etc.util.StringTool;

import java.io.Writer;
import java.util.List;

public class MenuGenerator2 {

	/**
	 * Generate the menu as a JSON structure.
	 * @param pw
	 * @param list
	 * @throws Exception
	 */
	public void generate(final Writer pw, final List<MenuItem> list) throws Exception {
		pw.append("[");
		boolean first = true;
		for(MenuItem m : list) {
			if(first)
				first = false;
			else
				pw.append(",\n");
			generateNode(pw, m);
		}
		pw.append("]");
	}

	private void generateNode(final Writer pw, final MenuItem m) throws Exception {
		pw.append("{");

		pw.append("v:");
		pw.append(Boolean.toString(!m.isDisabled()));
		pw.append(",");
		p(pw, "id", m.getId());
		p(pw, "desc", m.getDescription());
		p(pw, "label", m.getLabel());
		p(pw, "icon", m.getImage());
		p(pw, "kw", m.getSearchString());
		//		p(pw, "para", m.getPa());
		p(pw, "path", m.getPageClass() == null ? "" : m.getPageClass().getName() + ".ui");
		p(pw, "rurl", m.getRURL());
		p(pw, "knd", m.getPageClass() != null ? "domui" : "jsp");
		p(pw, "target", m.getTarget());
		//		p(pw, "domui", m.getPageClass() != null);
		//		p(pw, "", m.get());
		//		p(pw, "", m.get());
		//		p(pw, "", m.get());
		//		p(pw, "", m.get());
		//		p(pw, "", m.get());
		//		p(pw, "", m.get());
		//		p(pw, "", m.get());
		//		p(pw, "", m.get());
		//		p(pw, "", m.get());

		if(m.getChildren() != null && m.getChildren().size() > 0) {
			pw.append("children:");
			generate(pw, m.getChildren());
		} else {
			pw.append("leaf:true");
		}
		pw.append("}");
	}

	//	private void	p(final Writer w, final String key, final boolean value) throws Exception {
	//		w.append(key);
	//		w.append(":");
	//		w.append(Boolean.toString(value));
	//		w.append(",");
	//	}

	private void p(final Writer w, final String key, final String value) throws Exception {
		if(value == null)
			return;
		w.append(key);
		w.append(":");
		StringTool.strToJavascriptString(w, value, true);
		w.append(",");
	}
	//	private void	p(final Writer w, final String key, final Long value) throws Exception {
	//		if(value == null)
	//			return;
	//		w.append(key);
	//		w.append(":");
	//		w.append(value.toString());
	//		w.append(",");
	//	}
}
