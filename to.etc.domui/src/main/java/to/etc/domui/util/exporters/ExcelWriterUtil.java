package to.etc.domui.util.exporters;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Utils around cells styles and fonts, since these should be made cached and reusable inside workbook.
 */
@NonNullByDefault
public class ExcelWriterUtil {

	@Nullable
	private Font m_defaultFont;

	public enum FontStyle {
		BOLD("B"),
		ITALIC("I"),
		UNDERLINE("U"),
		Strikeout("S");

		private String m_code;
		FontStyle(String code) {
			m_code = code;
		}
		public String getCode() {
			return m_code;
		}
	}

	private final ExcelFormat m_format;

	private final Workbook m_workbook;

	private final Map<String, CellStyle> m_styles = new HashMap<>();

	private final Map<String, Font> m_fonts = new HashMap<>();

	private boolean m_autoSizeCols = true;

	public ExcelWriterUtil(ExcelFormat format, Workbook workbook) {
		m_format = format;
		m_workbook = workbook;
	}

	public CellStyle errorCs() {
		String key = "error";
		CellStyle cs = style(key);
		if(null == cs) {
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

	/**
	 * Default style with default font.
	 * @return
	 */
	public CellStyle defaultCs() {
		String key = "default";
		CellStyle cs = m_styles.get(key);
		if(null == cs) {
			cs = createCellStyle();
			cs.setFont(cloneFromDefault());
			addStyle(key, cs);
		}
		return cs;
	}

	/**
	 * Default style with bold font.
	 * @return
	 */
	public CellStyle boldCs() {
		String key = "bold";
		CellStyle cs = m_styles.get(key);
		if(null == cs) {
			cs = createCellStyle();
			cs.setFont(boldFont());
			addStyle(key, cs);
		}
		return cs;
	}

	public CellStyle wrapTextCs() {
		String key = "wrapText";
		CellStyle cs = m_styles.get(key);
		if(null == cs) {
			cs = createCellStyle();
			addStyle(key, cs);
		}
		return cs;
	}

	/**
	 * Custom color background.
	 * @return
	 */
	public CellStyle colorBk(Color color, @Nullable IndexedColors alternative) {
		return customCs(color, alternative, false);
	}

	/**
	 * Custom style, enables setting the rich color (awt) in case of xssf model in use, or fallback to predefined indexed color in case of less rich hssf model.
	 * Also enables standard font decoration and setting the wrap text option.
	 * @param xssfColor java.awt.Color that is used as template for XSSFColor that is constructed in case that XSSF model is in use
	 * @param hssfColor alternative hssfColor in case that less rich HSSF model is in use
	 * @param wrapText wraps the text if T
	 * @param fontStyles set of font styles to be applied to this cell style
	 * @return
	 */
	public CellStyle customCs(Color xssfColor, @Nullable IndexedColors hssfColor, boolean wrapText, FontStyle... fontStyles) {
		String fontKey = fontKey(fontStyles);
		String key = "xssf" + xssfColor + "hssf" + (null != hssfColor ? hssfColor.index : "") + wrapText + fontKey;
		CellStyle cs = style(key);
		if(null == cs) {
			cs = createCellStyle();
			boolean hasBackgroundFill = false;
			if(cs instanceof XSSFCellStyle) {
				XSSFCellStyle xssfcs = (XSSFCellStyle) cs;
				cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				xssfcs.setFillForegroundColor(new XSSFColor(xssfColor));
				XSSFColor borderColor = new XSSFColor(Color.LIGHT_GRAY);
				xssfcs.setBottomBorderColor(borderColor);
				xssfcs.setLeftBorderColor(borderColor);
				xssfcs.setRightBorderColor(borderColor);
				xssfcs.setTopBorderColor(borderColor);
				hasBackgroundFill = true;
			}else if(null != hssfColor) {
				cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				cs.setFillForegroundColor(hssfColor.index);
				cs.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.index);
				cs.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.index);
				cs.setRightBorderColor(IndexedColors.GREY_25_PERCENT.index);
				cs.setTopBorderColor(IndexedColors.GREY_25_PERCENT.index);
				hasBackgroundFill = true;
			}
			if(hasBackgroundFill) {
				cs.setBorderTop(BorderStyle.THIN);
				cs.setBorderBottom(BorderStyle.THIN);
				cs.setBorderLeft(BorderStyle.THIN);
				cs.setBorderRight(BorderStyle.THIN);
			}
			Font font = fontFor(fontStyles);
			cs.setFont(font);
			cs.setWrapText(wrapText);
			addStyle(key, cs);
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

	public Font fontFor(FontStyle... fontStyles) {
		String key = fontKey(fontStyles);
		Font font = font(key);
		if(null == font) {
			font = cloneFromDefault();
			for (FontStyle fs: fontStyles) {
				switch (fs) {
					case BOLD: font.setBold(true); break;
					case ITALIC: font.setItalic(true); break;
					case UNDERLINE: font.setUnderline(Font.U_SINGLE); break;
					case Strikeout: font.setStrikeout(true); break;
				}
			}
			addFont(key, font);
		}
		return font;
	}

	public Font boldFont() {
		return fontFor(FontStyle.BOLD);
	}

	@Nullable
	public Font font(String key) {
		return m_fonts.get(key);
	}

	public void addFont(String key, Font font) {
		m_fonts.put(key, font);
	}

	public Font cloneFromDefault() {
		Font defaultFont = m_defaultFont;
		if(defaultFont == null) {
			defaultFont = m_defaultFont = m_workbook.getFontAt(0);
		}
		Font font = m_workbook.createFont();
		font.setFontHeightInPoints(defaultFont.getFontHeightInPoints());
		font.setFontName(defaultFont.getFontName());
		font.setColor(defaultFont.getColor());
		return font;
	}

	public void setDefaultFont(String fontName, short fontHeightInPoints) {
		setDefaultFont(fontName, fontHeightInPoints, Font.COLOR_NORMAL);
	}

	public void setDefaultFont(String fontName, short fontHeightInPoints, short fontColor) {
		Font font = m_workbook.createFont();
		font.setFontHeightInPoints(fontHeightInPoints);
		font.setFontName(fontName);
		font.setColor(fontColor);
		m_defaultFont = font;
	}

	private String fontKey(FontStyle... fontStyles) {
		if(null == fontStyles) {
			return "normal";
		}
		String fontKey = "fs";
		if(null != fontStyles) {
			for (FontStyle fs: fontStyles) {
				fontKey += fs.getCode();
			}
		}
		return fontKey;
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

	public void autoSizeCols(int from, int to, @Nullable Integer maxWidthPx) {
		Iterator<Sheet> sheetIterator = m_workbook.sheetIterator();
		while (sheetIterator.hasNext()) {
			Sheet sheet = sheetIterator.next();
			for(int index = from; index < to; index++) {
				sheet.autoSizeColumn(index, true);
				if(null != maxWidthPx) {
					int colWidth = sheet.getColumnWidth(index);
					if(colWidth > maxWidthPx) {
						sheet.setColumnWidth(index, maxWidthPx);
						sheet.getColumnStyle(index).setWrapText(true);
					}
				}
			}
		}
	}
}
