package to.etc.xml;

import org.xml.sax.*;

public class DefaultErrorHandler implements ErrorHandler {
	/** This string buffer receives error messages while the document gets parsed. */
	private StringBuilder	m_xmlerr_sb	= new StringBuilder();

	private boolean			m_errors;

	private void genErr(SAXParseException exception, String type) {
		//		exception.printStackTrace();
		if(m_xmlerr_sb.length() > 0)
			m_xmlerr_sb.append("\n");
		String id = exception.getPublicId();
		if(id == null || id.length() == 0)
			id = exception.getPublicId();
		if(id == null || id.length() == 0)
			id = "unknown-source";
		m_xmlerr_sb.append(id);
		m_xmlerr_sb.append('(');
		m_xmlerr_sb.append(Integer.toString(exception.getLineNumber()));
		m_xmlerr_sb.append(':');
		m_xmlerr_sb.append(Integer.toString(exception.getColumnNumber()));
		m_xmlerr_sb.append(") ");
		m_xmlerr_sb.append(type);
		m_xmlerr_sb.append(":");
		m_xmlerr_sb.append(exception.getMessage());
	}

	public final void warning(SAXParseException exception) throws SAXException {
		genErr(exception, "warning");
	}

	public final void error(SAXParseException exception) throws SAXException {
		m_errors = true;
		genErr(exception, "error");
	}

	public final void fatalError(SAXParseException exception) throws SAXException {
		m_errors = true;
		genErr(exception, "fatal");
	}

	public String getErrors() {
		return m_xmlerr_sb.toString();
	}

	public boolean hasErrors() {
		return m_errors;
	}
}
