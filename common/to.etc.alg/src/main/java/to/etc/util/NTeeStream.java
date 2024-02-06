/*
 * DomUI Java User Interface - shared code
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.util;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.function.BiConsumerEx;
import to.etc.function.ConsumerEx;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An output stream which duplicates all contents written to it to N other output streams.
 * Uses parallel writes for each write command, so best is if it is wrapped inside some BufferedOutputStream.
 */
@NonNullByDefault
public class NTeeStream extends OutputStream {

	class BufferPool {


		private final int m_bufferSize;
		private final long m_totalCapacity;

		private final List<byte[]> m_buffers;

		private long m_writeIndex = 0;

		private long m_readIndex = 0;

		private boolean m_closed = false;

		private final ReentrantLock m_lock;

		private final Condition m_bufferFullCondition;

		private final Condition m_bufferEmptyCondition;

		/** If one of the providers failed the stream should fail too or we hang. */
		@Nullable
		private Exception m_failure;

		BufferPool(int capacity, int bufferSize) {
			m_lock = new ReentrantLock();
			m_bufferSize = bufferSize;
			m_totalCapacity = bufferSize * capacity;
			m_buffers = new ArrayList<>();
			for(int i = 0; i < capacity; i++) {
				m_buffers.add(new byte[bufferSize]);
			}
			m_bufferFullCondition = m_lock.newCondition();
			m_bufferEmptyCondition = m_lock.newCondition();
		}

		private long calcNewPos(long currentIndex, int length) {
			if(currentIndex + length <= m_totalCapacity) {
				return currentIndex + length;
			}
			return currentIndex + length - m_totalCapacity;
		}

		void write(byte[] data, int offset, int length) throws IOException, InterruptedException {
			m_lock.lock();
			if(length > m_totalCapacity) {
				throw new IllegalArgumentException("Insufficient capacity, total capacity is " + m_totalCapacity + ", but it received " + length + " bytes to write!");
			}
			try {
				if(m_failure != null) {
					throw new IOException("Write stream has failed: " + m_failure);
				}

				//-- Wait for space to be available
				for(; ; ) {
					if(getAvailableWriteSize() >= length) {
						break;
					}
					System.out.println(System.currentTimeMillis() + " m_bufferFullCondition.await()");
					m_bufferFullCondition.await();
				}

				//-- There is room. Write the data.
				writeToBuffer(data, offset, length);
				System.out.println(System.currentTimeMillis() + " m_bufferEmptyCondition.signalAll()");
				m_bufferEmptyCondition.signalAll();
			} finally {
				m_lock.unlock();
			}
		}

		private void writeToBuffer(byte[] data, int offset, int length) {
			int remainingToWrite = length;
			int readOffset = offset;
			while(remainingToWrite > 0) {
				BufferAndPos writeBuffer = getWriteBufferAndPos();
				int chunkToWrite = Math.min(remainingToWrite, m_bufferSize - writeBuffer.m_pos);
				System.arraycopy(data, readOffset, writeBuffer.m_buffer, writeBuffer.m_pos, chunkToWrite);
				m_writeIndex = calcNewPos(m_writeIndex, chunkToWrite);
				remainingToWrite -= chunkToWrite;
				readOffset += chunkToWrite;
			}
		}

		/**
		 * Returns null is there is no more data to read and buffer is already closed.
		 * Blocks until there is no data ready yet but buffer is not already closed.
		 * Otherwise returns read data with exact size, up to specified maxReadCapacity.
		 */
		@Nullable
		byte[] read(int maxReadCapacity) throws IOException, InterruptedException {
			m_lock.lock();
			try {
				//-- Wait for the data to arrive.
				for(; ; ) {
					//-- Did we fail in the meanwhile?
					if(m_failure != null) {
						throw new IOException("Data provider failed: " + m_failure, m_failure);
					}

					if(m_writeIndex == m_readIndex) {
						if(m_closed) {
							//nothing to read and buffer is closed, then we return null
							return null;
						}
						//-- It is not yet there. We need to wait for it to arrive.
						System.out.println(System.currentTimeMillis() + " m_bufferEmptyCondition.await()");
						m_bufferEmptyCondition.await();
					}

					if(m_writeIndex != m_readIndex) {
						long availableToRead = Math.min(getAvailableReadSize(), maxReadCapacity);

						BufferAndPos readBuffer = getReadBufferAndPos();

						byte[] readData = null;
						if(maxReadCapacity == m_bufferSize) {
							if(availableToRead == maxReadCapacity && readBuffer.m_pos == 0) {
								readData = readBuffer.m_buffer;
								m_readIndex += availableToRead;
							}else if (!m_closed && readBuffer.m_pos > 0) {
								//here we try to match the size for some next reads
								availableToRead = m_bufferSize - readBuffer.m_pos;
								readData = new byte[(int) availableToRead];
								readFromBuffer(readData, 0, (int) availableToRead);
							}else if (!m_closed && readBuffer.m_pos == 0) {
								//here we return empty buffer and wait for buffer to grow to a full size chunk
								availableToRead = 0;
							}else {
								//buffer is closed, no need to wait for complete buffer length chunks.
								readData = new byte[(int) availableToRead];
								readFromBuffer(readData, 0, (int) availableToRead);
							}
						}else {
							//maxReadCapacity is not in sync with buffer size, we just read what is available always
							readData = new byte[(int) availableToRead];
							readFromBuffer(readData, 0, (int) availableToRead);
						}

						if(availableToRead == 0) {
							System.out.println(System.currentTimeMillis() + " m_bufferEmptyCondition.await()");
							m_bufferEmptyCondition.await();
						}else {
							if(availableToRead > 0) {
								//if we managed to read anything we potentially unlock writing
								System.out.println(System.currentTimeMillis() + " m_bufferFullCondition.signalAll()");
								m_bufferFullCondition.signalAll();
							}
							return readData;
						}
					}
				}
			} finally {
				m_lock.unlock();
			}
		}

		private long getAvailableWriteSize() {
			if(m_writeIndex == m_readIndex) {
				return m_totalCapacity;
			}else if(m_writeIndex > m_readIndex) {
				return m_totalCapacity - m_writeIndex + m_readIndex;
			}else {
				return m_readIndex - m_writeIndex;
			}
		}

		private long getAvailableReadSize() {
			if(m_readIndex < m_writeIndex) {
				return m_writeIndex - m_readIndex;
			}else {
				return m_totalCapacity - m_readIndex + m_writeIndex;
			}
		}

		private void readFromBuffer(byte[] data, int offset, int length) {
			int remainingToRead = length;
			int writeOffset = offset;
			while(remainingToRead > 0) {
				BufferAndPos readBuffer = getReadBufferAndPos();
				int chunkToRead = Math.min(remainingToRead, m_bufferSize - readBuffer.m_pos);
				System.arraycopy(readBuffer.m_buffer, readBuffer.m_pos, data, writeOffset, chunkToRead);
				m_readIndex = calcNewPos(m_readIndex, chunkToRead);
				remainingToRead -= chunkToRead;
				writeOffset += chunkToRead;
			}
		}

		void close() {
			m_lock.lock();
			try {
				if(!m_closed) {
					m_closed = true;
				}
				System.out.println(System.currentTimeMillis() + " close -> m_bufferEmptyCondition.signalAll()");
				m_bufferEmptyCondition.signalAll();
			}finally {
				m_lock.unlock();
			}
		}

		private class BufferAndPos {
			final byte[] m_buffer;

			final int m_pos;

			BufferAndPos(byte[] buffer, int pos) {
				m_buffer = buffer;
				m_pos = pos;
			}
		}

		private BufferAndPos getReadBufferAndPos() {
			int index = (int) ((m_readIndex + 1) / m_bufferSize);
			int pos = (int) (m_readIndex - (index * m_bufferSize));
			return new BufferAndPos(m_buffers.get(index), pos);
		}

		private BufferAndPos getWriteBufferAndPos() {
			int index = (int) ((m_writeIndex + 1) / m_bufferSize);
			int pos = (int) (m_writeIndex - (index * m_bufferSize));
			return new BufferAndPos(m_buffers.get(index), pos);
		}

	}

	private final OutputStream[]	m_streams;

	private final Exception[]	m_exceptionsOnStreams;

	@Nullable
	private BiConsumerEx<Integer, Exception> m_exHandler;

	private ExecutorService m_executors;

	private final BufferPool m_writeReadBuffer;

	private final Thread m_readerThread;

	private boolean m_closed;

	private final ReentrantLock m_lock;

	private final Condition m_closedStreamCondition;


	public NTeeStream(OutputStream... streams) {
		m_streams = streams;
		m_exceptionsOnStreams = new Exception[streams.length];
		m_executors = Executors.newFixedThreadPool(streams.length);
		m_writeReadBuffer = new BufferPool(10, 1024 * 1024 * 4);
		m_lock = new ReentrantLock();
		m_closedStreamCondition = m_lock.newCondition();

		m_readerThread = new Thread(() -> {
			try {
				byte[] aData;
				System.out.println(System.currentTimeMillis() + " starting reader thread");
				while ((aData = m_writeReadBuffer.read(1024 * 1024 * 4)) != null) {
					byte[] data = aData;
					if(data.length > 0) {
						System.out.println(System.currentTimeMillis() + "reading " + data.length + " bytes");
					}else {
						System.out.print(System.currentTimeMillis() + ".");
					}
					parallelAction(stream -> stream.write(data, 0, data.length));
				}
				try {
					m_lock.lock();
					System.out.println(System.currentTimeMillis() + " m_closedStreamCondition.signalAll()");
					m_closedStreamCondition.signalAll();
				}finally {
					m_lock.unlock();
				}
			} catch(IOException e) {
				throw new RuntimeException(e);
			} catch(InterruptedException e) {
				throw new RuntimeException(e);
			}
		});
		m_readerThread.start();
	}

	@Override
	public void write(int b) throws IOException {
		//this should not be called... well, if it is called lets just fallback to write byte[]...
		byte[] bytes = new byte[1];
		bytes[0] = (byte) b;
		write(bytes, 0, 1);

		/*
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		dos.writeInt(b);
		byte[] byteArray = baos.toByteArray();
		write(byteArray);
     	 */
	}

	@Override
	public void write(byte[] data, int offset, int length) throws IOException {
		try {
			m_writeReadBuffer.write(data, offset, length);
			System.out.println(System.currentTimeMillis() + " writing " + length + " bytes");
		} catch(InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() throws IOException {
		if(m_closed) {
			return;
		}
		m_writeReadBuffer.close();

		try {
			m_lock.lock();
			System.out.println(System.currentTimeMillis() + " m_closedStreamCondition.await()");
			m_closedStreamCondition.await();
		} catch(InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			m_lock.unlock();
		}

		for(int index = 0; index < m_streams.length; index++) {
			try {
				if(null == m_exceptionsOnStreams[index]) {
					System.out.println(System.currentTimeMillis() + " m_streams[" + index + "].flush + close()");
					m_streams[index].flush();
					m_streams[index].close();
				}
			} catch(IOException x) {
				m_exceptionsOnStreams[index] = x;
				handle(index, x);
			}
		}

		m_executors.shutdown();

		if(Arrays.stream(m_exceptionsOnStreams).noneMatch(it ->  null == it)) {
			throw new RuntimeException("All streams are closed with exceptions!");
		}
		m_closed = true;
	}

	private void parallelAction(ConsumerEx<OutputStream> action) {
		List<Callable<Boolean>> parallelWriteTasks = new ArrayList<>();
		for(int index = 0; index < m_streams.length; index++) {

			if(null == m_exceptionsOnStreams[index]) {
				int aIndex = index;
				parallelWriteTasks.add(() -> {
					try {
						action.accept(m_streams[aIndex]);
						return Boolean.TRUE;
					} catch(IOException x) {
						m_exceptionsOnStreams[aIndex] = x;
						FileTool.closeAll(m_streams[aIndex]);
						handle(aIndex, x);
						return Boolean.FALSE;
					}
				});
			}
		}
		if(!parallelWriteTasks.isEmpty()) {
			try {
				m_executors.invokeAll(parallelWriteTasks);
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		}

		if(Arrays.stream(m_exceptionsOnStreams).noneMatch(it ->  null == it)) {
			throw new RuntimeException("All streams are closed with exceptions!");
		}
	}

	public Exception[] getExceptionsOnStreams() {
		return m_exceptionsOnStreams;
	}

	public int getStreamsWithExceptions() {
		return (int) Arrays.stream(m_exceptionsOnStreams).filter(it -> null != it).count();
	}

	public void setOnStreamException(BiConsumerEx<Integer, Exception> consumer) {
		m_exHandler = consumer;
	}

	private void handle(int index, Exception ex) {
		BiConsumerEx<Integer, Exception> exHandler = m_exHandler;
		if(null != exHandler) {
			try {
				exHandler.accept(index, ex);
			} catch(Exception exception) {
				throw new WrappedException(exception);
			}
		}
	}
}
