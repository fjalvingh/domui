package to.etc.launcher.collector;

import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import javax.annotation.*;

/**
 * Util for collecting all test classes that can be executed by TestNG.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Aug 1, 2013
 */
public class ClassTestArtefactsCollector extends ArtefactsCollector implements ITestArtefactsCollector {

	public ClassTestArtefactsCollector(@Nonnull URLClassLoader loader) {
		super(loader);
	}

	@Override
	@Nonnull
	public List<String> collectArtefacts(@Nonnull File location) throws ClassNotFoundException {
		List<String> res = new ArrayList<String>();
		collectArtefacts(location, location, res);
		return res;
	}

	private void collectArtefacts(@Nonnull File projectRoot, @Nonnull File location, @Nonnull List<String> res) throws ClassNotFoundException {
		List<File> subLocations = new ArrayList<File>();
		String packagePath = null;
		for(File file : location.listFiles()) {
			if(file.isDirectory()) {
				subLocations.add(file);
			} else if(file.getName().endsWith(".java")) {
				if(packagePath == null) {
					packagePath = assamblePackagePath(projectRoot, file);
				}
				handleFile(packagePath, file, res);
			}
		}
		for(File file : subLocations) {
			collectArtefacts(projectRoot, file, res);
		}
	}

	private void handleFile(@Nonnull String packagePath, @Nonnull File file, @Nonnull List<String> res) throws ClassNotFoundException {
		URLClassLoader loader = getLoader();
		Class loadedClass = loader.loadClass(packagePath + file.getName().substring(0, file.getName().length() - 5));
		Method[] methods = loadedClass.getDeclaredMethods();
		for(Method method : methods) {
			Annotation[] annotations = method.getAnnotations();
			for(Annotation ann : annotations) {
				if(ann.toString().contains("@org.testng.annotations.Test")) {
					res.add(loadedClass.getCanonicalName());
					return;
				}
			}
		}
	}

}
