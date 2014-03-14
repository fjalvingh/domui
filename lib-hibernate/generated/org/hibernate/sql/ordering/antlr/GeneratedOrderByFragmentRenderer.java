// $ANTLR 2.7.6 (2005-12-22): "order-by-render.g" -> "GeneratedOrderByFragmentRenderer.java"$

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2008, Red Hat Middleware LLC or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Middleware LLC.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 *
 */
package org.hibernate.sql.ordering.antlr;

import antlr.TreeParser;
import antlr.Token;
import antlr.collections.AST;
import antlr.RecognitionException;
import antlr.ANTLRException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.collections.impl.BitSet;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;


/**
 * Antlr grammar for rendering <tt>ORDER_BY</tt> trees as described by the {@link OrderByFragmentParser}

 * @author Steve Ebersole
 */
public class GeneratedOrderByFragmentRenderer extends antlr.TreeParser       implements GeneratedOrderByFragmentRendererTokenTypes
 {

    // the buffer to which we write the resulting SQL.
	private StringBuffer buffer = new StringBuffer();

	protected void out(String text) {
	    buffer.append( text );
	}

	protected void out(AST ast) {
	    buffer.append( ast.getText() );
	}

    /*package*/ String getRenderedFragment() {
        return buffer.toString();
    }
public GeneratedOrderByFragmentRenderer() {
	tokenNames = _tokenNames;
}

	public final void orderByFragment(AST _t) throws RecognitionException {
		
		traceIn("orderByFragment",_t);
		try { // debugging
			AST orderByFragment_AST_in = (_t == ASTNULL) ? null : (AST)_t;
			
			try {      // for error handling
				AST __t2 = _t;
				AST tmp1_AST_in = (AST)_t;
				match(_t,ORDER_BY);
				_t = _t.getFirstChild();
				sortSpecification(_t);
				_t = _retTree;
				{
				_loop4:
				do {
					if (_t==null) _t=ASTNULL;
					if ((_t.getType()==SORT_SPEC)) {
						out(", ");
						sortSpecification(_t);
						_t = _retTree;
					}
					else {
						break _loop4;
					}
					
				} while (true);
				}
				_t = __t2;
				_t = _t.getNextSibling();
			}
			catch (RecognitionException ex) {
				reportError(ex);
				if (_t!=null) {_t = _t.getNextSibling();}
			}
			_retTree = _t;
		} finally { // debugging
			traceOut("orderByFragment",_t);
		}
	}
	
	public final void sortSpecification(AST _t) throws RecognitionException {
		
		traceIn("sortSpecification",_t);
		try { // debugging
			AST sortSpecification_AST_in = (_t == ASTNULL) ? null : (AST)_t;
			
			try {      // for error handling
				AST __t6 = _t;
				AST tmp2_AST_in = (AST)_t;
				match(_t,SORT_SPEC);
				_t = _t.getFirstChild();
				sortKeySpecification(_t);
				_t = _retTree;
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case COLLATE:
				{
					collationSpecification(_t);
					_t = _retTree;
					break;
				}
				case 3:
				case ORDER_SPEC:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case ORDER_SPEC:
				{
					orderingSpecification(_t);
					_t = _retTree;
					break;
				}
				case 3:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				_t = __t6;
				_t = _t.getNextSibling();
			}
			catch (RecognitionException ex) {
				reportError(ex);
				if (_t!=null) {_t = _t.getNextSibling();}
			}
			_retTree = _t;
		} finally { // debugging
			traceOut("sortSpecification",_t);
		}
	}
	
	public final void sortKeySpecification(AST _t) throws RecognitionException {
		
		traceIn("sortKeySpecification",_t);
		try { // debugging
			AST sortKeySpecification_AST_in = (_t == ASTNULL) ? null : (AST)_t;
			
			try {      // for error handling
				AST __t10 = _t;
				AST tmp3_AST_in = (AST)_t;
				match(_t,SORT_KEY);
				_t = _t.getFirstChild();
				sortKey(_t);
				_t = _retTree;
				_t = __t10;
				_t = _t.getNextSibling();
			}
			catch (RecognitionException ex) {
				reportError(ex);
				if (_t!=null) {_t = _t.getNextSibling();}
			}
			_retTree = _t;
		} finally { // debugging
			traceOut("sortKeySpecification",_t);
		}
	}
	
	public final void collationSpecification(AST _t) throws RecognitionException {
		
		traceIn("collationSpecification",_t);
		try { // debugging
			AST collationSpecification_AST_in = (_t == ASTNULL) ? null : (AST)_t;
			AST c = null;
			
			try {      // for error handling
				c = (AST)_t;
				match(_t,COLLATE);
				_t = _t.getNextSibling();
				
				out( " collate " );
				out( c );
				
			}
			catch (RecognitionException ex) {
				reportError(ex);
				if (_t!=null) {_t = _t.getNextSibling();}
			}
			_retTree = _t;
		} finally { // debugging
			traceOut("collationSpecification",_t);
		}
	}
	
	public final void orderingSpecification(AST _t) throws RecognitionException {
		
		traceIn("orderingSpecification",_t);
		try { // debugging
			AST orderingSpecification_AST_in = (_t == ASTNULL) ? null : (AST)_t;
			AST o = null;
			
			try {      // for error handling
				o = (AST)_t;
				match(_t,ORDER_SPEC);
				_t = _t.getNextSibling();
				
				out( " " );
				out( o );
				
			}
			catch (RecognitionException ex) {
				reportError(ex);
				if (_t!=null) {_t = _t.getNextSibling();}
			}
			_retTree = _t;
		} finally { // debugging
			traceOut("orderingSpecification",_t);
		}
	}
	
	public final void sortKey(AST _t) throws RecognitionException {
		
		traceIn("sortKey",_t);
		try { // debugging
			AST sortKey_AST_in = (_t == ASTNULL) ? null : (AST)_t;
			AST i = null;
			
			try {      // for error handling
				i = (AST)_t;
				match(_t,IDENT);
				_t = _t.getNextSibling();
				
				out( i );
				
			}
			catch (RecognitionException ex) {
				reportError(ex);
				if (_t!=null) {_t = _t.getNextSibling();}
			}
			_retTree = _t;
		} finally { // debugging
			traceOut("sortKey",_t);
		}
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"ORDER_BY",
		"SORT_SPEC",
		"ORDER_SPEC",
		"SORT_KEY",
		"EXPR_LIST",
		"DOT",
		"IDENT_LIST",
		"COLUMN_REF",
		"\"collate\"",
		"\"asc\"",
		"\"desc\"",
		"COMMA",
		"HARD_QUOTE",
		"IDENT",
		"OPEN_PAREN",
		"CLOSE_PAREN",
		"NUM_DOUBLE",
		"NUM_FLOAT",
		"NUM_INT",
		"NUM_LONG",
		"QUOTED_STRING",
		"\"ascending\"",
		"\"descending\"",
		"ID_START_LETTER",
		"ID_LETTER",
		"ESCqs",
		"HEX_DIGIT",
		"EXPONENT",
		"FLOAT_SUFFIX",
		"WS"
	};
	
	}
	
