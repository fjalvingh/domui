package to.etc.domui.util.exporters;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Denotes the supported Excel formats.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 26-10-17.
 */
public enum ExcelFormat {
	XLSX("xlsx", "Microsoft Office Excel (xlsx)", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
	, XLS("xls", "Microsoft Office Excel (xls)", "application/vnd.ms-excel")
	;

	private final String m_description;
	private final String m_suffix;
	private final String m_mimeType;

	ExcelFormat(String suffix, String description, String mimeType) {
		m_description = description;
		m_suffix = suffix;
		m_mimeType = mimeType;
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

	public static Workbook getWorkbook(ExcelFormat excelFileType) {
		switch(excelFileType){
			case XLS:
				return new HSSFWorkbook();
			case XLSX:
				return new XSSFWorkbook();
			default:
				throw new IllegalArgumentException("Unsupported Excel file type: " + excelFileType);
		}
	}

	public static int getRowsLimit(ExcelFormat excelFileType) {
		switch(excelFileType){
			case XLS:
				return 65535;
			case XLSX:
				return 1048575;
			default:
				throw new IllegalArgumentException("Unsupported Excel file type: " + excelFileType);
		}
	}
}
