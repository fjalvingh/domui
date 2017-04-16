package to.etc.dbcompare.generator;

import to.etc.dbcompare.db.*;

public class SimpleMapping implements TypeMapping {
	private String	m_name;

	public SimpleMapping(String n) {
		m_name = n;
	}

	public String getTypeName(Column c) {
		return m_name;
	}

	public void renderType(Appendable sb, Column c) throws Exception {
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
