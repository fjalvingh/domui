package to.etc.webapp.query;

import javax.annotation.*;

import to.etc.webapp.annotations.*;

/**
 * Helper class to create joins between nested master and subqueries.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 12, 2013
 */
public class QJoiner<A, P, T> {
	@Nonnull
	final private QRestrictor<A> m_parent;

	@Nonnull
	final private QSubQuery<T, P> m_subQuery;

	protected QJoiner(@Nonnull QRestrictor<A> parent, @Nonnull QSubQuery<T, P> qSubQuery) {
		m_parent = parent;
		m_subQuery = qSubQuery;
	}

	/**
	 * Adds an eq restriction on the parent to this subquery on the specified properties,
	 * provided that the propertnames are not equal but their type is
	 * Use join if property names are the same.
	 * @param parentProperty
	 * @param property
	 */
	public QJoiner<A, P, T> eq(@Nonnull @GProperty("A") String parentProperty, @Nonnull @GProperty("T") String property) {
		m_subQuery.add(new QPropertyJoinComparison(QOperation.EQ, parentProperty, property));
		return this;
	}

	/**
	 * Adds an ne restriction on the parent to this subquery on the specified properties,
	 * @param parentProperty
	 * @param property
	 */
	public QJoiner<A, P, T> ne(@Nonnull @GProperty("A") String parentProperty, @Nonnull @GProperty("T") String property) {
		m_subQuery.add(new QPropertyJoinComparison(QOperation.NE, parentProperty, property));
		return this;
	}

	/**
	 * Adds an lt restriction on the parent to this subquery on the specified properties,
	 * @param parentProperty
	 * @param property
	 */
	public QJoiner<A, P, T> lt(@Nonnull @GProperty("A") String parentProperty, @Nonnull @GProperty("T") String property) {
		m_subQuery.add(new QPropertyJoinComparison(QOperation.LT, parentProperty, property));
		return this;
	}

	/**
	 * Adds an le restriction on the parent to this subquery on the specified properties,
	 * @param parentProperty
	 * @param property
	 */
	public QJoiner<A, P, T> le(@Nonnull @GProperty("A") String parentProperty, @Nonnull @GProperty("T") String property) {
		m_subQuery.add(new QPropertyJoinComparison(QOperation.LE, parentProperty, property));
		return this;
	}

	/**
	 * Adds an lt restriction on the parent to this subquery on the specified properties,
	 * @param parentProperty
	 * @param property
	 */
	public QJoiner<A, P, T> gt(@Nonnull @GProperty("A") String parentProperty, @Nonnull @GProperty("T") String property) {
		m_subQuery.add(new QPropertyJoinComparison(QOperation.GT, parentProperty, property));
		return this;
	}

	/**
	 * Adds an ge restriction on the parent to this subquery on the specified properties,
	 * @param parentProperty
	 * @param property
	 */
	public QJoiner<A, P, T> ge(@Nonnull @GProperty("A") String parentProperty, @Nonnull @GProperty("T") String property) {
		m_subQuery.add(new QPropertyJoinComparison(QOperation.GE, parentProperty, property));
		return this;
	}
}
