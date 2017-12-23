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

	@Nullable
	private String m_theme;

	@Nullable
	private String m_mode;

	private int m_gotoLine;

	private int m_tabSize = 4;

	private boolean m_disabled;

	private boolean m_readOnly;

	private boolean m_internalRo;

	@Override public void createContent() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		sb.append("let ed = ace.edit('").append(getActualID()).append("');\n");
		sb.append("window['").append(getActualID()).append("'] = ed;\n");
		//updateTheme();
		//sb.append("ed.getSession().setMode('ace/mode/javascript');\n");
		//String value = getValue();
		//if(null != value) {
		//	sb.append("ed.setValue(");
		//	StringTool.strToJavascriptString(sb, value, true);
		//	sb.append(");\n");
		//}

		sb.append("};\n");
		appendCreateJS(sb);
	}

	@Override public void renderJavascriptState(StringBuilder sb) throws Exception {
		updateTheme();
		updateMode();
		updateValue();
		updateTabSize();

		super.renderJavascriptState(sb);
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
		if(isBuilt()) {
			StringBuilder sb = new StringBuilder();
			if(null == value)
				value = "";
			callStringMethod("setValue", value);
		} else {
			forceRebuild();
		}
	}

	private void callStringMethod(String methodName, String value) {
		StringBuilder sb = new StringBuilder();
		handle(sb).append(methodName).append("(");
		if(null == value)
			sb.append("null");
		else
			StringTool.strToJavascriptString(sb, value, true);
		sb.append(");\n");
		appendJavascript(sb);
	}

	private void callSessionStringMethod(String methodName, String value) {
		StringBuilder sb = new StringBuilder();
		handle(sb).append("getSession().").append(methodName).append("(");
		if(null == value)
			sb.append("null");
		else
			StringTool.strToJavascriptString(sb, value, true);
		sb.append(");\n");
		appendJavascript(sb);
	}

	private void callMethod(String methodName, String... args) {
		StringBuilder sb = new StringBuilder();
		handle(sb).append(methodName).append("(");
		int count = 0;
		for(String arg : args) {
			if(count++ > 0)
				sb.append(",");
			sb.append(arg);
		}
		sb.append(");\n");
		appendJavascript(sb);
	}

	@Nullable public String getTheme() {
		return m_theme;
	}

	public void setTheme(@Nullable String theme) {
		if(Objects.equals(theme, m_theme))
			return;
		m_theme = theme;
		if(isBuilt()) {
			updateTheme();
		}
	}

	private void updateTheme() {
		String theme = m_theme;
		if(null == theme) {
			theme = "ace/theme/monokai";
		}
		callStringMethod("setTheme", theme);
	}

	private void updateMode() {
		String mode = m_mode;
		if(null == mode)
			mode = "ace/mode/javascript";
		callSessionStringMethod("setMode", mode);
	}

	private void updateValue() {
		String value = m_value;
		if(null == value)
			value = "";
		callStringMethod("setValue", value);
	}

	private void updateTabSize() {
		if(m_tabSize > 0 && m_tabSize < 20)
			callMethod("getSession().setTabSize", Integer.toString(m_tabSize));
	}

	private void updateReadOnly() {
		callMethod("setReadOnly", Boolean.valueOf(m_internalRo).toString());
	}

	@Nullable public String getMode() {
		return m_mode;
	}

	public void setMode(@Nullable String mode) {
		if(Objects.equals(mode, m_mode))
			return;
		m_mode = mode;
		if(isBuilt())
			updateMode();
	}

	public int getTabSize() {
		return m_tabSize;
	}

	public void setTabSize(int tabSize) {
		if(m_tabSize == tabSize)
			return;
		m_tabSize = tabSize;
		if(! isBuilt())
			updateTabSize();
	}

	private StringBuilder handle(StringBuilder sb) {
		sb.append("window['").append(getActualID()).append("'].");
		return sb;
	}

	public void gotoLine(int line) {
		if(! isBuilt())
			m_gotoLine = line;
		else {
			callMethod("gotoLine", Integer.toString(line));
		}
	}

	public static String getVersion() {
		return m_version;
	}

	public static void setVersion(String version) {
		m_version = version;
	}

	public int getGotoLine() {
		return m_gotoLine;
	}

	public void setGotoLine(int gotoLine) {
		m_gotoLine = gotoLine;
	}

	public boolean isDisabled() {
		return m_disabled;
	}

	public void setDisabled(boolean disabled) {
		m_disabled = disabled;
		updateInternalRo(m_disabled || m_readOnly);
	}

	private void updateInternalRo(boolean ro) {
		if(m_internalRo == ro)
			return;

		m_internalRo = ro;
		if(isBuilt())
			updateReadOnly();
	}

	public boolean isReadOnly() {
		return m_readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		m_readOnly = readOnly;
		updateInternalRo(m_disabled || m_readOnly);
	}
}
