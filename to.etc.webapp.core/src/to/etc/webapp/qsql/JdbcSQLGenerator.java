package to.etc.webapp.qsql;

import java.util.*;

import to.etc.webapp.query.*;

/**
 * Generate a SQL query from a QCriteria selection using the poor man's JDBC code.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 25, 2009
 */
public class JdbcSQLGenerator extends QNodeVisitorBase {
	private PClassRef m_root;

	private JdbcClassMeta m_rootMeta;

	private Map<String, PClassRef> m_tblMap = new HashMap<String, PClassRef>();

	private StringBuilder m_fields = new StringBuilder();

	/** The list of all retrievers for a single row */
	private List<IInstanceMaker> m_retrieverList = new ArrayList<IInstanceMaker>();

	private int m_nextFieldIndex = 1;

	private StringBuilder m_where = new StringBuilder();

	private int m_nextWhereIndex = 1;

	private int m_curPrec = 0;

	private List<ValSetter> m_valList = new ArrayList<ValSetter>();

	/** FIXME Need some better way to set this */
	private boolean m_oracle = true;

	@Override
	public void visitCriteria(QCriteria< ? > qc) throws Exception {
		m_root = new PClassRef(qc.getBaseClass(), "this_");
		m_tblMap.put(m_root.getAlias(), m_root);
		m_rootMeta = JdbcMetaManager.getMeta(qc.getBaseClass());
		generateClassGetter(m_root);
		super.visitCriteria(qc);
	}

	@Override
	public void visitSelection(QSelection< ? > s) throws Exception {
		throw new IllegalStateException("Not implemented yet");
	}

	private void generateClassGetter(PClassRef root) throws Exception {
		JdbcClassMeta cm = JdbcMetaManager.getMeta(root.getDataClass()); // Will throw exception if not proper jdbc class.
		int startIndex = m_nextFieldIndex;
		for(JdbcPropertyMeta pm : cm.getPropertyList()) {
			if(!pm.isTransient()) {
				if(m_fields.length() != 0)
					m_fields.append(",");
				m_fields.append(root.getAlias());
				m_fields.append(".");
				m_fields.append(pm.getColumnName());
				m_nextFieldIndex++;
			}
		}
		m_retrieverList.add(new ClassInstanceMaker(root, startIndex, cm));
	}

	public String getSQL() throws Exception {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select ");
		sb.append(m_fields);
		sb.append(" from ");

		JdbcClassMeta cm = JdbcMetaManager.getMeta(m_root.getDataClass());
		sb.append(cm.getTableName());
		sb.append(" ");
		sb.append(m_root.getAlias());

		if(m_where.length() > 0) {
			sb.append(" where ");
			sb.append(m_where);
		}
		return sb.toString();
	}

	public List<ValSetter> getValList() {
		return m_valList;
	}

	public List<IInstanceMaker> getRetrieverList() {
		return m_retrieverList;
	}

	public JdbcQuery< ? > getQuery() throws Exception {
		return new JdbcQuery<Object>(getSQL(), m_retrieverList, m_valList);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Restrictions rendering.								*/
	/*--------------------------------------------------------------*/
	private void appendWhere(String s) {
		m_where.append(s);
	}

	/**
	 * Render an operator set.
	 * @see to.etc.webapp.query.QNodeVisitorBase#visitMulti(to.etc.webapp.query.QMultiNode)
	 */
	@Override
	public void visitMulti(QMultiNode n) throws Exception {
		if(n.getChildren().length == 0)
			return;
		if(n.getChildren().length == 1) { // Should not really happen
			n.getChildren()[0].visit(this);
			return;
		}
		int oldprec = m_curPrec;
		m_curPrec = getOperationPrecedence(n.getOperation());
		if(oldprec > m_curPrec)
			appendWhere("(");
		int ct = 0;
		for(QOperatorNode c : n.getChildren()) {
			if(ct++ > 0)
				appendOperation(n.getOperation());

			//-- Visit lower
			c.visit(this);
		}
		if(oldprec > m_curPrec)
			appendWhere(")");
		m_curPrec = oldprec;
	}

	@Override
	public void visitPropertyComparison(QPropertyComparison n) throws Exception {
		int oldprec = m_curPrec;
		m_curPrec = getOperationPrecedence(n.getOperation());
		if(oldprec > m_curPrec)
			appendWhere("(");

		//-- Lookup the property name. For now it cannot be dotted
		if(n.getOperation() == QOperation.ILIKE && m_oracle) {
			JdbcPropertyMeta pm = resolveProperty(n.getProperty());
			appendWhere("upper(");
			appendWhere(pm.getColumnName());
			appendWhere(") like upper(");

			if(n.getExpr() instanceof QLiteral) {
				appendValueSetter(pm, (QLiteral) n.getExpr());
			} else
				throw new IllegalStateException("Unexpected argument to " + n + ": " + n.getExpr());
			appendWhere(")");
		} else {
			JdbcPropertyMeta pm = resolveProperty(n.getProperty());
			appendWhere(pm.getColumnName());
			appendOperation(n.getOperation());

			if(n.getExpr() instanceof QLiteral) {
				appendValueSetter(pm, (QLiteral) n.getExpr());
			} else
				throw new IllegalStateException("Unexpected argument to " + n + ": " + n.getExpr());
		}
		if(oldprec > m_curPrec)
			appendWhere(")");
		m_curPrec = oldprec;
	}

	/**
	 * Generate some comparison in where with a literal value. The literal must be conversion-compatible with
	 * the type converter for the property or sadness ensues.
	 * @param pm
	 * @param expr
	 */
	private void appendValueSetter(JdbcPropertyMeta pm, QLiteral expr) {
		appendWhere("?");
		int index = m_nextWhereIndex++;

		ITypeConverter tc = pm.getTypeConverter();
		if(tc == null)
			tc = JdbcMetaManager.getConverter(pm);
		ValSetter vs = new ValSetter(index, expr.getValue(), tc, pm);
		m_valList.add(vs);
	}

	private JdbcPropertyMeta resolveProperty(String pname) {
		if(pname.indexOf('.') != -1)
			throw new IllegalStateException("Path properties are not allowed (yet): " + pname);

		//-- Lookup,
		JdbcPropertyMeta pm = m_rootMeta.findProperty(pname);
		if(pm == null)
			throw new IllegalStateException(m_rootMeta.getDataClass() + "." + pname + ": unknown property");
		return pm;
	}

	@Override
	public void visitUnaryProperty(QUnaryProperty n) throws Exception {
		int oldprec = m_curPrec;
		m_curPrec = getOperationPrecedence(n.getOperation());
		if(oldprec > m_curPrec)
			appendWhere("(");

		appendOperation(n.getOperation());
		appendWhere("(");
		JdbcPropertyMeta pm = resolveProperty(n.getProperty());
		appendWhere(pm.getColumnName());
		appendWhere(")");

		if(oldprec > m_curPrec)
			appendWhere(")");
		m_curPrec = oldprec;
	}

	@Override
	public void visitBetween(QBetweenNode n) throws Exception {
		int oldprec = m_curPrec;
		m_curPrec = getOperationPrecedence(n.getOperation());
		if(oldprec > m_curPrec)
			appendWhere("(");

		//-- Lookup the property name. For now it cannot be dotted
		JdbcPropertyMeta pm = resolveProperty(n.getProp());
		appendWhere(pm.getColumnName());
		appendWhere(" between ");

		if(n.getA() instanceof QLiteral) {
			appendValueSetter(pm, (QLiteral) n.getA());
		} else
			throw new IllegalStateException("Unexpected argument to " + n + ": " + n.getA());

		appendWhere(" and ");
		if(n.getB() instanceof QLiteral) {
			appendValueSetter(pm, (QLiteral) n.getB());
		} else
			throw new IllegalStateException("Unexpected argument to " + n + ": " + n.getB());

		if(oldprec > m_curPrec)
			appendWhere(")");
		m_curPrec = oldprec;
	}

	@Override
	public void visitLiteral(QLiteral n) throws Exception {
		throw new IllegalStateException("!!! Trying to generate a naked literal!");
	}

	private void appendOperation(QOperation op) {
		appendOperation(renderOperation(op));
	}

	private void appendOperation(String renderOperation) {
		if(Character.isLetter(renderOperation.charAt(0))) {
			if(m_where.length() > 0 && m_where.charAt(m_where.length() - 1) != ' ')
				appendWhere(" ");
			appendWhere(renderOperation);
			appendWhere(" ");
		} else
			appendWhere(renderOperation);
	}

	static private String renderOperation(QOperation op) {
		switch(op){
			default:
				throw new IllegalStateException("Unexpected operation type=" + op);
			case AND:
				return "and";
			case OR:
				return "or";
			case BETWEEN:
				return "between";
			case EQ:
				return "=";
			case NE:
				return "!=";
			case LT:
				return "<";
			case LE:
				return "<=";
			case GT:
				return ">";
			case GE:
				return ">=";
			case ILIKE:
				return "ilike";
			case LIKE:
				return "like";
			case ISNOTNULL:
				return "is not null";
			case ISNULL:
				return "is null";
			case SQL:
				return "SQL";
		}
	}

	/**
	 * Returns the operator precedence
	 * @param ot
	 * @return
	 */
	static public int getOperationPrecedence(final QOperation ot) {
		switch(ot){
			default:
				throw new IllegalStateException("Unknown operator " + ot);
			case OR:
				return 10;
			case AND:
				return 20;
				/*case IN: */case BETWEEN:
			case LIKE:
			case ILIKE:
				return 30;

			case LT:
			case LE:
			case GT:
			case GE:
			case EQ:
			case NE:
			case ISNULL:
			case ISNOTNULL:
				return 40;
				//			case NOT:
				//				return 50;
				//				// ANY, ALL, SOME: 60
				//			case CONCAT:
				//				return 70;
				//			case PLUS: case MINUS:
				//				return 80;
				//			case MULT: case DIV: case MOD:
				//				return 90;
				//			case UMINUS:
				//				return 100;

			case LITERAL:
				return 100;
		}
	}
}
