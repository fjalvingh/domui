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

import to.etc.domui.component.htmleditor.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.component.misc.MsgBox.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;
import to.etc.util.*;
import to.etc.webapp.nls.*;

import javax.annotation.*;

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
public class CKEditor extends TextArea {
	@Nullable
	private String m_vn;

	@Nonnull
	private CKToolbarSet m_toolbarSet = CKToolbarSet.DOMUI;

	private IEditorFileSystem m_fileSystem; //not in use?

	@Nullable
	private IClicked<NodeBase> m_onDomuiImageClicked;

	@Nullable
	private IClicked<NodeBase> m_onDomuiOddCharsClicked;

	@Nonnull
	private static final String WEBUI_CK_DOMUIIMAGE_ACTION = "CKIMAGE";

	@Nonnull
	private static final String WEBUI_CK_DOMUIODDCHAR_ACTION = "CKODDCHAR";

	private boolean m_toolbarStartExpanded = true;

	@Nullable
	private String m_internalWidth;

	@Nullable
	private String m_internalHeight;

	public CKEditor() {
		super.setCssClass("ui-ckeditor");
		setVisibility(VisibilityType.HIDDEN);
	}

	@Override
	final public void setCssClass(final @Nullable String cssClass) {
		throw new IllegalStateException("Cannot set a class on CKEditor");
	}

	/**
	 * <p>To create the editor we need to replace the core code. We add a textarea having the ID and a
	 * special class (ui-ckeditor). This special class is an indicator to the submit logic that the textarea
	 * is a CKEditor instance. This causes it to use special logic to retrieve a value.</p>
	 *
	 * @see to.etc.domui.dom.html.NodeBase#createContent()
	 */
	@Override
	public void createContent() throws Exception {
		StringBuilder sb = new StringBuilder(1024);
		m_vn = "_ck" + getActualID();


		sb.append("var ").append(m_vn).append(" = CKEDITOR.replace('").append(getActualID()).append("', {");
		sb.append("customConfig: '").append(DomUtil.getRelativeApplicationResourceURL("$ckeditor/domuiconfig.js")).append("'\n");
		sb.append(",on: {instanceReady: function(ev) {WebUI.CKeditor_OnComplete('" + getActualID() + "');}}\n");

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

		//String color = DomApplication.get().getThemeMap().getValue(String.class, "dt_rowhdr_bg"); //FIXME btadic lf :see if that matches in all themes nicely.
		String color = "#5689E6";

		sb.append(", uiColor: '" + color + "'\n");

		String s = m_internalWidth;
		if(null != s)
			sb.append(", width:'").append(s).append("'\n");

		s = m_internalHeight;
		if(null != s)
			sb.append(", height:'").append(s).append("'\n");

		//-- Finish.
		sb.append("});\n");

		sb.append("WebUI.registerCkEditorId('" + getActualID() + "', " + m_vn + ");");
		appendCreateJS(sb);
		setDisplay(DisplayType.NONE);
	}

	@Override
	public void setWidth(@Nullable String width) {
		m_internalWidth = width;
	}

	@Override
	public void setHeight(@Nullable String height) {
		m_internalHeight = height;
	}

	private void appendOption(@Nonnull final StringBuilder sb, @Nonnull final String option, @Nonnull final String value) {
		sb.append(m_vn).append(".").append(option).append(" = ");
		try {
			StringTool.strToJavascriptString(sb, value, true);
		} catch(Exception x) {
			x.printStackTrace(); // Checked exceptions are idiotic
		}
		sb.append(";");
	}

	@Nonnull
	public CKToolbarSet getToolbarSet() {
		return m_toolbarSet;
	}

	public void setToolbarSet(@Nonnull final CKToolbarSet toolbarSet) {
		if(DomUtil.isEqual(toolbarSet, m_toolbarSet))
			return;
		m_toolbarSet = toolbarSet;
		if(!isBuilt())
			return;
		StringBuilder sb = new StringBuilder();
		appendOption(sb, "toolbar", m_toolbarSet.name());
		appendJavascript("");
	}

	public @Nullable
	IEditorFileSystem getFileSystem() {
		return m_fileSystem;
	}

	public void setFileSystem(final @Nullable IEditorFileSystem fileSystem) {
		m_fileSystem = fileSystem;
	}

	/**
	 * Handle {@link CKEditor#WEBUI_CK_DOMUIIMAGE_ACTION} activity on CKEditor customized commands that interacts with domui.
	 * Handle {@link CKEditor#WEBUI_CK_DOMUIODDCHAR_ACTION} activity on CKEditor customized commands that interacts with domui.
	 *
	 * @see to.etc.domui.dom.html.Div#componentHandleWebAction(to.etc.domui.server.RequestContextImpl, java.lang.String)
	 */
	@Override
	public void componentHandleWebAction(@Nonnull RequestContextImpl ctx, @Nonnull String action) throws Exception {
		if(WEBUI_CK_DOMUIIMAGE_ACTION.equals(action))
			selectImage(ctx);
		else if(WEBUI_CK_DOMUIODDCHAR_ACTION.equals(action))
			oddChars(ctx);
		else
			super.componentHandleWebAction(ctx, action);
	}

	private void selectImage(@Nonnull RequestContextImpl ctx) throws Exception {
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

	private void oddChars(@Nonnull RequestContextImpl ctx) throws Exception {
		IClicked<NodeBase> clicked = m_onDomuiOddCharsClicked;
		if(clicked == null) {
			//if no other handler is specified we show framework default OddCharacters dialog
			OddCharacters oddChars = new OddCharacters();
			oddChars.setOnClose(new IWindowClosed() {

				@Override
				public void closed(@Nonnull String closeReason) throws Exception {
					CKEditor.this.renderCloseOddCharacters();
				}
			});
			getPage().getBody().add(oddChars);
		} else {
			clicked.clicked(this);
		}
	}

	public void renderImageSelected(@Nonnull String url) {
		appendJavascript("CkeditorDomUIImage.addImage('" + getActualID() + "', '" + url + "');");
	}

	public void renderCancelImage() {
		appendJavascript("CkeditorDomUIImage.cancel('" + getActualID() + "');");
	}

	public void renderCloseOddCharacters() {
		appendJavascript("CkeditorDomUIOddChar.cancel('" + getActualID() + "');");
	}

	public void renderOddCharacters(@Nonnull String input) {
		appendJavascript("CkeditorDomUIOddChar.addString('" + getActualID() + "', '" + input + "');");
	}

	@Nullable
	public IClicked<NodeBase> getOnDomuiImageClicked() {
		return m_onDomuiImageClicked;
	}

	public void setOnDomuiImageClicked(@Nonnull IClicked<NodeBase> onDomuiImageClicked) {
		m_onDomuiImageClicked = onDomuiImageClicked;
	}

	@Nullable
	public IClicked<NodeBase> getOnDomuiOddCharsClicked() {
		return m_onDomuiOddCharsClicked;
	}

	public void setOnDomuiOddCharsClicked(@Nonnull IClicked<NodeBase> onDomuiOddCharsClicked) {
		m_onDomuiOddCharsClicked = onDomuiOddCharsClicked;
	}

	public boolean isToolbarStartExpanded() {
		return m_toolbarStartExpanded;
	}

	public void setToolbarStartExpanded(boolean toolbarStartExpanded) {
		m_toolbarStartExpanded = toolbarStartExpanded;
	}

	@Override
	public boolean acceptRequestParameter(@Nonnull String[] values) throws Exception {
		if(isDisabled()) {
			return false;
		}

		for(int i = 0; i < values.length; i++) {
			String s = values[i];
			StringBuilder sb = new StringBuilder();
			try {
				StringTool.entitiesToUnicode(sb, s, true);
				values[i] = sb.toString();
			} catch(Exception e) {
				e.printStackTrace();
				values[i] = e.toString();
			}
		}
		return super.acceptRequestParameter(values);
	}

	@Override
	@OverridingMethodsMustInvokeSuper
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

}
