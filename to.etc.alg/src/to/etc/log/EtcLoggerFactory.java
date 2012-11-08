package to.etc.log;

import java.io.*;
import java.text.*;
import java.util.*;

import javax.annotation.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.slf4j.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import to.etc.log.event.*;
import to.etc.log.handler.*;
import to.etc.util.*;

/**
 * Implements logger factory. Encapsulates definitions and configuration of loggers used.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Oct 30, 2012
 */
public class EtcLoggerFactory implements ILoggerFactory {

	/**
	 * The unique instance of this class.
	 */
	private static final EtcLoggerFactory				SINGLETON;

	private static final ThreadLocal<SimpleDateFormat>	DATEFORMATTER	= new ThreadLocal<SimpleDateFormat>() {
																			@Override
																			protected SimpleDateFormat initialValue() {
																				return new SimpleDateFormat("yyMMdd");
																			}
																		};

	/**
	 * Return the singleton of this class.
	 *
	 * @return the MyLoggerFactory singleton
	 */
	public @Nonnull
	static final EtcLoggerFactory getSingleton() {
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
		public LoggerConfigException(String msg) {
			super(msg);
		}
	}

	/** Root dir for logger configuration. */
	private File							m_configDir;

	/** Log dir where all logger are doing output. */
	private File							m_logDir;

	/** logLocation stored value inside config file. */
	private String							m_logDirOriginalConfigured;

	/** Contains loaded Logger instances. */
	private final Map<String, EtcLogger>	LOGGERS			= new HashMap<String, EtcLogger>();

	/** Contains handler instances - logger instances behavior definition. */
	private List<ILogHandler>			m_handlers		= new ArrayList<ILogHandler>();

	private Object m_handlersLock = new Object();

	/** Default general log level */
	private static final Level				DEFAULT_LEVEL	= Level.ERROR;

	/** Name of logger factory configuration file */
	public static final String				CONFIG_FILENAME	= "etcLoggerConfig.xml";

	/**
	 * @see org.slf4j.ILoggerFactory#getLogger(java.lang.String)
	 */
	@Override
	public EtcLogger getLogger(@Nonnull String key) {
		EtcLogger logger = null;
		synchronized(LOGGERS) {
			logger = LOGGERS.get(key);
			if(logger == null) {
				logger = EtcLogger.create(key, calcLevel(key));
				LOGGERS.put(key, logger);
			}
		}
		return logger;
	}

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
	 * This creates built-in log configuration that would log to stdout until application specific configuration is initialized.
	 */
	private synchronized void initializeBuiltInLoggerConfig() {
		String configXml;
		try {
			configXml = FileTool.readResourceAsString(this.getClass(), CONFIG_FILENAME, "utf-8");
			loadConfigFromXml(configXml);
			System.out.println(this.getClass().getName() + " is initialized by loading built-in logger configuration as " + this.getClass().getName() + " resource " + CONFIG_FILENAME);
		} catch(Exception e) {
			//this should not happen -> we load design time created resource - it must be valid
			System.err.println("Built-in logger config is invalid! Check class " + this.getClass().getName() + " resource " + CONFIG_FILENAME);
			e.printStackTrace();
		}
	}

	/**
	 * Call to initialize logger factory from persited configuration.
	 * Sets rootLocation, that is location where configFile is persisted.
	 * Configuration always resides in {@link EtcLoggerFactory#CONFIG_FILENAME} file.
	 * In case that configuration is missing or fails to load, new configuration is created as built-in configuration.
	 * IMPORTANT: this needs to be executed earliest possible in application starting.
	 *
	 * In case that logger factory has to be initialized with predefined application specific configuration use {@link EtcLoggerFactory#initialize(File, String)}.
	 *
	 * @param configLocation
	 * @throws Exception
	 */
	public synchronized void initialize(@Nonnull File configLocation) throws Exception {
		m_configDir = configLocation;
		File conf = new File(configLocation, CONFIG_FILENAME);
		String configXml = null;
		if(conf.exists()) {
			configXml = FileTool.readFileAsString(conf, "utf-8");
			if(tryLoadConfigFromXml(configLocation, configXml)) {
				return;
			}
		}
		//if existing config does not exists or fails to load, use one from resource
		initializeBuiltInLoggerConfig();
	}

	/**
	 * Call to initialize logger factory from specified configXml.
	 * Sets rootLocation, that is location where changes in logger configuration would be persisted.
	 * Persited configuration always resides in {@link EtcLoggerFactory#CONFIG_FILENAME} file.
	 * Returns false in case that configuration fails to load, it does not try any other logger configuration.
	 * This method should be used only as special case when persisted configuration should be by passed temporary.
	 * IMPORTANT: this needs to be executed earliest possible in application starting.
	 *
	 * Usual way to initialize logger is to use {@link EtcLoggerFactory#initialize(File, String)}.
	 *
	 * @param configLocation
	 * @param configXml
	 * @return
	 */
	public synchronized boolean tryLoadConfigFromXml(@Nonnull File configLocation, @Nonnull String configXml) {
		m_configDir = configLocation;
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
	 * Firts it tries to load persited logger config, if such exists in specified location.
	 * If that fails, it tries to load specified defaultConfig
	 * If that also fails, it loads default built-in connfig.
	 * Since later changes in logger config would be persisted inside configLocation, it should exists with write permissions.
	 * IMPORTANT: logger config needs to be executed earliest possible in application starting.
	 *
	 * @param configLocation
	 * @param defaultConfig
	 * @return
	 * @throws Exception
	 */
	public synchronized void initialize(@Nonnull File configLocation, @Nonnull String defaultConfig) throws Exception {
		m_configDir = configLocation;
		System.out.println(this.getClass().getName() + " logger configuration location set to " + configLocation.getAbsolutePath());
		File conf = new File(configLocation, CONFIG_FILENAME);
		String configXml = null;
		if(conf.exists()) {
			//try 1 : try persited config
			configXml = FileTool.readFileAsString(conf, "utf-8");
			if(tryLoadConfigFromXml(configLocation, configXml)) {
				System.out.println(this.getClass().getName() + " is initialized by loading persisted logger configuration.");
				return;
			}
		}
		//try 2 : try defaultConfig
		if(tryLoadConfigFromXml(configLocation, defaultConfig)) {
			System.out.println(this.getClass().getName() + " is initialized by loading application specific default configuration.");
			return;
		}
		//try 3 : try built-in config
		initializeBuiltInLoggerConfig();
	}

	private synchronized void loadConfigFromXml(String configXml) throws Exception {
		StringReader sr = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			sr = new StringReader(configXml);
			Document doc = db.parse(new InputSource(sr));
			loadConfig(doc);
		} finally {
			FileTool.closeAll(sr);
		}
	}

	private @Nonnull
	ILogHandler loadHandler(@Nonnull Node handlerNode) throws LoggerConfigException {
		Node typeNode = handlerNode.getAttributes().getNamedItem("type");
		if(typeNode == null) {
			throw new LoggerConfigException("Missing [type] attribute on <handler> element!");
		} else {
			String val = typeNode.getNodeValue();
			return LogHandlerBuilder.getSingleton().createHandler(val, m_logDir, handlerNode);
		}
	}

	/**
	 * Saves configuration of logger factory. Uses same root location as specified during {@link EtcLoggerFactory#loadConfig(File)}.
	 * @throws Exception
	 */
	public void saveConfig() throws Exception {
		Document doc = null;
		doc = toXml(false);

		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(m_configDir, CONFIG_FILENAME));

		// Output to console for testing
		// StreamResult result = new StreamResult(System.out);

		transformer.transform(source, result);
	}

	public @Nonnull
	Document toXml(boolean includeNonPerstistable) throws ParserConfigurationException {
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

	public @Nonnull
	String getRootDir() {
		return new File(m_configDir, CONFIG_FILENAME).getAbsolutePath();
	}

	public @Nonnull
	String getLogDir() {
		return m_logDir.getAbsolutePath();
	}

	public @Nonnull
	String logDirOriginalAsConfigured() {
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
				boolean checkNext = true;
				do {
					checkNext = false;
					int posStart = logLocation.indexOf("%");
					if(posStart > -1) {
						int posEnd = logLocation.indexOf("%", posStart + 1);
						if(posEnd > -1) {
							logLocation = logLocation.substring(0, posStart) + System.getProperty(logLocation.substring(posStart + 1, posEnd)) + logLocation.substring(posEnd + 1);
							checkNext = true;
						}
					}
				} while(checkNext);
				logLocation = logLocation.replace("/", File.separator);
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
			ILogHandler handler = LogHandlerBuilder.getSingleton().createDefaultHandler(m_configDir, DEFAULT_LEVEL);
			loadedHandlers.add(handler);
		}
		synchronized(m_handlersLock){
			m_handlers = loadedHandlers;
		}
		recalculateLoggers();
	}

	public @Nonnull
	Level getDefaultLevel() {
		return DEFAULT_LEVEL;
	}

	public @Nonnull
	String composeFullLogFileName(@Nonnull String logPath, @Nonnull String fileName) {
		String res;
		if(fileName.contains(":")) {
			res = fileName;
		} else {
			res = logPath + File.separator + fileName;
		}

		res += "_" + DATEFORMATTER.get().format(new Date()) + ".log";
		return res;
	}

	public @Nonnull
	String composeFullLogFileName(@Nonnull String fileName) {
		return composeFullLogFileName(m_logDir.getAbsolutePath(), fileName);
	}

	void notifyHandlers(@Nonnull EtcLogEvent event) {
		for(ILogHandler handler : getHandlers()) {
			handler.handle(event);
		}
	}

	private @Nonnull
	List<ILogHandler> getHandlers() {
		synchronized(m_handlersLock){
			return m_handlers;
		}
	}

	static {
		SINGLETON = new EtcLoggerFactory();
		SINGLETON.initializeBuiltInLoggerConfig();
	}
}
