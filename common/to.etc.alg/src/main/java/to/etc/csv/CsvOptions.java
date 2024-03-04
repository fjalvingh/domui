package to.etc.csv;

import org.eclipse.jdt.annotation.NonNullByDefault;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 9-5-17.
 */
@NonNullByDefault
final public class CsvOptions {
	public enum QuoteMode {
		ALL,
		MINIMAL,
		NON_NUMERIC,
		NONE
	}

	private char m_quote = '"';

	private char m_escape;

	private String m_dateFormatString = "yyyy-MM-dd";

	private SimpleDateFormat m_dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	private String m_dateTimeFormatString = "yyyy-MM-dd HH:mm:ss";

	private SimpleDateFormat m_dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private String m_recordSeparator = "\r\n";

	private QuoteMode m_quoteMode = QuoteMode.NON_NUMERIC;

	private char m_delimiter = ';';

	final private List<String> m_header = new ArrayList<>();

	public static final Locale DUTCH = new Locale("NL", "nl");

	/**
	 * The locale for dates.
	 */
	final private Locale m_locale;

	/**
	 * Should be used only when
	 */
	private NumberFormat m_numberFormat = NumberFormat.getInstance();

	private boolean m_exportForXml;

	private boolean m_decimalPointIsComma;

	public char getQuote() {
		return m_quote;
	}

	public DateFormat getDateFormat() {
		return m_dateFormat;
	}

	public String getRecordSeparator() {
		return m_recordSeparator;
	}

	public QuoteMode getQuoteMode() {
		return m_quoteMode;
	}

	public char getDelimiter() {
		return m_delimiter;
	}

	private CsvOptions(Locale locale) {
		m_locale = locale;
	}

	static public CsvOptions create(Locale locale) {
		return new CsvOptions(locale)
			.dateFormat("yyyy-MM-dd")
			.dateTimeFormat("yyyy-MM-dd HH:mm:ss")
			;
	}

	/**
	 * Create a writer using US locale (english dates, decimal point is point).
	 */
	static public CsvOptions create() {
		return create(Locale.US);
	}

	//static public CsvOptions createDutch() {
	//	return create(DUTCH)
	//		.decimalPointIsComma(true)
	//		;
	//}

	public CsvOptions header(String... hdr) {
		for(String s : hdr) {
			m_header.add(s);
		}
		return this;
	}

	public CsvOptions header(List<String> header) {
		m_header.addAll(header);
		return this;
	}

	public CsvOptions quote(char quote) {
		m_quote = quote;
		return this;
	}

	public CsvOptions escape(char c) {
		m_escape = c;
		return this;
	}

	/**
	 * Sets the decimal point for numbers to dot (false) or comma (true).
	 */
	public CsvOptions decimalPointIsComma(boolean yes) {
		m_decimalPointIsComma = yes;
		m_numberFormat = yes
			? NumberFormat.getNumberInstance(DUTCH)
			: NumberFormat.getNumberInstance(Locale.US);
		m_numberFormat.setGroupingUsed(false);
		return this;
	}

	/**
	 * Set the format for a field that is supposed to hold a date only value.
	 */
	public CsvOptions dateFormat(String format) {
		m_dateFormatString = format;
		m_dateFormat = new SimpleDateFormat(format, m_locale);
		return this;
	}

	public CsvOptions dateTimeFormat(String format) {
		m_dateTimeFormatString = format;
		m_dateTimeFormat = new SimpleDateFormat(format, m_locale);
		return this;
	}

	public CsvOptions recordSeparator(String rs) {
		m_recordSeparator = rs;
		return this;
	}

	public CsvOptions quoteMode(QuoteMode mode) {
		m_quoteMode = mode;
		return this;
	}

	public CsvOptions delimiter(char d) {
		m_delimiter = d;
		return this;
	}

	/**
	 * DOES NOT BELONG HERE.
	 */
	@Deprecated
	public CsvOptions exportForXml(boolean b) {
		m_exportForXml = b;
		return this;
	}

	//public CsvOptions numberLocale(@NonNull Locale locale) {
	//	m_locale = locale;
	//	return this;
	//}
	//
	///**
	// * DOES NOT BELONG HERE Set when you want numbers to use comma instead of dot.
	// */
	//@Deprecated
	//public CsvOptions dutchNumberLocale() {
	//	m_locale = DUTCH;
	//	return this;
	//}
	//
	///**
	// * Set when you want numbers to use dot instead of comma.
	// */
	//public CsvOptions usNumberLocale() {
	//	m_locale = Locale.US;
	//	return this;
	//}

	/**
	 * DOES NOT BELONG HERE.
	 */
	@Deprecated
	public boolean isExportForXml() {
		return m_exportForXml;
	}


	public List<String> getHeader() {
		return m_header;
	}

	public char getEscape() {
		return m_escape;
	}

	public SimpleDateFormat getDateTimeFormat() {
		return m_dateTimeFormat;
	}

	public boolean isDecimalPointIsComma() {
		return m_decimalPointIsComma;
	}

	//public NumberFormat getNumberFormat() {
	//	return m_numberFormat;
	//}
}
