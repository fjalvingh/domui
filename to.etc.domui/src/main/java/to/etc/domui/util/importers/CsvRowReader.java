package to.etc.domui.util.importers;

import org.jetbrains.annotations.NotNull;
import to.etc.util.WrappedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12-12-17.
 */
public class CsvRowReader implements IRowReader, AutoCloseable, Iterable<IImportRow> {
	static private int MAXBUF = 10;

	private Reader m_r;

	private boolean m_eof;

	private boolean m_ignoreQuotes;

	private boolean m_hasHeaderRow;

	private boolean m_headerRead;

	private StringBuilder m_sb = new StringBuilder();

	private List<String> m_columns = new ArrayList<>();

	private List<String> m_headers = new ArrayList<>();

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

	public CsvRowReader(Reader r) {
		m_r = new BufferedReader(r, 8192);
	}

	private int la() throws IOException {
		if(m_lastChar == -1 && ! m_eof) {
			accept();
		}
		return m_lastChar;
	}

	private int accept() throws IOException {
		if(m_eof)
			return -1;
		int c = m_r.read();
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

	public boolean readRecord() throws IOException {
		if(m_hasHeaderRow && ! m_headerRead) {
			m_headerRead = true;
			if(! readRecordPrimitive())
				return false;
			m_headers.addAll(m_columns);
			m_columns.clear();
		}
		return readRecordPrimitive();
	}

	public boolean readRecordPrimitive() throws IOException {
		m_columns.clear();
		if(m_eof)
			return false;
		while(la() == '\n')									// Skip empty lines
			accept();
		for(;;) {
			readField();
			int c = la();
			if(c == -1)
				break;
			if(c == '\n') {
				accept();
				if(m_columns.size() > 0) {
					break;
				}
				continue;
			}

			if(la() != m_fieldSeparator) {
				error(ImporterErrorCodes.csvExpectingSeparator);
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
		for(;;) {
			int c = la();
			if(c == -1) {
				if(qc != 0) {
					error(ImporterErrorCodes.csvEofInString, startLine);		// EOF in string started at line xxx
				}
				return;
			}

			if(qc == 0) {
				if(c == '\n' || c == m_fieldSeparator) {
					//-- End of record, end of field -> add content to list.
					if(m_sb.length() == 0)
						m_columns.add(null);								// ,, means null
					else
						m_columns.add(m_sb.toString());
					return;
				}
				m_sb.append((char) c);
				accept();
			} else {
				if(c == '\n' && ! m_multiLine) {
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
						error(ImporterErrorCodes.csvEofInString, startLine);		// EOF in string started at line xxx
						return;
					}
					m_sb.append((char) c);
				} else if(c == m_quoteChar) {
					if(m_doubleQuotes) {
						if(la() == m_quoteChar) {
							accept();								// Skip one of the quotes
							m_sb.append((char) c);
						} else {
							//-- We are done: end quote found, should be a field
							m_columns.add(m_sb.toString());			// "","" is empty string, not null
							skipWhiteSpace();						// Allow whitespace after " to next comma
							return;
						}
					} else {
						//-- We are done (no double quoting)
						m_columns.add(m_sb.toString());			// "","" is empty string, not null
						skipWhiteSpace();						// Allow whitespace after " to next comma
						return;
					}
				} else if(c != skipcr) {
					m_sb.append((char) c);
				}
			}
		}
	}

	private void skipWhiteSpace() throws IOException {
		for(;;) {
			int c = la();
			if(c == -1 || c == '\n' || ! Character.isWhitespace(c))
				return;
			accept();
		}
	}

	private void nextLine() throws IOException {
		for(;;) {
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


	@Override public IImportRow getHeaderRow() {
		return null;
	}

	@Override public int getSetCount() {
		return 1;
	}

	@Override public void setSetIndex(int setIndex) {
		if(setIndex != 0)
			throw new IllegalStateException("CSV files have but one set");
	}

	@Override public long getSetSizeIndicator() {
		return 0;
	}

	@Override public void close() throws IOException {
		try {
			if(! m_askedForErrors && m_errorList.size() > 0) {
				throw new IllegalStateException("The CSV file had errors; call getErrorList() to report them");
			}

			if(m_r != null)
				m_r.close();
		} catch(Exception x) {
		}
		m_r = null;
	}

	@Override public void setHasHeaderRow(boolean hasHeaderRow) {
		m_hasHeaderRow = hasHeaderRow;
	}

	@Override public long getProgressIndicator() {
		return 0;
	}

	public List<CsvError> getErrorList() {
		m_askedForErrors = true;
		return m_errorList;
	}

	public int getLineNumber() {
		return m_lineNumber;
	}

	@NotNull @Override public Iterator<IImportRow> iterator() {
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
	public CsvRowReader skipCr(boolean skip) {
		m_skipCr = skip;
		return this;
	}

	private class RowIterator implements Iterator<IImportRow> {
		private boolean m_nextRead;

		private boolean m_nextAvailable;

		private CsvImportRow m_row;

		public RowIterator() {
		}

		@Override public boolean hasNext() {
			if(! m_nextRead) {
				m_nextRead = true;
				try {
					m_nextAvailable = readRecord();
					if(m_nextAvailable) {
						m_row = new CsvImportRow(m_columns);
					}
				} catch(IOException x) {
					throw new WrappedException(x);					// Morons.
				}
			}
			return m_nextAvailable;
		}

		@Override public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override public IImportRow next() {
			if(! m_nextAvailable)
				throw new IllegalStateException("Calling next() after hasNext() returned false / missing call to hasNext()");
			m_nextRead = false;
			return m_row;
		}
	}
}
