package to.etc.webapp.query;

/**
 * Represents a single selected column.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 14, 2009
 */
public class QSelectionColumn {
	/** When used in a restriction or order an alias is needed for complex query parts. */
	private String				m_alias;

	/** The type of item, */
	private QSelectionFunction	m_function;

	protected QSelectionColumn(QSelectionFunction function, String alias) {
		m_alias = alias;
		m_function = function;
	}

	/**
	 * Get the function applied to this selection item.
	 * @return
	 */
	public QSelectionFunction getFunction() {
		return m_function;
	}

	/**
	 * Return the alias applied to this selection column.
	 * @return
	 */
	public String getAlias() {
		return m_alias;
	}


}
