package to.etc.domui.util.exporters;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.delayed.IProgress;
import to.etc.domui.component.layout.Dialog;
import to.etc.domui.component.menu.PopupMenu;
import to.etc.domui.component.misc.FaIcon;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.parts.TempFilePart;
import to.etc.domui.parts.TempFilePart.Disposition;
import to.etc.domui.util.AsyncDialogTask;
import to.etc.domui.util.Msgs;
import to.etc.function.ConsumerEx;
import to.etc.webapp.query.QCriteria;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;

/**
 * Helper class that creates Export buttons, or handles the actions thereof.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-10-17.
 */
public class ExporterButtons {
	private ExporterButtons() {
	}

	static public DefaultButton	createExportButton(to.etc.function.ConsumerEx<IExportFormat> onExport) {
		return new DefaultButton(Msgs.BUNDLE.getString(Msgs.EXPORT_BUTTON), FaIcon.faFileO, a -> exportAction(onExport, a));
	}

	public static void exportAction(ConsumerEx<IExportFormat> onExport, NodeBase target) {
		List<IExportFormat> exportFormats = ExportFormatRegistry.getExportFormats();
		PopupMenu pm = new PopupMenu();
		for(IExportFormat xf : exportFormats) {
			pm.addItem(xf.extension(), FaIcon.faFile, xf.name(), false, s -> onExport.accept(xf));
		}
		pm.show(target);
	}

	public static <T> void export(NodeContainer node, IExportFormat xf, QCriteria<T> query, List<String> columns) {
		Exporter<T> x = new Exporter<>(xf, query, columns);
		AsyncDialogTask.runInDialog(node, x, "Export", true, true);
	}

	static private class Exporter<T> extends AsyncDialogTask {
		private final IExportFormat m_format;

		final private QCriteria<T> m_criteria;

		final private List<String> m_columns;

		private File m_out;

		private IExportWriter<T> m_writer;

		public Exporter(IExportFormat format, QCriteria<T> criteria, List<String> columns) {
			m_format = format;
			m_criteria = criteria;
			m_columns = columns;
		}

		@Override protected void execute(@Nonnull IProgress progress) throws Exception {
			IExportWriter<T> writer = m_writer = (IExportWriter<T>) m_format.createWriter();
			File out = m_out = File.createTempFile("xp-",  "." + m_format.extension());
			QCriteriaExporter<T> qxp = new QCriteriaExporter<>(out, writer, dc(), m_criteria, m_columns);
			qxp.export(progress);
		}

		@Override public void onCompleted(NodeContainer node) {
			TempFilePart.createDownloadAction(node, m_out, m_writer.getMimeType(), Disposition.Attachment, m_out.getName());
		}

		@Override protected void onError(Dialog dlg, boolean cancelled, @Nonnull Exception errorException) {
			errorException.printStackTrace();
			super.onError(dlg, cancelled, errorException);
		}
	}
}
