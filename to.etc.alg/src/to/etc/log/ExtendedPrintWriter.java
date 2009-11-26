package to.etc.log;

import java.io.*;
import java.util.*;

import to.etc.util.*;

public class ExtendedPrintWriter extends PrintWriter {
	public ExtendedPrintWriter(Writer out) {
		super(out);
	}

	/**
	 * Print a string with a fixed width. If the size overflows it takes the overflown size.
	 * @param s
	 * @param width
	 */
	public void print(String s, int width) {
		int len = s.length();
		while(len++ < width)
			print(' ');
	}

	public void printright(String s, int width) {
		int len = s.length();
		while(len++ < width)
			print(' ');
		print(s);
	}

	public void print(long value, int width) {
		printright(Long.toString(value), width);
	}

	public void printCommad(long value, int width) {
		printright(StringTool.strCommad(value), width);
	}

	public void printSize(long value, int width) {
		print(StringTool.strSize(value), width);
	}

	/*--------------------------------------------------------------*/
	/*	CODING: Formatted printer.                           		*/
	/*--------------------------------------------------------------*/
	static private class FSpec {
		public String	m_header;

		public int		m_type;

		public int		m_width;

		public boolean	m_right;

		public FSpec(int width, int type, String header, boolean right) {
			m_width = width;
			m_type = type;
			m_header = header;
			m_right = right;
		}
	}

	static public final int		STR			= 0;

	static public final int		INT			= 1;

	static public final int		COMMAD		= 2;

	static public final int		SIZE		= 3;

	static public final int		STRTRUNC	= 4;

	static private final FSpec	NULL_FSPEC	= new FSpec(0, -1, "", false);

	private List<FSpec>			m_flist		= new ArrayList<FSpec>();

	private boolean				m_init;

	private int					m_index;

	public void clear() {
		m_flist.clear();
		if(m_index > 0)
			done();
		m_init = false;
	}

	public void add(int width, int format, String header, boolean right) {
		m_flist.add(new FSpec(width, format, header, right));
	}

	public void add(int width, int format, String header) {
		m_flist.add(new FSpec(width, format, header, false));
	}

	public void header(String hdr, int type) {
		add(hdr.length(), type, hdr, (type == INT || type == COMMAD));
	}

	private void init() {
		if(!m_init) {
			for(FSpec s : m_flist) {
				if(m_init)
					print(' ');
				print(s.m_header);
				m_init = true;
			}
			m_init = true;
			println();
		}
	}

	private FSpec current() {
		if(m_index >= m_flist.size())
			return NULL_FSPEC;
		return m_flist.get(m_index);
	}

	public void out(String s) {
		init();
		FSpec f = current();
		m_index++;
		if(m_index != 1)
			print(' ');
		int len = s.length();
		if(f.m_type == STRTRUNC && len > f.m_width) {
			s = s.substring(0, f.m_width);
			len = f.m_width;
		}
		if(f.m_right) {
			while(len++ < f.m_width)
				print(' ');
		}
		print(s);
		if(!f.m_right) {
			while(len++ < f.m_width)
				print(' ');
		}
		if(m_index >= m_flist.size()) {
			m_index = 0;
			println();
		}
	}

	public void done() {
		if(m_index > 0) {
			m_index = 0;
			println();
		}
	}

	public void out(long v) {
		init();
		FSpec f = current();
		switch(f.m_type){
			case COMMAD:
				out(StringTool.strCommad(v));
				break;
			case SIZE:
				out(StringTool.strSize(v));
				break;
			default:
				out(Long.toString(v));
				break;
		}
	}
}
