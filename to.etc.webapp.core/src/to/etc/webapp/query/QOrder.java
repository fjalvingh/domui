package to.etc.webapp.query;

public class QOrder extends QOperatorNode {
	private String		m_property;
	private QSortOrderDirection	m_direction;

	public QOrder(QSortOrderDirection direction, String property) {
		super(QOperation.ORDER);
		m_direction = direction;
		m_property = property;
	}
	public String getProperty() {
		return m_property;
	}
	public QSortOrderDirection getDirection() {
		return m_direction;
	}
	static public final QOrder	ascending(String name) {
		return new QOrder(QSortOrderDirection.ASC, name);
	}
	static public final QOrder	descending(String name) {
		return new QOrder(QSortOrderDirection.DESC, name);
	}

	static public final QOrder	order(String name, QSortOrderDirection dir) {
		return new QOrder(dir, name);
	}
	@Override
	public void visit(QNodeVisitor v) throws Exception {
		v.visitOrder(this);
	}
}
