package to.etc.log.handler;

import javax.annotation.*;

import org.w3c.dom.*;

import to.etc.log.*;
import to.etc.log.event.*;

/**
 * Log handler proxy.
 * Beside built-in handlers enables implementing custom handlers.
 * New implementation of handler needs to be registered via {@link LogHandlerFactory#register(String, to.etc.log.handler.LogHandlerFactory.ILogHandlerMaker)}.   
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
	void handle(@Nonnull EtcLogEvent event);

	/**
	 * Returns minimum level at handler is interested into certain logger key (name). Returns null in case that logger is not of interest to handler.
	 * @param event
	 */
	@Nullable
	Level listenAt(@Nonnull String key);

	/**
	 * Saving handler into xml configuration.
	 * @param event
	 */
	void saveToXml(Document doc, Element handlerNode, boolean includeNonPerstistable);

	/**
	 * Defines if it is on-the-fly handler - if it should not be saved into relodable configuration. Used when handling is session specific.
	 * @param event
	 */
	boolean isTemporary();
}
