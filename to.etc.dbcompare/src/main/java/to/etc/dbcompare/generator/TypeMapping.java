package to.etc.dbcompare.generator;

import to.etc.dbcompare.db.*;

public interface TypeMapping {
	public void renderType(Appendable a, Column c) throws Exception;
}
