package to.etc.log.handler;

import javax.annotation.*;

import org.slf4j.*;
import org.w3c.dom.*;

import to.etc.log.*;
import to.etc.log.EtcLoggerFactory.LoggerConfigException;
import to.etc.log.event.*;

/**
 * Filter that is applied during log handler processing.
 * Enables listening only on log events decorated by certain characteristics (like session id or login id).
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Oct 31, 2012
 */
class LogFilter {
	@Nonnull
	private final LogFilterType	m_type;

	@Nonnull
	private final String		m_key;

	@Nonnull
	private final String		m_value;

	private LogFilter(@Nonnull LogFilterType type, @Nonnull String key, @Nonnull String value) {
		m_type = type;
		m_key = key;
		m_value = value;
	}

	@Nonnull
	LogFilterType getType() {
		return m_type;
	}

	@Nullable
	String getKey() {
		return m_key;
	}

	@Nullable
	String getValue() {
		return m_value;
	}

	@Nonnull
	static LogFilter mdcFilter(@Nonnull String key, @Nonnull String value) {
		return new LogFilter(LogFilterType.MDC, key, value);
	}

	@Nonnull
	static LogFilter sessionFilter(@Nonnull String value) {
		return new LogFilter(LogFilterType.SESSION, "session", value);
	}

	boolean accept(@Nonnull EtcLogEvent event) {
		switch(m_type){
			case SESSION:
			case MDC:
				String val = MDC.get(m_key);
				return m_value.equalsIgnoreCase(val);
			default:
				throw new IllegalStateException("Filter of unknown type: " + m_type);
		}
	}

	@Nonnull
	static LogFilter createFromXml(@Nonnull Node node) throws LoggerConfigException {
		Node typeNode = node.getAttributes().getNamedItem("type");
		if(typeNode == null) {
			throw new EtcLoggerFactory.LoggerConfigException("Missing [type] at filter node.");
		}
		LogFilterType type = LogFilterType.valueOf(typeNode.getNodeValue().toUpperCase());

		String key = null;
		if(type == LogFilterType.MDC) {
			Node keyNode = node.getAttributes().getNamedItem("key");
			if(keyNode == null) {
				throw new EtcLoggerFactory.LoggerConfigException("Missing [key] at filter node of type=\"mdc\".");
			}
			key = keyNode.getNodeValue();
		} else if(type == LogFilterType.SESSION) {
			key = EtcMDCAdapter.SESSION;
		} else {
			throw new EtcLoggerFactory.LoggerConfigException("Non supported filter node of type=\"" + type + "\".");
		}

		Node valNode = node.getAttributes().getNamedItem("value");
		if(valNode == null) {
			throw new EtcLoggerFactory.LoggerConfigException("Missing [value] at filter node.");
		}
		String value = valNode.getNodeValue();
		return new LogFilter(type, key, value);
	}

	void saveToXml(@Nonnull Document doc, @Nonnull Element filterNode) {
		filterNode.setAttribute("type", m_type.name());
		filterNode.setAttribute("key", m_key);
		filterNode.setAttribute("value", m_value);
	}

	@Override
	public String toString() {
		return "filter " + m_type + " [" + m_key + "=" + m_value + "]";
	}
}
