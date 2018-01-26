package to.etc.domui.util.exporters;

import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import to.etc.domui.component.meta.MetaManager;
import to.etc.webapp.nls.NlsContext;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This writer exports data in Excel XLS or XLSX format.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-10-17.
 */
@DefaultNonNull
public class ExcelExportWriter<T> implements IExportWriter<T> {
	private static final int EXCEL_CHAR_WIDTH = 256;

	private final ExcelFormat m_format;

	private int m_sheetIndex;

	private int m_nextSheetIndex;

	@Nullable
	private Workbook m_workbook;

	/** The current index for all sheet rows. */
	private int[] m_sheetRowIndex = new int[256];

	private List<Sheet> m_sheetList = new ArrayList<>();

	@Nullable
	private CellStyle m_currencyStyle;

	@Nullable
	private CellStyle m_numberStyle;

	@Nullable
	private CellStyle m_headerStyle;

	@Nullable
	private CellStyle m_dateStyle;

	@Nullable
	private CellStyle m_defaultCellStyle;

	private int m_maxRows;

	private List<? extends IExportColumn<?>> m_columnList = Collections.emptyList();

	@Nullable
	private File m_target;

	private boolean m_started;

	public ExcelExportWriter(ExcelFormat format, File target) {
		m_target = target;
		m_format = format;
		Arrays.fill(m_sheetRowIndex, -1);
	}

	@Override public void startExport(List<? extends IExportColumn<?>> columnList) throws Exception {
		if(m_started)
			throw new IllegalArgumentException("The writer was already started");
		m_started = true;
		m_columnList = columnList;
		Workbook wb = m_workbook = createWorkbook();
		Font defaultFont = wb.createFont();
		defaultFont.setFontHeightInPoints((short) 10);
		defaultFont.setFontName("Arial");

		CellStyle dcs = m_defaultCellStyle = wb.createCellStyle();
		dcs.setFont(defaultFont);

		// FIXME Date format must be locale dependent?
		CellStyle dates = m_dateStyle = wb.createCellStyle();
		dates.setDataFormat(wb.createDataFormat().getFormat("d-m-yyyy"));
		dates.setFont(defaultFont);

		CellStyle curs = m_currencyStyle = wb.createCellStyle();
		curs.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
		curs.setFont(defaultFont);

		CellStyle nums = m_numberStyle = wb.createCellStyle();
		nums.setDataFormat(wb.createDataFormat().getFormat("#0"));
		nums.setFont(defaultFont);

		Font headerFont = wb.createFont();
		headerFont.setFontHeightInPoints((short) 10);
		headerFont.setColor((short) 0xc);
		headerFont.setBold(true);
		headerFont.setFontName("Arial");

		CellStyle hds = m_headerStyle = wb.createCellStyle();
		hds.setBorderBottom(BorderStyle.THIN);
		hds.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
		hds.setFont(headerFont);

		createNewSheet(columnList);
	}

	@Override public void exportRow(T record) throws Exception {
		Row row = addRow();

		for(int i = 0; i < m_columnList.size(); i++) {
			renderColumn(row, i, record);
		}
	}

	private void renderColumn(Row row, int columnIndex, T record) throws Exception {
		Cell cell = row.createCell(columnIndex);
		IExportColumn<?> columnInfo = m_columnList.get(columnIndex);
		Object value = columnInfo.getValue(record);
		Object convertedValue = columnInfo.convertValue(value);
		if(null == convertedValue) {
			cell.setCellStyle(m_defaultCellStyle);
			return;
		}

		IExportCellRenderer<ExcelExportWriter<?>, Cell, Object> renderer = (IExportCellRenderer<ExcelExportWriter<?>, Cell, Object>) columnInfo.getRenderer();
		if(null == renderer) {
			renderer = findRenderer(columnInfo.getActualType());
			if(null == renderer) {
				renderer = DEFAULT_RENDERER;
			}
		}

		renderer.renderCell(this, cell, columnIndex, value);
	}

	@Override public void close() throws Exception {
		if(! m_started)
			return;

		try(OutputStream out = new FileOutputStream(Objects.requireNonNull(m_target))) {
			getWorkbook().write(out);
		}
	}

	private Workbook createWorkbook() {
		switch(m_format) {
			default:
				throw new IllegalStateException("Excel format not implemented: " + m_format);

			case XLS:
				m_maxRows = 65535;
				return new HSSFWorkbook();

			case XLSX:
				m_maxRows = 1024*1024-1;
				return new SXSSFWorkbook();
		}
	}

	@Override public String getMimeType() {
		switch(m_format) {
			default:
				throw new IllegalStateException("Excel format not implemented: " + m_format);

			case XLS:
				return "application/x-xls";

			case XLSX:
				return "appication/x-xlsx";
		}
	}

	@Override public int getRowLimit() {
		switch(m_format) {
			default:
				throw new IllegalStateException("Excel format not implemented: " + m_format);

			case XLS:
				return 65535;

			case XLSX:
				return 1024*1024-1;
		}
	}

	private Row addRow() {
		int index = getRowIndex() + 1;
		Row row = getSheet().createRow(index);
		setRowIndex(index);
		return row;
	}

	protected Sheet createNewSheet(List<? extends IExportColumn<?>> columnList) {
		Sheet s = createSheet();
		s.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);
		s.getPrintSetup().setLandscape(true);
		renderHeader(columnList, s);
		return s;
	}

	private Sheet createSheet() {
		Sheet s = getWorkbook().createSheet();
		m_sheetList.add(s);
		getWorkbook().setSheetName(m_nextSheetIndex, "Data" + m_nextSheetIndex);
		m_sheetIndex = m_nextSheetIndex++;
		return s;
	}

	private Sheet getSheet() {
		return m_sheetList.get(m_sheetIndex);
	}

	protected void renderHeader(List<? extends IExportColumn<?>> itemlist, Sheet s) {
		int index = getRowIndex();
		Row r = s.createRow(++index);
		setRowIndex(index);
		int cellnum = 0;
		for(IExportColumn<?> formItem : itemlist) {
			renderHeaderCell(formItem, r, s, cellnum);
			cellnum++;
		}
	}

	void renderHeaderCell(IExportColumn<?> column, Row row, Sheet s, int cellnum) {
		Cell cell = row.createCell(cellnum);
		cell.setCellStyle(m_headerStyle);
		String label = column.getLabel();
		cell.setCellValue(label);
		setCellWidth(s, cellnum, label, true);
	}

	private void setCellWidth(Sheet sheet, int cellIndex, @Nullable Object value, boolean bold) {
		if(null == value)
			return;
		int numOfChars = value.toString().length();
		int cellWidht = calculateWidth(numOfChars);
		if(bold)
			cellWidht = (int) Math.round(cellWidht * 1.35);
		if(cellWidht > sheet.getColumnWidth(cellIndex))
			sheet.setColumnWidth(cellIndex, cellWidht);
	}

	void setCellWidth(int cellIndex, @Nullable Object value) {
		setCellWidth(getSheet(), cellIndex, value, false);
	}

	/**
	 * Get a reasonable width for the columns and convert to Excel units.
	 */
	static private int calculateWidth(int numOfChars) {
		if(numOfChars > 250)
			return 250 * EXCEL_CHAR_WIDTH;
		if(numOfChars < 10)
			numOfChars = 10;
		return numOfChars * EXCEL_CHAR_WIDTH;
	}

	public Workbook getWorkbook() {
		return Objects.requireNonNull(m_workbook);
	}

	/**
	 * Return the row index in the current sheet.
	 */
	private int getRowIndex() {
		return m_sheetRowIndex[m_sheetIndex];
	}

	private void setRowIndex(int index) {
		m_sheetRowIndex[m_sheetIndex] = index;
	}

	/**
	 * Registers all possible renderers for a type.
	 */
	static private final Map<Class<?>, IExportCellRenderer<ExcelExportWriter<?>, Cell, ?>> m_renderMap = new ConcurrentHashMap<>();

	static public void register(Class<?> clz, IExportCellRenderer<ExcelExportWriter<?>, Cell, ?> renderer) {
		m_renderMap.put(clz, renderer);
	}

	static public <V> IExportCellRenderer<ExcelExportWriter<?>, Cell, V> findRenderer(Class<?> clz) {
		if(Enum.class.isAssignableFrom(clz)) {
			return (IExportCellRenderer<ExcelExportWriter<?>, Cell, V>) ENUM_RENDERER;
		}

		IExportCellRenderer<ExcelExportWriter<?>, Cell, ?> r = m_renderMap.get(clz);
		return (IExportCellRenderer<ExcelExportWriter<?>, Cell, V>) r;
	}

	static private final IExportCellRenderer<ExcelExportWriter<?>, Cell, Number> NUMBER_CONVERTER = new IExportCellRenderer<ExcelExportWriter<?>, Cell, Number>() {
		@Override public void renderCell(ExcelExportWriter<?> exporter, Cell cell, int cellIndex, @Nullable Number value) throws Exception {
			cell.setCellStyle(exporter.m_numberStyle);
			if(null != value) {
				cell.setCellValue(value.doubleValue());
			}
			exporter.setCellWidth(cellIndex, String.valueOf(value));
		}
	};

	static private final IExportCellRenderer<ExcelExportWriter<?>, Cell, Date> DATE_CONVERTER = new IExportCellRenderer<ExcelExportWriter<?>, Cell, Date>() {
		@Override public void renderCell(ExcelExportWriter<?> exporter, Cell cell, int cellIndex, @Nullable Date value) throws Exception {
			cell.setCellStyle(exporter.m_dateStyle);
			if(null != value) {
				cell.setCellValue(value);
			}
			exporter.setCellWidth(cellIndex, value);
		}
	};

	static private final IExportCellRenderer<ExcelExportWriter<?>, Cell, Object> DEFAULT_RENDERER = new IExportCellRenderer<ExcelExportWriter<?>, Cell, Object>() {
		@Override public void renderCell(ExcelExportWriter<?> w, Cell cell, int cellIndex, @Nullable Object value) throws Exception {
			String string = String.valueOf(value);
			cell.setCellValue(string);
			cell.setCellStyle(w.m_defaultCellStyle);
			w.setCellWidth(cellIndex, string);
		}
	};

	static private final IExportCellRenderer<ExcelExportWriter<?>, Cell, Enum<?>> ENUM_RENDERER = new IExportCellRenderer<ExcelExportWriter<?>, Cell, Enum<?>>() {
		@Override public void renderCell(ExcelExportWriter<?> exporter, Cell cell, int cellIndex, @Nullable Enum<?> value) throws Exception {
			cell.setCellStyle(exporter.m_defaultCellStyle);
			if(null != value) {
				String text = MetaManager.findClassMeta(value.getClass()).getDomainLabel(NlsContext.getLocale(), value);
				cell.setCellValue(text);
				exporter.setCellWidth(cellIndex, text);
			}
		}
	};

	static {
		register(byte.class, NUMBER_CONVERTER);
		register(short.class, NUMBER_CONVERTER);
		register(int.class, NUMBER_CONVERTER);
		register(long.class, NUMBER_CONVERTER);
		register(float.class, NUMBER_CONVERTER);
		register(double.class, NUMBER_CONVERTER);

		register(Byte.class, NUMBER_CONVERTER);
		register(Short.class, NUMBER_CONVERTER);
		register(Integer.class, NUMBER_CONVERTER);
		register(Long.class, NUMBER_CONVERTER);
		register(Float.class, NUMBER_CONVERTER);
		register(Double.class, NUMBER_CONVERTER);

		register(BigDecimal.class, NUMBER_CONVERTER);
		register(BigInteger.class, NUMBER_CONVERTER);

		register(Date.class, DATE_CONVERTER);
		register(java.sql.Date.class, DATE_CONVERTER);
	}
}
