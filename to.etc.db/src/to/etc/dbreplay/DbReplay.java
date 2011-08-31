package to.etc.dbreplay;

import java.io.*;

import javax.annotation.*;

/**
 * This utility will replay a database logfile, for performance evaluation purposes.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 31, 2011
 */
public class DbReplay {
	public static void main(String[] args) {
		try {
			new DbReplay().run(args);
		} catch(Exception x) {
			x.printStackTrace();
		}
	}

	private File m_inputFile;

	/** The buffered input file containing statements. */
	private BufferedInputStream m_bis;

	private void run(String[] args) throws Exception {
		decodeOptions(args);

		try {
			initialize();

			//-- Input distributor loop.
			for(;;) {
				ReplayRecord rr = ReplayRecord.readRecord(this);
				if(null == rr)
					break;
				m_recordNumber++;
			}
			System.out.println("Normal EOF after " + m_recordNumber + " records and " + m_fileOffset + " file bytes");

		} catch(Exception x) {
			System.err.println("Error: " + x);
			System.err.println("   -at record " + m_recordNumber + ", file offset " + m_fileOffset);
			x.printStackTrace();
		} finally {
			releaseAll();
		}
	}

	private void decodeOptions(String[] args) throws Exception {
		int argc = 0;
		while(argc < args.length) {
			String s = args[argc++];
			if(s.startsWith("-")) {


			} else {
				if(m_inputFile == null) {
					m_inputFile = new File(s);
					if(!m_inputFile.exists() || !m_inputFile.isFile())
						throw new Exception(m_inputFile + ": file does not exist or is not a file.");
				} else {
					usage("Unexpected extra argument on command line");
					return;
				}
			}
		}

		if(m_inputFile == null) {
			usage("Missing input file name");
			return;
		}
	}

	private void usage(String msg) {
		System.out.println("Error: " + msg);
		System.out.println("Usage: DbReplay [options] filename");
	}

	private void releaseAll() {

	}

	private void initialize() throws Exception {
		m_bis = new BufferedInputStream(new FileInputStream(m_inputFile), 65536);


	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Accessing the data stream.							*/
	/*--------------------------------------------------------------*/

	private long m_fileOffset;

	private int m_recordNumber;

	/**
	 *
	 * @param is
	 * @return
	 * @throws Exception
	 */
	@Nullable
	public String readString() throws Exception {
		int len = readInt();
		if(len < 0)
			return null;
		byte[] data = new byte[len];
		int szrd = m_bis.read(data);
		if(szrd != len)
			throw new IOException("Unexpected EOF: got " + szrd + " bytes but needed " + len);
		m_fileOffset += len;
		return new String(data, "utf-8");
	}


	public long readLong() throws Exception {
		long a = (readInt() & 0xffffffffl);
		long b = (readInt() & 0xffffffffl);
		return (a << 32) | b;
	}

	public int readInt() throws Exception {
		int v = m_bis.read();
		if(v == -1)
			throw new EOFException();
		int r = v << 24;

		v = m_bis.read();
		if(v == -1)
			throw new EOFException();
		r |= v << 16;

		v = m_bis.read();
		if(v == -1)
			throw new EOFException();
		r |= v << 8;

		v = m_bis.read();
		if(v == -1)
			throw new EOFException();
		r |= v;

		m_fileOffset += 4;
		return r;
	}

	public int readByte() throws Exception {
		int b = m_bis.read();
		if(-1 == b)
			throw new EOFException();
		return b;
	}
}
