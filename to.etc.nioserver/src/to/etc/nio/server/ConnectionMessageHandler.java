package to.etc.nio.server;

import java.nio.*;

import to.etc.util.*;

/**
 * This interface should be implemented by stuff that wants to
 * recognise messages. Every opened connection to the server
 * gets the specified instance of this interface associated
 * with it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 27, 2006
 */
public interface ConnectionMessageHandler {
	/**
	 * This gets called as soon as a connection is established.
	 * @param ci
	 * @throws Exception
	 */
	public void connect(ConnectionInfo ci, ILogSink ls) throws Exception;

	public void received(ConnectionInfo ci, ByteBuffer bb) throws Exception;

	public void canWrite(ConnectionInfo ci, ByteBuffer bb) throws Exception;

	public void disconnected(ConnectionInfo ci, Throwable why) throws Exception;
}
