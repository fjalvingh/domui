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

	private SimpleDateFormat m_dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	private String m_recordSeparator = "\r\n";

	private QuoteMode m_quoteMode = QuoteMode.NON_NUMERIC;

	private char m_delimiter = ';';

	final private List<String> m_header = new ArrayList<>();

	static private final Locale DUTCH = new Locale("NL", "nl");

	private Locale m_locale = DUTCH;

	private boolean m_exportForXml;

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

	public CsvOptions dateFormat(String format) {
		m_dateFormat = new SimpleDateFormat(format);
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

	/**
	 * DOES NOT BELONG HERE Set when you want numbers to use comma instead of dot.
	 */
	@Deprecated
	public CsvOptions dutchLocale() {
		m_locale = DUTCH;
		return this;
	}

	/**
	 * Set when you want numbers to use dot instead of comma.
	 */
	public CsvOptions usLocale() {
		m_locale = Locale.US;
		return this;
	}

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

	public NumberFormat getNumberFormat() {
		NumberFormat instance = NumberFormat.getInstance(m_locale);
		instance.setGroupingUsed(false);
		return instance;
	}
}
