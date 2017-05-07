package to.etc.dbutil.reverse;

import java.math.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.Date;

import javax.annotation.*;

import to.etc.dbutil.schema.*;
import to.etc.util.*;
import to.etc.webapp.qsql.*;
import to.etc.webapp.query.*;

/**
 * Helper class to create a query statement where the exact table and column of each result row is known. It
 * only supports single-table queries, no joins.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 19, 2013
 */
public class SQLBuilder extends QNodeVisitorBase {
	@Nonnull
	final private Reverser m_reverser;

	@Nonnull
	final private QCriteria<SQLRow> m_criteria;

	@Nonnull
	final private DbTable m_table;

	@Nonnull
	private StringBuilder m_statement = new StringBuilder();

	private int m_firstRow = 0;

	private int m_maxRows = 50;

	private String m_sql;

	private boolean m_islimited;

	private List<DbColumn> m_columnList;

	private PTableRef m_root;

	private Map<String, PTableRef> m_tblMap = new HashMap<String, PTableRef>();

	private StringBuilder m_fields = new StringBuilder();

	/** The list of all retrievers for a single row */
//	private List<IInstanceMaker> m_retrieverList = new ArrayList<IInstanceMaker>();

	private int m_nextFieldIndex = 1;

	private StringBuilder m_where = new StringBuilder();

	private StringBuilder m_order;

	private int m_nextWhereIndex = 1;

	private int m_curPrec = 0;

	private List<IQValueSetter> m_valList = new ArrayList<IQValueSetter>();

	/** FIXME Need some better way to set this */
	private boolean m_oracle = true;

	private int m_start, m_limit;

	private int m_timeout = -1;

	public SQLBuilder(@Nonnull JDBCReverser r, @Nonnull QCriteria<SQLRow> table, boolean oracle) {
		m_oracle = oracle;
		m_reverser = r;
		m_criteria = table;
		QDbTable dbt = (QDbTable) table.getMetaTable();
		m_table = dbt.getTable();
	}

	public void setRows(int first, int max) {
		m_firstRow = first;
		m_maxRows = max;
	}

	@Nonnull
	private Reverser r() {
		return m_reverser;
	}

	/**
	 * Create the column-based select statement.
	 */
	public void createSelect() throws Exception {
		visitCriteria(m_criteria);

//		m_statement.setLength(0);
//
//		//-- 1. Sort all lobs and clobs to the end.
//		List<DbColumn> coll = new ArrayList<DbColumn>();
//		for(DbColumn c : m_table.getColumnList()) {
//			if(c.isLob())
//				continue;
//			coll.add(c);
//		}
//		for(DbColumn c : m_table.getColumnList()) {
//			if(!c.isLob())
//				continue;
//			coll.add(c);
//		}
//
//		//-- 2. Create a select xxx statement
//		a("select ");
//		int ix = 0;
//		for(DbColumn c: coll) {
//			if(ix++ > 0)
//				a(",");
//			r().addSelectColumnAs(m_statement, c.getName(), "_c" + ix);
//		}
//		a(" from ").a(m_table.getSchema().getName()).a(".").a(m_table.getName());
//
//		//-- Criteria.
//
//
//		String sql = m_statement.toString();
//		String nsql = r().wrapQueryWithRange(coll, sql, m_firstRow, m_maxRows);
//		if(nsql != null) {
//			m_sql = nsql;
//			m_islimited = true;
//		} else {
//			m_sql = sql;
//			m_islimited = false;
//		}
//		m_columnList = coll;
	}

	public SQLRowSet query(@Nonnull Connection dbc) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = dbc.prepareStatement(m_sql);
			for(IQValueSetter vs : m_valList)
				vs.assign(ps);
			rs = ps.executeQuery();

			int skiprows = 0;
			int maxrows = m_maxRows;
			if(! m_islimited) {
				skiprows = m_firstRow;
			}
			SQLRowSet	set = new SQLRowSet(m_columnList, m_table);

			List<SQLRow> res = new ArrayList<SQLRow>();
			while(rs.next()) {
				if(skiprows > 0) {
					skiprows--;
					continue;
				}

				SQLRow row = createRow(set, rs);
				res.add(row);
				if(res.size() >= maxrows)
					break;
			}
			set.init(res);
			return set;
		} finally {
			FileTool.closeAll(rs, ps);
		}
	}

	private SQLRow createRow(@Nonnull SQLRowSet set, @Nonnull ResultSet rs) throws Exception {
		int cols = set.getColumnList().size();
		Object[] values = new Object[cols];
		for(int i = 0; i < cols; i++) {
			DbColumn col = set.getColumn(i);
			values[i] = col.getValue(rs, i + 1);
		}
		return new SQLRow(set, values);
	}

	private SQLBuilder a(@Nonnull String s) {
		m_statement.append(s);
		return this;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Node visitor.										*/
	/*--------------------------------------------------------------*/

	@Override
	public void visitCriteria(QCriteria< ? > qc) throws Exception {
		QDbTable qt = (QDbTable) qc.getMetaTable();

		m_root = new PTableRef(qt.getTable(), "this_");
		m_tblMap.put(m_root.getAlias(), m_root);
		m_start = qc.getStart();
		m_limit = qc.getLimit();
		if(qc.getTimeout() < 0)
			m_timeout = 60;
		else if(qc.getTimeout() != 0)
			m_timeout = qc.getTimeout();

		super.visitCriteria(qc);

		/*
		 * Generate SQL. Most of this is disgusting and should be replaced by a SN* tree passed to a database-dependent handler.
		 */
		StringBuilder sb = new StringBuilder(256);
		boolean limiting = qc.getLimit() > 0 || qc.getStart() > 0;
		if(m_oracle && limiting) {
			sb.append("select * from (");
		}

		//-- 1. Sort all lobs and clobs to the end.
		List<DbColumn> coll = new ArrayList<DbColumn>();
		for(DbColumn c : m_table.getColumnList()) {
			if(c.isLob())
				continue;
			coll.add(c);
		}
		for(DbColumn c : m_table.getColumnList()) {
			if(!c.isLob())
				continue;
			coll.add(c);
		}
		m_columnList = coll;

		//-- 2. Create a select xxx statement
		sb.append("select ");
		int ix = 0;
		for(DbColumn c: coll) {
			if(ix++ > 0)
				sb.append(",");
			r().addSelectColumnAs(sb, c.getName(), "c" + ix);
		}
		sb.append(" from ");

		sb.append(m_root.getTable().getSchema().getName()).append(".").append(m_root.getTable().getName());
		sb.append(" ");
		sb.append(m_root.getAlias());

		if(m_where.length() > 0) {
			sb.append(" where ");
			sb.append(m_where);
		}
		if(m_order != null) {
			sb.append(" order by ");
			sb.append(m_order);
		}
		if(m_oracle && limiting) {
			sb.append(") where");
			if(qc.getStart() > 0)
				sb.append(" rownum >= ").append(qc.getStart());
			if(qc.getLimit() > 0) {
				if(qc.getStart() > 0)
					sb.append(" and");
				sb.append(" rownum <= ").append(qc.getLimit());
			}
		}
		m_sql = sb.toString();
		System.out.println("SQL=" + m_sql);
	}

	@Override
	public void visitSelection(QSelection< ? > s) throws Exception {
		throw new IllegalStateException("Not implemented yet");
	}

	private String getColumnRef(PTableRef ref, String name) {
		return ref.getAlias() + "." + name;
	}

	private void addSelectColumn(PTableRef root, String name) {
		if(m_fields.length() != 0)
			m_fields.append(",");
		m_fields.append(getColumnRef(root, name));
		m_nextFieldIndex++;
	}

	private void generatePropertyGetter(PTableRef root, JdbcPropertyMeta pm) throws Exception {
		for(String col : pm.getColumnNames())
			addSelectColumn(root, col);
	}

	public String getSQL() throws Exception {
		return m_sql;
	}

	public List<IQValueSetter> getValList() {
		return m_valList;
	}

	@Override
	public void visitOrder(@Nonnull QOrder o) throws Exception {
		if(m_order == null)
			m_order = new StringBuilder();
		DbColumn pm = resolveProperty(o.getProperty());
		if(m_order.length() > 0)
			m_order.append(",");
		m_order.append(getColumnRef(m_root, pm.getName()));
		switch(o.getDirection()){
			default:
				throw new IllegalStateException("Bad order: " + o.getDirection());
			case ASC:
				m_order.append(" asc");
				break;
			case DESC:
				m_order.append(" desc");
				break;
		}
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
		if(n.getChildren().size() == 0)
			return;
		if(n.getChildren().size() == 1) { // Should not really happen
			n.getChildren().get(0).visit(this);
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
		//-- Lookup the property name. For now it cannot be dotted
		DbColumn pm = resolveProperty(n.getProperty());

		int oldprec = m_curPrec;
		m_curPrec = getOperationPrecedence(n.getOperation());
		if(oldprec > m_curPrec)
			appendWhere("(");

		if(n.getOperation() == QOperation.ILIKE && m_oracle) {
			appendWhere("upper(");
			appendWhere(getColumnRef(m_root, pm.getName()));
			appendWhere(") like upper(");

			if(n.getExpr() instanceof QLiteral) {
				appendLikeValueSetter(pm, (QLiteral) n.getExpr());
			} else
				throw new QQuerySyntaxException("Unexpected argument to " + n + ": " + n.getExpr());
			appendWhere(")");
		} else if(n.getOperation() == QOperation.ILIKE || n.getOperation() == QOperation.LIKE) {
			appendWhere(getColumnRef(m_root, pm.getName()));
			appendOperation(n.getOperation());

			if(n.getExpr() instanceof QLiteral) {
				appendLikeValueSetter(pm, (QLiteral) n.getExpr());
			} else
				throw new QQuerySyntaxException("Unexpected argument to " + n + ": " + n.getExpr());
		} else {
			appendWhere(getColumnRef(m_root, pm.getName()));
			appendOperation(n.getOperation());

			if(n.getExpr() instanceof QLiteral) {
				appendValueSetter(pm, (QLiteral) n.getExpr());
			} else
				throw new QQuerySyntaxException("Unexpected argument to " + n + ": " + n.getExpr());
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
	private void appendValueSetter(@Nonnull DbColumn pm, @Nonnull QLiteral expr) {
		appendWhere("?");
		int index = m_nextWhereIndex++;
		ValSetter vs = new ValSetter(index, expr.getValue(), pm);
		m_valList.add(vs);
	}

	/**
	 * Append a value setter for a like operation, where the parameter is string by definition.
	 * @param pm
	 * @param expr
	 */
	private void appendLikeValueSetter(@Nonnull DbColumn pm, @Nonnull QLiteral expr) {
		if(!(expr.getValue() instanceof String))
			throw new QQuerySyntaxException("Invalid value type " + expr.getValue() + " for LIKE operation - expecting string.");
		appendWhere("?");
		int index = m_nextWhereIndex++;
		ValSetter vs = new ValSetter(index, expr.getValue(), pm);
//		LikeSetter vs = new LikeSetter(index, (String) expr.getValue(), pm);
		m_valList.add(vs);
	}

	private DbColumn resolveProperty(String pname) throws Exception {
		if(pname.indexOf('.') != -1) {
			String[] segs = pname.split("\\.");
			DbTable currclz = m_table;
			DbColumn selpm = null;
			int i = 0;
			for(;;) {
				String name = segs[i++];
				selpm = currclz.findColumn(name);
				if(selpm == null)
					throw new QQuerySyntaxException("Column '" + name + "' not found in table=" + currclz.getName() + " in property path '" + pname + "'");

				if(i >= segs.length) // Done?
					return selpm;

				//-- There is another property. The current property MUST refer to a compound.
				throw new QQuerySyntaxException("Property '" + name + "' in table=" + currclz.getName() + " in property path '" + pname + "' is not a compound");
//				currclz = JdbcMetaManager.getMeta(selpm.getActualClass());
			}
		}

		//-- Lookup,
		DbColumn pm = m_table.findColumn(pname);
		if(pm == null)
			throw new IllegalStateException(m_table.getName() + "." + pname + ": unknown column");
		return pm;
	}

	@Override
	public void visitUnaryProperty(QUnaryProperty n) throws Exception {
		int oldprec = m_curPrec;
		m_curPrec = getOperationPrecedence(n.getOperation());
		if(oldprec > m_curPrec)
			appendWhere("(");

		if(n.getOperation() == QOperation.ISNOTNULL || n.getOperation() == QOperation.ISNULL) {
			DbColumn pm = resolveProperty(n.getProperty());
			appendWhere(getColumnRef(m_root, pm.getName()));
			appendOperation(n.getOperation());
		} else {
			appendOperation(n.getOperation());
			appendWhere("(");
			DbColumn pm = resolveProperty(n.getProperty());
			appendWhere(getColumnRef(m_root, pm.getName()));
			appendWhere(")");
		}

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
		DbColumn pm = resolveProperty(n.getProp());
		appendWhere(getColumnRef(m_root, pm.getName()));
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
			case NOT:
				return "not";
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
			case NOT:
				return 25;
				/*case IN: */
			case BETWEEN:
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

	@Override
	public void visitUnaryNode(final QUnaryNode n) throws Exception {
		switch(n.getOperation()){
			default:
				throw new IllegalStateException("Unsupported UNARY operation: " + n.getOperation());
			case SQL:
				if(n.getNode() instanceof QLiteral) {
					QLiteral l = (QLiteral) n.getNode();
					appendWhere((String) l.getValue());
					return;
				}
				break;
			case NOT:
				if(n.getNode() == null) {
					return;
				}
				appendOperation(n.getOperation());
				int oldprec = m_curPrec;
				m_curPrec = getOperationPrecedence(n.getOperation());

				if(oldprec > m_curPrec) {
					appendWhere("(");
				}

				n.getNode().visit(this);

				if(oldprec > m_curPrec) {
					appendWhere(")");
				}

				m_curPrec = oldprec;
				return;
		}
		throw new IllegalStateException("Unsupported UNARY operation: " + n.getOperation());
	}


	/**
	 * Set a value.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Feb 20, 2013
	 */
	final class ValSetter implements IQValueSetter {
		final private int m_index;

		final private Object m_value;

		final private DbColumn m_column;

		public ValSetter(int index, Object value, DbColumn pm) {
			m_index = index;
			m_value = value;
			m_column = pm;
		}

		@Nonnull
		public DbColumn getColumn() {
			return m_column;
		}

		public int getIndex() {
			return m_index;
		}

		@Nullable
		public Object getValue() {
			return m_value;
		}

		/**
		 * @see to.etc.webapp.qsql.IQValueSetter#assign(java.sql.PreparedStatement)
		 */
		@Override
		public void assign(PreparedStatement ps) throws Exception {
			String s = (String) m_value;
			try {
				if(null == s) {
					ps.setNull(m_index, m_column.getSqlType());
					return;
				}

				//-- Convert the string to the actual/close to actual data type.
				s = s.trim();
				switch(m_column.getType().getSqlType()){
					default:
						ps.setString(m_index, s);
						break;

					case Types.DECIMAL:
					case Types.NUMERIC:
					case Types.BIGINT:
						BigDecimal bd = new BigDecimal(s);
						ps.setBigDecimal(m_index, bd);
						break;

					case Types.BIT:
					case Types.INTEGER:
					case Types.SMALLINT:
					case Types.TINYINT:
						long value = Long.valueOf(s);
						ps.setLong(m_index, value);
						break;

					case Types.BOOLEAN:
						s = s.toLowerCase();
						if(s.equals("t") || s.equals("true") || s.equals("y") || s.equals("1"))
							ps.setBoolean(m_index, true);
						else if(s.equals("f") || s.equals("false") || s.equals("n") || s.equals("0"))
							ps.setBoolean(m_index, false);
						else
							throw new RuntimeException();
						break;

					case Types.DATE:
					case Types.TIMESTAMP:
						DateFormat df = new SimpleDateFormat("yyyymmdd HH:mm:ss");
						df.setLenient(true);
						Date dt = df.parse(s);
						ps.setTimestamp(m_index, new Timestamp(dt.getTime()));
						break;

					case Types.DOUBLE:
						ps.setDouble(m_index, Double.parseDouble(s));
						break;

					case Types.FLOAT:
						ps.setDouble(m_index, Float.parseFloat(s));
						break;

					case Types.REAL:
						ps.setDouble(m_index, Double.parseDouble(s));
						break;
				}
			} catch(Exception x) {
				throw new SQLException("Invalid value '" + s + "' for column " + m_column.getName() + " (" + m_column.getTypeString() + ")");
			}
		}
	}


}
