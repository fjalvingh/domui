package to.etc.domuidemo.pages.plotly;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.util.exporters.ExcelFormat;
import to.etc.domui.util.importers.CsvRowReader;
import to.etc.domui.util.importers.ExcelRowReader;
import to.etc.domui.util.importers.IImportRow;
import to.etc.domui.util.importers.IRowReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-12-21.
 */
public class ImportDataset implements Iterable<IImportRow> {
	final private List<IImportRow> m_rowList;

	private IImportRow m_header;

	public ImportDataset(IImportRow headerRow, List<IImportRow> list) {
		m_header = headerRow;
		m_rowList = list;
	}

	static public ImportDataset create(InputStream is, String type, @Nullable Charset encoding) throws Exception {
		type = type.toLowerCase();

		IRowReader rr;
		switch(type) {
			default:
				throw new IllegalStateException("Unknown type: " + type);

			case "csv":
				if(null == encoding)
					encoding = StandardCharsets.UTF_8;
				rr = new CsvRowReader(new InputStreamReader(is, encoding));
				break;

			case "xls":
			case "xlsx":
				rr = new ExcelRowReader(is, ExcelFormat.byExtension(type));

				break;
		}
		rr.setHasHeaderRow(true);

		try {
			List<IImportRow> list = new ArrayList<>();
			for(IImportRow row : rr) {
				list.add(row);
			}

			return new ImportDataset(rr.getHeaderRow(), list);
		} finally {
			rr.close();
		}
	}

	static public ImportDataset createFromResource(String path, String type, @Nullable Charset encoding) throws Exception {
		InputStream is = ImportDataset.class.getResourceAsStream(path);
		if(null == is)
			throw new IOException("Resource not found: " + path);
		try {
			return create(is, type, encoding);
		} finally {
			is.close();
		}
	}

	@Override
	public Iterator<IImportRow> iterator() {
		return m_rowList.iterator();
	}
}
