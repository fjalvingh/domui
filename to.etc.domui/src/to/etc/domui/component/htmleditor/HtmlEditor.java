package to.etc.domui.component.htmleditor;

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
public class HtmlEditor extends TextArea {
	private String		m_vn;

	private String		m_toolbarSet = "DomUI";

	private IEditorFileSystem	m_fileSystem;

	public HtmlEditor() {
		super.setCssClass("ui-fck");
	}
	@Override
	public void setCssClass(String cssClass) {
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
		StringBuilder	sb	= new StringBuilder(1024);
		m_vn	= "_fck"+getActualID();
		sb.append("var ").append(m_vn).append(" = new FCKeditor('").append(getActualID()).append("');");
		appendOption(sb, "BasePath", PageContext.getRequestContext().getRelativePath("$fckeditor/"));
		appendOption(sb, "DefaultLanguage", NlsContext.getLocale().getLanguage());
		appendOption(sb, "ToolbarSet", m_toolbarSet);
		appendConfig(sb, "ToolbarStartExpanded", "false");

		//-- Override basic 'connector' config parameters
		appendConnectorConfig(sb, "ImageBrowser", "Image");

		sb.append(m_vn).append(".ReplaceTextarea();");
		appendCreateJS(sb);
	}
	private void	appendConfig(StringBuilder sb, String option, String value) {
		sb.append(m_vn).append(".Config['").append(option).append("'] = ").append(value).append(';');		// Disable this connector component
	}

	private void	appendConnectorConfig(StringBuilder sb, String option, String value) {
		if(getFileSystem() == null) {
			//-- Disable connector in config.
			sb.append(m_vn).append(".Config['").append(option).append("'] = false;");		// Disable this connector component
			return;
		}

		//-- Set the connector's URL proper.
		sb.append(m_vn).append(".Config['").append(option).append("URL'] = '");				// Start URL base path
		sb.append(PageContext.getRequestContext().getRelativePath("$fckeditor/editor/"));
		sb.append("filemanager/browser/default/browser.html?Type=Image&Connector=");
		sb.append(PageContext.getRequestContext().getRelativePath(EditResPart.class.getName()));
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

	private void	appendOption(StringBuilder sb, String option, String value) {
		sb.append(m_vn).append(".").append(option).append(" = ");
		try {
			StringTool.strToJavascriptString(sb, value, true);
		} catch(Exception x) {
			x.printStackTrace();				// James Gosling is an idiot.
		}
		sb.append(";");
	}

	public String getToolbarSet() {
		return m_toolbarSet;
	}

	public void setToolbarSet(String toolbarSet) {
		if(DomUtil.isEqual(toolbarSet, m_toolbarSet))
			return;
		m_toolbarSet = toolbarSet;
		if(! isBuilt())
			return;
		StringBuilder  sb = new StringBuilder();
		appendOption(sb, "ToolbarSet", m_toolbarSet);
		appendJavascript("");
	}
	public IEditorFileSystem getFileSystem() {
		return m_fileSystem;
	}
	public void setFileSystem(IEditorFileSystem fileSystem) {
		m_fileSystem = fileSystem;
	}
	public void	setToolbarSet(HtmlToolbarSet set) {
		switch(set) {
			default:
				throw new IllegalStateException("Unknown toolbar set: "+set);
			case BASIC:		setToolbarSet("Default");	break;
			case DEFAULT:	setToolbarSet("Basic");		break;
			case DOMUI:		setToolbarSet("DomUI");		break;
			case TXTONLY:	setToolbarSet("TxtOnly");	break;
		}
	}
}
