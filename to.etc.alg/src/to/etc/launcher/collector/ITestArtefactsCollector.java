package to.etc.launcher.collector;

import java.io.*;
import java.util.*;

import javax.annotation.*;

public interface ITestArtefactsCollector {

	@Nonnull
	List<String> collectArtefacts(@Nonnull File location) throws ClassNotFoundException;

}
