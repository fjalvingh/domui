package to.etc.launcher.runner;

import javax.annotation.*;

/**
 * Defines proxy to {@link RunnablePair} provider.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Jul 31, 2013
 */
public interface IRunnablePairProvider {
	/**
	 * Provides next {@link RunnablePair} for multithreaded test runners consumption.
	 * @return
	 */
	@Nullable
	RunnablePair getNext();


}
