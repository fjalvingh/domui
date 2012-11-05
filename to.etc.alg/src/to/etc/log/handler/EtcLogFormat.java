package to.etc.log.handler;

import org.w3c.dom.*;

import to.etc.log.EtcLoggerFactory.LoggerConfigException;

/**
 * Combines following markers with free text in format definition:  
 * 
 * %d (timestamp, always uses simple time format {@link EtcLogFormat#TIMESTAMP})
 * %l (logger name)
 * %msg (logged message)
 * %n (new line)
 * %p (level)
 * %t (thread)
 * %mdc{key} (mdc value for key, if it is missing skips log part)
 * %marker (outputs marker name, if such is provided in logged line)
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Nov 5, 2012
 */
public class EtcLogFormat {
	public static final String	DEFAULT		= "%d %p %mdc{loginId}\t%t [%l] %msg";

	public static final String	TIMESTAMP	= "HH:mm:ss.SSS";

	private String				m_format	= DEFAULT;

	public String getFormat() {
		return m_format;
	}

	public void setFormat(String format) {
		m_format = format;
	}

	static EtcLogFormat createFromXml(Node node) throws LoggerConfigException {
		EtcLogFormat format = new EtcLogFormat();
		format.setFormat(node.getAttributes().getNamedItem("pattern").getNodeValue());
		return format;
	}

	void saveToXml(Document doc, Element formatNode) {
		formatNode.setAttribute("pattern", m_format);
	}
}
