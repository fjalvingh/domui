package to.etc.domui.util.exporters;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.util.WrappedException;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

/**
 * Denotes the supported Excel formats.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-10-17.
 */
public enum ExcelFormat {
	XLSX("xlsx", "Microsoft Office Excel (xlsx)", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", () -> new XSSFWorkbook(), 1048575)
	, XLS("xls", "Microsoft Office Excel (xls)", "application/vnd.ms-excel", () -> new HSSFWorkbook(), 65535)
	;

	private final String m_description;
	private final String m_suffix;
	private final String m_mimeType;
	private final Supplier<Workbook> m_workbookFactory;
	private final int m_maxRowsLimit;

	ExcelFormat(String suffix, String description, String mimeType, Supplier<Workbook> workbookFactory, int maxRowsLimit) {
		m_description = description;
		m_suffix = suffix;
		m_mimeType = mimeType;
		m_workbookFactory = workbookFactory;
		m_maxRowsLimit = maxRowsLimit;
	}

	public String getDescription() {
		return m_description;
	}

	public String getSuffix() {
		return m_suffix;
	}

	public String getMimeType() {
		return m_mimeType;
	}

	@Nullable
	static public ExcelFormat	byExtension(String ext) {
		if(ext.startsWith("."))
			ext = ext.substring(1);
		for(ExcelFormat excelFormat : values()) {
			if(excelFormat.getSuffix().equalsIgnoreCase(ext))
				return excelFormat;
		}
		return null;
	}

	public Workbook createWorkbook() {
		return m_workbookFactory.get();
	}

	public Workbook createWorkbook(InputStream is) {
		try {
			return WorkbookFactory.create(is);
		} catch(IOException e) {
			throw WrappedException.wrap(e);
		}
	}

	public int getMaxRowsLimit() {
		return m_maxRowsLimit;
	}
}
