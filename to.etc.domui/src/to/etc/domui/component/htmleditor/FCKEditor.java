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
import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;
import to.etc.util.*;
import to.etc.webapp.nls.*;

/**
 * This represents a FCKEditor instance.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 30, 2008
 */
public class FCKEditor extends TextArea {
	private String m_vn;

	private String m_toolbarSet = "DomUI";


	private boolean m_toolbarStartExpanded;

	private IEditorFileSystem m_fileSystem;

	public FCKEditor() {
		super.setCssClass("ui-fck");
		setVisibility(VisibilityType.HIDDEN);
	}

	@Override
	public void setCssClass(final String cssClass) {
		throw new IllegalStateException("Cannot set a class on FCKEditor");
	}

	/**
	 * <p>To create the editor we need to replace the core code. We add a textarea having the ID and a
	 * special class (ui-fck). This special class is an indicator to the submit logic that the textarea
	 * is an FCKEditor instance. This causes it to use special logic to retrieve a value.</p>
	 *
	 * <p>Javascript stanza:
	 * <pre>
	 *	var oFCKeditor = new FCKeditor( 'FCKeditor1' ) ;
	 *	oFCKeditor.BasePath	= sBasePath ;
	 *	oFCKeditor.ReplaceTextarea() ;
	 * </pre>
	 *
	 * @see to.etc.domui.dom.html.NodeBase#createContent()
	 */
	@Override
	public void createContent() throws Exception {
		StringBuilder sb = new StringBuilder(1024);
		m_vn = "_fck" + getActualID();
		sb.append("var ").append(m_vn).append(" = new FCKeditor('").append(getActualID()).append("');");
		appendOption(sb, "BasePath", UIContext.getRequestContext().getRelativePath("$fckeditor/"));
		appendOption(sb, "DefaultLanguage", NlsContext.getLocale().getLanguage());
		if(getWidth() != null)
			appendOption(sb, "Width", getWidth());
		if(getHeight() != null)
			appendOption(sb, "Height", getHeight());
		appendOption(sb, "ToolbarSet", m_toolbarSet);
		appendConfig(sb, "ToolbarStartExpanded", new Boolean(m_toolbarStartExpanded).toString());

		//-- Override basic 'connector' config parameters
		appendConnectorConfig(sb, "ImageBrowser", "Image");

		sb.append(m_vn).append(".ReplaceTextarea();");
		appendCreateJS(sb);
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
		sb.append(UIContext.getRequestContext().getRelativePath(EditResPart.class.getName()));
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
				setToolbarSet("Default");
				break;
			case DEFAULT:
				setToolbarSet("Basic");
				break;
			case DOMUI:
				setToolbarSet("DomUI");
				break;
			case TXTONLY:
				setToolbarSet("TxtOnly");
				break;
		}
	}

	public void setToolbarStartExpanded() {
		m_toolbarStartExpanded = true;
	}

}
