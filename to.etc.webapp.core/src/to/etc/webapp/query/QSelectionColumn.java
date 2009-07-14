package to.etc.webapp.query;

/**
 * Represents a single selected column.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 14, 2009
 */
final public class QSelectionColumn extends QNodeBase {
	/** When used in a restriction or order an alias is needed for complex query parts. */
	private String				m_alias;

	private QSelectionItem		m_item;

	protected QSelectionColumn(QSelectionItem item) {
		m_item = item;
	}
	protected QSelectionColumn(QSelectionItem item, String alias) {
		m_item = item;
		m_alias = alias;
	}

	/**
	 * Return the alias applied to this selection column.
	 * @return
	 */
	public String getAlias() {
		return m_alias;
	}
	public QSelectionItem getItem() {
		return m_item;
	}
	@Override
	public void visit(QNodeVisitor v) throws Exception {
		v.visitSelectionColumn(this);
	}
}
