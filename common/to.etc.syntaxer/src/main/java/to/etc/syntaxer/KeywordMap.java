package to.etc.syntaxer;


import javax.swing.text.Segment;
import java.util.Vector;

/**
 * A <code>KeywordMap</code> is similar to a hashtable in that it maps keys
 * to values. However, the `keys' are Swing segments. This allows lookups of
 * text substrings without the overhead of creating a new string object.
 *
 * @author Slava Pestov, Mike Dillon
 * @version $Id: KeywordMap.java,v 1.8 2004/05/29 01:55:26 spestov Exp $
 */
public class KeywordMap
{
	//{{{ KeywordMap constructor
	/**
	 * Creates a new <code>KeywordMap</code>.
	 * @param ignoreCase True if keys are case insensitive
	 */
	public KeywordMap(boolean ignoreCase)
	{
		this(ignoreCase, 52);
		this.m_ignoreCase = ignoreCase;
		m_noWordSep = new StringBuffer();
	} //}}}

	//{{{ KeywordMap constructor
	/**
	 * Creates a new <code>KeywordMap</code>.
	 * @param ignoreCase True if the keys are case insensitive
	 * @param mapLength The number of `buckets' to create.
	 * A value of 52 will give good performance for most maps.
	 */
	public KeywordMap(boolean ignoreCase, int mapLength)
	{
		this.m_mapLength = mapLength;
		this.m_ignoreCase = ignoreCase;
		m_map = new Keyword[mapLength];
	} //}}}

	//{{{ lookup() method
	/**
	 * Looks up a key.
	 * @param text The text segment
	 * @param offset The offset of the substring within the text segment
	 * @param length The length of the substring
	 */
	public byte lookup(Segment text, int offset, int length)
	{
		if(length == 0)
			return Token.NULL;
		Keyword k = m_map[getSegmentMapKey(text, offset, length)];
		while(k != null)
		{
			if(length != k.keyword.length)
			{
				k = k.next;
				continue;
			}
			if(SyntaxUtilities.regionMatches(m_ignoreCase, text, offset, k.keyword))
				return k.id;
			k = k.next;
		}
		return Token.NULL;
	} //}}}

	//{{{ add() method
	/**
	 * Adds a key-value mapping.
	 * @param keyword The key
	 * @param id The value
	 */
	public void add(String keyword, byte id)
	{
		add(keyword.toCharArray(), id);
	} //}}}

	//{{{ add() method
	/**
	 * Adds a key-value mapping.
	 * @param keyword The key
	 * @param id The value
	 * @since jEdit 4.2pre3
	 */
	public void add(char[] keyword, byte id)
	{
		int key = getStringMapKey(keyword);

		// complete-word command needs a list of all non-alphanumeric
		// characters used in a keyword map.
		loop : for(int i = 0; i < keyword.length; i++)
		{
			char ch = keyword[i];
			if(!Character.isLetterOrDigit(ch))
			{
				for(int j = 0; j < m_noWordSep.length(); j++)
				{
					if(m_noWordSep.charAt(j) == ch)
						continue loop;
				}

				m_noWordSep.append(ch);
			}
		}

		m_map[key] = new Keyword(keyword, id, m_map[key]);
	} //}}}

	//{{{ getNonAlphaNumericChars() method
	/**
	 * Returns all non-alphanumeric characters that appear in the
	 * keywords of this keyword map.
	 * @since jEdit 4.0pre3
	 */
	public String getNonAlphaNumericChars()
	{
		return m_noWordSep.toString();
	} //}}}

	//{{{ getKeywords() method
	/**
	 * Returns an array containing all keywords in this keyword map.
	 * @since jEdit 4.0pre3
	 */
	public String[] getKeywords()
	{
		Vector vector = new Vector(100);
		for(int i = 0; i < m_map.length; i++)
		{
			Keyword keyword = m_map[i];
			while(keyword != null)
			{
				vector.addElement(new String(keyword.keyword));
				keyword = keyword.next;
			}
		}
		String[] retVal = new String[vector.size()];
		vector.copyInto(retVal);
		return retVal;
	} //}}}

	//{{{ getIgnoreCase() method
	/**
	 * Returns true if the keyword map is set to be case insensitive,
	 * false otherwise.
	 */
	public boolean getIgnoreCase()
	{
		return m_ignoreCase;
	} //}}}

	//{{{ setIgnoreCase() method
	/**
	 * Sets if the keyword map should be case insensitive.
	 * @param ignoreCase True if the keyword map should be case
	 * insensitive, false otherwise
	 */
	public void setIgnoreCase(boolean ignoreCase)
	{
		this.m_ignoreCase = ignoreCase;
	} //}}}

	//{{{ add() method
	/**
	 * Adds the content of another keyword map to this one.
	 * @since jEdit 4.2pre3
	 */
	public void add(KeywordMap map)
	{
		for(int i = 0; i < map.m_map.length; i++)
		{
			Keyword k = map.m_map[i];
			while(k != null)
			{
				add(k.keyword, k.id);
				k = k.next;
			}
		}
	} //}}}

	//{{{ Private members

	//{{{ Instance variables
	private int m_mapLength;

	private Keyword[] m_map;

	private boolean m_ignoreCase;

	private StringBuffer m_noWordSep;

	//}}}

	//{{{ getStringMapKey() method
	private int getStringMapKey(char[] s)
	{
		return (Character.toUpperCase(s[0]) + Character.toUpperCase(s[s.length - 1])) % m_mapLength;
	} //}}}

	//{{{ getSegmentMapKey() method
	protected int getSegmentMapKey(Segment s, int off, int len)
	{
		return (Character.toUpperCase(s.array[off]) + Character.toUpperCase(s.array[off + len - 1])) % m_mapLength;
	} //}}}

	//}}}

	//{{{ Keyword class
	class Keyword
	{
		public Keyword(char[] keyword, byte id, Keyword next)
		{
			this.keyword = keyword;
			this.id = id;
			this.next = next;
		}

		public char[] keyword;

		public byte id;

		public Keyword next;
	} //}}}
}
