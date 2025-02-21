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

public class CompressedTarUtil {
	/**
	 * Buffer size that we use for ParallelGZIP constructors. It eliminates need of using extra wrapped buffer streams.
	 */
	private static final int BUFFER_SIZE = 1024 * 1024;

	private CompressedTarUtil() {
		// Deliberately left empty
	}

	public interface ICompressionProgress {
		void progress(long uncompressedBytes, long compressedBytes);
	}

	private final static class ProgressInfo {
		private long m_compressed;

		private long m_uncompressed;
	}

	/**
	 * Create a tar archive, with an optional listener of progress. The progress listener,
	 * when present, will be called with 2 arguments: the first one the uncompressed size
	 * written, the second one the compressed size written.
	 */
	public static void tar(OutputStream os, @Nullable ICompressionProgress sizeProgress, File rootLocation, String... entryPaths) throws IOException {
		ProgressInfo pi = new ProgressInfo();
		try(
			SizeCountingOutputStream compressedSizerOs = new SizeCountingOutputStream(os);
			ParallelGZIPOutputStream pigzos = new ParallelGZIPOutputStream(compressedSizerOs, BUFFER_SIZE);
			SizeCountingOutputStream uncompressedSizerOs = new SizeCountingOutputStream(pigzos);
			TarArchiveOutputStream tarOs = new TarArchiveOutputStream(uncompressedSizerOs)
		) {
			if(null != sizeProgress) {
				uncompressedSizerOs.setListener(amount -> {
					pi.m_uncompressed = amount;
					sizeProgress.progress(pi.m_uncompressed, pi.m_compressed);
				});
				compressedSizerOs.setListener(amount -> {
					pi.m_compressed = amount;
					sizeProgress.progress(pi.m_uncompressed, pi.m_compressed);
				});
			}
			TarUtil.createTarArchive(tarOs, rootLocation, entryPaths);
		}
	}

	/**
	 * Extract all files from a compressed tar archive to a target location.
	 */
	public static void untar(File extractLocation, InputStream is, @Nullable ICompressionProgress sizeProgress) throws IOException {
		//we create intermediate progress monitor stream
		ProgressInfo pi = new ProgressInfo();
		try(
			SizeCountingInputStream compressedIs = new SizeCountingInputStream(is);
			ParallelGZIPInputStream pigzis = new ParallelGZIPInputStream(compressedIs, BUFFER_SIZE);
			SizeCountingInputStream uncompressedIs = new SizeCountingInputStream(pigzis);
			TarArchiveInputStream tarIs = new TarArchiveInputStream(uncompressedIs)
		) {
			if(null != sizeProgress) {
				uncompressedIs.setListener(amount -> {
					pi.m_uncompressed = amount;
					sizeProgress.progress(pi.m_uncompressed, pi.m_compressed);
				});
				compressedIs.setListener(amount -> {
					pi.m_compressed = amount;
					sizeProgress.progress(pi.m_uncompressed, pi.m_compressed);
				});
			}
			TarUtil.extractTarArchive(tarIs, extractLocation);
		}
	}
}
