package to.etc.syntaxer;

import gnu.regexp.*;
import javax.swing.text.Segment;
import java.util.*;

/**
 * A token marker splits lines of text into tokens. Each token carries
 * a length field and an identification tag that can be mapped to a color
 * or font style for painting that token.
 *
 * @author Slava Pestov, mike dillon
 * @version $Id: TokenMarker.java,v 1.62 2003/12/27 05:14:46 spestov Exp $
 *
 * @see org.gjt.sp.jedit.syntax.Token
 * @see org.gjt.sp.jedit.syntax.TokenHandler
 */
public class TokenMarker
{
	//{{{ TokenMarker constructor
	public TokenMarker()
	{
		m_ruleSets = new Hashtable(64);
	} //}}}

	//{{{ addRuleSet() method
	public void addRuleSet(ParserRuleSet rules)
	{
		m_ruleSets.put(rules.getSetName(), rules);

		if(rules.getSetName().equals("MAIN"))
			m_mainRuleSet = rules;
	} //}}}

	//{{{ getMainRuleSet() method
	public ParserRuleSet getMainRuleSet()
	{
		return m_mainRuleSet;
	} //}}}

	//{{{ getRuleSet() method
	public ParserRuleSet getRuleSet(String setName)
	{
		return (ParserRuleSet) m_ruleSets.get(setName);
	} //}}}

	//{{{ getRuleSets() method
	/**
	 * @since jEdit 4.2pre3
	 */
	public ParserRuleSet[] getRuleSets()
	{
		return (ParserRuleSet[]) m_ruleSets.values().toArray(new ParserRuleSet[m_ruleSets.size()]);
	} //}}}

	//{{{ markTokens() method
	/**
	 * Do not call this method directly; call Buffer.markTokens() instead.
	 */
	public LineContext markTokens(LineContext prevContext, TokenHandler tokenHandler, Segment line)
	{
		//{{{ Set up some instance variables
		// this is to avoid having to pass around lots and lots of
		// parameters.
		this.m_tokenHandler = tokenHandler;
		this.m_line = line;

		m_lastOffset = line.offset;
		m_lineLength = line.count + line.offset;

		m_context = new LineContext();

		if(prevContext == null) {
			m_context.rules = getMainRuleSet();
			if(m_context.rules == null)
				throw new IllegalStateException("No context rules??");
		} else {
			m_context.parent = prevContext.parent;
			m_context.inRule = prevContext.inRule;
			m_context.rules = prevContext.rules;
			m_context.spanEndSubst = prevContext.spanEndSubst;
		}

		m_keywords = m_context.rules.getKeywords();
		m_escaped = false;

		m_seenWhitespaceEnd = false;
		m_whitespaceEnd = line.offset;
		//}}}

		//{{{ Main parser loop
		ParserRule rule;
		int terminateChar = m_context.rules.getTerminateChar();
		boolean terminated = false;

		main_loop : for(m_pos = line.offset; m_pos < m_lineLength; m_pos++)
		{
			//{{{ check if we have to stop parsing
			if(terminateChar >= 0 && m_pos - line.offset >= terminateChar && !terminated)
			{
				terminated = true;
				m_context = new LineContext(ParserRuleSet.getStandardRuleSet(m_context.rules.getDefault()), m_context);
				m_keywords = m_context.rules.getKeywords();
			} //}}}

			//{{{ check for end of delegate
			if(m_context.parent != null)
			{
				rule = m_context.parent.inRule;
				if(rule != null)
				{
					if(checkDelegateEnd(rule))
					{
						m_seenWhitespaceEnd = true;
						continue main_loop;
					}
				}
			} //}}}

			//{{{ check every rule
			char ch = line.array[m_pos];

			rule = m_context.rules.getRules(ch);
			while(rule != null)
			{
				// stop checking rules if there was a match
				if(handleRule(rule, false))
				{
					m_seenWhitespaceEnd = true;
					continue main_loop;
				}

				rule = rule.next;
			} //}}}

			//{{{ check if current character is a word separator
			if(Character.isWhitespace(ch))
			{
				if(!m_seenWhitespaceEnd)
					m_whitespaceEnd = m_pos + 1;

				if(m_context.inRule != null)
					handleRule(m_context.inRule, true);

				handleNoWordBreak();

				markKeyword(false);

				if(m_lastOffset != m_pos)
				{
					tokenHandler.handleToken(line, m_context.rules.getDefault(), m_lastOffset - line.offset, m_pos - m_lastOffset, m_context);
				}

				tokenHandler.handleToken(line, m_context.rules.getDefault(), m_pos - line.offset, 1, m_context);
				m_lastOffset = m_pos + 1;

				m_escaped = false;
			}
			else
			{
				if(m_keywords != null || m_context.rules.getRuleCount() != 0)
				{
					String noWordSep = m_context.rules.getNoWordSep();

					if(!Character.isLetterOrDigit(ch) && noWordSep.indexOf(ch) == -1)
					{
						if(m_context.inRule != null)
							handleRule(m_context.inRule, true);

						handleNoWordBreak();

						markKeyword(true);

						tokenHandler.handleToken(line, m_context.rules.getDefault(), m_lastOffset - line.offset, 1, m_context);
						m_lastOffset = m_pos + 1;
					}
				}

				m_seenWhitespaceEnd = true;
				m_escaped = false;
			} //}}}
		} //}}}

		//{{{ Mark all remaining characters
		m_pos = m_lineLength;

		if(m_context.inRule != null)
			handleRule(m_context.inRule, true);

		handleNoWordBreak();
		markKeyword(true);
		//}}}

		//{{{ Unwind any NO_LINE_BREAK parent delegates
		unwind : while(m_context.parent != null)
		{
			rule = m_context.parent.inRule;
			if((rule != null && (rule.action & ParserRule.NO_LINE_BREAK) == ParserRule.NO_LINE_BREAK) || terminated)
			{
				m_context = m_context.parent;
				m_keywords = m_context.rules.getKeywords();
				m_context.inRule = null;
			}
			else
				break unwind;
		} //}}}

		tokenHandler.handleToken(line, Token.END, m_pos - line.offset, 0, m_context);

		m_context = m_context.intern();
		tokenHandler.setLineContext(m_context);
		return m_context;
	} //}}}

	private Hashtable m_ruleSets;

	private ParserRuleSet m_mainRuleSet;

	private TokenHandler m_tokenHandler;

	private Segment m_line;

	private LineContext m_context;

	private KeywordMap m_keywords;

	private Segment m_pattern = new Segment();

	private int m_lastOffset;

	private int m_lineLength;

	private int m_pos;

	private boolean m_escaped;

	private int m_whitespaceEnd;

	private boolean m_seenWhitespaceEnd;

	private boolean checkDelegateEnd(ParserRule rule)
	{
		if(rule.end == null)
			return false;

		LineContext tempContext = m_context;
		m_context = m_context.parent;
		m_keywords = m_context.rules.getKeywords();
		boolean tempEscaped = m_escaped;
		boolean b = handleRule(rule, true);
		m_context = tempContext;
		m_keywords = m_context.rules.getKeywords();

		if(b && !tempEscaped)
		{
			if(m_context.inRule != null)
				handleRule(m_context.inRule, true);

			markKeyword(true);

			m_context = (LineContext) m_context.parent.clone();

			m_tokenHandler.handleToken(m_line, (m_context.inRule.action & ParserRule.EXCLUDE_MATCH) == ParserRule.EXCLUDE_MATCH ? m_context.rules
					.getDefault() : m_context.inRule.token, m_pos - m_line.offset, m_pattern.count, m_context);

			m_keywords = m_context.rules.getKeywords();
			m_context.inRule = null;
			m_lastOffset = m_pos + m_pattern.count;

			// move pos to last character of match sequence
			m_pos += (m_pattern.count - 1);

			return true;
		}

		// check escape rule of parent
		if((rule.action & ParserRule.NO_ESCAPE) == 0)
		{
			ParserRule escape = m_context.parent.rules.getEscapeRule();
			return escape != null && handleRule(escape, false);
		}

		return false;
	}

	/**
	 * Checks if the rule matches the line at the current position
	 * and handles the rule if it does match
	 */
	private boolean handleRule(ParserRule checkRule, boolean end)
	{
		//{{{ Some rules can only match in certain locations
		if(!end)
		{
			if(Character.toUpperCase(checkRule.hashChar) != Character.toUpperCase(m_line.array[m_pos]))
			{
				return false;
			}
		}

		int offset = ((checkRule.action & ParserRule.MARK_PREVIOUS) != 0) ? m_lastOffset : m_pos;
		int posMatch = (end ? checkRule.endPosMatch : checkRule.startPosMatch);

		if((posMatch & ParserRule.AT_LINE_START) == ParserRule.AT_LINE_START)
		{
			if(offset != m_line.offset)
				return false;
		}
		else if((posMatch & ParserRule.AT_WHITESPACE_END) == ParserRule.AT_WHITESPACE_END)
		{
			if(offset != m_whitespaceEnd)
				return false;
		}
		else if((posMatch & ParserRule.AT_WORD_START) == ParserRule.AT_WORD_START)
		{
			if(offset != m_lastOffset)
				return false;
		} //}}}

		int matchedChars = 1;
		CharIndexedSegment charIndexed = null;
		REMatch match = null;

		//{{{ See if the rule's start or end sequence matches here
		if(!end || (checkRule.action & ParserRule.MARK_FOLLOWING) == 0)
		{
			// the end cannot be a regular expression
			if((checkRule.action & ParserRule.REGEXP) == 0 || end)
			{
				if(end)
				{
					if(m_context.spanEndSubst != null)
						m_pattern.array = m_context.spanEndSubst;
					else
						m_pattern.array = checkRule.end;
				}
				else
					m_pattern.array = checkRule.start;
				m_pattern.offset = 0;
				m_pattern.count = m_pattern.array.length;
				matchedChars = m_pattern.count;

				if(!SyntaxUtilities.regionMatches(m_context.rules.getIgnoreCase(), m_line, m_pos, m_pattern.array))
				{
					return false;
				}
			}
			else
			{
				// note that all regexps start with \A so they only
				// match the start of the string
				int matchStart = m_pos - m_line.offset;
				charIndexed = new CharIndexedSegment(m_line, matchStart);
				match = checkRule.startRegexp.getMatch(charIndexed, 0, RE.REG_ANCHORINDEX);
				if(match == null)
					return false;
				else if(match.getStartIndex() != 0)
					throw new InternalError("Can't happen");
				else
				{
					matchedChars = match.getEndIndex();
					/* workaround for hang if match was
					 * zero-width. not sure if there is
					 * a better way to handle this */
					if(matchedChars == 0)
						matchedChars = 1;
				}
			}
		} //}}}

		//{{{ Check for an escape sequence
		if((checkRule.action & ParserRule.IS_ESCAPE) == ParserRule.IS_ESCAPE)
		{
			if(m_context.inRule != null)
				handleRule(m_context.inRule, true);

			m_escaped = !m_escaped;
			m_pos += m_pattern.count - 1;
		}
		else if(m_escaped)
		{
			m_escaped = false;
			m_pos += m_pattern.count - 1;
		} //}}}
		//{{{ Handle start of rule
		else if(!end)
		{
			if(m_context.inRule != null)
				handleRule(m_context.inRule, true);

			markKeyword((checkRule.action & ParserRule.MARK_PREVIOUS) != ParserRule.MARK_PREVIOUS);

			switch(checkRule.action & ParserRule.MAJOR_ACTIONS)
			{
				//{{{ SEQ
				case ParserRule.SEQ:
					m_context.spanEndSubst = null;

					if((checkRule.action & ParserRule.REGEXP) != 0)
					{
						handleTokenWithSpaces(m_tokenHandler, checkRule.token, m_pos - m_line.offset, matchedChars, m_context);
					}
					else
					{
						m_tokenHandler.handleToken(m_line, checkRule.token, m_pos - m_line.offset, matchedChars, m_context);
					}

					// a DELEGATE attribute on a SEQ changes the
					// ruleset from the end of the SEQ onwards
					if(checkRule.delegate != null)
					{
						m_context = new LineContext(checkRule.delegate, m_context.parent);
						m_keywords = m_context.rules.getKeywords();
					}
					break;
				//}}}
				//{{{ SPAN, EOL_SPAN
				case ParserRule.SPAN:
				case ParserRule.EOL_SPAN:
					m_context.inRule = checkRule;

					byte tokenType = ((checkRule.action & ParserRule.EXCLUDE_MATCH) == ParserRule.EXCLUDE_MATCH ? m_context.rules.getDefault()
							: checkRule.token);

					if((checkRule.action & ParserRule.REGEXP) != 0)
					{
						handleTokenWithSpaces(m_tokenHandler, tokenType, m_pos - m_line.offset, matchedChars, m_context);
					}
					else
					{
						m_tokenHandler.handleToken(m_line, tokenType, m_pos - m_line.offset, matchedChars, m_context);
					}

					char[] spanEndSubst = null;
					/* substitute result of matching the rule start
					 * into the end string.
					 *
					 * eg, in shell script mode, <<\s*(\w+) is
					 * matched into \<$1\> to construct rules for
					 * highlighting read-ins like this <<EOF
					 * ...
					 * EOF
					 */
					if(charIndexed != null && checkRule.end != null)
					{
						spanEndSubst = substitute(match, checkRule.end);
					}

					m_context.spanEndSubst = spanEndSubst;
					m_context = new LineContext(checkRule.delegate, m_context);
					m_keywords = m_context.rules.getKeywords();

					break;
				//}}}
				//{{{ MARK_FOLLOWING
				case ParserRule.MARK_FOLLOWING:
					m_tokenHandler.handleToken(m_line, (checkRule.action & ParserRule.EXCLUDE_MATCH) == ParserRule.EXCLUDE_MATCH ? m_context.rules
							.getDefault() : checkRule.token, m_pos - m_line.offset, m_pattern.count, m_context);

					m_context.spanEndSubst = null;
					m_context.inRule = checkRule;
					break;
				//}}}
				//{{{ MARK_PREVIOUS
				case ParserRule.MARK_PREVIOUS:
					m_context.spanEndSubst = null;

					if((checkRule.action & ParserRule.EXCLUDE_MATCH) == ParserRule.EXCLUDE_MATCH)
					{
						if(m_pos != m_lastOffset)
						{
							m_tokenHandler.handleToken(m_line, checkRule.token, m_lastOffset - m_line.offset, m_pos - m_lastOffset, m_context);
						}

						m_tokenHandler.handleToken(m_line, m_context.rules.getDefault(), m_pos - m_line.offset, m_pattern.count, m_context);
					}
					else
					{
						m_tokenHandler.handleToken(m_line, checkRule.token, m_lastOffset - m_line.offset, m_pos - m_lastOffset + m_pattern.count,
								m_context);
					}

					break;
				//}}}
				default:
					throw new InternalError("Unhandled major action");
			}

			// move pos to last character of match sequence
			m_pos += (matchedChars - 1);
			m_lastOffset = m_pos + 1;

			// break out of inner for loop to check next char
		} //}}}
		//{{{ Handle end of MARK_FOLLOWING
		else if((m_context.inRule.action & ParserRule.MARK_FOLLOWING) != 0)
		{
			if(m_pos != m_lastOffset)
			{
				m_tokenHandler.handleToken(m_line, m_context.inRule.token, m_lastOffset - m_line.offset, m_pos - m_lastOffset, m_context);
			}

			m_lastOffset = m_pos;
			m_context.inRule = null;
		} //}}}

		return true;
	} //}}}

	//{{{ handleNoWordBreak() method
	private void handleNoWordBreak()
	{
		if(m_context.parent != null)
		{
			ParserRule rule = m_context.parent.inRule;
			if(rule != null && (m_context.parent.inRule.action & ParserRule.NO_WORD_BREAK) != 0)
			{
				if(m_pos != m_lastOffset)
				{
					m_tokenHandler.handleToken(m_line, rule.token, m_lastOffset - m_line.offset, m_pos - m_lastOffset, m_context);
				}

				m_lastOffset = m_pos;
				m_context = m_context.parent;
				m_keywords = m_context.rules.getKeywords();
				m_context.inRule = null;
			}
		}
	} //}}}

	//{{{ handleTokenWithSpaces() method
	private void handleTokenWithSpaces(TokenHandler tokenHandler, byte tokenType, int start, int len, LineContext context)
	{
		int last = start;
		int end = start + len;

		for(int i = start; i < end; i++)
		{
			if(Character.isWhitespace(m_line.array[i + m_line.offset]))
			{
				if(last != i)
				{
					tokenHandler.handleToken(m_line, tokenType, last, i - last, context);
				}
				tokenHandler.handleToken(m_line, tokenType, i, 1, context);
				last = i + 1;
			}
		}

		if(last != end)
		{
			tokenHandler.handleToken(m_line, tokenType, last, end - last, context);
		}
	} //}}}

	//{{{ markKeyword() method
	private void markKeyword(boolean addRemaining)
	{
		int len = m_pos - m_lastOffset;
		if(len == 0)
			return;

		//{{{ Do digits
		if(m_context.rules.getHighlightDigits())
		{
			boolean digit = false;
			boolean mixed = false;

			for(int i = m_lastOffset; i < m_pos; i++)
			{
				char ch = m_line.array[i];
				if(Character.isDigit(ch))
					digit = true;
				else
					mixed = true;
			}

			if(mixed)
			{
				RE digitRE = m_context.rules.getDigitRegexp();

				// only match against regexp if its not all
				// digits; if all digits, no point matching
				if(digit)
				{
					if(digitRE == null)
					{
						// mixed digit/alpha keyword,
						// and no regexp... don't
						// highlight as DIGIT
						digit = false;
					}
					else
					{
						CharIndexedSegment seg = new CharIndexedSegment(m_line, false);
						int oldCount = m_line.count;
						int oldOffset = m_line.offset;
						m_line.offset = m_lastOffset;
						m_line.count = len;
						if(!digitRE.isMatch(seg))
							digit = false;
						m_line.offset = oldOffset;
						m_line.count = oldCount;
					}
				}
			}

			if(digit)
			{
				m_tokenHandler.handleToken(m_line, Token.DIGIT, m_lastOffset - m_line.offset, len, m_context);
				m_lastOffset = m_pos;

				return;
			}
		} //}}}

		//{{{ Do keywords
		if(m_keywords != null)
		{
			byte id = m_keywords.lookup(m_line, m_lastOffset, len);

			if(id != Token.NULL)
			{
				m_tokenHandler.handleToken(m_line, id, m_lastOffset - m_line.offset, len, m_context);
				m_lastOffset = m_pos;
				return;
			}
		} //}}}

		//{{{ Handle any remaining crud
		if(addRemaining)
		{
			m_tokenHandler.handleToken(m_line, m_context.rules.getDefault(), m_lastOffset - m_line.offset, len, m_context);
			m_lastOffset = m_pos;
		} //}}}
	} //}}}

	//{{{ substitute() method
	private char[] substitute(REMatch match, char[] end)
	{
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < end.length; i++)
		{
			char ch = end[i];
			if(ch == '$')
			{
				if(i == end.length - 1)
					buf.append(ch);
				else
				{
					char digit = end[i + 1];
					if(!Character.isDigit(digit))
						buf.append(ch);
					else
					{
						buf.append(match.toString(digit - '0'));
						i++;
					}
				}
			}
			else
				buf.append(ch);
		}

		char[] returnValue = new char[buf.length()];
		buf.getChars(0, buf.length(), returnValue, 0);
		return returnValue;
	} //}}}

	//}}}

	//{{{ LineContext class
	/**
	 * Stores persistent per-line syntax parser state.
	 */
	public static class LineContext
	{
		private static Hashtable intern = new Hashtable();

		public LineContext parent;

		public ParserRule inRule;

		public ParserRuleSet rules;

		// used for SPAN_REGEXP rules; otherwise null
		public char[] spanEndSubst;

		//{{{ LineContext constructor
		public LineContext(ParserRuleSet rs, LineContext lc)
		{
			rules = rs;
			parent = (lc == null ? null : (LineContext) lc.clone());
		} //}}}

		//{{{ LineContext constructor
		public LineContext()
		{} //}}}

		//{{{ intern() method
		public LineContext intern()
		{
			Object obj = intern.get(this);
			if(obj == null)
			{
				intern.put(this, this);
				return this;
			}
			else
				return (LineContext) obj;
		} //}}}

		//{{{ hashCode() method
		@Override
		public int hashCode()
		{
			if(inRule != null)
				return inRule.hashCode();
			else if(rules != null)
				return rules.hashCode();
			else
				return 0;
		} //}}}

		//{{{ equals() method
		@Override
		public boolean equals(Object obj)
		{
			if(obj instanceof LineContext)
			{
				LineContext lc = (LineContext) obj;
				return lc.inRule == inRule && lc.rules == rules && objEqual(parent, lc.parent) && charArraysEqual(spanEndSubst, lc.spanEndSubst);
			}
			else
				return false;
		} //}}}

		//{{{ clone() method
		@Override
		public Object clone()
		{
			LineContext lc = new LineContext();
			lc.inRule = inRule;
			lc.rules = rules;
			lc.parent = (parent == null) ? null : (LineContext) parent.clone();
			lc.spanEndSubst = spanEndSubst;

			return lc;
		} //}}}

		//{{{ charArraysEqual() method
		private boolean charArraysEqual(char[] c1, char[] c2)
		{
			if(c1 == null)
				return (c2 == null);
			else if(c2 == null)
				return (c1 == null);

			if(c1.length != c2.length)
				return false;

			for(int i = 0; i < c1.length; i++)
			{
				if(c1[i] != c2[i])
					return false;
			}

			return true;
		} //}}}
	} //}}}

	static boolean objEqual(Object a, Object b)
	{
		if(a == b)
			return true;
		if(a == null || b == null)
			return false;
		return a.equals(b);
	}
}
