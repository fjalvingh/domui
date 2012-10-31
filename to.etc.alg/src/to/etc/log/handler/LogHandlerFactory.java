package to.etc.log.handler;

import java.io.*;
import java.util.*;

import javax.annotation.*;

import org.w3c.dom.*;

import to.etc.log.*;
import to.etc.log.EtcLoggerFactory.LoggerConfigException;

/**
 * Factory for different log handler types.  
 * 
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Oct 31, 2012
 */
public class LogHandlerFactory {

	private interface ILogHandlerMaker {
		ILogHandler createInstance(File logDir, Node handlerNode) throws LoggerConfigException;
	}

	private final Map<String, ILogHandlerMaker>	m_makerRegistry	= new HashMap<String, ILogHandlerMaker>();

	/** 
	 * The unique instance of this class. 
	 */
	private static final LogHandlerFactory		SINGLETON		= new LogHandlerFactory();

	/** 
	 * Return the singleton of this class. 
	 * 
	 * @return the LogHandlerFactory singleton 
	 */
	public static final LogHandlerFactory getSingleton() {
		return SINGLETON;
	}

	public void register(@Nonnull String type, @Nonnull ILogHandlerMaker handlerMaker) {
		m_makerRegistry.put(type, handlerMaker);
	}

	static {
		SINGLETON.register("stdout", new ILogHandlerMaker() {
			@Override
			public ILogHandler createInstance(@Nonnull File logDir, @Nonnull Node handlerNode) throws LoggerConfigException {
				return FileLogHandler.createFromStdoutTypeConfig(logDir, handlerNode);
			}
		});
		SINGLETON.register("file", new ILogHandlerMaker() {
			@Override
			public ILogHandler createInstance(@Nonnull File logDir, @Nonnull Node handlerNode) throws LoggerConfigException {
				return FileLogHandler.createFromFileTypeConfig(logDir, handlerNode);
			}
		});
	}

	public ILogHandler createHandler(@Nonnull String type, @Nonnull File logDir, @Nonnull Node handlerNode) throws LoggerConfigException {
		ILogHandlerMaker maker = m_makerRegistry.get(type);
		if(maker != null) {
			return maker.createInstance(logDir, handlerNode);
		} else {
			throw new LoggerConfigException("Unknown handler type found: " + type);
		}
	}

	public ILogHandler createDefaultHandler(File rootDir, Level level) {
		return FileLogHandler.createDefaultHandler(rootDir, level);
	}


}
