package to.etc.webapp.query;

import to.etc.webapp.nls.BundleRef;
import to.etc.webapp.nls.IBundleCode;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-09-22.
 */
public enum QMessages implements IBundleCode {
	recordNotFound,
	recordNotFoundSimple,
	concurrentUpdate,
	duplicateKey,
	constraintViolation,
	queryTimeout,
	tooManyResults,
	;

	@Override
	public BundleRef getBundle() {
		return BUNDLE;
	}

	/**
	 * A reference to the global shared message bundle for DomUI messages.
	 */
	public static BundleRef BUNDLE = BundleRef.create(QMessages.class, "messages");
}
