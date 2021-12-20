package to.etc.domui.uitest.pogenerator;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 09-12-21.
 */
@NonNullByDefault
final public class PoSelectorCell implements IPoSelector {
	/** Zero-based column index in the table. */
	private final int m_columnIndex;

	public PoSelectorCell(int columnIndex) {
		m_columnIndex = columnIndex;
	}

	@Override
	public String selectorAsCode() {
		return "() -> this.getCellSelector(" + m_columnIndex + ")";
	}
}
