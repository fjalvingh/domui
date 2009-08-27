package to.etc.xml;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

/**
 * This helper class handles conversions from XML to CSV format, using
 * a description of the file to generate from a .properties file (or 
 * Properties structure).
 * 
 * See the example conversion descriptor in ./examples/xmltocsv.properties.
 * 
 * <p>Created on May 23, 2005
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 */
public class XMLToCSVConverter {
	static private final int	QR_ALL		= 0;

	static private final int	QR_STRING	= 1;

	static private final int	QR_NONE		= 2;

	static private final int	ER_C		= 0;


	/** The name of the record element encapsulating a csv record instance, */
	private String				m_record;

	/** The path to the list of records. */
	private PathExpression		m_recordpath;

	/** The CSV layout in getters per position. */
	private FieldGetter[]		m_fg_ar;

	/** The encoding for the output stream. */
	private String				m_encoding;

	/** Global quote rule. */
	private int					m_quote		= QR_ALL;

	/** The field separator */
	private char				m_sep;

	private char				m_quotechar;

	private String				m_quoteescape;

	private int					m_escaperule;

	private StringBuffer		m_error_sb;

	public XMLToCSVConverter() {
	}

	public XMLToCSVConverter(Properties p) throws Exception {
		init(p);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Creating the conversion map.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Called internally to create the converter list for the
	 * document. This decodes all that's needed from the properties
	 * file to decode a document.
	 * @param p
	 */
	public void init(Properties p) throws Exception {
		//-- We MUST have a record path
		String s = p.getProperty("xml.recordlist"); // The path x to the list of records.
		if(s == null)
			error("Missing 'xml.recordlist' parameter."); // Mandatory
		m_recordpath = PathExpression.getExpression(s); // And convert to a path expr

		//-- And a record name descriptor,
		s = p.getProperty("xml.record");
		if(s == null)
			error("Missing 'xml.record' parameter");
		m_record = s.trim();
		if(m_record.length() == 0)
			error("Empty 'xml.record' parameter is invalid!");

		//-- Handle others,
		m_encoding = p.getProperty("csv.encoding");
		s = p.getProperty("csv.quote"); // Quoting type
		if(s != null) {
			if(s.equalsIgnoreCase("all"))
				m_quote = QR_ALL;
			else if(s.equalsIgnoreCase("string"))
				m_quote = QR_STRING;
			else if(s.equalsIgnoreCase("NONE"))
				m_quote = QR_NONE;
			else
				error("Invalid value '" + s + "' for csv.quote: must be all, none or string");
		}

		s = p.getProperty("csv.separator");
		if(s == null)
			m_sep = ',';
		else {
			s = s.trim();
			if(s.length() != 1)
				error("Bad field separator '" + s + "': it must be a single character.");
			m_sep = s.charAt(0);
		}

		s = p.getProperty("csv.quotechar");
		if(s == null)
			m_quotechar = '"';
		else {
			s = s.trim();
			if(s.length() != 1)
				error("Bad quotechar value '" + s + "': it must be a single character.");
			m_quotechar = s.charAt(0);
		}

		s = p.getProperty("csv.quoteescape");
		if(s == null)
			m_quoteescape = "\\\"";
		else {
			m_quoteescape = s.trim();
		}

		s = p.getProperty("csv.escaperule");
		if(s != null) {
			if("c".equalsIgnoreCase(s))
				m_escaperule = ER_C;
			else
				error("Invalid escape rule '" + s + "'. Valid is C only for now");
		} else
			m_escaperule = ER_C;


		//-- Now get the csv.layout descriptor
		s = p.getProperty("csv.layout"); // The main layout spec
		if(s == null)
			error("Missing 'csv.layout' field");
		decodeLayout(p, s);

	}

	/**
	 * Decodes the entire layout descriptor and builds the
	 * per-record descriptor for layout.
	 * 
	 * @param layout
	 * @throws Exception
	 */
	private void decodeLayout(Properties p, String layout) throws Exception {
		ArrayList al = new ArrayList(); // Each csv field's conversion descriptor.
		StringTokenizer st = new StringTokenizer(layout, ",");
		while(st.hasMoreTokens()) {
			String ve = st.nextToken(); // The next field's token
			FieldGetter fg = makeFieldGetter(p, ve); // Get a 'getter' for this CSV position.
			al.add(fg); // And add to the thingy list
		}
		m_fg_ar = (FieldGetter[]) al.toArray(new FieldGetter[al.size()]);
	}

	/**
	 * Decodes a single field.
	 * 
	 * @param key
	 * @return
	 * @throws Exception
	 */
	private FieldGetter makeFieldGetter(Properties p, String key) throws Exception {
		//-- Get the primary path expression for the field.
		String s = p.getProperty(key + ".path");
		if(s == null)
			error("Missing '" + key + ".path' parameter containing the path expression for csv field '" + key + "'");
		s = s.trim();
		if(s.length() == 0)
			error("Empty '" + key + ".path' parameter containing the path expression for csv field '" + key + "'");

		PathExpression px;
		try {
			px = PathExpression.getExpression(s);
		} catch(Exception x) {
			throw new IllegalStateException("Invalid path expression '" + s + "' for field '" + key + "': " + x.getMessage());
		}

		//-- The base expression.
		FieldGetter fg = new FieldGetter(key, px);

		//-- Get all field adapters.
		fg.setNoTrim(boolProp(p, key + ".notrim", false));
		fg.setKeepNL(boolProp(p, key + ".keepnewline", false));
		s = p.getProperty(key + ".quote");
		if(s == null)
			fg.setQuote(m_quote == QR_ALL);
		else
			fg.setQuote(boolProp(key + ".quote", s, false));

		s = p.getProperty(key + ".quotechar");
		if(s != null) {
			s = s.trim();
			if(s.length() != 1)
				error("Bad quotechar value '" + s + "' for field '" + key + "': it must be a single character.");
			fg.setQuotechar(s.charAt(0));
		} else
			fg.setQuotechar(m_quotechar);

		s = p.getProperty(key + ".quoteescape");
		if(s == null)
			fg.setQuoteescape(m_quoteescape);
		else {
			fg.setQuoteescape(s.trim());
		}

		s = p.getProperty(key + ".escaperule");
		if(s != null) {
			if("c".equalsIgnoreCase(s))
				fg.setEscaperule(ER_C);
			else
				error("Invalid escape rule '" + s + "'. Valid is C only for now");
		} else
			fg.setEscaperule(m_escaperule);

		return fg;
	}

	static private boolean boolProp(Properties p, String key, boolean def) {
		String s = p.getProperty(key);
		return boolProp(key, s, def);
	}

	static private boolean boolProp(String key, String s, boolean def) {
		if(s == null)
			return def;
		s = s.trim();
		if(s.length() == 0)
			return def;
		if(s.equalsIgnoreCase("true") || s.equalsIgnoreCase("on") || s.equalsIgnoreCase("1") || s.equalsIgnoreCase("yes"))
			return true;
		if(s.equalsIgnoreCase("false") || s.equalsIgnoreCase("off") || s.equalsIgnoreCase("0") || s.equalsIgnoreCase("no"))
			return false;
		throw new IllegalStateException("Invalid boolean value '" + s + "' in key '" + key + "'");
	}

	private void error(String s) throws Exception {
		throw new Exception(s);
	}

	/**
	 * This class contains all that's needed to convert a field.
	 * 
	 * <p>Created on May 23, 2005
	 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
	 */
	static private class FieldGetter {
		private PathExpression	m_path;

		private String			m_name;

		private boolean			m_notrim;

		private boolean			m_keepnl;

		/** T if the field needs to be quoted */
		boolean					m_quote;

		/** The character to use for quoting this field. */
		private char			m_quotechar;

		/** The string to use to quote the quote */
		private String			m_quoteescape;

		/** The invalid character escape rule. */
		private int				m_escaperule;

		public FieldGetter(String name, PathExpression path) {
			m_name = name;
			m_path = path;
		}

		public void setNoTrim(boolean v) {
			m_notrim = v;
		}

		public boolean getNoTrim() {
			return m_notrim;
		}

		public void setKeepNL(boolean yes) {
			m_keepnl = yes;
		}

		public int getEscaperule() {
			return m_escaperule;
		}

		public void setEscaperule(int escaperule) {
			m_escaperule = escaperule;
		}

		public boolean isQuote() {
			return m_quote;
		}

		public void setQuote(boolean quote) {
			m_quote = quote;
		}

		public char getQuotechar() {
			return m_quotechar;
		}

		public void setQuotechar(char quotechar) {
			m_quotechar = quotechar;
		}

		public String getQuoteescape() {
			return m_quoteescape;
		}

		public void setQuoteescape(String quoteescape) {
			m_quoteescape = quoteescape;
		}

		public boolean isKeepnl() {
			return m_keepnl;
		}

		public String getName() {
			return m_name;
		}

		public boolean isNotrim() {
			return m_notrim;
		}

		public PathExpression getPath() {
			return m_path;
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Doing a complete conversion.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Convert an XML document to a CSV file.
	 */
	public void convert(File f, Node doc) throws Exception {
		OutputStream os = new FileOutputStream(f);
		try {
			convert(os, doc);
		} finally {
			try {
				os.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Converts the document to an output stream.
	 * @param os
	 * @param doc
	 * @throws Exception
	 */
	public void convert(OutputStream os, Node doc) throws Exception {
		if(m_encoding == null)
			m_encoding = "utf-8";
		Writer w = new OutputStreamWriter(os, m_encoding);
		try {
			convertAll(w, doc);
		} finally {
			try {
				w.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Walks the document and generates a CSV record for every 
	 * record found there.
	 * @param w
	 * @param doc
	 * @throws Exception
	 */
	public void convertAll(Writer w, Node doc) throws Exception {
		m_error_sb = new StringBuffer();
		Node root = doc;
		Node n = m_recordpath.getNode(root, root, m_error_sb);
		if(n == null)
			error(m_error_sb + " in recordlist selector.");

		// mbp: treat a record node name of "/" as a single record having 
		// the given node for root
		if(m_record.compareToIgnoreCase("/") == 0) {
			convertRecord(w, root, root, 1);
		} else {
			//-- We have a record node. Walk it to get each individual record
			NodeList nl = n.getChildNodes();
			int recnr = 0;
			for(int i = 0; i < nl.getLength(); i++) {
				Node rec = nl.item(i);
				if(rec.getNodeName().equals(m_record))
					convertRecord(w, root, rec, recnr);
				recnr++;
			}
		}
	}

	public void convertRecord(Writer w, Node root, Node rec, int recnr) throws Exception {
		//-- Walk the layout descriptors and generate their output
		for(int i = 0; i < m_fg_ar.length; i++) {
			//-- Must we output a separator?
			if(i > 0)
				w.write(m_sep);
			convertField(w, root, rec, recnr, m_fg_ar[i]);
		}
		w.write("\n");
	}

	public void convertField(Writer w, Node doc, Node rec, int recnr, FieldGetter fg) throws Exception {
		//-- Get the raw value of a field as a raw string
		String v = getRaw(doc, rec, recnr, fg.getPath(), fg.getNoTrim());
		if(v == null) {
			//-- We have a null. Encode as a single non-quoted thing
			return;
		}

		//-- Must we quote the field?
		int quotechar = Integer.MIN_VALUE;
		if(fg.m_quote) // Must we quote?
		{
			quotechar = fg.getQuotechar(); // Get the char to use,
			w.write((char) quotechar); // Write the quote
		}
		writeFieldData_C(w, v, quotechar, fg.getQuoteescape());

		if(fg.m_quote) // Must we quote?
			w.write((char) quotechar); // Write the quote
	}

	static private void writeFieldData_C(Writer w, String v, int quotechar, String quoteesc) throws IOException {
		int len = v.length();
		for(int i = 0; i < len; i++) {
			char c = v.charAt(i);
			if(c == quotechar) {
				w.write(quoteesc);
			} else if(c < 32) {
				//-- Control sequence: escape
				escape(w, c);
			} else
				w.write(c);
		}
	}

	static private void escape(Writer w, char c) throws IOException {
		switch(c){
			case '\n':
				w.write("\\n");
				return;
			case '\r':
				w.write("\\r");
				return;
			case '\f':
				w.write("\\f");
				return;
			case '\t':
				w.write("\\t");
				return;
			case '\b':
				w.write("\\b");
				return;
			default:
				w.write("\\u");
				String v = "0000" + Integer.toHexString(c);
				w.write(v.substring(v.length() - 4, v.length()));
				return;
		}
	}

	private String getRaw(Node doc, Node rec, int recnr, PathExpression px, boolean notrim) throws Exception {
		//-- Get a node describing the value
		Node val = px.getNode(doc, rec, m_error_sb); // Get the thingty to display
		if(val == null) {
			System.out.println(m_error_sb + " in node " + getNodePath(rec));
			return null;
		}

		//-- We have a node. Can we convert it directly into text?
		if(val.getNodeType() == Node.TEXT_NODE)
			return val.getNodeValue();

		StringBuffer sb = new StringBuffer();
		NodeList nl = val.getChildNodes();
		for(int i = 0; i < nl.getLength(); i++) {
			Node cn = nl.item(i);
			if(cn.getNodeName().equals("#text")) {
				String txt = cn.getNodeValue();
				if(txt != null)
					sb.append(notrim ? txt : txt.trim());
			} else
				throw new IllegalStateException("Node '" + getNodePath(val) + "' contains elements: it is not a text-only node.");
		}
		String s = sb.toString();
		return s;
	}

	static private String getNodePath(Node n) {
		StringBuffer sb = new StringBuffer();
		getNodePath(sb, n);
		return sb.toString();
	}

	static private void getNodePath(StringBuffer sb, Node n) {
		if(n.getParentNode() == null) {
			sb.append(n.getNodeName());
			return;
		}
		getNodePath(sb, n.getParentNode());
		sb.append("->");
		sb.append(n.getNodeName());
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Testing code.										*/
	/*--------------------------------------------------------------*/

	static public void main(String[] args) {
		try {
			//-- Get the descriptor
			File pf = new File("./examples/xmltocsv.properties");
			InputStream is = new FileInputStream(pf);
			Properties p = new Properties();
			p.load(is);
			is.close();

			//-- Load a document
			File df = new File("./examples/input.xml");
			XmlReader r = new XmlReader();
			Document doc = r.getDocument(df);

			//-- Create the converter
			XMLToCSVConverter xc = new XMLToCSVConverter(p);

			//-- Now start converting away!
			File of = new File("/tmp/output.csv");
			xc.convert(of, doc);

			System.out.println("Document is at " + of);
		} catch(Exception x) {
			x.printStackTrace();
		}
	}
}
