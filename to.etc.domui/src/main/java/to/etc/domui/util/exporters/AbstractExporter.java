package to.etc.domui.util.exporters;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.util.asyncdialog.AbstractAsyncDialogTask;
import to.etc.util.Progress;

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

	@NonNull
	private String m_mimeType = "application/octet-stream";

	public AbstractExporter(IExportFormat format) {
		m_format = format;
		m_outputName = "export-" + new SimpleDateFormat("yyyymmdd-HHmm").format(new Date()) + "." + format.extension();
	}

	public AbstractExporter(IExportFormat format, String name) {
		m_format = format;
		m_outputName = name + " " + new SimpleDateFormat("yyyymmdd-HHmm").format(new Date()) + "." + format.extension();
	}

	@Override protected void execute(@NonNull Progress progress) throws Exception {
		File out = m_out = File.createTempFile("xp-",  "." + m_format.extension());
		try(IExportWriter<T> writer = m_writer = (IExportWriter<T>) m_format.createWriter(out)) {
			m_mimeType = writer.getMimeType();
			export(writer, progress);
		}
	}

	@Override
	@NonNull public File getOutputFile() {
		return Objects.requireNonNull(m_out);
	}

	@NonNull
	@Override
	public String getOutputName() {
		return m_outputName;
	}

	@Override
	@NonNull public String getMimeType() {
		return m_mimeType;
	}

	abstract protected void export(@NonNull IExportWriter<T> out, @NonNull Progress progress) throws Exception;
}
