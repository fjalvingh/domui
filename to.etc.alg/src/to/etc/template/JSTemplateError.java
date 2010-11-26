package to.etc.template;

public class JSTemplateError extends RuntimeException {
	private String	m_source;

	private int		m_line;

	private int		m_column;

	public JSTemplateError(String message, String source, int line, int column) {
		super(message);
		m_source = source;
		m_line = line;
		m_column = column;
	}

	public int getColumn() {
		return m_column;
	}

	public int getLine() {
		return m_line;
	}

	public String getSource() {
		return m_source;
	}
}
