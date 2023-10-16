package to.etc.csv;

import to.etc.util.FileTool;
import to.etc.util.StringTool;

import java.io.Reader;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.List;

/**
 * Writes values in CSV format, according to specific options.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 9-5-17.
 */
public class CsvWriter implements AutoCloseable {
	final private CsvOptions m_options;

	final private Appendable m_output;

	private boolean m_headerWritten;

	private int m_columnIndex;

	public CsvWriter(Appendable output, CsvOptions options) {
		m_options = options;
		m_output = output;
	}

	public void print(ResultSet rs) throws Exception {
		ResultSetMetaData md = rs.getMetaData();
		while(rs.next()) {
			printRow(rs, md);
		}
	}

	public void printRow(ResultSet rs, ResultSetMetaData md) throws Exception {
		for(int column = 1; column <= md.getColumnCount(); column++) {
			printColumn(rs, md, column);
		}
		println();
	}

	public void printColumn(ResultSet rs, ResultSetMetaData metaData, int column) throws Exception {
		int sqlType = metaData.getColumnType(column);
		switch(sqlType){
			case Types.DECIMAL:
			case Types.NUMERIC:
			case Types.BIGINT:
			case Types.DOUBLE:
			case Types.FLOAT:
			case Types.INTEGER:
			case Types.REAL:
			case Types.SMALLINT:
			case Types.TINYINT:
				Object value = rs.getObject(column);
				if(value instanceof Number) {
					printNumber((Number) value);
				} else if(value == null) {
					printNull();
				} else {
					printString(String.valueOf(value));
				}
				break;
			case Types.CLOB:
				Clob val = rs.getClob(column);
				if(val == null) {
					printNull();
				} else {
					try(Reader characterStream = val.getCharacterStream()) {
						StringWriter writer = new StringWriter();
						FileTool.copyFile(writer, characterStream);
						printString(writer.toString());
					}
				}
				break;
			default:
				value = rs.getObject(column);
				if(null == value) {
					printNull();
				} else {
					printString(String.valueOf(value));
				}
				break;

			case Types.DATE:
			case Types.TIMESTAMP:
			case Types.TIMESTAMP_WITH_TIMEZONE:
				Timestamp ts = rs.getTimestamp(column);
				printDate(ts);
				break;
		}
	}

	public void printDate(Date date) throws Exception {
		if(null == date) {
			printNull();
			return;
		}
		String text = m_options.getDateFormat().format(date);
		printString(text);
	}

	public void printNumber(Number value) throws Exception {
		if(null == value) {
			printNull();
			return;
		}
		String text = m_options.getNumberFormat().format(value);
		switch(m_options.getQuoteMode()){
			default:
				throw new IllegalStateException(m_options.getQuoteMode() + ": unhandled");

			case ALL:
				printString(text);
				break;

			case MINIMAL:
			case NON_NUMERIC:
			case NONE:
				append(text);
				break;
		}
	}

	public void printNull() throws Exception {
		append("");
	}

	public void printString(String value) throws Exception {
		if(null == value) {
			printNull();
			return;
		}

		// FIXME This does not belong here. Remove and solve properly.
		if(m_options.isExportForXml()) {
			value = StringTool.xmlStringize(value);
		}
		switch(m_options.getQuoteMode()){
			default:
				throw new IllegalStateException(m_options.getQuoteMode() + ": unhandled");

			case NON_NUMERIC:
			case ALL:
				printStringEscapedOrQuoted(value);
				break;

			case MINIMAL:
				if(needsQuoting(value)) {
					printStringEscapedOrQuoted(value);
				} else {
					append(value);
				}
				break;

			case NONE:
				append(value);
				break;
		}
	}

	private boolean needsQuoting(String value) {
		if(value.indexOf(m_options.getQuote()) != -1)
			return true;
		if(value.indexOf(m_options.getDelimiter()) != -1)
			return true;
		return false;
	}

	private StringBuilder m_sb = new StringBuilder();

	/**
	 * Print a String, as defined by the format.
	 */
	private void printStringEscapedOrQuoted(String value) throws Exception {
		StringBuilder sb = m_sb;
		sb.setLength(0);
		char quote = m_options.getQuote();
		char escape = m_options.getEscape();
		if(0 != quote) {
			sb.append(quote);
		}

		for(int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if(quote != 0 && c == quote) {
				if(0 != escape) {
					sb.append(escape);
				} else {
					sb.append(quote);
				}
			} else if(escape != 0 && quote == 0 && c == m_options.getDelimiter()) {
				sb.append(escape);
			} else if(escape != 0 && c == escape) {
				sb.append(escape);
			}
			sb.append(c);
		}
		if(0 != quote) {
			sb.append(quote);
		}
		append(sb);
	}

	public void println() throws Exception {
		m_output.append(m_options.getRecordSeparator());
		m_columnIndex = 0;
	}

	/**
	 * Writes raw output, but ensures that the header or a field separator is written.
	 */
	public CsvWriter append(CharSequence raw) throws Exception {
		header();
		if(m_columnIndex++ > 0) {
			m_output.append(m_options.getDelimiter());
		}
		m_output.append(raw);
		return this;
	}

	public void header() throws Exception {
		if(m_headerWritten) {
			return;
		}
		m_headerWritten = true;                          // ORDERED
		List<String> header = m_options.getHeader();
		if(header.isEmpty()) {
			return;
		}
		StringBuilder sbOld = m_sb;
		m_sb = new StringBuilder();

		for(String s : header) {
			printString(s);
		}
		m_columnIndex = 0;
		println();
		m_sb = sbOld;
	}

	@Override
	public void close() throws Exception {
		if(m_output instanceof AutoCloseable) {
			((AutoCloseable) m_output).close();
		}
	}
}
