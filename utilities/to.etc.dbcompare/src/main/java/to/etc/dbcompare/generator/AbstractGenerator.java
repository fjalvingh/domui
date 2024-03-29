package to.etc.dbcompare.generator;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.dbutil.schema.ColumnType;
import to.etc.dbutil.schema.DbColumn;
import to.etc.dbutil.schema.DbIndex;
import to.etc.dbutil.schema.DbPrimaryKey;
import to.etc.dbutil.schema.DbRelation;
import to.etc.dbutil.schema.DbRelation.RelationUpdateAction;
import to.etc.dbutil.schema.DbSchema;
import to.etc.dbutil.schema.DbSequence;
import to.etc.dbutil.schema.DbTable;
import to.etc.dbutil.schema.DbView;
import to.etc.dbutil.schema.FieldPair;
import to.etc.dbutil.schema.IndexColumn;
import to.etc.dbutil.schema.Package;
import to.etc.dbutil.schema.Trigger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static to.etc.dbcompare.AbstractSchemaComparator.csCOMMENT;
import static to.etc.dbcompare.AbstractSchemaComparator.csNULLABLE;
import static to.etc.dbcompare.AbstractSchemaComparator.csPLATFORMTYPE;
import static to.etc.dbcompare.AbstractSchemaComparator.csPRECISION;
import static to.etc.dbcompare.AbstractSchemaComparator.csSCALE;
import static to.etc.dbcompare.AbstractSchemaComparator.csSQLTYPE;

/**
 * Abstract thingy which is able to generate the appropriate SQL for schema creation.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 6, 2007
 */
abstract public class AbstractGenerator {
	protected boolean m_appendSchemaNames = true;

	private Set<String> m_reservedWordMap = new HashSet<>();

	abstract public String getIdent();

	private Map<ColumnType, TypeMapping> m_mapmap = new HashMap<ColumnType, TypeMapping>();

	public AbstractGenerator() {
		registerReservedWords("from", "select", "insert", "update", "merge", "procedure", "function"
			, "where", "join", "inner", "outer", "left", "right", "cross", "natural", "sum", "avg"
			, "union"
		);
	}

	protected void registerMapping(ColumnType t, TypeMapping m) {
		m_mapmap.put(t, m);
	}

	protected void registerSimpleMapping(ColumnType t, String name) {
		m_mapmap.put(t, new SimpleMapping(name));
	}

	protected void registerReservedWords(String... words) {
		for(String word : words) {
			m_reservedWordMap.add(word.toLowerCase());
		}
	}

	public boolean isQuotingNeeded(String name) {
		if(m_reservedWordMap.contains(name.toLowerCase()))
			return true;

		for(int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if(! Character.isLetterOrDigit(c) && c != '_' && c != '$')
				return true;
		}
		return ! (name.toLowerCase().equals(name) || name.toUpperCase().equals(name));
	}


	/**
	 * Basic name renderer. Renders the name literally except when it
	 * contains lowercase or bad chars.
	 */
	public void renderName(StringBuilder a, String name, boolean forceQuote) {
		if(! forceQuote && !isQuotingNeeded(name)) {
			a.append(name);
			return;
		}
		a.append('"');
		a.append(name);
		a.append('"');
	}

	protected void renderQualifiedName(StringBuilder sb, DbSchema schema, String name, boolean forceQuote) {
		if(m_appendSchemaNames && schema != null) {
			renderName(sb, schema.getName(), schema.isForceQuote());
			sb.append('.');
		}
		renderName(sb, name, forceQuote);
	}

	public void renderFieldName(StringBuilder a, String name) {
		a.append(name);
	}

	public String quoted(String txt) {
		StringBuilder sb = new StringBuilder(txt.length() + 10);
		sb.append("'");
		for(int i = 0, e = txt.length(); i < e; i++) {
			char c = txt.charAt(i);
			if(c == '\'')
				sb.append(c);
			sb.append(c);
		}
		sb.append("'");
		return sb.toString();
	}

	public String getStatementDelimiter() {
		return ";";
	}

	final public String renderColumnType(DbColumn c, boolean rest) {
		StringBuilder sb = new StringBuilder();
		renderColumnType(sb, c, rest);
		return sb.toString();
	}

	static private final TypeMapping BASE = (sb, c) -> {
		ColumnType ct = c.getType();
		sb.append(ct.getName());

		//if(c.getPlatformTypeName() != null)
		//	sb.append(c.getPlatformTypeName());
		//else {
		//	sb.append(ct.getName());
		//}
		if(ct.isPrecision() && c.getPrecision() >= 0) {
			sb.append("(");
			sb.append(Integer.toString(c.getPrecision()));
			if(ct.isScale() && c.getScale() >= 0) {
				sb.append(',');
				sb.append(Integer.toString(c.getScale()));
			}
			sb.append(')');
		}
	};

	public TypeMapping getTypeMapping(DbColumn c) {
		TypeMapping m = m_mapmap.get(c.getType());
		return m == null ? BASE : m;
	}

	public void renderColumnType(StringBuilder sb, DbColumn c, boolean rest) {
		TypeMapping m = getTypeMapping(c);
		m.renderType(sb, c);
		if(!c.isNullable() && rest)
			sb.append(" not null");
	}

	public void renderDefault(StringBuilder sb, DbColumn c) {
		DbSequence usedSequence = c.getUsedSequence();
		String dflt = c.getDefault();
		if(null != usedSequence) {
			renderDefaultSequence(sb, c, usedSequence);
		} else if(null != dflt) {
			renderColumnDefault(sb, c, dflt);
		}
	}

	protected void renderColumnDefault(StringBuilder sb, DbColumn c, String dflt) {
		sb.append(" default ");
		sb.append(dflt);
	}

	public void renderDefaultSequence(StringBuilder sb, @NonNull DbColumn c, @NonNull DbSequence usedSequence) {
		sb.append(" default ");
		sb.append("nextval('");
		renderQualifiedName(sb, usedSequence.getSchema(), usedSequence.getName(), false);
		sb.append("')");
	}

	public enum GenSequenceType {
		/** Generate the sequence with its initial value */
		useInitial,

		/** Generate the sequence with its current value */
		useCurrent
	}

	public void addSequence(List<String> out, DbSequence sq, GenSequenceType type) {
		StringBuilder sb = new StringBuilder();
		sb.append("create sequence ");
		renderQualifiedName(sb, sq.getSchema(), sq.getName(), sq.isQuoteName());

		if(sq.getIncrement() != Long.MIN_VALUE) {
			sb.append(" increment by ").append(sq.getIncrement());
		}
		if(sq.getMinValue() != Long.MIN_VALUE) {
			sb.append(" minvalue ").append(sq.getMinValue());
		}
		if(sq.getMaxValue() != Long.MIN_VALUE) {
			sb.append(" maxvalue ").append(sq.getMaxValue());
		}
		if(sq.getLastValue() != Long.MIN_VALUE && type == GenSequenceType.useCurrent) {
			long val = sq.getLastValue();
			if(sq.getMinValue() != Long.MIN_VALUE && val >= sq.getMinValue()) {
				sb.append(" start with ").append(sq.getLastValue() + 10);
			}
		}
		if(sq.getCacheSize() != Long.MIN_VALUE) {
			sb.append(" cache ").append(sq.getCacheSize());
		}
		out.add(sb.toString());
	}

	public void renderAddColumn(StringBuilder sb, DbTable dt, DbColumn sc) {
		sb.append("alter table ");
		renderTableName(sb, dt);
		sb.append("\n\tadd ");
		renderName(sb, sc.getName(), sc.isQuoteName());
		sb.append(' ');
		renderColumnType(sb, sc, true);
		renderDefault(sb, sc);
		sb.append(getStatementDelimiter() + "\n");
	}

	private void renderTableName(StringBuilder sb, DbTable dt) {
		renderQualifiedName(sb, dt.getSchema(), dt.getName(), dt.isQuoteName());
	}

	public void renderColumnDrop(List<String> l, DbTable dt, DbColumn dc) {
		//-- Create "drop"
		l.add("alter table " + dt.getName() + "\n\tdrop column " + dc.getName() + getStatementDelimiter() + "\n");
	}

	public void renderColumnComment(List<String> sl, DbColumn sc) {
		if(sc.getComment() != null && !sc.getComment().isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("comment on column ");
			renderQualifiedName(sb, sc.getTable().getSchema(), sc.getTable().getName(), sc.getTable().isQuoteName());
			sb.append('.');
			renderName(sb, sc.getName(), sc.isQuoteName());
			sb.append(" is ");
			sb.append(quoted(sc.getComment()));
			sl.add(sb.toString());
		}
	}

	public void renderTableComment(List<String> sl, DbTable sc) {
		String comments = sc.getComments();
		if(comments != null && !comments.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("comment on table ");
			renderQualifiedName(sb, sc.getSchema(), sc.getName(), sc.isQuoteName());
			sb.append(" is ");
			sb.append(quoted(comments));
			sb.append(";\n");
			sl.add(sb.toString());
		}
	}

	public void columnChanged(List<String> l, DbTable dt, DbColumn newc, DbColumn oldc, int flag) {
		StringBuilder sb = new StringBuilder();

		//-- What changes can I support?
		if(0 != (flag & (csPRECISION | csSCALE))) {
			//-- Same type BUT size has changed. Is nullability changed too?
			//String nu = null;
			if(0 != (flag & csNULLABLE)) {
				if(!newc.isNullable()) {
					System.out.println("TODO: Need not-null modification for " + dt.getName() + "." + oldc.getName());
					//nu = " not null";
				//} else {
				//	nu = " null";
				}
			}
			flag &= ~csNULLABLE;

			//-- Create an inline comment detailing the change
			sb.setLength(0);
			sb.append("-- Change from old type= ");
			renderColumnType(sb, oldc, true);
			l.add(sb.toString());

			sb.setLength(0);
			sb.append("alter table ");
			sb.append(dt.getName());
			sb.append("\n\tmodify ");
			sb.append(oldc.getName());
			sb.append(' ');
			renderColumnType(sb, newc, 0 != (flag & csNULLABLE));
			sb.append(getStatementDelimiter() + "\n");
			l.add(sb.toString());
		} else if(0 != (flag & csNULLABLE)) {
			//-- Nullability changed. If changed to "null" we accept,
			if(newc.isNullable()) {
				sb.setLength(0);
				sb.append("alter table ");
				sb.append(dt.getName());
				sb.append("\n\tmodify ");
				sb.append(oldc.getName());
				sb.append(" null;\n");
				l.add(sb.toString());
			} else {
				//-- Column has become not-null!! This can only be done when a default is known
				System.out.println("TODO: Need not-null modification for " + dt.getName() + "." + oldc.getName());

				//-- Naive update
				sb.setLength(0);
				sb.append("alter table ");
				sb.append(dt.getName());
				sb.append("\n\tmodify ");
				sb.append(oldc.getName());
				sb.append(" not null;\n");
				l.add(sb.toString());
			}
		}
		if(0 != (flag & (csPLATFORMTYPE | csSQLTYPE))) {
			System.out.println("TODO: Need column TYPE modification for " + dt.getName() + "." + oldc.getName() + " from " + renderColumnType(oldc, true) + " to " + renderColumnType(newc, true));
		}
		if(0 != (flag & csCOMMENT)) // Comment has changed?
			renderColumnComment(l, newc);
	}

	protected boolean isAllowEmbeddedPK() {
		return true;
	}

	public void addTable(List<String> l, DbTable st) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("create table ");
		renderQualifiedName(sb, st.getSchema(), st.getName(), st.isQuoteName());
		sb.append(" (\n");

		//-- Create the PK field 1st *if* the table has a single PK.
		boolean needcomma = false;
		List<DbColumn> columnList = new ArrayList<>(st.getColumnList());
		DbPrimaryKey pk = st.getPrimaryKey();
		boolean pkrendered = false;
		if(pk != null && pk.getColumnList().size() == 1 && isAllowEmbeddedPK()) {
			//-- Dump PK column,
			DbColumn ignorec = pk.getColumnList().get(0);
			columnList.remove(ignorec);				// Do not render it after
			renderPkColumn(sb, pk);
			needcomma = true;
			pkrendered = true;
		}

		//-- Render all other columns,
		needcomma = renderAllColumns(sb, needcomma, columnList);

		//-- If a compound PK is present add that,
		if(! pkrendered)
			renderCompoundPK(sb, needcomma, pk);

		sb.append(")");
		l.add(sb.toString());

		//-- Table comments,
		renderTableComment(l, st);
		renderColumnComments(l, st);
	}

	protected void renderPkColumn(StringBuilder sb, DbPrimaryKey pk) {
		DbColumn ignorec = pk.getColumnList().get(0);
		sb.append("\t");
		renderInTableColumn(sb, ignorec);
		String name = pk.getName();
		if(null != name) {
			sb.append(" constraint ");
			renderName(sb, name, false);
		}
		sb.append(" primary key\n");
	}

	private void renderColumnComments(List<String> l, DbTable st) {
		for(DbColumn sc : st.getColumnList()) {
			renderColumnComment(l, sc);
		}
	}

	protected void renderCompoundPK(StringBuilder sb, boolean needcomma, DbPrimaryKey pk) {
		if(pk == null) {
			return;
		}
		if(needcomma)
			sb.append(',');
		sb.append("\t");
		if(pk.getName() != null) {
			sb.append("constraint ");
			sb.append(pk.getName());
			sb.append(' ');
		}
		sb.append("primary key(");
		boolean fst = true;
		for(DbColumn c : pk.getColumnList()) {
			if(!fst)
				sb.append(',');
			else
				fst = false;
			sb.append(c.getName());
		}
		sb.append(")");
	}

	private boolean renderAllColumns(StringBuilder sb, boolean needcomma, List<DbColumn> columnList) {
		for(DbColumn c : columnList) {
			if(needcomma)
				sb.append(',');
			sb.append("\t");
			renderInTableColumn(sb, c);
			sb.append("\n");
			needcomma = true;
		}
		return needcomma;
	}

	protected void renderInTableColumn(StringBuilder sb, DbColumn c) {
		renderCreateColumnNameAndType(sb, c);
		renderDefault(sb, c);
	}

	protected void renderCreateColumnNameAndType(StringBuilder sb, DbColumn c) {
		renderName(sb, c.getName(), c.isQuoteName());
		sb.append(" ");
		renderColumnType(sb, c, true);
	}

	public void renderDropTable(List<String> l, DbTable dt) {
		StringBuilder sb = new StringBuilder();
		sb.append("drop table ");
		renderName(sb, dt.getName(), dt.isQuoteName());
		sb.append(" cascade constraints;\n");
		l.add(sb.toString());

	}

	public void renderCreatePK(List<String> l, DbPrimaryKey pk) {
		StringBuilder a = new StringBuilder();
		a.append("alter table ");
		renderTableName(a, pk.getTable());
		a.append("\n\tadd ");
		if(pk.getName() != null) {
			a.append("constraint ");
			a.append(pk.getName());
			a.append(' ');
		}
		a.append("primary key(");
		boolean comma = false;
		for(DbColumn c : pk.getColumnList()) {
			if(comma)
				a.append(',');
			else
				comma = true;
			renderFieldName(a, c.getName());
		}
		a.append(")" + getStatementDelimiter());
		l.add(a.toString());
	}

	public void renderDropPK(List<String> l, DbPrimaryKey pk) {
		StringBuilder a = new StringBuilder();
		a.append("alter table ");
		renderTableName(a, pk.getTable());
		a.append("\n\tdrop ");
		if(pk.getName() != null) {
			a.append("constraint ");
			a.append(pk.getName());
			a.append(';');
			return;
		}
		a.append("primary key" + getStatementDelimiter());
		l.add(a.toString());
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Relation delta.										*/
	/*--------------------------------------------------------------*/

	public void renderDropRelation(List<String> l, DbTable dt, DbRelation sr) {
		StringBuilder a = new StringBuilder(512);
		a.append("alter table ");
		renderTableName(a, dt);
		a.append("\n\tdrop constraint ");
		renderName(a, sr.getName(), false);
		l.add(a.toString());
	}

	public void renderAddRelation(List<String> l, DbTable dt, DbRelation dr) {
		StringBuilder a = new StringBuilder(512);
		a.append("alter table ");
		renderTableName(a, dt);
		a.append("\n\tadd constraint ");
		renderName(a, dr.getName(), false);
		a.append(" foreign key(");
		int i = 0;
		for(FieldPair p : dr.getPairList()) {
			if(i++ > 0)
				a.append(',');
			renderName(a, p.getChildColumn().getName(), p.getChildColumn().isQuoteName());
		}
		a.append(")\n\t\treferences ");
		renderTableName(a, dr.getParent());
		a.append("(");
		i = 0;
		for(FieldPair p : dr.getPairList()) {
			if(i++ > 0)
				a.append(',');
			renderName(a, p.getParentColumn().getName(), p.getParentColumn().isQuoteName());
		}
		a.append(")");

		renderUpdateRule(a, dr, dr.getUpdateAction());
		renderDeleteRule(a, dr, dr.getDeleteAction());
		l.add(a.toString());
	}

	private void renderUpdateRule(StringBuilder sb, DbRelation dr, RelationUpdateAction action) {
		if(action == RelationUpdateAction.None)
			return;

		sb.append(" on update ");
		renderUpdateAction(sb, action);
	}

	private void renderDeleteRule(StringBuilder sb, DbRelation dr, RelationUpdateAction action) {
		if(action == RelationUpdateAction.None)
			return;

		sb.append(" on delete ");
		renderUpdateAction(sb, action);
	}

	private void renderUpdateAction(StringBuilder sb, RelationUpdateAction action) {
		switch(action) {
			default:
				throw new IllegalStateException(action + "??");
			case Cascade:
				sb.append("cascade");
				break;

			case SetDefault:
				sb.append("set default");
				break;

			case SetNull:
				sb.append("set null");
				break;
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Views.												*/
	/*--------------------------------------------------------------*/

	public void renderCreateView(List<String> l, DbView v) {
		StringBuilder a = new StringBuilder();
		a.append("create or replace view ");
		a.append(v.getName());
		a.append(" as\n");
		a.append(v.getSql());
		a.append(";\n/");
		l.add(a.toString());
	}

	public void renderDropView(List<String> l, DbView v) {
		StringBuilder a = new StringBuilder();
		a.append("drop view ");
		a.append(v.getName());
		l.add(a.toString());
	}

	public void renderCreatePackageDefinition(List<String> l, Package p) {
		StringBuilder a = new StringBuilder();
		a.append("create or replace package ");
		a.append(p.getName());
		a.append(" is\n");
		a.append(p.getDefinition());
		a.append("\n/");
		l.add(a.toString());
	}

	public void renderCreatePackageBody(List<String> l, Package p) {
		StringBuilder a = new StringBuilder();
		a.append("create or replace package body ");
		a.append(p.getName());
		a.append(" is\n");
		a.append(p.getBody());
		a.append("\n/");
		l.add(a.toString());
	}

	public void renderDropPackage(List<String> l, Package p) {
		StringBuilder a = new StringBuilder();
		a.append("drop package body ");
		a.append(p.getName());
		l.add(a.toString());
		a.setLength(0);
		a.append("drop package ");
		a.append(p.getName());
		l.add(a.toString());
	}

	public void renderAddTrigger(List<String> l, Trigger t) {
		StringBuilder a = new StringBuilder();
		a.append("create or replace trigger ");
		a.append(t.getName());
		//    	a.append(" is\n");
		a.append(t.getCode());
		a.append("\n/");
		l.add(a.toString());
	}

	public void renderDropTrigger(List<String> l, Trigger t) {
		StringBuilder a = new StringBuilder();
		a.append("drop trigger ");
		a.append(t.getName());
		a.append(";\n/");
		l.add(a.toString());
	}

	public void renderDropIndex(List<String> l, DbIndex ix) {
		StringBuilder a = new StringBuilder();
		a.append("drop index ");
		a.append(ix.getName());
		a.append(";\n/");
		l.add(a.toString());
	}

	public void renderCreateIndex(List<String> l, DbIndex ix) {
		StringBuilder a = new StringBuilder();
		a.append("create ");
		if(ix.isUnique())
			a.append("unique ");
		a.append("index ");
		renderName(a, ix.getName(), false);
		a.append(" on ");
		renderTableName(a, ix.getTable());
		a.append('(');
		int i = 0;
		for(IndexColumn c : ix.getColumnList()) {
			if(i++ != 0)
				a.append(',');
			renderName(a, c.getColumn().getName(), c.getColumn().isQuoteName());
			if(c.isDescending())
				a.append(" desc");
		}
		a.append(");\n/");
		l.add(a.toString());
	}

	public boolean isAppendSchemaNames() {
		return m_appendSchemaNames;
	}

	public void setAppendSchemaNames(boolean appendSchemaNames) {
		m_appendSchemaNames = appendSchemaNames;
	}
}
