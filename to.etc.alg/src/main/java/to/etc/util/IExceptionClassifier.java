package to.etc.util;

import javax.annotation.*;

import to.etc.util.ExceptionClassifier.Severity;

/**
 * Throwables can be classified as severe or not severe
 *
 *
 * This can be implemented by classes for determining whether custom exceptions
 * are severe or not.
 *
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since Jun 20, 2014
 */
public interface IExceptionClassifier {
	@Nonnull Severity getExceptionSeverity(@Nonnull Throwable throwable);
}
