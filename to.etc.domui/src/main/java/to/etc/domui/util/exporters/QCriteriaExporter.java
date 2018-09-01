package to.etc.domui.util.exporters;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.util.Progress;
import to.etc.webapp.query.QCriteria;
import to.etc.webapp.query.QDataContext;

import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-10-17.
 */
public class QCriteriaExporter<T> extends AbstractObjectExporter<T> {
	private final QDataContext m_dc;

	private final QCriteria<T> m_query;

	private final IExportWriter<T>	m_exportWriter;

	private final List<IExportColumn<?>> m_columnList;

	public QCriteriaExporter(@NonNull IExportWriter<T> writer, @NonNull QDataContext dc, @NonNull QCriteria<T> query, List<IExportColumn<?>> columnList) {
		m_dc = dc;
		m_query = query;
		m_exportWriter = writer;

		Class<T> baseClass = query.getBaseClass();
		if(null == baseClass)
			throw new IllegalStateException("Metadata-query not yet supported");
		m_columnList = columnList;
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
