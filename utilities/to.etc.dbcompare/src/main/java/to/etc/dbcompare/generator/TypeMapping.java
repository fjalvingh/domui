package to.etc.dbcompare.generator;

import to.etc.dbutil.schema.*;

public interface TypeMapping {
	void renderType(Appendable a, DbColumn c) throws Exception;
}
