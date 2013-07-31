package to.etc.launcher.collector;

import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import javax.annotation.*;

public class PackageTestArtefactsCollector extends ArtefactsCollector implements ITestArtefactsCollector {

	public PackageTestArtefactsCollector(@Nonnull URLClassLoader loader) {
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
		boolean containsTestClass = false;
		String packagePath = null;
		for(File file : location.listFiles()) {
			if(file.isDirectory()) {
				subLocations.add(file);
			} else if(!containsTestClass && file.getName().endsWith(".java")) {
				if(packagePath == null) {
					packagePath = assamblePackagePath(projectRoot, file);
				}
				if(handleFile(packagePath, file)) {
					containsTestClass = true;
				}
			}
		}
		if(containsTestClass) {
			res.add(packagePath.substring(0, packagePath.length() - 1));
		}
		for(File file : subLocations) {
			collectArtefacts(projectRoot, file, res);
		}
	}

	private boolean handleFile(@Nonnull String path, @Nonnull File file) throws ClassNotFoundException {
		URLClassLoader loader = getLoader();
		//"nl.itris.vp.webdriver.core." +

		Class loadedClass = loader.loadClass(path + file.getName().substring(0, file.getName().length() - 5));
		Method[] methods = loadedClass.getDeclaredMethods();
		for(Method method : methods) {
			Annotation[] annotations = method.getAnnotations();
			for(Annotation ann : annotations) {
				if(ann.toString().contains("@org.testng.annotations.Test")) {
					return true;
				}
			}
		}
		return false;
	}

}
