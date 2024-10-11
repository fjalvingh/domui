package to.etc.domui.util.importers;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-10-17.
 */
public class ExcelRowReader implements IRowReader, AutoCloseable, Iterable<IImportRow>, IExcelRowReader {
	private final ExcelFormat m_format;

	private final Workbook m_workbook;

	private InputStream m_inputStream;

	private int m_setIndex = -1;

	private Sheet m_currentSheet;

	private long m_progressIndicator;

	private int m_headerRowCount;

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

	public ExcelRowReader(File file) throws Exception {
		String suffix = FileTool.getFileExtension(file.getName());
		ExcelFormat format = ExcelFormat.byExtension(suffix);
		if(null == format)
			throw new IOException("Excel format for file extension '" + suffix + "' is not found");

		InputStream is = new FileInputStream(file);
		try {
			m_inputStream = is;
			m_format = format;
			m_workbook = openWorkbook();
			DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(Locale.US);
			m_doubleFormatter = new DecimalFormat("#.#####", dfs);
			is = null;
		} finally {
			FileTool.closeAll(is);
		}
	}

	public ExcelRowReader(InputStream is, ExcelFormat format) throws Exception {
		m_inputStream = is;
		m_format = format;
		m_workbook = openWorkbook();
		DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(Locale.US);
		m_doubleFormatter = new DecimalFormat("#.#####", dfs);
	}

	/**
	 * When set all fields read by getValueAsString() will return this date format string
	 * if Excel has the idea the field was a date. This means that a call to getString()
	 * might return something that looks different in the Excel file itself!
	 */
	public ExcelRowReader forceStringDateFormat(String dateFormat) {
		m_forceStringDateFormat = new SimpleDateFormat(dateFormat);
		return this;
	}

	@Override
	public void setDateFormat(String dateFormat) {
		m_defaultDateFormat = new SimpleDateFormat(dateFormat);
	}

	@Override
	public DateFormat getDateFormat(String dateFormat) {
		return m_dateFormatMap.computeIfAbsent(dateFormat, a -> {
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			sdf.setLenient(false);
			return sdf;
		});
	}

	@Nullable
	@Override
	public DateFormat getForceStringDateFormat() {
		return m_forceStringDateFormat;
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
		Sheet sheet = getSheet();
		try {
			return new RowIterator(sheet, sheet.getFirstRowNum() + m_headerRowCount, getCurrentHeaderNames());
		} catch(IOException ix) {
			throw WrappedException.wrap(ix);                // morons
		}
	}

	@Override
	public IImportRow getHeaderRow() {
		if(m_headerRowCount <= 0)
			throw new IllegalStateException("You cannot ask for a header row when hasHeaderRow is false");
		return new ExcelImportRow(this, getSheet().getRow(getSheet().getFirstRowNum() + m_headerRowCount - 1), Collections.emptyList());
	}

	private void checkStart() {
		if(m_setIndex == -1) {
			setSetIndex(0);
		}
	}

	private Workbook openWorkbook() throws IOException {
		switch(m_format){
			default:
				throw new IllegalStateException("Unhandled Excel format " + m_format);
			case XLS:
				return new HSSFWorkbook(m_inputStream);

			case XLSX:
				return new XSSFWorkbook(m_inputStream);
		}
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
		return getSheet().getLastRowNum() - getSheet().getFirstRowNum();
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

	private class RowIterator implements Iterator<IImportRow> {
		private final Sheet m_sheet;

		/** The next row to read */
		private int m_nextRow;

		private final List<String> m_headerNames;

		/** The last row we're expecting (inclusive) */
		private int m_lastRow;

		public RowIterator(Sheet sheet, int firstRowNum, List<String> headerNames) {
			m_sheet = sheet;
			m_lastRow = sheet.getLastRowNum();
			m_nextRow = firstRowNum;
			m_headerNames = headerNames;
		}

		@Override
		public boolean hasNext() {
			return m_nextRow <= m_lastRow;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public IImportRow next() {
			if(!hasNext())
				throw new IllegalStateException("Calling next() after hasNext() returned false");
			Row row = m_sheet.getRow(m_nextRow++);
			m_progressIndicator++;
			if(null == row)
				return new EmptyRow();
			return new ExcelImportRow(ExcelRowReader.this, row, m_headerNames);
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

		@Override
		public boolean isEmpty() {
			return true;
		}
	}

}
