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

	public JSTemplateError(Throwable x, String message, String source, int line, int column) {
		super(message, x);
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(m_source != null)
			sb.append(m_source);
		if(m_line != -1 || m_column != -1) {
			sb.append('(');
			if(m_line != -1)
				sb.append(m_line);
			if(m_column != -1) {
				sb.append(':');
				sb.append(m_column);
			}
			sb.append(") ");
		} else if(m_source != null)
			sb.append(": ");

		sb.append(getMessage());
		return sb.toString();
	}
}
