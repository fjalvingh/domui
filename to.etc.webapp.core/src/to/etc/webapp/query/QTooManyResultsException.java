package to.etc.webapp.query;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2/9/16.
 */
public class QTooManyResultsException extends QDbException {
	public QTooManyResultsException(QCriteriaQueryBase<?> base, int count) {
		super(BUNDLE, "toomanyresults", base.toString(), count);
	}
}
