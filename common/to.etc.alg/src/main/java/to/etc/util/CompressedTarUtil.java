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

	/** Buffer size that we use for ParallelGZIP constructors. It eliminates need of using extra wrapped buffer streams. */
	private static final int BUFFER_SIZE = 1024 * 1024;

	public static void createCompressedTarArchive(OutputStream os, File rootLocation, String... entryPaths) throws IOException {
		internalCreateCompressedTarArchive(os, -1, -1, null, null, rootLocation, entryPaths);
	}

	public static void createCompressedTarArchiveWithSizeProgress(OutputStream os, int estimatedSize, int reportChunk, Consumer<Long> sizeProgress, File rootLocation, String... entryPaths) throws IOException {
		internalCreateCompressedTarArchive(os, estimatedSize, reportChunk, sizeProgress, null, rootLocation, entryPaths);
	}

	public static void createCompressedTarArchiveWithPercentageProgress(OutputStream os, int estimatedSize, Consumer<Integer> percentageProgress, File rootLocation, String... entryPaths) throws IOException {
		internalCreateCompressedTarArchive(os, estimatedSize, -1, null, percentageProgress, rootLocation, entryPaths);
	}

	private static void internalCreateCompressedTarArchive(OutputStream os, int estimatedSize, int reportChunk, @Nullable Consumer<Long> sizeProgress, @Nullable Consumer<Integer> percentageProgress, File rootLocation, String... entryPaths) throws IOException {
		if(estimatedSize > -1 && (null != sizeProgress || null != percentageProgress)) {
			//we create intermediate progress monitor stream
			try(
				ParallelGZIPOutputStream pigzos = new ParallelGZIPOutputStream(os, BUFFER_SIZE);
				ProgressOutputStream pos = new ProgressOutputStream(pigzos, estimatedSize, reportChunk);
				TarArchiveOutputStream tarOs = new TarArchiveOutputStream(pos)
			) {
				if(null != sizeProgress) {
					pos.addOnSizeListener(sizeProgress);
				}
				if(null != percentageProgress) {
					pos.addOnPercentListener(percentageProgress);
				}
				TarUtil.createTarArchive(tarOs, rootLocation, entryPaths);
			}
		}else {
			//we go without intermediate progress monitor stream
			try(
				ParallelGZIPOutputStream pigzos = new ParallelGZIPOutputStream(os, BUFFER_SIZE);
				TarArchiveOutputStream tarOs = new TarArchiveOutputStream(pigzos)
			) {
				TarUtil.createTarArchive(tarOs, rootLocation, entryPaths);
			}
		}
	}

	public static void extractCompressedTarArchive(InputStream is, File extractLocation) throws IOException {
		internalExtractCompressedTarArchive(is, extractLocation, -1, -1, null, null);
	}

	public static void extractCompressedTarArchiveWithSizeProgress(InputStream is, File extractLocation, int estimatedSize, int reportChunk, Consumer<Long> sizeProgress) throws IOException {
		internalExtractCompressedTarArchive(is, extractLocation, estimatedSize, reportChunk, sizeProgress, null);
	}

	public static void extractCompressedTarArchiveWithPercentageProgress(InputStream is, File extractLocation, int estimatedSize, Consumer<Integer> percentageProgress) throws IOException {
		internalExtractCompressedTarArchive(is, extractLocation, estimatedSize, -1, null, percentageProgress);
	}

	private static void internalExtractCompressedTarArchive(InputStream is, File extractLocation, int estimatedSize, int reportChunk, @Nullable Consumer<Long> sizeProgress, @Nullable Consumer<Integer> percentageProgress) throws IOException {
		//we have to wrap BufferedInputStream if needed, since downstream streams work optimal with BufferedInputStream
		if(estimatedSize > -1 && (null != sizeProgress || null != percentageProgress)) {
			//we create intermediate progress monitor stream
			try(
				ParallelGZIPInputStream pigzis = new ParallelGZIPInputStream(is, BUFFER_SIZE);
				ProgressInputStream pis = new ProgressInputStream(pigzis, estimatedSize, reportChunk);
				TarArchiveInputStream tarIs = new TarArchiveInputStream(pis)
			) {
				if(null != sizeProgress) {
					pis.addOnSizeListener(sizeProgress);
				}
				if(null != percentageProgress) {
					pis.addOnPercentListener(percentageProgress);
				}
				TarUtil.extractTarArchive(tarIs, extractLocation);
			}
		}else {
			//we go without intermediate progress monitor stream
			try(
				ParallelGZIPInputStream pigzis = new ParallelGZIPInputStream(is, BUFFER_SIZE);
				TarArchiveInputStream tarIs = new TarArchiveInputStream(pigzis)
			) {
				TarUtil.extractTarArchive(tarIs, extractLocation);
			}
		}
	}
}
