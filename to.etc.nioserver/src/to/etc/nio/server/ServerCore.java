package to.etc.nio.server;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

import to.etc.log.*;
import to.etc.telnet.*;
import to.etc.util.*;

/**
 * Main code for the MServer. This is a very high-performance NIO-based server which
 * allows for a shitload of connections. It uses NIO in nonblocking mode to allow
 * the #of connections we'll ultimately need.
 * The threading model is as follows:
 * <ul>
 *  <li>For every port we listen on we have a Listener+Thread which accepts
 *      incoming connections on the port. The only task of this thread is to
 *      find the DataListener with the least #of open connections and register
 *      the new connection with that listener. If all DataListeners have more
 *      than the allowed max.#of connections open (63) it allocates a new
 *      datalistener and adds the connection there.</li>
 *  <li>Each DataListener+Thread selects() on all it's connections (to a max of
 *      63). When any connection gets a select event then the only task of this thread
 *      is to remove the key, disable it's interests and queue it on a central worker
 *      queue.</li>
 *  <li>The request is actually handled by a pool of Worker threads all reading from
 *      the work queue.
 * </ul>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 26, 2006
 */
public class ServerCore implements ILogSink {
	static public final boolean DEBUG = false;

	static private final int MAX_PER_DATALISTENER = 60;

	private RotatingLogfile m_log;

	/** The handler factory for this server. */
	private ConnectionMessageHandlerFactory m_factory;

	/** All registered listeners. */
	private List<Listener> m_listenerList = new ArrayList<Listener>();

	private ServerState m_serverState = ServerState.STOPPED;

	/** The pool executor for queued commands */
	private ThreadPoolExecutor m_executor;

	/** The list of available byte buffers. */
	private List<ByteBuffer> m_freeList = new ArrayList<ByteBuffer>();

	/** The total #of buffers allocated currently. */
	private long m_nTotalBuffersAllocated;

	private long m_nMaxBuffersAllocated;

	/** The largest #of commands that have ever been queued */
	//    private int                 m_nMaxQueuedCommands;

	private boolean m_allocateDirect = true;

	/** T if data handled by this server is little-endian. */
	private boolean m_littleEndian;

	private TreeSet<DataListener> m_dataListeners = new TreeSet<DataListener>();

	private boolean m_logReceive, m_logWrites;

	/**
	 * The queue of all pending commands accepted by all listeners, in order of acceptance.
	 */
	private BlockingQueue<Runnable> m_ioQueue = new ArrayBlockingQueue<Runnable>(10);

	public ServerCore(RotatingLogfile log, ConnectionMessageHandlerFactory f) {
		if(f == null)
			throw new NullPointerException("no connection message factory");
		if(log == null)
			throw new NullPointerException("no log");
		m_log = log;
		m_factory = f;
	}

	public synchronized boolean isLoggingReceive() {
		return m_logReceive;
	}

	public synchronized boolean isLoggingSend() {
		return m_logWrites;
	}

	public synchronized void setLoggingReceive(boolean on) {
		m_logReceive = on;
	}

	public synchronized void setLoggingSend(boolean on) {
		m_logWrites = on;
	}

	ConnectionMessageHandler createMessageHandler() {
		return m_factory.createMessageHandler();
	}

	public final boolean isLittleEndian() {
		return m_littleEndian;
	}

	public final synchronized ServerState getState() {
		return m_serverState;
	}

	public void terminate() {
		msg("Terminating all of my listeners and data handlers");
		List<Listener> list;
		Set<DataListener> dataset;
		synchronized(this) {
			if(m_serverState != ServerState.RUNNING)
				throw new IllegalStateException("The server is not in RUNNING state but in " + m_serverState);
			m_serverState = ServerState.STOPPING;
			list = new ArrayList(m_listenerList);
			dataset = m_dataListeners;
			m_dataListeners = new TreeSet<DataListener>();
		}
		//-- Terminate all listeners
		for(Listener l : list)
			l.terminate();

		//-- Now terminate all of the data listeners. This closes all connections.
		log("Terminating all " + dataset.size() + " data listeners");
		for(DataListener dl : dataset) {
			dl.terminate();
		}
		log("All listeners terminated. Terminating the executor.");
		m_executor.shutdown();
		try {
			m_executor.awaitTermination(60, TimeUnit.SECONDS);
			//        } catch(InterruptedException x) {
			//            //-- ignore
		} catch(Exception x) {
			exception(x, "Exception while waiting for executor to stop");
		}
		log("Executor shutdown " + (m_executor.isTerminated() ? "okay" : "failed"));
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	LogSink implementation.                          	*/
	/*--------------------------------------------------------------*/
	public void log(String msg) {
		System.out.println("msg: " + msg);
		m_log.log(msg);
	}

	public void debug(String s) {
		if(DEBUG)
			m_log.log("dbg: " + s);
	}

	public void exception(Throwable x, String msg) {
		System.out.println("exc: " + msg);
		x.printStackTrace();
		m_log.exception(x, msg);
	}

	public void msg(String msg) {
		log(msg);
	}

	public synchronized void register(Listener l) {
		if(m_serverState != ServerState.STOPPED)
			throw new IllegalStateException("Server is not in STOPPED state");
		m_listenerList.add(l);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:  Byte buffer pool manager.                   		*/
	/*--------------------------------------------------------------*/

	/**
	 * Allocates a byte buffer for a handler thread.
	 */
	public ByteBuffer allocateBuffer() {
		synchronized(m_freeList) {
			if(m_freeList.size() > 0) {
				ByteBuffer bb = m_freeList.remove(m_freeList.size() - 1);
				return bb;
			}

			//-- Allocate a new byte buffer.
			ByteBuffer bb = null;
			if(m_allocateDirect) {
				try {
					bb = ByteBuffer.allocateDirect(2048);
				} catch(Exception x) {
					x.printStackTrace();
				}
				if(bb != null) {
					m_nTotalBuffersAllocated++;
					if(m_nTotalBuffersAllocated > m_nMaxBuffersAllocated)
						m_nMaxBuffersAllocated = m_nTotalBuffersAllocated;
					return bb;
				}
				m_allocateDirect = false;
			}
			bb = ByteBuffer.allocate(2048);
			m_nTotalBuffersAllocated++;
			if(m_nTotalBuffersAllocated > m_nMaxBuffersAllocated)
				m_nMaxBuffersAllocated = m_nTotalBuffersAllocated;
			return bb;
		}
	}

	void release(ByteBuffer bb) {
		synchronized(m_freeList) {
			if(m_freeList.size() < 80) {
				m_freeList.add(bb);
				return;
			}
			m_nTotalBuffersAllocated--;
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:  Server startup and termination.                 	*/
	/*--------------------------------------------------------------*/

	public void start() throws Exception {
		List<Listener> todo = new ArrayList<Listener>(m_listenerList); // Dup the listeners
		synchronized(this) {
			if(m_serverState != ServerState.STOPPED)
				throw new IllegalStateException("The server is not in STOPPED state but in " + m_serverState + " state.");
			m_serverState = ServerState.STARTING;
		}
		m_dataListeners.add(new DataListener(this));
		m_dataListeners.add(new DataListener(this));

		//-- Start all listeners. If this fails we quit all listeners that were started, then we abort.
		List<Listener> started = new ArrayList<Listener>();
		Listener listener = null;
		Exception error = null;
		try {
			msg("Starting " + todo.size() + " listeners");
			for(Listener l : todo) {
				listener = l;
				l.start(this);
				started.add(l);
			}
		} catch(Exception x) {
			exception(x, "In starting listener " + listener + ": " + x);
			error = x;
		}

		//-- If we're not in trouble move to RUNNING state and exit,
		if(error == null) {
			m_executor = new ThreadPoolExecutor(2, 40, 10, TimeUnit.SECONDS, m_ioQueue);

			msg("The server has started " + m_listenerList.size() + " listeners and is now running.");
			synchronized(this) {
				m_serverState = ServerState.RUNNING;
			}
			return;
		}

		//-- Discard all listeners that were started synchronously.
		log("Server aborting. Terminating all started listeners");
		synchronized(this) {
			m_serverState = ServerState.STOPPING;
		}
		for(Listener l : started) {
			try {
				msg("Terminating listener " + l);
				l.terminate();
				l.waitForTermination();
			} catch(Exception x) {
				exception(x, "Cannot terminate listener " + l + ": " + x);
			}
		}
		synchronized(this) {
			m_serverState = ServerState.STOPPED;
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IO queuer and worker servers.                    	*/
	/*--------------------------------------------------------------*/
	void decrement(DataListener dl) {
		synchronized(this) {
			if(m_serverState == ServerState.RUNNING) {
				m_dataListeners.remove(dl);
				dl.m_connectionCount--;
				m_dataListeners.add(dl);
			}
			//            System.out.println("**** connection count decremented to "+dl.m_connectionCount);
			dl.wakeup();
		}
	}

	/**
	 * Register a channel just accepted with one of the data readers. We use the
	 * data reader that has the least connections associated with it.
	 * @param ch
	 */
	void registerChannel(SocketChannel ch) throws IOException {
		//-- Find or allocate a data listener
		DataListener dl = null;
		synchronized(this) {
			if(m_serverState != ServerState.RUNNING) {
				//-- Discard socket and abort,
				try {
					ch.close();
				} catch(Exception x) {}
				throw new IllegalStateException("The server is in " + m_serverState + " state and cannot accept connections now");
			}
			dl = m_dataListeners.first();
			if(dl.m_connectionCount > MAX_PER_DATALISTENER)
				dl = new DataListener(this);
			else
				m_dataListeners.remove(dl); // Remove @old count
			dl.m_connectionCount++;
			m_dataListeners.add(dl); // add @new count
			boolean ok = false;
			try {
				dl.attach(ch); // Attach this socket with handler.
				ok = true;
			} catch(Exception x) {
				//--
				x.printStackTrace();
				exception(x, "Cannot queue new connection with DataListener: " + x);
			} finally {
				if(!ok) {
					//-- Discard this!
					try {
						ch.close();
					} catch(Exception x) {}
					decrement(dl);
				}
			}
		}
	}

	/**
	 * Adds a new key to the queue. Any task waiting on the queue is awakened.
	 */
	void appendIO(DataListener dl, SelectionKey k) {
		//        System.out.println("Queueing, qsize="+m_ioQueue.size());
		try {
			if(getState() != ServerState.RUNNING) {
				log("Ignored awakened key " + k + " because server is in state " + getState());
				return;
			}

			ConnectionHandler ch = (ConnectionHandler) k.attachment();
			//            ch._setKey(k);            Now done when the thingy is registered
			m_executor.execute(ch);
			//            m_ioQueue.add(new DataWorkerThread(this, dl, k));
		} catch(Exception x) {
			x.printStackTrace();
			System.out.println("Fatal: queue exception.");
			System.exit(10);
		}
	}

	public synchronized List<Listener> getListeners() {
		return new ArrayList<Listener>(m_listenerList);
	}

	/*--------------------------------------------------------------*/
	/*	CODING: Status reporting for debug.                  		*/
	/*--------------------------------------------------------------*/
	public void tnDumpFullStatus(TelnetPrintWriter pw) throws Exception {
		ServerState ss;
		synchronized(this) {
			ss = m_serverState;
			pw.println("The server's state is " + ss);
			if(m_allocateDirect)
				pw.println("This server uses DIRECT allocated NIO buffers");
			else
				pw.println("This server does NOT use DIRECT allocated NIO buffers (not supported on platform)");
			pw.println("Byte order is " + (m_littleEndian ? "little-endian" : "big-endian"));
			pw.println("Buffers:");
			pw.clear();
			pw.header("Allocated from OS", ExtendedPrintWriter.COMMAD);
			pw.header("Peak allocated", ExtendedPrintWriter.COMMAD);
			pw.header("Currently free", ExtendedPrintWriter.COMMAD);
			pw.header("Currently used", ExtendedPrintWriter.COMMAD);

			pw.out(m_nTotalBuffersAllocated);
			pw.out(m_nMaxBuffersAllocated);
			pw.out(m_freeList.size());
			pw.out(m_nTotalBuffersAllocated - m_freeList.size());
			pw.clear();
		}

		List<Listener> ll = getListeners();
		pw.println("The server has " + ll.size() + " active listeners:");
		for(Listener l : ll) {
			l.tnDumpState(pw);
		}

		//-- Dump the data listeners.
	}

}
