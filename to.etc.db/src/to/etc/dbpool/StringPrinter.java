package to.etc.dbpool;

public class StringPrinter implements IPrinter {
	private StringBuilder m_sb;

	public StringPrinter() {
		m_sb = new StringBuilder(8192);
	}

	public StringPrinter(StringBuilder sb) {
		m_sb = sb;
	}

	public IPrinter header(String cssclass, String name) {
		m_sb.append("--- ").append(name).append(" ---\n");
		return this;
	}

	public IPrinter warning(String what) {
		m_sb.append("*warning: ").append(what).append("\n");
		return this;
	}

	public IPrinter nl() {
		m_sb.append("\n");
		return this;
	}

	public IPrinter pre(String css, String pre) {
		m_sb.append(pre);
		return this;
	}

	public IPrinter text(String s) {
		m_sb.append(s);
		return this;
	}

	public String getText() {
		String s = m_sb.toString();
		m_sb.setLength(0);
		return s;
	}
}
