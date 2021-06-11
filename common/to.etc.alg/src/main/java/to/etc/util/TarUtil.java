package to.etc.util;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class TarUtil {

	private static final Logger LOG = LoggerFactory.getLogger(TarUtil.class);

	public static void createTarArchive(TarArchiveOutputStream tarOs, File rootLocation, String... entriesPaths) throws IOException {
		if(!rootLocation.exists() || !rootLocation.isDirectory()) {
			throw new IllegalArgumentException("rootLocation must exist and needs to directory: " + rootLocation.getAbsolutePath());
		}
		tarOs.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
		String rootLocationPath = rootLocation.getAbsolutePath();
		for(String entryPath : entriesPaths) {
			if(entryPath.startsWith("/")) {
				throw new IllegalArgumentException("entryPath can't start with /");
			}
			File entry = new File(rootLocation, entryPath);
			if(! entry.exists()) {
				LOG.debug("TarUtil, file not found for " + entryPath + ", relative to " + rootLocationPath);
				continue;
			}
			addFilesToTarGZ(rootLocation, rootLocationPath, entry, tarOs);
		}
	}

	private static void addFilesToTarGZ(File rootLocation, String rootLocationPath, File entry, TarArchiveOutputStream tarArchive) throws IOException {
		String entryName = entry.getAbsolutePath();

		if(entryName.startsWith(rootLocationPath)) {
			entryName = entryName.substring(rootLocationPath.length());
		}

		tarArchive.putArchiveEntry(new TarArchiveEntry(entry, entryName));
		if(entry.isFile()) {
			try(
				FileInputStream fis = new FileInputStream(entry);
				BufferedInputStream bis = new BufferedInputStream(fis);
			) {
				IOUtils.copy(bis, tarArchive);
				tarArchive.closeArchiveEntry();
			}
		} else if(entry.isDirectory()) {
			tarArchive.closeArchiveEntry();
			for(File f : entry.listFiles()) {
				addFilesToTarGZ(rootLocation, rootLocationPath, f, tarArchive);
			}
		}
	}

	public static void extractTarArchive(TarArchiveInputStream tarIs, File rootLocation) throws IOException {
		TarArchiveEntry entry;
		while((entry = tarIs.getNextTarEntry()) != null) {
			if(entry.isDirectory()) {
				continue;
			}
			final File file = new File(rootLocation, entry.getName());
			final File parent = file.getParentFile();
			if(! parent.exists()) {
				parent.mkdirs();
			}
			try(FileOutputStream fos = new FileOutputStream(file)) {
				IOUtils.copy(tarIs, fos);
			}
		}
	}
}

