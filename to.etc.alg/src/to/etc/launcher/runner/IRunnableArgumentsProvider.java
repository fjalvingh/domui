package to.etc.launcher.runner;

import java.io.*;

import javax.annotation.*;
import javax.resource.spi.IllegalStateException;

/**
 * Defines proxy for general testNg runner command arguments.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Jul 31, 2013
 */
public interface IRunnableArgumentsProvider {
	@Nonnull
	String getClassPath() throws IllegalStateException;

	@Nullable
	String getRemoteHub();

	@Nullable
	String getServerUrl();

	@Nullable
	String getUserName();

	@Nullable
	String getPassword();

	@Nullable
	String getUnitTestProperties();

	@Nonnull
	File getReportRoot() throws IllegalStateException;

}
