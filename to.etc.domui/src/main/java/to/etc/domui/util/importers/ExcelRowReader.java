package to.etc.domui.util.importers;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import to.etc.domui.util.exporters.ExcelFormat;
import to.etc.util.FileTool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Objects;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-10-17.
 */
public class ExcelRowReader implements IRowReader, AutoCloseable, Iterable<IImportRow> {
	private final ExcelFormat m_format;

	private final Workbook m_workbook;

	private InputStream m_inputStream;

	private int m_setIndex = -1;

	private Sheet m_currentSheet;

	/** When set the first row read from a dataset is treated as a header row. */
	private boolean m_hasHeaderRow = true;

	private long m_progressIndicator;

	public ExcelRowReader(File file) throws Exception {
		String suffix = FileTool.getFileExtension(file.getName());
		ExcelFormat format = ExcelFormat.byExtension(suffix);
		if(null == format)
			throw new IOException("Excel format for file extension '" + suffix + "' is not found");

		InputStream is = new FileInputStream(file);
		m_inputStream = is;
		m_format = format;
		m_workbook = openWorkbook();
	}

	public ExcelRowReader(InputStream is, ExcelFormat format) throws Exception {
		m_inputStream = is;
		m_format = format;
		m_workbook = openWorkbook();
	}

	@NotNull @Override public Iterator<IImportRow> iterator() {
		checkStart();
		Sheet sheet = getSheet();
		if(m_hasHeaderRow) {
			return new RowIterator(sheet, sheet.getFirstRowNum() + 1);
		} else {
			return new RowIterator(sheet, sheet.getFirstRowNum());
		}
	}

	@Override public IImportRow getHeaderRow() {
		if(! m_hasHeaderRow)
			throw new IllegalStateException("You cannot ask for a header row when hasHeaderRow is false");
		return new ExcelImportRow(getSheet().getRow(getSheet().getFirstRowNum()));
	}

	private void checkStart() {
		if(m_setIndex == -1) {
			setSetIndex(0);
		}
	}

	private Workbook openWorkbook() throws IOException {
		switch(m_format) {
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
	@Override public int getSetCount() {
		return m_workbook.getNumberOfSheets();
	}

	@Override public void setSetIndex(int setIndex) {
		if(m_setIndex != setIndex) {
			Sheet sheet = m_currentSheet = m_workbook.getSheetAt(setIndex);
			m_setIndex = setIndex;
			m_progressIndicator = 0;
		}
	}

	private Sheet getSheet() {
		checkStart();
		return Objects.requireNonNull(m_currentSheet);
	}

	@Override public long getSetSizeIndicator() {
		return getSheet().getLastRowNum() - getSheet().getFirstRowNum();
	}

	@Override public long getProgressIndicator() {
		return m_progressIndicator;
	}

	@Override public void close() throws IOException {
		InputStream is = m_inputStream;
		if(null != is) {
			is.close();
			m_inputStream = null;
		}
	}

	public boolean isHasHeaderRow() {
		return m_hasHeaderRow;
	}

	@Override public void setHasHeaderRow(boolean hasHeaderRow) {
		m_hasHeaderRow = hasHeaderRow;
	}

	private class RowIterator implements Iterator<IImportRow> {
		private final Sheet m_sheet;

		/** The next row to read */
		private int m_nextRow;

		/** The last row we're expecting (inclusive) */
		private int m_lastRow;

		public RowIterator(Sheet sheet, int firstRowNum) {
			m_sheet = sheet;
			m_lastRow = sheet.getLastRowNum();
			m_nextRow = firstRowNum;
		}

		@Override public boolean hasNext() {
			return m_nextRow <= m_lastRow;
		}

		@Override public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override public IImportRow next() {
			if(! hasNext())
				throw new IllegalStateException("Calling next() after hasNext() returned false");
			Row row = m_sheet.getRow(m_nextRow++);
			m_progressIndicator++;
			return new ExcelImportRow(row);
		}
	}
}
