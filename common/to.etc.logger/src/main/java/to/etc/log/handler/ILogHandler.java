package to.etc.log.handler;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import to.etc.log.Level;
import to.etc.log.event.EtcLogEvent;

/**
 * Log handler proxy.
 * Beside built-in handlers enables implementing custom handlers.
 * New implementation of handler needs to be registered via {@link LogHandlerRegistry#register(String, to.etc.log.handler.LogHandlerRegistry.ILogHandlerMaker)}.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Oct 31, 2012
 */
public interface ILogHandler {
	/**
	 * Handles logging event.
	 * @param event
	 */
	void handle(@NonNull EtcLogEvent event);

	/**
	 * Returns minimum level at handler is interested into certain logger key (name). Returns null in case that logger is not of interest to handler.
	 * @param event
	 */
	@Nullable
	Level listenAt(@NonNull String key);

	/**
	 * Saving handler into xml configuration.
	 * @param doc
	 * @param handlerNode
	 * @param includeNonPersistable If T, also saves data that is not meant to be saved as part of persistent config. Used to send logger configuration to other interested parties.
	 */
	void saveToXml(@NonNull Document doc, @NonNull Element handlerNode, boolean includeNonPersistable);

	/**
	 * Defines if it is on-the-fly handler - if it should not be saved into reload-able configuration. Used when handling is session specific.
	 * @param event
	 */
	boolean isTemporary();
}
