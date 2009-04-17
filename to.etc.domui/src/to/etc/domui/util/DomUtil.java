package to.etc.domui.util;

import java.lang.reflect.*;
import java.sql.*;

import javax.annotation.*;

import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.domui.trouble.*;
import to.etc.util.*;
import to.etc.webapp.nls.*;

final public class DomUtil {
	static public final BundleRef	BUNDLE	= BundleRef.create(DomUtil.class, "messages");

	static private int		m_guidSeed;

	private DomUtil() {}

	static {
		long val = System.currentTimeMillis()/1000/60;
		m_guidSeed = (int)val;
	}

	static public final boolean	isEqual(final Object a, final Object b) {
		if(a == b)
			return true;
		if(a == null || b == null)
			return false;
		return a.equals(b);
	}

	static public final boolean isEqual(final Object... ar) {
		if(ar.length < 2)
			throw new IllegalStateException("Silly.");
		Object a = ar[0];
		for(int i = ar.length; --i >= 1;) {
			if(! isEqual(a, ar[i]))
				return false;
		}
		return true;
	}

	static public final Class<?>	findClass(@Nonnull final ClassLoader cl, @Nonnull final String name) {
		try {
			return cl.loadClass(name);
		} catch(Exception x) {
			return null;
		}
	}

	/**
	 * Retrieves a value from an object using introspection. The name is the direct
	 * name of a method that *must* exist; it does not add a "get". If the method
	 * does not exist this throws an exception.
	 *
	 * @param inst
	 * @param name
	 * @return
	 */
	static public final Object		getClassValue(@Nonnull final Object inst, @Nonnull final String name) throws Exception {
		if(inst == null)
			throw new IllegalStateException("The input object is null");
		Class<?>	clz = inst.getClass();
		Method m;
		try {
			m = clz.getMethod(name);
		} catch(NoSuchMethodException x) {
			throw new IllegalStateException("Unknown method '"+name+"()' on class="+clz);
		}
		try {
			return m.invoke(inst);
		} catch(IllegalAccessException iax) {
			throw new IllegalStateException("Cannot call method '"+name+"()' on class="+clz+": "+iax);
		} catch(InvocationTargetException itx) {
			Throwable c = itx.getCause();
			if(c instanceof Exception)
				throw (Exception) c;
			else if(c instanceof Error)
				throw (Error) c;
			else
				throw itx;
		}
	}

	/**
	 * Resolve the property's value
	 * @param base
	 * @param path
	 * @return
	 */
	static public Object	getPropertyValue(@Nonnull final Object base, @Nonnull final String path) {
		int	pos	= 0;
		int	len	= path.length();
		Object	next = base;
		while(pos < len) {
			if(next == null)
				return null;
			int npos = path.indexOf('.', pos);
			String	name;
			if(npos == -1) {
				name = path.substring(pos);
				pos	= len;
			} else {
				name = path.substring(pos, npos);
				pos = npos;
			}
			if(name.length() == 0)
				throw new IllegalStateException("Invalid property path: "+path);

			//-- Do a single-property resolve;
			next	= getSinglePropertyValue(next, name);
			if(pos < len) {
				//-- Next thingy must be a '.'
				if(path.charAt(pos) != '.')
					throw new IllegalStateException("Invalid property path: "+path);
				pos++;
			}
		}
		return next;
	}

	static private Object	getSinglePropertyValue(final Object base, final String name) {
		try {
			StringBuilder	sb	= new StringBuilder(name.length()+3);
			sb.append("get");
			if(Character.isUpperCase(name.charAt(0)))
				sb.append(name);
			else {
				sb.append(Character.toUpperCase(name.charAt(0)));
				sb.append(name,1 , name.length());
			}
			Method	m = base.getClass().getMethod(sb.toString());
			return m.invoke(base);
		} catch(NoSuchMethodException x) {
			throw new IllegalStateException("No property '"+name+"' on class="+base.getClass());
		} catch(Exception x) {
			Trouble.wrapException(x);
		}
		return null;
	}

	static public String	createRandomColor() {
		int	value = (int)(0xffffff * Math.random());
		return  "#"+StringTool.intToStr(value, 16, 6);
	}

	static public IErrorFence	getMessageFence(NodeBase start) {
		for(;;) {
			if(start == null)
				throw new IllegalStateException("Cannot locate error fence. Did you call an error routine on an unattached Node?");
			if(start instanceof NodeContainer) {
				NodeContainer nc = (NodeContainer) start;
				if(nc.getErrorFence() != null)
					return nc.getErrorFence();
			}
//			if(start.getParent() == null) {
//				return start.getPage().getErrorFence();	// Use the generic page's fence.
//			}
			start = start.getParent();
		}
	}

	static private final char[]	BASE64MAP = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz$_".toCharArray();

	/**
	 * Generate an unique identifier with reasonable expectations that it will be globally unique. This
	 * does not use the known GUID format but shortens the string by encoding into base64-like encoding.
	 * @return
	 */
	static public String	generateGUID() {
		byte[]	bin	= new byte[18];
		ByteArrayUtil.setInt(bin, 0, m_guidSeed);			// Start with the seed
		ByteArrayUtil.setShort(bin, 4, (short)(Math.random()*65536));
		long v = System.currentTimeMillis() / 1000 - (m_guidSeed*60);
		ByteArrayUtil.setInt(bin, 6, (int)v);
		ByteArrayUtil.setLong(bin, 10, System.nanoTime());

//		ByteArrayUtil.setLong(bin, 6, System.currentTimeMillis());
//		System.out.print(StringTool.toHex(bin)+"   ");

		StringBuilder	sb	= new StringBuilder((bin.length+2)/3*4);

		//-- 3-byte to 4-byte conversion + 0-63 to ascii printable conversion
		int	sidx;
		for(sidx=0; sidx < bin.length-2; sidx += 3) {
			sb.append( BASE64MAP[(bin[sidx] >>> 2) & 0x3f] );
			sb.append( BASE64MAP[(bin[sidx+1] >>> 4) & 0xf | (bin[sidx] << 4) & 0x3f] );
			sb.append( BASE64MAP[(bin[sidx+2] >>> 6) & 0x3 | (bin[sidx+1] << 2) & 0x3f] );
			sb.append( BASE64MAP[bin[sidx+2] & 0x3f] );
		}
		if(sidx < bin.length) {
			sb.append( BASE64MAP[(bin[sidx] >>> 2) & 077] );
			if (sidx < bin.length-1) {
				sb.append( BASE64MAP[(bin[sidx+1] >>> 4) & 017 | (bin[sidx] << 4) & 077] );
				sb.append( BASE64MAP[(bin[sidx+1] << 2) & 077] );
			} else
				sb.append( BASE64MAP[(bin[sidx] << 4) & 077] );
		}
		return sb.toString();
	}

	static public void	addUrlParameters(final StringBuilder sb, final RequestContext ctx, boolean first) {
		for(String name: ctx.getParameterNames()) {
			if(name.equals(Constants.PARAM_CONVERSATION_ID))
				continue;
			for(String value: ctx.getParameters(name)) {
				if(first) {
					sb.append('?');
					first = false;
				} else
					sb.append('&');
				StringTool.encodeURLEncoded(sb, name);
				sb.append('=');
				StringTool.encodeURLEncoded(sb, value);
			}
		}
	}
	static public void	addUrlParameters(final StringBuilder sb, final PageParameters ctx, boolean first) {
		if(ctx == null)
			return;
		for(String name: ctx.getParameterNames()) {
			if(name.equals(Constants.PARAM_CONVERSATION_ID))
				continue;
			String	value = ctx.getString(name);
			if(first) {
				sb.append('?');
				first = false;
			} else
				sb.append('&');
			StringTool.encodeURLEncoded(sb, name);
			sb.append('=');
			StringTool.encodeURLEncoded(sb, value);
		}
	}

	static public String[]	decodeCID(final String param) {
		if(param == null)
			return null;
		int	pos = param.indexOf('.');
		if(pos == -1)
			throw new IllegalStateException("Missing '.' in $CID parameter");
		String[]	res = new String[] {
			param.substring(0, pos)
		,	param.substring(pos+1)
		};
		return res;
	}

	/**
	 * Ensures that all of a node tree has been built.
	 * @param p
	 */
	static public void		buildTree(final NodeBase p) throws Exception {
		p.build();
		if(p instanceof NodeContainer) {
			NodeContainer nc = (NodeContainer)p;
			for(NodeBase c: nc)
				buildTree(c);
		}
	}

	/**
	 * Walks the tree starting at the node passed and returns the first instance of the given class
	 * that is found in a normal walk of the tree.
	 * @param <T>
	 * @param p
	 * @param clz
	 * @return
	 * @throws Exception
	 */
	static public <T extends NodeBase> T	findComponentInTree(final NodeBase p, final Class<T> clz) throws Exception {
		if(clz.isAssignableFrom(p.getClass()))
			return (T) p;
		p.build();
		if(p instanceof NodeContainer) {
			NodeContainer nc = (NodeContainer)p;
			for(NodeBase c: nc) {
				T res = findComponentInTree(c, clz);
				if(res != null)
					return res;
			}
		}
		return null;
	}

	static public String	nlsLabel(final String label) {
		if(label == null)
			return label;
		if(label.charAt(0) != '~')
			return label;
		if(label.startsWith("~~"))
			return label.substring(1);

		//-- Lookup as a resource.
		return "???"+label.substring(1)+"???";
	}

	/**
	 * Walks the entire table and adjusts it's colspans.
	 * @param t
	 */
	static public void		adjustTableColspans(final Table table) {
		//-- Count the max. row length (max #cells in a row)
		int maxcol = 0;
		for(NodeBase b: table) {		// For all TBody's
			if(b instanceof TBody) {
				TBody tb = (TBody)b;
				for(NodeBase b2: tb) {			// For all TR's
					if(b2 instanceof TR) {
						TR tr = (TR) b2;
						int count = 0;
						for(NodeBase b3: tr) {
							if(b3 instanceof TD) {
								TD td = (TD) b3;
								count += td.getColspan() > 0 ? td.getColspan() : 1;
							}
						}
						if(count > maxcol)
							maxcol = count;
					}
				}
			}
		}

		/*
		 * Adjust all rows that have less cells than the maximum by specifying a colspan on every last cell.
		 */
		for(NodeBase b: table) {		// For all TBody's
			if(b instanceof TBody) {
				TBody tb = (TBody)b;
				for(NodeBase b2: tb) {			// For all TR's
					if(b2 instanceof TR) {
						TR tr = (TR) b2;
						int count = 0;
						for(NodeBase b3: tr) {
							if(b3 instanceof TD) {
								TD td = (TD) b3;
								count += td.getColspan() > 0 ? td.getColspan() : 1;
							}
						}

						if(count < maxcol) {
							//-- Find last TD
							TD	td	= (TD) tr.getChild(tr.getChildCount()-1);
							td.setColspan(maxcol - count+1);			// Adjust colspan
						}
					}
				}
			}
		}
	}

	/**
	 * Remove all HTML tags from the input and keep only the text content. Things like script tags and the like
	 * will be removed but their contents will be kept.
	 * @param sb
	 * @param in
	 */
	static public void	stripHtml(final StringBuilder sb, final String in) {
		HtmlScanner	hs	= new HtmlScanner();
		int	lpos	= 0;
		hs.setDocument(in);
		for(;;) {
			String tag = hs.nextTag();						// Find the next tag.
			if(tag == null)
				break;

			//-- Append any text segment between the last tag and the current one,
			int len = hs.getPos() - lpos;
			if(len > 0)
				sb.append(in, lpos, hs.getPos());			// Append the normal text fragment

			//-- Skip this tag;
			hs.skipTag();
			lpos	= hs.getPos();							// Position just after the >
		}
		if(hs.getPos() < in.length())
			sb.append(in, hs.getPos(), in.length());
	}

	static public void		dumpException(final Exception x) {
        x.printStackTrace();

        Throwable next = null;
        for(Throwable curr = x; curr != null; curr= next) {
        	next = curr.getCause();
        	if(next == curr)
        		next = null;

        	if(curr instanceof SQLException) {
        		SQLException sx = (SQLException) curr;
        		while(sx.getNextException() != null) {
        			sx	= sx.getNextException();
        			System.err.println("SQL NextException: "+sx);
        		}
        	}
        }
	}

	static public String	getJavaResourceRURL(final Class<?> resourceBase, final String name) {
		String rb = resourceBase.getName();
		int pos = rb.lastIndexOf('.');
		if(pos == -1)
			throw new IllegalStateException("??");
		return Constants.RESOURCE_PREFIX+rb.substring(0, pos+1).replace('.', '/')+name;
	}

	public static void main(final String[] args) {
		for(int i = 0; i < 10; i++)
			System.out.println(generateGUID());
	}
}
