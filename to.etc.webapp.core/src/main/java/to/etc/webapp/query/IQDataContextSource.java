package to.etc.webapp.query;

import org.eclipse.jdt.annotation.NonNull;

public interface IQDataContextSource {
	/**
	 * Get the current Session to use for querying.
	 * @return
	 * @throws Exception
	 */
	@NonNull QDataContext getDataContext() throws Exception;
}
