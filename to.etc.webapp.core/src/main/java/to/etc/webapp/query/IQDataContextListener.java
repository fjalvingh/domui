package to.etc.webapp.query;

import javax.annotation.*;

/**
 * Listener to keep track of all data saved during a unittest.
 *
 * @author <a href="mailto:marc.mol@itris.nl">Marc Mol</a>
 * @since Dec 17, 2012
 */
public interface IQDataContextListener {

	/**
	 * For every instance saved during a unittest, this method is called.
	 * If the listener is added to the QDataContext.
	 *
	 * @param testDataObject
	 * @throws Exception
	 */
	<T> void instanceSaved(@Nonnull IIdentifyable<T> testDataObject) throws Exception;
}