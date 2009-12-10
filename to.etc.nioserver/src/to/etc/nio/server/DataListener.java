package to.etc.nio.server;

import java.nio.channels.*;
import java.nio.channels.spi.*;
import java.util.*;

/**
 * This is a single data connection listener. It wraps a thread and a Selector
 * and is used to wait for READ and WRITE events on the selector for the set
 * of connections handled by this listener. Each listener accepts MAX_CONNS
 * connections before it is full.
 * The listener only accepts the keys from the selector for later processing
 * by the handler code.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 26, 2006
 */
public class DataListener implements Comparable<DataListener> {
	static private int m_lastid;

	/** The listener I'm a data receiver for (lock) */
	private ServerCore m_core;

	/** My index number */
	private int m_id;

	/** The selector */
	private Selector m_selector;

	/** The thread handling this reader. */
	private Thread m_thread;

	/** Protected by the listener, and accessed by the listener. */
	int m_connectionCount;

	private boolean m_terminated;

	private List<ConnectionHandler> m_newChannels = new ArrayList<ConnectionHandler>();

	public DataListener(ServerCore l) {
		m_core = l;
		m_id = newID();
	}

	static private synchronized int newID() {
		return ++m_lastid;
	}

	public ServerCore getCore() {
		return m_core;
	}

	public synchronized boolean isTerminated() {
		return m_terminated;
	}

	int getConnectionCount() {
		synchronized(m_core) {
			return m_connectionCount;
		}
	}

	public int compareTo(DataListener o) {
		return getConnectionCount() - o.getConnectionCount();
	}

	/**
	 * Attachs this thingy to a selector and makes sure the handler thread runs.
	 * @param sch
	 * @param ch
	 */
	void attach(SocketChannel sch) throws Exception {
		ConnectionMessageHandler mh = getCore().createMessageHandler();
		ConnectionHandler ch = new ConnectionHandler(this, mh, sch);
		mh.connect(ch, getCore());

		synchronized(m_core) {
			if(m_selector == null) {
				m_selector = SelectorProvider.provider().openSelector();
			}
			if(m_thread == null) {
				m_thread = new Thread(new Runnable() {
					public void run() {
						runMain();
					}
				});
				m_thread.setDaemon(true);
				m_thread.setName("reader#" + m_id);
				m_thread.start();
			}
		}

		//-- Now post the thingy for handling by the connector (asy). The connection will be picked up by the handler (hopefully)
		synchronized(this) {
			m_newChannels.add(ch); // Append to "todo" list
		}
		m_selector.wakeup(); // Reader thread wakeup to add new thingy.
		//
		//        synchronized(ch) {
		//            SelectionKey k = sch.register(m_selector, SelectionKey.OP_READ, ch);
		//            ch._setKey(k);
		//        }
	}

	/**
	 * Forces the data listener to terminate.
	 *
	 */
	void terminate() {
		m_core.log(this + ": terminate request received");
		List<ConnectionHandler> chl = null;
		Thread t = null;
		synchronized(this) {
			if(m_terminated)
				return;
			m_terminated = true;
			if(m_newChannels.size() > 0) {
				chl = m_newChannels;
				m_newChannels = new ArrayList<ConnectionHandler>();
			}
			m_connectionCount = 0;
			m_selector.wakeup();
			t = m_thread;
		}
		if(t != null) {
			m_core.log(this + ": waiting for my thread to die");
			try {
				t.join(5000);
			} catch(Exception x) {}
			if(t.isAlive())
				m_core.log(this + ": My thread does not die. We'll clean up regardless...");
			else
				m_core.log(this + ": Thread has died OK");
		} else
			m_core.log(this + ": No thread was running");

		//-- Disconnect all new pending connections
		DisconnectionException dc = new DisconnectionException("The server is stopping");
		if(chl != null) {
			m_core.log(this + ": disconnecting " + chl.size() + " pending data connections");
			for(ConnectionHandler ch : chl) {
				try {
					ch.disconnectImmediately(dc);
				} catch(Exception x) {
					m_core.exception(x, "Exception while cancelling pending data connection");
				}
			}
		}

		//-- Disconnect all existing stuff.
		SelectionKey[] ar = m_selector.keys().toArray(new SelectionKey[m_selector.keys().size()]);
		for(SelectionKey k : ar) {
			try {
				ConnectionHandler ch = (ConnectionHandler) k.attachment();
				if(ch != null) {
					ch.disconnectImmediately(dc);
				} else {
					k.channel().close();
				}
				try {
					k.cancel();
				} catch(Exception x) {}
			} catch(Exception x) {
				m_core.exception(x, "Error while disconnecting existing channels");
			}
		}
		m_core.log(this + ": Closing selector");
		try {
			m_selector.close();
		} catch(Exception x) {
			m_core.exception(x, "Error while closing the selector");
		}
		m_core.log(this + ": Termination completed");
	}

	void runMain() {
		getCore().msg("starting reader thread");
		while(getConnectionCount() > 0) {
			//            System.out.println("******* Conncount="+getConnectionCount());
			int retry = 0;
			try {
				readLoop();
				retry = 0;
			} catch(Exception x) {
				x.printStackTrace();
				retry++;
				if(retry > 10) {
					//-- Abort this listener.
					m_core.log("Too many exceptions: aborting thread");
					try {
						m_selector.close();
					} catch(Exception xx) {}
					return;
				}
			}
			//            System.out.println("**** End of data listener thread.");
		}
		synchronized(m_core) {
			m_thread = null;
			getCore().msg("Stopping reader thread");
		}
	}

	private void readLoop() throws Exception {
		int nread;
		for(;;) {
			if(getConnectionCount() <= 0 || isTerminated())
				return;
			nread = m_selector.select();
			if(isTerminated())
				return;
			if(false)
				m_core.msg("data: got " + nread + " keys");

			//-- First handle any new selectors.
			synchronized(this) {
				while(m_newChannels.size() > 0) {
					ConnectionHandler ch = m_newChannels.remove(m_newChannels.size() - 1);
					//                    m_core.msg("Appending one new channel: "+ch);
					synchronized(ch) {
						SelectionKey k = ch.channel().register(m_selector, SelectionKey.OP_READ, ch);
						ch._setKey(k);
					}
				}
			}

			Set<SelectionKey> keyset = m_selector.selectedKeys();
			for(Iterator<SelectionKey> it = keyset.iterator(); it.hasNext();) {
				SelectionKey k = it.next();
				it.remove();
				//                StringBuilder sb = new StringBuilder(80);
				//                sb.append("ready( ");
				//                if((k.readyOps() & SelectionKey.OP_ACCEPT) != 0)
				//                    sb.append("accept ");
				//                if((k.readyOps() & SelectionKey.OP_CONNECT) != 0)
				//                    sb.append("connect ");
				//                if((k.readyOps() & SelectionKey.OP_READ) != 0)
				//                    sb.append("read ");
				//                if((k.readyOps() & SelectionKey.OP_WRITE) != 0)
				//                    sb.append("write ");
				//                sb.append(") for conn=");
				//                sb.append(k.channel().toString());
				//                System.out.println(sb.toString());

				//-- Disable all events.
				k.interestOps(k.interestOps() & ~(SelectionKey.OP_READ | SelectionKey.OP_WRITE));
				//                int mask = 0;
				//
				//                if(k.isAcceptable())
				//                    mask |= SelectionKey.OP_ACCEPT;
				//                if(k.isConnectable())
				//                    mask |= SelectionKey.OP_CONNECT;
				//                if(k.isReadable())
				//                    mask |= SelectionKey.OP_READ;
				//                if(k.isWritable())
				//                    mask |= SelectionKey.OP_WRITE;
				m_core.appendIO(this, k);
			}
		}
	}

	void connectionClosed() {
		m_core.decrement(this);
	}

	void wakeup() {
		m_selector.wakeup();
	}

	@Override
	public String toString() {
		return "[DataListener:" + m_id + "]";
	}
}
