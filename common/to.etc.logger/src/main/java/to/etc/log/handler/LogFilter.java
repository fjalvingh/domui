package to.etc.log.handler;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.MDC;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import to.etc.log.EtcLoggerFactory;
import to.etc.log.EtcLoggerFactory.LoggerConfigException;
import to.etc.log.EtcMDCAdapter;
import to.etc.log.event.EtcLogEvent;

/**
 * Filter that is applied during log handler processing.
 * Enables listening only on log events decorated by certain characteristics (like session id or login id).
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Oct 31, 2012
 */
class LogFilter {
	@NonNull
	private final LogFilterType	m_type;

	@NonNull
	private final String		m_key;

	@NonNull
	private final String		m_value;

	private LogFilter(@NonNull LogFilterType type, @NonNull String key, @NonNull String value) {
		m_type = type;
		m_key = key;
		m_value = value;
	}

	@NonNull
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

	@NonNull
	static LogFilter mdcFilter(@NonNull String key, @NonNull String value) {
		return new LogFilter(LogFilterType.MDC, key, value);
	}

	@NonNull
	static LogFilter sessionFilter(@NonNull String value) {
		return new LogFilter(LogFilterType.SESSION, "session", value);
	}

	boolean accept(@NonNull EtcLogEvent event) {
		switch(m_type){
			case SESSION:
			case MDC:
				String val = MDC.get(m_key);
				return m_value.equalsIgnoreCase(val);
			default:
				throw new IllegalStateException("Filter of unknown type: " + m_type);
		}
	}

	@NonNull
	static LogFilter createFromXml(@NonNull Node node) throws LoggerConfigException {
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

	void saveToXml(@NonNull Document doc, @NonNull Element filterNode) {
		filterNode.setAttribute("type", m_type.name());
		filterNode.setAttribute("key", m_key);
		filterNode.setAttribute("value", m_value);
	}

	@Override
	public String toString() {
		return "filter " + m_type + " [" + m_key + "=" + m_value + "]";
	}
}
