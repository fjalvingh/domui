package to.etc.domui.component.fasthtml;

import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;
import to.etc.util.*;

/**
 * This is a small but very fast html editor. It shows way faster than
 * the full Html editor but has a little less options. This uses a slightly
 * adapter version of the <a href="http://code.google.com/p/jwysiwyg/">wysigyg</a>
 * plugin.
 *
 * <p>One oddity in the code here is the handling of the "display" css property. The
 * plugin adds a div just before the original textarea, then it makes the textarea
 * display: none. The textarea is retained so the plugin can put it's content in there
 * still. DomUI however will reset the display:none value after a value is entered
 * because the changeAttribute call sent will clear it (the attribute is BLOCK in
 * the DomUI DOM). To prevent this we set the attribute to BLOCK on a full render and
 * reset it back no none as soon as a partial delta is to be rendered by listening
 * for input on this control.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 19, 2010
 */
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

	/**
	 * Contains the in-editor stylesheet to use, which determines the presentation\
	 * of the document inside the editor. If not set it defaults to
	 * THEME/minieditor.css.
	 * @return
	 */
	public String getStyleSheet() {
		return PageContext.getRequestContext().translateResourceName(m_styleSheet);
	}

	public void setStyleSheet(String styleSheet) {
		if(DomUtil.isEqual(styleSheet, m_styleSheet))
			return;
		m_styleSheet = styleSheet;
		changed();
	}

	@Override
	public void onBeforeFullRender() throws Exception {
		setDisplay(DisplayType.BLOCK);
	}

	@Override
	public boolean acceptRequestParameter(String[] values) throws Exception {
		setDisplay(DisplayType.NONE);
		return super.acceptRequestParameter(values);
	}
}
