package to.etc.domui.hibernate.model;

import org.hibernate.*;
import org.hibernate.criterion.*;

import to.etc.webapp.*;
import to.etc.webapp.query.*;

/**
 * Thingy which creates a Hibernate Criteria thingy from a generic query.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 24, 2008
 */
public class CriteriaCreatingVisitor extends QNodeVisitorBase {
	private final Criteria m_crit;

	private Criterion m_last;

	public CriteriaCreatingVisitor(final Criteria crit) {
		m_crit = crit;
	}

	@Override
	public void visitRestrictionsBase(QRestrictionsBase<?> n) throws Exception {
		QOperatorNode r = n.getRestrictions();
		if(r == null)
			return;
		if(r.getOperation() == QOperation.AND) {
			QMultiNode mn = (QMultiNode) r;
			for(QOperatorNode qtn : mn.getChildren()) {
				qtn.visit(this);
				if(m_last != null)
					m_crit.add(m_last);
			}
		} else {
			r.visit(this);
			m_crit.add(m_last);
		}
	}

	@Override
	public void visitCriteria(final QCriteria< ? > qc) throws Exception {
		super.visitCriteria(qc);

		//-- 2. Handle limits and start
		if(qc.getLimit() > 0)
			m_crit.setMaxResults(qc.getLimit());
		if(qc.getStart() > 0) {
			m_crit.setFirstResult(qc.getStart());
		}
	}


	@Override
	public void visitPropertyComparison(QPropertyComparison n) throws Exception {
		QOperatorNode rhs = n.getExpr();
		String name = n.getProperty();
		QLiteral lit = null;
		if(rhs.getOperation() == QOperation.LITERAL) {
			lit = (QLiteral) rhs;
		} else
			throw new IllegalStateException("Unknown operands to " + n.getOperation() + ": " + name + " and " + rhs.getOperation());

		//-- If prop refers to some relation (dotted pair):
		Criteria subcrit = null;
		if(name.contains(".")) {
			//-- Dotted pair: construe a SubCriteria for the subproperty.
			int ix = 0;
			int len = name.length();
			Criteria c = m_crit;
			while(ix < len) {
				int pos = name.indexOf('.', ix);
				if(pos == -1) {
					name = name.substring(ix); // What's left of the name after prefixes have been removed.
					break;
				}
				String sub = name.substring(ix, pos);
				ix = pos + 1;

				c = c.createCriteria(sub);
			}
			subcrit = c;
		}

		Criterion last = null;
		switch(n.getOperation()){
			default:
				throw new IllegalStateException("Unexpected operation: " + n.getOperation());

			case EQ:
				if(lit.getValue() == null) {
					last = Restrictions.isNull(name);
					break;
				}
				last = Restrictions.eq(name, lit.getValue());
				break;
			case NE:
				if(lit.getValue() == null) {
					last = Restrictions.isNotNull(name);
					break;
				}
				last = Restrictions.ne(name, lit.getValue());
				break;
			case GT:
				last = Restrictions.gt(name, lit.getValue());
				break;
			case GE:
				last = Restrictions.ge(name, lit.getValue());
				break;
			case LT:
				last = Restrictions.lt(name, lit.getValue());
				break;
			case LE:
				last = Restrictions.le(name, lit.getValue());
				break;
			case LIKE:
				last = Restrictions.like(name, lit.getValue());
				break;
			case ILIKE:
				last = Restrictions.ilike(name, lit.getValue());
				break;
		}

		if(subcrit != null)
			subcrit.add(last);
		else
			m_last = last;
	}

	@Override
	public void visitBetween(final QBetweenNode n) throws Exception {
		if(n.getA().getOperation() != QOperation.LITERAL || n.getB().getOperation() != QOperation.LITERAL)
			throw new IllegalStateException("Expecting literals as 2nd and 3rd between parameter");
		QLiteral a = (QLiteral) n.getA();
		QLiteral b = (QLiteral) n.getB();

		//-- If prop refers to some relation (dotted pair):
		String name = n.getProp();
		Criteria subcrit = null;
		if(name.contains(".")) {
			//-- Dotted pair: construe a SubCriteria for the subproperty.
			int ix = 0;
			int len = name.length();
			Criteria c = m_crit;
			while(ix < len) {
				int pos = name.indexOf('.', ix);
				if(pos == -1) {
					name = name.substring(ix); // What's left of the name after prefixes have been removed.
					break;
				}
				String sub = name.substring(ix, pos);
				ix = pos + 1;

				c = c.createCriteria(sub);
			}
			subcrit = c;
		}

		if(subcrit != null)
			subcrit.add(Restrictions.between(name, a.getValue(), b.getValue()));
		else
			m_last = Restrictions.between(n.getProp(), a.getValue(), b.getValue());
	}

	/**
	 * Compound. Ands and ors.
	 *
	 * @see to.etc.webapp.query.QNodeVisitorBase#visitMulti(to.etc.webapp.query.QMultiNode)
	 */
	@Override
	public void visitMulti(final QMultiNode inn) throws Exception {
		//-- Walk all members, create nodes from 'm.
		Criterion c1 = null;
		for(QOperatorNode n : inn.getChildren()) {
			n.visit(this); // Convert node to Criterion thingydoodle
			if(c1 == null)
				c1 = m_last; // If 1st one use as lhs,
			else {
				switch(inn.getOperation()){
					default:
						throw new IllegalStateException("Unexpected operation: " + inn.getOperation());
					case AND:
						if(m_last != null)
							c1 = Restrictions.and(c1, m_last);
						break;
					case OR:
						if(m_last != null)
							c1 = Restrictions.or(c1, m_last);
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
		switch(n.getOperation()){
			default:
				throw new IllegalStateException("Unsupported UNARY operation: " + n.getOperation());
			case SQL:
				if(n.getNode() instanceof QLiteral) {
					QLiteral l = (QLiteral) n.getNode();
					String s = (String) l.getValue();
					m_last = Restrictions.sqlRestriction(s);
					return;
				}
				break;
		}
		throw new IllegalStateException("Unsupported UNARY operation: " + n.getOperation());
	}

	@Override
	public void visitUnaryProperty(final QUnaryProperty n) throws Exception {
		switch(n.getOperation()){
			default:
				throw new IllegalStateException("Unsupported UNARY operation: " + n.getOperation());

			case ISNOTNULL:
				m_last = Restrictions.isNotNull(n.getProperty());
				return;
			case ISNULL:
				m_last = Restrictions.isNull(n.getProperty());
				return;
		}
	}

	@Override
	public void visitLiteral(final QLiteral n) throws Exception {
		throw new IllegalStateException("? Unexpected literal: " + n);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Selection translation to Projection.				*/
	/*--------------------------------------------------------------*/
	private ProjectionList		m_proli;
	private Projection			m_lastProj;

	@Override
	public void visitMultiSelection(QMultiSelection n) throws Exception {
		throw new ProgrammerErrorException("multi-operation selections not supported by Hibernate");
	}
	@Override
	public void visitSelection(QSelection< ? > s) throws Exception {
		if(m_proli != null)
			throw new IllegalStateException("? Projection list already initialized??");
		m_proli = Projections.projectionList();
		visitSelectionColumns(s);
		m_crit.setProjection(m_proli);
		visitRestrictionsBase(s);
		visitOrderList(s.getOrder());
	}

	@Override
	public void visitSelectionColumn(QSelectionColumn n) throws Exception {
		super.visitSelectionColumn(n);
		if(m_lastProj != null)
			m_proli.add(m_lastProj);
	}

	@Override
	public void visitSelectionItem(QSelectionItem n) throws Exception {
		throw new ProgrammerErrorException("Unexpected selection item: "+n);
	}
	@Override
	public void visitPropertySelection(QPropertySelection n) throws Exception {
		switch(n.getFunction()) {
			default:
				throw new IllegalStateException("Unexpected selection item function: "+n.getFunction());
			case AVG:	m_lastProj = Projections.avg(n.getProperty());	break;
			case MAX:	m_lastProj = Projections.max(n.getProperty());	break;
			case MIN:	m_lastProj = Projections.min(n.getProperty());	break;
			case SUM:	m_lastProj = Projections.sum(n.getProperty());	break;
			case COUNT:	m_lastProj = Projections.count(n.getProperty());	break;
			case COUNT_DISTINCT:	m_lastProj = Projections.countDistinct(n.getProperty());	break;
			case ID:	m_lastProj = Projections.id();	break;
			case PROPERTY:	m_lastProj = Projections.property(n.getProperty());	break;
			case ROWCOUNT:	m_lastProj = Projections.rowCount();			break;
		}
	}
}
