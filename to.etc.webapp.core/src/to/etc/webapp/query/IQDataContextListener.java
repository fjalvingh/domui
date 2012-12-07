package to.etc.webapp.query;

import javax.annotation.*;

public interface IQDataContextListener {

	<T> void instanceSaved(@Nonnull T testDataObject) throws Exception;
}