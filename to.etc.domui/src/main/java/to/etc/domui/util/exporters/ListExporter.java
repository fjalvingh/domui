package to.etc.domui.util.exporters;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.util.Progress;

import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 28-5-18.
 */
public class ListExporter<T> extends AbstractObjectExporter<T> {
	private final IExportWriter<T>	m_exportWriter;

	private final List<IExportColumn<?>> m_columnList;

	private final Class<T> m_baseClass;

	private final List<T> m_list;

	public ListExporter(Class<T> baseClass, @NonNull List<T> list, @NonNull IExportWriter<T> writer, List<IExportColumn<?>> columns) {
		m_baseClass = baseClass;
		m_list = list;
		m_exportWriter = writer;
		m_columnList = columns;
	}

	public ExportResult export(Progress p) throws Exception {
		if(m_columnList.size() == 0)
			return ExportResult.EMPTY;
		List<T> list = m_list;
		m_exportWriter.startExport(m_columnList);
		try {
			int count = 0;
			p.setTotalWork(list.size() + (list.size() / 100));
			for(T t : list) {
				m_exportWriter.exportRow(t);
				p.setCompleted(count);
			}
			return ExportResult.COMPLETED;
		} finally {
			//m_exportWriter.close();					// We do not own exportWriter, this leads to double close.
		}
	}
}
