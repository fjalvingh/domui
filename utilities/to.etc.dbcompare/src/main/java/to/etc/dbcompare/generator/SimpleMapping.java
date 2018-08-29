package to.etc.dbcompare.generator;

import to.etc.dbutil.schema.*;

public class SimpleMapping implements TypeMapping {
	private String m_name;

	public SimpleMapping(String n) {
		m_name = n;
	}

	public String getTypeName(DbColumn c) {
		return m_name;
	}

	@Override
	public void renderType(Appendable sb, DbColumn c) throws Exception {
		sb.append(getTypeName(c));
		ColumnType ct = c.getType();
		if(ct.isPrecision()) {
			sb.append("(");
			sb.append(Integer.toString(c.getPrecision()));
			if(ct.isScale()) {
				sb.append(',');
				sb.append(Integer.toString(c.getScale()));
			}
			sb.append(')');
		}

	}

}
