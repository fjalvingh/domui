package to.etc.domui.util.exporters;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.menu.PopupMenu;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.impl.ExpandedDisplayProperty;
import to.etc.domui.component.misc.FaIcon;
import to.etc.domui.component.searchpanel.SearchPanel;
import to.etc.domui.component.tbl.ColumnDef;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.converter.IObjectToStringConverter;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.parts.TempFilePart;
import to.etc.domui.parts.TempFilePart.Disposition;
import to.etc.domui.util.Msgs;
import to.etc.domui.util.asyncdialog.AsyncDialog;
import to.etc.function.ConsumerEx;
import to.etc.function.SupplierEx;
import to.etc.util.Progress;
import to.etc.webapp.nls.NlsContext;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QField;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

	//public static <T> void export(NodeContainer node, IExportFormat xf, QCriteria<T> query, List<String> columns, String fileName) {
	//	List<ExpandedDisplayProperty<?>> xProps = ExpandedDisplayProperty.expandPropertiesWithDefaults(query.getBaseClass(), columns.toArray(new String[0]));
	//	List<? extends ExpandedDisplayPropertyColumnWrapper<?>> tlist = xProps.stream().map(a -> new ExpandedDisplayPropertyColumnWrapper<>(a)).collect(Collectors.toList());
	//
	//
	//}

	public static <T> void export(NodeContainer node, IExportFormat xf, QCriteria<T> query, List<IExportColumn<?>> columns, String fileName) {
		QueryExporterTask<T> x = new QueryExporterTask<>(xf, query, columns);
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

	public static <T> void export(NodeContainer node, Class<T> baseClass, List<T> list, IExportFormat xf, List<IExportColumn<?>> columns, String fileName) {
		ListExporterTask<T> exporterTask = new ListExporterTask<>(xf, baseClass, list, columns);
		AsyncDialog.runInDialog(node, exporterTask, "Export", true, task -> {
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


	static public <T> ExportButtonBuilder<T> from(Class<T> baseClass, SupplierEx<QCriteria<T>> supplier) {
		return new ExportButtonBuilder<>(baseClass, supplier);
	}

	static public <T> ExportButtonBuilder<T> from(SearchPanel<T> panel) {
		return new ExportButtonBuilder<>(panel);
	}

	static private class QueryExporterTask<T> extends AbstractExporter<T> {
		final private QCriteria<T> m_criteria;

		final private List<IExportColumn<?>> m_columns;

		public QueryExporterTask(IExportFormat format, QCriteria<T> criteria, List<IExportColumn<?>> columns) {
			super(format);
			m_criteria = criteria;
			m_columns = columns;
		}

		@Override protected void export(IExportWriter<T> writer, @NonNull Progress progress) throws Exception {
			QCriteriaExporter<T> qxp = new QCriteriaExporter<>(writer, dc(), m_criteria, m_columns);
			qxp.export(progress);
		}
	}

	static private class ListExporterTask<T> extends AbstractExporter<T> {
		final private List<IExportColumn<?>> m_columns;

		private final Class<T> m_baseClass;

		private final List<T> m_list;

		public ListExporterTask(IExportFormat format, Class<T> baseClass, List<T> list, List<IExportColumn<?>> columns) {
			super(format);
			m_baseClass = baseClass;
			m_list = list;
			m_columns = columns;
		}

		@Override protected void export(IExportWriter<T> writer, @NonNull Progress progress) throws Exception {
			ListExporter<T> qxp = new ListExporter<>(m_baseClass, m_list, writer, m_columns);
			qxp.export(progress);
		}
	}

	public static class ExportColumnBuilder<T, P> implements IExportColumn<P> {
		private final ExportButtonBuilder<T> m_parent;

		private final PropertyMetaModel<P> m_property;

		@Nullable
		private String m_label;

		@Nullable
		private IObjectToStringConverter<P> m_converter;

		public ExportColumnBuilder(ExportButtonBuilder<T> parent, PropertyMetaModel<P> property) {
			m_parent = parent;
			m_property = property;
		}

		public ExportColumnBuilder<T, P> label(String label) {
			m_label = label;
			return this;
		}

		public ExportColumnBuilder<T, P> converter(IObjectToStringConverter<P> c) {
			m_converter = c;
			return this;
		}

		public ExportButtonBuilder<T> build() {
			m_parent.addColumn(this);
			return m_parent;
		}

		@Override public String getLabel() {
			String label = m_label;
			return null == label ? m_property.getDefaultLabel() : label;
		}

		@Override public Class<?> getActualType() {
			return m_property.getActualType();
		}

		@Override public IExportCellRenderer<?, ?, ?> getRenderer() {
			return null;
		}

		@Override public Object convertValue(Object value) {
			IObjectToStringConverter<P> converter = m_converter;
			return converter == null ? value : converter.convertObjectToString(NlsContext.getLocale(), (P) value);
		}

		@Override public P getValue(Object in) throws Exception {
			return m_property.getValue(in);
		}
	}

	public static final class ExportButtonBuilder<T> {
		private final ClassMetaModel m_classModel;

		private final SupplierEx<QCriteria<T>> m_criteriaSupplier;

		private final SearchPanel<T> m_searchPanel;

		private ConsumerEx<QCriteria<T>> m_customizer;

		private final List<IExportColumn<?>> m_columnList = new ArrayList<>();

		private String m_fileName;

		@Nullable
		private List<T> m_sourceRecords;

		public ExportButtonBuilder(Class<T> classModel, SupplierEx<QCriteria<T>> criteriaSupplier) {
			m_classModel = MetaManager.findClassMeta(classModel);
			m_criteriaSupplier = criteriaSupplier;
			m_searchPanel = null;
		}

		public ExportButtonBuilder(SearchPanel<T> searchPanel) {
			m_classModel = searchPanel.getMetaModel();
			m_searchPanel = searchPanel;
			m_criteriaSupplier = null;
		}

		void addColumn(ExportColumnBuilder<T, ?> c) {
			m_columnList.add(c);
		}

		//public ExportButtonBuilder<T> columns(List<String> list) {
		//	m_columns.addAll(list);
		//	return this;
		//}
		//
		//public ExportButtonBuilder<T> columns(String... names) {
		//	for(String name : names) {
		//		m_columns.add(name);
		//	}
		//	return this;
		//}
		//
		//public ExportButtonBuilder<T> columns(QField<T, ?>... names) {
		//	for(QField<T, ?> name : names) {
		//		m_columns.add(name.getName());
		//	}
		//	return this;
		//}

		public <P> ExportColumnBuilder<T, P> column(QField<T, P> field) {
			return new ExportColumnBuilder<>(this, m_classModel.getProperty(field));
		}

		public ExportColumnBuilder<T, Object> column(String name) {
			return new ExportColumnBuilder<>(this, (PropertyMetaModel<Object>) m_classModel.getProperty(name));
		}

		public ExportButtonBuilder<T> fileName(String name) {
			m_fileName = name;
			return this;
		}

		public ExportButtonBuilder<T> source(List<T> source) {
			m_sourceRecords = source;
			return this;
		}

		public ExportButtonBuilder<T> customizer(ConsumerEx<QCriteria<T>> c) {
			m_customizer = c;
			return this;
		}

		/**
		 * Try to generate the column definitions from a row renderer.
		 */
		public ExportButtonBuilder<T> rowRenderer(@Nullable RowRenderer<T> rr) {
			if(null == rr)
				return this;

			if(m_columnList.size() > 0)
				throw new IllegalArgumentException("Columns have been added already, a row renderer can only be used as a definition for all columns");
			for(ColumnDef<T, ?> rrCol : rr.getColumnList()) {
				appendColumn(rrCol);
			}
			return this;
		}

		private void appendColumn(ColumnDef<T, ?> c) {
			RowRendererCellWrapper<Object> w = RowRendererCellWrapper.create(c);
			if(null != w)
				m_columnList.add(w);
		}

		public List<IExportColumn<?>> calculateColumnList() {
			List<IExportColumn<?>> columnList = m_columnList;
			if(columnList.size() > 0)
				return columnList;

			//-- Try to create a column list.
			List<ExpandedDisplayProperty<?>> xProps = ExpandedDisplayProperty.expandPropertiesWithDefaults(m_classModel, null);
			columnList = convertExpandedToColumn(xProps);
			return columnList;
		}

		protected List<IExportColumn<?>> convertExpandedToColumn(List<ExpandedDisplayProperty<?>> xProps) {
			return xProps.stream().map(a -> new ExpandedDisplayPropertyColumnWrapper<>(a)).collect(Collectors.toList());
		}

		protected void executeExportByQuery(NodeContainer node, IExportFormat format) throws Exception {
			QCriteria<T> criteria = getSelectionCriteria();
			if(null == criteria) {
				return;
			}
			String fileName = calculateFileName(criteria);
			ConsumerEx<QCriteria<T>> customizer = m_customizer;
			if(customizer != null)
				customizer.accept(criteria);

			ExporterButtons.export(node, format, criteria, calculateColumnList(), fileName);
		}

		protected void executeExportFromList(NodeContainer targetNode, IExportFormat format) throws Exception {
			QCriteria<T> criteria = getSelectionCriteria();
			if(null == criteria) {
				return;
			}
			List<T> sourceRecords = m_sourceRecords;
			if(null == sourceRecords)
				return;
			String fileName = calculateFileName(criteria);
			ConsumerEx<QCriteria<T>> customizer = m_customizer;
			if(customizer != null)
				customizer.accept(criteria);
			List<T> result = MetaManager.query(sourceRecords, criteria);

			ExporterButtons.export(targetNode, criteria.getBaseClass(), result, format, calculateColumnList(), fileName);
		}

		private String calculateFileName(QCriteria<T> criteria) {
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
			return fileName;
		}

		@Nullable
		private QCriteria<T> getSelectionCriteria() throws Exception {
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
			return criteria;
		}

		public DefaultButton build() {
			DefaultButton button = new DefaultButton(Msgs.BUNDLE.getString(Msgs.EXPORT_BUTTON), FaIcon.faFileExcelO);
			button.setClicked(ab -> {
				showFormatPopup(format -> {
					if(m_sourceRecords != null) {
						executeExportFromList(ab.getParent(), format);
					} else {
						executeExportByQuery(ab.getParent(), format);
					}
				}, ab);
			});
			return button;
		}
	}


}
