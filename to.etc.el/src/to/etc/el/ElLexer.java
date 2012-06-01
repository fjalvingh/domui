/*
 * DomUI Java User Interface - shared code
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
package to.etc.el;

import java.math.*;

import javax.servlet.jsp.el.*;

/**
 * A quicky lexer for EL, the expression language. This hand-coded lexer
 * does not return objects but ints to specify reserved words and token
 * codes. It saves token values in variables that can be gotten before
 * the next token is requested. The lexer keeps track of a string position
 * for a token only; it does NOT handle line numbering.
 *
 * Created on May 17, 2005
 * @author jal
 */
public class ElLexer {
	/** The string being lexed. */
	private String m_in;

	/** The end position of the input string. */
	private int m_len;

	/** The "current" input position. */
	private int m_pos;

	/** The position where the "last" token started. */
	private int m_token_pos;

	private boolean m_conforming;

	/** The token text collector buffer. This contains all chars that belong to a token */
	private StringBuffer m_text_sb = new StringBuffer(64);

	/** The identifier parsed. */
	private String m_text;

	private double m_double;

	private BigInteger m_integer;

	private ElToken m_lastToken;

	public ElLexer() {}

	public ElLexer dup() {
		ElLexer lx = new ElLexer();
		lx.m_conforming = m_conforming;
		lx.m_double = m_double;
		lx.m_in = m_in;
		lx.m_integer = m_integer;
		lx.m_len = m_len;
		lx.m_pos = m_pos;
		lx.m_text = m_text;
		lx.m_text_sb.append(m_text_sb);
		lx.m_token_pos = m_token_pos;
		lx.m_lastToken = m_lastToken;
		return lx;
	}

	public ElLexer(String s) {
		set(s);
	}

	public void set(String s) {
		m_in = s;
		m_len = s.length();
		m_pos = 0;
		m_token_pos = 0;
		m_text = null;
	}

	public final String getText() {
		return m_text;
	}

	public final BigInteger getInt() {
		return m_integer;
	}

	public final double getDouble() {
		return m_double;
	}

	public final int getTokenPos() {
		return m_token_pos;
	}

	public final String getInput() {
		return m_in;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Simple input traversal.								*/
	/*--------------------------------------------------------------*/
	private int LA() {
		if(m_pos >= m_len) // At/past the end?
			return -1; // return eof
		return (m_in.charAt(m_pos) & 0xffff); // Return the current character and advance.
	}

	//	private int		LA(int pos)
	//	{
	//		if(m_pos + pos >= m_len)
	//			return -1;
	//		return ((int)m_in.charAt(m_pos+pos) & 0xffff);	// Return the current character and advance.
	//	}
	//	private void consume()
	//	{
	//		m_pos++;
	//	}
	//	private void consume(int n)
	//	{
	//		m_pos	+= n;
	//	}
	private int nextc() {
		if(m_pos >= m_len) // At/past the end?
			return -1; // return eof
		return (m_in.charAt(m_pos++) & 0xffff); // Return the current character and advance.
	}

	//	private void	skipws()
	//	{
	//		while(m_pos < m_len)
	//		{
	//			char c = m_in.charAt(m_pos);
	//			if(! Character.isWhitespace(c))
	//				return;
	//			m_pos++;
	//		}
	//	}
	private void reset() {
		m_text_sb.setLength(0);
	}

	private void append(int c) {
		m_text_sb.append((char) c);
	}

	private void checkConforming(ElToken t) throws ELException {
		if(m_conforming)
			throw new ELException("The token '" + t + "' is not allowed by the JSP specification.");
	}

	private void error(int pos, String s) throws ELException {
		throw new ELException(s + " [at position " + pos + " of expression '" + m_in + "']");
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	String scanner.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Scans string constants.
	 */
	private ElToken scanString(int c) throws ELException {
		reset();
		int endc = c;
		for(;;) {
			c = nextc(); // Get the next string character & advance,
			if(c == endc) {
				m_text = m_text_sb.toString();
				m_text_sb.setLength(0);
				return ElToken.StringLiteral; // Exit: end of string accepted.
			} else if(c == -1)
				error(m_token_pos, "Missing string end quote");
			else if(c == '\\') // Escape?
			{
				int nc = LA(); // Char after it
				switch(nc){
					case '\'':
						m_pos++;
						append('\''); // Append unquoted
						break;

					case '\"':
						m_pos++;
						append('"');
						break;

					case '\\':
						m_pos++;
						append('\\');
						break;

					default:
						append('\\');
						break;
				}
			} else {
				append(c);
			}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Number scanner.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Scans both integer and float numbers.
	 */
	private ElToken scanNumber(int c) throws ELException {
		ElToken t = c == '.' ? ElToken.FloatLiteral : ElToken.IntLiteral;
		reset(); // Clean the initial string
		append(c);

		//-- Step 1: scan normal digits
		for(;;) {
			c = LA();
			if(c < '0' || c > '9') // Not a normal digit?
				break; // Then exit
			append(c);
			m_pos++;
		}

		//-- We're at the end of the 1st integer stretch. Is the next part indicative of a float?
		if(c == '.') // Dot: is a float.
		{
			if(t == ElToken.FloatLiteral) // Already float-> duplicate '.'
				error(m_pos, "Duplicate '.' in floating-point literal");

			//-- Valid decimal point. Assume float,
			t = ElToken.FloatLiteral; // Set as float now
			m_pos++;
			append('.'); // Append the dot

			//-- The dot must be followed by 1 or more digits
			int count = 0;
			for(;;) {
				c = LA();
				if(c < '0' || c > '9')
					break;
				append(c);
				count++;
				m_pos++;
			}
			if(count == 0)
				error(m_pos - 1, "Invalid floating-point literal: there must be at least one digit after the decimal-point");
		}

		if(c == 'e' || c == 'E') // Exponent?
		{
			m_pos++; // Accept
			append(c); // And add,
			t = ElToken.FloatLiteral; // Is a float thing now
			c = LA(); // Next,
			if(c == '+' || c == '-') // Sign?
			{
				m_pos++; // Accept
				append(c); // And add,
			}

			//-- Now get at least 1 digit to end the exponent.
			int count = 0;
			for(;;) {
				c = LA();
				if(c < '0' || c > '9')
					break;
				append(c);
				count++;
				m_pos++;
			}
			if(count == 0)
				error(m_pos - 1, "Invalid floating-point literal: there must be at least one digit after the 'e' exponent");
		}

		//-- We're done. Convert to some value depending on the type.
		m_text = m_text_sb.toString();
		m_text_sb.setLength(0);
		if(t == ElToken.FloatLiteral) {
			try {
				m_double = Double.parseDouble(m_text);
			} catch(Exception x) {
				error(m_token_pos, "Invalid double literal '" + m_text + "'");
			}
		} else {
			try {
				m_integer = new BigInteger(m_text);
			} catch(Exception x) {
				error(m_token_pos, "Invalid integer literal '" + m_text + "'");
			}
		}
		return t;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Identifier scanner.									*/
	/*--------------------------------------------------------------*/
	private ElToken scanIdentifier(int c) throws ELException {
		if(!Character.isJavaIdentifierStart(c))
			error(m_pos - 1, "Illegal identifier start character '" + (char) c + "' (int code=" + c + ")");
		reset();
		append(c);

		//-- Scan for the rest,
		for(;;) {
			c = LA();
			if(!Character.isJavaIdentifierPart(c))
				break;
			append(c);
			m_pos++;
		}
		m_text = m_text_sb.toString();
		m_text_sb.setLength(0);

		//-- Handle predefined identifiers as quickly as possible
		// and		4
		// div		3
		// empty	4
		// eq		2
		// false	4
		// ge		3
		// gt		4
		// le		1
		// lt		4
		// mod		3
		// ne		4
		// not		2
		// null		4
		// or		3
		// true
		int res = "le".compareTo(m_text);
		if(res == 0)
			return ElToken.LessThan;
		if(res > 0) {
			res = "eq".compareTo(m_text);
			if(res == 0)
				return ElToken.Equal;
			if(res > 0) {
				res = "div".compareTo(m_text);
				if(res == 0)
					return ElToken.Divide;
				if(res > 0) {
					res = "and".compareTo(m_text);
					if(res == 0)
						return ElToken.LogicalAnd;

					//-- No more tokens
				} else // bigger than "div"
				{
					res = "empty".compareTo(m_text);
					if(res == 0)
						return ElToken.Empty;
					//-- no more tokens
				}
			} else // bigger than "eq"
			{
				res = "ge".compareTo(m_text);
				if(res == 0)
					return ElToken.GreaterThanOrEqual;
				if(res > 0) {
					res = "false".compareTo(m_text);
					if(res == 0)
						return ElToken.False;
					//-- No more tokens
				} else // bigger than "ge"
				{
					res = "gt".compareTo(m_text);
					if(res == 0)
						return ElToken.GreaterThan;
					//-- no more tokens
				}
			}
		} else // bigger than "le"
		{
			res = "not".compareTo(m_text);
			if(res == 0)
				return ElToken.LogicalNot;
			if(res > 0) {
				res = "mod".compareTo(m_text);
				if(res == 0)
					return ElToken.Modulus;
				if(res > 0) {
					res = "lt".compareTo(m_text);
					if(res == 0)
						return ElToken.LessThan;
					//-- No more tokens
				} else // > mod
				{
					res = "ne".compareTo(m_text);
					if(res == 0)
						return ElToken.NotEqual;
					//-- No more tokens
				}
			} else // > not
			{
				res = "or".compareTo(m_text);
				if(res == 0)
					return ElToken.LogicalOr;
				if(res > 0) {
					res = "null".compareTo(m_text);
					if(res == 0)
						return ElToken.Null;
					//-- No more tokens
				} else // > or
				{
					res = "true".compareTo(m_text);
					if(res == 0)
						return ElToken.True;
					//-- No more tokens
				}
			}
		}

		//-- No known name: must be identifier
		return ElToken.Id;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Main entrypoint for token handling					*/
	/*--------------------------------------------------------------*/
	public ElToken nextToken() throws ELException {
		return (m_lastToken = nextTokenPrim());
	}

	public ElToken getLastToken() {
		return m_lastToken;
	}

	private ElToken nextTokenPrim() throws ELException {
		m_text = null;
		for(;;) {
			int c = nextc(); // Get and consume
			if(c == -1)
				return ElToken.EOF; // Exit quickly at eof
			else if(Character.isWhitespace(c))
				;
			else {
				//-- We have a meaningful character.. Parse it.
				m_token_pos = m_pos - 1; // Token start
				switch(c){
					case '&': {
						int nc = LA(); // Peek at current loc
						if(nc == '&') // && is logical and
						{
							m_pos++;
							return ElToken.LogicalAnd;
						}
						checkConforming(ElToken.BinaryAnd);
						return ElToken.BinaryAnd; // Single & is binary and
					}

					case '|': {
						int nc = LA();
						if(nc == '|') // || is logical or
						{
							m_pos++;
							return ElToken.LogicalOr;
						}
						checkConforming(ElToken.BinaryOr);
						return ElToken.BinaryOr;
					}

					case '+':
						return ElToken.Plus;

					case '-':
						return ElToken.Minus;

					case '*':
						return ElToken.Multiply;

					case '/':
						return ElToken.Divide;

					case '%':
						return ElToken.Modulus;

					case '>': {
						int nc = LA();
						if(nc == '=') {
							m_pos++;
							return ElToken.GreaterThanOrEqual;
						}
						return ElToken.GreaterThan;
					}

					case '<': {
						int nc = LA();
						if(nc == '=') {
							m_pos++;
							return ElToken.LessThanOrEqual;
						}
						return ElToken.LessThan;
					}

					case '=': {
						//-- Only syntax allowed is ==
						int nc = LA();
						if(nc != '=')
							throw new ELException("Illegal token '=' in expression '" + m_in + "'. Use '==' to test for equality");
						m_pos++;
						return ElToken.Equal;
					}

					case '!': {
						int nc = LA();
						if(nc == '=') {
							m_pos++;
							return ElToken.NotEqual;
						}
						return ElToken.LogicalNot;
					}
					case '.': {
						//-- This can denote a float literal OR the member lookup operator
						int nc = LA();
						if(nc >= '0' && nc <= '9') // Is .[digit] -> number
							return scanNumber(c);
						return ElToken.Dot;
					}

					case '?':
						return ElToken.QuestionMark;
					case ':':
						return ElToken.Colon;
					case ',':
						return ElToken.Comma;
					case '(':
						return ElToken.ParentheseOpen;
					case ')':
						return ElToken.ParentheseClose;
					case '[':
						return ElToken.BraceOpen;
					case ']':
						return ElToken.BraceClose;

					default:
						//-- All other tokens...
						if(c >= '0' && c <= '9')
							return scanNumber(c);
						if(Character.isLetter(c) || c == '_')
							return scanIdentifier(c);
						if(c == '\'' || c == '\"')
							return scanString(c);

						//-- Nothing of this!?
						throw new ELException("Bad character '" + (char) c + " (int code " + c + ") at position " + (m_pos - 1) + " of expression '" + m_in + "'");
				}
			}
		}
	}

	static public void main(String[] args) {
		try {
			ElLexer l = new ElLexer("1+2*3.2e3+'hello \\\"dude\\\"'");

			for(;;) {
				ElToken t = l.nextToken();
				System.out.println("Token is: " + t + ", value=" + l.getText());
				if(t == ElToken.EOF)
					break;
			}
		} catch(Exception x) {
			x.printStackTrace();
		}
	}


}
