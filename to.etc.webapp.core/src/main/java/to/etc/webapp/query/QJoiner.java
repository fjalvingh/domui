package to.etc.webapp.query;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Helper class to create joins between nested master and subqueries.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 12, 2013
 */
public class QJoiner<A, P, T> {
	@NonNull
	final private QRestrictor<A, ?> m_parent;

	@NonNull
	final private QSubQuery<T, P> m_subQuery;

	protected QJoiner(@NonNull QRestrictor<A, ?> parent, @NonNull QSubQuery<T, P> qSubQuery) {
		m_parent = parent;
		m_subQuery = qSubQuery;
	}

	/**
	 * Adds an eq restriction on the parent to this subquery on the specified properties,
	 * provided that the propertnames are not equal but their type is
	 * Use join if property names are the same.
	 */
	public QJoiner<A, P, T> eq(@NonNull String parentProperty, @NonNull String property) {
		m_subQuery.add(new QPropertyJoinComparison(QOperation.EQ, parentProperty, property));
		return this;
	}

	/**
	 * Adds an ne restriction on the parent to this subquery on the specified properties,
	 */
	public QJoiner<A, P, T> ne(@NonNull String parentProperty, @NonNull String property) {
		m_subQuery.add(new QPropertyJoinComparison(QOperation.NE, parentProperty, property));
		return this;
	}

	/**
	 * Adds an lt restriction on the parent to this subquery on the specified properties,
	 */
	public QJoiner<A, P, T> lt(@NonNull String parentProperty, @NonNull String property) {
		m_subQuery.add(new QPropertyJoinComparison(QOperation.LT, parentProperty, property));
		return this;
	}

	/**
	 * Adds an le restriction on the parent to this subquery on the specified properties,
	 */
	public QJoiner<A, P, T> le(@NonNull String parentProperty, @NonNull String property) {
		m_subQuery.add(new QPropertyJoinComparison(QOperation.LE, parentProperty, property));
		return this;
	}

	/**
	 * Adds an lt restriction on the parent to this subquery on the specified properties,
	 */
	public QJoiner<A, P, T> gt(@NonNull String parentProperty, @NonNull String property) {
		m_subQuery.add(new QPropertyJoinComparison(QOperation.GT, parentProperty, property));
		return this;
	}

	/**
	 * Adds an ge restriction on the parent to this subquery on the specified properties,
	 */
	public QJoiner<A, P, T> ge(@NonNull String parentProperty, @NonNull String property) {
		m_subQuery.add(new QPropertyJoinComparison(QOperation.GE, parentProperty, property));
		return this;
	}
}
