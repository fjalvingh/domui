package to.etc.webapp.query;

import javax.annotation.*;

public interface IQDataContextSource {
	/**
	 * Get the current Session to use for querying.
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	public QDataContext getDataContext() throws Exception;
}
