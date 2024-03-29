package to.etc.log;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.ILoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import to.etc.log.event.EtcLogEvent;
import to.etc.log.handler.ILogHandler;
import to.etc.log.handler.LogHandlerRegistry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static to.etc.log.LogUtil.createDocumentBuilderFactory;
import static to.etc.log.LogUtil.createTransformerFactory;

/**
 * Implements logger factory. Encapsulates definitions and configuration of loggers used.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Oct 30, 2012
 */
final public class EtcLoggerFactory implements ILoggerFactory {

	static public final String DEFAULT_CONFIG_FILENAME = "etclogger.config.xml";

	static public final String CONFIG_RESOURCE = "/to/etc/log/" + DEFAULT_CONFIG_FILENAME;

	/**
	 * The unique instance of this class.
	 */
	@NonNull
	private static final EtcLoggerFactory SINGLETON;

	@NonNull
	private static final ThreadLocal<SimpleDateFormat> DATEFORMATTER = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyMMdd");
		}
	};

	/** Root config file for logger configuration. */
	@Nullable
	private File m_writableConfig = new File(LogUtil.getTmpDir(), DEFAULT_CONFIG_FILENAME);

	/** Log dir where all logger are doing output. */
	@NonNull
	private File m_logDir = LogUtil.getTmpDir();

	/** logLocation stored value inside config file. */
	@Nullable
	private String m_logDirOriginalConfigured;

	/** Contains loaded Logger instances. */
	@NonNull
	private final Map<String, EtcLogger> LOGGERS = new HashMap<>();

	/** Contains handler instances - logger instances behavior definition. */
	@NonNull
	private List<ILogHandler> m_handlers = new CopyOnWriteArrayList<>();

	/** Default general log level */
	@NonNull
	private static final Level DEFAULT_LEVEL = Level.ERROR;

	private boolean m_initialized;

	/**
	 * Return the singleton of this class.
	 *
	 * @return the MyLoggerFactory singleton
	 */
	@NonNull
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
		public LoggerConfigException(@NonNull String msg) {
			super(msg);
		}
	}

	@Override
	@NonNull
	public EtcLogger getLogger(@NonNull String key) {
		initialize();
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

	private synchronized void initialize() {
		if(m_initialized)
			return;
		m_initialized = true;

		//System.out.println("[etclogger] Initializing");;
		try {
			String cfname = System.getProperty("LOGCONFIG");
			if(null != cfname) {
				File f = new File(cfname);
				if(!f.exists()) {
					System.err.println("[etclogger] Log config file " + f + " does not exist");
					return;
				}
				initializeFromFile(f);
				return;
			}

			initializeFromResource(CONFIG_RESOURCE, m_writableConfig);
		} catch(Exception x) {
			System.err.println("[etclogger] Initialization failed: " + x);
		}
	}

	@Nullable
	private Level calcLevel(@NonNull String key) {
		Level current = null;
		for(ILogHandler handler : getHandlers()) {
			Level level = handler.listenAt(key);
			if(current == null || (level != null && !current.includes(level))) {
				current = level;
			}
		}
		return current;
	}

	public void setLevelFor(String loggerPath, Level level) {
		for(ILogHandler handler : getHandlers()) {
			handler.setLogLevel(loggerPath, level);
		}
		recalculateLoggers();
	}

	/**
	 * Initialize from a classpath resource. If editableConfigPath has been passed AND if it
	 * exists its content will be used instead.
	 */
	public void initializeFromResource(String resourceName, @Nullable File editableConfigPath) throws Exception {
		if(initializeFromEditableFile(editableConfigPath))
			return;

		//-- Either no file or no editable config -> just load the specified resource
		String configXml = LogUtil.readResourceAsString(this.getClass(), resourceName, "utf-8");
		loadConfigFromXml(configXml);
		//System.out.println(getClass().getName() + "@" + System.identityHashCode(this) + " initialized from classpath resource " + resourceName);
		//if(null != m_writableConfig)
		//	System.out.println(getClass().getName() + " writable config file location set to " + m_writableConfig);
	}

	private boolean initializeFromEditableFile(@Nullable File editableConfigPath) {
		if(editableConfigPath != null) {
			if(editableConfigPath.canWrite()) {
				m_writableConfig = editableConfigPath;
			}

			if(editableConfigPath.exists()) {
				//-- Can we read the editable config file? Then prefer that for data
				try {
					initializeFromFile(editableConfigPath);
					return true;
				} catch(Exception x) {
				}
			}
		}
		return false;
	}


	public synchronized void initializeFromFile(@NonNull File configFile, @Nullable File editableConfigPath) throws Exception {
		if(initializeFromEditableFile(editableConfigPath))
			return;
		initializeFromFile(configFile);
	}

	/**
	 * Initialize the logger config from the given file.
	 */
	public synchronized void initializeFromFile(@NonNull File configFile) throws Exception {
		if(! configFile.exists())
			throw new IOException(configFile + ": file does not exist");

		String configXml = LogUtil.readFileAsString(configFile, "utf-8");
		loadConfigFromXml(configXml);
		//System.out.println(getClass().getName() + " initialized from file " + configFile);
	}

	/**
	 * Initialize the logger config from the given xml config.
	 */
	public synchronized void initializeFromConfigXml(@NonNull String configXml) throws Exception {
		loadConfigFromXml(configXml);
		//System.out.println(getClass().getName() + " initialized from " + configXml);
	}

	public synchronized void loadConfigFromXml(@NonNull String configXml) throws Exception {
		StringReader sr = null;
		try {
			DocumentBuilderFactory dbf = createDocumentBuilderFactory();
			DocumentBuilder db = dbf.newDocumentBuilder();
			sr = new StringReader(configXml);
			Document doc = db.parse(new InputSource(sr));
			loadConfig(doc);
		} finally {
			sr.close();
		}
	}

	@NonNull
	private ILogHandler loadHandler(@NonNull Node handlerNode) throws LoggerConfigException {
		Node typeNode = handlerNode.getAttributes().getNamedItem("type");
		if(typeNode == null) {
			throw new LoggerConfigException("Missing [type] attribute on <handler> element!");
		} else {
			String val = typeNode.getNodeValue();
			return LogHandlerRegistry.getSingleton().createHandler(val, m_logDir, handlerNode);
		}
	}

	public boolean canSave() {
		File writableConfig = m_writableConfig;
		try{
			if(null != writableConfig) {
				writableConfig.getParentFile().mkdirs();
				if(! writableConfig.canWrite()) {
					writableConfig.createNewFile();
				}
			}
		}catch(Exception ex) {
			//ignore
		}
		return null != writableConfig && writableConfig.canWrite();
	}

	/**
	 * Saves configuration of logger factory. Uses same root location as specified during .
	 */
	public void saveConfig() throws Exception {
		File writableConfig = m_writableConfig;
		if(! canSave() || writableConfig == null) {
			throw new IllegalStateException("The configuration cannot be saved: no output file or the output file is not writable");
		}

		Document doc = toXml(false);

		// write the content into xml file
		TransformerFactory transformerFactory = createTransformerFactory();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		try(Writer fw = new OutputStreamWriter(new FileOutputStream(writableConfig), "utf-8")) {
			StreamResult result = new StreamResult(fw);
			transformer.transform(source, result);
		}
	}

	@NonNull
	public Document toXml(boolean includeNonPerstistable) throws ParserConfigurationException {
		DocumentBuilderFactory dbf = createDocumentBuilderFactory().newInstance();
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

	@NonNull
	public File getLogDir() {
		return m_logDir;
	}

	@NonNull
	public String logDirOriginalAsConfigured() {
		return m_logDirOriginalConfigured;
	}

	public void loadConfig(@NonNull Document doc) throws LoggerConfigException {
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
					m_logDir = new File(logLocation).getAbsoluteFile();
					//System.out.println(getClass().getName() + " - log dir configured to : " + m_logDir.getAbsolutePath());
				} catch(Exception ex) {
					System.out.println(
						"Etc logger - problem in resolving logger configuration location from loaded default config: "
							+ m_logDirOriginalConfigured + ".\nUsing default location: "
							+ logLocation);
				}
			}
		}
		NodeList handlerNodes = doc.getElementsByTagName("handler");
		for(int i = 0; i < handlerNodes.getLength(); i++) {
			Node handlerNode = handlerNodes.item(i);
			loadedHandlers.add(loadHandler(handlerNode));
		}
		if(loadedHandlers.isEmpty()) {
			ILogHandler handler = LogHandlerRegistry.getSingleton().createDefaultHandler(getLogDir(), DEFAULT_LEVEL);
			loadedHandlers.add(handler);
		}
		m_handlers.clear();
		m_handlers.addAll(loadedHandlers);
		recalculateLoggers();
	}

	@NonNull
	public Level getDefaultLevel() {
		return DEFAULT_LEVEL;
	}

	@NonNull
	public String composeFullLogFileName(@NonNull String logPath, @NonNull String fileName) {
		String res;
		if(fileName.contains(":")) {
			res = fileName;
		} else {
			res = logPath + File.separator + fileName;
		}

		res += "_" + DATEFORMATTER.get().format(new Date()) + ".log";
		return res;
	}

	@NonNull
	public String composeFullLogFileName(@NonNull String fileName) {
		return composeFullLogFileName(m_logDir.getAbsolutePath(), fileName);
	}

	void notifyHandlers(@NonNull EtcLogEvent event) {
		for(ILogHandler handler : getHandlers()) {
			handler.handle(event);
		}
	}

	@NonNull
	private List<ILogHandler> getHandlers() {
		return m_handlers;
	}

	static {
		SINGLETON = new EtcLoggerFactory();
	}
}
