package to.etc.launcher.collector;

import java.io.*;
import java.util.*;

import javax.annotation.*;

/**
 * Proxy for test artefacts collector.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Aug 1, 2013
 */
public interface ITestArtefactsCollector {

	/**
	 * Collects test artefacts within specified resource and its sub locations.
	 *
	 * @param location
	 * @return
	 * @throws ClassNotFoundException
	 */
	@Nonnull
	List<String> collectArtefacts(@Nonnull File location) throws ClassNotFoundException;

}
