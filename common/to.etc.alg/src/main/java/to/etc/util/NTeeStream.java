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

import org.apache.commons.lang3.time.FastDateFormat;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.function.BiConsumerEx;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * An output stream which duplicates all contents written to it to N other output streams.
 * Uses separate reader thread to read data in parallel, for each output stream.
 */
@NonNullByDefault
public class NTeeStream extends OutputStream {

	static FastDateFormat DF = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS");

	static void log(String msg) {
		String thread = "[" + Thread.currentThread().getName() + "] ";
		//System.out.println(thread + DF.format(System.currentTimeMillis()) + " " + msg);
		System.out.println(DF.format(System.currentTimeMillis()) + " " + msg);
	}

	class Data {
		private final byte[] m_data;

		private final int m_length;

		public Data(byte[] data, int length) {
			m_data = data;
			m_length = length;
		}
	}

	class BufferWithReferenceCounts {
		private byte[] m_data;

		private List<Object> m_consumers;

		private int m_lengthStored;

		BufferWithReferenceCounts(int size) {
			m_data = new byte[size];
			m_consumers = new ArrayList<>();
		}

		void addConsumer(Object consumer) {
			m_consumers.add(consumer);
		}

		void removeConsumer(Object consumer) {
			m_consumers.remove(consumer);
		}

		boolean isCompletedByAllConsumers() {
			return m_consumers.isEmpty();
		}

		int getLengthAvailable() {
			return m_data.length - m_lengthStored;
		}

		void reset() {
			if(!m_consumers.isEmpty()) {
				throw new IllegalStateException("Can't reset block that is still consumed!?");
			}
			m_lengthStored = 0;
		}

		int write(byte[] source, int offset, int length) {
			int toWrite = Math.min(length, getLengthAvailable());
			System.arraycopy(source, offset, m_data, m_lengthStored, toWrite);
			m_lengthStored += toWrite;
			return toWrite;
		}

		Data getData() {
			return new Data(m_data, m_lengthStored);
		}
	}

	class Buffer {

		private final int m_blockSize;
		private final int m_capacity;

		private final List<BufferWithReferenceCounts> m_blocks;

		private final List<BufferWithReferenceCounts> m_freeBlocks;

		private final List<BufferWithReferenceCounts> m_fullBlocks;

		private boolean m_closed = false;

		private boolean m_started = false;

		private final ReentrantLock m_lock;

		private final Condition m_bufferFullCondition;

		private final List<Object> m_consumers = new ArrayList<>();

		/** If one of the providers failed the stream should fail too or we hang. */
		@Nullable
		private Exception m_failure;

		private Map<Object, Condition> m_consumerLocks = new ConcurrentHashMap<>();

		Buffer(int capacity, int blockSize) {
			m_lock = new ReentrantLock();
			m_capacity = capacity;
			m_blockSize = blockSize;
			m_blocks = new ArrayList<>();
			m_freeBlocks = new ArrayList<>();
			m_fullBlocks = new ArrayList<>();
			m_bufferFullCondition = m_lock.newCondition();
		}

		void initialize(Object... consumers) {
			if(m_started) {
				throw new IllegalStateException("Can't register consumers on started buffer!");
			}
			Arrays.stream(consumers).forEach(it -> m_consumers.add(it));
			m_started = true;
		}

		void write(byte[] data, int offset, int length) throws IOException, InterruptedException {
			if(!m_started) {
				throw new IllegalStateException("Can't write, buffer not properly initialized yet!");
			}
			if(m_consumers.isEmpty()) {
				throw new IllegalStateException("Can't write, no active consumers found!");
			}
			m_lock.lock();
			try {
				int currentOffset = offset;
				int bytesToWrite = length;
				//-- Wait for space to be available
				while(bytesToWrite > 0) {
					if(m_failure != null) {
						throw new IOException("Write stream has failed: " + m_failure);
					}
					BufferWithReferenceCounts activeForWrite = getBlockForWrite();
					int writtenLength = activeForWrite.write(data, currentOffset, bytesToWrite);
					currentOffset += writtenLength;
					bytesToWrite -= writtenLength;
					if(activeForWrite.getLengthAvailable() == 0) {
						m_freeBlocks.remove(activeForWrite);
						m_consumers.forEach(it -> activeForWrite.addConsumer(it));
						m_fullBlocks.add(activeForWrite);
						m_consumers.forEach(consumer -> {
							Condition consumerLock = m_consumerLocks.computeIfAbsent(consumer, k -> m_lock.newCondition());
							log("write fullBlocks -> consumerLock.signalAll() " + consumer);
							consumerLock.signalAll();
						});
					}
				}
			} finally {
				m_lock.unlock();
			}
		}

		private BufferWithReferenceCounts getBlockForWrite() throws InterruptedException {
			m_lock.lock();
			try {
				for(;; ) {
					if(m_freeBlocks.isEmpty()) {
						if(m_blocks.size() == m_capacity) {
							log("m_bufferFullCondition.await()");
							m_bufferFullCondition.await();
						}else {
							BufferWithReferenceCounts block = new BufferWithReferenceCounts(m_blockSize);
							m_blocks.add(block);
							m_freeBlocks.add(block);
						}
					}else {
						return m_freeBlocks.get(0);
					}
				}
			} finally {
				m_lock.unlock();
			}
		}

		@Nullable
		Data consume(Object consumer) throws InterruptedException {
			m_lock.lock();
			try {
				for(; ; ) {
					for(BufferWithReferenceCounts buffer : m_fullBlocks) {
						if(buffer.m_consumers.contains(consumer)) {
							return buffer.getData();
						}
					}
					if(m_closed) {
						return null;
					}
					//block on condition specific for a consumer object?
					Condition consumerLock = m_consumerLocks.computeIfAbsent(consumer, k -> m_lock.newCondition());
					log("consumerLock.await() " + consumer);
					consumerLock.await();
				}
			}finally {
				m_lock.unlock();
			}
		}

		@Nullable
		void consumed(Object consumer) throws InterruptedException {
			BufferWithReferenceCounts consumedBlock = null;
			for(BufferWithReferenceCounts buffer : m_fullBlocks) {
				if(buffer.m_consumers.contains(consumer)) {
					consumedBlock = buffer;
					break;
				}
			}
			if(null == consumedBlock) {
				throw new IllegalStateException("Unable to locate consumedBlock???");
			}
			m_lock.lock();
			try {
				consumedBlock.removeConsumer(consumer);
				if(consumedBlock.isCompletedByAllConsumers()) {
					m_fullBlocks.remove(consumedBlock);
					consumedBlock.reset();
					m_freeBlocks.add(consumedBlock);
					if(!m_closed) {
						log("consumed m_bufferFullCondition.signalAll()");
						m_bufferFullCondition.signalAll();
					}
				}
			} finally {
				m_lock.unlock();
			}
		}

		public void registerFailedConsumer(Object consumer) {
			m_lock.lock();
			try {
				m_consumers.remove(consumer);
				List<BufferWithReferenceCounts> withConsumer = m_fullBlocks.stream().filter(block -> block.m_consumers.contains(consumer)).collect(Collectors.toList());
				boolean anyIsFreed = false;
				for(BufferWithReferenceCounts block: withConsumer) {
					block.m_consumers.remove(consumer);
					if(block.isCompletedByAllConsumers()) {
						m_fullBlocks.remove(block);
						block.reset();
						m_freeBlocks.add(block);
						anyIsFreed = true;
					}
				}
				if(!m_closed && anyIsFreed) {
					log("registerFailedConsumer -> m_bufferFullCondition.signalAll()");
					m_bufferFullCondition.signalAll();
				}
			} finally {
				m_lock.unlock();
			}
		}

		void close() {
			m_lock.lock();
			try {
				if(!m_closed) {
					m_closed = true;
				}
				m_consumers.forEach(consumer -> {
					Condition consumerLock = m_consumerLocks.computeIfAbsent(consumer, k -> m_lock.newCondition());
					log("close -> consumerLock.signalAll() " + consumer);
					consumerLock.signalAll();
				});
			}finally {
				m_lock.unlock();
			}
		}
	}

	private final int m_blockSize = 1024 * 1024 * 1;

	private final OutputStream[]	m_streams;

	private final Exception[]	m_exceptionsOnStreams;

	@Nullable
	private BiConsumerEx<Integer, Exception> m_exHandler;

	private final Buffer m_buffer;

	private boolean m_closed;

	private final ReentrantLock m_lock;

	private final List<Thread> m_threads = new ArrayList<>();

	public NTeeStream(OutputStream... streams) {
		m_streams = streams;
		m_exceptionsOnStreams = new Exception[streams.length];
		m_buffer = new Buffer(10, m_blockSize);

		m_lock = new ReentrantLock();

		AtomicInteger index = new AtomicInteger(0);
		Arrays.stream(streams).forEach(outStream -> {
			Thread senderThread = new Thread(() -> {
				Thread thread = Thread.currentThread();
				try {
					Data aData;
					log("started reader thread " + thread);
					while((aData = m_buffer.consume(thread)) != null) {
						byte[] data = aData.m_data;
						int length = aData.m_length;
						log("reading and sending " + data.length + " bytes " + thread);
						outStream.write(data, 0, length);
						m_buffer.consumed(thread);
					}
					log(thread.getName() + " completed!");
				} catch(Exception e) {
					m_exceptionsOnStreams[index.get()] = e;
					m_buffer.registerFailedConsumer(thread);
					log(Thread.currentThread().getName() + " fas failed.");
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}, "Sender-" + index.getAndIncrement());
			m_threads.add(senderThread);
		});
		m_buffer.initialize(m_threads.toArray());
		m_threads.forEach(it -> it.start());
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
			log("writing " + length + " bytes");
			m_buffer.write(data, offset, length);
		} catch(InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() throws IOException {
		if(m_closed) {
			return;
		}
		m_buffer.close();

		m_threads.forEach(it -> {
			try {
				it.join();
			} catch(InterruptedException e) {
				throw new RuntimeException(e);
			}
		});

		for(int index = 0; index < m_streams.length; index++) {
			try {
				if(null == m_exceptionsOnStreams[index]) {
					log("m_streams[" + index + "].flush + close()");
					m_streams[index].flush();
					m_streams[index].close();
				}
			} catch(IOException x) {
				m_exceptionsOnStreams[index] = x;
				handle(index, x);
			}
		}

		if(Arrays.stream(m_exceptionsOnStreams).noneMatch(it ->  null == it)) {
			throw new RuntimeException("All streams are closed with exceptions!");
		}
		m_closed = true;
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
