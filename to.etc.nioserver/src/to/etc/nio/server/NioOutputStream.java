package to.etc.nio.server;

import java.io.*;
import java.nio.*;
import java.util.*;

/**
 * This outputstream writes data to a queue of ByteBuffers, allocated
 * from the server.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 3, 2006
 */
abstract public class NioOutputStream extends OutputStream {
	private ConnectionHandler m_ch;

	/** The list of buffers allocated, in order. */
	private List<ByteBuffer> m_bufferList;

	/** The buffer we're currently writing in. */
	private ByteBuffer m_buffer;

	/** T if I have allocated my own buffer list; this is a command which needs posting to the write queue. */
	private boolean m_ownBuffer;

	abstract public void writeInt(int i) throws IOException;

	abstract public void writeShort(short i) throws IOException;

	NioOutputStream(ConnectionHandler ch) {
		m_ch = ch;
		m_bufferList = new ArrayList<ByteBuffer>();
		m_ownBuffer = true;
	}

	NioOutputStream(ConnectionHandler ch, List<ByteBuffer> l) {
		m_ch = ch;
		m_bufferList = l;
	}

	synchronized List<ByteBuffer> getBuffers() {
		if(m_buffer != null)
			m_buffer.flip(); // Make last buffer enter "read" mode
		return m_bufferList;
	}

	@Override
	public void write(int b) throws IOException {
		if(m_ch == null)
			throw new IllegalStateException("Output stream is closed");
		if(m_buffer == null || m_buffer.remaining() < 1) {
			if(m_buffer != null)
				m_buffer.flip(); // Make old buffer enter "read" mode
			//-- Allocate a new buffer,
			m_buffer = m_ch.getCore().allocateBuffer();
			m_buffer.clear();
			m_bufferList.add(m_buffer);
		}
		m_buffer.put((byte) b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		//        System.out.println("NioOutputStream: write "+len+" bytes");
		if(m_ch == null)
			throw new IllegalStateException("Output stream is closed");
		if(m_buffer == null) {
			m_buffer = m_ch.getCore().allocateBuffer();
			m_bufferList.add(m_buffer);
			m_buffer.clear();
		}
		while(len > 0) {
			int todo = m_buffer.remaining();
			//            System.out.println("... todo="+todo);
			if(todo <= 0) {
				//-- Allocate a new buffer,
				m_buffer.flip(); // Finish off the last buffer
				m_buffer = m_ch.getCore().allocateBuffer();
				m_buffer.clear();
				m_bufferList.add(m_buffer);
				todo = m_buffer.remaining();
				//                System.out.println("... new buffer was needed, todo="+todo);
			}
			if(todo > len)
				todo = len;
			m_buffer.put(b, off, todo);
			off += todo;
			len -= todo;
		}
		//        System.out.println("... completed");
	}

	@Override
	public void close() throws IOException {
		ConnectionHandler ch = null;
		synchronized(this) {
			if(m_ch == null)
				return;
			if(m_ownBuffer)
				ch = m_ch;
			m_ch = null;
		}
		if(ch != null)
			ch.commandStreamClosed(this);
	}

	/**
	 * Called when the command is to be aborted. This is only valid for commands
	 * allocated with getCommandWriter(), not for ISendCommand streams.
	 */
	public void cancel() {
		synchronized(this) {
			if(m_ch == null)
				return;
			for(ByteBuffer bb : m_bufferList) {
				if(bb != null)
					m_ch.getCore().release(bb);
			}
			m_ch = null; // Invalidate
			m_bufferList.clear(); // Drop list
		}
	}

	/*--------------------------------------------------------------*/
	/* CODING: Encoding of often-used data structures.              */
	/*--------------------------------------------------------------*/
	public void writeByte(byte b) throws IOException {
		write(b);
	}

	public void writeString(String s, String encoding) throws UnsupportedEncodingException, IOException {
		byte[] data = s.getBytes(encoding);
		writeInt(data.length);
		if(data.length > 0)
			write(data);
	}

	public void writeString(String s) throws IOException {
		byte[] data = s.getBytes("UTF-8");
		writeInt(data.length);
		if(data.length > 0)
			write(data);
	}
}
