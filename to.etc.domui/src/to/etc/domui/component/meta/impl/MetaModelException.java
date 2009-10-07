package to.etc.domui.component.meta.impl;

import to.etc.domui.trouble.*;
import to.etc.webapp.nls.*;

public class MetaModelException extends UIException {
	public MetaModelException(BundleRef bundle, String code, Object... parameters) {
		super(bundle, code, parameters);
	}

	public MetaModelException(String code, Object... parameters) {
		super(code, parameters);
	}

	public MetaModelException(Throwable t, BundleRef bundle, String code, Object... parameters) {
		super(t, bundle, code, parameters);
	}

	public MetaModelException(Throwable t, String code, Object... parameters) {
		super(t, code, parameters);
	}
}
