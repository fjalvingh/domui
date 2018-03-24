package to.etc.domui.util.exporters;

import to.etc.domui.component.meta.impl.ExpandedDisplayProperty;
import to.etc.util.Progress;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QDataContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-10-17.
 */
public class QCriteriaExporter<T> {
	private final QDataContext m_dc;

	private final QCriteria<T> m_query;

	private final IExportWriter<T>	m_exportWriter;

	private final List<IExportColumn<?>> m_columnList;

	public QCriteriaExporter(@Nonnull IExportWriter<T> writer, @Nonnull QDataContext dc, @Nonnull QCriteria<T> query, @Nullable String... columns) {
		m_dc = dc;
		m_query = query;
		m_exportWriter = writer;

		Class<T> baseClass = query.getBaseClass();
		if(null == baseClass)
			throw new IllegalStateException("Metadata-query not yet supported");
		List<ExpandedDisplayProperty<?>> xProps = ExpandedDisplayProperty.expandPropertiesWithDefaults(baseClass, columns);
		m_columnList = convertExpandedToColumn(xProps);
	}

	private List<IExportColumn<?>> convertExpandedToColumn(List<ExpandedDisplayProperty<?>> xProps) {
		return xProps.stream().map(a -> new ExpandedDisplayPropertyColumnWrapper<>(a)).collect(Collectors.toList());
	}

	public QCriteriaExporter(IExportWriter<T> writer, QDataContext dc, QCriteria<T> query, List<String> columns) {
		this(writer, dc, query, columns == null ? null : columns.toArray(new String[columns.size()]));
	}

	public ExportResult export(Progress p) throws Exception {
		if(m_columnList.size() == 0)
			return ExportResult.EMPTY;

		int rowLimit = m_exportWriter.getRowLimit();
		int limit = m_query.getLimit();
		if(limit > rowLimit || limit <= 0)
			m_query.limit(rowLimit + 1);

		List<T> list = m_dc.query(m_query);
		m_exportWriter.startExport(m_columnList);
		try {
			int count = 0;
			p.setTotalWork(list.size() + (list.size() / 100));
			for(T t : list) {
				if(++count >= rowLimit) {
					break;
				}
				m_exportWriter.exportRow(t);
				p.setCompleted(count);
			}
			return list.size() >= rowLimit ? ExportResult.TRUNCATED : ExportResult.COMPLETED;
		} finally {
			//m_exportWriter.close();					// We do not own exportWriter, this leads to double close.
		}
	}
}
