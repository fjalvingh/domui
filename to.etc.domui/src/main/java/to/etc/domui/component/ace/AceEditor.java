package to.etc.domui.component.ace;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.errors.MsgType;
import to.etc.domui.dom.header.HeaderContributor;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.parts.IComponentJsonProvider;
import to.etc.domui.state.IPageParameters;
import to.etc.util.FileTool;
import to.etc.util.StringTool;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * DomUI wrapper for the <a href="https://ace.c9.io/">ACE code editor</a>.
 *
 * Todo:
 * <pre>
 *     https://github.com/ajaxorg/ace/wiki/How-to-enable-Autocomplete-in-the-Ace-editor
 * </pre>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 23-12-17.
 */
public class AceEditor extends Div implements IControl<String>, IComponentJsonProvider {
	static private String m_version = "1.2.9";

	private int m_nextId;

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

	private boolean m_mandatory;

	@Nullable
	private IValueChanged<?> m_valueChanged;

	private Set<Marker> m_markerSet = new HashSet<>();

	private Set<Marker> m_oldMarkerSet = new HashSet<>();


	/**
	 * A completion result for the ICompletionHandler interface.
	 */
	public static class Completion {
		private final String m_name;
		private final String m_value;
		private final String m_meta;
		private final int m_score;

		public Completion(String name, String value, String meta, int score) {
			m_name = name;
			m_value = value;
			m_meta = meta;
			m_score = score;
		}

		public String getName() {
			return m_name;
		}

		public String getValue() {
			return m_value;
		}

		public String getMeta() {
			return m_meta;
		}

		public int getScore() {
			return m_score;
		}
	}

	@NonNullByDefault
	public static final class Marker {
		private final int m_id;

		private final MsgType m_type;

		private final String m_message;

		private final int m_line;

		private final int m_column;

		@Nullable
		private final String m_cssClass;

		public Marker(int id, MsgType type, String message, int line, int column, @Nullable String cssClass) {
			m_id = id;
			m_type = type;
			m_message = message;
			m_line = line;
			m_column = column;
			m_cssClass = cssClass;
		}

		public int getId() {
			return m_id;
		}

		public MsgType getType() {
			return m_type;
		}

		public String getMessage() {
			return m_message;
		}

		public int getLine() {
			return m_line;
		}

		public int getColumn() {
			return m_column;
		}

		@Nullable
		public String getCssClass() {
			return m_cssClass;
		}

		@Override public boolean equals(@Nullable Object o) {
			if(this == o)
				return true;
			if(o == null || getClass() != o.getClass())
				return false;
			Marker marker = (Marker) o;
			return m_line == marker.m_line &&
				m_column == marker.m_column &&
				m_type == marker.m_type &&
				m_message.equals(marker.m_message) &&
				Objects.equals(m_cssClass, marker.m_cssClass);
		}

		@Override public int hashCode() {
			return Objects.hash(m_type, m_message, m_line, m_column, m_cssClass);
		}
	}

	public interface ICompletionHandler {
		@NonNull
		List<Completion> getCompletions(@NonNull String text, int row, int col, @NonNull String prefix) throws Exception;
	}

	@Nullable
	private ICompletionHandler m_completionHandler;

	@Override public void createContent() throws Exception {
		getPage().addHeaderContributor(HeaderContributor.loadJavascript("https://cdnjs.cloudflare.com/ajax/libs/ace/" + m_version + "/ace.js"), 10);

		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		sb.append("let ed = ace.edit('").append(getActualID()).append("');\n");
		sb.append("var Range = require('ace/range').Range;\n");
		sb.append("window['").append(getActualID()).append("'] = ed;\n");
		sb.append("WebUI.registerInputControl('").append(getActualID()).append("', {getInputField: function() {");
		sb.append(" return ed.getValue();\n");
		sb.append("},"
			+ "onVisibilityChanged: function() {"
			+ "  ed.resize();"
			//+ "  alert('rezi');"
			+ "}"
			+ "});\n");

		if(null != getOnValueChanged()) {
			sb.append("ed.getSession().on('change', function() {\n");
			sb.append(" if(ed.__utimer) {\n");
			sb.append("  clearTimeout(ed.__utimer);\n");
			sb.append("  delete ed.__utimer;\n");
			sb.append("}\n;");

			sb.append("  ed.__utimer = setTimeout(function() {\n");
			sb.append("    delete ed.__utimer;\n");
			sb.append("    WebUI.valuechanged('', '").append(getActualID()).append("');\n");

			sb.append("}, 500);\n");

			sb.append("});");
		}

		//-- Autocomplete?
		ICompletionHandler ch = getCompletionHandler();
		if(null != ch) {
			String js = FileTool.readResourceAsString(getClass(), "/resources/ace/acecompletion.js", "utf-8");
			sb.append(js.replace("$ID$", getActualID()));
		}

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

	@Override public Object provideJsonData(IPageParameters parameterSource) throws Exception {
		int col = parameterSource.getInt(getActualID() + "_col");
		int row = parameterSource.getInt(getActualID() + "_row");
		String req = parameterSource.getString(getActualID() + "_prefix");
		ICompletionHandler ch = getCompletionHandler();
		String value = m_value;
		if(ch == null || req == null || value == null) {
			return Collections.emptyList();
		}

		return ch.getCompletions(value, row, col, req);
	}

	@Override public void renderJavascriptState(StringBuilder sb) throws Exception {
		updateTheme();
		updateMode();
		updateValue();
		updateTabSize();
		updateReadOnly();

		super.renderJavascriptState(sb);
	}

	static public void initialize(UrlPage page) {
		page.getPage().addHeaderContributor(HeaderContributor.loadJavascript("https://cdnjs.cloudflare.com/ajax/libs/ace/" + m_version + "/ace.js"), 10);
		page.getPage().addHeaderContributor(HeaderContributor.loadJavascript("https://cdnjs.cloudflare.com/ajax/libs/ace/" + m_version + "/ext-language_tools.js"), 11);
	}

	@Override
	@Nullable public String getValue() {
		return m_value;
	}

	@Override
	public void setValue(@Nullable String value) {
		if(Objects.equals(value, m_value))
			return;
		m_value = value;
		if(isBuilt()) {
			StringBuilder sb = new StringBuilder();
			if(null == value)
				value = "";
			callStringMethod("setValue", value);
			callMethod("clearSelection");
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
		callMethod("clearSelection");
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

	public void gotoLine(int line, int col) {
		if(! isBuilt())
			m_gotoLine = line;
		else {
			callMethod("gotoLine", Integer.toString(line), Integer.toString(col));
		}
	}

	public void selectWord(int line, int col) {
		if(isBuilt()) {
			callMethod("selection.getWordRange", Integer.toString(line), Integer.toString(col));
		}
	}

	public void select(int line, int col, int line2, int col2) {
		if(isBuilt()) {
			line--;
			line2--;
			appendJavascript("var Range = require('ace/range').Range;\n");
			callMethod("selection.setRange", "new Range(" + line + "," + col + ", " + line2 + "," + col2 + ")", "true");
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

	@Override
	public boolean isDisabled() {
		return m_disabled;
	}

	@Override
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

	@Override
	public boolean isReadOnly() {
		return m_readOnly;
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		m_readOnly = readOnly;
		updateInternalRo(m_disabled || m_readOnly);
	}

	@Override public boolean isMandatory() {
		return m_mandatory;
	}

	@Override public void setMandatory(boolean mandatory) {
		m_mandatory = mandatory;
	}

	@Override public IValueChanged<?> getOnValueChanged() {
		return m_valueChanged;
	}

	@Override public void setOnValueChanged(IValueChanged<?> onValueChanged) {
		m_valueChanged = onValueChanged;
	}

	/**
	 * The editor does not support being used as a label for= target.
	 */
	@Nullable @Override public NodeBase getForTarget() {
		return null;
	}

	@Override public boolean acceptRequestParameter(@NonNull String[] values) throws Exception {
		if(values.length != 1)
			throw new IllegalStateException("? Expecting but one value?");
		String value = values[0];
		m_value = value;
		return true;
	}

	public ICompletionHandler getCompletionHandler() {
		return m_completionHandler;
	}

	public void setCompletionHandler(ICompletionHandler completionHandler) {
		m_completionHandler = completionHandler;
	}

	public void markerClear() {
		m_markerSet.clear();
	}

	public Marker markerAdd(MsgType sev, int line, int col, String message, @Nullable String css) {
		Marker marker = new Marker(m_nextId++, sev, message, line, col, css);
		if(! m_markerSet.contains(marker))
			m_markerSet.add(marker);
		return marker;
	}

	public void markerRemove(Marker m) {
		m_markerSet.remove(m);
	}
}
