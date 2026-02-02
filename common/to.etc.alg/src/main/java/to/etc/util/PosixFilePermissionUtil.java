package to.etc.util;

import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;

import static java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.GROUP_READ;
import static java.nio.file.attribute.PosixFilePermission.GROUP_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_READ;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;

public class PosixFilePermissionUtil {

	private static final int O400 = 256;
	private static final int O200 = 128;
	private static final int O100 = 64;
	private static final int O040 = 32;
	private static final int O020 = 16;
	private static final int O010 = 8;
	private static final int O004 = 4;
	private static final int O002 = 2;
	private static final int O001 = 1;

	public static int getPosixPermissionsAsInt(Set<PosixFilePermission> permissionSet) {
		int result = 0;
		if (permissionSet.contains(OWNER_READ)) {
			result = result | O400;//  w w w  . j a  v a 2 s. c  o  m
		}
		if (permissionSet.contains(OWNER_WRITE)) {
			result = result | O200;
		}
		if (permissionSet.contains(OWNER_EXECUTE)) {
			result = result | O100;
		}
		if (permissionSet.contains(GROUP_READ)) {
			result = result | O040;
		}
		if (permissionSet.contains(GROUP_WRITE)) {
			result = result | O020;
		}
		if (permissionSet.contains(GROUP_EXECUTE)) {
			result = result | O010;
		}
		if (permissionSet.contains(OTHERS_READ)) {
			result = result | O004;
		}
		if (permissionSet.contains(OTHERS_WRITE)) {
			result = result | O002;
		}
		if (permissionSet.contains(OTHERS_EXECUTE)) {
			result = result | O001;
		}
		return result;
	}

	private static final PosixFilePermission[] decodeMap = {
		OTHERS_EXECUTE, OTHERS_WRITE, OTHERS_READ, GROUP_EXECUTE, GROUP_WRITE, GROUP_READ, OWNER_EXECUTE,
		OWNER_WRITE, OWNER_READ
	};

	public static Set<PosixFilePermission> posixFilePermissions(int mode) {
		int mask = 1;
		Set<PosixFilePermission> perms = EnumSet.noneOf(PosixFilePermission.class);
		for (PosixFilePermission flag : decodeMap) {
			if (flag != null && (mask & mode) != 0) {
				perms.add(flag);
			}
			mask = mask << 1;
		}
		return perms;
	}
}
