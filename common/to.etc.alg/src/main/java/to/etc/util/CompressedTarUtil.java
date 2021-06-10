package to.etc.util;

import org.anarres.parallelgzip.ParallelGZIPInputStream;
import org.anarres.parallelgzip.ParallelGZIPOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CompressedTarUtil {

	public static void createCompressedTarArchive(OutputStream os, File rootLocation, String... entryPaths) throws IOException {
		try(
			ParallelGZIPOutputStream pigzos = new ParallelGZIPOutputStream(os);
			TarArchiveOutputStream tarOs = new TarArchiveOutputStream(pigzos)
		) {
			TarUtil.createTarArchive(tarOs, rootLocation, entryPaths);
		}
	}

	public static void extractCompressedTarArchive(InputStream is, File extractLocation) throws IOException {
		//we have to wrap BufferedInputStream if needed, since downstream streams work optimal with BufferedInputStream
		if(is instanceof BufferedInputStream) {
			try(
				ParallelGZIPInputStream pigzis = new ParallelGZIPInputStream(is);
				TarArchiveInputStream tarIs = new TarArchiveInputStream(pigzis)
			) {
				TarUtil.extractTarArchive(tarIs, extractLocation);
			}
		}else {
			try(BufferedInputStream bis = new BufferedInputStream(is)) {
				extractCompressedTarArchive(bis, extractLocation);
			}
		}
	}
}
