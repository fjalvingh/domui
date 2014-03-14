// $ANTLR 2.7.6 (2005-12-22): "order-by.g" -> "GeneratedOrderByFragmentParser.java"$

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

import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;
import antlr.collections.AST;
import java.util.Hashtable;
import antlr.ASTFactory;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;

/**
 * Antlr grammar for dealing with <tt>order-by</tt> mapping fragments.

 * @author Steve Ebersole
 */
public class GeneratedOrderByFragmentParser extends antlr.LLkParser       implements OrderByTemplateTokenTypes
 {

    /**
     * Method for logging execution trace information.
     *
     * @param msg The trace message.
     */
    protected void trace(String msg) {
        System.out.println( msg );
    }

    /**
     * Extract a node's text.
     *
     * @param ast The node
     *
     * @return The text.
     */
    protected final String extractText(AST ast) {
        // for some reason, within AST creation blocks "[]" I am somtimes unable to refer to the AST.getText() method
        // using #var (the #var is not interpreted as the rule's output AST).
        return ast.getText();
    }

    /**
     * Process the given node as a quote identifier.  These need to be quoted in the dialect-specific way.
     *
     * @param ident The quoted-identifier node.
     *
     * @return The processed node.
     *
     * @see org.hibernate.dialect.Dialect#quote
     */
    protected AST quotedIdentifier(AST ident) {
        return ident;
    }

    /**
     * Process the given node as a quote string.
     *
     * @param ident The quoted string.  This is used from within function param recognition, and represents a
     * SQL-quoted string.
     *
     * @return The processed node.
     */
    protected AST quotedString(AST ident) {
        return ident;
    }

    /**
     * A check to see if the text of the given node represents a known function name.
     *
     * @param ast The node whose text we want to check.
     *
     * @return True if the node's text is a known function name, false otherwise.
     *
     * @see org.hibernate.dialect.function.SQLFunctionRegistry
     */
    protected boolean isFunctionName(AST ast) {
        return false;
    }

    /**
     * Process the given node as a function.
     *
     * @param The node representing the function invocation (including parameters as subtree components).
     *
     * @return The processed node.
     */
    protected AST resolveFunction(AST ast) {
        return ast;
    }

    /**
     * Process the given node as an IDENT.  May represent either a column reference or a property reference.
     *
     * @param ident The node whose text represents either a column or property reference.
     *
     * @return The processed node.
     */
    protected AST resolveIdent(AST ident) {
        return ident;
    }

    /**
     * Allow post processing of each <tt>sort specification</tt>
     *
     * @param The grammar-built sort specification subtree.
     *
     * @return The processed sort specification subtree.
     */
    protected AST postProcessSortSpecification(AST sortSpec) {
        return sortSpec;
    }


protected GeneratedOrderByFragmentParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public GeneratedOrderByFragmentParser(TokenBuffer tokenBuf) {
  this(tokenBuf,3);
}

protected GeneratedOrderByFragmentParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public GeneratedOrderByFragmentParser(TokenStream lexer) {
  this(lexer,3);
}

public GeneratedOrderByFragmentParser(ParserSharedInputState state) {
  super(state,3);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

/**
 * Main recognition rule for this grammar
 */
	public final void orderByFragment() throws RecognitionException, TokenStreamException {
		
		traceIn("orderByFragment");
		try { // debugging
			returnAST = null;
			ASTPair currentAST = new ASTPair();
			AST orderByFragment_AST = null;
			trace("orderByFragment");
			
			try {      // for error handling
				sortSpecification();
				astFactory.addASTChild(currentAST, returnAST);
				{
				_loop3:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						sortSpecification();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop3;
					}
					
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					orderByFragment_AST = (AST)currentAST.root;
					
					orderByFragment_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(ORDER_BY,"order-by")).add(orderByFragment_AST));
					
					currentAST.root = orderByFragment_AST;
					currentAST.child = orderByFragment_AST!=null &&orderByFragment_AST.getFirstChild()!=null ?
						orderByFragment_AST.getFirstChild() : orderByFragment_AST;
					currentAST.advanceChildToEnd();
				}
				orderByFragment_AST = (AST)currentAST.root;
			}
			catch (RecognitionException ex) {
				if (inputState.guessing==0) {
					reportError(ex);
					recover(ex,_tokenSet_0);
				} else {
				  throw ex;
				}
			}
			returnAST = orderByFragment_AST;
		} finally { // debugging
			traceOut("orderByFragment");
		}
	}
	
/**
 * Reconition rule for what ANSI SQL terms the <tt>sort specification</tt>, which is essentially each thing upon which
 * the results should be sorted.
 */
	public final void sortSpecification() throws RecognitionException, TokenStreamException {
		
		traceIn("sortSpecification");
		try { // debugging
			returnAST = null;
			ASTPair currentAST = new ASTPair();
			AST sortSpecification_AST = null;
			trace("sortSpecification");
			
			try {      // for error handling
				sortKey();
				astFactory.addASTChild(currentAST, returnAST);
				{
				switch ( LA(1)) {
				case COLLATE:
				{
					collationSpecification();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case EOF:
				case ASCENDING:
				case DESCENDING:
				case COMMA:
				case LITERAL_ascending:
				case LITERAL_descending:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				{
				switch ( LA(1)) {
				case ASCENDING:
				case DESCENDING:
				case LITERAL_ascending:
				case LITERAL_descending:
				{
					orderingSpecification();
					astFactory.addASTChild(currentAST, returnAST);
					break;
				}
				case EOF:
				case COMMA:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				if ( inputState.guessing==0 ) {
					sortSpecification_AST = (AST)currentAST.root;
					
					sortSpecification_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(SORT_SPEC,"{sort specification}")).add(sortSpecification_AST));
					sortSpecification_AST = postProcessSortSpecification( sortSpecification_AST );
					
					currentAST.root = sortSpecification_AST;
					currentAST.child = sortSpecification_AST!=null &&sortSpecification_AST.getFirstChild()!=null ?
						sortSpecification_AST.getFirstChild() : sortSpecification_AST;
					currentAST.advanceChildToEnd();
				}
				sortSpecification_AST = (AST)currentAST.root;
			}
			catch (RecognitionException ex) {
				if (inputState.guessing==0) {
					reportError(ex);
					recover(ex,_tokenSet_1);
				} else {
				  throw ex;
				}
			}
			returnAST = sortSpecification_AST;
		} finally { // debugging
			traceOut("sortSpecification");
		}
	}
	
/**
 * Reconition rule for what ANSI SQL terms the <tt>sort key</tt> which is the expression (column, function, etc) upon
 * which to base the sorting.
 */
	public final void sortKey() throws RecognitionException, TokenStreamException {
		
		traceIn("sortKey");
		try { // debugging
			returnAST = null;
			ASTPair currentAST = new ASTPair();
			AST sortKey_AST = null;
			AST e_AST = null;
			trace("sortKey");
			
			try {      // for error handling
				expression();
				e_AST = (AST)returnAST;
				if ( inputState.guessing==0 ) {
					sortKey_AST = (AST)currentAST.root;
					
					sortKey_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(SORT_KEY,"sort key")).add(e_AST));
					
					currentAST.root = sortKey_AST;
					currentAST.child = sortKey_AST!=null &&sortKey_AST.getFirstChild()!=null ?
						sortKey_AST.getFirstChild() : sortKey_AST;
					currentAST.advanceChildToEnd();
				}
			}
			catch (RecognitionException ex) {
				if (inputState.guessing==0) {
					reportError(ex);
					recover(ex,_tokenSet_2);
				} else {
				  throw ex;
				}
			}
			returnAST = sortKey_AST;
		} finally { // debugging
			traceOut("sortKey");
		}
	}
	
/**
 * Reconition rule for what ANSI SQL terms the <tt>collation specification</tt> used to allow specifying that sorting for
 * the given {@link #sortSpecification} be treated within a specific character-set.
 */
	public final void collationSpecification() throws RecognitionException, TokenStreamException {
		
		traceIn("collationSpecification");
		try { // debugging
			returnAST = null;
			ASTPair currentAST = new ASTPair();
			AST collationSpecification_AST = null;
			Token  c = null;
			AST c_AST = null;
			AST cn_AST = null;
			trace("collationSpecification");
			
			try {      // for error handling
				c = LT(1);
				c_AST = astFactory.create(c);
				match(COLLATE);
				collationName();
				cn_AST = (AST)returnAST;
				if ( inputState.guessing==0 ) {
					collationSpecification_AST = (AST)currentAST.root;
					
					collationSpecification_AST = (AST)astFactory.make( (new ASTArray(1)).add(astFactory.create(COLLATE,extractText(cn_AST))));
					
					currentAST.root = collationSpecification_AST;
					currentAST.child = collationSpecification_AST!=null &&collationSpecification_AST.getFirstChild()!=null ?
						collationSpecification_AST.getFirstChild() : collationSpecification_AST;
					currentAST.advanceChildToEnd();
				}
			}
			catch (RecognitionException ex) {
				if (inputState.guessing==0) {
					reportError(ex);
					recover(ex,_tokenSet_3);
				} else {
				  throw ex;
				}
			}
			returnAST = collationSpecification_AST;
		} finally { // debugging
			traceOut("collationSpecification");
		}
	}
	
/**
 * Reconition rule for what ANSI SQL terms the <tt>ordering specification</tt>; <tt>ASCENDING</tt> or
 * <tt>DESCENDING</tt>.
 */
	public final void orderingSpecification() throws RecognitionException, TokenStreamException {
		
		traceIn("orderingSpecification");
		try { // debugging
			returnAST = null;
			ASTPair currentAST = new ASTPair();
			AST orderingSpecification_AST = null;
			trace("orderingSpecification");
			
			try {      // for error handling
				switch ( LA(1)) {
				case ASCENDING:
				case LITERAL_ascending:
				{
					{
					switch ( LA(1)) {
					case ASCENDING:
					{
						match(ASCENDING);
						break;
					}
					case LITERAL_ascending:
					{
						match(LITERAL_ascending);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					if ( inputState.guessing==0 ) {
						orderingSpecification_AST = (AST)currentAST.root;
						
						orderingSpecification_AST = (AST)astFactory.make( (new ASTArray(1)).add(astFactory.create(ORDER_SPEC,"asc")));
						
						currentAST.root = orderingSpecification_AST;
						currentAST.child = orderingSpecification_AST!=null &&orderingSpecification_AST.getFirstChild()!=null ?
							orderingSpecification_AST.getFirstChild() : orderingSpecification_AST;
						currentAST.advanceChildToEnd();
					}
					break;
				}
				case DESCENDING:
				case LITERAL_descending:
				{
					{
					switch ( LA(1)) {
					case DESCENDING:
					{
						match(DESCENDING);
						break;
					}
					case LITERAL_descending:
					{
						match(LITERAL_descending);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					if ( inputState.guessing==0 ) {
						orderingSpecification_AST = (AST)currentAST.root;
						
						orderingSpecification_AST = (AST)astFactory.make( (new ASTArray(1)).add(astFactory.create(ORDER_SPEC,"desc")));
						
						currentAST.root = orderingSpecification_AST;
						currentAST.child = orderingSpecification_AST!=null &&orderingSpecification_AST.getFirstChild()!=null ?
							orderingSpecification_AST.getFirstChild() : orderingSpecification_AST;
						currentAST.advanceChildToEnd();
					}
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
			}
			catch (RecognitionException ex) {
				if (inputState.guessing==0) {
					reportError(ex);
					recover(ex,_tokenSet_1);
				} else {
				  throw ex;
				}
			}
			returnAST = orderingSpecification_AST;
		} finally { // debugging
			traceOut("orderingSpecification");
		}
	}
	
/**
 * Reconition rule what this grammar recognizes as valid <tt>sort key</tt>.
 */
	public final void expression() throws RecognitionException, TokenStreamException {
		
		traceIn("expression");
		try { // debugging
			returnAST = null;
			ASTPair currentAST = new ASTPair();
			AST expression_AST = null;
			Token  qi = null;
			AST qi_AST = null;
			AST f_AST = null;
			AST p_AST = null;
			Token  i = null;
			AST i_AST = null;
			trace("expression");
			
			try {      // for error handling
				if ((LA(1)==HARD_QUOTE)) {
					AST tmp6_AST = null;
					tmp6_AST = astFactory.create(LT(1));
					match(HARD_QUOTE);
					qi = LT(1);
					qi_AST = astFactory.create(qi);
					match(IDENT);
					AST tmp7_AST = null;
					tmp7_AST = astFactory.create(LT(1));
					match(HARD_QUOTE);
					if ( inputState.guessing==0 ) {
						expression_AST = (AST)currentAST.root;
						
						expression_AST = quotedIdentifier( qi_AST );
						
						currentAST.root = expression_AST;
						currentAST.child = expression_AST!=null &&expression_AST.getFirstChild()!=null ?
							expression_AST.getFirstChild() : expression_AST;
						currentAST.advanceChildToEnd();
					}
				}
				else {
					boolean synPredMatched12 = false;
					if (((LA(1)==IDENT) && (LA(2)==DOT||LA(2)==OPEN_PAREN) && (_tokenSet_4.member(LA(3))))) {
						int _m12 = mark();
						synPredMatched12 = true;
						inputState.guessing++;
						try {
							{
							match(IDENT);
							{
							_loop11:
							do {
								if ((LA(1)==DOT)) {
									match(DOT);
									match(IDENT);
								}
								else {
									break _loop11;
								}
								
							} while (true);
							}
							match(OPEN_PAREN);
							}
						}
						catch (RecognitionException pe) {
							synPredMatched12 = false;
						}
						rewind(_m12);
inputState.guessing--;
					}
					if ( synPredMatched12 ) {
						functionCall();
						f_AST = (AST)returnAST;
						if ( inputState.guessing==0 ) {
							expression_AST = (AST)currentAST.root;
							
							expression_AST = f_AST;
							
							currentAST.root = expression_AST;
							currentAST.child = expression_AST!=null &&expression_AST.getFirstChild()!=null ?
								expression_AST.getFirstChild() : expression_AST;
							currentAST.advanceChildToEnd();
						}
					}
					else if ((LA(1)==IDENT) && (LA(2)==DOT) && (LA(3)==IDENT)) {
						simplePropertyPath();
						p_AST = (AST)returnAST;
						if ( inputState.guessing==0 ) {
							expression_AST = (AST)currentAST.root;
							
							expression_AST = resolveIdent( p_AST );
							
							currentAST.root = expression_AST;
							currentAST.child = expression_AST!=null &&expression_AST.getFirstChild()!=null ?
								expression_AST.getFirstChild() : expression_AST;
							currentAST.advanceChildToEnd();
						}
					}
					else if ((LA(1)==IDENT) && (_tokenSet_5.member(LA(2)))) {
						i = LT(1);
						i_AST = astFactory.create(i);
						match(IDENT);
						if ( inputState.guessing==0 ) {
							expression_AST = (AST)currentAST.root;
							
							if ( isFunctionName( i_AST ) ) {
							expression_AST = resolveFunction( i_AST );
							}
							else {
							expression_AST = resolveIdent( i_AST );
							}
							
							currentAST.root = expression_AST;
							currentAST.child = expression_AST!=null &&expression_AST.getFirstChild()!=null ?
								expression_AST.getFirstChild() : expression_AST;
							currentAST.advanceChildToEnd();
						}
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
				}
				catch (RecognitionException ex) {
					if (inputState.guessing==0) {
						reportError(ex);
						recover(ex,_tokenSet_5);
					} else {
					  throw ex;
					}
				}
				returnAST = expression_AST;
			} finally { // debugging
				traceOut("expression");
			}
		}
		
/**
 * Recognition rule for a function call
 */
	public final void functionCall() throws RecognitionException, TokenStreamException {
		
		traceIn("functionCall");
		try { // debugging
			returnAST = null;
			ASTPair currentAST = new ASTPair();
			AST functionCall_AST = null;
			AST fn_AST = null;
			AST pl_AST = null;
			trace("functionCall");
			
			try {      // for error handling
				functionName();
				fn_AST = (AST)returnAST;
				AST tmp8_AST = null;
				tmp8_AST = astFactory.create(LT(1));
				match(OPEN_PAREN);
				functionParameterList();
				pl_AST = (AST)returnAST;
				AST tmp9_AST = null;
				tmp9_AST = astFactory.create(LT(1));
				match(CLOSE_PAREN);
				if ( inputState.guessing==0 ) {
					functionCall_AST = (AST)currentAST.root;
					
					functionCall_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(IDENT,extractText(fn_AST))).add(pl_AST));
					functionCall_AST = resolveFunction( functionCall_AST );
					
					currentAST.root = functionCall_AST;
					currentAST.child = functionCall_AST!=null &&functionCall_AST.getFirstChild()!=null ?
						functionCall_AST.getFirstChild() : functionCall_AST;
					currentAST.advanceChildToEnd();
				}
			}
			catch (RecognitionException ex) {
				if (inputState.guessing==0) {
					reportError(ex);
					recover(ex,_tokenSet_5);
				} else {
				  throw ex;
				}
			}
			returnAST = functionCall_AST;
		} finally { // debugging
			traceOut("functionCall");
		}
	}
	
/**
 * A simple-property-path is an IDENT followed by one or more (DOT IDENT) sequences
 */
	public final void simplePropertyPath() throws RecognitionException, TokenStreamException {
		
		traceIn("simplePropertyPath");
		try { // debugging
			returnAST = null;
			ASTPair currentAST = new ASTPair();
			AST simplePropertyPath_AST = null;
			Token  i = null;
			AST i_AST = null;
			Token  i2 = null;
			AST i2_AST = null;
			
			trace("simplePropertyPath");
			StringBuffer buffer = new StringBuffer();
			
			
			try {      // for error handling
				i = LT(1);
				i_AST = astFactory.create(i);
				astFactory.addASTChild(currentAST, i_AST);
				match(IDENT);
				if ( inputState.guessing==0 ) {
					buffer.append( i.getText() );
				}
				{
				int _cnt31=0;
				_loop31:
				do {
					if ((LA(1)==DOT)) {
						AST tmp10_AST = null;
						tmp10_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp10_AST);
						match(DOT);
						i2 = LT(1);
						i2_AST = astFactory.create(i2);
						astFactory.addASTChild(currentAST, i2_AST);
						match(IDENT);
						if ( inputState.guessing==0 ) {
							buffer.append( '.').append( i2.getText() );
						}
					}
					else {
						if ( _cnt31>=1 ) { break _loop31; } else {throw new NoViableAltException(LT(1), getFilename());}
					}
					
					_cnt31++;
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					simplePropertyPath_AST = (AST)currentAST.root;
					
					simplePropertyPath_AST = (AST)astFactory.make( (new ASTArray(1)).add(astFactory.create(IDENT,buffer.toString())));
					
					currentAST.root = simplePropertyPath_AST;
					currentAST.child = simplePropertyPath_AST!=null &&simplePropertyPath_AST.getFirstChild()!=null ?
						simplePropertyPath_AST.getFirstChild() : simplePropertyPath_AST;
					currentAST.advanceChildToEnd();
				}
				simplePropertyPath_AST = (AST)currentAST.root;
			}
			catch (RecognitionException ex) {
				if (inputState.guessing==0) {
					reportError(ex);
					recover(ex,_tokenSet_5);
				} else {
				  throw ex;
				}
			}
			returnAST = simplePropertyPath_AST;
		} finally { // debugging
			traceOut("simplePropertyPath");
		}
	}
	
/**
 * Intended for use as a syntactic predicate to determine whether an IDENT represents a known SQL function name.
 */
	public final void functionCallCheck() throws RecognitionException, TokenStreamException {
		
		traceIn("functionCallCheck");
		try { // debugging
			returnAST = null;
			ASTPair currentAST = new ASTPair();
			AST functionCallCheck_AST = null;
			trace("functionCallCheck");
			
			try {      // for error handling
				AST tmp11_AST = null;
				tmp11_AST = astFactory.create(LT(1));
				match(IDENT);
				{
				_loop15:
				do {
					if ((LA(1)==DOT)) {
						AST tmp12_AST = null;
						tmp12_AST = astFactory.create(LT(1));
						match(DOT);
						AST tmp13_AST = null;
						tmp13_AST = astFactory.create(LT(1));
						match(IDENT);
					}
					else {
						break _loop15;
					}
					
				} while (true);
				}
				AST tmp14_AST = null;
				tmp14_AST = astFactory.create(LT(1));
				match(OPEN_PAREN);
				if (!( true ))
				  throw new SemanticException(" true ");
			}
			catch (RecognitionException ex) {
				if (inputState.guessing==0) {
					reportError(ex);
					recover(ex,_tokenSet_0);
				} else {
				  throw ex;
				}
			}
			returnAST = functionCallCheck_AST;
		} finally { // debugging
			traceOut("functionCallCheck");
		}
	}
	
/**
 * A function-name is an IDENT followed by zero or more (DOT IDENT) sequences
 */
	public final void functionName() throws RecognitionException, TokenStreamException {
		
		traceIn("functionName");
		try { // debugging
			returnAST = null;
			ASTPair currentAST = new ASTPair();
			AST functionName_AST = null;
			Token  i = null;
			AST i_AST = null;
			Token  i2 = null;
			AST i2_AST = null;
			
			trace("functionName");
			StringBuffer buffer = new StringBuffer();
			
			
			try {      // for error handling
				i = LT(1);
				i_AST = astFactory.create(i);
				astFactory.addASTChild(currentAST, i_AST);
				match(IDENT);
				if ( inputState.guessing==0 ) {
					buffer.append( i.getText() );
				}
				{
				_loop19:
				do {
					if ((LA(1)==DOT)) {
						AST tmp15_AST = null;
						tmp15_AST = astFactory.create(LT(1));
						astFactory.addASTChild(currentAST, tmp15_AST);
						match(DOT);
						i2 = LT(1);
						i2_AST = astFactory.create(i2);
						astFactory.addASTChild(currentAST, i2_AST);
						match(IDENT);
						if ( inputState.guessing==0 ) {
							buffer.append( '.').append( i2.getText() );
						}
					}
					else {
						break _loop19;
					}
					
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					functionName_AST = (AST)currentAST.root;
					
					functionName_AST = (AST)astFactory.make( (new ASTArray(1)).add(astFactory.create(IDENT,buffer.toString())));
					
					currentAST.root = functionName_AST;
					currentAST.child = functionName_AST!=null &&functionName_AST.getFirstChild()!=null ?
						functionName_AST.getFirstChild() : functionName_AST;
					currentAST.advanceChildToEnd();
				}
				functionName_AST = (AST)currentAST.root;
			}
			catch (RecognitionException ex) {
				if (inputState.guessing==0) {
					reportError(ex);
					recover(ex,_tokenSet_6);
				} else {
				  throw ex;
				}
			}
			returnAST = functionName_AST;
		} finally { // debugging
			traceOut("functionName");
		}
	}
	
/**
 * Recognition rule used to "wrap" all function parameters into an EXPR_LIST node
 */
	public final void functionParameterList() throws RecognitionException, TokenStreamException {
		
		traceIn("functionParameterList");
		try { // debugging
			returnAST = null;
			ASTPair currentAST = new ASTPair();
			AST functionParameterList_AST = null;
			trace("functionParameterList");
			
			try {      // for error handling
				functionParameter();
				astFactory.addASTChild(currentAST, returnAST);
				{
				_loop22:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						functionParameter();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop22;
					}
					
				} while (true);
				}
				if ( inputState.guessing==0 ) {
					functionParameterList_AST = (AST)currentAST.root;
					
					functionParameterList_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(EXPR_LIST,"{param list}")).add(functionParameterList_AST));
					
					currentAST.root = functionParameterList_AST;
					currentAST.child = functionParameterList_AST!=null &&functionParameterList_AST.getFirstChild()!=null ?
						functionParameterList_AST.getFirstChild() : functionParameterList_AST;
					currentAST.advanceChildToEnd();
				}
				functionParameterList_AST = (AST)currentAST.root;
			}
			catch (RecognitionException ex) {
				if (inputState.guessing==0) {
					reportError(ex);
					recover(ex,_tokenSet_7);
				} else {
				  throw ex;
				}
			}
			returnAST = functionParameterList_AST;
		} finally { // debugging
			traceOut("functionParameterList");
		}
	}
	
/**
 * Recognized function parameters.
 */
	public final void functionParameter() throws RecognitionException, TokenStreamException {
		
		traceIn("functionParameter");
		try { // debugging
			returnAST = null;
			ASTPair currentAST = new ASTPair();
			AST functionParameter_AST = null;
			trace("functionParameter");
			
			try {      // for error handling
				switch ( LA(1)) {
				case HARD_QUOTE:
				case IDENT:
				{
					expression();
					astFactory.addASTChild(currentAST, returnAST);
					functionParameter_AST = (AST)currentAST.root;
					break;
				}
				case NUM_DOUBLE:
				{
					AST tmp17_AST = null;
					tmp17_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp17_AST);
					match(NUM_DOUBLE);
					functionParameter_AST = (AST)currentAST.root;
					break;
				}
				case NUM_FLOAT:
				{
					AST tmp18_AST = null;
					tmp18_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp18_AST);
					match(NUM_FLOAT);
					functionParameter_AST = (AST)currentAST.root;
					break;
				}
				case NUM_INT:
				{
					AST tmp19_AST = null;
					tmp19_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp19_AST);
					match(NUM_INT);
					functionParameter_AST = (AST)currentAST.root;
					break;
				}
				case NUM_LONG:
				{
					AST tmp20_AST = null;
					tmp20_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp20_AST);
					match(NUM_LONG);
					functionParameter_AST = (AST)currentAST.root;
					break;
				}
				case QUOTED_STRING:
				{
					AST tmp21_AST = null;
					tmp21_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp21_AST);
					match(QUOTED_STRING);
					if ( inputState.guessing==0 ) {
						functionParameter_AST = (AST)currentAST.root;
						
						functionParameter_AST = quotedString( functionParameter_AST );
						
						currentAST.root = functionParameter_AST;
						currentAST.child = functionParameter_AST!=null &&functionParameter_AST.getFirstChild()!=null ?
							functionParameter_AST.getFirstChild() : functionParameter_AST;
						currentAST.advanceChildToEnd();
					}
					functionParameter_AST = (AST)currentAST.root;
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
			}
			catch (RecognitionException ex) {
				if (inputState.guessing==0) {
					reportError(ex);
					recover(ex,_tokenSet_8);
				} else {
				  throw ex;
				}
			}
			returnAST = functionParameter_AST;
		} finally { // debugging
			traceOut("functionParameter");
		}
	}
	
/**
 * The collation name wrt {@link #collationSpecification}.  Namely, the character-set.
 */
	public final void collationName() throws RecognitionException, TokenStreamException {
		
		traceIn("collationName");
		try { // debugging
			returnAST = null;
			ASTPair currentAST = new ASTPair();
			AST collationName_AST = null;
			trace("collationSpecification");
			
			try {      // for error handling
				AST tmp22_AST = null;
				tmp22_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp22_AST);
				match(IDENT);
				collationName_AST = (AST)currentAST.root;
			}
			catch (RecognitionException ex) {
				if (inputState.guessing==0) {
					reportError(ex);
					recover(ex,_tokenSet_3);
				} else {
				  throw ex;
				}
			}
			returnAST = collationName_AST;
		} finally { // debugging
			traceOut("collationName");
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
	
	protected void buildTokenTypeASTClassMap() {
		tokenTypeToASTClassMap=null;
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 2L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 32770L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 100724738L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { 100720642L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { 32702464L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { 101249026L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = { 262144L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = { 524288L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = { 557056L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	
	}
