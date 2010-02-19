package to.etc.domui.component.fasthtml;

import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;
import to.etc.util.*;

public class HtmlMiniEditor extends TextArea {
	private String m_styleSheet;


	@Override
	public void createContent() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("$(\"#").append(getActualID()).append("\").wysiwyg({css:");
		String css = getStyleSheet();
		if(css == null)
			css = PageContext.getRequestContext().getRelativeThemePath("minieditor.css");
		StringTool.strToJavascriptString(sb, css, false);
		sb.append("});");
		appendCreateJS(sb);
		//		appendCreateJS("$(\"#" + getActualID() + "\").wysiwyg({css:'/ui/$themes/blue/style.theme.css'});");
	}

	public String getStyleSheet() {
		return PageContext.getRequestContext().translateResourceName(m_styleSheet);
	}

	public void setStyleSheet(String styleSheet) {
		if(DomUtil.isEqual(styleSheet, m_styleSheet))
			return;
		m_styleSheet = styleSheet;
		changed();
	}
}
