package to.etc.domui.dom;

import java.io.*;

import to.etc.util.*;

/**
 * Pretty-printing output renderer. Slower than the non-pretty variant, used for
 * debugging.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 17, 2007
 */
public class PrettyXmlOutputWriter extends XmlOutputWriterBase implements IBrowserOutput {
	private IndentWriter m_w;

	public PrettyXmlOutputWriter(Writer out) {
		super(new IndentWriter(out));
		m_w = (IndentWriter) getWriter();
	}

	@Override
	public void nl() throws IOException {
		m_w.forceNewline();
	}

	@Override
	public void inc() {
		m_w.inc();
	}

	@Override
	public void dec() {
		m_w.dec();
	}

	public void setIndentEnabled(boolean ind) {
		m_w.setIndentEnabled(ind);
	}

	@Override
	public boolean isIndentEnabled() {
		return m_w.isIndentEnabled();
	}

	@Override
	protected void println() throws IOException {
		m_w.println();
	}
}
