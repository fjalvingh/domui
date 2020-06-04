package to.etc.domui.util.exporters;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

@NonNullByDefault
public class ExcelWriterUtil {

	private final ExcelFormat m_format;

	private final Workbook m_workbook;

	private final Map<String, CellStyle> m_styles = new HashMap<>();

	private final Map<String, Font> m_fonts = new HashMap<>();

	public ExcelWriterUtil(ExcelFormat format, Workbook workbook) {
		m_format = format;
		m_workbook = workbook;
	}

	private boolean m_autoSizeCols = true;

	public CellStyle errorCs() {
		String key = "error";
		CellStyle cs = m_styles.get(key);
		if (null == cs) {
			cs = m_workbook.createCellStyle();
			cs.setAlignment(HorizontalAlignment.LEFT);
			cs.setIndention((short) 1);
			cs.setFillForegroundColor(IndexedColors.RED.getIndex());
			cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			cs.setWrapText(false);
			Font font = cloneFromDefault();
			font.setItalic(true);
			font.setColor(IndexedColors.DARK_RED.getIndex());
			cs.setFont(font);
			m_styles.put(key, cs);
		}
		return cs;
	}

	@Nullable
	public CellStyle style(String key) {
		return m_styles.get(key);
	}

	public void addStyle(String key, CellStyle style) {
		m_styles.put(key, style);
	}

	@Nullable
	public Font font(String key) {
		return m_fonts.get(key);
	}

	public void addFont(String key, Font font) {
		m_fonts.put(key, font);
	}

	public Font cloneFromDefault() {
		Font font = m_workbook.createFont();
		Font defaultFont = m_workbook.getFontAt((short) 0);
		font.setFontHeightInPoints(defaultFont.getFontHeightInPoints());
		font.setFontName(defaultFont.getFontName());
		font.setColor(defaultFont.getColor());
		return font;
	}

	public Font boldFont() {
		String key = "bold";
		Font font = font(key);
		if (null == font) {
			font = cloneFromDefault();
			font.setBold(true);
			addFont(key, font);
		}
		return font;
	}

	public CellStyle createCellStyle() {
		return m_workbook.createCellStyle();
	}

	public boolean isAutoSizeCols() {
		return m_autoSizeCols;
	}

	public void setAutoSizeCols(boolean autoSizeCols) {
		this.m_autoSizeCols = autoSizeCols;
	}
}
