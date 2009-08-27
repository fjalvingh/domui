package to.etc.server.servlet.parts;

import java.awt.*;
import java.io.*;
import java.util.*;

/**
 * Static utilities for within the taglibs.
 * 
 * Created on May 13, 2005
 * @author jal
 */
public class TagUtil {
	private TagUtil() {
	}

	/**
	 * This converts a Java string which is to be placed as an HTML
	 * tag attribute to a quoted form. It outputs the start and end
	 * quotes and will escape or convert any invalid character to
	 * the appropriate format.
	 * 
	 * To quote proper we assume we need double quotes. If the content
	 * contains those we switch to single quote; if the content contains
	 * single quotes and double quotes we use double quotes and make
	 * entities from the double ones.
	 * @param a
	 * @param ha
	 */
	static public void quoteHtmlAttribute(Appendable a, String ha) throws IOException {
		boolean hasdouble = false;
		boolean hassingle = false;
		int len = ha.length();
		StringBuffer sb = new StringBuffer(len + 5);
		for(int i = 0; i < len; i++) {
			char c = ha.charAt(i);
			switch(c){
				default:
					sb.append(c);
					break;

				case '\'':
					hassingle = true;
					if(hassingle && hasdouble) {
						quoteHtmlAttributeWithEntities(a, ha);
						return;
					}
					sb.append(c);
					break;

				case '\"':
					hasdouble = true;
					if(hassingle && hasdouble) {
						quoteHtmlAttributeWithEntities(a, ha);
						return;
					}
					sb.append(c);
					break;

				case '&':
					sb.append("&amp;");
					break;

				case '<':
					sb.append("&lt;");
					break;
				case '>':
					sb.append("&gt;");
					break;
			}
		}

		//-- Buffer complete: output it.
		char c = hasdouble ? '\'' : '"';
		a.append(c);
		a.append(sb);
		a.append(c);
	}

	static private void quoteHtmlAttributeWithEntities(Appendable a, String ha) throws IOException {
		int len = ha.length();
		a.append('"');
		for(int i = 0; i < len; i++) {
			char c = ha.charAt(i);
			switch(c){
				default:
					a.append(c);
					break;
				case '\"':
					a.append("&quote;");
					break;

				case '&':
					a.append("&amp;");
					break;

				case '<':
					a.append("&lt;");
					break;
				case '>':
					a.append("&gt;");
					break;
			}
		}
		a.append('"');
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Quicky bind evaluator...							*/
	/*--------------------------------------------------------------*/
	//	static public Object	calcBindValue(PageContext pc, String binding) throws Exception
	//	{
	//		//-- 1. Get the first thingy before the dot (the bean name) and resolve that thing,
	//		int pos = binding.indexOf('.');
	//		String	bname;
	//		if(pos == -1)
	//		{
	//			bname = binding;
	//			binding = null;
	//		}
	//		else
	//		{
	//			bname = binding.substring(0, pos);
	//			binding	= binding.substring(pos+1);
	//		}
	//		Object	bean = pc.getVariableResolver().resolveVariable(bname);
	//		if(bean == null)
	//			throw new JspTagException("The variable '"+bname+"' was not found in any context.");
	//		
	//		//-- Now resolve the rest.
	//		return BeanUtil.getSimpleProperty(bean, binding);
	//	}
	//	
	//	static public void	bindInput(NaviContext ctx, IMessageSink ms, String binding, String value) throws Exception
	//	{
	//		//-- 1. Get the first thingy before the dot (the bean name) and resolve that thing,
	//		int pos = binding.indexOf('.');
	//		String	bname;
	//		if(pos == -1)
	//		{
	//			bname = binding;
	//			binding = null;
	//		}
	//		else
	//		{
	//			bname = binding.substring(0, pos);
	//			binding	= binding.substring(pos+1);
	//		}
	//		Object	bean = ctx.resolveVariable(bname);
	//		if(bean == null)
	//			throw new JspTagException("The variable '"+bname+"' was not found in any context.");
	////		BeanUtil.setSProperty(bean, binding, value);
	//	}


	/**
	 *	Enter with a string; it returns the same string but replaces HTML
	 *  recognised characters with their &..; equivalent. This allows parts of
	 *  HTML to be rendered neatly.
	 */
	static public void xmlStringize(Appendable sb, CharSequence is) throws IOException {
		if(is == null) {
			sb.append("null");
			return;
		}
		for(int i = 0; i < is.length(); i++) {
			char c = is.charAt(i);
			switch(c){
				case '>':
					sb.append("&gt;");
					break;
				case '<':
					sb.append("&lt;");
					break;
				case '&':
					sb.append("&amp;");
					break;
				default:
					sb.append(c);
					break;
			}
		}
	}

	static public void writeAttr(Appendable out, String tag, String val) throws IOException {
		out.append(' ');
		out.append(tag);
		out.append('=');
		quoteHtmlAttribute(out, val);
	}

	/**
	 * Generates a hidden input field.
	 * @param a
	 * @param name
	 * @param val
	 * @throws IOException
	 */
	static public void genHidden(Appendable a, String name, String val) throws IOException {
		a.append("<input type=\"hidden\" name=");
		quoteHtmlAttribute(a, name);
		a.append(" value=");
		quoteHtmlAttribute(a, val);
		a.append(" />");
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Binding and stuff.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Returns TRUE if this is some kind of data binding, i.e. something
	 * bound to a bean. This is the case if the string contains a thingy
	 * with binding syntax, defined the same way as the JSF binding syntax
	 * #{ ... }, meaning a hash followed by a string enclosed in curly
	 * braces. This does NOT do a validity check on the contents of the
	 * string!
	 * <p>The current version does *not* allow for extra embedded stuff
	 * in the string, i.e. a value binding expression of "mama#{papa}" is
	 * not treated as a binding expression.
	 * 
	 * @param s
	 * @return
	 */
	static public final boolean isBinding(String s) {
		if(s == null)
			return false;
		s = s.trim();
		if(s.length() < 4) // Must be at least 3 chars plus something IN the braces.
			return false;
		if(s.charAt(0) != '#')
			return false;
		if(s.charAt(1) != '{')
			return false;
		if(s.charAt(s.length() - 1) != '}')
			return false;
		return true;
	}

	static public final boolean isElExpr(String s) {
		if(s == null)
			return false;
		s = s.trim();
		if(s.length() < 4) // Must be at least 3 chars plus something IN the braces.
			return false;
		if(s.charAt(0) != '$')
			return false;
		if(s.charAt(1) != '{')
			return false;
		if(s.charAt(s.length() - 1) != '}')
			return false;
		return true;
	}

	static public Color makeColor(String col) {
		if(col == null)
			return Color.WHITE;
		if(col.startsWith("#")) {
			try {
				int c = Integer.parseInt(col.substring(1), 16);
				return new Color(c);
			} catch(Exception x) {
				return Color.WHITE;
			}
		}
		Color c = m_colors.get(col.toLowerCase());
		if(c != null)
			return c;
		return Color.getColor(col, Color.WHITE);
	}

	static public int makeColorCode(String col) {
		if(col == null)
			return 0xffffff;
		Color c = m_colors.get(col.toLowerCase());
		if(c != null)
			return c.getRGB();
		if(col.startsWith("#"))
			col = col.substring(1);
		try {
			return (int) (Long.parseLong(col, 16) & 0xffffffff);
		} catch(Exception x) {
			return 0xffffff;
		}
	}

	static private Map<String, Color>	m_colors	= new Hashtable<String, Color>();

	static {
		m_colors.put("white", Color.WHITE);
		m_colors.put("black", Color.BLACK);
		m_colors.put("blue", Color.BLUE);
		m_colors.put("cyan", Color.CYAN);
		m_colors.put("darkgray", Color.DARK_GRAY);
		m_colors.put("gray", Color.GRAY);
		m_colors.put("green", Color.GREEN);
		m_colors.put("lightgray", Color.LIGHT_GRAY);
		m_colors.put("magenta", Color.MAGENTA);
		m_colors.put("orange", Color.ORANGE);
		m_colors.put("pink", Color.PINK);
		m_colors.put("red", Color.RED);
		m_colors.put("yellow", Color.YELLOW);
	}

	/**
	 * @author mbp
	 * Compare two objects and return true if they represent the same value.
	 * Used for the comparison in navi:switch / navi:case, where the values may
	 * be of different types yet represent the same value.
	 * When objects are of different type, we convert both to string and compare
	 * these ignoring case.
	 */
	static public boolean equals(Object a, Object b) {
		// if both null, declare equal
		if(a == null && b == null)
			return true;
		// if one null and the other not, declare unequal
		if(a == null || b == null)
			return false;

		// must compare. try to convert both to string representation, then compare that
		String sa = a.toString();
		String sb = b.toString();
		return sa.equalsIgnoreCase(sb);

	}
}
