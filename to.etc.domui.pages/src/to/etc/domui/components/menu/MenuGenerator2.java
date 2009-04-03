package to.etc.domui.components.menu;

import java.io.*;
import java.util.*;

import to.etc.util.*;

public class MenuGenerator2 {

	/**
	 * Generate the menu as a JSON structure.
	 * @param pw
	 * @param list
	 * @throws Exception
	 */
	public void		generate(final Writer pw, final List<IMenuItem> list) throws Exception {
		pw.append("[");
		boolean first = true;
		for(IMenuItem m: list) {
			if(first)
				first = false;
			else
				pw.append(",\n");
			generateNode(pw, m);
		}
		pw.append("]");
	}

	private void generateNode(final Writer pw, final IMenuItem m) throws Exception {
		pw.append("{");

		pw.append("v:");
		pw.append(Boolean.toString(! m.isDisabled()));
		pw.append(",");
		p(pw, "id", m.getId());
		p(pw, "desc", m.getDescription());
		p(pw, "label", m.getLabel());
		p(pw, "icon", m.getIconPath());
		p(pw, "kw", m.getSearchString());
//		p(pw, "para", m.getPa());
		p(pw, "path", m.getPageClass() == null ? "" : m.getPageClass().getName()+".ui");
		p(pw, "knd", m.getPageClass() != null ? "domui" : "jsp");
		p(pw, "tgt", "FRAME");
		p(pw, "domui", m.getPageClass() != null);
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

	private void	p(final Writer w, final String key, final boolean value) throws Exception {
		w.append(key);
		w.append(":");
		w.append(Boolean.toString(value));
		w.append(",");
	}

	private void	p(final Writer w, final String key, final String value) throws Exception {
		if(value == null)
			return;
		w.append(key);
		w.append(":");
		StringTool.strToJavascriptString(w, value, true);
		w.append(",");
	}
	private void	p(final Writer w, final String key, final Long value) throws Exception {
		if(value == null)
			return;
		w.append(key);
		w.append(":");
		w.append(value.toString());
		w.append(",");
	}
}
