package to.etc.domui.hibernate.model;

import org.hibernate.*;
import org.hibernate.criterion.*;

import to.etc.webapp.query.*;

/**
 * Thingy which creates a Hibernate Criteria thingy from a generic query.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 24, 2008
 */
public class CriteriaCreatingVisitor extends QNodeVisitorBase {
	private final Criteria		m_crit;
	private Criterion		m_last;

	public CriteriaCreatingVisitor(final Criteria crit) {
		m_crit = crit;
	}

	@Override
	public void visitCriteria(final QCriteria<?> qc) throws Exception {
		//-- 1. Handle all filters as a compound AND
		for(QOperatorNode n : qc.getOperatorList()) {
			n.visit(this);							// Convert to Criterion
			if(m_last != null)
				m_crit.add(m_last);
		}

		//-- 2. Handle order
		for(QOrder o : qc.getOrder())
			o.visit(this);

		//-- 2. Handle limits and start
		if(qc.getLimit() > 0)
			m_crit.setMaxResults(qc.getLimit());
		if(qc.getStart() > 0) {
			m_crit.setFirstResult(qc.getStart());
		}
	}

	@Override
	public void visitBinaryNode(final QBinaryNode n) throws Exception {
		QOperatorNode	lhs = n.getLhs();
		QOperatorNode	rhs	= n.getRhs();

		QLiteral		lit	= null;
		QPropertyNode	prop= null;
		QPropertyNode	prop2= null;
		if(lhs.getOperation() == QOperation.LITERAL && rhs.getOperation() == QOperation.PROP) {
			lit = (QLiteral) lhs;
			prop= (QPropertyNode) rhs;
		} else if(rhs.getOperation() == QOperation.LITERAL && lhs.getOperation() == QOperation.PROP) {
			lit = (QLiteral) rhs;
			prop= (QPropertyNode) lhs;
		} else if(rhs.getOperation() == QOperation.PROP && lhs.getOperation() == QOperation.PROP) {
			prop= (QPropertyNode) lhs;
			prop2= (QPropertyNode) rhs;
		} else
			throw new IllegalStateException("Unknown operands to "+n.getOperation()+": "+lhs.getOperation()+" and "+rhs.getOperation());

		//-- If prop refers to some relation (dotted pair):
		Criteria	subcrit	= null;
		String		name = null;
		if(prop != null) {
			name = prop.getName();
			if(name.contains(".")) {
				//-- Dotted pair: construe a SubCriteria for the subproperty.
				int	ix	= 0;
				int	len = name.length();
				Criteria	c	= m_crit;
				while(ix < len) {
					int pos = name.indexOf('.', ix);
					if(pos == -1) {
						name = name.substring(ix);			// What's left of the name after prefixes have been removed.
						break;
					}
					String sub = name.substring(ix, pos);
					ix	= pos+1;

					c	= c.createCriteria(sub);
				}
				subcrit = c;
			}
		}

		Criterion	last = null;
		switch(n.getOperation()) {
			default:
				throw new IllegalStateException("Unexpected operation: "+n.getOperation());

			case EQ:
				if(prop2 == null && lit.getValue() == null) {
					last = Restrictions.isNull(name);
					break;
				}
				last	= prop2 != null ? Restrictions.eqProperty(name, prop2.getName()) : Restrictions.eq(name, lit.getValue());
				break;
			case NE:
				if(prop2 == null && lit.getValue() == null) {
					last = Restrictions.isNotNull(name);
					break;
				}
				last	= prop2 != null ? Restrictions.neProperty(name, prop2.getName()) : Restrictions.ne(name, lit.getValue());
				break;
			case GT: 	last	= prop2 != null ? Restrictions.gtProperty(name, prop2.getName()) : Restrictions.gt(name, lit.getValue());	break;
			case GE: 	last	= prop2 != null ? Restrictions.geProperty(name, prop2.getName()) : Restrictions.ge(name, lit.getValue());	break;
			case LT: 	last	= prop2 != null ? Restrictions.ltProperty(name, prop2.getName()) : Restrictions.lt(name, lit.getValue());	break;
			case LE: 	last	= prop2 != null ? Restrictions.leProperty(name, prop2.getName()) : Restrictions.le(name, lit.getValue());	break;
			case LIKE:
				if(prop2 != null)
					throw new IllegalStateException("Cannot use 'like' using two properties.");
				last	= Restrictions.like(name, lit.getValue());
				break;
			case ILIKE:
				if(prop2 != null)
					throw new IllegalStateException("Cannot use 'ilike' using two properties.");
				last	= Restrictions.ilike(name, lit.getValue());
				break;
		}

		if(subcrit != null)
			subcrit.add(last);
		else
			m_last = last;
	}

	@Override
	public void visitBetween(final QBetweenNode n) throws Exception {
		if(! (n.getProp() instanceof QPropertyNode))
			throw new IllegalStateException("Expecting a property as 1st node in between");
		QPropertyNode p = (QPropertyNode)n.getProp();
		if(n.getA().getOperation() != QOperation.LITERAL || n.getB().getOperation() != QOperation.LITERAL)
			throw new IllegalStateException("Expecting literals as 2nd and 3rd between parameter");
		QLiteral a = (QLiteral) n.getA();
		QLiteral b = (QLiteral) n.getB();
		m_last	= Restrictions.between(p.getName(), a.getValue(), b.getValue());
	}

	/**
	 * Compound. Ands and ors.
	 *
	 * @see to.etc.webapp.query.QNodeVisitorBase#visitMulti(to.etc.webapp.query.QMultiNode)
	 */
	@Override
	public void visitMulti(final QMultiNode inn) throws Exception {
		//-- Walk all members, create nodes from 'm.
		Criterion	c1	= null;
		for(QOperatorNode n : inn.getChildren()) {
			n.visit(this);				// Convert node to Criterion thingydoodle
			if(c1 == null)
				c1 = m_last;			// If 1st one use as lhs,
			else {
				switch(inn.getOperation()) {
					default:
						throw new IllegalStateException("Unexpected operation: "+inn.getOperation());
					case AND:
						if(m_last != null)
							c1 = Restrictions.and(c1, m_last);
						break;
					case OR:
						if(m_last != null)
							c1	= Restrictions.or(c1, m_last);
						break;
				}
			}
		}
		if(c1 == null)
			throw new IllegalStateException("? Odd multi - no members?!");
		m_last = c1;
	}

	@Override
	public void visitOrder(final QOrder o) throws Exception {
		Order ho = o.getDirection() == QSortOrderDirection.ASC ? Order.asc(o.getProperty()) : Order.desc(o.getProperty());
		m_crit.addOrder(ho);
	}

	@Override
	public void visitUnaryNode(final QUnaryNode n) throws Exception {
		switch(n.getOperation()) {
			default:
				throw new IllegalStateException("Unsupported UNARY operation: "+n.getOperation());

			case ISNOTNULL:
				if(n.getNode() instanceof QPropertyNode) {
					QPropertyNode pn = (QPropertyNode) n.getNode();
					m_last = Restrictions.isNotNull(pn.getName());
					return;
				}
				break;
			case ISNULL:
				if(n.getNode() instanceof QPropertyNode) {
					QPropertyNode pn = (QPropertyNode) n.getNode();
					m_last = Restrictions.isNull(pn.getName());
					return;
				}
				break;
			case SQL:
				if(n.getNode() instanceof QLiteral) {
					QLiteral l = (QLiteral) n.getNode();
					String s = (String) l.getValue();
					m_last = Restrictions.sqlRestriction(s);
					return;
				}
				break;
		}
		throw new IllegalStateException("Unsupported UNARY operation "+n.getOperation()+" on a(n) "+n.getNode());
	}

	@Override
	public void visitLiteral(final QLiteral n) throws Exception {
		throw new IllegalStateException("? Unexpected literal: "+n);
	}
	@Override
	public void visitPropertyNode(final QPropertyNode n) throws Exception {
		throw new IllegalStateException("? Unexpected property: "+n);
	}

}
