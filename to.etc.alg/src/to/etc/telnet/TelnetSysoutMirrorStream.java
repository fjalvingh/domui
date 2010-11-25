package to.etc.telnet;

import java.io.*;

/**
 * This forms a TEE for sysout and the telnet server. All data written to this
 * stream is copied both to the Telnet server AND the system.out stream.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class TelnetSysoutMirrorStream extends OutputStream {
	/// The original system.out print stream,
	protected PrintStream	m_out_ps;

	/// The Telnet server to talk to,
	protected TelnetServer	m_ts;


	public TelnetSysoutMirrorStream(TelnetServer ts, PrintStream ps) {
		m_out_ps = ps;
		m_ts = ts;
	}

	@Override
	public void write(int b) throws java.io.IOException {
		m_ts._write(b);
		m_out_ps.write(b);
	}

	@Override
	public void write(byte[] parm1) throws java.io.IOException {
		m_ts._write(parm1, 0, parm1.length);
		m_out_ps.write(parm1);
	}

	@Override
	public void flush() throws java.io.IOException {
		super.flush();
	}

	@Override
	public void write(byte[] parm1, int offset, int len) throws java.io.IOException {
		m_ts._write(parm1, offset, len);
		m_out_ps.write(parm1, offset, len);
	}

	@Override
	public void close() throws java.io.IOException {
		super.close();
	}


}
