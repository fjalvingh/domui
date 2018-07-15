package to.etc.util;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;

public class LineIterator implements Iterable<String>, Iterator<String> {
	private LineNumberReader m_reader;

	private boolean m_eof;

	@Nullable
	private String m_nextLine;

	public LineIterator(String s) {
		m_reader = new LineNumberReader(new StringReader(s));
	}

	public LineIterator(Reader r) {
		if(r instanceof LineNumberReader)
			m_reader = (LineNumberReader) r;
		else
			m_reader = new LineNumberReader(r);
	}

	@NonNull
	@Override
	public Iterator<String> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		if(m_nextLine != null)
			return true;
		if(m_eof)
			return false;
		try {
			m_nextLine = m_reader.readLine();
			m_eof = m_nextLine == null;
			return !m_eof;
		} catch(IOException x) {
			throw WrappedException.wrap(x);
		}
	}

	@Nullable
	@Override
	public String next() {
		if(m_nextLine == null)
			throw new IllegalStateException("Call hasNext 1st");
		String s = m_nextLine;
		m_nextLine = null;
		return s;
	}

	@Override
	public void remove() {}
}
