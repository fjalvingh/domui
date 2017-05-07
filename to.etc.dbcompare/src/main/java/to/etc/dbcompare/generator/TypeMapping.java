package to.etc.dbcompare.generator;

import to.etc.dbutil.schema.*;

public interface TypeMapping {
	public void renderType(Appendable a, DbColumn c) throws Exception;
}
