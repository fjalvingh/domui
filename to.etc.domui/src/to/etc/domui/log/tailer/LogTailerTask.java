package to.etc.domui.log.tailer;

import java.io.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.state.*;
import to.etc.util.*;

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
	/** The path of the tailed file on server. */
	@Nonnull
	final private String m_logpath;

	/** Writer: offsets of every 100th line (0, 100, 200, etc) */
	private List<Long> m_offsetList = new ArrayList<Long>();

	/** The current file size, gets updated via readFileDelta method */
	private long m_currentFileSize;

	/** The current file total number of lines, gets updated via readFileDelta method */
	private int m_currentLinesCount;

	/** File reader used to get specified lines of file */
	private RandomAccessFile m_fileContentReader;

	/** File reader used to monitor for file updates */
	private FileReader m_fileDeltaReader;

	private String m_errorMsg = null;

	public LogTailerTask(@Nonnull String logpath) {
		m_logpath = logpath;
	}

	public boolean start() throws Exception {
		try {
			m_fileContentReader = new RandomAccessFile(m_logpath, "r");
		}catch(FileNotFoundException ex){
			//file not found - logger did not log any lines yet
			return false;
		}
		m_fileDeltaReader = new FileReader(m_logpath);
		m_currentFileSize = 0;
		m_currentLinesCount = 0;
		m_offsetList.clear();
		m_offsetList.add(Long.valueOf(0));					// Line 0..99 at offset 0
		readFileDelta();
		return true;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Reading data.										*/
	/*--------------------------------------------------------------*/

	/**
	 * Return the file total lines count.
	 * @return
	 */
	public synchronized int getLastLine() {
		return m_currentLinesCount;
	}

	/**
	 * Return the file size.
	 * @return
	 */
	public synchronized long getSize() {
		return m_currentFileSize;
	}

	/**
	 * Read file for delta changes.
	 * @throws IOException
	 */
	public synchronized void readFileDelta() throws IOException {
		int ix = 0;
		int item;
		do {
			item = m_fileDeltaReader.read();
			if(item > -1) {
				ix++;
				if(item == 0x0a) {
					m_currentLinesCount++;
					if(m_currentLinesCount % 100 == 0) {
						long toff = m_currentFileSize + ix;
						m_offsetList.add(Long.valueOf(toff));			// Add this line
					}
				}
			}
		} while(item > -1);
		m_currentFileSize += ix;
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
			long eoffset = eix >= m_offsetList.size() ? m_currentFileSize : m_offsetList.get(eix).longValue();
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
		m_fileContentReader.seek(boffset);
		int sz = m_fileContentReader.read(data);

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
	public void conversationNew(@Nonnull ConversationContext cc) throws Exception {}

	@Override
	public void conversationAttached(@Nonnull ConversationContext cc) throws Exception {}

	@Override
	public void conversationDetached(@Nonnull ConversationContext cc) throws Exception {}

	@Override
	public void conversationDestroyed(@Nonnull ConversationContext cc) throws Exception {
		synchronized(this) {
			FileTool.closeAll(m_fileContentReader, m_fileDeltaReader);
		}
	}
}
