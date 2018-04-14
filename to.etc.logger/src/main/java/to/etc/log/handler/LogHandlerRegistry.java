package to.etc.log.handler;

import org.eclipse.jdt.annotation.NonNull;
import org.w3c.dom.Node;
import to.etc.log.EtcLoggerFactory;
import to.etc.log.EtcLoggerFactory.LoggerConfigException;
import to.etc.log.Level;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory for different log handler types. Contains registry of {@link ILogHandlerFactory}.
 * Provides factory {@link LogHandlerRegistry#createHandler(String, File, Node)} that creates {@link ILogHandler} instance based on provided String type.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Oct 31, 2012
 */
public class LogHandlerRegistry {

	/**
	 * Factory for {@link ILogHandler} instance.
	 *
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Nov 8, 2012
	 */
	public interface ILogHandlerFactory {
		/**
		 * Creates {@link ILogHandler} from specified xml definition. Uses logDir as preferred location for created log outputs, if such are created by handler.
		 * @param logDir
		 * @param handlerNode
		 * @return
		 * @throws LoggerConfigException
		 */
		@NonNull
		ILogHandler createInstance(@NonNull File logDir, @NonNull Node handlerNode) throws LoggerConfigException;
	}

	@NonNull
	private final Map<String, ILogHandlerFactory>	m_makerRegistry	= new HashMap<String, ILogHandlerFactory>();

	/**
	 * The unique instance of this class.
	 */
	@NonNull
	private static final LogHandlerRegistry		SINGLETON		= new LogHandlerRegistry();

	/**
	 * Return the singleton of this class.
	 *
	 * @return the LogHandlerFactory singleton
	 */
	@NonNull
	public static final LogHandlerRegistry getSingleton() {
		return SINGLETON;
	}

	/**
	 * Register {@link ILogHandlerFactory} for specified String type.
	 * @param type
	 * @param handlerFactory
	 */
	public synchronized void register(@NonNull String type, @NonNull ILogHandlerFactory handlerFactory) {
		m_makerRegistry.put(type, handlerFactory);
	}

	static {
		SINGLETON.register("stdout", new ILogHandlerFactory() {
			@Override
			public ILogHandler createInstance(@NonNull File logDir, @NonNull Node handlerNode) throws LoggerConfigException {
				return FileLogHandler.createFromStdoutTypeConfig(logDir, handlerNode);
			}
		});
		SINGLETON.register("file", new ILogHandlerFactory() {
			@Override
			public ILogHandler createInstance(@NonNull File logDir, @NonNull Node handlerNode) throws LoggerConfigException {
				return FileLogHandler.createFromFileTypeConfig(logDir, handlerNode);
			}
		});
	}

	/**
	 * Creates {@link ILogHandler} instance based on registered factory for specified type.
	 * NOTE: this should not be used directly - meant to be used only internally by {@link EtcLoggerFactory}. But left public since logger factory is not in same package.
	 * @param type
	 * @param logDir
	 * @param handlerNode
	 * @return
	 * @throws LoggerConfigException
	 */
	@NonNull
	public synchronized ILogHandler createHandler(@NonNull String type, @NonNull File logDir, @NonNull Node handlerNode) throws LoggerConfigException {
		ILogHandlerFactory maker = m_makerRegistry.get(type);
		if(maker != null) {
			return maker.createInstance(logDir, handlerNode);
		} else {
			throw new LoggerConfigException("Unknown handler type found: " + type);
		}
	}

	/**
	 * Creates default handler.
	 * NOTE: this should not be used directly - meant to be used only internally by {@link EtcLoggerFactory}. But left public since logger factory is not in same package.
	 *
	 * @param rootDir
	 * @param level
	 * @return
	 */
	@NonNull
	public ILogHandler createDefaultHandler(@NonNull File rootDir, @NonNull Level level) {
		return FileLogHandler.createDefaultHandler(rootDir, level);
	}
}
