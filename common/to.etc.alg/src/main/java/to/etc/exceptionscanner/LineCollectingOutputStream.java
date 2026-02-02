package to.etc.exceptionscanner;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.function.ConsumerEx;
import to.etc.util.ByteArrayUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

/**
 * Wraps an output stream, and calls a listener for every line written to that
 * OutputStream. These lines are written as byte[] segments to any listener that
 * can then do as it desires with it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2021/01/07.
 */
@NonNullByDefault
final class LineCollectingOutputStream extends OutputStream {
	final private PrintStream m_original;

	/** Max stored length of a line. */
	static private final int MAXLINE = 512;

	final private ConsumerEx<String> m_lineReceiver;

	private Charset m_charSet;

	final private byte[] m_lineBuffer = new byte[MAXLINE];

	private int m_length;

	final private byte[] m_byteOnly = new byte[1];


	public LineCollectingOutputStream(PrintStream original, Charset charSet, ConsumerEx<String> receiver) {
		m_original = original;
		m_lineReceiver = receiver;
		m_charSet = charSet;
	}

	@Override
	public void write(int b) throws IOException {
		m_original.write(b);
		m_byteOnly[0] = (byte) b;
		write(m_byteOnly, 0, 1);
	}

	@Override
	public void write(byte[] b) throws IOException {
		m_original.write(b);
		write(b, 0, b.length);
	}

	@Override
	public void write(byte[] data, int off, int len) {
		m_original.write(data, off, len);

		//-- Scan for \n to find separate lines.
		while(off < len) {
			int lfpos = ByteArrayUtil.indexOf(data, off, len, (byte) '\n');
			int endpos = lfpos == -1 ? len : lfpos;						// How much data to copy?

			addLinePart(data, off, endpos);							// Append this to the data buffer for the current line
			if(lfpos != -1) {
				//-- Line end found -> flush the current line
				flushCurrentLine();
			}

			off = endpos + 1;
		}
	}

	@Override
	public void flush() throws IOException {
		m_original.flush();
	}

	/**
	 * Call the listener with the current line.
	 */
	private void flushCurrentLine() {
		try {
			String s = new String(m_lineBuffer, 0, m_length, m_charSet);
			m_lineReceiver.accept(s);
			m_length = 0;
		} catch(Exception x) {
			System.err.println("Receiver threw " + x);
		}
	}

	private void addLinePart(byte[] data, int off, int endpos) {
		int inputLength = endpos - off;
		int left = MAXLINE - m_length;
		if(left <= 0)
			return;
		if(inputLength > left)
			inputLength = left;

		System.arraycopy(data, off, m_lineBuffer, m_length, inputLength);
		m_length += inputLength;
	}
}
