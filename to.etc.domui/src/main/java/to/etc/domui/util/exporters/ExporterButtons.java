package to.etc.domui.util.exporters;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.menu.PopupMenu;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.misc.FaIcon;
import to.etc.domui.component.searchpanel.SearchPanel;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.parts.TempFilePart;
import to.etc.domui.parts.TempFilePart.Disposition;
import to.etc.domui.util.Msgs;
import to.etc.domui.util.asyncdialog.AsyncDialog;
import to.etc.function.ConsumerEx;
import to.etc.function.SupplierEx;
import to.etc.util.Progress;
import to.etc.webapp.query.QCriteria;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
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

	static public DefaultButton createExportButton(to.etc.function.ConsumerEx<IExportFormat> onExport) {
		return new DefaultButton(Msgs.BUNDLE.getString(Msgs.EXPORT_BUTTON), FaIcon.faFileExcelO, a -> showFormatPopup(onExport, a));
	}

	static public DefaultButton	createExportButton(String name, String icon, to.etc.function.ConsumerEx<IExportFormat> onExport) {
		return new DefaultButton(name, icon, a -> showFormatPopup(onExport, a));
	}

	public static void showFormatPopup(ConsumerEx<IExportFormat> onExport, NodeBase target) {
		List<IExportFormat> exportFormats = ExportFormatRegistry.getExportFormats();
		PopupMenu pm = new PopupMenu();
		for(IExportFormat xf : exportFormats) {
			pm.addItem(xf.extension(), FaIcon.faFile, xf.name(), false, s -> onExport.accept(xf));
		}
		pm.show(target);
	}

	public static <T> void export(NodeContainer node, IExportFormat xf, QCriteria<T> query, List<String> columns, String fileName) {
		Exporter<T> x = new Exporter<>(xf, query, columns);
		AsyncDialog.runInDialog(node, x, "Export", true, task -> {
			File target = Objects.requireNonNull(task.getOutputFile());
			String fn = fileName;
			if(null == fn) {
				fn = target.getName();
			} else {
				if(fn.lastIndexOf('.') == -1) {
					fn += "." + xf.extension();
				}
			}

			TempFilePart.createDownloadAction(node, target, task.getMimeType(), Disposition.Attachment, fn);
		});
	}

	static public <T> ExportButtonBuilder<T> from(SupplierEx<QCriteria<T>> supplier) {
		return new ExportButtonBuilder<>(supplier);
	}

	static public <T> ExportButtonBuilder<T> from(SearchPanel<T> panel) {
		return new ExportButtonBuilder<>(panel);
	}

	static private class Exporter<T> extends AbstractExporter<T> {
		final private QCriteria<T> m_criteria;

		final private List<String> m_columns;

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

	public static final class ExportButtonBuilder<T> {
		private final SupplierEx<QCriteria<T>> m_criteriaSupplier;

		private final SearchPanel<T> m_searchPanel;

		private ConsumerEx<QCriteria<T>> m_customizer;

		private final List<String> m_columns = new ArrayList<>();

		private String m_fileName;

		public ExportButtonBuilder(SupplierEx<QCriteria<T>> criteriaSupplier) {
			m_criteriaSupplier = criteriaSupplier;
			m_searchPanel = null;
		}

		public ExportButtonBuilder(SearchPanel<T> searchPanel) {
			m_searchPanel = searchPanel;
			m_criteriaSupplier = null;
		}

		public ExportButtonBuilder<T> columns(List<String> list) {
			m_columns.addAll(list);
			return this;
		}

		public ExportButtonBuilder<T> columns(String... names) {
			for(String name : names) {
				m_columns.add(name);
			}
			return this;
		}

		public ExportButtonBuilder<T> fileName(String name) {
			m_fileName = name;
			return this;
		}

		public ExportButtonBuilder<T> customizer(ConsumerEx<QCriteria<T>> c) {
			m_customizer = c;
			return this;
		}

		protected void executeExport(NodeContainer node, IExportFormat format) throws Exception {
			SupplierEx<QCriteria<T>> criteriaSupplier = m_criteriaSupplier;
			QCriteria<T> criteria;
			if(criteriaSupplier != null)
				criteria = criteriaSupplier.get();
			else {
				SearchPanel<T> searchPanel = m_searchPanel;
				if(null != searchPanel) {
					criteria = searchPanel.getCriteria();
				} else {
					throw new IllegalStateException("No panel nor criteria");
				}
			}
			if(null == criteria) {
				return;
			}
			String fileName = m_fileName;
			if(null == fileName) {
				//-- Get table name
				Class<T> baseClass = criteria.getBaseClass();
				if(null != baseClass) {
					ClassMetaModel classMeta = MetaManager.findClassMeta(baseClass);
					String tableName = classMeta.getTableName();
					if(null != tableName) {
						int pos = tableName.lastIndexOf('.');
						if(pos >= 0) {
							tableName = tableName.substring(pos + 1);
						}

						fileName = tableName.toLowerCase();
					} else {
						fileName = baseClass.getSimpleName().toLowerCase();
					}
				}
			}
			ConsumerEx<QCriteria<T>> customizer = m_customizer;
			if(customizer != null)
				customizer.accept(criteria);

			ExporterButtons.export(node, format, criteria, m_columns.size() == 0 ? null : m_columns, fileName);
		}

		public DefaultButton build() {
			DefaultButton button = new DefaultButton(Msgs.BUNDLE.getString(Msgs.EXPORT_BUTTON), FaIcon.faFileExcelO);
			button.setClicked(ab -> {
				showFormatPopup(format -> {
					executeExport(ab.getParent(), format);
				}, ab);
			});
			return button;
		}
	}


}
