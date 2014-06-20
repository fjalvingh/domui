package to.etc.util;

import javax.annotation.*;

/**
 *
 *
 *
 * @author <a href="mailto:ben.schoen@itris.nl">Ben Schoen</a>
 * @since Jun 20, 2014
 */
public interface IExceptionClassifier<T extends Throwable> {
	public boolean isSevereException(@Nonnull T throwable);
}