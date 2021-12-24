package to.etc.domui.uitest.pogenerator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.util.StringTool;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 09-12-21.
 */
@NonNullByDefault
final public class PoSelectorCellComponent implements IPoSelector {
	private final int m_columnIndex;

	private final String m_testId;

	public PoSelectorCellComponent(int columnIndex, String testId) {
		m_columnIndex = columnIndex;
		m_testId = testId;
	}

	@Override
	public String selectorAsCode() {
		return "() -> this.getCellComponentSelectorCss(" + m_columnIndex + ", " + StringTool.strToJavascriptString(m_testId, true) + ")";
	}
}
