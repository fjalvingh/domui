/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.webapp.ajax.renderer;

import java.io.*;
import java.util.*;

import to.etc.lexer.*;

/**
 * This class parses JSON streams and creates a generic Java
 * structure consisting of arrays and map's representing the
 * JSON data.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 19, 2006
 */
public class JSONParser extends ReaderTokenizerBase {
	public JSONParser(final Reader r) {
		super("JSON Source", r);
		setReturnNewline(false);
		setReturnWhitespace(false);
	}

	public Object parse() throws Exception {
		nextToken();
		return parseItem();
	}

	/**
	 * Override to scan Javascript string constants.
	 * @see to.etc.lexer.ReaderTokenizerBase#scanString()
	 */
	@Override
	protected int scanString() throws IOException {
		int qc = LA(); // Get quote start
		accept();
		startToken();
		for(;;) {
			int c = LA();
			if(c == qc)
				break;
			else if(c == -1)
				throw new JSONParserException("Unexpected EOF in string constant");
			else if(c == '\n')
				throw new JSONParserException("Unexpected newline in string constant");
			else if(c == '\\') {
				accept();
				c = LA();
				switch(c){
					default:
						throw new JSONParserException("Unexpected string escape sequence \\" + (char) c);
					case '"':
					case '\'':
					case '\\':
					case '/':
						copy();
						break;
					case 'b':
						append('\b');
						accept();
						break;
					case 'f':
						append('\f');
						accept();
						break;
					case 'n':
						append('\n');
						accept();
						break;
					case 'r':
						append('\r');
						accept();
						break;
					case 't':
						append('\t');
						accept();
						break;
					case 'u': {
						//- UNICODE hex sequence
						accept();
						int val = 0;
						for(int i = 0; i < 4; i++) {
							c = LA();
							int d = Character.digit(c, 16);
							if(d == -1)
								throw new JSONParserException("Invalid hex digit " + (char) c + "in string unicode escape sequence");
							val = val * 16 + d;
							accept();
						}
						append(val);
					}
						break;
				}
			} else {
				copy();
			}
		}
		accept();
		return T_STRING;
	}

	/**
	 * Parse the JSON input and return a JSON data structure.
	 * @return
	 * @throws Exception
	 */
	public Object parseItem() throws Exception {
		int token = getLastToken();
		switch(token){
			default:
				throw new JSONParserException("Unexpected token '" + getCopied() + "' in input");

			case '{':
				return parseObject();
			case '[':
				return parseArray();
			case ReaderScannerBase.T_EOF:
				return null;
			case ReaderScannerBase.T_IDENT:
				//-- 'undefined' is allowed and treated as 'null'
				if(getCopied().equals("undefined") || getCopied().equals("null")) {
					nextToken();
					return null;
				}
				if(getCopied().equals("true")) {
					nextToken();
					return Boolean.TRUE;
				} else if(getCopied().equals("false")) {
					nextToken();
					return Boolean.FALSE;
				}
				throw new JSONParserException("Unexpected identifier '" + getCopied() + "'");
			case ReaderScannerBase.T_IPADDR:
				throw new JSONParserException("Unexpected IP address '" + getCopied() + "'");
			case ReaderScannerBase.T_NUMBER:
				return parseNumber();
			case ReaderScannerBase.T_STRING:
				String txt = getCopied();
				nextToken();
				return txt;
			case '-':
				token = nextToken();
				if(token != ReaderScannerBase.T_NUMBER)
					throw new JSONParserException("Unexpected token '" + getCopied() + "' after '-' sign; expecting a number");
				Number v = parseNumber();
				return Long.valueOf(-v.longValue());
		}
	}

	private Number parseNumber() throws Exception {
		String copied = getCopied();
		Number res;
		if(copied.contains(".") || copied.contains("e") || copied.contains("E"))
			res = Double.valueOf(copied);
		else
			res = Long.decode(copied);
		nextToken();
		return res;
	}

	private Object parseKey() throws Exception {
		int token = getLastToken();
		switch(token){
			default:
				throw new JSONParserException("Unexpected token '" + getCopied() + "' in input");
			case '{':
				throw new JSONParserException("An object cannot be the key of an object item");
			case '[':
				throw new JSONParserException("An array cannot be the key of an object item");
			case ReaderScannerBase.T_EOF:
				throw new JSONParserException("Unexpected EOF while parsing an object key");
			case ReaderScannerBase.T_IPADDR:
				throw new JSONParserException("Unexpected IP address '" + getCopied() + "' in input");
			case ReaderScannerBase.T_NUMBER:
				return parseNumber();
			case ReaderScannerBase.T_IDENT:
			case ReaderScannerBase.T_STRING:
				String txt = getCopied();
				nextToken();
				return txt;
		}
	}

	/**
	 * Parses an object/map structure. When called the starting '{' has been
	 * seen already. The base formats are:
	 * <pre>
	 * 	object ::= '{' pairlist '}'
	 *  pairlist ::= pair { ',' pair }*
	 *  pair ::= key ':' value
	 * 	key ::= STRING | ID | NUMBER;
	 * </pre>
	 * @return
	 */
	private Object parseObject() throws Exception {
		Map<Object, Object> res = new HashMap<Object, Object>();
		nextToken();
		for(;;) {
			if(getLastToken() == '}') {
				//-- Empty map or ,} syntax
				nextToken();
				return res;
			}

			Object v = parseKey(); // Parse whatever is there: it should be keyable.
			if(!(v instanceof Number || v instanceof String))
				throw new JSONParserException("Invalid 'key' object for object notation: " + v);
			if(getLastToken() != ':')
				throw new JSONParserException("Expecting a ':' after the key value");
			nextToken();
			Object val = parseItem(); // Parse any item
			if(null != res.put(v, val)) // Put in map
				throw new JSONParserException("Duplicate 'key' value: '" + v + "'");

			//-- Next is either comma or }
			if(getLastToken() == '}') {
				nextToken();
				return res;
			} else if(getLastToken() != ',')
				throw new JSONParserException("Expecting a ',' or a '}' after an object item:value pair, but I got an '" + getCopied() + "'");
			nextToken();
		}
	}

	/**
	 * Parse a JSON array value.
	 * @return
	 * @throws Exception
	 */
	private Object parseArray() throws Exception {
		nextToken();
		List<Object> res = new ArrayList<Object>();
		for(;;) {
			if(getLastToken() == ']') {
				//-- Empty array or ,] notation
				nextToken();
				return res;
			}

			//-- Get a value - any item
			Object val = parseItem();
			res.add(val);
			if(getLastToken() == ']') {
				nextToken();
				return res;
			} else if(getLastToken() != ',')
				throw new JSONParserException("Expecting a ',' or a ']' after an array value, but I got an '" + getCopied() + "'");
			nextToken();
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Main entrypoints: class initializers using JSON		*/
	/*--------------------------------------------------------------*/
	/**
	 * Parse the input, and try to assign the JSON input to a created
	 * instance of the specified class.
	 */
	static public Object parseJSON(final String in, final Class< ? > totype) throws Exception {
		StringReader r = new StringReader(in);
		try {
			return parseJSON(r, totype);
		} finally {
			try {
				r.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Parse the input stream as a JSON object, and try to assign it's content to
	 * the specified class.
	 * @param r
	 * @param totype
	 * @return
	 * @throws Exception
	 */
	static public Object parseJSON(final Reader r, final Class< ? > totype) throws Exception {
		throw new IllegalStateException("Not implemented yet");
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Main entrypoints - returning a generic JSON struct	*/
	/*--------------------------------------------------------------*/

	/**
	 * Parse the string as a JSON structure. This returns a generic Java
	 * structure representing the JSON request, where all arrays are
	 * returned as List&lt;Object&gt;, all Javascript objects as a Map&lt;Object, Object&gt;
	 * and all primitives are translated to a reasonable Java type.
	 */
	static public Object parseJSON(final String in) throws Exception {
		StringReader r = new StringReader(in);
		try {
			return parseJSON(r);
		} finally {
			try {
				r.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Parse the input stream as a JSON structure. This returns a generic Java
	 * structure representing the JSON request, where all arrays are
	 * returned as List&lt;Object&gt;, all Javascript objects as a Map&lt;Object, Object&gt;
	 * and all primitives are translated to a reasonable Java type.
	 */
	static public Object parseJSON(final Reader in) throws Exception {
		JSONParser p = new JSONParser(in);
		return p.parse();
	}


	static public void main(final String[] args) {
		try {
			File src = new File("./test.json");
			Reader r = new InputStreamReader(new FileInputStream(src), "utf-8");
			Object val = parseJSON(r);

			System.out.println("Parsed ok, result is " + val);
		} catch(Exception x) {
			x.printStackTrace();
		}
	}
}
