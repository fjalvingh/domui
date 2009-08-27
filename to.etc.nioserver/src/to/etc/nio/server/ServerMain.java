package to.etc.nio.server;

import to.etc.util.*;

/**
 * MServer startup class. This reads the server's configuration, then
 * starts the listeners.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 26, 2006
 */
public class ServerMain {
	private ServerCore m_core;

	private void run(String[] args) throws Exception {
		RotatingLogfile rlf = new RotatingLogfile("/tmp/mserver.log");
		m_core = new ServerCore(rlf, null);
		Listener l = new Listener(8987); // TODO Replace with config-based code.
		m_core.register(l);
		m_core.start();

		System.out.println("Just sleeping");
		Thread.sleep(60000);
		System.out.println("Terminating");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new ServerMain().run(args);
		} catch(Exception x) {
			x.printStackTrace();
		}
	}

}
