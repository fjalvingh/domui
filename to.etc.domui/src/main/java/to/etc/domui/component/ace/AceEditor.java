package to.etc.domui.component.ace;

import to.etc.domui.dom.header.HeaderContributor;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.UrlPage;
import to.etc.util.StringTool;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * DomUI wrapper for the <a href="https://ace.c9.io/">ACE code editor</a>.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 23-12-17.
 */
public class AceEditor extends Div {
	static private String m_version = "1.2.9";

	@Nullable
	private String m_value;

	@Override public void createContent() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		sb.append("let ed = ace.edit('").append(getActualID()).append("');\n");
		sb.append("ed.setTheme('ace/theme/monokai');\n");
		sb.append("ed.getSession().setMode('ace/mode/javascript');\n");
		sb.append("window['").append(getActualID()).append("'] = ed;\n");
		String value = getValue();
		if(null != value) {
			sb.append("ed.setValue(");
			StringTool.strToJavascriptString(sb, value, true);
			sb.append(");\n");
		}

		sb.append("};\n");
		appendCreateJS(sb);
	}
	static public void initialize(UrlPage page) {
		page.getPage().addHeaderContributor(HeaderContributor.loadJavascript("https://cdnjs.cloudflare.com/ajax/libs/ace/" + m_version + "/ace.js"), 10);
	}

	@Nullable public String getValue() {
		return m_value;
	}

	public void setValue(@Nullable String value) {
		if(Objects.equals(value, m_value))
			return;
		m_value = value;
		forceRebuild();
	}
}
