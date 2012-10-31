package to.etc.log;

import java.io.*;
import java.util.*;

import javax.annotation.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.slf4j.*;
import org.w3c.dom.*;

import to.etc.log.handler.*;

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

	/** Contains loaded Logger instances. */
	private final Map<String, EtcLogger>	LOGGERS			= new HashMap<String, EtcLogger>();

	/** Contains handler instances - logger instances behavior definition. */
	private final List<ILogHandler>		HANDLERS		= new ArrayList<ILogHandler>();

	/** Default location of created log files. Relative to root dir. */
	private static final String								DEFAULT_LOG_DIR			= "log";

	/** Default general log level */
	private static final Level					DEFAULT_LEVEL	= Level.WARN;

	/** Name of logger factory configuration file */
	private static final String					CONFIG_FILENAME	= "loggerConfig.xml";

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
			logger = LOGGERS.get(key);
			if(logger == null) {
				logger = EtcLogger.create(key, calcLevel(key), HANDLERS);
				LOGGERS.put(key, logger);
			}
		}
		return logger;
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
	 * Call to initialize logger factory.
	 * Loads logging configuration from specified rootLocation.
	 * Configuration resides in {@link EtcLoggerFactory#CONFIG_FILENAME} file.
	 * In case that configuration is missing default configuration is created. 
	 * IMPORTANT: this needs to be executed early as possible.
	 *  
	 * @param rootLocation
	 * @throws Exception
	 */
	public synchronized void loadConfig(@Nonnull File rootLocation) throws Exception {
		m_rootDir = rootLocation;
		rootLocation.mkdirs();
		File conf = new File(rootLocation, CONFIG_FILENAME);
		if(!conf.exists()) {
			makeDefaultConfig(rootLocation);
		}
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(conf);
		loadConfig(doc);
	}

	private ILogHandler loadHandler(Node handlerNode) throws LoggerConfigException {
		Node typeNode = handlerNode.getAttributes().getNamedItem("type");
		if(typeNode == null) {
			throw new LoggerConfigException("Handler of undefined type found.");
		} else {
			String val = typeNode.getNodeValue();
			return LogHandlerFactory.getSingleton().createHandler(val, new File(m_rootDir, DEFAULT_LOG_DIR), handlerNode);
		}
	}

	private void makeDefaultConfig(File root) throws Exception {
		fixDefaultHandler();
		saveConfig();
		recalculateLoggers();
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

		for(ILogHandler handler : HANDLERS) {
			Element handlerNode = doc.createElement("handler");
			rootElement.appendChild(handlerNode);
			handler.saveToXml(doc, handlerNode, includeNonPerstistable);
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
		return new File(m_rootDir, CONFIG_FILENAME).getAbsolutePath();
	}

	public @Nonnull
	String getLogDir() {
		return new File(m_rootDir, DEFAULT_LOG_DIR).getAbsolutePath();
	}

	public void loadConfig(Document doc) throws LoggerConfigException {
		synchronized(HANDLERS) {
			HANDLERS.clear();
			doc.getDocumentElement().normalize();
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

	/*
	 * FIXME: see if it would be useful to do this automatic initialization of logger.
	 * Currently we use alternative - loading inside application - that enables logger location per application.
	 *  
	  	static{
			try {
				getSingleton().loadConfig(new File(System.getProperty("user.home") + File.separatorChar + "logger"));
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	*/
}
