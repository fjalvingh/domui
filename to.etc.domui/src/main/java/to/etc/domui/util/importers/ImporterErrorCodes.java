package to.etc.domui.util.importers;

import to.etc.webapp.nls.BundleRef;
import to.etc.webapp.nls.IBundleCode;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12-12-17.
 */
public enum ImporterErrorCodes implements IBundleCode {
	csvExpectingSeparator
	, csvEofInString
	, csvNewlineInsideQuote
	;

	static private final BundleRef BUNDLE = BundleRef.create(ImporterErrorCodes.class, "messages");

	@Override public BundleRef getBundle() {
		return BUNDLE;
	}
}
