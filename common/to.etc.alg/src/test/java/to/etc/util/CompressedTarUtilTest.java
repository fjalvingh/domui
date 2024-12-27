package to.etc.util;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@Ignore("Keep as sandbox")
public class CompressedTarUtilTest {

	@Test
	public void testCreateCompressedTarArchive() throws IOException {
		File rootLocation = new File("/home/vmijic/Downloads/NT15_BZK_DVI_20210120.b Berichten/berichten");
		String tarName = rootLocation.getAbsolutePath().concat(".tar.gz");
		try(
			FileOutputStream fos = new FileOutputStream(tarName);
		) {
			CompressedTarUtil.tar(fos, (uncompressed, compressed) -> System.out.println("compressed size so far: " + compressed), rootLocation, "NT15_BZK_dVi_20210120.b - testsuite");
		}
		File tarFile = new File(tarName);
		System.out.println("Completed, created " + tarFile.getAbsolutePath() + ", of size: " + StringTool.strSize(tarFile.length()));
	}

	@Test
	public void testExtractCompressedTarArchive() throws IOException {
		File targzFile = new File("/home/vmijic/Downloads/NT15_BZK_DVI_20210120.b Berichten/berichten1/berichten.tar.gz");
		File extractLocation = new File("/home/vmijic/Downloads/NT15_BZK_DVI_20210120.b Berichten/berichten2/");
		try(FileInputStream fis = new FileInputStream(targzFile)) {
			CompressedTarUtil.untar(extractLocation, fis, (uncompressed, compressed) -> System.out.println("size so far: " + uncompressed + " uncompressed"));
		}
		System.out.println("Completed!");
	}

}
