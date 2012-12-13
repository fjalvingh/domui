package to.etc.webapp.query;

import javax.annotation.*;

public interface IQDataContextListener {

	<T> void instanceSaved(@Nonnull IIdentifyable<T> testDataObject) throws Exception;
}