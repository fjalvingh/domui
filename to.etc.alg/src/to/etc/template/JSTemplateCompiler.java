package to.etc.template;

import java.io.*;
import java.util.*;

import javax.annotation.*;
import javax.script.*;

import to.etc.util.*;

/**
 * This factory class creates a compiled template for a JSP like template. The
 * language is Javascript, using JDK 6 scripting engine. The template's data
 * is copied verbatim to output until a &lt;% or &lt;%= is found; from there
 * it assumes the code is Javascript. The engine first creates a Javascript
 * program from the code entered, then it compiles it into the JSTemplate.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 26, 2010
 */
public class JSTemplateCompiler {
	/** Work buffer */
	private StringBuilder	m_sb	= new StringBuilder(1024);

	/** Javascript output buffer */
	private StringBuilder	m_jsb	= new StringBuilder(1024);

	private Reader			m_r;

	private String			m_source;

	private int				m_line, m_col;

	private int				m_pushed;

	private int						m_oline;

	private List<JSLocationMapping>	m_mapList;

	private enum Pha {
		/** Literal text */
		LIT,

		/** Got &lt;, */
		LT,

		/** Got &lt;% */
		PCT,

		/** Got &lt;%=, in expr there */
		XPR,

		/** In code section (&lt;%) */
		CODE,

		/** In % end delimiter */
		EPCT,
	}

	private Pha	m_pha;

	private Pha	m_opha;

	private class JSLib {
		private String	m_source;

		private String	m_identifier;

		public JSLib(String source, String identifier) {
			m_source = source;
			m_identifier = identifier;
		}

		public String getIdentifier() {
			return m_identifier;
		}

		public String getSource() {
			return m_source;
		}
	}

	@Nonnull
	private List<JSLib>	m_libraryList	= new ArrayList<JSTemplateCompiler.JSLib>();

	public void addLibrary(String identifier, String sourceCode) {
		m_libraryList.add(new JSLib(sourceCode, identifier));
	}

	public void addLibrary(File input) throws Exception {
		addLibrary(input.toString(), FileTool.readFileAsString(input, "utf-8"));
	}

	public void addLibrary(Class< ? > resourceClass, String name) throws Exception {
		addLibrary(resourceClass.getName() + ":" + name, FileTool.readResourceAsString(resourceClass, name, "utf-8"));
	}

	/**
	 * Create a template from input.
	 *
	 * @param input
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	public JSTemplate compile(Reader input, String sourceName) throws Exception {
		m_source = sourceName;
		translate(input);
		return compile();
	}

	public String getTranslation() {
		return m_jsb.toString();
	}

	/**
	 * Get a class resource as a template and compile it.
	 * @param clz
	 * @param resource
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	public JSTemplate compile(@Nonnull Class< ? > clz, @Nonnull String resource, @Nullable String encoding) throws Exception {
		if(null == encoding)
			encoding = "utf-8";

		InputStream is = clz.getResourceAsStream(resource);
		if(null == is)
			throw new IllegalArgumentException("No class resource " + clz + ":" + resource + " found");
		try {
			Reader r = new InputStreamReader(is, encoding);
			return compile(r, clz.getName() + ":" + resource);
		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Compile, then execute the specified template once.
	 * @param res
	 * @param input
	 * @param sourceName
	 * @throws Exception
	 */
	public void execute(Appendable res, Reader input, String sourceName, Object... assignments) throws Exception {
		JSTemplate tmpl = compile(input, sourceName);
		tmpl.execute(res, assignments);
	}

	/**
	 * Compile, then execute the specified template once.
	 * @param tc
	 * @param input
	 * @param sourceName
	 * @param assignments
	 * @throws Exception
	 */
	public void execute(IJSTemplateContext tc, Reader input, String sourceName, Object... assignments) throws Exception {
		JSTemplate tmpl = compile(input, sourceName);
		tmpl.execute(tc, assignments);
	}

	public Object execute(Appendable res, Class< ? > clz, String resource, Object... assignments) throws Exception {
		JSTemplate tmpl = compile(clz, resource, "utf-8");
		return tmpl.execute(res, assignments);
	}

	public void execute(IJSTemplateContext tc, Reader input, String sourceName, Map<String, Object> assignments) throws Exception {
		JSTemplate tmpl = compile(input, sourceName);
		tmpl.execute(tc, assignments);
	}

	public void executeMap(Appendable tc, Reader input, String sourceName, Map<String, Object> assignments) throws Exception {
		JSTemplate tmpl = compile(input, sourceName);
		tmpl.execute(tc, assignments);
	}

	public Object executeMap(Appendable res, Class< ? > clz, String resource, Map<String, Object> assignments) throws Exception {
		JSTemplate tmpl = compile(clz, resource, "utf-8");
		return tmpl.execute(res, assignments);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Compiling the javascript.							*/
	/*--------------------------------------------------------------*/
	/**
	 * Compile the Javascript program in m_jsb, then create a template.
	 * @throws Exception
	 */
	private JSTemplate compile() throws Exception {
		//-- Get a Javascript compiler.
		ScriptEngineManager sem = new ScriptEngineManager();
		ScriptEngine jsengine = sem.getEngineByName("js");
		if(!(jsengine instanceof Compilable))
			throw new IllegalStateException("Got Javascript engine " + jsengine + " which cannot compile Javascripts!?");
		Compilable	compiler = (Compilable) jsengine;

		jsengine.getBindings(ScriptContext.ENGINE_SCOPE).put(ScriptEngine.FILENAME, m_source);

		//-- Add all libraries at the end of the translated script.
		for(JSLib l : m_libraryList) {
			m_jsb.append("\n;\n");
			m_jsb.append(l.getSource());
		}

		//-- Get the Javascript thing, then compile
		String js = m_jsb.toString();
		try {
			CompiledScript cs = compiler.compile(js);
			return new JSTemplate(m_source, jsengine, cs, m_mapList);
		} catch(ScriptException sx) {
			int[] res = remapLocation(m_mapList, sx.getLineNumber(), sx.getColumnNumber());
			throw new JSTemplateError(sx.getMessage(), m_source, res[0], res[1]);
		}
	}

	/**
	 * Walk the remap list, and try to calculate a source location for a given output location.
	 * @param mapList
	 * @param lineNumber
	 * @param columnNumber
	 * @return
	 */
	static public int[] remapLocation(List<JSLocationMapping> mapList, int lineNumber, int columnNumber) {
		//-- Walk the mapping backwards. Find 1st thing that is at/before this location.
		for(int i = mapList.size(); --i >= 0;) {
			JSLocationMapping m = mapList.get(i);
			if(m.getTline() <= lineNumber) {
				if(m.getTcol() <= columnNumber) {
					//-- Gotcha.
					int dline = lineNumber - m.getTline();
					int dcol = columnNumber - m.getTcol();
					return new int[]{m.getSline() + dline, m.getScol() + dcol};
				}
			}
		}
		//-- Nothing found: return verbatim.
		return new int[]{lineNumber, columnNumber};
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Translate to Javascript.							*/
	/*--------------------------------------------------------------*/
	/**
	 * Translate input to a Javascript program.
	 * @param input
	 */
	protected void translate(Reader input) throws Exception {
		if(!(input instanceof BufferedReader))
			input = new BufferedReader(input);
		m_r = input;
		m_jsb.setLength(0);
		m_line = 0;
		m_col = 0;
		m_pha = Pha.LIT;
		m_pushed = -1;
		m_mapList = new ArrayList<JSLocationMapping>();

		//-- Per-char state machine
		int scol = 0;
		int sline = 0;
		for(;;) {
			int c = la();
			if(c == -1) {
				if(m_pha != Pha.LIT)
					error("Unexpected end-of-file.");

				//-- Flush latest lit
				flushLiteral();
				return;
			}
			switch(m_pha){
				default:
					throw new IllegalStateException("Bad pha");

				case LIT:
					if(c == '<') {
						m_pha = Pha.LT;
					} else {
						m_sb.append((char) c);
					}
					break;

				case LT:
					if(c == '%') {
						m_pha = Pha.PCT;

						//-- Ok: we need to flush the javascript string collected here.
						flushLiteral();
						sline = m_line;
						scol = m_col;
					} else {
						//-- Not <%. Just add the < and pushback this char
						m_sb.append('<');
						m_pha = Pha.LIT;
						push(c);
					}
					break;

				case PCT:
					if(c == '=') {
						//-- EXPR section.
						m_pha = Pha.XPR;
					} else {
						//-- Nothing special- must be javascript code.
						m_pha = Pha.CODE;
						m_sb.append((char) c);
					}
					break;

				case XPR:
				case CODE:
					if(c == '%') {
						m_opha = m_pha;
						m_pha = Pha.EPCT;
					} else {
						m_sb.append((char) c);
					}
					break;

				case EPCT:
					if(c == '>') {
						m_pha = Pha.LIT;
						flushJavascript(m_opha == Pha.XPR, sline, scol);
					} else {
						m_sb.append('%');
						m_pha = m_opha;
					}
					break;
			}
			if(c == '\n') {
				m_line++;
				m_col = 0;
			} else {
				m_col++;
			}
		}
	}

	private void addMapping(int oline, int ocol, int line, int col) {
		JSLocationMapping m = new JSLocationMapping(oline, ocol, line, col);
		m_mapList.add(m);
	}

	/**
	 * @param scol
	 * @param sline
	 */
	private void flushJavascript(boolean isexpr, int sline, int scol) {
		String code = m_sb.toString(); // Get Javascript fragment
		m_sb.setLength(0);

		if(isexpr) {
			m_jsb.append("__tv="); // Must assign to variable
			addMapping(m_oline, 5, sline, scol);
			m_jsb.append(code); // Append Javascript
			m_jsb.append(";\n"); // Assignment
			m_oline++;

			//-- Force output
			m_jsb.append("out.writeValue(__tv);\n");
			m_oline++;
			return;
		}

		addMapping(m_oline, 0, sline, scol);
		m_jsb.append(code);
		m_jsb.append('\n');
		m_oline++;
	}

	/**
	 * Create a Javascript command to print the string in m_sb to out.
	 */
	private void flushLiteral() {
		m_jsb.append("out.write(");
		strToJavascriptString(m_jsb, m_sb, true);
		m_jsb.append(");\n");
		m_oline++;
		m_sb.setLength(0);
	}

	static public void strToJavascriptString(final StringBuilder w, final CharSequence cs, final boolean dblquote) {
		int len = cs.length();
		//		if(len == 0)					jal 20090225 WTF!?!! Empty strings MUST be ""!!!!!
		//			return;
		int ix = 0;
		char quotechar;
		quotechar = dblquote ? '\"' : '\'';
		w.append(quotechar);

		while(ix < len) {
			//-- Collect a run
			int runstart = ix;
			char c = 0;
			while(ix < len) {
				c = cs.charAt(ix);
				if(c < 32 || c == '\'' || c == '\\' || c == quotechar)
					break;
				ix++;
			}
			if(ix > runstart) {
				w.append(cs, runstart, ix);
				if(ix >= len)
					break;
			}
			ix++;
			switch(c){
				default:
					w.append("\\u"); // Unicode escape
					intToStr(w, c & 0xffff, 16, 4);
					break;
				case '\n':
					w.append("\\n");
					break;
				case '\b':
					w.append("\\b");
					break;
				case '\f':
					w.append("\\f");
					break;
				case '\r':
					w.append("\\r");
					break;
				case '\t':
					w.append("\\t");
					break;
				case '\'':
					w.append("\\'");
					break;
				case '\"':
					w.append("\\\"");
					break;
				case '\\':
					w.append("\\\\");
					break;
			}
		}
		w.append(quotechar);
	}

	private static void intToStr(StringBuilder w, int value, int radix, int len) {
		String v = Integer.toString(value, radix);
		int sl = v.length();
		while(sl < len) {
			w.append('0');
			sl++;
		}
		w.append(v);
	}

	private void push(int c) {
		if(m_pushed != -1)
			throw new IllegalStateException("Dup push");
		m_pushed = c;
	}

	private int la() throws IOException {
		if(m_pushed != -1) {
			int c = m_pushed;
			m_pushed = -1;
			return c;
		}
		return m_r.read();
	}

	protected void error(String string) {
		throw new JSTemplateError(string, m_source, m_line, m_col);
	}
}
