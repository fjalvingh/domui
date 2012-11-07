package to.etc.domui.log.tailer;

import java.io.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.state.*;

/**
 * This will run a "tail -f xxxx" operation through an ssh session to a remote system. The ssh session
 * will run in a separate thread and will be kept alive for the time this instance is active. To support
 * looking back into the log file the tail command will be used in such a way that the entire logfile
 * is retrieved before starting in "follow" mode. The data is sent to a tempfile, and the code maintains
 * a paging array of the tempfile's location every 100 lines.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 2, 2012
 */
public class LogTailerTask implements IConversationStateListener {
	/** The path of the tailed file on that server. */
	@Nonnull
	final private String m_logpath;

	/** Writer: offsets of every 100th line (0, 100, 200, etc) */
	private List<Long> m_offsetList = new ArrayList<Long>();

	/** The last output offset */
	private long m_currentWriteOffset;

	/** The last offset since we flushed the output */
	private long m_lastFlushOffset;

	/** The last line# we have seen. */
	private int m_lastLine;

	private RandomAccessFile m_input;

	public LogTailerTask(@Nonnull String logpath) {
		m_logpath = logpath;
	}

	public void start() throws Exception {
		m_input = new RandomAccessFile(m_logpath, "rw");
		m_currentWriteOffset = m_input.length();
		m_lastLine = getNumberOfLinesInFile(new File(m_logpath), m_currentWriteOffset);
		m_offsetList.clear();
		m_offsetList.add(Long.valueOf(0));					// Line 0..99 at offset 0
	}

	public int getNumberOfLinesInFile(File file, long size) throws IOException {
		LineNumberReader lr = null;
		try {
			lr = new LineNumberReader(new FileReader(file));
			lr.skip(size);
			return lr.getLineNumber();
		} finally {
			if(lr != null) {
				lr.close();
			}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Reading data.										*/
	/*--------------------------------------------------------------*/
	
	/**
	 * Return the last 0-based line # that was completely written. -1 means no data is present at all. This does NOT include the last
	 * line number *itself* because it can be seen partially.
	 * @return
	 */
	public synchronized int getLastLine() {
		return m_lastLine;
	}

	public synchronized long getSize() {
		return m_currentWriteOffset;
	}

	/**
	 * Get the specified #of lines from the set.
	 * @param first
	 * @param last
	 * @return
	 * @throws Exception
	 */
	public List<String> getLines(int first, int last) throws IOException {
		int ll;
		long boffset;
		int blength;
		synchronized(this) {
			ll = getLastLine();
			if(first >= ll)
				return Collections.EMPTY_LIST;
			else if(first < 0)
				first = 0;
			if(last > ll)
				last = ll + 1;
			if(first >= last)
				return Collections.EMPTY_LIST;

			//-- Get the nearest offset to the start
			int six = (first / 100);
			int eix = (last / 100) + 1;			// Get block INCLUDING the last line

			boffset = m_offsetList.get(six).longValue();
			long eoffset = eix >= m_offsetList.size() ? m_currentWriteOffset : m_offsetList.get(eix).longValue();
			long l = eoffset - boffset;
			if(l > 1024 * 1024) {
				List<String> res = new ArrayList<String>(1);
				res.add("Puzzler viewer: lines " + first + " to " + last + " need " + l + " bytes of space to show - which is too much... Sorry...");
				return res;
			}
			blength = (int) (eoffset - boffset);	// We'll assume no overflows here.
		}

		//-- Load all data.
		byte[] data = new byte[blength];			// Load this many bytes; this will contain all the lines we're interested in.
		m_input.seek(boffset);
		int sz = m_input.read(data);

		//-- Find the start and the end of the line we need.
		int sline = (first / 100) * 100;					// Block starts with this line
		int ix = 0;

		List<String> res = new ArrayList<String>(last - first);
		while(ix < sz) {
			//-- First: collect the current line
			int loff = ix;
			while(ix < sz) {
				if(data[ix++] == 0x0a) {
					break;
				}
			}

			//-- Is this line within the interval?
			if(sline > last)
				break;

			if(sline >= first) {
				int len = ix - loff;
				if(loff >= sz || loff + len > sz)
					throw new IllegalStateException("Indexing overflow");

				String line = new String(data, loff, len, "utf-8");
				res.add(line);
			}
			sline++;
		}
		return res;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	IConversationStateListener.							*/
	/*--------------------------------------------------------------*/

	@Override
	public void conversationNew(ConversationContext cc) throws Exception {}

	@Override
	public void conversationAttached(ConversationContext cc) throws Exception {}

	@Override
	public void conversationDetached(ConversationContext cc) throws Exception {}

	@Override
	public void conversationDestroyed(ConversationContext cc) throws Exception {
		synchronized(this) {
			try {
				if(null != m_input)
					m_input.close();
			} catch(Exception x) {}
		}
	}
}
