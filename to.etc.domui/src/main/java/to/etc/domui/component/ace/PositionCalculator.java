package to.etc.domui.component.ace;

import org.eclipse.jdt.annotation.NonNullByDefault;

import java.awt.*;

/**
 * This class helps with converting offsets to line:column and vice versa
 * for a single multiline string value.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 8-10-18.
 */
@NonNullByDefault
public final class PositionCalculator {
	private final String m_text;

	private int[] m_lineOffsets;

	public PositionCalculator(String text) {
		m_text = text;
		int len = text.length();
		int ix = 0;

		int[] res = new int[1024];
		res[0] = 0;
		int line = 1;
		while(ix < len) {
			ix = text.indexOf('\n', ix);
			if(ix == -1) {
				break;
			}

			ix++;							// Past offset
			if(res.length >= line) {
				int[] nw = new int[line + 1024];
				System.arraycopy(res, 0, nw, 0, res.length);
				res = nw;
			}
			res[line++] = ix;
		}

		if(res.length == line) {
			m_lineOffsets = res;
		} else {
			m_lineOffsets = new int[line];
			System.arraycopy(res, 0, m_lineOffsets, 0, line);
		}
	}

	/**
	 * Get the text offset of the specified zero-based line and column.
	 */
	public int getLinePosition(int row, int column) {
		if(row < 0 || row >= m_lineOffsets.length || column < 0)
			return -1;
		int index = m_lineOffsets[row] + column;
		if(index > m_text.length())
			return m_text.length();
		return index;
	}

	public void getXYPosition(Point out, int position) {
		for(int i = 0; i < m_lineOffsets.length; i++) {
			int lineOffset = m_lineOffsets[i];
			if(lineOffset > position) {
				out.y = i - 1;							// Previous line
				out.x = position - m_lineOffsets[i - 1];
				return;
			}
		}
		out.y = m_lineOffsets.length;
		out.x = 0;
	}

	public String getText() {
		return m_text;
	}
}
