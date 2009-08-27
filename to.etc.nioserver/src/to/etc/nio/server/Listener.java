package to.etc.nio.server;

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.nio.channels.spi.*;
import java.util.*;

/**
 * This is a NIO high-performance nonblocking server listener. It accepts
 * connections and uses the core worker pool to handle data as it arrives
 * from the sockets. When a complete message has arrived it passes the message
 * to the router.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 26, 2006
 */
public class Listener {
	static private final long RECONNECT_INTERVAL = 60 * 1000;

	private ServerCore m_core;

	private int m_port;

	private ListenerState m_listenerState = ListenerState.STOPPED;

	private Thread m_thread;

	/** Time that a reopen of the socket channel should be attempted. */
	private long m_ts_reconnect;

	private boolean m_terminate;

	/** My server address, */
	private InetSocketAddress m_address;

	private Selector m_selector;

	private ServerSocketChannel m_serverChannel;

	public Listener(int port) {
		m_port = port;
	}

	public void log(String s) {
		m_core.log(this + ": " + s);
	}

	public void msg(String s) {
		m_core.msg(this + ": " + s);
	}

	@Override
	public String toString() {
		return "Listener[" + m_port + "]";
	}

	public ServerCore getCore() {
		return m_core;
	}

	/**
	 * Starts this listener. The server socket is opened to accept connections and the
	 * core listener thread is started. This fails only if the thread could not be started. If
	 * the socket could not be opened the listener enters "wait" mode where it waits to retry
	 * opening the server socket.
	 *
	 * @param core
	 * @throws Exception
	 */
	public void start(ServerCore core) throws Exception {
		m_core = core;
		synchronized(m_core) {
			if(m_listenerState != ListenerState.STOPPED)
				throw new IllegalStateException("The listener is not in STOPPED state but in " + m_listenerState + " state");
			m_listenerState = ListenerState.WAITING;
			m_ts_reconnect = 0; // Reconnect soonest
			m_terminate = false;
		}

		m_thread = new Thread(new Runnable() {
			public void run() {
				runMain();
			}
		});
		m_thread.setName("listener@" + m_port);
		m_thread.setDaemon(true);
		m_thread.start();
	}

	/**
	 * Force the listener to abort.
	 * @throws Exception
	 */
	public void terminate() {
		synchronized(m_core) {
			if(m_thread.isAlive()) {
				m_terminate = true;
				m_thread.interrupt();
			}
		}
	}

	public void waitForTermination() throws Exception {
		m_thread.join(10000);
		if(m_thread.isAlive())
			throw new IllegalStateException("Listener thread " + this + " won't stop.");
	}

	public ListenerState getListenerState() {
		synchronized(m_core) {
			return m_listenerState;
		}
	}

	public synchronized void tnDumpState(PrintWriter pw) throws Exception {
		pw.println("TCP listener on port " + m_port + ", status is " + getListenerState() + ", listener thread is " + (m_thread == null ? "NOT " : "") + "running");
	}

	/**
	 * Listener mainloop. 
	 *
	 */
	void runMain() {
		log("Starting.");
		for(;;) {
			switch(getListenerState()){
				default:
					throw new IllegalStateException("Invalid listener state in runMain");
				case LISTENING:
					listen();
					break;
				case WAITING:
					reconnect();
					break;
				case STOPPING:
					log("Stopped.");
					return;
			}
		}
	}

	private boolean hasTermRequest() {
		synchronized(m_core) {
			if(m_terminate)
				m_listenerState = ListenerState.STOPPING;
			return m_terminate;
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:  NIO Server open code.                       		*/
	/*--------------------------------------------------------------*/
	/**
	 * Wait until it's time to reconnect. Awake every 5 seconds 
	 */
	private boolean waitForReconnectTime() {
		for(;;) {
			if(hasTermRequest())
				return false;
			long ts = System.currentTimeMillis();
			if(ts >= m_ts_reconnect)
				break;
			try {
				Thread.sleep(m_ts_reconnect - ts);
			} catch(InterruptedException x) {
				//-- Ignore; just loop again (probably terminate request)
			}
		}

		//-- It's time..
		synchronized(m_core) {
			m_ts_reconnect = System.currentTimeMillis() + RECONNECT_INTERVAL;
		}
		return true;
	}

	/**
	 * Loops waiting until we can open the server socket succesfully. If opening
	 * it fails we close all resources we've allocated till then and sleep.
	 */
	private void reconnect() {
		if(!waitForReconnectTime()) // Wait till it's time and not terminated
			return; // Oops, we must terminate

		//-- Attempt to open the server channel shoops
		boolean ok = false;
		try {
			log("Opening server socket.");
			if(m_address == null) {
				//-- Allocate my address
				//                InetAddress ia = InetAddress.getLocalHost();
				m_address = new InetSocketAddress(m_port);
			}
			m_selector = SelectorProvider.provider().openSelector();
			m_serverChannel = ServerSocketChannel.open();
			m_serverChannel.configureBlocking(false);

			//-- Bind the server to my address
			m_serverChannel.socket().bind(m_address);
			m_serverChannel.register(m_selector, SelectionKey.OP_ACCEPT);

			//-- We're ready to roll!
			synchronized(m_core) {
				m_listenerState = ListenerState.LISTENING;
			}
			msg("Ready to accept connections.");
			ok = true;
		} catch(Exception x) {
			x.printStackTrace();
			m_core.exception(x, "reconnect failed");
		} finally {
			if(!ok) {
				try {
					if(m_selector != null)
						m_selector.close();
				} catch(Exception x) {}
				m_selector = null;
				try {
					if(m_serverChannel != null)
						m_serverChannel.close();
				} catch(Exception x) {}
				m_serverChannel = null;
			}
		}
	}

	/**
	 * Main listener and acceptor loop.
	 */
	private void listen() {
		msg("Starting the listener loop");
		for(;;) {
			try {
				int nread;
				while(0 < (nread = m_selector.select())) {
					if(false)
						msg("Got " + nread + " keys");
					Set<SelectionKey> keyset = m_selector.selectedKeys();
					for(Iterator<SelectionKey> it = keyset.iterator(); it.hasNext();) {
						SelectionKey k = it.next();
						it.remove();
						if(!k.isValid()) {
							log(this + ": invalid key discarded.");
						} else if(k.isAcceptable()) {
							//-- We have a new connection - handle it.
							SocketChannel newch = m_serverChannel.accept();
							newch.configureBlocking(false);

							// FIXME Refuse all connections from Capelle
							//                            if(newch.socket().getRemoteSocketAddress().toString().contains("80.126.40.201")) {
							//                                newch.close();
							//                            } else
							getCore().registerChannel(newch);
						}
					}
				}
			} catch(ClosedByInterruptException x) {
				if(!hasTermRequest())
					m_core.exception(x, "in listener loop of " + this);
				disconnect();
				return;
			} catch(Exception x) {
				x.printStackTrace();
				m_core.exception(x, "in listener loop of " + this);
				disconnect();
				return;
			}
		}
	}

	private void disconnect() {
		synchronized(m_core) {
			msg("Disconnecting server socket.");
			m_ts_reconnect = System.currentTimeMillis() + 10000;
			m_listenerState = ListenerState.WAITING;

			try {
				if(m_serverChannel != null)
					m_serverChannel.close();
			} catch(Exception x) {}
			m_serverChannel = null;
			try {
				if(m_selector != null)
					m_selector.close();
			} catch(Exception x) {}
			m_selector = null;
		}
	}

}
