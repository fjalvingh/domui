package to.etc.domui.util.exporters;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-10-17.
 */
public class ExportFormatRegistry {
	static private List<IExportFormat> m_list = Collections.emptyList();

	static synchronized public void register(IExportFormat format) {
		ArrayList<IExportFormat> list = new ArrayList<>(m_list);
		list.add(format);
		list.sort( (a,b) -> a.name().compareToIgnoreCase(b.name()));
		m_list = Collections.unmodifiableList(list);
	}

	public static synchronized List<IExportFormat> getExportFormats() {
		return m_list;
	}

	static {
		register(new AbstractExportFormat("xls", "Microsoft Office Excel (old)") {
			@Override public IExportWriter<?> createWriter(@Nonnull File out) {
				return new ExcelExportWriter<>(ExcelFormat.XLS, out);
			}
		});
		register(new AbstractExportFormat("xlsx", "Microsoft Office Excel") {
			@Override public IExportWriter<?> createWriter(@Nonnull File out) {
				return new ExcelExportWriter<>(ExcelFormat.XLSX, out);
			}
		});
	}
}
