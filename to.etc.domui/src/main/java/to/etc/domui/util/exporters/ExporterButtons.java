package to.etc.domui.util.exporters;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.menu.PopupMenu;
import to.etc.domui.component.misc.FaIcon;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.parts.TempFilePart;
import to.etc.domui.parts.TempFilePart.Disposition;
import to.etc.domui.util.Msgs;
import to.etc.domui.util.asyncdialog.AsyncDialog;
import to.etc.function.ConsumerEx;
import to.etc.util.Progress;
import to.etc.webapp.query.QCriteria;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.Objects;

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

	static public DefaultButton	createExportButton(String name, String icon, to.etc.function.ConsumerEx<IExportFormat> onExport) {
		return new DefaultButton(name, icon, a -> exportAction(onExport, a));
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
		AsyncDialog.runInDialog(node, x, "Export", true, task -> {
			File target = Objects.requireNonNull(task.getOutputFile());
			TempFilePart.createDownloadAction(node, target, task.getMimeType(), Disposition.Attachment, target.getName());
		});
	}

	static private class Exporter<T> extends AbstractExporter<T> {
		final private QCriteria<T> m_criteria;

		final private List<String> m_columns;

		private IExportWriter<T> m_writer;

		public Exporter(IExportFormat format, QCriteria<T> criteria, List<String> columns) {
			super(format);
			m_criteria = criteria;
			m_columns = columns;
		}

		@Override protected void export(IExportWriter<T> writer, @Nonnull Progress progress) throws Exception {
			QCriteriaExporter<T> qxp = new QCriteriaExporter<>(writer, dc(), m_criteria, m_columns);
			qxp.export(progress);
		}
	}
}
