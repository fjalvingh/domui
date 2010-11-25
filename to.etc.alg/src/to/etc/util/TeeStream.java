package to.etc.util;

import java.io.*;

/**
 * An outputstream which duplicates all contents written to it to two
 * other output streams.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 25, 2010
 */
public class TeeStream extends OutputStream {
	private OutputStream	m_a, m_b;

	public TeeStream(OutputStream a, OutputStream b) {
		m_a = a;
		m_b = b;
	}

	@Override
	public void write(byte[] parm1) throws java.io.IOException {
		IOException sx = null;
		try {
			m_a.write(parm1);
		} catch(IOException x) {
			sx = x;
		}

		try {
			m_b.write(parm1);
		} catch(IOException x) {
			sx = x;
		}
		if(sx != null)
			throw sx;
	}

	@Override
	public void flush() throws java.io.IOException {
		IOException sx = null;
		try {
			m_a.flush();
		} catch(IOException x) {
			sx = x;
		}

		try {
			m_b.flush();
		} catch(IOException x) {
			sx = x;
		}
		if(sx != null)
			throw sx;
	}

	@Override
	public void write(int b) throws java.io.IOException {
		IOException sx = null;
		try {
			m_a.write(b);
		} catch(IOException x) {
			sx = x;
		}

		try {
			m_b.write(b);
		} catch(IOException x) {
			sx = x;
		}
		if(sx != null)
			throw sx;
	}

	@Override
	public void write(byte[] parm1, int parm2, int parm3) throws java.io.IOException {
		IOException sx = null;
		try {
			m_a.write(parm1, parm2, parm3);
		} catch(IOException x) {
			sx = x;
		}

		try {
			m_b.write(parm1, parm2, parm3);
		} catch(IOException x) {
			sx = x;
		}
		if(sx != null)
			throw sx;
	}

	@Override
	public void close() throws java.io.IOException {
		IOException sx = null;
		try {
			m_a.close();
		} catch(IOException x) {
			sx = x;
		}

		try {
			m_b.close();
		} catch(IOException x) {
			sx = x;
		}
		if(sx != null)
			throw sx;
	}


}
