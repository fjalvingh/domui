package to.etc.util;

import org.anarres.parallelgzip.ParallelGZIPInputStream;
import org.anarres.parallelgzip.ParallelGZIPOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

public class CompressedTarUtil {
	/**
	 * Buffer size that we use for ParallelGZIP constructors. It eliminates need of using extra wrapped buffer streams.
	 */
	private static final int BUFFER_SIZE = 1024 * 1024;

	private CompressedTarUtil() {
		// Deliberately left empty
	}

	/**
	 * Create a tar archive, with an optional listener of progress.
	 */
	public static void tar(OutputStream os, @Nullable Consumer<Long> sizeProgress, File rootLocation, String... entryPaths) throws IOException {
		try(
			ParallelGZIPOutputStream pigzos = new ParallelGZIPOutputStream(os, BUFFER_SIZE);
			ProgressOutputStream pos = new ProgressOutputStream(pigzos);
			TarArchiveOutputStream tarOs = new TarArchiveOutputStream(pos)
		) {
			if(null != sizeProgress) {
				pos.addOnSizeListener(sizeProgress);
			}
			TarUtil.createTarArchive(tarOs, rootLocation, entryPaths);
		}
	}

	/**
	 * Extract all files from a compressed tar archive to a target location.
	 */
	public static void untar(File extractLocation, InputStream is, @Nullable Consumer<Long> sizeProgress) throws IOException {
		//we create intermediate progress monitor stream
		try(
			ParallelGZIPInputStream pigzis = new ParallelGZIPInputStream(is, BUFFER_SIZE);
			ProgressInputStream pis = new ProgressInputStream(pigzis);
			TarArchiveInputStream tarIs = new TarArchiveInputStream(pis)
		) {
			if(null != sizeProgress) {
				pis.addOnSizeListener(sizeProgress);
			}
			TarUtil.extractTarArchive(tarIs, extractLocation);
		}
	}
}
