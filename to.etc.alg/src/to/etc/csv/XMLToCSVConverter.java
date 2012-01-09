package to.etc.csv;

/**
 * @author mbp
 * january 2005
 *
 * Converts the contents of a node in an XML document to a CSV format.
 * The conversion is described by properties in a properties file.
 * The XML under the input node is expected to conform to the
 * structure and tag-naming described in the properties.
 *
 * The format of the resulting CSV (field delimiter, separator,
 * how to treat null values etc) can als be controlled by properties.
 * The output CSV is written to anything that implements the Writer
 * interface.
 *
 * This class is intended as a general purpose converter.
 * A test class exists in the VFO package:
 *
 * 		nl.nccwcasa.qd.TestXMLToCSVConverter
 */

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

import to.etc.xml.*;

/**
 * @author mbp
 * @deprecated okt 2005 - use the equally-named converter in the to.mumble.xml pachkage in this same lib.
 */
@Deprecated
public class XMLToCSVConverter {
	// properties that influence the "syntax" of the CSV

	// field delimiter char, surrounds a data value in a column
	// default is quotes
	char						m_csv_delimiter				= '"';

	// field separator char, separates two columns in a line
	// default is comma
	char						m_csv_separator				= ',';

	// line terminator sequence, default UNIX
	private String				m_csv_lineterm				= "\n";

	// how to escape the field delimiter when it is present in
	// the data contents of a column
	static private final int	CSV_ESC_DEL_NONE			= 0;				// don't escape, not recommended

	static private final int	CSV_ESC_DEL_BSLASH			= 1;				// prefix the delimiter with a backslash

	static private final int	CSV_ESC_DOUBLE				= 2;				// output two delimiter chars

	int							m_csv_esc_delimiters		= CSV_ESC_DOUBLE;


	// wether or not to escape newline characters in the data contents
	// is governed by having a replacement string or not.
	// The default is "don't bother"
	String						m_csv_newline_replacement	= null;

	// This list holds objects that describe how to obtain and format
	// data for one CSV column. The objects are ordered in the
	// list by the column number that they represent.
	// Gaps in column number are allowed, the intermediate
	// (missing) columns will be rendered as if a null value
	// was retrieved from the XML for that column number,
	// and the governing CSV options are used to render this value.
	private ArrayList			m_columns_al				= new ArrayList();

	/*------------------------------------------------------*/
	/* OBJECT DESCRIBING TRANSLATION FOR 1 COLUMN			*/
	/*------------------------------------------------------*/

	// This class describes all we known about one column in the
	// CSV output. The idea is to create a sequence of these
	// object filling in all peculiarities of the CSV columns
	// as read from the properties file.
	// The we use these definitions to render the data to CSV.
	private class CSVColumnData {
		// sequence (or order) of this column,
		// counting from 1 for the left-most on the line
		int				m_column;

		// surround the value for this column with field delimiter
		// default : yes, but sometimes for an int value we want no instead
		private boolean	m_delimited			= true;

		// what to output to CSV in case no value is provided
		// for this column (in this line). Comes in usefull for
		// printing a zero for a missing integer value.
		// default null, means "output an empty string"
		private String	m_output_when_null	= null;

		// This is the XML "datapath" that tells us how to
		// obtain a data value from the XML for one record
		// for this column.
		String			m_xml_path;

		CSVColumnData(int seq, boolean delimited, String output_when_null, String path) {
			m_column = seq;
			m_delimited = delimited;
			// process the "null" value for embedded field delimiters
			// (not bloody likely, but better safe than sorry)
			m_output_when_null = escapeDelimiters(output_when_null);
			m_xml_path = path;
		}

		// prints the value according to the specs to the Writer
		protected void printValue(String value, Writer w) throws IOException {
			// do we need to print an "open quote"
			if(m_delimited)
				w.write(m_csv_delimiter); // yes

			// null value or empty string both count as "not known"
			if((value == null) || (value.length() == 0)) {
				// note: null value was escaped for delimiters
				// but not for newlines when it was filled in.
				if(m_output_when_null != null)
					w.write(m_output_when_null);
			} else {
				// output the escaped value
				if(m_csv_esc_delimiters != CSV_ESC_DEL_NONE)
					value = escapeDelimiters(value);
				if(m_csv_newline_replacement != null)
					value = escapeNewlines(value);
				w.write(value);
			}
			// do we need to print a closing quote ?
			if(m_delimited)
				w.write(m_csv_delimiter); // yes
		}
	}

	// A comparator for sorting CSVColumnData on column
	static protected final Comparator	COLUMNCOMP	= new Comparator() {
														public int compare(Object lo, Object hi) {
															CSVColumnData l = (CSVColumnData) lo;
															CSVColumnData h = (CSVColumnData) hi;
															return (l.m_column - h.m_column);
														}
													};

	/*------------------------------------------------------*/
	/* GENEREAL STUFF RELATED TO CSV						*/
	/*------------------------------------------------------*/

	/**
	 * format the presented value as per the specified handling of
	 * embedded CSV field delimiters in the value (escaping)
	 * If value is null return null, if value contains no
	 * delimiter characters return value as-is.
	 */
	public String escapeDelimiters(String value) {
		if(value == null)
			return null;

		// are we doing any escape formatting at all ?
		if(m_csv_esc_delimiters == CSV_ESC_DEL_NONE)
			return value; // nope, return string as it is.

		// do we need to perform escape formatting (are escape
		// chars present in the value?)
		int pos = value.indexOf(m_csv_delimiter);
		if(pos < 0)
			return value; // nope. Use value as-is

		// Must create one or more escape sequences.
		// assume not more than three for a first estimate.
		int start = 0;
		StringBuffer sb = new StringBuffer(value.length() + 3);
		while(pos > 0) {
			// copy anything between previous and current delimiters
			// to output
			sb.append(value.substring(start, pos));

			// prepend escape sequence
			switch(m_csv_esc_delimiters){
				case CSV_ESC_DEL_BSLASH: {
					// insert one extra backslash (note that java
					// uses the 'DOUBLE' escape sequence for backslashes in strings :)
					sb.append('\\');
				}
					break;

				case CSV_ESC_DOUBLE: {
					// insert one extra delimiter
					sb.append(m_csv_delimiter);
				}
					break;
			}
			// now append the offending delimiter we found in the value
			sb.append(m_csv_delimiter);

			// and loop around for the next delimiter, if any
			start = pos + 1;
			pos = value.indexOf(m_csv_delimiter, start);
		}

		// finally, append whatever comes to the right
		// of the lastly escaped delimiter
		sb.append(value.substring(start));

		// and return what we created in the stringbuffer
		return sb.toString();
	}

	/**
	 * Replace newline characters with something else.
	 * If value is null return null, if value contains no
	 * newline characters return value as-is.
	 */
	public String escapeNewlines(String value) {
		if(value == null)
			return null;

		// are we doing any newline replacement at all ?
		if(m_csv_newline_replacement == null)
			return value; // nope, return string as it is.

		// Do we have one or more newlines ?
		int pos = value.indexOf('\n');
		if(pos < 0)
			return value; // nope

		// Must create one or more escape sequences.
		// assume not more than three for a first estimate.
		int start = 0;
		StringBuffer sb = new StringBuffer(value.length() + 3);
		while(pos > 0) {
			// copy anything between previous and current newline
			sb.append(value.substring(start, pos));

			// replace the newline with its replacement value
			sb.append(m_csv_newline_replacement);

			// and loop around for the next newline, if any
			start = pos + 1;
			pos = value.indexOf('\n', start);
		}
		// finally, append whatever comes to the right
		// of the lastly escaped newline
		sb.append(value.substring(start));

		return sb.toString();
	}

	/*------------------------------------------------------*/
	/* INITIALIZING											*/
	/*------------------------------------------------------*/

	/**
	 * Initializing the array of CSV column descrpiptions from the
	 * properties file could turn into a small bit of a mind-bender.
	 * We do not know in advance how many CSV columns must be written
	 * to one line of CSV output. We could require this number to be
	 * present in the properties file itself but it seems cleaner
	 * when this value could be determined from the other
	 * contents of the properties file.
	 * On the other hand, retrieveing field-related data from
	 * a properties file that also holds non-field related stuff
	 * is tricky too.
	 * I'll settle for this syntax for describing a translation:
	 * all tag-related properties start with tag.nnn where
	 * nnn is an integer that increments with no gaps and has the
	 * sole purpose of identifying the various tags in the XML
	 * by enumeration.
	 * So we have tag.0.tagname=xxx, tag.0.column=mmm, tag.0.quoted=no etc
	 * The end of the enumeration is identified by a missing number.
	 * Each tag must have at least a property stating the tagname
	 * and a property stating the 1-based number of the CSV column.
	 * Note that the enumeration in the properties file does not in
	 * any way impose an ordering of data fields in the XML,
	 * nor in the CSV.
	 *
	 * And still some more tricky stuff: BIS may require us to
	 * fill certain columns with static data instead of getting
	 * values for the CSV column from the XML. E.g. an integer field
	 * that is always to be filled with the value zero or some
	 * such contraption.
	 * This requirement can be met by adding the description of
	 * such a column to the properties file but with a tagname
	 * that will surely never be present in the actual XML data.
	 * In that case, the value of output_when_null will be used.
	 */

	protected void initFields(Properties prop) throws Exception {
		// the enumeration for tags in the properties file
		int eenum = 0;

		String prefix;
		while(true) {
			// construct a new key prefix for the next enum
			prefix = "tag." + eenum + "."; // e.g. "tag.4."

			// if we cannot find a tagname for this enum we think we're ready
			String xmlpath = prop.getProperty(prefix + "xmlpath");
			if(xmlpath == null)
				break;
			xmlpath = xmlpath.trim();
			if(xmlpath.length() < 1)
				throw new Exception("Error in XML-to-CSV properties file: xmlpath is empty for " + eenum);

			// If we cannot at least find the coresponding column index
			// for the CSV file, someone was sloppy and we complain
			String iv = prop.getProperty(prefix + "column");
			if(iv == null)
				throw new Exception("Error in XML-to-CSV properties file: xmlpath present but no column for " + eenum);
			int column = Integer.parseInt(iv);

			// Ok, get the other non-mandatory properties for this enum
			// invert the logic here for convenience
			boolean unquoted = false;

			iv = prop.getProperty(prefix + "quoted");
			if(iv != null) {
				iv = iv.trim().toLowerCase();
				unquoted = iv.startsWith("f") || iv.startsWith("n") || iv.equals("off");
			}

			// leave this one null if not found
			String output_when_null = prop.getProperty(prefix + "output_when_null");

			// Construct a new CSVColumnData to receive the values for this enum
			CSVColumnData cvd = new CSVColumnData(column, !unquoted, output_when_null, xmlpath);

			// append this CSVColumnData to the ArrayList of thingies
			m_columns_al.add(cvd);

			// finally, increment enum value for next xmlpath-to-column
			eenum++;
		}

		// Guard a bit against sillyness in general:
		// if we cannot find the tagname for enum N, then if we can
		// find enum N+1 somebody messed up the properties file
		if(prop.getProperty("tag." + (eenum + 1) + ".xmlpath") != null)
			throw new Exception("Error in XML-to-CSV properties file: detected a gap in the enum for " + eenum);

		// Now, sort the arraylist according to the value of the CSV column index
		Collections.sort(m_columns_al, COLUMNCOMP);
	}

	/**
	 * Initialize the CSV properties that are the same for all columns
	 * If not mentioned specifically they retain the default values
	 * that were set when the data was declared.
	 */
	protected void initCSVProps(Properties prop) {
		String s;
		s = prop.getProperty("csv.delimiter");
		if(s != null)
			m_csv_delimiter = s.charAt(0);
		s = prop.getProperty("csv.separator");
		if(s != null)
			m_csv_separator = s.charAt(0);
		s = prop.getProperty("csv.lineterm");
		if(s != null)
			m_csv_lineterm = s;
		s = prop.getProperty("csv.esc.delimiter");
		if(s != null)
			m_csv_esc_delimiters = Integer.parseInt(s);
		m_csv_newline_replacement = prop.getProperty("csv.esc.newline");
	}

	/**
	 * initialize delegates to initializing overall CSV properties
	 * and processing the column definitions.
	 */
	public void init(Properties prop) throws Exception {
		initCSVProps(prop);
		initFields(prop);
	}

	/*------------------------------------------------------*/
	/* FINDING NODES IN THE XML 							*/
	/*------------------------------------------------------*/

	/**
	 * for debug
	 */
	//	private void printnode( Node nd)
	//	{
	//		if( nd == null)
	//			System.out.println( "NULL NODE ENCOUNTERED");
	//		else
	//		{
	//			System.out.print( "NODE type=" + nd.getNodeType());
	//			System.out.print( ", name=" + nd.getNodeName());
	//			System.out.println( ", value=" + nd.getNodeValue());
	//		}
	//	}

	/**
	 * Returns a node given the XML rootnode and the 'xmlpath'
	 * that must be followed from there to get at the node.
	 * If node not found, returns null.
	 * The method may call itself recursively to delve into deeper
	 * levels below the current node.
	 * Presently, it has only been tested for a simple XML structure:
	 * payload
	 *   record
	 *     item
	 * 		 subitem
	 * 		   .. etc
	 *
	 * TODO: write better explanation then move to DomTools
	 */

	static public Node findNodePath(Node node, String xmlpath) {
		// see if we must recurse. This is the case when the path
		// contains a dot
		int pos = xmlpath.indexOf('.');
		if(pos > 0) {
			// yes, recurse. break up xmlpath and find the new
			// 'look inside here with path' node.
			// If not found, return null
			//			System.out.println("FINDNODE RECURSING");
			String nodename = xmlpath.substring(0, pos);
			String subpath = xmlpath.substring(pos + 1);

			// Advance across siblings of node until one matches
			// the new nodename
			//			printnode( node);
			while((node != null) && !(nodename.equalsIgnoreCase(node.getNodeName()))) {
				node = node.getNextSibling();
				//				printnode(node);
			}
			// If exchaused, not found
			if(node == null)
				return null;

			// Now try to find a node below this node using
			// the remainder of the "xmlpath"
			Node sublevel = node.getFirstChild();
			//			printnode( sublevel);

			// If no childnodes at all return null
			if(sublevel == null)
				return null;

			// recurse
			return findNodePath(sublevel, subpath);
		}

		// Okay, no further delving into sublevels.
		// Find the node with the given name on this level
		//		printnode( node);
		while((node != null) && (!node.getNodeName().equalsIgnoreCase(xmlpath))) {
			node = node.getNextSibling();
			//			printnode( node);
		}
		// Return value may be nul when node not found
		return node;
	}

	/**
	 *	Find a node and return the data value. If node not found,
	 *	returns null.
	 */
	protected String findValue(Node node, String xmlpath) {
		Node nd = findNodePath(node, xmlpath);
		if(nd == null)
			return null;
		return DomTools.textFrom(nd);
	}

	/*------------------------------------------------------*/
	/* PERFORMING THE CONVERSION 							*/
	/*------------------------------------------------------*/

	/**
	 * Read the XML data under the given 'record' node and write
	 * it to the writer, formatted as CSV.
	 */

	protected void convertRecord(Node record, Writer w) throws Exception {
		// walk the list of CSVColumnData objects, while
		// watching for gaps or overlaps in the column
		// index values. Overlap is bad, gap is handled
		// by outputting a series of empty columns

		// Note the 'record' node is the parent for all data nodes,
		// so must descend one level
		Node firstdatalevelnode = record.getFirstChild();

		int eenum; // walk all objects by this counter
		int prevcol = 0; // keep track of which column we're doing by this counter
		boolean first = true; // used to suprress comma before first column value
		for(eenum = 0; eenum < m_columns_al.size(); eenum++) {
			// obtain the next column descriptor from the list
			CSVColumnData cvd = (CSVColumnData) m_columns_al.get(eenum);

			// Inspect the column index for overlaps or gaps
			// if CSV column number did not change, this is an error
			int diff = cvd.m_column - prevcol;
			if(diff == 0)
				throw new Exception("Error in XML-to-CSV properties file: multiple defines for column " + prevcol);

			// If difference cur-prev > 1, must output some empty CSV columns
			while(diff > 1) {
				if(first)
					first = false;
				else
					w.write(m_csv_separator);
				// and write two field delimiters which is default
				// for undefined values
				w.write(m_csv_delimiter);
				w.write(m_csv_delimiter);
				diff--;
			}
			// update previous column number
			prevcol = cvd.m_column;

			// see if we nust output a separator char
			if(first)
				first = false;
			else
				w.write(m_csv_separator);


			// Now look for a data value to write for this column
			String value = findValue(firstdatalevelnode, cvd.m_xml_path);

			// Value may be null but that should be handled correctly.
			cvd.printValue(value, w);
		}
		// Finally, print a line terminator sequence
		w.write(m_csv_lineterm);
	}

	/**
	 * Converts the data under the XML node to CSV.
	 * The property "tag.recordtag" holds the "xmlpath"
	 * which we can use to get at the nodes that contain
	 * data representing one record, starting at node "payload"
	 * @return the number of records converted
	 */

	public int convert(Node firstrecordlevelnode, Writer w, Properties prop) throws Exception {
		// get the path that identifies record nodes within
		// the payload.
		String recordtag = prop.getProperty("tag.recordtag");

		// sanity check
		if(recordtag == null)
			throw new Exception("Error in XML-to-CSV: property \"xml.recordtag\" not found.");

		// not supported (yet ?): recordtag is complex xml "path",
		// so recordtag must lie directly under payload
		if(recordtag.indexOf('.') > 0)
			throw new Exception("Not supported in XML-to-CSV: complex xmlpath to recordtag (no dots allowed in xml.recordtag)");

		// Intialize translation XML to CSV from properties file
		init(prop);

		// See if we can locate the first "recordtag"
		// if not, zero bytes are written to the Writer
		Node record = findNodePath(firstrecordlevelnode, recordtag);
		int count = 0;
		while(record != null) {
			convertRecord(record, w);
			count++;

			// skip to next record-level node
			record = record.getNextSibling();
			//			printnode( record);
			while((record != null) && (!record.getNodeName().equalsIgnoreCase(recordtag))) {
				record = record.getNextSibling();
				//				printnode( record);
			}
		}
		return count;
	}

	/*------------------------------------------------------*/
	/* MAIN METHOD FOR TESTING AND DEBUGGING				*/
	/*------------------------------------------------------*/

	/** SEE VFO PROJECT  :  nl.nccwcasa.qd.TestXMLToCSVConverter */
}
