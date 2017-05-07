package to.etc.dbcompare.generator;

import to.etc.dbutil.schema.*;

/**
 * Generator for the POSTGRESQL database
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 6, 2007
 */
public class PostgresGenerator extends AbstractGenerator {

	static private final TypeMapping NUMBER = new TypeMapping() {
		@Override
		public void renderType(Appendable a, DbColumn c) throws Exception {
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
		public void renderType(Appendable a, DbColumn c) throws Exception {
			int p = c.getPrecision();
			if(p <= 4000) {
				a.append("varchar(" + p + ")");
				return;
			}
			a.append("text");
		}
	};

	public PostgresGenerator() {
		registerMapping(ColumnType.NUMBER, NUMBER);
		registerMapping(ColumnType.VARCHAR, VARCHAR);
	}


	@Override
	public String getIdent() {
		return "PostgreSQL 8.x Generator";
	}

}
