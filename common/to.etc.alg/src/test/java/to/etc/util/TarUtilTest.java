package to.etc.util;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@Ignore("Keep as sandbox")
public class TarUtilTest {

	@Test
	public void testCreateTarArchive() throws Exception {
		File rootLocation = new File("/home/vmijic/Downloads/NT15_BZK_DVI_20210120.b Berichten/berichten");
		String tarName = rootLocation.getAbsolutePath().concat(".tar");
		try(
			FileOutputStream fos = new FileOutputStream(tarName);
			TarArchiveOutputStream tarOs = new TarArchiveOutputStream(fos)
		) {
			TarUtil.createTarArchive(tarOs, rootLocation, "NT15_BZK_dVi_20210120.b - testsuite");
		}
		File tarFile = new File(tarName);
		System.out.println("Completed, created " + tarFile.getAbsolutePath() + ", of size: " + StringTool.strSize(tarFile.length()));
	}

	@Test
	public void extractTarArchive() throws IOException {
		File tarFile = new File("/home/vmijic/Downloads/NT15_BZK_DVI_20210120.b Berichten/berichten.tar");
		File extractLocation = new File("/home/vmijic/Downloads/NT15_BZK_DVI_20210120.b Berichten/berichten1/");
		extractLocation.mkdirs();
		try(
			FileInputStream fis = new FileInputStream(tarFile);
			TarArchiveInputStream tarIs = new TarArchiveInputStream(fis)
		) {
			TarUtil.extractTarArchive(tarIs, extractLocation);
		}
		System.out.println("Completed!");
	}
}
