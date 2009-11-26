package to.etc.nio.server;

import java.net.*;

/**
 * Data that is available while message fragments are being handled.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 27, 2006
 */
public interface ConnectionInfo {
	public void addSendCommand(ISendCommand wc) throws Exception;

	public NioOutputStream getCommandWriter();

	/**
	 * Normal disconnect. This causes a disconnect when all writes have completed.
	 * @throws Exception
	 */
	public void disconnect() throws Exception;

	/**
	 * Forces immediate disconnection, usually due to an error. All pending reads and writes are
	 * cancelled.
	 * @param why
	 */
	public void disconnectImmediately(Exception why);

	public String getRemoteAddress();

	public InetAddress getRemoteIpAddress();
}
