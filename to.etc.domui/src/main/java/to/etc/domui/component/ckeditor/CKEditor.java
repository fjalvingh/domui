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
package to.etc.domui.component.ckeditor;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.component.htmleditor.IEditorFileSystem;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.component.misc.MsgBox.IAnswer;
import to.etc.domui.component.misc.MsgBox.Type;
import to.etc.domui.component.misc.MsgBoxButton;
import to.etc.domui.component.misc.OddCharacters;
import to.etc.domui.dom.css.DisplayType;
import to.etc.domui.dom.css.VisibilityType;
import to.etc.domui.dom.header.HeaderContributor;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Page;
import to.etc.domui.dom.html.TextArea;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.RequestContextImpl;
import to.etc.domui.server.XssChecker;
import to.etc.domui.state.IPageParameters;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.javascript.JavascriptStmt;
import to.etc.util.StringTool;
import to.etc.webapp.nls.NlsContext;

import java.util.Objects;

/**
 * This represents a CKEditor instance.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 30, 2008
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Refactored on Oct 07, 2011
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Refactored on Dec 07, 2013 - made it for CKEditor
 */
public class CKEditor extends Div implements IControl<String> {
	static private final Logger LOG = LoggerFactory.getLogger(CKEditor.class);

	private final CkEditorArea m_area = new CkEditorArea();

	@Nullable
	private String m_vn;

	@NonNull
	private CKToolbarSet m_toolbarSet = CKToolbarSet.DOMUI;

	private IEditorFileSystem m_fileSystem; //not in use?

	@Nullable
	private IClicked<NodeBase> m_onDomuiImageClicked;

	@Nullable
	private IClicked<NodeBase> m_onDomuiOddCharsClicked;

	@NonNull
	private static final String WEBUI_CK_DOMUIIMAGE_ACTION = "CKIMAGE";

	@NonNull
	private static final String WEBUI_CK_DOMUIODDCHAR_ACTION = "CKODDCHAR";

	private boolean m_toolbarStartExpanded = true;

	@Nullable
	private String m_internalWidth;

	@Nullable
	private String m_internalHeight;

	@Nullable
	private IValueChanged<?> m_onValueChanged;

	public CKEditor() {
		setCssClass("ui-cked");
		m_area.setCssClass("ui-ckeditor");
		m_area.setVisibility(VisibilityType.HIDDEN);
	}

	static public void initialize(UrlPage page) {
		page.getPage().addHeaderContributor(HeaderContributor.loadJavascript("$ckeditor/ckeditor.js"), -760);
	}


	/**
	 * <p>To create the editor we need to replace the core code. We add a textarea having the ID and a
	 * special class (ui-ckeditor). This special class is an indicator to the submit logic that the textarea
	 * is a CKEditor instance. This causes it to use special logic to retrieve a value.</p>
	 */
	@Override
	public void createContent() throws Exception {
		add(m_area);
		String areaID = m_area.getActualID();
		StringBuilder sb = new StringBuilder(1024);
		m_vn = "_ck" + getActualID();

		sb.append("\nvar ").append(m_vn).append(" = CKEDITOR.replace('").append(areaID).append("', {");
		sb.append("customConfig: '").append(DomUtil.getRelativeApplicationResourceURL("$ckeditor/domuiconfig.js")).append("'\n");
		sb.append(",on: {instanceReady: function(ev) {WebUI.CKeditor_OnComplete('" + areaID + "');}}\n");

		String dl = NlsContext.getLocale().getLanguage();
		if(dl.contains("nl"))
			dl = "nl";
		else if(dl.contains("en"))
			dl = "en";
		sb.append(", defaultLanguage:'").append(dl).append("'\n");

		if(!isToolbarStartExpanded()) {
			sb.append(", toolbarStartupExpanded: false\n");
			sb.append(", toolbarCanCollapse: true\n");
		} else {
			sb.append(", toolbarCanCollapse: false\n");
		}
		sb.append(", resize_enabled: false\n");

		sb.append(", toolbar: '" + m_toolbarSet.name() + "'\n");
		switch (m_toolbarSet) {
			case DOMUI:
			case FULL:
				sb.append(", extraPlugins : 'domuiimage,domuioddchar,justify,colorbutton,smiley,font'\n");
				break;
			case BASIC:
			case TXTONLY:
			default:
				break;
		}

		String color = "#5689E6";

		sb.append(", uiColor: '" + color + "'\n");

		String s = m_internalWidth;
		if(null != s) {
			sb.append(", width:'").append(s).append("'\n");
			sb.append(", _setWidth:'").append(s).append("'\n");
		}

		s = m_internalHeight;
		if(null != s) {
			sb.append(", height:'").append(s).append("'\n");
			sb.append(", _setHeight:'").append(s).append("'\n");
		}

		//-- Finish.
		sb.append("});\n");

		sb.append("WebUI.registerCkEditorId('" + areaID + "', " + m_vn + ");");
		appendCreateJS(sb);
		m_area.setDisplay(DisplayType.NONE);
	}

	@Override protected void renderJavascriptState(JavascriptStmt b) throws Exception {
		String value = m_area.internalGetValue();
		if(null == value)
			value = "";
		appendJavascript("WebUI.CKeditor_setValue('" + m_area.getActualID() + "'," + StringTool.strToJavascriptString(value, true) + ");\n");
		super.renderJavascriptState(b);
	}

	//@Override
	//public void setWidth(@Nullable String width) {
	//	m_internalWidth = width;
	//}
	//
	//@Override
	//public void setHeight(@Nullable String height) {
	//	m_internalHeight = height;
	//}

	private void appendOption(@NonNull final StringBuilder sb, @NonNull final String option, @NonNull final String value) {
		sb.append(m_vn).append(".").append(option).append(" = ");
		try {
			StringTool.strToJavascriptString(sb, value, true);
		} catch(Exception x) {
		}
		sb.append(";");
	}

	@NonNull
	public CKToolbarSet getToolbarSet() {
		return m_toolbarSet;
	}

	public void setToolbarSet(@NonNull final CKToolbarSet toolbarSet) {
		if(DomUtil.isEqual(toolbarSet, m_toolbarSet))
			return;
		m_toolbarSet = toolbarSet;
		if(!isBuilt())
			return;
		StringBuilder sb = new StringBuilder();
		appendOption(sb, "toolbar", m_toolbarSet.name());
		appendJavascript("");
	}

	@Nullable
	public IEditorFileSystem getFileSystem() {
		return m_fileSystem;
	}

	public void setFileSystem(final @Nullable IEditorFileSystem fileSystem) {
		m_fileSystem = fileSystem;
	}


	@Nullable
	public IClicked<NodeBase> getOnDomuiImageClicked() {
		return m_onDomuiImageClicked;
	}

	public void setOnDomuiImageClicked(@NonNull IClicked<NodeBase> onDomuiImageClicked) {
		m_onDomuiImageClicked = onDomuiImageClicked;
	}

	@Nullable
	public IClicked<NodeBase> getOnDomuiOddCharsClicked() {
		return m_onDomuiOddCharsClicked;
	}

	public void setOnDomuiOddCharsClicked(@NonNull IClicked<NodeBase> onDomuiOddCharsClicked) {
		m_onDomuiOddCharsClicked = onDomuiOddCharsClicked;
	}

	public boolean isToolbarStartExpanded() {
		return m_toolbarStartExpanded;
	}

	public void setToolbarStartExpanded(boolean toolbarStartExpanded) {
		m_toolbarStartExpanded = toolbarStartExpanded;
	}

	@Override
	public void onRemoveFromPage(Page p) {
		super.onRemoveFromPage(p);
		appendJavascript("WebUI.unregisterCkEditorId('" + getActualID() + "');");
	}

	/**
	 * Needed in cases when page layout is changed without resizing browser window.
	 */
	public void appendfixSizeJS() {
		appendJavascript("WebUI.CKeditor_OnComplete('" + getActualID() + "');");
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	IControl interface											*/
	/*----------------------------------------------------------------------*/

	@Override public void setValue(String value) {
		if(Objects.equals(m_area.internalGetValue(), value))
			return;
		m_area.setValue(value);
		if(null == value)
			value = "";
		appendJavascript("WebUI.CKeditor_setValue('" + m_area.getActualID() + "'," + StringTool.strToJavascriptString(value, true) + ");\n");
	}

	@Override public String getValue() {
		return m_area.getValue();
	}

	@Override public boolean isReadOnly() {
		return m_area.isReadOnly();
	}

	@Override public void setReadOnly(boolean ro) {
		m_area.setReadOnly(ro);
	}

	@Override public boolean isDisabled() {
		return m_area.isDisabled();
	}

	@Override public void setDisabled(boolean d) {
		m_area.setDisabled(d);
	}

	@Override public boolean isMandatory() {
		return m_area.isMandatory();
	}

	@Override public void setMandatory(boolean ro) {
		m_area.setMandatory(ro);
	}

	@Override public NodeBase getForTarget() {
		return null;
	}

	@Override public IValueChanged<?> getOnValueChanged() {
		return m_onValueChanged;
	}

	@Override public void setOnValueChanged(IValueChanged<?> onValueChanged) {
		m_onValueChanged = onValueChanged;
		if(onValueChanged == null) {
			m_area.setOnValueChanged(null);
		} else {
			m_area.setOnValueChanged(component -> {
				((IValueChanged<CKEditor>)onValueChanged).onValueChanged(this);
			});
		}
	}

	/**
	 * Bind-capable version of getValue(). If called (usually from binding) this will act as follows:
	 * <ul>
	 * 	<li>If this component has an input error: throw the ValidationException for that error</li>
	 * 	<li>On no error this returns the value.</li>
	 * </ul>
	 * @return
	 */
	@Nullable
	public String getBindValue() {
		return m_area.getBindValue();
	}

	public void setBindValue(@Nullable String value) {
		if(MetaManager.areObjectsEqual(m_area.internalGetValue(), value)) {
			return;
		}
		setValue(value);
	}

	final private class CkEditorArea extends TextArea {
		@Override public void setValue(String v) {
			internalSetValue(v);
		}

		@Override
		public String getValue() {
			return internalGetValue();
		}

		/**
		 * Handle {@link CKEditor#WEBUI_CK_DOMUIIMAGE_ACTION} activity on CKEditor customized commands that interacts with domui.
		 * Handle {@link CKEditor#WEBUI_CK_DOMUIODDCHAR_ACTION} activity on CKEditor customized commands that interacts with domui.
		 *
		 * @see to.etc.domui.dom.html.Div#componentHandleWebAction(to.etc.domui.server.RequestContextImpl, java.lang.String)
		 */
		@Override
		public void componentHandleWebAction(@NonNull RequestContextImpl ctx, @NonNull String action) throws Exception {
			if(WEBUI_CK_DOMUIIMAGE_ACTION.equals(action))
				selectImage(ctx);
			else if(WEBUI_CK_DOMUIODDCHAR_ACTION.equals(action))
				oddChars(ctx);
			else
				super.componentHandleWebAction(ctx, action);
		}

		private void selectImage(@NonNull RequestContextImpl ctx) throws Exception {
			IClicked<NodeBase> clicked = m_onDomuiImageClicked;
			if(clicked == null) {
				MsgBox.message(this, Type.ERROR, "No image picker is defined", new IAnswer() {
					@Override
					public void onAnswer(MsgBoxButton result) throws Exception {
						renderCancelImage();
					}
				});
			} else {
				clicked.clicked(this);
			}
		}

		private void oddChars(@NonNull RequestContextImpl ctx) throws Exception {
			IClicked<NodeBase> clicked = m_onDomuiOddCharsClicked;
			if(clicked == null) {
				//if no other handler is specified we show framework default OddCharacters dialog
				OddCharacters oddChars = new OddCharacters();
				oddChars.setOnClose(closeReason -> renderCloseOddCharacters());
				getPage().getBody().add(oddChars);
			} else {
				clicked.clicked(this);
			}
		}

		public void renderImageSelected(@NonNull String url) {
			appendJavascript("CkeditorDomUIImage.addImage('" + getActualID() + "', '" + url + "');");
		}

		public void renderCancelImage() {
			appendJavascript("CkeditorDomUIImage.cancel('" + getActualID() + "');");
		}

		public void renderCloseOddCharacters() {
			appendJavascript("CkeditorDomUIOddChar.cancel('" + getActualID() + "');");
		}

		public void renderOddCharacters(@NonNull String input) {
			appendJavascript("CkeditorDomUIOddChar.addString('" + getActualID() + "', '" + input + "');");
		}

		/**
		 * Accept the data from the editor. We need special handling because the html editor
		 * can have image tags what would normally be filtered by the XSS handlers. So in here
		 * we use the raw, unfiltered data and handle the xss filtering by ourselves.
		 */
		@Override
		public boolean acceptRequestParameter(@NonNull String[] cookedValues, @NonNull IPageParameters allParameters) throws Exception {
			if(isDisabled()) {
				return false;
			}
			String[] rawValues = allParameters.getRawUnsafeParameterValues(getActualID());
			if(null == rawValues || rawValues.length == 0) {
				return super.acceptRequestParameter(cookedValues);
			}

			String[] newValues = new String[rawValues.length];
			StringBuilder sb = new StringBuilder();
			XssChecker xssChecker = DomApplication.get().getXssChecker();
			for(int i = 0; i < rawValues.length; i++) {
				String s = rawValues[i];
				try {
					sb.setLength(0);
					StringTool.entitiesToUnicode(sb, s, true);
					s = xssChecker.stripXSS(sb.toString(), XssChecker.F_ALLOWLOCALSRC);
					newValues[i] = s;
				} catch(Exception e) {
					LOG.error("Error stripping XSS", e);
					newValues[i] = "";
				}
			}
			return super.acceptRequestParameter(newValues);
		}
	}


	@Override public void setHint(String hintText) {
		//-- For CKEditor having a hint seems unwanted.
		//setTitle(hintText);
	}
}
