package to.etc.webapp.query;

import java.util.*;

public class QProjection {
	private int					m_limit = -1;
	private int					m_start = 0;
	private List<QOperatorNode>	m_operatorList = Collections.EMPTY_LIST;
	private List<QOrder>		m_order = Collections.EMPTY_LIST;

	public QProjection	add(QOperatorNode r) {
		if(m_operatorList == Collections.EMPTY_LIST)
			m_operatorList = new ArrayList<QOperatorNode>();
		m_operatorList.add(r);
		return this;
	}
	public QProjection	add(QOrder r) {
		if(m_order == Collections.EMPTY_LIST)
			m_order = new ArrayList<QOrder>();
		m_order.add(r);
		return this;
	}
	public QProjection	ascending(String property) {
		add(QOrder.ascending(property));
		return this;
	}
	public QProjection	descending(String property) {
		add(QOrder.descending(property));
		return this;
	}
	public QProjection	eq(String property, Object value) {
		add(QRestriction.eq(property, value));
		return this;
	}
	public QProjection	ne(String property, Object value) {
		add(QRestriction.ne(property, value));
		return this;
	}
	public QProjection	gt(String property, Object value) {
		add(QRestriction.gt(property, value));
		return this;
	}
	public QProjection	lt(String property, Object value) {
		add(QRestriction.lt(property, value));
		return this;
	}
	public QProjection	ge(String property, Object value) {
		add(QRestriction.ge(property, value));
		return this;
	}
	public QProjection	le(String property, Object value) {
		add(QRestriction.le(property, value));
		return this;
	}
	public QProjection	like(String property, Object value) {
		add(QRestriction.like(property, value));
		return this;
	}
	public QProjection	between(String property, Object a, Object b) {
		add(QRestriction.between(property, a, b));
		return this;
	}
	public QProjection	ilike(String property, Object value) {
		add(QRestriction.ilike(property, value));
		return this;
	}
	public QProjection	or(QOperatorNode... a) {
		add(QRestriction.or(a));
		return this;
	}
	public QProjection	isnull(String property) {
		add(QRestriction.isnull(property));
		return this;
	}
	public QProjection	isnotnull(String property) {
		add(QRestriction.isnotnull(property));
		return this;
	}
	public QProjection	sqlCondition(String sql) {
		add(QRestriction.sqlCondition(sql));
		return this;
	}
	public QProjection limit(int limit) {
		m_limit = limit;
		return this;
	}
	public QProjection start(int start) {
		m_start = start;
		return this;
	}
	public int getLimit() {
		return m_limit;
	}
	public int getStart() {
		return m_start;
	}
	public List<QOperatorNode> getWhereList() {
		return m_operatorList;
	}
	public int	getOperatorCount() {
		return m_operatorList.size();
	}
	public List<QOrder> getOrder() {
		return m_order;
	}

}
