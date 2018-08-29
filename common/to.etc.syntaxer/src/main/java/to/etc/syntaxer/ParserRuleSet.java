package to.etc.syntaxer;


//{{{ Imports
import gnu.regexp.RE;
import java.util.*;

//}}}

/**
 * A set of parser rules.
 * @author mike dillon
 * @version $Id: ParserRuleSet.java,v 1.23 2003/06/05 00:01:49 spestov Exp $
 */
public class ParserRuleSet
{
	//{{{ getStandardRuleSet() method
	/**
	 * Returns a parser rule set that highlights everything with the
	 * specified token type.
	 * @param id The token type
	 */
	public static ParserRuleSet getStandardRuleSet(byte id)
	{
		return standard[id];
	} //}}}

	//{{{ ParserRuleSet constructor
	public ParserRuleSet(String modeName, String setName)
	{
		this.modeName = modeName;
		this.setName = setName;
		ruleMapFirst = new ParserRule[RULE_BUCKET_COUNT];
		ruleMapLast = new ParserRule[RULE_BUCKET_COUNT];
		imports = new LinkedList();
	} //}}}

	//{{{ getModeName() method
	public String getModeName()
	{
		return modeName;
	} //}}}

	//{{{ getSetName() method
	public String getSetName()
	{
		return setName;
	} //}}}

	//{{{ getName() method
	public String getName()
	{
		return modeName + "::" + setName;
	} //}}}

	//{{{ getProperties() method
	public Hashtable getProperties()
	{
		return props;
	} //}}}

	//{{{ setProperties() method
	public void setProperties(Hashtable props)
	{
		this.props = props;
		_noWordSep = null;
	} //}}}

	//{{{ resolveImports() method
	/**
	 * Resolves all rulesets added with {@link #addRuleSet(ParserRuleSet)}.
	 * @since jEdit 4.2pre3
	 */
	public void resolveImports()
	{
		Iterator iter = imports.iterator();
		while(iter.hasNext())
		{
			ParserRuleSet ruleset = (ParserRuleSet) iter.next();
			for(int i = 0; i < ruleset.ruleMapFirst.length; i++)
			{
				ParserRule rule = ruleset.ruleMapFirst[i];
				while(rule != null)
				{
					addRule(rule);
					rule = rule.next;
				}
			}

			if(ruleset.keywords != null)
			{
				if(keywords == null)
					keywords = new KeywordMap(ignoreCase);
				keywords.add(ruleset.keywords);
			}
		}
		imports.clear();
	} //}}}

	//{{{ addRuleSet() method
	/**
	 * Adds all rules contained in the given ruleset.
	 * @param ruleset The ruleset
	 * @since jEdit 4.2pre3
	 */
	public void addRuleSet(ParserRuleSet ruleset)
	{
		imports.add(ruleset);
	} //}}}

	//{{{ addRule() method
	public void addRule(ParserRule r)
	{
		ruleCount++;

		int key = Character.toUpperCase(r.hashChar) % RULE_BUCKET_COUNT;
		ParserRule last = ruleMapLast[key];
		if(last == null)
			ruleMapFirst[key] = ruleMapLast[key] = r;
		else
		{
			last.next = r;
			ruleMapLast[key] = r;
		}
	} //}}}

	//{{{ getRules() method
	public ParserRule getRules(char ch)
	{
		int key = Character.toUpperCase(ch) % RULE_BUCKET_COUNT;
		return ruleMapFirst[key];
	} //}}}

	//{{{ getRuleCount() method
	public int getRuleCount()
	{
		return ruleCount;
	} //}}}

	//{{{ getTerminateChar() method
	public int getTerminateChar()
	{
		return terminateChar;
	} //}}}

	//{{{ setTerminateChar() method
	public void setTerminateChar(int atChar)
	{
		terminateChar = (atChar >= 0) ? atChar : -1;
	} //}}}

	//{{{ getIgnoreCase() method
	public boolean getIgnoreCase()
	{
		return ignoreCase;
	} //}}}

	//{{{ setIgnoreCase() method
	public void setIgnoreCase(boolean b)
	{
		ignoreCase = b;
	} //}}}

	//{{{ getKeywords() method
	public KeywordMap getKeywords()
	{
		return keywords;
	} //}}}

	//{{{ setKeywords() method
	public void setKeywords(KeywordMap km)
	{
		keywords = km;
		_noWordSep = null;
	} //}}}

	//{{{ getHighlightDigits() method
	public boolean getHighlightDigits()
	{
		return highlightDigits;
	} //}}}

	//{{{ setHighlightDigits() method
	public void setHighlightDigits(boolean highlightDigits)
	{
		this.highlightDigits = highlightDigits;
	} //}}}

	//{{{ getDigitRegexp() method
	public RE getDigitRegexp()
	{
		return digitRE;
	} //}}}

	//{{{ setDigitRegexp() method
	public void setDigitRegexp(RE digitRE)
	{
		this.digitRE = digitRE;
	} //}}}

	//{{{ getEscapeRule() method
	public ParserRule getEscapeRule()
	{
		return escapeRule;
	} //}}}

	//{{{ setEscapeRule() method
	public void setEscapeRule(ParserRule escapeRule)
	{
		addRule(escapeRule);
		this.escapeRule = escapeRule;
	} //}}}

	//{{{ getDefault() method
	public byte getDefault()
	{
		return defaultToken;
	} //}}}

	//{{{ setDefault() method
	public void setDefault(byte def)
	{
		defaultToken = def;
	} //}}}

	//{{{ getNoWordSep() method
	public String getNoWordSep()
	{
		if(_noWordSep == null)
		{
			_noWordSep = noWordSep;
			if(noWordSep == null)
				noWordSep = "";
			if(keywords != null)
				noWordSep += keywords.getNonAlphaNumericChars();
		}
		return noWordSep;
	} //}}}

	//{{{ setNoWordSep() method
	public void setNoWordSep(String noWordSep)
	{
		this.noWordSep = noWordSep;
		_noWordSep = null;
	} //}}}

	//{{{ isBuiltIn() method
	/**
	 * Returns if this is a built-in ruleset.
	 * @since jEdit 4.2pre1
	 */
	public boolean isBuiltIn()
	{
		return builtIn;
	} //}}}

	//{{{ toString() method
	@Override
	public String toString()
	{
		return getClass().getName() + "[" + modeName + "::" + setName + "]";
	} //}}}

	//{{{ Private members
	private static ParserRuleSet[] standard;

	static
	{
		standard = new ParserRuleSet[Token.ID_COUNT];
		for(byte i = 0; i < standard.length; i++)
		{
			standard[i] = new ParserRuleSet(null, null);
			standard[i].setDefault(i);
			standard[i].builtIn = true;
		}
	}

	private static final int RULE_BUCKET_COUNT = 128;

	private String modeName, setName;

	private Hashtable props;

	private KeywordMap keywords;

	private int ruleCount;

	private ParserRule[] ruleMapFirst;

	private ParserRule[] ruleMapLast;

	private LinkedList imports;

	private int terminateChar = -1;

	private boolean ignoreCase = true;

	private byte defaultToken;

	private ParserRule escapeRule;

	private boolean highlightDigits;

	private RE digitRE;

	private String _noWordSep;

	private String noWordSep;

	private boolean builtIn;
	//}}}
}
