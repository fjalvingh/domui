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

import java.lang.reflect.*;
import java.math.*;
import java.util.*;

import javax.servlet.jsp.el.*;

import to.etc.el.node.*;
import to.etc.util.*;

/**
 * <p>This is the actual EL evaluator code. This implements a fast stack-based
 * expression compiler which uses a hand-written lexer and a hand-written
 * grammar. This has the advantage of being fast, and since EL is not a complex
 * grammar it is easy to do.
 *
 * <p>The expression is a generic expression without ${ ... } or #{ .... } constructs
 * attached to it. The code will calculate a get for the value at minimum and can be
 * asked to provide a set for the value too.
 *
 * <p>This code tries to compile an expression into an object form where the
 * object form can be executed very quickly.
 *
 * Created on May 17, 2005
 * @author jal
 */
public class CoreElEvaluator {
	static private final int MAX_STACK = 6;

	/** A quicky lexer. */
	private ElLexer m_lexer = new ElLexer();

	/** The current token */
	private ElToken m_t;

	/** The topmost used stack position */
	private int m_tokstk_ix = -1;

	private int m_valstk_ix = -1;

	private ElToken[] m_tokstk = new ElToken[MAX_STACK];

	private NdBase[] m_valstk = new NdBase[MAX_STACK];

	private FunctionMapper m_mapper;

	/**
	 * The base compiler for compiling expressions.
	 * @param s
	 * @param mapper
	 * @return
	 * @throws ELException
	 */
	public NdBase evaluate(String s, FunctionMapper mapper) throws ELException {
		m_mapper = mapper;
		m_lexer.set(s); // Init the lexer for this go
		m_t = m_lexer.nextToken(); // Get the current token
		return eval();
	}

	public PropertyExpression evaluatePropertyExpression(String s, FunctionMapper mapper) throws ELException {
		m_mapper = mapper;
		m_lexer.set(s);
		m_t = m_lexer.nextToken(); // Start at the current token. This *must* be a name.

		//-- First part must be identifier or []
		NdLookup root = null;
		if(m_t == ElToken.BraceOpen) {
			//-- [ xxx ] expression
			nextToken(); // Past open [
			NdBase b = eval(); // Evaluate between []
			if(m_t != ElToken.BraceClose) // Must end in ]
				error("Missing ']' in lookup (map) property expression");
			nextToken(); // Past ]
			root = new NdLookup(NdPropertyBase.getInstance(), b);
		} else if(m_t == ElToken.Id) {
			root = new NdLookup(NdPropertyBase.getInstance(), new NdStringLit(m_lexer.getText()));
			nextToken();
		} else
			error("Expecting an identifier at the start of a property expression");

		for(;;) {
			switch(m_t){
				default:
					error("Unexpected token '" + m_t + "' in property expression");
					return null;

				case EOF:
					return new PropertyExpression(root);

				case Dot:
					//-- Dotted expression following: add a lookup spec
					nextToken();
					if(m_t != ElToken.Id)
						error("Expecting an identifier after the '.' operator (but got " + m_t + ")");

					//-- Create an Unmap op
					root = new NdLookup(root, new NdStringLit(m_lexer.getText()));
					nextToken(); // Past identifier
					break; // Continue loop

				case BraceOpen:
					//-- [ xxx ] expression
					nextToken(); // Past open [
					NdBase b = eval(); // Evaluate between []
					if(m_t != ElToken.BraceClose) // Must end in ]
						error("Missing ']' in lookup (map) subexpression");
					nextToken(); // Past ]

					//-- Create the lookup
					root = new NdLookup(root, b);
					break; // And loop
			}
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Small helpers.										*/
	/*--------------------------------------------------------------*/
	private void error(String s) throws ELException {
		throw new ELException(s + " [at position " + m_lexer.getTokenPos() + " of expression '" + m_lexer.getInput() + "']");
	}

	private void push(ElToken tok) {
		m_tokstk_ix++;
		if(m_tokstk_ix >= m_tokstk.length) {
			//-- Reallocate
			ElToken[] tar = new ElToken[m_tokstk_ix + 10];
			System.arraycopy(m_tokstk, 0, tar, 0, m_tokstk_ix);
			m_tokstk = tar;
		}
		m_tokstk[m_tokstk_ix] = tok;
	}

	private void push(NdBase n) {
		m_valstk_ix++;
		if(m_valstk_ix >= m_valstk.length) {
			//-- Reallocate
			NdBase[] bar = new NdBase[m_valstk_ix + 10];
			System.arraycopy(m_valstk, 0, bar, 0, m_valstk_ix);
			m_valstk = bar;
		}
		m_valstk[m_valstk_ix] = n;
	}

	private ElToken peekTopToken() {
		if(m_tokstk_ix < 0)
			throw new IllegalStateException("!? Operator stack underflow");
		return m_tokstk[m_tokstk_ix];
	}

	private ElToken popToken() {
		if(m_tokstk_ix < 0)
			throw new IllegalStateException("!? Operator stack underflow");
		return m_tokstk[m_tokstk_ix--];
	}

	private NdBase popValue() {
		if(m_valstk_ix < 0)
			throw new IllegalStateException("!? Value stack underflow");
		return m_valstk[m_valstk_ix--];
	}

	private void nextToken() throws ELException {
		m_t = m_lexer.nextToken();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Evaluator and parser.								*/
	/*--------------------------------------------------------------*/
	/**
	 * Evaluates a complete expression. Returns as soon as the current
	 * token cannot be used. This mainly handles the syntax term operator term
	 * and uses a precedence stack to handle operator precedence.
	 */
	private NdBase eval() throws ELException {
		//-- First get a term,

		push(ElToken.EOF); // End of stack marker
		for(;;) {
			NdBase t = term(); // Start with a term

			//-- The current token must be an operator or we're done,
			ElToken currop = m_t; // Get the current operation

			/*
			 * Now we handle precedence: while the "current" token's precedence is
			 * below the stacked precedence create the stacked operation; repeat till
			 * the stacked op has a lower prec than the current.
			 */
			for(;;) {
				ElToken stkop = peekTopToken(); // Get the top stacked token
				if(currop.getPrec() > stkop.getPrec()) {
					/*
					 * The current op has higher precedence than the stacked
					 * one. We need to stack this and continue.
					 */
					push(currop);
					push(t); // Save the current term
					nextToken(); // Skip token
					break;
				} else {
					/*
					 * The current op has lower or equal precedence than the stacked one. Create
					 * the operation of current with stacked. Except when the stacked op is eof
					 * meaning that the expression stack is empty.
					 */
					if(stkop == ElToken.EOF) // Met the marker?
					{
						popToken(); // Remove from stack
						return t; // Return constructed expr
					}
					//-- We need to construct the stacked expr
					NdBase sex = popValue(); // Pop stacked value
					popToken(); // Pop stacked token
					t = NdBinary.create(stkop, sex, t);
				}
			}
		}
	}

	/**
	 * Evaluate a term and stack it. The token to use is current.
	 *
	 * @throws ELException
	 */
	private NdBase term() throws ELException {
		for(;;) {
			NdBase tn = null;
			switch(m_t){
				default:
					error("Expected a term but got a " + m_t);
					return null; // NOTREACHED.

				case EOF:
					error("Expecting another term");
					return null;

				case Id:
					return termID();

				case StringLiteral:
					tn = new NdStringLit(m_lexer.getText());
					nextToken();
					return tn;

				case IntLiteral:
					tn = new NdIntLit(m_lexer.getInt());
					nextToken();
					return tn;

				case FloatLiteral:
					tn = new NdFloatLit(m_lexer.getDouble());
					nextToken();
					return tn;

				case True:
					nextToken();
					return NdBoolLit.getInstance(true);

				case False:
					nextToken();
					return NdBoolLit.getInstance(false);

				case Null:
					nextToken();
					return NdNull.getInstance();

					//-- Unary functions
				case Plus:
					nextToken();
					break; // Loop, ignore unary plus

				case Minus:
					nextToken(); // Past minus
					NdBase t = term(); // Get the next term
					return new NdUnaryMinus(t);

				case LogicalNot:
					nextToken(); // Past not
					NdBase t1 = term();
					return new NdUnaryNot(t1);

				case Empty:
					nextToken(); // Past empty
					NdBase t2 = term();
					return new NdEmpty(t2);

				case ParentheseOpen: {
					//-- ( expr )
					nextToken(); // To first token of expr
					tn = eval(); // Eval that,
					if(m_t != ElToken.ParentheseClose) // Must end in closing )
						error("Missing ')' in parenthesed subexpression");
					nextToken(); // To 1st token after )
					return tryExtension(tn);
				}
			}
		}
	}

	/**
	 * Scans something starting with 'identifier'. The only constructs recognised are
	 * id.id.id and ident ':' ident.
	 * @return
	 * @throws ELException
	 */
	private NdBase termID() throws ELException {
		//-- Start with a variable lookup,
		String id1 = m_lexer.getText(); // Get identifier name
		nextToken(); // Move to next token
		NdBase root = null;
		if(m_t == ElToken.Colon) // Is possible stupid function name?
		{
			//-- We must lookahead to see if the next token sequence is ident '('
			ElLexer oldlexer = m_lexer.dup(); // Dup the current lexer state

			try {
				nextToken();
				if(m_t == ElToken.Id) // : ident
				{
					String id2 = m_lexer.getText();
					nextToken();
					if(m_t == ElToken.ParentheseOpen) // : ident (
					{
						//-- This IS a qualified function call!!
						root = new NdQualifiedName(id1, id2);
					}
				}
			} catch(Exception x) {}
			if(root == null) {
				m_lexer = oldlexer; // Restore lexer state
				m_t = m_lexer.getLastToken(); // Restore last token
			}
		}

		//-- Not a qualified function name. Must be a variable lookup or an unqualified function.
		if(root == null)
			root = new NdVarLookup(id1); // Get the name
		NdBase ret = root;
		while(m_t == ElToken.Dot) {
			//-- Must be followed by Identifier
			nextToken();
			if(m_t != ElToken.Id)
				error("Expecting an identifier after the '.' operator (but got " + m_t + ")");

			//-- Create an Unmap op
			ret = new NdLookup(ret, new NdStringLit(m_lexer.getText()));
			nextToken(); // Past identifier
		}
		return tryExtension(ret);
	}

	/**
	 * Checks for high-precedence occurences of [], '.' and fcall()
	 * @param root
	 * @return
	 * @throws ELException
	 */
	private NdBase tryExtension(NdBase root) throws ELException {
		for(;;) {
			switch(m_t){
				default:
					return root;

				case Dot:
					//-- Dotted expression following: add a lookup spec
					nextToken();
					if(m_t != ElToken.Id)
						error("Expecting an identifier after the '.' operator (but got " + m_t + ")");

					//-- Create an Unmap op
					root = new NdLookup(root, new NdStringLit(m_lexer.getText()));
					nextToken(); // Past identifier
					break; // Continue loop

				case BraceOpen:
					//-- [ xxx ] expression
					nextToken(); // Past open [
					NdBase b = eval(); // Evaluate between []
					if(m_t != ElToken.BraceClose) // Must end in ]
						error("Missing ']' in lookup (map) subexpression");
					nextToken(); // Past ]

					//-- Create the lookup
					root = new NdLookup(root, b);
					break; // And loop

				case ParentheseOpen: // Functor expression
					//-- Functor
					root = tryFunctor(root);
					break;
			}
		}
	}

	/**
	 * Tries if the next part can be functor ( param-list )
	 * @param root
	 * @return
	 */
	private NdBase tryFunctor(NdBase root) throws ELException {
		if(m_t != ElToken.ParentheseOpen) // If not ( then no functor here
			return root;
		List<NdBase> al = new ArrayList<NdBase>(); // Actual-parameter-list
		nextToken();
		if(m_t == ElToken.ParentheseClose) // Only ()?
			nextToken(); // Parameterless function call
		else {
			for(;;) {
				NdBase act = eval();
				al.add(act);

				//-- If the next thing is ',' we continue
				if(m_t == ElToken.ParentheseClose) {
					nextToken(); // Past close parentheses
					break;
				} else if(m_t == ElToken.Comma) {
					nextToken(); // Comma: expecting another parameter
				} else
					error("Expecting either a ',' or a ')' in function-call actual argument list");
			}
		}

		//-- We must have a Qualified name or a VariableLookup
		NdQualifiedName qn;
		if(root instanceof NdQualifiedName)
			qn = (NdQualifiedName) root;
		else if(root instanceof NdVarLookup) {
			//-- Must be converted into a qualified name
			qn = new NdQualifiedName(null, ((NdVarLookup) root).getName());
		} else
			throw new ELException("The expression '" + root.getExpression() + "' cannot be \"called\" as a function");

		if(m_mapper != null) {
			Method m = m_mapper.resolveFunction(qn.getNS(), qn.getName());
			if(m != null) {
				//-- The method provided must have the same #of parameters as passed
				int clen = m.getParameterTypes().length;
				if(clen != al.size()) // Different parameter count?
					throw new ELException("The EL function '" + qn.getExpression() + "' (implemented by the method " + m.toGenericString() + ") needs " + clen + " parameters, but you called it with "
						+ al.size() + " parameters.");

				//-- Create the function-call expression
				return new NdFCall(qn, m, al);
			}
		}
		throw new ELException("Undefined function '" + qn.getExpression() + "'");
	}

	static public void main(String[] args) {
		try {
			NdBase b;
			Object res;
			CoreElEvaluator e = new CoreElEvaluator();
			//			b	= e.evaluate("a + b * c * (d + 12)", null);
			//			b.dump();

			FunctionMapperImpl fmi = new FunctionMapperImpl();
			fmi.add("test", CalculationUtil.class);

			VariableResolver vr = new VariableResolver() {
				public Object resolveVariable(String n) throws ELException {
					if("a".equals(n))
						return new Integer(123);
					if("b".equals(n))
						return new Long(7);
					if("c".equals(n))
						return new BigInteger("631");
					if("d".equals(n))
						return new Byte((byte) 7);
					return null;
				}
			};
			//			res	= b.evaluate(vr);
			//			System.out.println("Result is "+res);

			b = e.evaluate("false", fmi);
			res = b.evaluate(vr);
			System.out.println("Result is " + res);


			b = e.evaluate("1 > 2 ? 100 : test:age(test:date('1964/10/20'))", fmi);
			res = b.evaluate(vr);
			System.out.println("Result is " + res);
		} catch(Exception x) {
			x.printStackTrace();
		}
	}
}
