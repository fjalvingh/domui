package to.etc.dbcompare.generator;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.dbutil.schema.ColumnType;
import to.etc.dbutil.schema.DbColumn;
import to.etc.dbutil.schema.DbIndex;
import to.etc.dbutil.schema.DbPrimaryKey;
import to.etc.dbutil.schema.DbRelation;
import to.etc.dbutil.schema.DbSchema;
import to.etc.dbutil.schema.DbSequence;
import to.etc.dbutil.schema.DbTable;
import to.etc.dbutil.schema.DbView;
import to.etc.dbutil.schema.FieldPair;
import to.etc.dbutil.schema.IndexColumn;
import to.etc.dbutil.schema.Package;
import to.etc.dbutil.schema.Trigger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	abstract public String getIdent();

	private Map<ColumnType, TypeMapping> m_mapmap = new HashMap<ColumnType, TypeMapping>();

	protected void registerMapping(ColumnType t, TypeMapping m) {
		m_mapmap.put(t, m);
	}

	protected void registerSimpleMapping(ColumnType t, String name) {
		m_mapmap.put(t, new SimpleMapping(name));
	}

	public boolean isQuotingNeeded(String name) {
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
	public void renderName(Appendable a, String name) throws Exception {
		if(!isQuotingNeeded(name)) {
			a.append(name);
			return;
		}
		a.append('"');
		a.append(name);
		a.append('"');
	}

	protected void renderQualifiedName(Appendable sb, DbSchema schema, String name) throws Exception {
		if(m_appendSchemaNames && schema != null) {
			renderName(sb, schema.getName());
			sb.append('.');
		}
		renderName(sb, name);
	}

	public void renderFieldName(Appendable a, String name) throws Exception {
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

	final public String renderColumnType(DbColumn c, boolean rest) throws Exception {
		StringBuilder sb = new StringBuilder();
		renderColumnType(sb, c, rest);
		return sb.toString();
	}

	static private final TypeMapping BASE = new TypeMapping() {
		@Override
		public void renderType(Appendable sb, DbColumn c) throws Exception {
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
		}
	};

	public TypeMapping getTypeMapping(DbColumn c) {
		TypeMapping m = m_mapmap.get(c.getType());
		return m == null ? BASE : m;
	}

	public void renderColumnType(Appendable sb, DbColumn c, boolean rest) throws Exception {
		TypeMapping m = getTypeMapping(c);
		m.renderType(sb, c);
		if(!c.isNullable() && rest)
			sb.append(" not null");
	}

	public void renderDefault(Appendable sb, DbColumn c) throws Exception {
		DbSequence usedSequence = c.getUsedSequence();
		String dflt = c.getDefault();
		if(null != usedSequence) {
			renderDefaultSequence(sb, c, usedSequence);
		} else if(null != dflt) {
			renderColumnDefault(sb, c, dflt);
		}
	}

	private void renderColumnDefault(Appendable sb, DbColumn c, String dflt) throws IOException {
		sb.append(" default ");
		sb.append(dflt);
	}

	public void renderDefaultSequence(Appendable sb, @NonNull DbColumn c, @NonNull DbSequence usedSequence) throws Exception {
		sb.append(" default ");
		sb.append("nextval('");
		renderQualifiedName(sb, usedSequence.getSchema(), usedSequence.getName());
		sb.append("')");
	}

	public enum GenSequenceType {
		/** Generate the sequence with its initial value */
		useInitial,

		/** Generate the sequence with its current value */
		useCurrent
	}

	public void addSequence(List<String> out, DbSequence sq, GenSequenceType type) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("create sequence ");
		renderQualifiedName(sb, sq.getSchema(), sq.getName());

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
				sb.append(" start with ").append(sq.getLastValue());
			}
		}
		if(sq.getCacheSize() != Long.MIN_VALUE) {
			sb.append(" cache ").append(sq.getCacheSize());
		}
		out.add(sb.toString());
	}

	public void renderAddColumn(Appendable sb, DbTable dt, DbColumn sc) throws Exception {
		sb.append("alter table ");
		renderTableName(sb, dt);
		sb.append("\n\tadd ");
		sb.append(sc.getName());
		sb.append(' ');
		renderColumnType(sb, sc, true);
		renderDefault(sb, sc);
		sb.append(getStatementDelimiter() + "\n");
	}

	private void renderTableName(Appendable sb, DbTable dt) throws Exception {
		renderQualifiedName(sb, dt.getSchema(), dt.getName());
	}

	public void renderColumnDrop(List<String> l, DbTable dt, DbColumn dc) throws Exception {
		//-- Create "drop"
		l.add("alter table " + dt.getName() + "\n\tdrop column " + dc.getName() + getStatementDelimiter() + "\n");
	}

	public void renderColumnComment(List<String> sl, DbColumn sc) throws Exception {
		if(sc.getComment() != null && sc.getComment().length() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("comment on column ");
			renderQualifiedName(sb, sc.getTable().getSchema(), sc.getTable().getName());
			sb.append('.');
			renderName(sb, sc.getName());
			sb.append(" is ");
			sb.append(quoted(sc.getComment()));
			sl.add(sb.toString());
		}
	}

	public void renderTableComment(List<String> sl, DbTable sc) throws Exception {
		if(sc.getComments() != null && sc.getComments().length() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("comment on table ");
			renderQualifiedName(sb, sc.getSchema(), sc.getName());
			sb.append(" is ");
			sb.append(quoted(sc.getComments()));
			sb.append(";\n");
			sl.add(sb.toString());
		}
	}

	public void columnChanged(List<String> l, DbTable dt, DbColumn newc, DbColumn oldc, int flag) throws Exception {
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

	public void addTable(List<String> l, DbTable st) throws Exception {
		StringBuilder sb = new StringBuilder(512);
		sb.append("create table ");
		renderQualifiedName(sb, st.getSchema(), st.getName());
		sb.append(" (\n");

		//-- Create the PK field 1st *if* the table has a single PK.
		boolean needcomma = false;
		List<DbColumn> columnList = st.getColumnList();
		DbPrimaryKey pk = st.getPrimaryKey();
		if(pk != null && pk.getColumnList().size() == 1) {
			//-- Dump PK column,
			DbColumn ignorec = pk.getColumnList().get(0);
			columnList.remove(ignorec);
			sb.append("\t");
			renderCreateColumn(sb, ignorec);

			String name = pk.getName();
			if(null != name) {
				sb.append(" constraint ");
				renderName(sb, name);
			}
			sb.append(" primary key\n");
			needcomma = true;
		}

		//-- Render all other columns,
		needcomma = renderAllColumns(sb, needcomma, columnList);

		//-- If a compound PK is present add that,
		renderCompoundPK(sb, needcomma, pk);

		sb.append(")");
		l.add(sb.toString());

		//-- Table comments,
		renderTableComment(l, st);
		renderColumnComments(l, st);
	}

	private void renderColumnComments(List<String> l, DbTable st) throws Exception {
		for(DbColumn sc : st.getColumnList()) {
			renderColumnComment(l, sc);
		}
	}

	private void renderCompoundPK(StringBuilder sb, boolean needcomma, DbPrimaryKey pk) {
		if(pk != null && pk.getColumnList().size() > 1) {
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
			needcomma = true;
		}
	}

	private boolean renderAllColumns(StringBuilder sb, boolean needcomma, List<DbColumn> columnList) throws Exception {
		for(DbColumn c : columnList) {
			if(needcomma)
				sb.append(',');
			sb.append("\t");
			renderCreateColumn(sb, c);
			sb.append("\n");
			needcomma = true;
		}
		return needcomma;
	}

	private void renderCreateColumn(StringBuilder sb, DbColumn c) throws Exception {
		sb.append(c.getName());
		sb.append(" ");
		renderColumnType(sb, c, true);
		renderDefault(sb, c);
	}

	public void renderDropTable(List<String> l, DbTable dt) {
		StringBuilder sb = new StringBuilder();
		sb.append("drop table ");
		sb.append(dt.getName());
		sb.append(" cascade constraints;\n");
		l.add(sb.toString());

	}

	public void renderCreatePK(List<String> l, DbPrimaryKey pk) throws Exception {
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

	public void renderDropPK(List<String> l, DbPrimaryKey pk) throws Exception {
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

	public void renderDropRelation(List<String> l, DbTable dt, DbRelation sr) throws Exception {
		StringBuilder a = new StringBuilder(512);
		a.append("alter table ");
		renderTableName(a, dt);
		a.append("\n\tdrop constraint ");
		renderName(a, sr.getName());
		l.add(a.toString());
	}

	public void renderAddRelation(List<String> l, DbTable dt, DbRelation dr) throws Exception {
		StringBuilder a = new StringBuilder(512);
		a.append("alter table ");
		renderTableName(a, dt);
		a.append("\n\tadd constraint ");
		renderName(a, dr.getName());
		a.append(" foreign key(");
		int i = 0;
		for(FieldPair p : dr.getPairList()) {
			if(i++ > 0)
				a.append(',');
			renderName(a, p.getChildColumn().getName());
		}
		a.append(")\n\t\treferences ");
		renderTableName(a, dr.getParent());
		a.append("(");
		i = 0;
		for(FieldPair p : dr.getPairList()) {
			if(i++ > 0)
				a.append(',');
			renderName(a, p.getParentColumn().getName());
		}
		a.append(")");
		l.add(a.toString());
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Views.												*/
	/*--------------------------------------------------------------*/

	public void renderCreateView(List<String> l, DbView v) throws Exception {
		StringBuilder a = new StringBuilder();
		a.append("create or replace view ");
		a.append(v.getName());
		a.append(" as\n");
		a.append(v.getSql());
		a.append(";\n/");
		l.add(a.toString());
	}

	public void renderDropView(List<String> l, DbView v) throws Exception {
		StringBuilder a = new StringBuilder();
		a.append("drop view ");
		a.append(v.getName());
		l.add(a.toString());
	}

	public void renderCreatePackageDefinition(List<String> l, Package p) throws Exception {
		StringBuilder a = new StringBuilder();
		a.append("create or replace package ");
		a.append(p.getName());
		a.append(" is\n");
		a.append(p.getDefinition());
		a.append("\n/");
		l.add(a.toString());
	}

	public void renderCreatePackageBody(List<String> l, Package p) throws Exception {
		StringBuilder a = new StringBuilder();
		a.append("create or replace package body ");
		a.append(p.getName());
		a.append(" is\n");
		a.append(p.getBody());
		a.append("\n/");
		l.add(a.toString());
	}

	public void renderDropPackage(List<String> l, Package p) throws Exception {
		StringBuilder a = new StringBuilder();
		a.append("drop package body ");
		a.append(p.getName());
		l.add(a.toString());
		a.setLength(0);
		a.append("drop package ");
		a.append(p.getName());
		l.add(a.toString());
	}

	public void renderAddTrigger(List<String> l, Trigger t) throws Exception {
		StringBuilder a = new StringBuilder();
		a.append("create or replace trigger ");
		a.append(t.getName());
		//    	a.append(" is\n");
		a.append(t.getCode());
		a.append("\n/");
		l.add(a.toString());
	}

	public void renderDropTrigger(List<String> l, Trigger t) throws Exception {
		StringBuilder a = new StringBuilder();
		a.append("drop trigger ");
		a.append(t.getName());
		a.append(";\n/");
		l.add(a.toString());
	}

	public void renderDropIndex(List<String> l, DbIndex ix) throws Exception {
		StringBuilder a = new StringBuilder();
		a.append("drop index ");
		a.append(ix.getName());
		a.append(";\n/");
		l.add(a.toString());
	}

	public void renderCreateIndex(List<String> l, DbIndex ix) throws Exception {
		StringBuilder a = new StringBuilder();
		a.append("create ");
		if(ix.isUnique())
			a.append("unique ");
		a.append("index ");
		renderName(a, ix.getName());
		a.append(" on ");
		renderTableName(a, ix.getTable());
		a.append('(');
		int i = 0;
		for(IndexColumn c : ix.getColumnList()) {
			if(i++ != 0)
				a.append(',');
			renderName(a, c.getColumn().getName());
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
