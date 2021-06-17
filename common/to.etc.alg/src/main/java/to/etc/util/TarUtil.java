package to.etc.util;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;

public class TarUtil {

	private static final Logger LOG = LoggerFactory.getLogger(TarUtil.class);

	public static void createTarArchive(TarArchiveOutputStream tarOs, File rootLocation, String... entriesPaths) throws IOException {
		if(!rootLocation.exists() || !rootLocation.isDirectory()) {
			throw new IllegalArgumentException("rootLocation must exist and needs to directory: " + rootLocation.getAbsolutePath());
		}
		boolean withFileAttributes = SystemUtils.IS_OS_UNIX;

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
			addFilesToTarGZ(withFileAttributes, rootLocationPath, entry, tarOs);
		}
	}

	private static void addFilesToTarGZ(boolean withFileAttributes, String rootLocationPath, File entry, TarArchiveOutputStream tarArchive) throws IOException {
		String entryName = entry.getAbsolutePath();

		if(entryName.startsWith(rootLocationPath)) {
			entryName = entryName.substring(rootLocationPath.length());
		}

		final TarArchiveEntry archiveEntry = new TarArchiveEntry(entry, entryName);
		if(withFileAttributes) {
			final PosixFileAttributes fileAttributes = Files.readAttributes(entry.toPath(), PosixFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
			archiveEntry.setUserName(fileAttributes.owner().getName());
			archiveEntry.setGroupName(fileAttributes.group().getName());
			archiveEntry.setMode(PosixFilePermissionUtil.getPosixPermissionsAsInt(fileAttributes.permissions()));
		}

		tarArchive.putArchiveEntry(archiveEntry);
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
				addFilesToTarGZ(withFileAttributes, rootLocationPath, f, tarArchive);
			}
		}
	}

	public static void extractTarArchive(TarArchiveInputStream tarIs, File rootLocation) throws IOException {
		TarArchiveEntry entry;
		boolean withFileAttributes = SystemUtils.IS_OS_UNIX;

		UserPrincipalLookupService service = withFileAttributes
			? FileSystems.getDefault().getUserPrincipalLookupService()
			: null;
		while((entry = tarIs.getNextTarEntry()) != null) {
			final File file = new File(rootLocation, entry.getName());
			final File parent = file.getParentFile();
			if(! parent.exists()) {
				parent.mkdirs();
			}
			if(! entry.isDirectory()) {
				try(FileOutputStream fos = new FileOutputStream(file)) {
					IOUtils.copy(tarIs, fos);
				}
			}else {
				file.mkdir();
			}
			if(withFileAttributes) {
				final Path path = file.toPath();
				String userName = entry.getUserName();
				if(null != userName) {
					UserPrincipal owner = service.lookupPrincipalByName(userName);
					Files.setOwner(path, owner);
				}

				String groupName = entry.getGroupName();
				if(null != groupName) {
					GroupPrincipal group = service.lookupPrincipalByGroupName(groupName);
					Files.getFileAttributeView(path, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS).setGroup(group);
				}

				final int mode = entry.getMode();
				if(mode > 0) {
					Files.setPosixFilePermissions(path, PosixFilePermissionUtil.posixFilePermissions(mode));
				}
			}
		}
	}
}

