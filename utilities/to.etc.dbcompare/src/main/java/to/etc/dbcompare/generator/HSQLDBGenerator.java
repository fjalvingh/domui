package to.etc.dbcompare.generator;

import to.etc.dbutil.schema.ColumnType;
import to.etc.dbutil.schema.DbColumn;
import to.etc.dbutil.schema.DbPrimaryKey;
import to.etc.dbutil.schema.DbSequence;

import java.util.List;

/**
 * Generator for the POSTGRESQL database
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 6, 2007
 */
public class HSQLDBGenerator extends AbstractGenerator {

	static private final TypeMapping NUMBER = new TypeMapping() {
		@Override
		public void renderType(StringBuilder a, DbColumn c) {
			int p = c.getPrecision();
			int s = c.getScale();
			if(s != 0) {
				a.append("numeric(" + p + "," + s + ")");
				return;
			}
			if(p <= 9) {
				a.append("int");
				return;
			}
			a.append("numeric(" + p + "," + s + ")");
		}
	};

	static private final TypeMapping VARCHAR = new TypeMapping() {
		@Override
		public void renderType(StringBuilder a, DbColumn c) {
			int p = c.getPrecision();
			if(p <= 4000) {
				a.append("varchar(" + p + ")");
				return;
			}
			a.append("text");
		}
	};

	public HSQLDBGenerator() {
		registerMapping(ColumnType.NUMBER, NUMBER);
		registerMapping(ColumnType.VARCHAR, VARCHAR);
		registerMapping(ColumnType.BOOLEAN, (a, c) -> a.append("boolean"));
		registerMapping(ColumnType.INTEGER, (a, c) -> a.append("integer"));
		registerMapping(ColumnType.BIGINT, (a, c) -> a.append("bigint"));
		//registerMapping(ColumnType.BLOB, (a, c) -> a.append("oid"));
		registerMapping(ColumnType.JSON, (a, c) -> a.append("text"));

	}

	public void addSequence(List<String> out, DbSequence sq, GenSequenceType type) {
		StringBuilder sb = new StringBuilder();
		sb.append("create sequence ");
		renderQualifiedName(sb, sq.getSchema(), sq.getName());

		if(sq.getIncrement() != Long.MIN_VALUE) {
			sb.append(" increment by ").append(sq.getIncrement());
		}

		//-- has trouble with minvalue
		//if(sq.getMinValue() != Long.MIN_VALUE) {
		//	sb.append(" minvalue ").append(sq.getMinValue());
		//}
		//if(sq.getMaxValue() != Long.MIN_VALUE) {
		//	sb.append(" maxvalue ").append(sq.getMaxValue());
		//}
		if(sq.getLastValue() != Long.MIN_VALUE && type == GenSequenceType.useCurrent) {
			long val = sq.getLastValue();
			if(sq.getMinValue() != Long.MIN_VALUE && val >= sq.getMinValue()) {
				sb.append(" start with ").append(sq.getLastValue() + 20);
			}
		}
		out.add(sb.toString());
	}

	@Override
	protected void renderPkColumn(StringBuilder sb, DbPrimaryKey pk) {
		DbColumn ignorec = pk.getColumnList().get(0);
		sb.append("\t");
		renderCreateColumnNameAndType(sb, ignorec);

		//String name = pk.getName();
		//if(null != name) {
		//	sb.append(" constraint ");
		//	renderName(sb, name);
		//}
		sb.append(" primary key");
		renderDefault(sb, ignorec);
		sb.append("\n");

	}

	@Override protected void renderColumnDefault(StringBuilder sb, DbColumn c, String dflt) {
		int pos = dflt.indexOf("::");
		if(pos > 0)
			dflt = dflt.substring(0, pos).trim();

		sb.append(" default ");
		sb.append(dflt);
	}

	@Override
	public String getIdent() {
		return "HSQLDB 2.4.x generator";
	}

}
