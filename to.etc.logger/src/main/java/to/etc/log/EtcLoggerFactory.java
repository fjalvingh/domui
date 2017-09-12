package to.etc.log;

import org.slf4j.ILoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import to.etc.log.event.EtcLogEvent;
import to.etc.log.handler.ILogHandler;
import to.etc.log.handler.LogHandlerRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements logger factory. Encapsulates definitions and configuration of loggers used.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Oct 30, 2012
 */
public class EtcLoggerFactory implements ILoggerFactory {
	static public final String DEFAULT_CONFIG_FILENAME = "etclogger.config.xml";

	/**
	 * The unique instance of this class.
	 */
	@Nonnull
	private static final EtcLoggerFactory SINGLETON;

	@Nonnull
	private static final ThreadLocal<SimpleDateFormat> DATEFORMATTER = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyMMdd");
		}
	};

	/** Root config file for logger configuration. */
	@Nullable
	private File m_configFile;

	/** Log dir where all logger are doing output. */
	@Nullable
	private File m_logDir;

	/** logLocation stored value inside config file. */
	@Nullable
	private String m_logDirOriginalConfigured;

	/** Contains loaded Logger instances. */
	@Nonnull
	private final Map<String, EtcLogger> LOGGERS = new HashMap<String, EtcLogger>();

	/** Contains handler instances - logger instances behavior definition. */
	@Nonnull
	private List<ILogHandler> m_handlers = new ArrayList<ILogHandler>();

	@Nonnull
	private Object m_handlersLock = new Object();

	/** Default general log level */
	@Nonnull
	private static final Level DEFAULT_LEVEL = Level.ERROR;

	/** Name of logger factory configuration file */
	//public static final String CONFIG_FILENAME = "etcLoggerConfig.xml";


	/**
	 * Return the singleton of this class.
	 *
	 * @return the MyLoggerFactory singleton
	 */
	@Nonnull
	public static final EtcLoggerFactory getSingleton() {
		return SINGLETON;
	}

	/**
	 * Exception type used to notify errors during loading of logger configuration.
	 *
	 *
	 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
	 * Created on Oct 30, 2012
	 */
	public static class LoggerConfigException extends Exception {
		public LoggerConfigException(@Nonnull String msg) {
			super(msg);
		}
	}

	/**
	 * @see org.slf4j.ILoggerFactory#getLogger(java.lang.String)
	 */
	@Override
	@Nonnull
	public EtcLogger getLogger(@Nonnull String key) {
		EtcLogger logger;
		synchronized(LOGGERS) {
			logger = LOGGERS.get(key);
			if(logger == null) {
				logger = EtcLogger.create(key, calcLevel(key));
				LOGGERS.put(key, logger);
			}
		}
		return logger;
	}

	@Nullable
	private Level calcLevel(@Nonnull String key) {
		Level current = null;
		for(ILogHandler handler : getHandlers()) {
			Level level = handler.listenAt(key);
			if(current == null || (level != null && !current.includes(level))) {
				current = level;
			}
		}
		return current;
	}

	/**
	 * This creates built-in log configuration that would log to stout until application specific configuration is initialized.
	 */
	private synchronized void initializeBuiltInLoggerConfig() {
		String configXml;
		try {
			configXml = LogUtil.readResourceAsString(this.getClass(), DEFAULT_CONFIG_FILENAME, "utf-8");
			loadConfigFromXml(configXml);
			System.err.println(
				this.getClass().getName() + " is initialized by loading built-in logger configuration as " + this
					.getClass().getName() + " resource " + DEFAULT_CONFIG_FILENAME);
		} catch(Exception e) {
			//this should not happen -> we load design time created resource - it must be valid
			System.err.println(
				"Built-in logger config is invalid! Check class " + this.getClass().getName() + " resource "
					+ DEFAULT_CONFIG_FILENAME);
			e.printStackTrace();
		}
	}

	/**
	 * Call to initialize logger factory from persisted configuration.
	 * Sets rootLocation, that is location where configFile is persisted.
	 * In case that configuration is missing or fails to load, new configuration is created as built-in configuration.
	 * IMPORTANT: this needs to be executed earliest possible in application starting.
	 *
	 * In case that logger factory has to be initialized with predefined application specific configuration use {@link EtcLoggerFactory#initialize(File, String)}.
	 *
	 * @param configFile
	 * @throws Exception
	 */
	public synchronized void initialize(@Nonnull File configFile) throws Exception {
		m_configFile = configFile;
		if(configFile.exists()) {
			String configXml = LogUtil.readFileAsString(configFile, "utf-8");
			if(tryLoadConfigFromXml(configFile, configXml)) {
				return;
			}
		}
		//if existing config does not exists or fails to load, use one from resource
		initializeBuiltInLoggerConfig();
	}

	/**
	 * Call to initialize logger factory from specified configXml.
	 * Sets rootLocation, that is location where changes in logger configuration would be persisted.
	 * Persisted configuration always resides in {@link #m_configFile} file.
	 * Returns false in case that configuration fails to load, it does not try any other logger configuration.
	 * This method should be used only as special case when persisted configuration should be by passed temporary.
	 * IMPORTANT: this needs to be executed earliest possible in application starting.
	 *
	 * Usual way to initialize logger is to use {@link EtcLoggerFactory#initialize(File, String)}.
	 *
	 * @param configFile
	 * @param configXml
	 * @return
	 */
	public synchronized boolean tryLoadConfigFromXml(@Nonnull File configFile, @Nonnull String configXml) {
		m_configFile = configFile;
		try {
			loadConfigFromXml(configXml);
			return true;
		} catch(Exception ex) {
			System.err.println("Failed logger config load from xml:" + configXml);
			ex.printStackTrace();
		}
		return false;
	}

	/**
	 * Call to initialize logger factory.
	 * Sets rootLocation, that is location where configFile updates are persisted.
	 * First it tries to load persisted logger config, if such exists in specified location.
	 * If that fails, it tries to load specified defaultConfig
	 * If that also fails, it loads default built-in config.
	 * Since later changes in logger config would be persisted inside configLocation, it should exists with write permissions.
	 * IMPORTANT: logger config needs to be executed earliest possible in application starting.
	 *
	 * @param configFile
	 * @param defaultConfig
	 * @return
	 * @throws Exception
	 */
	public synchronized void initialize(@Nonnull File configFile, @Nonnull String defaultConfig) throws Exception {
		m_configFile = configFile;
		System.out.println(
			this.getClass().getName() + " logger configuration location set to " + configFile.getAbsolutePath());
		String configXml = null;
		if(configFile.exists()) {
			//try 1 : try persisted config
			configXml = LogUtil.readFileAsString(configFile, "utf-8");
			if(tryLoadConfigFromXml(configFile, configXml)) {
				System.out
					.println(this.getClass().getName() + " is initialized by loading persisted logger configuration.");
				return;
			}
		}
		//try 2 : try defaultConfig
		if(tryLoadConfigFromXml(configFile, defaultConfig)) {
			System.out.println(
				this.getClass().getName() + " is initialized by loading application specific default configuration.");
			return;
		}
		//try 3 : try built-in config
		initializeBuiltInLoggerConfig();
	}

	private synchronized void loadConfigFromXml(@Nonnull String configXml) throws Exception {
		StringReader sr = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			sr = new StringReader(configXml);
			Document doc = db.parse(new InputSource(sr));
			loadConfig(doc);
		} finally {
			sr.close();
		}
	}

	@Nonnull
	private ILogHandler loadHandler(@Nonnull Node handlerNode) throws LoggerConfigException {
		Node typeNode = handlerNode.getAttributes().getNamedItem("type");
		if(typeNode == null) {
			throw new LoggerConfigException("Missing [type] attribute on <handler> element!");
		} else {
			String val = typeNode.getNodeValue();
			return LogHandlerRegistry.getSingleton().createHandler(val, m_logDir, handlerNode);
		}
	}

	/**
	 * Saves configuration of logger factory. Uses same root location as specified during .
	 * @throws Exception
	 */
	public void saveConfig() throws Exception {
		Document doc = toXml(false);

		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		try(Writer fw = new OutputStreamWriter(new FileOutputStream(m_configFile), "utf-8")) {
			StreamResult result = new StreamResult(fw);
			transformer.transform(source, result);
		}
	}

	@Nonnull
	public Document toXml(boolean includeNonPerstistable) throws ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.newDocument();
		Element rootElement = doc.createElement("config");
		doc.appendChild(rootElement);
		rootElement.setAttribute("logLocation", m_logDirOriginalConfigured);

		for(ILogHandler handler : getHandlers()) {
			if(includeNonPerstistable || !handler.isTemporary()) {
				Element handlerNode = doc.createElement("handler");
				rootElement.appendChild(handlerNode);
				handler.saveToXml(doc, handlerNode, includeNonPerstistable);
			}
		}
		return doc;
	}

	private void recalculateLoggers() {
		synchronized(LOGGERS) {
			for(EtcLogger logger : LOGGERS.values()) {
				logger.setLevel(calcLevel(logger.getName()));
			}
		}
	}

	//@Nonnull
	//public String getRootDir() {
	//	return new File(m_configFile, CONFIG_FILENAME).getAbsolutePath();
	//}

	@Nonnull
	public String getLogDir() {
		File logDir = m_logDir;
		if(null == logDir) {
			m_logDir = logDir = new File("/tmp");
		}
		return logDir.getAbsolutePath();
	}

	@Nonnull
	public String logDirOriginalAsConfigured() {
		return m_logDirOriginalConfigured;
	}

	public void loadConfig(@Nonnull Document doc) throws LoggerConfigException {
		List<ILogHandler> loadedHandlers = new ArrayList<ILogHandler>();
		doc.getDocumentElement().normalize();
		NodeList configNodes = doc.getElementsByTagName("config");
		if(configNodes.getLength() == 0) {
			throw new LoggerConfigException("Missing config root node.");
		} else if(configNodes.getLength() > 1) {
			throw new LoggerConfigException("Multiple config element nodes found.");
		} else {
			Node val = configNodes.item(0).getAttributes().getNamedItem("logLocation");
			if(val == null) {
				throw new LoggerConfigException("Missing [logLocation] attribute in config root node.");
			} else {
				String logLocation = val.getNodeValue();
				m_logDirOriginalConfigured = logLocation;
				try {
					boolean checkNext = true;
					do {
						checkNext = false;
						int posStart = logLocation.indexOf("%");
						if(posStart > -1) {
							int posEnd = logLocation.indexOf("%", posStart + 1);
							if(posEnd > -1) {
								String part = System.getProperty(logLocation.substring(posStart + 1, posEnd));
								if(part == null) {
									throw new Exception("Empty part!");
								}
								logLocation =
									logLocation.substring(0, posStart) + part + logLocation.substring(posEnd + 1);
								checkNext = true;
							}
						}
					} while(checkNext);
					logLocation = logLocation.replace("/", File.separator);
				} catch(Exception ex) {
					System.out.println(
						"Etc logger - problem in resolving logger configuration location from loaded default config: "
							+ m_logDirOriginalConfigured + ".\nUsing default location: "
							+ logLocation);
				}
				m_logDir = new File(logLocation);
				m_logDir.mkdirs();
			}
		}
		NodeList handlerNodes = doc.getElementsByTagName("handler");
		for(int i = 0; i < handlerNodes.getLength(); i++) {
			Node handlerNode = handlerNodes.item(i);
			loadedHandlers.add(loadHandler(handlerNode));
		}
		if(loadedHandlers.isEmpty()) {
			ILogHandler handler = LogHandlerRegistry.getSingleton().createDefaultHandler(m_configFile, DEFAULT_LEVEL);
			loadedHandlers.add(handler);
		}
		synchronized(m_handlersLock) {
			m_handlers = loadedHandlers;
		}
		recalculateLoggers();
	}

	@Nonnull
	public Level getDefaultLevel() {
		return DEFAULT_LEVEL;
	}

	@Nonnull
	public String composeFullLogFileName(@Nonnull String logPath, @Nonnull String fileName) {
		String res;
		if(fileName.contains(":")) {
			res = fileName;
		} else {
			res = logPath + File.separator + fileName;
		}

		res += "_" + DATEFORMATTER.get().format(new Date()) + ".log";
		return res;
	}

	@Nonnull
	public String composeFullLogFileName(@Nonnull String fileName) {
		return composeFullLogFileName(m_logDir.getAbsolutePath(), fileName);
	}

	void notifyHandlers(@Nonnull EtcLogEvent event) {
		for(ILogHandler handler : getHandlers()) {
			handler.handle(event);
		}
	}

	@Nonnull
	private List<ILogHandler> getHandlers() {
		synchronized(m_handlersLock) {
			return m_handlers;
		}
	}

	static {
		SINGLETON = new EtcLoggerFactory();
		SINGLETON.initializeBuiltInLoggerConfig();
	}
}
