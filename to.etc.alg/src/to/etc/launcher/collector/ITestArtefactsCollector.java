package to.etc.launcher.collector;

import java.io.*;
import java.util.*;

import javax.annotation.*;

/**
 * Proxy for test artifacts collector.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Aug 1, 2013
 */
public interface ITestArtefactsCollector {

	/**
	 * Collects test artifacts within specified resource and its sub locations.
	 *
	 * @param location
	 * @param skipPackages
	 * @return
	 * @throws ClassNotFoundException
	 */
	@Nonnull
	List<String> collectArtefacts(@Nonnull File location) throws ClassNotFoundException;

	void setMatcher(@Nonnull IArtefactMatcher matcher);

}
