package to.etc.webapp.query;


/**
 * Represents the "where" part of a query, or a part of that "where" part, under construction.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 22, 2009
 */
abstract class QRestrictor<T> {
	/** The base class being queried in this selector. */
	private final Class<T> m_baseClass;

	abstract public QOperatorNode getRestrictions();

	/**
	 * Returns the #of restrictions added to this set!? Useless??
	 * @return
	 */
	abstract public boolean hasRestrictions();

	/**
	 * Add a new restriction to the list of restrictions on the data. This will do "and" collapsion: when the node added is an "and"
	 * it's nodes will be added directly to the list (because that already represents an and combinatory).
	 * @param r
	 * @return
	 */
	abstract public void internalAdd(QOperatorNode r);

	protected QRestrictor(Class<T> baseClass) {
		m_baseClass = baseClass;
	}

	/**
	 * Returns the persistent class being queried and returned.
	 * @return
	 */
	public Class<T> getBaseClass() {
		return m_baseClass;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Adding selection restrictions (where clause)		*/
	/*--------------------------------------------------------------*/
	public QRestrictor<T> add(QOperatorNode n) {
		internalAdd(n);
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> eq(String property, Object value) {
		add(QRestriction.eq(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> eq(String property, long value) {
		add(QRestriction.eq(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> eq(String property, double value) {
		add(QRestriction.eq(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> ne(String property, Object value) {
		add(QRestriction.ne(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> ne(String property, long value) {
		add(QRestriction.ne(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> ne(String property, double value) {
		add(QRestriction.ne(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> gt(String property, Object value) {
		add(QRestriction.gt(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> gt(String property, long value) {
		add(QRestriction.gt(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> gt(String property, double value) {
		add(QRestriction.gt(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> lt(String property, Object value) {
		add(QRestriction.lt(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> lt(String property, long value) {
		add(QRestriction.lt(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> lt(String property, double value) {
		add(QRestriction.lt(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> ge(String property, Object value) {
		add(QRestriction.ge(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> ge(String property, long value) {
		add(QRestriction.ge(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> ge(String property, double value) {
		add(QRestriction.ge(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> le(String property, Object value) {
		add(QRestriction.le(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> le(String property, long value) {
		add(QRestriction.le(property, value));
		return this;
	}

	/**
	 * Compare a property with some literal object value.
	 *
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> le(String property, double value) {
		add(QRestriction.le(property, value));
		return this;
	}

	/**
	 * Do a 'like' comparison. The wildcard marks here are always %; a literal % is to
	 * be presented as \%. The comparison is case-dependent.
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> like(String property, Object value) {
		add(QRestriction.like(property, value));
		return this;
	}

	/**
	 * Compare the value of a property with two literal bounds.
	 * @param property
	 * @param a
	 * @param b
	 * @return
	 */
	public QRestrictor<T> between(String property, Object a, Object b) {
		add(QRestriction.between(property, a, b));
		return this;
	}

	/**
	 * Do a case-independent 'like' comparison. The wildcard marks here are always %; a literal % is to
	 * be presented as \%. The comparison is case-independent.
	 * @param property
	 * @param value
	 * @return
	 */
	public QRestrictor<T> ilike(String property, Object value) {
		add(QRestriction.ilike(property, value));
		return this;
	}

	/**
	 * Add a set of OR nodes to the set.
	 * @param a
	 * @return
	 */
	@Deprecated
	public QRestrictor<T> or(QOperatorNode a1, QOperatorNode a2, QOperatorNode... rest) {
		QOperatorNode[] ar = new QOperatorNode[rest.length + 2];
		ar[0] = a1;
		ar[1] = a2;
		System.arraycopy(rest, 0, ar, 2, rest.length);
		add(QRestriction.or(ar));
		return this;
	}

	/**
	 * Return a thingy that can be used to create "or" nodes;
	 * @return
	 */
	public QOr<T> or() {
		QMultiNode or = new QMultiNode(QOperation.OR);
		add(or);
		return new QOr<T>(this, or);
	}

	/**
	 * Add the restriction that the property specified must be null.
	 * @param property
	 * @return
	 */
	public QRestrictor<T> isnull(String property) {
		add(QRestriction.isnull(property));
		return this;
	}

	/**
	 * Add the restriction that the property specified must be not-null.
	 *
	 * @param property
	 * @return
	 */
	public QRestrictor<T> isnotnull(String property) {
		add(QRestriction.isnotnull(property));
		return this;
	}

	/**
	 * Add a restriction specified in bare SQL. This is implementation-dependent.
	 * @param sql
	 * @return
	 */
	public QRestrictor<T> sqlCondition(String sql) {
		add(QRestriction.sqlCondition(sql));
		return this;
	}

	/**
	 * Create a joined "exists" subquery on some child list property. The parameters passed have a relation with eachother;
	 * this relation cannot be checked at compile time because Java still lacks property references (Sun is still too utterly
	 * stupid to define them). They will be checked at runtime when the query is executed.
	 *
	 * @param <U>			The type of the children.
	 * @param childclass	The class type of the children, because Java Generics is too bloody stupid to find out itself.
	 * @param childproperty	The name of the property <i>in</i> the parent class <T> that represents the List<U> of child records.
	 * @return
	 */
	public <U> QRestrictor<U> exists(Class<U> childclass, String childproperty) {
		QExistsSubquery<U> sq = new QExistsSubquery<U>(this, childclass, childproperty);
		QDelegatingRestrictor<U> builder = new QDelegatingRestrictor<U>(childclass, sq);
		add(sq);
		return builder;
	}
}
