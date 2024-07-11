package to.etc.domui.util.exporters;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.csv.CsvOptions;
import to.etc.csv.CsvOptions.QuoteMode;
import to.etc.csv.CsvWriter;
import to.etc.util.FileTool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 14-04-22.
 */
public class CsvExportWriter<T> implements IExportWriter<T> {
	private final File m_target;

	private List<? extends IExportColumn<?>> m_columnList;

	@Nullable
	private CsvWriter m_writer;

	public CsvExportWriter(File target) {
		m_target = target;
	}

	@Override
	public String getMimeType() {
		return "text/csv";
	}

	@Override
	public void startExport(List<? extends IExportColumn<?>> columnList) throws Exception {
		m_columnList = columnList;

		Writer output = null;

		try {
			List<String> header = columnList.stream()
				.map(this::calculateLabel)
				.collect(Collectors.toList());

			output = new OutputStreamWriter(new FileOutputStream(m_target));
			CsvOptions option = CsvOptions.create()
				.delimiter(',')
				.quote('"')
				.quoteMode(QuoteMode.ALL)
				.escape('"')
				.dateFormat("yyyyMMdd")
				.header(header)
				;
			CsvWriter w = new CsvWriter(output, option);

			m_writer = w;
			output = null;
		} finally {
			FileTool.closeAll(output);
		}
	}

	private <V extends IExportColumn<?>> String calculateLabel(V c) {
		String label = c.getLabel();
		if(null != label)
			return label;

		int index = m_columnList.indexOf(c);
		return "column" + index;
	}

	@Override
	public void exportRow(T data) throws Exception {
		CsvWriter writer = m_writer;
		if(null == writer)
			throw new IllegalStateException("Writer is not open; did you call startExport?");
		for(IExportColumn<?> col : m_columnList) {
			Object value = col.getValue(data);
			Object convertedValue = col.convertValue(value);
			if(null == convertedValue) {
				writer.printNull();
			} else {
				if(convertedValue instanceof Date) {
					writer.printDate((Date) convertedValue);
				} else if(convertedValue instanceof Boolean) {
					writer.printString(((Boolean) convertedValue) ? "true" : "false");
				} else if(convertedValue instanceof Number) {
					writer.printNumber((Number) convertedValue);
				} else if(convertedValue instanceof String) {
					writer.printString((String) convertedValue);
				} else {
					writer.printString(String.valueOf(convertedValue));
				}
			}
		}
		writer.println();
	}

	@Override
	public int getRowLimit() {
		return Integer.MAX_VALUE;
	}

	@Override
	public void close() throws Exception {
		CsvWriter writer = m_writer;
		if(null != writer) {
			writer.close();
			m_writer = null;
		}
	}
}
