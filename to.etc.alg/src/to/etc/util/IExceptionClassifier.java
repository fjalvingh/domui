package to.etc.util;

import javax.annotation.*;

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
	public boolean isSevereException(@Nonnull Throwable throwable);
}