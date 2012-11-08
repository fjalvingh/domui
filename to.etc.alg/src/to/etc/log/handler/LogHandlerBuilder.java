package to.etc.log.handler;

import java.io.*;
import java.util.*;

import javax.annotation.*;

import org.w3c.dom.*;

import to.etc.log.EtcLoggerFactory.LoggerConfigException;
import to.etc.log.*;

/**
 * Factory for different log handler types. Contains registry of {@link ILogHandlerFactory}.
 * Priovides factory {@link LogHandlerBuilder#createHandler(String, File, Node)} that creates {@link ILogHandler} instance based on provided String type.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Oct 31, 2012
 */
public class LogHandlerBuilder {

	/**
	 * Factory for {@link ILogHandler} instance.
	 *
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Nov 8, 2012
	 */
	public interface ILogHandlerFactory {
		/**
		 * Creates {@link ILogHandler} from specified xml definition. Uses logDir as prefered location for created log outputs, if such are created by handler.
		 * @param logDir
		 * @param handlerNode
		 * @return
		 * @throws LoggerConfigException
		 */
		@Nonnull
		ILogHandler createInstance(@Nonnull File logDir, @Nonnull Node handlerNode) throws LoggerConfigException;
	}

	private final Map<String, ILogHandlerFactory>	m_makerRegistry	= new HashMap<String, ILogHandlerFactory>();

	/**
	 * The unique instance of this class.
	 */
	private static final LogHandlerBuilder		SINGLETON		= new LogHandlerBuilder();

	/**
	 * Return the singleton of this class.
	 *
	 * @return the LogHandlerFactory singleton
	 */
	public static final LogHandlerBuilder getSingleton() {
		return SINGLETON;
	}

	/**
	 * Register {@link ILogHandlerFactory} for specified String type.
	 * @param type
	 * @param handlerFactory
	 */
	public synchronized void register(@Nonnull String type, @Nonnull ILogHandlerFactory handlerFactory) {
		m_makerRegistry.put(type, handlerFactory);
	}

	static {
		SINGLETON.register("stdout", new ILogHandlerFactory() {
			@Override
			public ILogHandler createInstance(@Nonnull File logDir, @Nonnull Node handlerNode) throws LoggerConfigException {
				return FileLogHandler.createFromStdoutTypeConfig(logDir, handlerNode);
			}
		});
		SINGLETON.register("file", new ILogHandlerFactory() {
			@Override
			public ILogHandler createInstance(@Nonnull File logDir, @Nonnull Node handlerNode) throws LoggerConfigException {
				return FileLogHandler.createFromFileTypeConfig(logDir, handlerNode);
			}
		});
	}

	/**
	 * Creates {@link ILogHandler} instance based on registered factory for specified type.
	 * @param type
	 * @param logDir
	 * @param handlerNode
	 * @return
	 * @throws LoggerConfigException
	 */
	public synchronized @Nonnull
	ILogHandler createHandler(@Nonnull String type, @Nonnull File logDir, @Nonnull Node handlerNode) throws LoggerConfigException {
		ILogHandlerFactory maker = m_makerRegistry.get(type);
		if(maker != null) {
			return maker.createInstance(logDir, handlerNode);
		} else {
			throw new LoggerConfigException("Unknown handler type found: " + type);
		}
	}

	/**
	 * Creates dafault hanlder.
	 * @param rootDir
	 * @param level
	 * @return
	 */
	public @Nonnull
	ILogHandler createDefaultHandler(@Nonnull File rootDir, @Nonnull Level level) {
		return FileLogHandler.createDefaultHandler(rootDir, level);
	}


}
