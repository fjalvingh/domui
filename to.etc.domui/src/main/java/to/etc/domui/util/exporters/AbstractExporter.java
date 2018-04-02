package to.etc.domui.util.exporters;

import to.etc.domui.util.asyncdialog.AbstractAsyncDialogTask;
import to.etc.util.Progress;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-10-17.
 */
abstract public class AbstractExporter<T> extends AbstractAsyncDialogTask implements IExportFile {
	private final IExportFormat m_format;

	private final String m_outputName;

	@Nullable
	private File m_out;

	@Nullable
	private IExportWriter<T> m_writer;

	@Nonnull
	private String m_mimeType = "application/octet-stream";

	public AbstractExporter(IExportFormat format) {
		m_format = format;
		m_outputName = "export-" + new SimpleDateFormat("yyyymmdd-HHmm").format(new Date()) + "." + format.extension();
	}

	public AbstractExporter(IExportFormat format, String name) {
		m_format = format;
		m_outputName = name + " " + new SimpleDateFormat("yyyymmdd-HHmm").format(new Date()) + "." + format.extension();
	}

	@Override protected void execute(@Nonnull Progress progress) throws Exception {
		File out = m_out = File.createTempFile("xp-",  "." + m_format.extension());
		try(IExportWriter<T> writer = m_writer = (IExportWriter<T>) m_format.createWriter(out)) {
			m_mimeType = writer.getMimeType();
			export(writer, progress);
		}
	}

	@Override
	@Nonnull public File getOutputFile() {
		return Objects.requireNonNull(m_out);
	}

	@Nonnull
	@Override
	public String getOutputName() {
		return m_outputName;
	}

	@Override
	@Nonnull public String getMimeType() {
		return m_mimeType;
	}

	abstract protected void export(@Nonnull IExportWriter<T> out, @Nonnull Progress progress) throws Exception;
}
