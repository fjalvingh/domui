package to.etc.log.handler;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import to.etc.log.EtcLoggerFactory;
import to.etc.log.EtcLoggerFactory.LoggerConfigException;
import to.etc.log.Level;
import to.etc.log.event.EtcLogEvent;

/**
 * Defines what logger level is listened on defined logger name pattern.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Oct 31, 2012
 */
class LogMatcher {
	/**
	 * Defines logger name pattern on which handler applies.
	 */
	@NonNull
	private final String	m_name;

	/**
	 * Defines log level on which handler applies.
	 */
	@NonNull
	private final Level		m_level;

	LogMatcher(@NonNull String name, @NonNull Level level) {
		m_name = name;
		m_level = level;
	}

	@NonNull
	String getName() {
		return m_name;
	}

	@NonNull
	Level getLevel() {
		return m_level;
	}

	@Nullable
	boolean matches(@NonNull EtcLogEvent event) {
		return matchesName(event.getLogger().getName()) && m_level.includes(event.getLevel());
	}

	/**
	 * Returns if current matcher is subset of other matcher.
	 */
	boolean isSubmatcherOf(@NonNull LogMatcher other) {
		if(m_name.length() == 0) {
			return false;
		} else if(other.getName().length() == 0) {
			return true;
		} else {
			return m_name.startsWith(other.getName() + ".");
		}
	}

	boolean matchesName(@NonNull String key) {
		return (m_name.length() == 0 || key.startsWith(m_name + ".") || key.equals(m_name));
	}

	@NonNull
	static LogMatcher createFromXml(@NonNull Node node) throws LoggerConfigException {
		Node nameNode = node.getAttributes().getNamedItem("name");
		String name = nameNode == null ? "" : nameNode.getNodeValue();
		Node levelNode = node.getAttributes().getNamedItem("level");
		if(levelNode == null) {
			throw new EtcLoggerFactory.LoggerConfigException("Missing [level] at log node.");
		}
		Level level = Level.valueOf(levelNode.getNodeValue().toUpperCase());
		return new LogMatcher(name, level);
	}

	public void saveToXml(@NonNull Document doc, @NonNull Element logNode) {
		logNode.setAttribute("name", m_name);
		logNode.setAttribute("level", m_level.name());
	}

	@Override
	public String toString() {
		return "matcher " + m_name + " " + m_level;
	}
}
