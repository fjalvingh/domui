package to.etc.domui.util.exporters;

import to.etc.domui.component.delayed.IProgress;
import to.etc.domui.component.layout.Dialog;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.parts.TempFilePart;
import to.etc.domui.parts.TempFilePart.Disposition;
import to.etc.domui.util.AsyncDialogTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Objects;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-10-17.
 */
abstract public class AbstractExporter<T> extends AsyncDialogTask {
	private final IExportFormat m_format;

	@Nullable
	private File m_out;

	@Nullable
	private IExportWriter<T> m_writer;

	public AbstractExporter(IExportFormat format) {
		m_format = format;
	}

	@Override protected void execute(@Nonnull IProgress progress) throws Exception {
		File out = m_out = File.createTempFile("xp-",  "." + m_format.extension());
		try(IExportWriter<T> writer = m_writer = (IExportWriter<T>) m_format.createWriter(out)) {
			export(writer, progress);
		}
	}

	abstract protected void export(@Nonnull IExportWriter<T> out, @Nonnull IProgress progress) throws Exception;

	@Override public void onCompleted(NodeContainer node) {
		File target = Objects.requireNonNull(m_out);
		TempFilePart.createDownloadAction(node, target, Objects.requireNonNull(m_writer).getMimeType(), Disposition.Attachment, target.getName());
	}

	@Override protected void onError(Dialog dlg, boolean cancelled, @Nonnull Exception errorException) {
		errorException.printStackTrace();
		super.onError(dlg, cancelled, errorException);
	}
}
