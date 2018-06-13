package to.etc.template;

import java.util.*;

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

	/**
	 * Walk the remap list, and try to calculate a source location for a given output location.
	 * @param mapList
	 * @param lineNumber
	 * @param columnNumber
	 * @return
	 */
	static public int[] remapLocation(List<JSLocationMapping> mapList, int lineNumber, int columnNumber) {
		//-- Walk the mapping backwards. Find 1st thing that is at/before this location.
		for(int i = mapList.size(); --i >= 0;) {
			JSLocationMapping m = mapList.get(i);
			if(m.getTline() <= lineNumber) {
				if(m.getTcol() <= columnNumber) {
					//-- Gotcha.
					int dline = lineNumber - m.getTline();
					int dcol = columnNumber - m.getTcol();
					return new int[]{m.getSline() + dline, m.getScol() + dcol};
				}
			}
		}
		//-- Nothing found: return verbatim.
		return new int[]{lineNumber, columnNumber};
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
