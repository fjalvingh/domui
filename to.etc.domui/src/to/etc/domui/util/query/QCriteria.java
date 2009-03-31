package to.etc.domui.util.query;

import java.util.*;

/**
 * 
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 24, 2008
 */
public class QCriteria<T> {
	private Class<T>			m_baseClass;

	private int					m_limit = -1;
	private int					m_start = 0;

	private List<QOperatorNode>	m_operatorList = Collections.EMPTY_LIST;
	private List<QOrder>		m_order = Collections.EMPTY_LIST;

	public QCriteria(Class<T> b) {
		m_baseClass = b;
	}

	/**
	 * Copy constructor.
	 * @param q
	 */
	protected QCriteria(QCriteria<T> q) {
		m_operatorList	= new ArrayList<QOperatorNode>(q.m_operatorList);
		m_order	= new ArrayList<QOrder>(q.m_order);
		m_baseClass = q.m_baseClass;
		m_limit		= q.m_limit;
		m_start		= q.m_start;
	}

	static public <U> QCriteria<U>	create(Class<U> clz) {
		return new QCriteria<U>(clz);
	}

	public QCriteria<T>	add(QOperatorNode r) {
		if(m_operatorList == Collections.EMPTY_LIST)
			m_operatorList = new ArrayList<QOperatorNode>();
		m_operatorList.add(r);
		return this;
	}
	public QCriteria<T>	add(QOrder r) {
		if(m_order == Collections.EMPTY_LIST)
			m_order = new ArrayList<QOrder>();
		m_order.add(r);
		return this;
	}
	public QCriteria<T>	ascending(String property) {
		add(QOrder.ascending(property));
		return this;
	}
	public QCriteria<T>	descending(String property) {
		add(QOrder.descending(property));
		return this;
	}
	public QCriteria<T>	eq(String property, Object value) {
		add(QRestriction.eq(property, value));
		return this;
	}
	public QCriteria<T>	ne(String property, Object value) {
		add(QRestriction.ne(property, value));
		return this;
	}
	public QCriteria<T>	gt(String property, Object value) {
		add(QRestriction.gt(property, value));
		return this;
	}
	public QCriteria<T>	lt(String property, Object value) {
		add(QRestriction.lt(property, value));
		return this;
	}
	public QCriteria<T>	ge(String property, Object value) {
		add(QRestriction.ge(property, value));
		return this;
	}
	public QCriteria<T>	le(String property, Object value) {
		add(QRestriction.le(property, value));
		return this;
	}
	public QCriteria<T>	like(String property, Object value) {
		add(QRestriction.like(property, value));
		return this;
	}
	public QCriteria<T>	between(String property, Object a, Object b) {
		add(QRestriction.between(property, a, b));
		return this;
	}
	public QCriteria<T>	ilike(String property, Object value) {
		add(QRestriction.ilike(property, value));
		return this;
	}
	public QCriteria<T>	or(QOperatorNode... a) {
		add(QRestriction.or(a));
		return this;
	}
	public QCriteria<T>	isnull(String property) {
		add(QRestriction.isnull(property));
		return this;
	}
	public QCriteria<T>	isnotnull(String property) {
		add(QRestriction.isnotnull(property));
		return this;
	}
	public QCriteria<T>	sqlCondition(String sql) {
		add(QRestriction.sqlCondition(sql));
		return this;
	}
	public QCriteria<T> limit(int limit) {
		m_limit = limit;
		return this;
	}
	public QCriteria<T> start(int start) {
		m_start = start;
		return this;
	}
	public int getLimit() {
		return m_limit;
	}
	public int getStart() {
		return m_start;
	}
	public Class<T> getBaseClass() {
		return m_baseClass;
	}
	public List<QOperatorNode> getOperatorList() {
		return m_operatorList;
	}
	public int	getOperatorCount() {
		return m_operatorList.size();
	}
	public List<QOrder> getOrder() {
		return m_order;
	}

	/**
	 * Create a duplicate of this Criteria.
	 * @return
	 */
	public QCriteria<T>		dup() {
		return new QCriteria<T>(this);
	}
	public void		visit(QNodeVisitor v) throws Exception {
		v.visitCriteria(this);
	}
}
