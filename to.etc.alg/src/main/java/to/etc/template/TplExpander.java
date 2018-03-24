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
package to.etc.template;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import to.etc.util.*;

/**
 * VERY OLD - DO NOT USE
 *	Expands simple templates in runtime.
 *	All instances of $name are replaced with the value of name in the
 *	context hash table.
 *	When the tag <loop value> is encountered, the value is looked up in the
 *	context; it must be an iteration, array or enumeration. If so, the data
 *	following the loop tag till the </loop> tag is repeated for each instance
 *	of the enumeration.
 *
 *	<h3>Expanded names (default) are:</h3>
 *	<ul>
 *		<li>appurl: the URL for the servlet. This is the registered name of the
 *			servlet, like http://localhost:8080/servlet/WebNote/
 *		</li>
 *		<li>fullurl: The complete URL, including query string (if applicable).
 *			for instance: http://localhost:8080/servlet/WebNote/klant?key=121</li>
 *		<li>host: the complete host and port name: http://localhost:8080/</li>
 *		<li>request: returns the HttpServletRequest instance</li>
 *		<li>prevurl, referred, referrer: returns the REFERER header.</li>
 *	</ul>
 *
 * <p>This uses Java Introspection to allow very basic functions to be called
 * on variables found thru the context. For a name like <code>foo.bar</code> it
 * will attempt to get an object by looking up the key <code>foo</code> in the
 * hash table. It will then look for a function that is called <code>getBar()</code>
 * or <code>bar()</code> on that object. When found it will call that function
 * and the result will be used for the next item in the dotted list.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
@Deprecated
public class TplExpander {
	/// The callback to use to retrieve values.
	protected TplCallback		m_cb;

	/// The current "local vars" table for loop stuff..
	private Map<String, Object>	m_ht	= new HashMap<String, Object>(15);

	/// The contents of the template.
	private String				m_buf;

	/// The outputstream to expand to.

	/// The writer thing to write the expanded output to
	private PrintWriter			m_ps;

	boolean						m_html	= true;


	/**
	 * Constructor
	 */
	public TplExpander(TplCallback cb) {
		m_cb = cb;
	}

	public TplExpander(TplCallback cb, boolean html) {
		m_cb = cb;
		m_html = html;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Stuff to retrieve servlet base path					*/
	/*--------------------------------------------------------------*/
	/// The servlet's base path, excluding the host name,
	protected String	m_servlet_basepath;

	/// The servlet's FULL path
	protected String	m_servlet_fullpath;

	/// The servlet's host URL, excluding the servlet path but including port e.a.
	protected String	m_hosturl;

	/// This-servlet's host name only
	protected String	m_hostname;

	/// The port number for the host.
	protected int		m_hostport;


	protected String slconc(String s1, String s2) {
		if(s1.length() == 0)
			return s2;
		if(s2.length() == 0)
			return s1;

		boolean b1 = s1.endsWith("/");
		boolean b2 = s2.charAt(0) == '/';

		if(b1 && b2)
			return s1 + s2.substring(1);
		else if(!b1 && !b2)
			return s1 + "/" + s2;
		return s1 + s2;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	The main expander...								*/
	/*--------------------------------------------------------------*/
	/**
	 *	Expands the template to the printwriter spec'd.
	 */
	public void expand(InputStream is, PrintWriter pw) throws Exception {
		try {
			m_ps = pw;
			StringBuffer sb = new StringBuffer(8192); // Get a big buffer.
			char[] buf = new char[512];

			Reader r = new InputStreamReader(is);
			while(true) {
				int bl = r.read(buf);
				if(bl <= 0)
					break;
				sb.append(buf, 0, bl);
			}
			m_buf = sb.toString(); // The actual contents
			expandPart(m_buf);
		} finally {
			m_ps = null;
			m_buf = null;
		}
	}

	//	public String	expand(String input) throws Exception
	//	{
	//		m_buf = input;
	//		expandInternal()
	//	}

	public void expand(String input, PrintWriter output) throws Exception {
		m_buf = input;
		m_ps = output;
		expandPart(input);
	}

	public String expand(String input) throws Exception {
		m_buf = input;
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		m_ps = pw;
		expandPart(input);
		return sw.toString();
	}

	/**
	 *	Expands the string passed to the outputstream, taking all special tokens
	 *	into account.
	 */
	private void expandPart(String s) throws TplException {
		ExpReader r = new ExpReader(s);

		while(true) {
			int tc = r.nextToken(); // Read next thing,
			switch(tc){
				case ExpReader.tvEof:
					//-- End of expansion - exit.
					return;

				case ExpReader.tvLit:
					//-- Expand directly to outputstream;
					m_ps.print(r.getValue());
					break;

				case ExpReader.tvEndLoop:
					throw new TplException("/endloop tag found without /loop.");

				case ExpReader.tvExpand:
					expandVar(r.getValue());
					break;

				case ExpReader.tvLoop:
					expandLoop(r);
					break;

				case ExpReader.tvIf:
					expandIf(r, false);
					break;

				case ExpReader.tvIfNot:
					expandIf(r, true);
					break;
			}
		}
	}


	/**
	 *	Takes the (dotted) variable name, and determines it's value. The value
	 *	is then written to the output, thereby expanding the variable.
	 */
	private void expandVar(String expr) throws TplException {
		//-- 1. The expression may consist of dotted names.
		if(expr.startsWith("*")) {
			Object s = evalExpr(expr.substring(1));
			m_ps.print(s.toString());
			return;
		}
		Object s = evalExpr(expr);
		if(m_html) {
			m_ps.print(StringTool.htmlStringize(s.toString()));
		} else {
			m_ps.print(s.toString());
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Variable evaluation and expansion					*/
	/*--------------------------------------------------------------*/
	/**
	 *	This function can be overridden to provide more predefined names. It
	 *	should return null if it does not recognise the name.
	 */
	public Object findName(String name) {
		return null;
	}


	/**
	 * If the name passed is a predefined name this function will return the
	 * result object for the name. This is better than putting all predefined
	 * names in the hash table because many names must be computed, and building
	 * such a hash table takes time and resources (that are garbage after each
	 * request!).
	 * If the name passed is not a predefined name the routine returns null.
	 */
	protected Object findPredef(String name) {
		return null;
	}

	/**
	 * Locates a root name. The name is first searched in this-context's hashtable;
	 * then all user functions are tried. This allows the put method to
	 * override <b>all</b> names!!
	 */
	protected Object locateName(String name) throws TplException {
		Object o;

		o = m_ht.get(name); // Is this a local variable?
		if(o != null)
			return o; // Yes-> use it,
		o = m_cb.getValue(name); // Is it a caller-known name?
		if(o != null)
			return o;
		o = findName(name);
		if(o != null)
			return o;
		o = findPredef(name);
		return o;

	}


	/**
	 *	Evaluates a dotted name and returns the object referred to.
	 */
	private Object evalExpr(String ins) throws TplException {
		Object v = m_cb.getValue(ins);
		if(v != null)
			return v;

		try {
			StringTokenizer st = new StringTokenizer(ins, ".", true);

			//-- Now loop: get all names in the dotted names array,
			String[] ar = new String[20];
			int ix = 0;
			while(true) {
				if(ix >= ar.length)
					throw new TplException("Too many dots in name..");

				//-- Get a name,
				if(!st.hasMoreTokens())
					throw new TplException("Missing name in variable.");

				String tok = st.nextToken().trim();
				ar[ix] = tok; // Save token;
				ix++;

				//-- Now handle DOT or other specifier.
				tok = null;
				if(!st.hasMoreTokens())
					break;
				tok = st.nextToken(); // Next token == dot?
				if(!tok.equals("."))
					break;
			}

			//-- Ok: get the value for the expanded thingo.
			Object o = evalNameList(ar, ix);

			//-- TODO: Handle extra specifiers!!!
			return o;
		} catch(TplException x) {
			x.printStackTrace();
			throw new TplException(ins + ": error- " + x.toString());
		}
	}


	private String getNonSpaceToken(StringTokenizer st) {
		for(;;) {
			if(!st.hasMoreTokens())
				return null;
			String v = st.nextToken();
			if(!v.equals(" "))
				return v;
		}
	}

	/**
	 *	Takes a tokenizer and scans an entire dotted name into an array. It
	 *	returns the next token in the array.
	 */
	private int scanIntoArray(String[] ar, StringTokenizer st) throws TplException {
		int ix = 0;
		while(true) {
			if(ix >= ar.length)
				throw new TplException("Too many dots in name..");

			//-- Get a name,
			String tok = getNonSpaceToken(st);
			if(tok == null)
				throw new TplException("Missing name in variable.");
			ar[ix] = tok; // Save token;
			ix++;

			//-- Now handle DOT or other specifier.
			tok = getNonSpaceToken(st);
			if(tok == null)
				break;
			if(!tok.equals("."))
				break;
		}
		return ix;
	}


	private int			m_v_ix;

	private int			m_v_ct;

	private String[]	m_v;


	/**
	 *	Takes a names array, and returns a valid object referred to.
	 */
	private Object evalNameList(String[] ar, int ct) throws TplException {
		try {
			//-- 1.	Get the root name..
			m_v_ix = 1;
			m_v_ct = ct;
			m_v = ar;
			Object o = locateName(ar[0]);
			if(o == null) {
				//-- Try name.name if possible,
				if(ct > 1)
					o = locateName(ar[0] + "." + ar[1]);
				if(o == null)
					throw new TplException(ar[0] + ": variable not found.");
				m_v_ix = 2;
			}

			//-- Right. Locate all segments.
			while(m_v_ix < m_v_ct) {
				o = evalReference(o);
			}
			return o;
		} finally {
			m_v = null;
		}
	}


	private Object evalReference(Object root_o) throws TplException {
		Object o = null;

		//		String	name= m_v[m_v_ix];			// Get the current name;

		//-- 1. Does a getXxx method exist?
		o = tryResultSet(root_o);
		if(o != null)
			return o;
		o = tryBasic(root_o);
		if(o != null)
			return o;
		o = tryGetOnly(root_o);
		if(o != null)
			return o;
		throw new TplException("Name/method " + m_v[m_v_ix] + "not found in class " + root_o.getClass().getName());
	}

	/**
	 *	If the object is a result set, just call the getString thingy on the
	 *	object.
	 */
	private Object tryResultSet(Object root_o) throws TplException {
		if(!(root_o instanceof java.sql.ResultSet))
			return null;
		String name = m_v[m_v_ix++]; // Get the current name;
		java.sql.ResultSet rs = (java.sql.ResultSet) root_o;
		try {
			String v = rs.getString(name);
			if(rs.wasNull())
				return "";
			if(v == null)
				return "(field " + name + " not in result set)";
			return v;
		} catch(java.sql.SQLException x) {
			return "SQLException " + x.toString();
		}
	}

	private Object tryGetOnly(Object root_o) throws TplException {
		//-- Does a get(String) method exist?
		String n = "get";
		Class< ? >[] args = new Class[1];
		args[0] = n.getClass();

		Method m;
		try {
			m = root_o.getClass().getMethod(n, args);
		} catch(Exception x) {
			return null;
		}

		//-- Method found- use it.
		String[] aa = new String[1];
		aa[0] = m_v[m_v_ix++]; // The name to GET.

		try {
			return m.invoke(root_o, (Object[]) aa);
		} catch(Exception x) {
			x.printStackTrace();
			return null;
		}
	}

	private Object tryBasic(Object root_o) throws TplException {
		//-- Try to find & call a getXxx or Xxx method.
		String name = m_v[m_v_ix];
		String aname = "get" + name;
		Method[] mar = root_o.getClass().getMethods();

		try {
			for(int i = 0; i < mar.length; i++) {
				Method m = mar[i];

				if(m.getName().equalsIgnoreCase(name) || m.getName().equalsIgnoreCase(aname)) {
					//-- Is this getXxx(void)?
					Class< ? >[] par = m.getParameterTypes();
					if(par.length == 0) {
						Object o = m.invoke(root_o, (Object[]) null);
						if(o == null)
							throw new TplException(m.getName() + ": Method call returned null");
						m_v_ix++;
						return o;
					} else if(par.length == 1) {
						//-- Allow getXxx(string) where string is the next value;
						if(par[0] == name.getClass()) {
							//-- Is STRING parameter. Is there a next element?
							if(m_v_ix + 1 < m_v_ct) {
								Object aar[] = new Object[1];
								aar[0] = m_v[m_v_ix + 1];
								Object o = m.invoke(root_o, aar);
								if(o == null)
									throw new TplException(m.getName() + ": Method call returned null");
								m_v_ix += 2;
								return o;
							}
						}
					}
				}
			}
		} catch(Exception x) {
			x.printStackTrace();
			throw new TplException("Exception in METHOD call- " + x.toString());
		}

		return null;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Loop expansion.										*/
	/*--------------------------------------------------------------*/
	private void expandLoop(ExpReader org_r) throws TplException {
		//		System.out.println("Loop: ->"+org_r.getValue()+"<-");

		//-- 1. Get the ITERATOR object and the name of the loop var...
		StringTokenizer st = new StringTokenizer(org_r.getValue(), " \t.", true);

		String name = getNonSpaceToken(st);
		if(name == null)
			throw new TplException("loop: missing parameters.");

		if(!st.hasMoreTokens())
			throw new TplException("loop: missing loop expression or bad separator between loop iterator and expression");
		if(!st.nextToken().equals(" "))
			throw new TplException("loop: missing loop expression or bad separator between loop iterator and expression");

		//-- Now loop: get all names in the dotted names array,
		String[] ar = new String[20];
		int ix = scanIntoArray(ar, st);

		//-- Now: evaluate the loop expression,
		Object o = evalNameList(ar, ix);

		//-- Ok: now we have to find the section that needs to be looped;
		String loopsect = findLoopEnd(org_r);

		//		System.out.println("\n---- Loop start ------");
		//		System.out.println(loopsect);
		//		System.out.println("\n---- Loop ends ------");

		//-- Now: try all kinds of iterators.
		if(tryArrayLoop(o, name, loopsect))
			return;
		if(tryIsEnum(o, name, loopsect))
			return;
		if(tryHasEnum(o, name, loopsect))
			return;


	}


	/**
	 *	Finds the next /loop thing. It allows for nested loops. It returns the
	 *	substring between the loop and the /loop so that this string can be
	 *	repeatedly expanded.
	 */
	private String findLoopEnd(ExpReader org_r) throws TplException {
		//-- Get the current start of the string;
		int spos = org_r.getPos(); // Directly after the loop tag.
		int epos;
		int depth = 0;

		for(;;) {
			epos = org_r.getPos(); // Save position just before next token.
			int tc = org_r.nextToken(); // Get next token!
			if(tc == ExpReader.tvEof)
				throw new TplException("loop: missing /loop tag.");
			else if(tc == ExpReader.tvLoop)
				depth++; // Nested loop found..
			else if(tc == ExpReader.tvEndLoop) {
				if(depth == 0) // Depth 0: end of loop found,
				{
					//-- Return the substring between loop and /loop. The current position is left after the /loop
					return m_buf.substring(spos, epos);
				}
				depth--;
			}
		}
	}


	/**
	 *	Called to expand a loop section once. It assigns the value to the
	 *	loop iterator, then expands.
	 */
	private void expandLoopSection(String ivar, Object ival, String loopsect) throws TplException {
		//-- Set the loop iterator thingo.
		if(ival == null)
			ival = "";
		m_ht.put(ivar, ival); // Put local variable

		//-- Now get a ExpReader for this loop section.
		expandPart(loopsect);
		m_ht.remove(ivar); // Remove var after loop
	}


	/**
	 *	Iterates thru all elements in an array.
	 */
	private boolean tryArrayLoop(Object o, String ivar, String loopsect) throws TplException {
		Class< ? > acl = o.getClass();
		if(!acl.isArray())
			return false; // Not an array-> begone

		//-- Start the iteration!
		Class< ? > cl = acl.getComponentType(); // Type of components.
		int len = Array.getLength(o); // Get #elements in table
		for(int ix = 0; ix < len; ix++) // Iterate,
		{
			//-- Get the nth element and assign to ivar,
			Object ival = null;
			if(cl.isPrimitive()) {
				if(cl == Integer.TYPE) {
					int i = Array.getInt(o, ix);
					ival = new Integer(i);
				} else if(cl == Long.TYPE) {
					ival = new Long(Array.getLong(o, ix));
				} else
					throw new TplException("Unsupported 'primitive' type in loop array");
			} else {
				ival = Array.get(o, ix);
			}

			expandLoopSection(ivar, ival, loopsect);
		}
		return true;
	}

	private boolean doesImpl(Class< ? > cl, String ifname) {
		Class< ? >[] ar = cl.getInterfaces();
		for(int i = 0; i < ar.length; i++) {
			Class< ? > ic = ar[i];
			if(ic.getName().equals(ifname))
				return true;
		}
		return false;
	}


	/**
	 *	If the object IS an iterator or an Enumeration then iterate or enumerate
	 *	thru the thingo.
	 */
	private boolean tryIsEnum(Object o, String ivar, String loopsect) throws TplException {
		Class< ? > acl = o.getClass();

		//		try
		//		{
		//			ecl	= Class.forName("java.util.Enumeration");
		//		}
		//		catch(ClassNotFoundException x)
		//		{
		//			throw new TplException("Cannot find java.util.Enumeration");
		//		}
		//		if(! acl.isInstance(ecl)) return false;
		if(!doesImpl(acl, "java.util.Enumeration"))
			return false;

		//-- Cold! We can!
		Enumeration< ? > e = (Enumeration< ? >) o;
		while(e.hasMoreElements()) {
			Object ival = e.nextElement();
			expandLoopSection(ivar, ival, loopsect);
		}
		return true;
	}


	private static Method findMethod(Class< ? > cl, String name, int nparm) {
		Method ar[] = cl.getMethods();
		for(int i = 0; i < ar.length; i++) {
			if(ar[i].getName().equals(name)) {
				if(ar[i].getParameterTypes().length == nparm)
					return ar[i];
			}
		}
		return null;
	}


	/**
	 *	If the object has a method that returns an enumeration call the method
	 *	then use the enumeration.
	 */
	private boolean tryHasEnum(Object o, String ivar, String loopsect) throws TplException {
		Class< ? > acl = o.getClass();

		Method m = findMethod(acl, "elements", 0);
		if(tryEnumMethod(o, ivar, loopsect, m))
			return true;
		m = findMethod(acl, "values", 0);
		return tryEnumMethod(o, ivar, loopsect, m);
	}

	private boolean tryEnumMethod(Object o, String ivar, String loopsect, Method m) throws TplException {
		if(m == null)
			return false;

		Enumeration< ? > e;
		try {
			e = (Enumeration< ? >) m.invoke(o, (Object[]) null);
		} catch(Exception x) {
			throw new TplException("Exception in method invocation " + m.getName() + ": " + x.toString());
		}
		while(e.hasMoreElements()) {
			Object ival = e.nextElement();
			expandLoopSection(ivar, ival, loopsect);
		}
		return true;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	If and ifnot expansion								*/
	/*--------------------------------------------------------------*/
	private void expandIf(ExpReader org_r, boolean isnot) throws TplException {
		//-- Try: get the expression after the if,
		//		System.out.println("if/ifnot: ->"+org_r.getValue()+"<-");
		Object o = evalExpr(org_r.getValue()); // Evaluate thing after if / ifnot,
		String ifsect;

		if(isnot)
			ifsect = findIfEnd(org_r, ExpReader.tvIfNot, ExpReader.tvEndIfNot);
		else
			ifsect = findIfEnd(org_r, ExpReader.tvIf, ExpReader.tvEndIf);

		//-- Now: is condition true or false?
		boolean cond = false;
		if(o != null) {
			if(o instanceof Boolean)
				cond = ((Boolean) o).booleanValue();
			else if(o instanceof Integer)
				cond = ((Integer) o).intValue() != 0;
			else
				cond = true;
		}
		if(isnot)
			cond = !cond;

		if(cond)
			expandPart(ifsect);

	}

	/**
	 *	Finds the next /if or /ifnot thing. It allows for nested ifs. It returns the
	 *	substring between the if and the /if so that this string can be expanded
	 *	if the condition is TRUE.
	 */
	private String findIfEnd(ExpReader org_r, int starttoken, int endtoken) throws TplException {
		//-- Get the current start of the string;
		int spos = org_r.getPos(); // Directly after the start tag.
		int epos;
		int depth = 0;

		for(;;) {
			epos = org_r.getPos(); // Save position just before next token.
			int tc = org_r.nextToken(); // Get next token!
			if(tc == ExpReader.tvEof)
				throw new TplException("loop: missing end (/) tag for if.");
			else if(tc == starttoken)
				depth++; // Nested loop found..
			else if(tc == endtoken) {
				if(depth == 0) // Depth 0: end of loop found,
				{
					//-- Return the substring between loop and /loop. The current position is left after the /loop
					return m_buf.substring(spos, epos);
				}
				depth--;
			}
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Dummy sets and the like.							*/
	/*--------------------------------------------------------------*/
	public void putGetter(String name, Object o) {
		m_ht.put(name, o);
	}

	/**
	 *	This method posts the "dummy getter" method for a given name. The
	 *	dummy getter posts a class that returns, for all names retrieved from
	 *	it, the empty string.
	 */
	public void putDummy(String name) {
		m_ht.put(name, TplDummy.m_dummy);
	}

}


class TplDummy {
	public String get(String name) {
		return "";
	}

	static protected TplDummy	m_dummy	= new TplDummy();
}
