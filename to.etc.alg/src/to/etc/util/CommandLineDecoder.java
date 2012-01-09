package to.etc.util;

import java.io.*;
import java.lang.reflect.*;
import java.text.*;
import java.util.*;


/**
 * <p>This static class is a convenience class to create a command line decoder. It
 * is special in that it uses Reflection to determine what parameters are valid
 * for a command line.
 * <p>The main entrypoint, decode(), gets passed a command line and a base class
 * implementing iCommandLineHandler. This interface contains some methods that
 * are always needed, such as usage() etc.
 * The other methods in the object determine what parameters are allowed. When
 * an argument is encountered the method cmdArgument(String) is looked up in the
 * object and gets called with the value.
 *
 * When an argument is found the system will lookup a method with the argument's
 * name preceded by cmd. This returns a list of methods. The arguments of each
 * method are then used to parse the rest of the command line. The method
 * whose arguments match gets called.
 *
 * <p>Title: Mumble Global Libraries - Non-database tools</p>
 * <p>Description: Small tools for Java programs</p>
 * <p>Copyright: Copyright (c) 2002 Frits Jalvingh; released under the LGPL licence.</p>
 * <p>Website <a href="http://www.mumble.to/">Mumble</a></p>
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 * @version 1.0
 */
public class CommandLineDecoder {
	private String[]			m_args;

	private int					m_arg;

	private iCommandLineHandler	m_ich;

	private Class				m_cl;

	private CommandLineDecoder(String[] args, iCommandLineHandler ich) {
		m_args = args;
		m_arg = 0;
		m_ich = ich;
		m_cl = ich.getClass();
	}

	static public int decode(String[] args, iCommandLineHandler ich) throws Exception {
		CommandLineDecoder cld = new CommandLineDecoder(args, ich);
		return cld.run();
	}


	/**
	 * Initializes the class by getting the command handler (if found) and by
	 * getting all methods with the cmd prefix in the list.
	 */
	private void init() {
		//-- Can we find a command handler?
		m_argument_m = findMethod("cmdArgument", new Class[]{String.class});

		Vector v = new Vector();
		Method[] mar = m_ich.getClass().getMethods();
		for(int i = 0; i < mar.length; i++) {
			Method m = mar[i];
			if(Modifier.isPublic(m.getModifiers())) {
				String name = m.getName();
				if(name.startsWith("cmdi") || name.startsWith("cmdc")) {
					//-- Is a command handler. Must return void or int,
					Class cl = m.getReturnType();
					//					Class	vcl	= java.lang.Void.class;
					//					Class	v2cl= java.lang.Void.TYPE;


					if(cl == java.lang.Void.TYPE || cl == Integer.TYPE) {
						//-- Valid!
						v.add(m);
					}
				}
			}
		}

		//-- Now make the handler array.
		m_cmd_ar = (Method[]) v.toArray(new Method[v.size()]);
	}


	/**
	 * Scans the methods for those that can handle the parameter passed.
	 * @param name	the parameter name ex the -
	 * @return		the methods that can handle these, or null if none.
	 */
	private Method[] findParamMethods(String name) {
		String rname = "cmdc" + name;
		String iname = "cmdi" + name;

		Vector v = null;
		for(int i = 0; i < m_cmd_ar.length; i++) {
			if(rname.equals(m_cmd_ar[i].getName()) || iname.equalsIgnoreCase(m_cmd_ar[i].getName())) {
				if(v == null)
					v = new Vector();
				v.add(m_cmd_ar[i]);
			}
		}

		if(v == null)
			return null;

		return (Method[]) v.toArray(new Method[v.size()]);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Start of decoder....								*/
	/*--------------------------------------------------------------*/
	/** The argument handler to use. */
	private Method		m_argument_m;

	/** The list of command methods that were found in the handler class.. */
	private Method[]	m_cmd_ar;


	private int callArgument(String arg) throws Exception {
		if(m_argument_m == null)
			throw new CommandLineException(arg, "No arguments expected");

		try {
			Object res = m_argument_m.invoke(m_ich, new Object[]{arg});
			if(res instanceof Integer)
				return ((Integer) res).intValue();
			return 0;
		} catch(InvocationTargetException tx) {
			Throwable ox = tx.getTargetException();
			if(ox instanceof Exception)
				throw (Exception) ox;
			throw tx;
		}
	}


	private int callInvoke(Method m, Object[] args) throws Exception {
		try {
			Object res = m.invoke(m_ich, args);
			if(res instanceof Integer)
				return ((Integer) res).intValue();
			return 0;
		} catch(InvocationTargetException tx) {
			Throwable ox = tx.getTargetException();
			if(ox instanceof Exception)
				throw (Exception) ox;
			throw tx;
		}
	}


	/**
	 * Returns the first entry in the list that is another option, starting
	 * at the current option.
	 * @param start		the index to start checking
	 * @return			the index of the next option
	 */
	private int findEndIndex(int start) {
		for(;;) {
			if(start >= m_args.length)
				return start;
			String name = m_args[start];
			if(name.length() > 0 && name.charAt(0) == '-')
				return start;
			start++;
		}
	}


	private int run() throws Exception {
		init();
		while(m_arg < m_args.length) {
			String part = m_args[m_arg++];
			if(part.length() == 0 || part.charAt(0) != '-') {
				//-- Not an option. Call argument handler if appropriate;
				int rc = callArgument(part);
				if(rc != 0)
					return rc;
			} else {
				//-- Get [a list of] methods that are appropriate,
				Method[] mar = findParamMethods(part.substring(1));
				if(mar == null)
					throw new CommandLineException(part, "Unknown option.");

				//-- Option is defined- we need to decode which one will fit!
				int eix = findEndIndex(m_arg);

				int rc = handleOptions(part, mar, eix);
				if(rc != 0)
					return rc;
			}
		}

		return 0;
	}


	private int handleOptions(String name, Method[] mar, int lastix) throws Exception {
		//-- Handle all alternatives and use the most explicit one.
		//-- Now loop: find the most explicit option; try it; if not remove and loop.
		for(;;) {
			Method m = findMostExplicit(mar); // Find most explicit one,
			if(m == null)
				throw new CommandLineException(name, "Invalid arguments for this option");

			//-- Try: can we provide all arguments for the method?
			if(canInvoke(name, m, lastix)) // Can we invoke?
				return m_invoke_rc; // Yes-> return code,
		}
	}


	/**
	 * Finds the most explicit (having the most arguments) method and remove
	 * it from the array. If nothing can be found it will return null.
	 * @param mar
	 * @return
	 */
	private Method findMostExplicit(Method[] mar) {
		int topscore = -1;
		int topix = -1;

		for(int i = 0; i < mar.length; i++) {
			if(mar[i] != null) {
				//-- What score here?
				Class[] par = mar[i].getParameterTypes();
				int score = par.length;

				for(int j = 0; j < par.length; j++) {
					if(par[j].isArray())
						score += 10000;
				}

				//-- Okay.
				if(score > topscore) {
					topix = i;
					topscore = score;
				}
			}
		}

		if(topix < 0)
			return null;

		Method m = mar[topix]; // Get topscore thing
		mar[topix] = null;
		return m;
	}


	private int	m_invoke_rc;

	private int	m_parmix;

	private int	m_parmend;

	private boolean canInvoke(String name, Method m, int lastix) throws Exception {
		//-- Try to create a valid invocation array for this handler.
		int maxargs = lastix - m_arg; // Max. #of args available to the option;
		Class[] par = m.getParameterTypes();
		if(par.length > maxargs)
			return false; // Not enough arguments.

		//-- Quickly handle the case where 0 args are needed- always allow
		if(par.length == 0) {
			m_invoke_rc = callInvoke(m, null);
			return true;
		}

		//-- The thing has arguments....
		Object[] var = new Object[par.length];
		StringBuffer sb = new StringBuffer();

		//-- Handle each parameter type in turn and collect problems...
		m_parmix = m_arg;
		m_parmend = lastix;
		for(int i = 0; i < par.length; i++) {
			Object o = getParameter(sb, par[i]);
			if(o == null)
				return false; // Invalid argument.
			var[i] = o; // Save stored object..
		}

		//-- All parameters were matched! Invoke and exit!
		m_arg = m_parmix;
		m_invoke_rc = callInvoke(m, var);
		return true;
	}


	private Object getParameter(StringBuffer sb, Class ty) throws Exception {
		if(m_parmix >= m_parmend)
			return null; // Exhausted

		//-- Try to get a parameter matching the type...
		String parm = m_args[m_parmix++];

		if(ty == Integer.class) {
			try {
				int res = Integer.parseInt(parm);
				return new Integer(res);
			} catch(Exception x) {
				sb.append(parm + " is not a valid integer");
				return null;
			}
		} else if(ty == String.class) {
			return parm; // Literal string.
		} else if(ty == Boolean.class) {
			if(parm.equalsIgnoreCase("yes") || parm.equalsIgnoreCase("on") || parm.equalsIgnoreCase("true"))
				return new Boolean(true);

			if(parm.equalsIgnoreCase("no") || parm.equalsIgnoreCase("off") || parm.equalsIgnoreCase("false"))
				return new Boolean(false);

			sb.append(parm + " is not a boolean; need yes/no, on/off or true/false");
			return null;
		} else if(ty == File.class) {
			File f = new File(parm);
			return f;
		} else if(ty == Date.class) {
			//-- Try to parse a date string...
			DateFormat df = DateFormat.getDateTimeInstance();
			try {
				Date dt = df.parse(parm);
				return dt;
			} catch(Exception x) {
				sb.append(parm + " is not a valid date/time.");
				return null;
			}
		}

		sb.append("Unknown parameter type " + ty.getName());
		return null;
	}


	/**
	 * This tries to find the named method with the specified parameters in the
	 * @param name
	 * @param args
	 * @return
	 */
	private Method findMethod(String name, Class[] args) {
		try {
			Method m = m_cl.getMethod(name, args);
			return m;
		} catch(Exception ex) {}
		return null;
	}


}
