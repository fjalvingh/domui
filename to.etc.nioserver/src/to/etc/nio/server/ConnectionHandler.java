package to.etc.nio.server;

import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import to.etc.util.*;

/**
 * This class gets associated with a connection thru the attach() method
 * on it's key. It handles data read, write and disconnect stuff. This is
 * a "state" handler which turns the individual calls from the reader and
 * writers to a consistent whole.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 26, 2006
 */
public class ConnectionHandler implements Runnable, ConnectionInfo {
	static private int m_lastID;

	private int m_id;

	/** The key that belongs to this connection, */
	private SelectionKey m_key;

	private DataListener m_dl;

	/** The allocated byte buffer, while running. */
	private ByteBuffer m_bb;

	/** The user-defined handler which accepts all events. */
	private ConnectionMessageHandler m_cmh;

	private String m_ident;

	/** Concurrency check - for debug pps */
	private int m_depth;

	/** Set to an exception when the thingy has disconnected. */
	private Throwable m_disconnected;

	/** When T the connection should be closed as soon as writing completes. */
	private boolean m_pendingDisconnect;

	private SocketChannel m_channel;

	ConnectionHandler(DataListener dl, ConnectionMessageHandler mh, SocketChannel sch) {
		m_dl = dl;
		m_cmh = mh;
		m_id = newID();
		m_channel = sch;
		m_ident = sch.toString(); // Tempident
	}

	static private final int newID() {
		return ++m_lastID;
	}

	private synchronized SelectionKey key() {
		return m_key;
	}

	public SocketChannel channel() {
		return m_channel;
	}

	void _setKey(SelectionKey k) {
		m_key = k;
		SocketChannel sch = (SocketChannel) m_key.channel();
		m_ident = "#[" + m_id + ": " + sch.socket().getRemoteSocketAddress() + "]";
	}

	public String getRemoteAddress() {
		return m_channel.socket().getRemoteSocketAddress() + ":" + m_channel.socket().getPort();
	}

	public InetAddress getRemoteIpAddress() {
		return m_channel.socket().getInetAddress();
	}

	@Override
	public String toString() {
		return "[niohandler: " + m_ident + "]";
	}

	private void exception(Throwable t, String msg) {
		getCore().exception(t, this + ": " + msg);
	}

	private void msg(String s) {
		getCore().msg(this + ": " + s);
	}

	public ServerCore getCore() {
		return m_dl.getCore();
	}

	public void debug(String s) {
		getCore().debug(this + ":" + s);
	}

	public synchronized ByteBuffer buffer() {
		if(m_bb == null)
			m_bb = getCore().allocateBuffer();
		return m_bb;
	}

	private Exception wrapThrowable(String msg, Throwable t) throws Exception {
		if(t instanceof Exception)
			throw (Exception) t;
		else if(t instanceof Error)
			throw (Error) t;
		else
			throw new RuntimeException(msg, t);
	}

	private synchronized void checkDisconnected() {
		if(m_disconnected != null | m_pendingDisconnect) {
			IllegalStateException x = new IllegalStateException("The connection has been disconnected or is pending disconnection");
			exception(x, "Checking for channel state");
			throw x;
		}
	}

	private synchronized void inc() throws Exception {
		if(m_depth != 0)
			throw new IllegalStateException("Second task executing in connection handler!?");
		if(m_disconnected != null)
			throw wrapThrowable("The connection has been disconnected", m_disconnected);
		m_depth++;
	}

	private synchronized void dec() {
		if(m_depth != 1)
			throw new IllegalStateException("Trouble with inc() and dec() reentrancy checker");
		m_depth--;
	}

	public void run() {
		//        msg("Handle work");
		SelectionKey key = key();
		synchronized(this) {
			m_wantToWrite = false;
		}
		try {
			inc();
			boolean done;
			do {
				done = false;
				if(!key.isValid()) {
					//-- Connection closed?
					doCloseConnection(new IllegalStateException("The connection's key is no longer valid!?"));
					return;
				} else if(key.isReadable()) {
					if(doRead())
						done = true;
				} else if(key.isWritable()) {
					if(doWrite())
						done = true;
				}
			} while(done);
		} catch(ClosedChannelException cx) {
			if(!m_dl.isTerminated())
				exception(cx, "Exception in NIO handler: " + cx + "; disconnecting.");
			doCloseConnection(cx);
		} catch(Exception x) {
			//-- Any exception here means we'll disconnect and forget about all of this :-(
			exception(x, "Exception in NIO handler: " + x + "; disconnecting.");
			doCloseConnection(x);
		} finally {
			if(m_bb != null) {
				getCore().release(m_bb);
				m_bb = null;
			}
			synchronized(this) {
				dec();
				if(m_pendingDisconnect && !m_wantToWrite && m_disconnected == null) {
					doCloseConnection(null);
				} else if(key.isValid() && m_disconnected == null) {
					if(m_wantToWrite)
						key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
					else
						key.interestOps(SelectionKey.OP_READ);
				}
			}
			key.selector().wakeup();
		}
	}

	/**
	 * Primitive reader: read as much as is available of the message until
	 * the entire message is available and queued.
	 */
	private boolean doRead() throws Exception {
		ByteBuffer bb = buffer();
		int szrd;
		SocketChannel ch = (SocketChannel) key().channel();
		bb.clear();
		boolean done = false;
		try {
			while(0 < (szrd = ch.read(bb))) {
				done = true;
				bb.flip();

				boolean ok = false;
				Throwable errorx = null;
				try {
					//                System.out.println("Read "+bb.remaining()+" bytes.");

					//-- Handle buffer logging
					if(getCore().isLoggingReceive()) {
						int oldpos = bb.position(); // Save current position
						int len = bb.remaining(); // Get #bytes available,
						byte[] tmp = new byte[len];
						bb.get(tmp);
						bb.position(oldpos); // Move back,

						//-- Log the data
						StringBuffer sb = new StringBuffer();
						sb.append("Received data:\n");
						for(int off = 0; off < tmp.length; off += 16) {
							StringTool.arrayToDumpLine(sb, tmp, off, 16);
							sb.append("\n");
						}
						msg(sb.toString());
					}
					m_cmh.received(this, bb);
					ok = true;
				} catch(Exception x) {
					//-- Oops.... Log the exception
					errorx = x;
					//                    x.printStackTrace();
					getCore().log("Exception in connection's message handler: " + x);
					throw x;
				} catch(Error x) {
					errorx = x;
					//                    x.printStackTrace();
					getCore().log("java.lang.ERROR in connection's message handler: " + x);
					//                    exception(x, "Error in connection's message handler. Closing connection.");
					throw x;
				} finally {
					if(!ok) {
						try {
							if(errorx == null)
								errorx = new IllegalStateException("Handling received data has failed with an unknown exception (see server log)");
							doCloseConnection(errorx);
						} catch(Exception x) {
							exception(x, "Can't close the connection: " + x);
						}
					}
				}
				bb.clear();
			}
		} catch(ClosedChannelException x) {
			//-- Channel was closed.
			doCloseConnection(null);
			return false;
		}
		if(szrd < 0) {
			//-- Connection was closed by remote
			msg("Normal disconnection.");
			doCloseConnection(null);
			done = false;
		}
		return done;
	}

	/**
	 * Immediately closes this connection, and releases *all* resources.
	 * @param why
	 */
	private void doCloseConnection(Throwable why) {
		List<ByteBuffer> list = null;
		ByteBuffer bb1 = null;
		Queue<Object> qlist = null;
		synchronized(this) {
			if(m_disconnected != null)
				return;

			//            msg("Closing connection");
			if(why == null)
				m_disconnected = new DisconnectionException("Normal disconnection");
			else
				m_disconnected = why;
			list = m_pbl;
			m_pbl = new ArrayList<ByteBuffer>();
			m_wantToWrite = false;
			bb1 = m_cwb;
			m_cwb = null;
			qlist = m_writeQueue;
			m_writeQueue = new LinkedList<Object>();
		}
		discardBuffers(list);
		if(bb1 != null)
			getCore().release(bb1);
		while(qlist.size() > 0) {
			Object o = qlist.remove();
			if(o instanceof ByteBuffer) {
				getCore().release((ByteBuffer) o);
			} else if(o instanceof ISendCommand) {
				ISendCommand s = (ISendCommand) o;
				try {
					s.sendAborted(why);
				} catch(Throwable t) {
					exception(t, "Failed to cancel pending command at connection disconnect time.");
				}
			}
		}

		//-- 1. Close the key.
		SocketChannel ch = (SocketChannel) key().channel();
		try {
			ch.close();
		} catch(Exception x) {
			exception(x, "Can't close key's channel: " + x);
		}
		try {
			key().cancel();
		} catch(Exception x) {
			exception(x, "Can't cancel key: " + x);
		}

		//-- Notify my DataListener that it has a new connection free.
		m_dl.connectionClosed();
		try {
			m_cmh.disconnected(this, why);
		} catch(Exception x) {
			exception(x, "Exception in user disconnection handler: " + x);
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:  Write handler.                              		*/
	/*--------------------------------------------------------------*/
	/*
	 * There are two ways to send data to the stream:
	 * 1. By posting ISendCommand's
	 * 2. By posting a list of ByteBuffer's.
	 */
	/** The pending buffer list of writes of the 1st order (they come from the current send command). */
	private List<ByteBuffer> m_pbl = new ArrayList<ByteBuffer>();

	/** The pending buffer list's next buffer index. */
	private int m_pblix;

	/** All queued write commands or write buffers (order 2). These are queued and handled in order. */
	private Queue<Object> m_writeQueue = new LinkedList<Object>();

	private boolean m_wantToWrite;

	/** The command that is currently providing data to the sender queue */
	private ISendCommand m_currentCmd;

	/** T when the current command has completed work. Used to callback the command's done() handler when it's data has been sent. */
	private boolean m_cmdHasFinished;

	/** The 'current' write buffer. */
	private ByteBuffer m_cwb;

	//    /**
	//     * This either gets a command or a buffer to write.
	//     * @return
	//     */
	//    private Object   findQueuedWriteCommand() {
	//        for(;;) {
	//            if(m_writeQueue.size() == 0)
	//                return null;
	//            Object  oo = m_writeQueue.remove();         // Obtain the thingy
	//            if(oo instanceof byte[])
	//                return oo;
	//            WriteCommand wc = (WriteCommand) oo;        // We've gotten a write command
	//            try {
	//                wc.open();
	//                return wc;
	//            }
	//            catch(Exception x) {
	//                exception(x, "Exception while opening a write command");
	//                try { wc.close(x); } catch(Exception xx) {}
	//            }
	//        }
	//    }

	/**
	 * Completes a write session because there's no more data to write.
	 */
	private void endWrite() {
		ByteBuffer bb = null;
		discardBuffers(m_pbl); // Release all buffers.
		List<ByteBuffer> list;
		synchronized(this) {
			list = m_pbl;
			m_pbl = new ArrayList<ByteBuffer>();
			m_wantToWrite = false;
			bb = m_cwb;
			m_cwb = null;
		}
		discardBuffers(list);
		if(bb != null)
			getCore().release(bb);
	}

	/**
	 * This gets called when a new write is to take place. We look if
	 * a write command is pending and if so we continue to write from
	 * it. Parts of a write get buffered in this class: if a write is
	 * only partially completed it gets cached herein until the next
	 * time data is needed.
	 */
	private synchronized boolean doWrite() {
		boolean logwrites = getCore().isLoggingSend();
		if(logwrites)
			msg("doWrite");
		boolean written = false;
		try {
			SocketChannel ch = (SocketChannel) key().channel();
			for(;;) {
				if(m_cwb == null || m_cwb.remaining() <= 0) { // Is there buffer space available?
					if(logwrites)
						msg("Filling buffer");
					fillWriteBuffer(); // Write to the buffer, then return in read mode
					if(m_cwb == null) { // No more data?
						//-- End of write
						if(logwrites)
							msg("Writer completed.");
						endWrite();
						return false; // No need to restart as there's nothing to do
					}
				}
				if(logwrites) {
					msg("Got write buffer; write size = " + m_cwb.remaining());

					//-- There's stuff to write. Try to do so,
					int pos = m_cwb.position();
					int lim = m_cwb.limit();
					if(pos < lim) {
						byte[] data = new byte[lim - pos];
						msg("... pos=" + pos + ", lim=" + lim);
						m_cwb.get(data, 0, data.length);
						m_cwb.position(pos);

						StringBuffer sb = new StringBuffer();
						sb.append("Sent data:\n");
						for(int off = 0; off < data.length; off += 16) {
							StringTool.arrayToDumpLine(sb, data, off, 16);
							sb.append("\n");
						}
						msg(sb.toString());
					}
				}

				int szdone = ch.write(m_cwb);
				if(logwrites)
					msg("Wrote " + szdone + " bytes.. Rest is " + m_cwb.remaining());

				if(szdone > 0)
					written = true;
				else if(szdone == 0) {
					//-- We need to continue writing.
					m_wantToWrite = true;
					if(logwrites)
						msg("writer sleeping, still needs to write.");
					return written;
				}
			}
		} catch(Throwable x) {
			exception(x, "Exception in writing.");
			msg("Closing connection " + this + " due to write exception " + x);
			doCloseConnection(x);
			return false;
		}
	}

	/**
	 * This does it's best to get a new Write buffer current. When called the current buffer is exhausted and
	 * can be released.
	 *
	 * @throws Exception
	 */
	private synchronized boolean fillWriteBuffer() throws Exception {
		if(m_cwb != null) {
			getCore().release(m_cwb); // Discard exhausted buffer
			m_cwb = null;
		}

		boolean isnewcommand = false;
		for(;;) { // Loop until a command provides data
			//-- 1. Is a write buffer available in order 1 queue?
			if(m_pblix < m_pbl.size()) { // Data in pending buffer list?
				m_cwb = m_pbl.get(m_pblix); // Get the buffer (it is already in read mode)
				m_pbl.set(m_pblix++, null); // Release ownership
				if(m_pblix >= m_pbl.size()) {// This has emptied the list?
					m_pbl.clear(); // Then drop it.
					m_pblix = 0; // Reset buffer index.
				}
				return true; // Got some
			}

			//-- The order-1 queue is empty. Has a previous command completed?
			if(m_currentCmd != null && m_cmdHasFinished) {
				//-- The current command has completed. Finish the poor thing off.
				try {
					m_currentCmd.sendCompleted(); // Call completed handler.
				} catch(Exception x) {
					getCore().exception(x, "In completed() handler for write command");
				} finally {
					m_currentCmd = null;
					m_cmdHasFinished = false;
				}
			}

			//-- Try to get something new to do...
			if(m_currentCmd == null) { // No command pending?
				//-- Get something off the order-2 queue.
				if(m_writeQueue.size() == 0) { // Write queue thing is empty?
					endWrite(); // Then we're done writing anything.
					return false; // Empty.
				}
				Object oo = m_writeQueue.remove(); // Get the next thing to write;
				if(oo instanceof ByteBuffer) { // Is a buffer?
					m_cwb = (ByteBuffer) oo; // Set as thingy,
					return true; // And be done
				}
				if(oo instanceof ISendCommand) { // Is a new command?
					m_currentCmd = (ISendCommand) oo;// Yep -> post as a new command
					isnewcommand = true;
				} else {
					throw new IllegalStateException("!? Unknown object in write queue!?");
				}
			}

			//-- At this time a command *must* be present.
			if(m_currentCmd == null)
				throw new IllegalStateException("!? Command should be present now!");

			//-- 1. Make the command provide input;
			NioOutputStream nos = getCore().isLittleEndian() ? new LittleEndianOutputStream(this, m_pbl) : new BigEndianOutputStream(this, m_pbl);
			boolean ok = false;
			Throwable error = null;
			try {
				m_cmdHasFinished = !m_currentCmd.prepareData(nos); // Force the command to generate output;
				List<ByteBuffer> bl = nos.getBuffers(); // Get the written buffers
				//                System.out.println("commandRetrieval: prepareData returned "+bl.size()+" buffers");
				m_pbl = bl; // Assign to pending buffer list. This will cause the next loop to retrieve data
				m_pblix = 0;
				ok = true;
				isnewcommand = false;
			} catch(Exception t) {
				error = t;
				throw t;
			} catch(Error t) {
				error = t;
				throw t;
			} finally {
				if(!ok) {
					//-- Some kind of throwable occured. Cancel this command, and discard any allocated buffers; then disconnect if this was != first
					List<ByteBuffer> bl = nos.getBuffers();
					discardBuffers(bl);
					m_pbl.clear();

					//-- Cancel the command unsuccesfully
					try {
						m_currentCmd.sendAborted(error);
					} catch(Throwable x) {
						getCore().exception(x, "Error cancelling ISendCommand");
					} finally {
						m_currentCmd = null;
						m_cmdHasFinished = false;
						if(!isnewcommand) {
							try {
								doCloseConnection(error);
							} catch(Exception x) {
								getCore().exception(x, "Error in disconnect after write error");
							}
							//-- no need to return: the exception will be thrown when finally exits.
						}
					}
				}
			}
		}
	}

	private void discardBuffers(List<ByteBuffer> l) {
		for(ByteBuffer bb : l)
			if(bb != null)
				getCore().release(bb);
		l.clear();
	}

	//    public synchronized void    addCommand(WriteCommand wc) throws Exception {
	//        checkDisconnected();
	//        m_writeQueue.add(wc);
	//        indicateWriteWanted();
	//    }
	private synchronized void indicateWriteWanted() {
		if(!m_wantToWrite) {
			m_wantToWrite = true;
			if(ServerCore.DEBUG)
				debug("wantToWrite set to TRUE");
			if(m_depth == 0) {
				debug("Updating the selectedOps to include WRITE");
				m_key.interestOps(m_key.interestOps() | SelectionKey.OP_WRITE);
				m_key.selector().wakeup();
			}
		}
	}

	public final synchronized void addSendCommand(ISendCommand sc) throws Exception {
		checkDisconnected();
		m_writeQueue.add(sc);
		indicateWriteWanted();
	}

	/**
	 * This creates a stream to write to. When the stream is closed all of it's data
	 * gets queued for transmission.
	 *
	 * @see to.etc.nio.server.ConnectionInfo#getCommandWriter()
	 */
	public synchronized NioOutputStream getCommandWriter() {
		checkDisconnected();
		return getCore().isLittleEndian() ? new LittleEndianOutputStream(this) : new BigEndianOutputStream(this);
	}

	/**
	 * Called when a command writer stream is closed for a stream allocated
	 * by getCommandWriter(). It takes the buffer list and adds it to the write
	 * queue.
	 *
	 * @param nos
	 */
	synchronized void commandStreamClosed(NioOutputStream nos) {
		List<ByteBuffer> l = nos.getBuffers();
		if(l.size() == 0)
			return;
		//        System.out.println("WRITER: Got "+l.size()+" buffers from NioOutputStream.close");
		//        ByteBuffer  bb = l.get(0);
		//        int pos = bb.position();
		//        int lim = bb.limit();
		//        if(pos < lim) {
		//            byte[] data = new byte[lim - pos];
		//            bb.get(data, pos, data.length);
		//            bb.position(pos);
		//            System.out.println("WRITER: first buffer data = "+StringTool.toHex(data));
		//        }
		m_writeQueue.addAll(l); // Pump all bytes to the sender
		indicateWriteWanted(); // Make the writer start soonest
	}

	/**
	 * Client-initiated controlled disconnect. This disconnects immediately if the
	 * writer is idle, else it posts a disconnect flag causing a disconnect when
	 * writing completes.
	 *
	 * @see to.etc.nio.server.ConnectionInfo#disconnect()
	 */
	public void disconnect() throws Exception {
		synchronized(this) {
			if(m_disconnected != null || m_pendingDisconnect)
				return;
			if(m_wantToWrite) {
				m_pendingDisconnect = true;
				return;
			}

			//-- We're idling! Disconnect immediately
			doCloseConnection(null);
		}
	}

	public void disconnectImmediately(Exception why) {
		doCloseConnection(why);
	}
}
