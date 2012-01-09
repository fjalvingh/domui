package to.etc.el.node;

import to.etc.util.*;

public class NdFloatLit extends NdLiteral {
	public NdFloatLit(double val) {
		super(WrapperCache.getDouble(val));
	}
}
