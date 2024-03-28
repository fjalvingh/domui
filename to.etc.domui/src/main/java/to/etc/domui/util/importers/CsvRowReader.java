package to.etc.domui.util.importers;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.util.WrappedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This reads CSV files in multiple different formats. It can
 * be used in two ways:
 * <ul>
 *	<li>As one of the generic variants reading columnar data,
 *		using a common interface of row and column objects while
 *		reading rows one by one</li>
 *	<li>As a primitive (but faster) reader which just gets the data
 *		from each column as a String, without any intermediary
 *		objects. This mode should be faster.</li>
 * </ul>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12-12-17.
 */
@NonNullByDefault
public class CsvRowReader implements IRowReader, AutoCloseable, Iterable<IImportRow> {
	static private int MAXBUF = 10;

	@Nullable
	private Reader m_r;

	private boolean m_eof;

	private boolean m_ignoreQuotes;

	private boolean m_hasHeaderRow;

	private boolean m_headerRead;

	private StringBuilder m_sb = new StringBuilder();

	private List<String> m_columns = new ArrayList<>();

	@Nullable
	private IImportRow m_headerRow;

	private final List<String> m_headerNames = new ArrayList<>();

	private int m_fieldSeparator = ',';

	private int m_lastChar = -1;

	private int m_lineNumber = 1;

	private List<CsvError> m_errorList = new ArrayList<>();

	private int m_charNumber;

	private int m_quoteChar = '"';

	private int m_escapeChar = -1;

	private boolean m_doubleQuotes;

	private boolean m_multiLine;

	private boolean m_skipCr = true;

	private boolean m_askedForErrors;

	private int m_la1 = -2;

	@Nullable
	private DateFormat m_dateFormat;

	private final Map<String, DateFormat> m_dateFormatMap = new HashMap<>();

	private long m_totalCharactersRead;

	private final long m_fileSize;

	static public class CsvError {
		private final ImporterErrorCodes m_code;

		private final Object[] m_param;

		public CsvError(ImporterErrorCodes code, Object[] param) {
			m_code = code;
			m_param = param;
		}

		@Override
		public String toString() {
			return m_code.getBundle().formatMessage(m_code.name(), m_param);
		}
	}

	/**
	 * FileSize is used to report progress while reading.
	 */
	public CsvRowReader(Reader r, long fileSize) {
		m_r = new BufferedReader(r, 8192);
		m_fileSize = fileSize;
	}

	public CsvRowReader(Reader r) {
		this(r, 0);
	}

	private int la() throws IOException {
		if(m_lastChar == -1 && !m_eof) {
			accept();
		}
		return m_lastChar;
	}

	private int la1() throws IOException {
		int la = m_la1;
		if(la != -2)
			return la;
		int c = Objects.requireNonNull(m_r).read();
		m_totalCharactersRead++;
		m_la1 = c;
		return c;
	}

	private int accept() throws IOException {
		if(m_eof)
			return -1;
		int la = m_la1;
		int c;
		if(la != -2) {
			c = la;
			m_la1 = -2;
		} else {
			c = Objects.requireNonNull(m_r).read();
			m_charNumber++;
			m_totalCharactersRead++;
		}
		if(c == -1) {
			m_eof = true;
			m_lastChar = -1;
			return -1;
		} else if(c == '\n') {
			m_lineNumber++;
			m_charNumber = 0;
		}
		return m_lastChar = c & 0xffff;
	}

	boolean readRecordWithoutErrorCheck() throws IOException {
		if(m_hasHeaderRow && !m_headerRead) {
			if(!readHeader())
				return false;
		}
		return readRecordPrimitive();
	}


	private boolean readHeader() throws IOException {
		if(!m_hasHeaderRow || m_headerRead)
			return true;

		m_headerRead = true;
		if(!readRecordPrimitive())
			return false;
		m_headerRow = new CsvImportRow(this, m_columns);
		m_headerNames.addAll(m_columns);
		m_columns.clear();
		return true;
	}

	public boolean readRecordPrimitive() throws IOException {
		m_columns.clear();
		if(m_eof)
			return false;
		while(la() == '\n' || (la() == '\r' && la1() == '\n'))                                    // Skip empty lines
			accept();
		for(; ; ) {
			readField();
			int c = la();
			if(c == -1)
				break;
			if(c == '\n') {
				accept();
				if(!m_columns.isEmpty()) {
					break;
				}
				continue;
			}
			if(c == '\r' && la1() == '\n') {
				accept();
				accept();
				if(!m_columns.isEmpty()) {
					break;
				}
				continue;
			}

			if(la() != m_fieldSeparator) {
				error(ImporterErrorCodes.csvExpectingSeparator, (char) la());
				nextLine();
				m_columns.clear();
			} else {
				accept();
			}
		}

		return true;
	}

	/**
	 * Handle reading a complete field.
	 */
	private void readField() throws IOException {
		m_sb.setLength(0);

		int startLine = m_lineNumber;

		int qc = 0;
		if(la() == m_quoteChar) {
			qc = m_quoteChar;
			accept();
		}
		int skipcr = m_skipCr ? '\r' : -1;
		for(; ; ) {
			int c = la();
			if(c == -1) {
				if(qc != 0) {
					error(ImporterErrorCodes.csvEofInString, startLine);        // EOF in string started at line xxx
				} else {
					//-- Can be a line without a newline.
					if(m_sb.length() == 0)
						m_columns.add(null);                                // ,, means null
					else
						m_columns.add(m_sb.toString());
				}
				return;
			}

			if(qc == 0) {
				if((c == '\n' || (c == '\r' && la1() == '\n')) || c == m_fieldSeparator) {
					//-- End of record, end of field -> add content to list.
					if(m_sb.length() == 0)
						m_columns.add(null);                                // ,, means null
					else
						m_columns.add(m_sb.toString());
					return;
				}
				m_sb.append((char) c);
				accept();
			} else {
				if(c == '\n' && !m_multiLine) {
					error(ImporterErrorCodes.csvNewlineInsideQuote);
					m_columns.add(m_sb.toString());
					return;
				}

				/*
				 * In all cases we can now lookahead...
				 */
				accept();
				if(c == m_escapeChar) {
					c = la();
					if(c == -1) {
						error(ImporterErrorCodes.csvEofInString, startLine);        // EOF in string started at line xxx
						return;
					}
					m_sb.append((char) c);
					accept();
				} else if(c == m_quoteChar) {
					if(m_doubleQuotes) {
						if(la() == m_quoteChar) {
							accept();                                // Skip one of the quotes
							m_sb.append((char) c);
						} else {
							//-- We are done: end quote found, should be a field
							m_columns.add(m_sb.toString());            // "","" is empty string, not null
							skipWhiteSpace();                        // Allow whitespace after " to next comma
							return;
						}
					} else {
						//-- We are done (no double quoting)
						m_columns.add(m_sb.toString());            // "","" is empty string, not null
						skipWhiteSpace();                        // Allow whitespace after " to next comma
						return;
					}
				} else if(c != skipcr) {
					m_sb.append((char) c);
				}
			}
		}
	}

	private void skipWhiteSpace() throws IOException {
		for(; ; ) {
			int c = la();
			if(c == -1 || c == '\n' || !Character.isWhitespace(c))
				return;
			accept();
		}
	}

	private void nextLine() throws IOException {
		for(; ; ) {
			int c = la();
			if(c == '\n' || c == -1)
				return;
			accept();
		}
	}

	private void error(ImporterErrorCodes code, Object... args) {
		Object[] param = new Object[2 + args.length];
		System.arraycopy(args, 0, param, 2, args.length);
		param[0] = Integer.valueOf(m_lineNumber);
		param[1] = Integer.valueOf(m_charNumber);
		m_errorList.add(new CsvError(code, param));
	}


	@Nullable
	@Override
	public IImportRow getHeaderRow() throws IOException {
		if(!m_hasHeaderRow)
			return null;
		if(!readHeader()) {
			return null;
		}
		return m_headerRow;
	}

	public int getColumnIndex(String name) throws IOException {
		readHeader();
		for(int i = m_headerNames.size(); --i >= 0; ) {
			String s = m_headerNames.get(i);
			if(name.equalsIgnoreCase(s)) {
				return i;
			}
		}
		return -1;
	}

	@Nullable
	public String getColumnName(int index) throws IOException {
		readHeader();
		if(index <= 0 || index >= m_headerNames.size())
			return null;
		return m_headerNames.get(index);
	}

	@Override
	public int getSetCount() {
		return 1;
	}

	private void checkForErrors() {
		if(!getErrorList().isEmpty()) {
			throw new ImportValueException("CSV File format error at " + getErrorList().get(0).toString());
		}
	}

	@Override
	public List<IDatasetInfo> getSets() throws Exception {
		return Collections.singletonList(new IDatasetInfo() {
			@Override
			public String getName() {
				return "file";
			}

			@Override
			public int getIndex() {
				return 0;
			}
		});
	}

	@Override
	public void setSetIndex(int setIndex) {
		if(setIndex != 0)
			throw new IllegalStateException("CSV files have but one set");
	}

	@Override
	public long getSetSizeIndicator() {
		return m_fileSize;
	}

	@Override
	public void close() throws IOException {
		try {
			if(!m_askedForErrors && !m_errorList.isEmpty()) {
				throw new IllegalStateException("The CSV file had errors; call getErrorList() to report them");
			}

			if(m_r != null)
				m_r.close();
		} catch(Exception x) {
		}
		m_r = null;
	}

	@Override
	public void setHasHeaderRow(boolean hasHeaderRow) {
		m_hasHeaderRow = hasHeaderRow;
	}

	@Override
	public long getProgressIndicator() {
		return m_totalCharactersRead;
	}

	public List<CsvError> getErrorList() {
		m_askedForErrors = true;
		return m_errorList;
	}

	public int getLineNumber() {
		return m_lineNumber;
	}

	@NonNull
	@Override
	public Iterator<IImportRow> iterator() {
		return new RowIterator();
	}

	public CsvRowReader hasHeader() {
		m_hasHeaderRow = true;
		return this;
	}

	public CsvRowReader ignoreQuotes() {
		m_ignoreQuotes = true;
		return this;
	}

	public CsvRowReader fieldSeparator(int sepa) {
		m_fieldSeparator = sepa;
		return this;
	}

	public CsvRowReader quoteChar(int quote) {
		m_quoteChar = quote;
		return this;
	}

	public CsvRowReader escapeChar(int escape) {
		m_escapeChar = escape;
		return this;
	}

	public CsvRowReader doubleQuotes() {
		m_doubleQuotes = true;
		return this;
	}

	public CsvRowReader multiLine() {
		m_multiLine = true;
		return this;
	}

	public CsvRowReader dateFormat(String dateFormat) {
		DateFormat f = m_dateFormat = new SimpleDateFormat(dateFormat);
		f.setLenient(false);
		return this;
	}

	@Override
	public void setDateFormat(String dateFormat) {
		dateFormat(dateFormat);
	}

	@Nullable
	public DateFormat getDateFormat() {
		return m_dateFormat;
	}

	public DateFormat getDateFormat(String dateFormat) {
		return m_dateFormatMap.computeIfAbsent(dateFormat, a -> {
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			sdf.setLenient(false);
			return sdf;
		});
	}

	public CsvRowReader skipCr(boolean skip) {
		m_skipCr = skip;
		return this;
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Direct row access (without Row and Column objects)			*/
	/*----------------------------------------------------------------------*/
	/**
	 * Reads the next record into the internal buffers.
	 */
	public boolean readRecord() throws IOException {
		boolean res = readRecordWithoutErrorCheck();
		if(!m_errorList.isEmpty())
			throw new ImportValueException("CSV File format error at " + m_errorList.get(0).toString());

		checkForErrors();
		return res;
	}

	/**
	 * Return the #of columns present.
	 */
	public int getColumnCount() {
		return m_columns.size();
	}

	/**
	 * Get the result of the nth column (0 based).
	 */
	@Nullable
	public String getColumnValue(int index) {
		return index >= m_columns.size() ? null : m_columns.get(index);
	}

	private class RowIterator implements Iterator<IImportRow> {
		private boolean m_nextRead;

		private boolean m_nextAvailable;

		@Nullable
		private CsvImportRow m_row;

		public RowIterator() {
		}

		@Override
		public boolean hasNext() {
			if(!m_nextRead) {
				m_nextRead = true;
				try {
					m_nextAvailable = readRecordWithoutErrorCheck();
					if(m_nextAvailable) {
						m_row = new CsvImportRow(CsvRowReader.this, m_columns);
					}
				} catch(IOException x) {
					throw new WrappedException(x);                    // Morons.
				}
			}
			return m_nextAvailable;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public IImportRow next() {
			CsvImportRow row = m_row;
			checkForErrors();
			if(!m_nextAvailable || row == null)
				throw new IllegalStateException("Calling next() after hasNext() returned false / missing call to hasNext()");
			m_nextRead = false;
			return row;
		}
	}
}
