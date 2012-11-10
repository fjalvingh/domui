package to.etc.log.handler;

import javax.annotation.*;

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

	@Nonnull
	private String				m_format	= DEFAULT;

	@Nonnull
	public String getFormat() {
		return m_format;
	}

	public void setFormat(@Nonnull String format) {
		m_format = format;
	}

	@Nonnull
	static EtcLogFormat createFromXml(@Nonnull Node node) throws LoggerConfigException {
		EtcLogFormat format = new EtcLogFormat();
		format.setFormat(node.getAttributes().getNamedItem("pattern").getNodeValue());
		return format;
	}

	void saveToXml(@Nonnull Document doc, @Nonnull Element formatNode) {
		formatNode.setAttribute("pattern", m_format);
	}
}
