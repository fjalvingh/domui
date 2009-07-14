package to.etc.webapp.query;

/**
 * Part of a selected thing.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 14, 2009
 */
public class QSelectionItem extends QNodeBase {
	/** The type of item, */
	private QSelectionFunction	m_function;

	protected QSelectionItem(QSelectionFunction function) {
		m_function = function;
	}

	/**
	 * Get the function applied to this selection item.
	 * @return
	 */
	public QSelectionFunction getFunction() {
		return m_function;
	}
}
