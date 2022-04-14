package to.etc.domui.util.exporters;

import org.eclipse.jdt.annotation.NonNull;

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

	static public synchronized IExportFormat getByExt(String extension) {
		return getExportFormats().stream()
			.filter(a -> a.extension().equals(extension))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Unknown format " + extension));
	}

	static {
		register(new AbstractExportFormat("xls", "Microsoft Office Excel (old)") {
			@Override public IExportWriter<?> createWriter(@NonNull File out) {
				return new ExcelExportWriter<>(ExcelFormat.XLS, out);
			}
		});
		register(new AbstractExportFormat("xlsx", "Microsoft Office Excel") {
			@Override public IExportWriter<?> createWriter(@NonNull File out) {
				return new ExcelExportWriter<>(ExcelFormat.XLSX, out);
			}
		});
		register(new AbstractExportFormat("csv", "Comma-separated file") {
			@Override
			public IExportWriter<?> createWriter(File output) throws Exception {
				return new CsvExportWriter<>(output);
			}
		});
	}
}
