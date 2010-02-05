package to.etc.csv;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import to.etc.util.*;

/**
 * This converts a CSV file to some XML format
 * based on a properties file describing the format for
 * each field. The output is generated into an output
 * writer.
 *
 * Created on Jan 20, 2005
 * @author jal
 */
public class CSVToXMLConverter {
	private Properties	m_prop_p;

	private ArrayList	m_conv_al	= new ArrayList();

	private ArrayList	m_opt_al	= new ArrayList();

	abstract private class Converter {
		public Converter() {
		}

		abstract public boolean convert(String in, Writer ow) throws IOException;
	}

	private class FieldOptions {
		//		private int		m_fix;

		private boolean	m_hidewhenempty;

		private boolean	m_emptystringisnull;

		private boolean	m_zeroisnull;

		private boolean	m_hasspacesisnull;

		private Pattern	m_regex;

		public FieldOptions(int fix) {
			//			m_fix = fix;
			m_hidewhenempty = getBool("field." + fix + ".hideempty");
			m_emptystringisnull = getBool("field." + fix + ".emptyisnull");
			m_zeroisnull = getBool("field." + fix + ".zeroisnull");
			m_hasspacesisnull = getBool("field." + fix + ".spacesisnull");
			String re = getProp("field." + fix + ".regexisnull");
			if(re != null) {
				//-- Compile this into a pattern.
				m_regex = Pattern.compile(re);
			}
		}

		public boolean hideWhenEmpty() {
			return m_hidewhenempty;
		}

		public boolean hasZeroIsNull() {
			return m_zeroisnull;
		}

		public boolean hasEmptyStringIsNull() {
			return m_emptystringisnull;
		}

		public boolean hasSpacesIsNull() {
			return m_hasspacesisnull;
		}

		public Pattern getPattern() {
			return m_regex;
		}
	}

	public CSVToXMLConverter(Properties p) {
		m_prop_p = p;
	}

	String getProp(String key) {
		return m_prop_p.getProperty(key);
	}

	boolean getBool(String prop) {
		String s = getProp(prop);
		if(s == null || s.trim().length() == 0)
			return false;
		s = s.trim().toLowerCase();
		if(s.startsWith("y") || s.startsWith("t") || s.equals("on"))
			return true;

		return false;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Stream converter.									*/
	/*--------------------------------------------------------------*/
	public void convertFile(iRecordReader rr, Writer w) throws IOException {
		String head = getProp("xml.heading");
		if(head != null)
			w.write(head + "\n");

		while(rr.nextRecord()) {
			convertRecord(rr, w);
		}

		String tail = getProp("xml.tail");
		if(tail != null)
			w.write(tail + "\n");
	}

	private void inc(Writer w) {
		if(w instanceof IndentWriter)
			((IndentWriter) w).inc();
	}

	private void dec(Writer w) {
		if(w instanceof IndentWriter)
			((IndentWriter) w).dec();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Record converter.									*/
	/*--------------------------------------------------------------*/
	public void convertRecord(iRecordReader rr, Writer w) throws IOException {
		//-- Is a start tag requested?
		String st = getProp("xml.recordtag");
		if(st != null) {
			w.write("<" + st + ">\n");
			inc(w);
		} else {
			String s = getProp("xml.recordstart");
			if(s != null)
				w.write(s + "\n");
			inc(w);
		}

		//-- Is there an ordered conversion?
		String ordered = getProp("field.order");
		if(ordered == null) {
			//-- Just convert one by one.
			for(int i = 0; i < rr.size(); i++)
				convFieldByNR(rr, w, i);
		} else {
			//-- Get the field#s one by one...
			IntTokenizer it = new IntTokenizer(ordered, " \t,;");
			while(it.hasMoreTokens()) {
				int i = -1;
				try {
					i = it.nextInt();
				} catch(Exception x) {
					throw new IOException("Error in field.order: " + x);
				}
				if(i >= 0)
					convFieldByNR(rr, w, i);
			}
		}

		//-- Last..
		if(st != null) {
			dec(w);
			w.write("</" + st + ">\n");
		} else {
			dec(w);
			String s = getProp("xml.recordend");
			if(s != null)
				w.write(s + "\n");
		}
	}


	private void convFieldByNR(iRecordReader rr, Writer w, int i) throws IOException {
		//-- Handle field.x.before
		String bef = getProp("field." + i + ".before");
		if(bef != null)
			w.write(bef);

		//-- Handle field data
		iInputField f = rr.getField(i);
		String value = f.getValue();
		FieldOptions fo = getFieldOptions(i);
		if(f.isEmpty())
			value = null;
		else {
			if(value.length() == 0) // Zero-length string?
			{
				if(fo.hasEmptyStringIsNull()) // And empty string is null?
					value = null;
			} else if(value.trim().length() == 0 && fo.hasSpacesIsNull()) {
				value = null;
			}
			if(value != null && value.trim().equals("0") && fo.hasZeroIsNull())
				value = null;
			if(value != null && fo.getPattern() != null) {
				Matcher m = fo.getPattern().matcher(value);
				if(m.matches())
					value = null;
			}
		}

		//-- Handle whatever has to happen for empty fields.
		if(value == null) {
			if(fo.hideWhenEmpty())
				value = "";
			else {
				//-- If an alternate value for the field is present use that
				String alt = getProp("field." + i + ".ifempty");
				if(alt != null)
					w.write(alt);
			}
		}

		if(value == null && fo.hideWhenEmpty())
			value = "";
		if(value != null) {
			if(convertField(i, value, w))
				w.write("\n");
		}

		//-- Handle field.x.after
		bef = getProp("field." + i + ".after");
		if(bef != null)
			w.write(bef);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Field converter.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Converts a single field as per the directions in the
	 * properties file.
	 *
	 * @param fnr		The CSV field # starting at 0.
	 * @param input		The value read from that field
	 * @param ow		The writer to dump the shit to.
	 */
	public boolean convertField(int fnr, String input, Writer ow) throws IOException {
		Converter c = getConverter(fnr); // Get this-field's converter
		if(c == null) // Is to be ignored?
			return false;
		return c.convert(input, ow); // Ask it to convert.
	}

	private FieldOptions getFieldOptions(int ix) {
		while(m_opt_al.size() <= ix)
			m_opt_al.add(new FieldOptions(ix));
		return (FieldOptions) m_opt_al.get(ix);
	}

	private Converter getConverter(int fnr) {
		if(fnr < m_conv_al.size()) {
			Converter c = (Converter) m_conv_al.get(fnr);
			if(c != null)
				return c;
		}

		//-- Make a converter. What kind is needed?
		Converter c = null;
		String kbase = "field." + fnr + ".";
		String s = getProp(kbase + "tag"); // Is TAG code?
		if(s != null)
			c = new TagConverter(fnr, s); // Create a TAG converter.
		else {
			//-- A full-format converter?
			s = getProp(kbase + "xml");
			if(s != null)
				c = new FormatConverter(fnr, s);
		}

		if(c == null)
			c = new IgnoreConverter();

		//-- Make room in the table for the converter.
		while(m_conv_al.size() <= fnr)
			m_conv_al.add(null);
		m_conv_al.set(fnr, c);
		return c;
	}

	public String getReplaceVariable(String name) {
		return null;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Converters.....										*/
	/*--------------------------------------------------------------*/
	private class TagConverter extends Converter {
		private String	m_tag;

		public TagConverter() {
		}

		public TagConverter(int fnr, String tag) {
			m_tag = tag;
		}

		@Override
		public boolean convert(String value, Writer ow) throws IOException {
			StringBuffer sb = new StringBuffer();
			sb.append('<');
			sb.append(m_tag);
			sb.append('>');
			ow.write(sb.toString());
			value = StringTool.xmlStringize(value);

			ow.write(value);

			sb.setLength(0);
			sb.append("</");
			sb.append(m_tag);
			sb.append('>');
			ow.write(sb.toString());
			return true;
		}
	}

	private static abstract class Formatter {
		public Formatter() {
		}

		abstract public void output(Writer w) throws IOException;
	}

	private static class StringFormatter extends Formatter {
		private String	m_s;

		public StringFormatter(String s) {
			m_s = s;
		}

		@Override
		public void output(Writer w) throws IOException {
			w.write(m_s);
		}
	}

	class VarFormatter extends Formatter {
		private String			m_name;

		private FormatConverter	m_fc;

		public VarFormatter(FormatConverter fc, String name) {
			m_fc = fc;
			m_name = name;
		}

		@Override
		public void output(Writer w) throws IOException {
			String v = m_fc.getVariable(m_name);
			if(v != null) {
				v = StringTool.xmlStringize(v);
				w.write(v);
			}
		}
	}

	/**
	 * Converts by writing the entire string and replacing parameter
	 * values embedded as %name%.
	 * Created on Jan 20, 2005
	 * @author jal
	 */
	private class FormatConverter extends Converter {
		private Formatter[]	m_format_ar;

		//		private int			m_fnr;

		/** The current value being formatted. */
		private String		m_value;

		public FormatConverter(int fnr, String format) {
			//			m_fnr = fnr;
			setFormat(format);
		}

		public String getVariable(String name) {
			if(name.equalsIgnoreCase("value") || name.equalsIgnoreCase("v"))
				return m_value;
			else
				return getReplaceVariable(name);
		}

		@Override
		public boolean convert(String value, Writer w) throws IOException {
			if(m_format_ar.length == 0)
				return false;
			m_value = value;
			for(int i = 0; i < m_format_ar.length; i++)
				m_format_ar[i].output(w);
			return true;
		}

		public void setFormat(String format) {
			//-- Strip into name and string pairs.
			int len = format.length();
			int ix = 0;
			ArrayList al = new ArrayList();
			while(ix < len) {
				int pos = format.indexOf('%', ix);
				int ran = pos < 0 ? len : pos;
				if(ran > ix)
					al.add(new StringFormatter(format.substring(ix, ran)));
				if(pos == -1)
					break;

				//-- Move to get a name to replace.
				ix = ran + 1;
				int epos = format.indexOf('%', ix); // Get terminator.
				if(epos == -1)
					throw new IllegalStateException("The format string '" + format + "' is missing a second '%' mark for the name starting at " + ix);
				if(epos == ix) // %% => use single %
					al.add(new StringFormatter("%"));
				else {
					String name = format.substring(ix, epos);
					ix = epos + 1;
					al.add(new VarFormatter(this, name.toLowerCase()));
				}
			}

			//-- all done.
			m_format_ar = (Formatter[]) al.toArray(new Formatter[al.size()]);
		}
	}

	private class IgnoreConverter extends Converter {
		public IgnoreConverter() {
		}

		@Override
		public boolean convert(String s, Writer w) {
			return false;
		}
	}
}
