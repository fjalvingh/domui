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
package to.etc.qte;

import java.io.*;
import java.util.*;

import javax.servlet.jsp.el.*;

import to.etc.el.*;

/**
 * <p>The Quick Template Compiler. This static class accepts templates from a Reader, compiles
 * them and returns a RuntimeTemplate which can be used to generate the template to any
 * Appendable.
 * </p>
 * <p>The engine is typically used to do very simple template expansions from code. Typical
 * use is to precompile templates from Java resources into Template constants using static
 * initializers, The rest of the code can then expand the template by simply providing an
 * Appendable as sink and a VariableResolver.
 * </p>
 * <p>The template engine uses the EL expression evaluator to allow for expressions in the
 * template. The EL expressions in this engine do not start with ${ ... } or #{ ... } though
 * to make it easier to generate JSP from these templates. The syntax used in the templates is
 * shown below.
 * </p>
 *
 * <p>The templates are very simple and only allow for replacement EL expressions and
 * if/else/endif/loop.</p>
 *
 * <p>Created on Nov 25, 2005
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class TemplateCompiler {
	private enum TokenCode {
		EOF("eof"), LIT("Literal"), EXPR("<%= expression %>"), IF("<%if"), ELSE("<%else%>"), ENDIF("<%endif%>"), ;

		private String m_rep;

		TokenCode(String s) {
			m_rep = s;
		}

		@Override
		public String toString() {
			return m_rep;
		}
	}

	private Reader m_r;

	private String m_ident;

	/** The actual line and column number during the scan. */
	private int m_lnr, m_cnr;

	/** The current token's start line and column */
	private int m_t_lnr, m_t_cnr;

	private boolean m_ateof;

	private int[] m_q = new int[5];

	private int m_qlen;

	private int m_qgetp, m_qputp;

	private StringBuilder m_sb = new StringBuilder();

	private TemplateCompiler(Reader r, String ident) {
		m_r = r;
		m_ident = ident;
		m_cnr = 1;
		m_lnr = 1;
	}

	static public Template compile(Reader r, String ident) throws Exception {
		TemplateCompiler c = new TemplateCompiler(r, ident);
		return c.compile();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Input reader.										*/
	/*--------------------------------------------------------------*/
	private void fill() throws IOException {
		//-- Is there room in the queue?
		if(m_qlen + 1 >= m_q.length)
			throw new IllegalStateException("Lookahead bigger than queue size");
		int c;
		if(m_ateof)
			c = -1;
		else {
			c = m_r.read();
			if(c == -1)
				m_ateof = true;
		}

		if(m_qputp >= m_q.length)
			m_qputp = 0;
		m_q[m_qputp++] = c;
		m_qlen++;
	}

	/**
	 * Get nth character.
	 */
	private int LA(int offset) throws IOException {
		while(m_qlen <= offset)
			fill();
		offset = (m_qgetp + offset) % m_q.length;
		return m_q[offset];
	}

	/**
	 * Eat the current character and add to the token text buffer. This
	 * is the code incrementing positions and the like.
	 */
	private void _consume(boolean buffer) {
		if(m_qlen == 0)
			throw new IllegalStateException("No more data!");
		int c = m_q[m_qgetp];
		if(c == -1) // Do not consume eof
			return;
		m_qgetp++;
		if(m_qgetp >= m_q.length)
			m_qgetp = 0;
		m_qlen--;
		if(c == '\n') {
			m_lnr++;
			m_cnr = 1;
		} else
			m_cnr++;
		if(buffer)
			m_sb.append((char) c);
	}

	/**
	 * Eat the current character and add to the token text buffer. This
	 * is the code incrementing positions and the like.
	 */
	private void consume() {
		_consume(true);
	}

	private void consume(int nr) {
		while(nr-- > 0)
			consume();
	}

	private String getText() {
		return m_sb.toString();
	}

	private void error(boolean tokenpos, String msg) {
		StringBuilder sb = new StringBuilder();
		sb.append("QuickTemplate error ");
		sb.append(m_ident);
		sb.append("(");
		if(tokenpos) {
			sb.append(m_t_lnr);
			sb.append(':');
			sb.append(m_t_cnr);
		} else {
			sb.append(m_lnr);
			sb.append(':');
			sb.append(m_cnr);
		}
		sb.append(") ");
		sb.append(msg);

		throw new RuntimeException(sb.toString());
	}

	private void error(int lnr, int cnr, String msg) {
		StringBuilder sb = new StringBuilder();
		sb.append("QuickTemplate error ");
		sb.append(m_ident);
		sb.append("(");
		sb.append(lnr);
		sb.append(':');
		sb.append(cnr);
		sb.append(") ");
		sb.append(msg);

		throw new RuntimeException(sb.toString());
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Lexer												*/
	/*--------------------------------------------------------------*/
	//	/** Lexer mode: when T we're in an expression part (scanning for %>). */
	//	private boolean		m_inexpr;
	//
	/**
	 * Scans for data until a token start character is found (&lt;%).
	 * @return
	 */
	private TokenCode scanLiteral() throws Exception {
		for(;;) {
			int c = LA(0);
			if(c == -1)
				return TokenCode.LIT; // Eof-> return completed token
			if(c == '<' && LA(1) == '%') // Open token def?
				return TokenCode.LIT; // Yes-> return lit token.

			//-- Nothing special: consume.
			consume();
		}
	}

	private TokenCode scanExpr() throws Exception {
		//-- Find the terminating %> while taking care of quotes.
		int qc = 0;
		m_sb.setLength(0);
		for(;;) {
			int c = LA(0);
			if(c == -1)
				error(true, "Unexpected EOF in expression starting here.");
			if(qc == 0) {
				if(c == '%' && LA(1) == '>')
					break;
			} else {
				if(c == qc)
					qc = 0;
			}
			consume();
		}

		//-- The expression has been found.
		_consume(false);
		_consume(false);
		return TokenCode.EXPR;
	}

	/**
	 * Found a token start &lt;%. Determine what token it is.
	 * @return
	 */
	private TokenCode scanThingy() throws Exception {
		int c = LA(2);
		if(c == '=') {
			consume(3);
			return scanExpr();
		} else if(c == '%') // The sequence <%% is replaced with <%.
		{
			consume(3);
			m_sb.setLength(0);
			m_sb.append("<%");
			return scanLiteral();
		}

		// Must be an identifier - scan it
		consume(2); // Eat <%
		m_sb.setLength(0);
		for(;;) {
			c = LA(0);
			if(c == '%' && LA(1) == '>') // Found delimiter?
				break;
			else if(m_sb.length() > 20)
				error(false, "Unexpected/unknown token.");
			consume();
		}
		String name = m_sb.toString().trim();
		consume(2); // Eat %>
		if(name.equalsIgnoreCase("if"))
			return TokenCode.IF;
		else if(name.equalsIgnoreCase("else"))
			return TokenCode.ELSE;
		else if(name.equalsIgnoreCase("endif"))
			return TokenCode.ENDIF;
		error(false, "Unexpected/unknown token: '" + name + "'");
		return null; // NOTREACHED
	}

	private TokenCode nextTokenPrim() throws Exception {
		for(;;) {
			m_t_cnr = m_cnr;
			m_t_lnr = m_lnr;
			m_sb.setLength(0);
			int c = LA(0);
			switch(c){
				default:
					return scanLiteral();

				case '<':
					if(LA(1) == '%')
						return scanThingy();
					return scanLiteral();

				case -1:
					return TokenCode.EOF;
			}
		}
	}

	private TokenCode nextToken() throws Exception {
		TokenCode tc = nextTokenPrim();
		System.out.println("token: " + tc + ": " + getText());
		return tc;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Compiler part.										*/
	/*--------------------------------------------------------------*/
	static private class State {
		public int m_lnr, m_cnr;

		/** The statement list */
		public List<NdBase> m_list = new ArrayList<NdBase>();

		public TokenCode m_mode;

		public NdBase m_node;

		public State() {}
	}

	private Stack<State> m_stack = new Stack<State>();

	private State push(TokenCode tc) {
		State s = new State();
		s.m_mode = tc;
		s.m_lnr = m_t_lnr;
		s.m_cnr = m_t_cnr;
		m_stack.push(s);
		return s;
	}

	private State current() {
		if(m_stack.size() == 0)
			throw new IllegalStateException("Stack underflow.");
		return m_stack.peek();
	}

	private State pop() {
		if(m_stack.size() == 0)
			throw new IllegalStateException("Stack underflow.");
		return m_stack.pop();
	}

	private void append(NdBase b) {
		current().m_list.add(b);
	}

	private NdList makeList(State s) {
		return new NdList(s.m_list.toArray(new NdBase[s.m_list.size()]));
	}

	private void parseIf() throws Exception {
		//-- Parse the expression..
		m_sb.setLength(0);
		scanExpr();
		Expression x = SharedEvaluator.getInstance().parseExpression(getText(), Boolean.class, null, getText());
		State s = push(TokenCode.IF);
		s.m_node = new NdIf(x);

	}

	private void parseEndIf() throws Exception {
		State s = current();
		if(s.m_mode != TokenCode.IF && s.m_mode != TokenCode.ELSE)
			error(true, "endif without a matching if/else.");

		//-- Complete the statement
		NdIf nif = (NdIf) current().m_node;
		NdList l = makeList(current()); // Get the current list,
		if(current().m_mode == TokenCode.IF)
			nif.setIf(l);
		else
			nif.setElse(l);
		pop();
		append(nif);
	}

	private void parseElse() throws Exception {
		State s = current();
		if(s.m_mode != TokenCode.IF)
			error(true, "else without a matching if.");
		s.m_mode = TokenCode.ELSE;
		NdList l = makeList(current()); // Get the current list,
		current().m_list.clear();
		NdIf nif = (NdIf) current().m_node;
		nif.setIf(l);
	}

	private Template compile() throws Exception {
		push(TokenCode.EOF);
		bleh : for(;;) {
			TokenCode t = nextToken();
			switch(t){
				case EOF:
					break bleh;
				case LIT:
					append(new NdLit(getText()));
					break;

				case EXPR: {
					Expression x = SharedEvaluator.getInstance().parseExpression(getText(), String.class, null, getText());
					append(new NdExpr(x));
					break;
				}

				case IF:
					parseIf();
					break;
				case ENDIF:
					parseEndIf();
					break;
				case ELSE:
					parseElse();
					break;
			}
		}

		//-- Eof found. Must have eof list current
		while(m_stack.size() > 1) {
			State s = pop();
			error(s.m_lnr, s.m_cnr, "Missing terminator for construct " + s.m_mode + " started here");
		}
		return makeList(current());
	}

	static public void main(String[] args) {
		try {
			Reader r = new FileReader(new File("./in.txt"));
			compile(r, "test");

		} catch(Exception x) {
			x.printStackTrace();
		}
	}
}
