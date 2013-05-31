package to.etc.log.handler;

import java.io.*;
import java.util.*;

import javax.annotation.*;

import org.w3c.dom.*;

import to.etc.log.*;
import to.etc.log.EtcLoggerFactory.LoggerConfigException;
import to.etc.log.event.*;

class FileLogHandler implements ILogHandler {
	/**
	 * Defines where to write log output.
	 */
	@Nullable
	private final String					m_out;

	@Nullable
	private final File						m_logRoot;

	/**
	 * Defines matchers to calculate on which logger handler applies. To apply on logger, matcher closest to logger name must match with logEvent.
	 */
	@Nonnull
	private final List<LogMatcher>			m_matchers	= new ArrayList<LogMatcher>();

	/**
	 * Defines filters on which handler applies. To apply on logger, all filters must be matched.
	 */
	@Nonnull
	private List<LogFilter>					m_filters	= Collections.EMPTY_LIST;

	/**
	 * Keeps list of loggers that are marked as handled by handler.
	 */
	@Nonnull
	private final Map<EtcLogger, Boolean[]>	m_loggers	= new HashMap<EtcLogger, Boolean[]>();

	@Nonnull
	private final Object					m_writeLock	= new Object();

	@Nullable
	private EtcLogFormat					m_format	= null;

	public FileLogHandler(@Nonnull File logRoot, @Nullable String out) {
		m_logRoot = logRoot;
		m_out = out;
	}

	@Nonnull
	public static FileLogHandler createDefaultHandler(@Nonnull File logRoot, @Nonnull Level level) {
		FileLogHandler handler = new FileLogHandler(logRoot, null);
		LogMatcher matcher = new LogMatcher("", level);
		handler.addMatcher(matcher);
		return handler;
	}

	public void addMatcher(@Nonnull LogMatcher matcher) {
		m_matchers.add(matcher);
		m_loggers.clear();
	}

	public void addFilter(@Nonnull LogFilter filter) {
		if(m_filters == Collections.EMPTY_LIST) {
			m_filters = new ArrayList<LogFilter>();
		}
		m_filters.add(filter);
	}

	@Override
	public void handle(@Nonnull EtcLogEvent event) {
		Boolean[] applicablePerLevels = m_loggers.get(event.getLogger());
		if(null == applicablePerLevels) {
			applicablePerLevels = new Boolean[Level.values().length];
			m_loggers.put(event.getLogger(), applicablePerLevels);
		}
		Boolean isApplicable = applicablePerLevels[event.getLevel().getCode()];
		if(isApplicable == null) {
			isApplicable = decideOnMatchers(event);
			applicablePerLevels[event.getLevel().getCode()] = isApplicable;
		}
		if(isApplicable.booleanValue()) {
			if(checkFilters(event)) {
				log(event);
			}
		}
	}

	private void log(@Nonnull EtcLogEvent event) {
		String line = EtcLogFormatter.format(event, m_format != null ? m_format.getFormat() : EtcLogFormat.DEFAULT, getLogPartFromFilters());

		synchronized(m_writeLock) {
			if(m_out == null) {
				System.out.println(line);
			} else {
				BufferedWriter w = null;
				String fileName = null;
				fileName = EtcLoggerFactory.getSingleton().composeFullLogFileName(m_logRoot.getAbsolutePath(), m_out);

				File outFile = new File(fileName);
				outFile.getParentFile().mkdirs();
				try {
					outFile.createNewFile();
					w = new BufferedWriter(new FileWriter(outFile, true));
					w.write(line);
					w.newLine();
				} catch(IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				} finally {
					if(w != null) {
						try {
							w.close();
						} catch(IOException e) {
							e.printStackTrace();
							throw new RuntimeException(e);
						}
					}
				}
			}
		}
	}

	@Nullable
	private String getLogPartFromFilters() {
		if(m_filters.isEmpty()) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for(LogFilter filter : m_filters) {
			sb.append("[").append(filter.getKey()).append("=").append(filter.getValue()).append("]");
		}
		return sb.toString();
	}

	@Nonnull
	private Boolean decideOnMatchers(@Nonnull EtcLogEvent event) {
		LogMatcher closest = null;
		for(LogMatcher matcher : m_matchers) {
			if(matcher.matches(event)) {
				if(closest == null || matcher.isSubmatcherOf(closest)) {
					closest = matcher;
				}
			}
		}
		if(closest != null && closest.getLevel().includes(event.getLevel())) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	private boolean checkFilters(@Nonnull EtcLogEvent event) {
		for(LogFilter filter : m_filters) {
			if(!filter.accept(event)) {
				return false;
			}
		}
		return true;
	}

	@Nullable
	public File getLogRoot() {
		return m_logRoot;
	}

	@Override
	@Nullable
	public Level listenAt(@Nonnull String key) {
		LogMatcher closest = null;
		for(LogMatcher matcher : m_matchers) {
			if(matcher.matchesName(key)) {
				if(closest == null || matcher.isSubmatcherOf(closest)) {
					closest = matcher;
				}
			}
		}
		return closest != null ? closest.getLevel() : null;
	}

	@Nonnull
	public static FileLogHandler createFromFileTypeConfig(@Nonnull File logRoot, @Nonnull Node handlerNode) throws LoggerConfigException {
		Node file = handlerNode.getAttributes().getNamedItem("file");
		if(file == null) {
			throw new EtcLoggerFactory.LoggerConfigException("Missing file attribute inside file type handler.");
		}
		FileLogHandler res = new FileLogHandler(logRoot, file.getNodeValue());
		res.load(handlerNode);
		return res;
	}

	void load(@Nonnull Node handlerNode) throws LoggerConfigException {
		NodeList nodes = handlerNode.getChildNodes();
		for(int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if("log".equals(node.getNodeName())) {
				addMatcher(LogMatcher.createFromXml(node));
			} else if("filter".equals(node.getNodeName())) {
				addFilter(LogFilter.createFromXml(node));
			} else if("format".equals(node.getNodeName())) {
				addFormat(EtcLogFormat.createFromXml(node));
			}
		}
	}

	private void addFormat(@Nonnull EtcLogFormat format) throws LoggerConfigException {
		if(m_format != null) {
			throw new EtcLoggerFactory.LoggerConfigException("Multiple format definitions found in log handler.");
		} else {
			m_format = format;
		}
	}

	@Nonnull
	public static FileLogHandler createFromStdoutTypeConfig(@Nonnull File logRoot, @Nonnull Node handlerNode) throws LoggerConfigException {
		FileLogHandler res = new FileLogHandler(logRoot, null);
		res.load(handlerNode);
		return res;
	}

	@Override
	public void saveToXml(@Nonnull Document doc, @Nonnull Element handlerNode, boolean includeNonPerstistable) {
		handlerNode.setAttribute("type", m_out == null ? "stdout" : "file");
		if(m_out != null) {
			handlerNode.setAttribute("file", m_out);
		}
		if(m_format != null) {
			Element formatNode = doc.createElement("format");
			handlerNode.appendChild(formatNode);
			m_format.saveToXml(doc, formatNode);
		}
		for(LogMatcher matcher : m_matchers) {
			Element logNode = doc.createElement("log");
			handlerNode.appendChild(logNode);
			matcher.saveToXml(doc, logNode);
		}
		for(LogFilter filter : m_filters) {
			if(includeNonPerstistable || filter.getType().isPersistable()) {
				Element filterNode = doc.createElement("filter");
				handlerNode.appendChild(filterNode);
				filter.saveToXml(doc, filterNode);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("handler ").append(m_out != null ? "file: " + m_out : "stdout");
		if(!m_matchers.isEmpty()) {
			sb.append("\nmatchers: ");
			for(LogMatcher matcher : m_matchers) {
				sb.append("[").append(matcher.toString()).append("]");
			}
		}
		if(!m_filters.isEmpty()) {
			sb.append("\nfilters: ");
			for(LogFilter filter : m_filters) {
				sb.append("[").append(filter.toString()).append("]");
			}
		}
		return sb.toString();
	}

	@Override
	public boolean isTemporary() {
		for(LogFilter filter : m_filters) {
			if(filter.getType() == LogFilterType.SESSION) {
				return true;
			}
		}
		return false;
	}
}
