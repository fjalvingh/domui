package to.etc.domui.util.importers;

import com.monitorjbl.xlsx.StreamingReader;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.util.exporters.ExcelFormat;
import to.etc.util.FileTool;
import to.etc.util.WrappedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * This reads Excel rows in streaming mode (XSLX only).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-01-22.
 */
public class ExcelStreamingRowReader implements IRowReader, AutoCloseable, Iterable<IImportRow>, IExcelRowReader {
	private final ExcelFormat m_format;

	private final Workbook m_workbook;

	private InputStream m_inputStream;

	private int m_setIndex = -1;

	private Sheet m_currentSheet;

	/** Zero-based row */
	private int m_currentSheetRow;

	private Iterator<Row> m_currentSheetIterator;

	private long m_progressIndicator;

	private int m_headerRowCount;

	private List<ExcelImportRow> m_headerRows = new ArrayList<>();

	private final NumberFormat m_doubleFormatter;

	private final Map<String, DateFormat> m_dateFormatMap = new HashMap<>();

	@Nullable
	private DateFormat m_defaultDateFormat;

	/**
	 * When set all fields read by getValueAsString() will return this date format string
	 * if Excel has the idea the field was a date.
	 */
	@Nullable
	private DateFormat m_forceStringDateFormat;

	public ExcelStreamingRowReader(File file) throws Exception {
		String suffix = FileTool.getFileExtension(file.getName());
		ExcelFormat format = ExcelFormat.byExtension(suffix);
		if(null == format)
			throw new IOException("Excel format for file extension '" + suffix + "' is not found");
		if(format != ExcelFormat.XLSX)
			throw new IOException("Only XSLX supported for streaming imports");

		InputStream is = new FileInputStream(file);
		try {
			m_inputStream = is;
			m_format = format;

			m_workbook = StreamingReader.builder()
				.rowCacheSize(100)    // number of rows to keep in memory (defaults to 10)
				.bufferSize(4096)     // buffer size to use when reading InputStream to file (defaults to 1024)
				.open(is);
			DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(Locale.US);
			m_doubleFormatter = new DecimalFormat("#.#####", dfs);
			is = null;
		} finally {
			FileTool.closeAll(is);
		}
	}

	public ExcelStreamingRowReader(InputStream is, ExcelFormat format) throws Exception {
		m_inputStream = is;
		m_format = format;
		m_workbook = StreamingReader.builder()
			.rowCacheSize(100)    // number of rows to keep in memory (defaults to 10)
			.bufferSize(4096)     // buffer size to use when reading InputStream to file (defaults to 1024)
			.open(is);            // be aware that this closes is!
		DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(Locale.US);
		m_doubleFormatter = new DecimalFormat("#.#####", dfs);
	}

	/**
	 * When set all fields read by getValueAsString() will return this date format string
	 * if Excel has the idea the field was a date. This means that a call to getString()
	 * might return something that looks different in the Excel file itself!
	 */
	public ExcelStreamingRowReader forceStringDateFormat(String dateFormat) {
		m_forceStringDateFormat = new SimpleDateFormat(dateFormat);
		return this;
	}

	@Override
	public void setDateFormat(String dateFormat) {
		m_defaultDateFormat = new SimpleDateFormat(dateFormat);
	}

	@Override
	@Nullable
	public DateFormat getDefaultDateFormat() {
		return m_defaultDateFormat;
	}

	@Override
	public String convertDouble(double value) {
		return m_doubleFormatter.format(value);
	}

	@NonNull
	@Override
	public Iterator<IImportRow> iterator() {
		checkStart();
		try {
			return new RowIterator(getSheet(), m_currentSheetIterator, getCurrentHeaderNames());
		} catch(IOException ix) {
			throw WrappedException.wrap(ix);                // morons
		}
	}

	/**
	 * Reads the n header rows, if present. Leaves the current sheet at the 1st data row.
	 */
	private void readHeaderRows() {
		while(m_currentSheetRow < m_headerRowCount) {
			Iterator<Row> it = m_currentSheetIterator;
			if(it.hasNext()) {
				Row headerRow = it.next();
				ExcelImportRow xrow = new ExcelImportRow(this, headerRow, Collections.emptyList());
				m_headerRows.add(xrow);

				m_currentSheetRow++;
			}
		}
	}

	@Override
	public IImportRow getHeaderRow() {
		if(m_headerRowCount <= 0) {
			throw new IllegalStateException("You cannot ask for a header row when hasHeaderRow is false");
		}
		checkStart();
		ExcelImportRow row = m_headerRows.get(0);
		return row;
		//
		//return new ExcelImportRow(this, getSheet().getRow(getSheet().getFirstRowNum() + m_headerRowCount - 1), Collections.emptyList());
	}

	private void checkStart() {
		if(m_setIndex == -1) {
			setSetIndex(0);
		}
		readHeaderRows();
	}

	/**
	 * Returns the #of datasets, sheets in Excel files.
	 */
	@Override
	public int getSetCount() {
		return m_workbook.getNumberOfSheets();
	}

	@Override
	public void setSetIndex(int setIndex) {
		if(m_setIndex != setIndex) {
			Sheet sheet = m_currentSheet = m_workbook.getSheetAt(setIndex);
			m_setIndex = setIndex;
			m_progressIndicator = 0;
			m_currentSheetRow = 0;
			m_headerRows.clear();
			m_currentSheetIterator = sheet.iterator();
			readHeaderRows();
		}
	}

	@Override
	public List<IDatasetInfo> getSets() {
		List<IDatasetInfo> res = new ArrayList<>();
		for(int i = 0; i < getSetCount(); i++) {
			Sheet sheet = m_workbook.getSheetAt(i);
			res.add(new ExcelSheet(sheet.getSheetName(), i));
		}
		return res;
	}

	/**
	 * If the current sheet has a header (as defined with hasHeader) then this reads that 1st row
	 * and returns a list of header names indexed by column index. If no header names are available
	 * this returns the empty list.
	 */
	private List<String> getCurrentHeaderNames() throws IOException {
		if(m_headerRowCount <= 0) {
			return Collections.emptyList();
		}
		IImportRow row = getHeaderRow();
		if(null == row)
			return Collections.emptyList();

		List<String> res = new ArrayList<>();
		for(int i = 0; i < row.getColumnCount(); i++) {
			String name = row.get(i).getStringValue();
			if(null != name) {
				name = name.trim();
				if(name.isEmpty())
					name = null;
			}
			res.add(name);
		}
		return res;
	}

	private Sheet getSheet() {
		checkStart();
		return Objects.requireNonNull(m_currentSheet);
	}

	public String getSheetName() {
		return getSheet().getSheetName();
	}

	@Override
	public long getSetSizeIndicator() {
		return getSheet().getLastRowNum() /*- getSheet().getFirstRowNum() this one is not implemented */;
	}

	@Override
	public long getProgressIndicator() {
		return m_progressIndicator;
	}

	@Override
	public void close() throws IOException {
		InputStream is = m_inputStream;
		if(null != is) {
			is.close();
			m_inputStream = null;
		}
	}

	@Override
	public void setHasHeaderRow(boolean hasHeaderRow) {
		setHeaderRowCount(hasHeaderRow ? 1 : 0);
	}

	public void setHeaderRowCount(int count) {
		m_headerRowCount = count;
	}

	public int getHeaderRowCount() {
		return m_headerRowCount;
	}

	@Override
	public DateFormat getDateFormat(String dateFormat) {
		return m_dateFormatMap.computeIfAbsent(dateFormat, a -> {
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			sdf.setLenient(false);
			return sdf;
		});
	}

	@Override
	@Nullable
	public DateFormat getForceStringDateFormat() {
		return m_forceStringDateFormat;
	}

	private class RowIterator implements Iterator<IImportRow> {
		private final Sheet m_sheet;

		private final Iterator<Row> m_sheeterator;

		private final List<String> m_headerNames;

		public RowIterator(Sheet sheet, Iterator<Row> sheeterator, List<String> headerNames) {
			m_sheet = sheet;
			m_sheeterator = sheeterator;
			m_headerNames = headerNames;
		}

		@Override
		public boolean hasNext() {
			return m_sheeterator.hasNext();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public IImportRow next() {
			if(!hasNext())
				throw new IllegalStateException("Calling next() after hasNext() returned false");

			Row row = m_sheeterator.next();
			m_progressIndicator++;
			if(null == row)
				return new EmptyRow();
			return new ExcelImportRow(ExcelStreamingRowReader.this, row, m_headerNames);
		}
	}

	static private class ExcelSheet implements IDatasetInfo {
		private final String m_name;

		private final int m_index;

		public ExcelSheet(String name, int index) {
			m_name = name;
			m_index = index;
		}

		@Override
		public String getName() {
			return m_name;
		}

		@Override
		public int getIndex() {
			return m_index;
		}
	}

	private static class EmptyRow implements IImportRow {
		@Override
		public int getColumnCount() {
			return 0;
		}

		@NonNull
		@Override
		public IImportColumn get(int index) {
			return new EmptyColumn("Column" + index);
		}

		@NonNull
		@Override
		public IImportColumn get(@NonNull String name) throws IOException {
			return new EmptyColumn(name);
		}
	}


}
