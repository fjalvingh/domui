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

import javax.annotation.*;

import to.etc.domui.component.htmleditor.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.component.misc.MsgBox.IAnswer;
import to.etc.domui.component.misc.MsgBox.Type;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;
import to.etc.util.*;
import to.etc.webapp.nls.*;

/**
 * This represents a FCKEditor instance.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 30, 2008
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Refactored on Oct 07, 2011
 */
public class CKEditor extends TextArea {
	private String m_vn;

	private String m_toolbarSet = "DomUI";

	private IEditorFileSystem m_fileSystem;

	private IClicked<NodeBase> m_onDomuiImageClicked;

	private IClicked<NodeBase> m_onDomuiOddCharsClicked;

	private static final String WEBUI_FCK_DOMUIIMAGE_ACTION = "FCKIMAGE";

	private static final String WEBUI_FCK_DOMUIODDCHAR_ACTION = "FCKODDCHAR";

	private boolean m_toolbarStartExpanded;

	private String m_internalWidth;

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
		m_vn = "_fck" + getActualID();


		sb.append("var ").append(m_vn).append(" = CKEDITOR.replace('").append(getActualID()).append("', {");
		sb.append("customConfig: '").append(DomUtil.getRelativeApplicationResourceURL("$ckeditor/domuiconfig.js")).append("'\n");

		String dl = NlsContext.getLocale().getLanguage();
		if(dl.contains("nl"))
			dl = "nl";
		else if(dl.contains("en"))
			dl = "en";
		sb.append(", defaultLanguage:'").append(dl).append("'\n");

		String s = m_internalWidth;
		if(null != s)
			sb.append(", width:'").append(s).append("'\n");

		s = m_internalHeight;
		if(null != s)
			sb.append(", height:'").append(s).append("'\n");

		//-- Finish.
		sb.append("});\n");

//		if(isToolbarStartExpanded()) {
//			appendConfig(sb, "ToolbarStartExpanded", "true");
//		}
//		appendOption(sb, "ToolbarSet", m_toolbarSet);
//
//		//-- Override basic 'connector' config parameters
//		appendConnectorConfig(sb, "ImageBrowser", "Image"); //FIXME: vmijic 20111010 I'm not sure why this stands -> we probably should remove that since it looks useless...
//
//		sb.append(m_vn).append(".ReplaceTextarea();");
		//-- We must do custom layout fixes once editor is transformed by FCKEditor initialization javascript. On IE8+ we need additonal hack on fckeditor internals, and we need to repeat then on each iframe resize.
//		sb.append("WebUI.registerFckEditorId('" + getActualID() + "');");
		appendCreateJS(sb);
	}

	@Override
	public void setWidth(String width) {
		m_internalWidth = width;
	}

	@Override
	public void setHeight(String height) {
		m_internalHeight = height;
	}

	private void appendConfig(final StringBuilder sb, final String option, final String value) {
		sb.append(m_vn).append(".Config['").append(option).append("'] = ").append(value).append(';'); // Disable this connector component
	}

	private void appendConnectorConfig(final StringBuilder sb, final String option, final String value) {
		if(getFileSystem() == null) {
			//-- Disable connector in config.
			sb.append(m_vn).append(".Config['").append(option).append("'] = false;"); // Disable this connector component
			return;
		}

		//-- Set the connector's URL proper.
		sb.append(m_vn).append(".Config['").append(option).append("URL'] = '"); // Start URL base path
		sb.append(UIContext.getRequestContext().getRelativePath("$fckeditor/editor/"));
		sb.append("filemanager/browser/default/browser.html?Type=Image&Connector=");
		sb.append(UIContext.getRequestContext().getRelativePath(CKEditResPart.class.getName()));
		sb.append("/");
		sb.append(getPage().getConversation().getFullId());
		sb.append("/");
		sb.append(getPage().getBody().getClass().getName());
		sb.append("/");
		sb.append(getActualID());
		sb.append("/");
		sb.append(value);
		sb.append(".part");

		sb.append("';");
	}

	private void appendOption(final StringBuilder sb, final String option, final String value) {
		sb.append(m_vn).append(".").append(option).append(" = ");
		try {
			StringTool.strToJavascriptString(sb, value, true);
		} catch(Exception x) {
			x.printStackTrace(); // Checked exceptions are idiotic
		}
		sb.append(";");
	}

	public String getToolbarSet() {
		return m_toolbarSet;
	}

	public void setToolbarSet(final String toolbarSet) {
		if(DomUtil.isEqual(toolbarSet, m_toolbarSet))
			return;
		m_toolbarSet = toolbarSet;
		if(!isBuilt())
			return;
		StringBuilder sb = new StringBuilder();
		appendOption(sb, "ToolbarSet", m_toolbarSet);
		appendJavascript("");
	}

	public IEditorFileSystem getFileSystem() {
		return m_fileSystem;
	}

	public void setFileSystem(final IEditorFileSystem fileSystem) {
		m_fileSystem = fileSystem;
	}

	public void setToolbarSet(final FCKToolbarSet set) {
		switch(set){
			default:
				throw new IllegalStateException("Unknown toolbar set: " + set);
			case BASIC:
				setToolbarSet("Basic");
				break;
			case DEFAULT:
				setToolbarSet("Default");
				break;
			case DOMUI:
				setToolbarSet("DomUI");
				break;
			case TXTONLY:
				setToolbarSet("TxtOnly");
				break;
			case NEW_MESSAGE:
				setToolbarSet("NewMessage");
				break;
		}
	}

	/**
	 * Handle {@link CKEditor#WEBUI_FCK_DOMUIIMAGE_ACTION} activity on FCKEditor customized commands that interacts with domui.
	 * Handle {@link CKEditor#WEBUI_FCK_DOMUIODDCHAR_ACTION} activity on FCKEditor customized commands that interacts with domui.
	 *
	 * @see to.etc.domui.dom.html.Div#componentHandleWebAction(to.etc.domui.server.RequestContextImpl, java.lang.String)
	 */
	@Override
	public void componentHandleWebAction(@Nonnull RequestContextImpl ctx, @Nonnull String action) throws Exception {
		if(WEBUI_FCK_DOMUIIMAGE_ACTION.equals(action))
			selectImage(ctx);
		else if(WEBUI_FCK_DOMUIODDCHAR_ACTION.equals(action))
			oddChars(ctx);
		else
			super.componentHandleWebAction(ctx, action);
	}

	private void selectImage(RequestContextImpl ctx) throws Exception {
		if(m_onDomuiImageClicked == null) {
			MsgBox.message(this, Type.ERROR, "No image picker is defined", new IAnswer() {
				@Override
				public void onAnswer(MsgBoxButton result) throws Exception {
					renderCancelImage();
				}
			});
		} else {
			m_onDomuiImageClicked.clicked(this);
		}
	}

	private void oddChars(RequestContextImpl ctx) throws Exception {
		if(m_onDomuiOddCharsClicked == null) {
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
			m_onDomuiOddCharsClicked.clicked(this);
		}
	}

	public void renderImageSelected(String url) {
		//FCK_DOMUIIMAGE
		///Itris_VO02/General/Images/Organisations/1000/demo_hlpv_menu_header.gif
		appendJavascript("var fckIFrame = document.getElementById('" + getActualID() + "___Frame'); if (fckIFrame){ fckIFrame.contentWindow.DomuiImage_addImage('"
			+ getActualID()
 + "', '" + url + "');};");
	}

	public void renderCancelImage() {
		appendJavascript("var fckIFrame = document.getElementById('" + getActualID() + "___Frame'); if (fckIFrame){ fckIFrame.contentWindow.DomuiImage_cancel('" + getActualID() + "');};");
	}

	public void renderCloseOddCharacters() {
		appendJavascript("var fckIFrame = document.getElementById('" + getActualID() + "___Frame'); if (fckIFrame){ fckIFrame.contentWindow.DomuiOddChar_cancel('" + getActualID() + "');};");
	}

	public void renderOddCharacters(String input) {
		appendJavascript("var fckIFrame = document.getElementById('" + getActualID() + "___Frame'); if (fckIFrame){ fckIFrame.contentWindow.DomuiImage_addString('" + getActualID() + "', '" + input + "');};");
	}

	public IClicked<NodeBase> getOnDomuiImageClicked() {
		return m_onDomuiImageClicked;
	}

	public void setOnDomuiImageClicked(IClicked<NodeBase> onDomuiImageClicked) {
		m_onDomuiImageClicked = onDomuiImageClicked;
	}

	public IClicked<NodeBase> getOnDomuiOddCharsClicked() {
		return m_onDomuiOddCharsClicked;
	}

	public void setOnDomuiOddCharsClicked(IClicked<NodeBase> onDomuiOddCharsClicked) {
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
		appendJavascript("WebUI.unregisterFckEditorId('" + getActualID() + "');");
	}

}
