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
	private static final EtcLoggerFactory	SINGLETON	= new EtcLoggerFactory();
	
	private final DateFormat	m_df		= new SimpleDateFormat("yyMMdd");

	/** 
	 * Return the singleton of this class. 
	 * 
	 * @return the MyLoggerFactory singleton 
	 */
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
		public LoggerConfigException(String msg) {
			super(msg);
		}
	}

	/** Root dir for logger configuration and root path for created log files. */
	private File						m_rootDir;

	/** Log dir where all logger are doing output. */
	private File							m_logDir;

	/** logLocation stored value inside config file. */
	private String							m_logDirOriginalConfigured;

	/** Contains loaded Logger instances. */
	private final Map<String, EtcLogger>	LOGGERS			= new HashMap<String, EtcLogger>();

	/** Contains handler instances - logger instances behavior definition. */
	private final List<ILogHandler>		HANDLERS		= new ArrayList<ILogHandler>();

	/** Default general log level */
	private static final Level					DEFAULT_LEVEL	= Level.WARN;

	/** Name of logger factory configuration file */
	public static final String				CONFIG_FILENAME	= "etcLoggerConfig.xml";

	private boolean							m_initialized	= false;

	/**
	 * @see org.slf4j.ILoggerFactory#getLogger(java.lang.String)
	 */
	@Override
	public EtcLogger getLogger(@Nonnull String key) {
		return get(key);
	}

	private EtcLogger get(@Nonnull String key) {
		EtcLogger logger = null;
		synchronized(LOGGERS) {
			checkInitialized();
			logger = LOGGERS.get(key);
			if(logger == null) {
				logger = EtcLogger.create(key, calcLevel(key), HANDLERS);
				LOGGERS.put(key, logger);
			}
		}
		return logger;
	}

	private void checkInitialized() {
		if(!m_initialized) {
			try {
				initialize(new File(System.getProperty("user.home")));
			} catch(Exception e) {
				// This can not happen since we have stored class resource
				e.printStackTrace();
			}
		}
	}

	private Level calcLevel(String key) {
		Level current = null;
		for(ILogHandler handler : HANDLERS) {
			Level level = handler.listenAt(key);
			if(current == null || (level != null && !current.includes(level))) {
				current = level;
			}
		}
		return current;
	}

	/**
	 * Call to initialize logger factory with specified configXml.
	 * Sets rootLocation, that is location where configFile updates are persisted.
	 * Therefore rootLocation needs to have write permissions.
	 * In case that provided configuration is incorrect default configuration (persisted or built-in) is used.
	 * IMPORTANT: this needs to be executed earliest possible in application starting.
	 * 
	 * @param rootLocation
	 * @param configFile
	 * @throws Exception
	 */
	public synchronized void initialize(@Nonnull File rootLocation, @Nonnull String configXml) throws Exception {
		m_rootDir = rootLocation;
		rootLocation.mkdirs();
		ByteArrayInputStream bais = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			bais = new ByteArrayInputStream(configXml.getBytes());
			Document doc = db.parse(bais);
			loadConfig(doc);
			File conf = new File(rootLocation, CONFIG_FILENAME);
			if(!conf.exists()) {
				FileTool.writeFileFromString(conf, configXml, "utf-8");
			}
		} catch(LoggerConfigException ex) {
			System.err.println(ex);
			System.out.println("Invalid EtcLoggerFactory configuration - reloading default configuration...");
			initialize(rootLocation);
		} finally {
			if(bais != null) {
				bais.close();
			}
		}
	}

	/**
	 * Call to initialize logger factory with existing configuration.
	 * Sets rootLocation, that is location where configFile updates are persisted.
	 * Configuration always resides in {@link EtcLoggerFactory#CONFIG_FILENAME} file.
	 * In case that configuration is missing built-in configuration is created. 
	 * IMPORTANT: this needs to be executed earliest possible in application starting.
	 * 
	 * In case that logger factory has to be initialized with user specific configuration use {@link EtcLoggerFactory#initialize(File, String)}.   
	 *  
	 * @param rootLocation
	 * @throws Exception
	 */
	public synchronized void initialize(@Nonnull File rootLocation) throws Exception {
		m_rootDir = rootLocation;
		rootLocation.mkdirs();
		File conf = new File(rootLocation, CONFIG_FILENAME);
		String configXml = null;
		if(!conf.exists()) {
			configXml = FileTool.readResourceAsString(this.getClass(), CONFIG_FILENAME, "utf-8");
		} else {
			configXml = FileTool.readFileAsString(conf, "utf-8");
		}
		initialize(rootLocation, configXml);
	}

	private ILogHandler loadHandler(Node handlerNode) throws LoggerConfigException {
		Node typeNode = handlerNode.getAttributes().getNamedItem("type");
		if(typeNode == null) {
			throw new LoggerConfigException("Handler of undefined type found.");
		} else {
			String val = typeNode.getNodeValue();
			return LogHandlerFactory.getSingleton().createHandler(val, m_logDir, handlerNode);
		}
	}

	private void fixDefaultHandler() {
		if(HANDLERS.isEmpty()) {
			ILogHandler handler = LogHandlerFactory.getSingleton().createDefaultHandler(m_rootDir, DEFAULT_LEVEL);
			HANDLERS.add(handler);
		}
	}

	/**
	 * Saves configuration of logger factory. Uses same root location as specified during {@link EtcLoggerFactory#loadConfig(File)}.  
	 * @throws Exception
	 */
	public void saveConfig() throws Exception {
		Document doc = null;
		synchronized(HANDLERS) {
			doc = toXml(false);
		}

		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(m_rootDir, CONFIG_FILENAME));

		// Output to console for testing
		// StreamResult result = new StreamResult(System.out);

		transformer.transform(source, result);
	}

	public Document toXml(boolean includeNonPerstistable) throws ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.newDocument();
		Element rootElement = doc.createElement("config");
		doc.appendChild(rootElement);
		rootElement.setAttribute("logLocation", m_logDirOriginalConfigured);

		for(ILogHandler handler : HANDLERS) {
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
			m_initialized = true;
		}
	}

	public @Nonnull
	String getRootDir() {
		return new File(m_rootDir, CONFIG_FILENAME).getAbsolutePath();
	}

	public @Nonnull
	String getLogDir() {
		return m_logDir.getAbsolutePath();
	}

	public @Nonnull
	String logDirOriginalAsConfigured() {
		return m_logDirOriginalConfigured;
	}

	public void loadConfig(Document doc) throws LoggerConfigException {
		synchronized(HANDLERS) {
			HANDLERS.clear();
			doc.getDocumentElement().normalize();
			NodeList configNodes = doc.getElementsByTagName("config");
			if (configNodes.getLength() == 0){
				throw new LoggerConfigException("Missing config root node.");
			}else if (configNodes.getLength() > 1){
				throw new LoggerConfigException("Multiple config element nodes found.");
			}else{
				Node val = configNodes.item(0).getAttributes().getNamedItem("logLocation");
				if (val == null){
					throw new LoggerConfigException("Missing [logLocation] attribute in config root node.");
				}else{
					String logLocation = val.getNodeValue();
					m_logDirOriginalConfigured = logLocation;
					boolean checkNext = true;
					do {
						checkNext = false;
						int posStart = logLocation.indexOf("%");
						if (posStart > -1){
							int posEnd = logLocation.indexOf("%", posStart + 1);
							if (posEnd > -1){
								logLocation = logLocation.substring(0, posStart) + System.getProperty(logLocation.substring(posStart + 1, posEnd)) + logLocation.substring(posEnd + 1);
								checkNext = true;
							}
						}
					}while(checkNext);
					logLocation = logLocation.replace("/", File.separator);
					m_logDir = new File(logLocation);
					m_logDir.mkdirs();
				}
			}
			NodeList handlerNodes = doc.getElementsByTagName("handler");
			for(int i = 0; i < handlerNodes.getLength(); i++) {
				Node handlerNode = handlerNodes.item(i);
				HANDLERS.add(loadHandler(handlerNode));
			}
			fixDefaultHandler();
		}
		recalculateLoggers();
	}

	public Level getDefaultLevel() {
		return DEFAULT_LEVEL;
	}
	
	public String composeFullLogFileName(@Nonnull String logPath, @Nonnull String fileName) {
		String res;
		if(fileName.contains(":")) {
			res = fileName;
		} else {
			res = logPath + File.separator + fileName;
		}

		res += "_" + m_df.format(new Date()) + ".log";
		return res;
	}
	
	public String composeFullLogFileName(@Nonnull String fileName) {
		return composeFullLogFileName(m_logDir.getAbsolutePath(), fileName);
	}
}
