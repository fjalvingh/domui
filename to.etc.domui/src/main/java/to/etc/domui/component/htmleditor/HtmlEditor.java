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
package to.etc.domui.component.htmleditor;

import to.etc.domui.dom.css.*;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.util.*;

import javax.annotation.*;

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
public class HtmlEditor extends TextArea {
	private String m_styleSheet;

	private String m_updateValueJS;

	public HtmlEditor() {}

	public HtmlEditor(int cols, int rows) {
		super(cols, rows);
	}

	@Override
	public void createContent() throws Exception {
		StringBuilder sb = new StringBuilder();
		appendJQuerySelector(sb);
		sb.append(".wysiwyg({css:");
		String css = getStyleSheet();
		StringTool.strToJavascriptString(sb, css, false);

		sb.append(", controls: {");
		disable(sb, "separator05");
		sb.append(",");
		disable(sb, "createLink");
		sb.append(",");
		disable(sb, "insertImage");
		sb.append(",");
		disable(sb, "separator06");
		sb.append(",");
		disable(sb, "h1mozilla");
		sb.append(",");
		disable(sb, "h2mozilla");
		sb.append(",");
		disable(sb, "h3mozilla");
		sb.append(",");
		disable(sb, "h1");
		sb.append(",");
		disable(sb, "h2");
		sb.append(",");
		disable(sb, "h3");
		sb.append(",");
		disable(sb, "separator08");
		sb.append(",");
		disable(sb, "separator09");
		sb.append(",");
		disable(sb, "unLink");
		sb.append(",");
		enable(sb, "highlight");
		//		sb.append(",");
		//		disable(sb, "");
		//		sb.append(",");
		//		disable(sb, "");
		//		sb.append(",");
		//		disable(sb, "");
		//		sb.append(",");
		sb.append("}");

		sb.append(", options: {");
		sb.append("autosave: false");
		sb.append("}");

		sb.append(", plugins: {");
		sb.append("rmFormat: {rmMsWordMarkup: {enabled: true, rules: {inlineCSS: true}}}");
		sb.append("}");

		sb.append(", initialContent: ''");
		sb.append("});");

		if(isFocusRequested()) {
			sb.append("setTimeout(function() {");
//			sb.append("xxxFocus('#").append(getActualID()).append("');");
			appendJQuerySelector(sb);
			sb.append(".focus();");
//			sb.append("alert('focused');");
			sb.append("}, 500);");
		}

		appendCreateJS(sb);
		//		appendCreateJS("$(\"#" + getActualID() + "\").wysiwyg({css:'/ui/$themes/blue/style.theme.css'});");

		handleError();
	}

	static private void disable(StringBuilder sb, String what) {
		sb.append(what).append(": {visible:false}");
	}

	static private void enable(StringBuilder sb, String what) {
		sb.append(what).append(": {visible:true}");
	}

	/**
	 * Contains the in-editor stylesheet to use, which determines the presentation\
	 * of the document inside the editor. If not set it defaults to
	 * THEME/minieditor.css.
	 * @return
	 */
	protected String getStyleSheet() throws Exception {
		return getThemedResourceRURL(m_styleSheet == null ? "THEME/minieditor.css" : m_styleSheet);
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

	/**
	 * Set a new HTML document value into the area. If the page is built we need to call a method on
	 * the editor object so that it will update it's presentation.
	 * @see to.etc.domui.dom.html.TextArea#setValue(java.lang.String)
	 */
	@Override
	public void setValue(@Nullable String v) {
//		System.out.println("setValue: " + v);
		if(null != v) {
			v = HtmlUtil.removeUnsafe(v);
		}

		if(isBuilt()) {
			//-- Leave a marker to set the value through Javascript too.
			m_updateValueJS = v;
		}

		super.setValue(v);
	}

	@Override
	public void onBeforeRender() throws Exception {
		if(null != m_updateValueJS) {
			StringBuilder sb = new StringBuilder();
			appendJQuerySelector(sb);
			sb.append(".wysiwyg('setContent', ");
			StringTool.strToJavascriptString(sb, m_updateValueJS, true);
			sb.append(");");
			appendJavascript(sb);
			m_updateValueJS = null;
		}
	}

	@Override
	public boolean acceptRequestParameter(@Nonnull String[] values) throws Exception {
		if(isDisabled()) {
			return false;
		}
		setDisplay(DisplayType.NONE);
		for(int i = 0; i < values.length; i++) {
			String s = values[i];
			try {
				System.out.println("pre-value[" + i + "]=" + s);
				values[i] = HtmlUtil.removeUnsafe(s);
//
//
//				StringTool.entitiesToUnicode(sb, s, true);
//				String tmp = sb.toString();
//				System.out.println("pre-value[" + i + "]=" + tmp);
//				sb.setLength(0);
//
//
//				DomUtil.htmlRemoveUnsafe(sb, tmp);
//				values[i] = sb.toString();
				System.out.println("post-value[" + i + "]=" + values[i]);
			} catch(Exception e) {
				e.printStackTrace();
				values[i] = e.toString();
			}
		}
		return super.acceptRequestParameter(values);
	}

	@Nullable @Override public UIMessage setMessage(@Nullable UIMessage msg) {
		UIMessage uiMessage = super.setMessage(msg);
		handleError();
		return uiMessage;
	}

	private void handleError() {
		UIMessage message = getMessage();
		if(null == message) {
			//removeCssClass("ui-input-err");
			appendJavascript("$('#" + getActualID() + "-wysiwyg-iframe').removeClass('ui-input-err');");
		} else {
			//addCssClass("ui-input-err");
			appendJavascript("$('#" + getActualID() + "-wysiwyg-iframe').addClass('ui-input-err');");
		}
	}
}
