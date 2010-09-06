package to.etc.domui.util;

import java.io.*;
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

import javax.annotation.*;
import javax.servlet.http.*;

import to.etc.domui.annotations.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.domui.trouble.*;
import to.etc.util.*;
import to.etc.webapp.*;
import to.etc.webapp.nls.*;

final public class DomUtil {
	static private int m_guidSeed;

	private DomUtil() {}

	static {
		long val = System.currentTimeMillis() / 1000 / 60;
		m_guidSeed = (int) val;
	}

	static public final void ie8Capable(HttpServletResponse req) throws IOException {
		if(!(req instanceof WrappedHttpServetResponse))
			return;
		WrappedHttpServetResponse wsr = (WrappedHttpServetResponse) req;
		wsr.setIE8Capable();
	}

	static public final boolean isEqual(final Object a, final Object b) {
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
			if(!isEqual(a, ar[i]))
				return false;
		}
		return true;
	}

	static public <T> T getValueSafe(IInputNode<T> node) {
		try {
			return node.getValue();
		} catch(ValidationException x) {
			return null;
		}
	}

	/**
	 * Returns T if the given Java Resource exists.
	 * @param clz
	 * @param name
	 * @return
	 */
	static public boolean classResourceExists(final Class< ? extends DomApplication> clz, final String name) {
		InputStream is = clz.getResourceAsStream(name);
		if(is == null)
			return false;
		try {
			is.close();
		} catch(Exception x) {
			// IGNORE
		}
		return true;
	}

	static public final Class< ? > findClass(@Nonnull final ClassLoader cl, @Nonnull final String name) {
		try {
			return cl.loadClass(name);
		} catch(Exception x) {
			return null;
		}
	}

	/**
	 * Returns T if the class represents an integer numeric type.
	 * @param clz
	 * @return
	 */
	static public boolean isIntegerType(Class< ? > clz) {
		return clz == int.class || clz == Integer.class || clz == long.class || clz == Long.class || clz == Short.class || clz == short.class;
	}

	static public boolean isDoubleOrWrapper(Class< ? > clz) {
		return clz == Double.class || clz == double.class;
	}

	static public boolean isFloatOrWrapper(Class< ? > clz) {
		return clz == Float.class || clz == float.class;
	}

	static public boolean isIntegerOrWrapper(Class< ? > clz) {
		return clz == Integer.class || clz == int.class;
	}

	static public boolean isShortOrWrapper(Class< ? > clz) {
		return clz == Short.class || clz == short.class;
	}

	static public boolean isLongOrWrapper(Class< ? > clz) {
		return clz == Long.class || clz == long.class;
	}

	/**
	 * Return T if the class represents a real (double or float) type.
	 * @param clz
	 * @return
	 */
	static public boolean isRealType(Class< ? > clz) {
		return clz == float.class || clz == Float.class || clz == Double.class || clz == double.class;
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
	static public final Object getClassValue(@Nonnull final Object inst, @Nonnull final String name) throws Exception {
		if(inst == null)
			throw new IllegalStateException("The input object is null");
		Class< ? > clz = inst.getClass();
		Method m;
		try {
			m = clz.getMethod(name);
		} catch(NoSuchMethodException x) {
			throw new IllegalStateException("Unknown method '" + name + "()' on class=" + clz);
		}
		try {
			return m.invoke(inst);
		} catch(IllegalAccessException iax) {
			throw new IllegalStateException("Cannot call method '" + name + "()' on class=" + clz + ": " + iax);
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
	static public Object getPropertyValue(@Nonnull final Object base, @Nonnull final String path) {
		int pos = 0;
		int len = path.length();
		Object next = base;
		while(pos < len) {
			if(next == null)
				return null;
			int npos = path.indexOf('.', pos);
			String name;
			if(npos == -1) {
				name = path.substring(pos);
				pos = len;
			} else {
				name = path.substring(pos, npos);
				pos = npos;
			}
			if(name.length() == 0)
				throw new IllegalStateException("Invalid property path: " + path);

			//-- Do a single-property resolve;
			next = getSinglePropertyValue(next, name);
			if(pos < len) {
				//-- Next thingy must be a '.'
				if(path.charAt(pos) != '.')
					throw new IllegalStateException("Invalid property path: " + path);
				pos++;
			}
		}
		return next;
	}

	static private Object getSinglePropertyValue(final Object base, final String name) {
		try {
			StringBuilder sb = new StringBuilder(name.length() + 3);
			sb.append("get");
			if(Character.isUpperCase(name.charAt(0)))
				sb.append(name);
			else {
				sb.append(Character.toUpperCase(name.charAt(0)));
				sb.append(name, 1, name.length());
			}
			Method m = base.getClass().getMethod(sb.toString());
			return m.invoke(base);
		} catch(NoSuchMethodException x) {
			throw new IllegalStateException("No property '" + name + "' on class=" + base.getClass());
		} catch(Exception x) {
			Trouble.wrapException(x);
		}
		return null;
	}

	static public String createRandomColor() {
		int value = (int) (0xffffff * Math.random());
		return "#" + StringTool.intToStr(value, 16, 6);
	}

	static public IErrorFence getMessageFence(NodeBase start) {
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

	static private final char[] BASE64MAP = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz$_".toCharArray();

	/**
	 * Generate an unique identifier with reasonable expectations that it will be globally unique. This
	 * does not use the known GUID format but shortens the string by encoding into base64-like encoding.
	 * @return
	 */
	static public String generateGUID() {
		byte[] bin = new byte[18];
		ByteArrayUtil.setInt(bin, 0, m_guidSeed); // Start with the seed
		ByteArrayUtil.setShort(bin, 4, (short) (Math.random() * 65536));
		long v = System.currentTimeMillis() / 1000 - (m_guidSeed * 60);
		ByteArrayUtil.setInt(bin, 6, (int) v);
		ByteArrayUtil.setLong(bin, 10, System.nanoTime());

		//		ByteArrayUtil.setLong(bin, 6, System.currentTimeMillis());
		//		System.out.print(StringTool.toHex(bin)+"   ");

		StringBuilder sb = new StringBuilder((bin.length + 2) / 3 * 4);

		//-- 3-byte to 4-byte conversion + 0-63 to ascii printable conversion
		int sidx;
		for(sidx = 0; sidx < bin.length - 2; sidx += 3) {
			sb.append(BASE64MAP[(bin[sidx] >>> 2) & 0x3f]);
			sb.append(BASE64MAP[(bin[sidx + 1] >>> 4) & 0xf | (bin[sidx] << 4) & 0x3f]);
			sb.append(BASE64MAP[(bin[sidx + 2] >>> 6) & 0x3 | (bin[sidx + 1] << 2) & 0x3f]);
			sb.append(BASE64MAP[bin[sidx + 2] & 0x3f]);
		}
		if(sidx < bin.length) {
			sb.append(BASE64MAP[(bin[sidx] >>> 2) & 077]);
			if(sidx < bin.length - 1) {
				sb.append(BASE64MAP[(bin[sidx + 1] >>> 4) & 017 | (bin[sidx] << 4) & 077]);
				sb.append(BASE64MAP[(bin[sidx + 1] << 2) & 077]);
			} else
				sb.append(BASE64MAP[(bin[sidx] << 4) & 077]);
		}
		return sb.toString();
	}

	static public void addUrlParameters(final StringBuilder sb, final IRequestContext ctx, boolean first) {
		for(String name : ctx.getParameterNames()) {
			if(name.equals(Constants.PARAM_CONVERSATION_ID))
				continue;
			for(String value : ctx.getParameters(name)) {
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

	static public void addUrlParameters(final StringBuilder sb, final PageParameters ctx, boolean first) {
		if(ctx == null)
			return;
		for(String name : ctx.getParameterNames()) {
			if(name.equals(Constants.PARAM_CONVERSATION_ID))
				continue;
			String value = ctx.getString(name);
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

	/**
	 *
	 * @param clz
	 * @param pp
	 * @return
	 */
	static public String createPageURL(Class< ? extends UrlPage> clz, PageParameters pp) {
		StringBuilder sb = new StringBuilder();
		sb.append(DomApplication.get().getApplicationURL());
		sb.append(clz.getName());
		sb.append('.');
		sb.append(DomApplication.get().getUrlExtension());
		addUrlParameters(sb, pp, true);
		return sb.toString();
	}

	/**
	 * Generate an URL to some page with parameters.
	 *
	 * @param rurl	The absolute or relative URL to whatever resource.
	 * @param pageParameters
	 * @return
	 */
	public static String createPageURL(String rurl, PageParameters pageParameters) {
		StringBuilder sb = new StringBuilder();
		if(DomUtil.isRelativeURL(rurl)) {
			RequestContextImpl ctx = (RequestContextImpl) PageContext.getRequestContext();
			sb.append(ctx.getRelativePath(rurl));
		}
		else
			sb.append(rurl);
		if(pageParameters != null) {
			addUrlParameters(sb, pageParameters, true);
		}
		return sb.toString();
	}

	/**
	 * Calculate a full URL from a rurl. If the rurl starts with a scheme it is returned verbatim;
	 * if it starts with slash (host-relative path absolute) it is returned verbatim; in all other
	 * cases it is returned with the webapp context appended. Examples:
	 * <ul>
	 *	<li>img/text.gif becomes /Itris_VO02/img/text.gif</li>
	 *	<li>/ui/generic.gif remains the same</li>
	 * </ul>
	 * @param ci
	 * @param rurl
	 * @return
	 */
	static public String calculateURL(IRequestContext ci, String rurl) {
		int pos = rurl.indexOf(":/"); // http://?
		if(pos > 0 && pos < 20)
			return rurl;
		if(rurl.startsWith("/"))
			return rurl;

		//-- Append context.
		return ci.getRelativePath(rurl);
	}

	static public String[] decodeCID(final String param) {
		if(param == null)
			return null;
		int pos = param.indexOf('.');
		if(pos == -1)
			throw new IllegalStateException("Missing '.' in $CID parameter");
		String[] res = new String[]{param.substring(0, pos), param.substring(pos + 1)};
		return res;
	}

	/**
	 * Ensures that all of a node tree has been built.
	 * @param p
	 */
	static public void buildTree(final NodeBase p) throws Exception {
		p.build();
		if(p instanceof NodeContainer) {
			NodeContainer nc = (NodeContainer) p;
			for(NodeBase c : nc)
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
	static public <T extends NodeBase> T findComponentInTree(final NodeBase p, final Class<T> clz) throws Exception {
		if(clz.isAssignableFrom(p.getClass()))
			return (T) p;
		p.build();
		if(p instanceof NodeContainer) {
			NodeContainer nc = (NodeContainer) p;
			for(NodeBase c : nc) {
				T res = findComponentInTree(c, clz);
				if(res != null)
					return res;
			}
		}
		return null;
	}

	static public String nlsLabel(final String label) {
		if(label == null)
			return label;
		if(label.charAt(0) != '~')
			return label;
		if(label.startsWith("~~"))
			return label.substring(1);

		//-- Lookup as a resource.
		return "???" + label.substring(1) + "???";
	}

	/**
	 * Walks the entire table and adjusts it's colspans.
	 * @param t
	 */
	static public void adjustTableColspans(final Table table) {
		//-- Count the max. row length (max #cells in a row)
		int maxcol = 0;
		for(NodeBase b : table) { // For all TBody's
			if(b instanceof TBody) {
				TBody tb = (TBody) b;
				for(NodeBase b2 : tb) { // For all TR's
					if(b2 instanceof TR) {
						TR tr = (TR) b2;
						int count = 0;
						for(NodeBase b3 : tr) {
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
		for(NodeBase b : table) { // For all TBody's
			if(b instanceof TBody) {
				TBody tb = (TBody) b;
				for(NodeBase b2 : tb) { // For all TR's
					if(b2 instanceof TR) {
						TR tr = (TR) b2;
						int count = 0;
						for(NodeBase b3 : tr) {
							if(b3 instanceof TD) {
								TD td = (TD) b3;
								count += td.getColspan() > 0 ? td.getColspan() : 1;
							}
						}

						if(count < maxcol) {
							//-- Find last TD
							TD td = (TD) tr.getChild(tr.getChildCount() - 1);
							td.setColspan(maxcol - count + 1); // Adjust colspan
						}
					}
				}
			}
		}
	}


	/**
	 * This balances tables to ensure that all rows have an equal number of rows and
	 * columns, taking rowspans and colspans into effect.
	 * FIXME Boring, lotso work, complete later.
	 * @param t
	 */
	@SuppressWarnings("unused")
	public static void balanceTable(Table t) {
		List<List<TD>> matrix = new ArrayList<List<TD>>(40);

		//-- Phase 1: start marking extends in the matrix.
		int rowindex = 0;
		int maxcols = 0;
		for(NodeBase l0 : t) { // Expecting THead and TBodies here.
			if(l0 instanceof THead || l0 instanceof TBody) {
				//-- Walk all rows.
				for(NodeBase trb : ((NodeContainer) l0)) {
					if(!(trb instanceof TR))
						throw new IllegalStateException("Unexpected child of type " + l0 + " in TBody/THead node (expecting TR)");
					TR tr = (TR) trb;
					int minrowspan = 1;

					//-- Start traversing the TD's.
					List<TD> baserowlist = getTdList(matrix, rowindex);
					int colindex = 0;
					for(NodeBase tdb : tr) {
						if(!(tdb instanceof TD))
							throw new IllegalStateException("Unexpected child of type " + tr + " in TBody/THead node (expecting TD)");
						TD td = (TD) tdb;

						int colspan = td.getColspan();
						int rowspan = td.getRowspan();
						if(colspan < 1)
							colspan = 1;
						if(rowspan < 1)
							rowspan = 1;



					}
					rowindex += minrowspan;
				}
			} else
				throw new IllegalStateException("Unexpected child of type " + l0 + " in TABLE node");
		}

		//-- Phase 2: for all cells, handle their row/colspan by recounting their spread
	}

	static private List<TD> getTdList(List<List<TD>> matrix, int row) {
		while(matrix.size() <= row) {
			matrix.add(new ArrayList<TD>());
		}
		return matrix.get(row);
	}

	/**
	 * Remove all HTML tags from the input and keep only the text content. Things like script tags and the like
	 * will be removed but their contents will be kept.
	 * @param sb
	 * @param in
	 */
	static public void stripHtml(final StringBuilder sb, final String in) {
		HtmlScanner hs = new HtmlScanner();
		int lpos = 0;
		hs.setDocument(in);
		for(;;) {
			String tag = hs.nextTag(); // Find the next tag.
			if(tag == null)
				break;

			//-- Append any text segment between the last tag and the current one,
			int len = hs.getPos() - lpos;
			if(len > 0)
				sb.append(in, lpos, hs.getPos()); // Append the normal text fragment

			//-- Skip this tag;
			hs.skipTag();
			lpos = hs.getPos(); // Position just after the >
		}
		if(hs.getPos() < in.length())
			sb.append(in, hs.getPos(), in.length());
	}

	static public void dumpException(final Exception x) {
		x.printStackTrace();

		Throwable next = null;
		for(Throwable curr = x; curr != null; curr = next) {
			next = curr.getCause();
			if(next == curr)
				next = null;

			if(curr instanceof SQLException) {
				SQLException sx = (SQLException) curr;
				while(sx.getNextException() != null) {
					sx = sx.getNextException();
					System.err.println("SQL NextException: " + sx);
				}
			}
		}
	}

	static public String getJavaResourceRURL(final Class< ? > resourceBase, final String name) {
		String rb = resourceBase.getName();
		int pos = rb.lastIndexOf('.');
		if(pos == -1)
			throw new IllegalStateException("??");
		return Constants.RESOURCE_PREFIX + rb.substring(0, pos + 1).replace('.', '/') + name;
	}

	public static void main(final String[] args) {
		for(int i = 0; i < 10; i++)
			System.out.println(generateGUID());
	}

	/**
	 * Returns T if the specified resource exists.
	 * @param clz
	 * @param cn
	 * @return
	 */
	public static boolean hasResource(final Class< ? extends UrlPage> clz, final String cn) {
		InputStream is = null;
		try {
			is = clz.getResourceAsStream(cn);
			return is != null;
		} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception x) {}
		}
	}

	static public String getClassNameOnly(final Class< ? > clz) {
		String cn = clz.getName();
		return cn.substring(cn.lastIndexOf('.') + 1);
	}

	/**
	 *
	 * @param ma
	 * @param clz
	 * @return
	 */
	static public BundleRef findBundle(final UIMenu ma, final Class< ? > clz) {
		if(ma != null && ma.bundleBase() != Object.class) { // Bundle base class specified?
			String s = ma.bundleName();
			if(s.length() == 0) // Do we have a name?
				s = "messages"; // If not use messages in this package
			return BundleRef.create(ma.bundleBase(), s);
		}

		//-- No BundleBase- use class as resource base and look for 'classname' as the properties base.
		if(clz != null) {
			String s = clz.getName();
			s = s.substring(s.lastIndexOf('.') + 1); // Get to base class name (no path)
			BundleRef br = BundleRef.create(clz, s); // Get ref to this bundle;
			if(br.exists())
				return br; // Return if it has data

			//-- Use messages bundle off this thing
			return BundleRef.create(clz, "messages");
		}
		return null;
	}

	/**
	 * Returns the bundle for the specified class, defined as classname[nls].properties.
	 * @param clz
	 * @return
	 */
	static public BundleRef getClassBundle(final Class< ? > clz) {
		String s = clz.getName();
		s = s.substring(s.lastIndexOf('.') + 1); // Get to base class name (no path)
		return BundleRef.create(clz, s); // Get ref to this bundle;
	}

	static public BundleRef getPackageBundle(final Class< ? > base) {
		return BundleRef.create(base, "messages"); // Default package bundle is messages[nls].properties
	}

	/**
	 * Lookup a page Title bar text..
	 * @param clz
	 * @return
	 */
	static public String calcPageTitle(final Class< ? > clz) {
		UIMenu ma = clz.getAnnotation(UIMenu.class); // Is annotated with UIMenu?
		Locale loc = NlsContext.getLocale();
		BundleRef br = findBundle(ma, clz);

		//-- Explicit specification of the names?
		if(ma != null && br != null) {
			//-- Has menu annotation. Is there a title key?
			if(ma.titleKey().length() != 0)
				return br.getString(loc, ma.titleKey()); // When present it MUST exist.

			//-- Is there a keyBase?
			if(ma.baseKey().length() != 0) {
				String s = br.findMessage(loc, ma.baseKey() + ".title"); // Is this base thing present?
				if(s != null) // This can be not-present...
					return s;
			}

			//-- No title. Can we use the menu label?
			if(ma.labelKey().length() > 0)
				return br.getString(loc, ma.labelKey()); // When present this must exist

			//-- Try the label from keyBase..
			if(ma.baseKey().length() != 0) {
				String s = br.findMessage(loc, ma.baseKey() + ".label");
				if(s != null) // This can be not-present...
					return s;
			}
		}

		//-- Try default page bundle and package bundle names.
		br = getClassBundle(clz); // Find bundle for the class
		String s = br.findMessage(loc, "title"); // Find title key
		if(s != null)
			return s;
		s = br.findMessage(loc, "label");
		if(s != null)
			return s;

		//-- Try package bundle.
		br = getPackageBundle(clz);
		String root = clz.getName();
		root = root.substring(root.lastIndexOf('.') + 1); // Class name without package
		s = br.findMessage(loc, root + ".title"); // Find title key
		if(s != null)
			return s;
		s = br.findMessage(loc, root + ".label");
		if(s != null)
			return s;

		//-- No annotation, or the annotation did not deliver data. Try the menu.

		//-- Try metadata
		ClassMetaModel cmm = MetaManager.findClassMeta(clz);
		String name = cmm.getUserEntityName();
		if(name != null)
			return name;

		//-- Nothing worked.... Return the class name as a last resort.
		s = clz.getName();
		return s.substring(s.lastIndexOf('.') + 1);
	}

	/**
	 * Lookup a page Title bar text..
	 * @param clz
	 * @return
	 */
	static public String calcPageLabel(final Class< ? > clz) {
		UIMenu ma = clz.getAnnotation(UIMenu.class); // Is annotated with UIMenu?
		Locale loc = NlsContext.getLocale();
		BundleRef br = findBundle(ma, clz);

		//-- Explicit specification of the names?
		if(ma != null && br != null) {
			//-- Has menu annotation. Is there a title key?
			if(ma.titleKey().length() != 0)
				return br.getString(loc, ma.titleKey()); // When present it MUST exist.

			//-- Is there a keyBase?
			if(ma.baseKey().length() != 0) {
				String s = br.findMessage(loc, ma.baseKey() + ".label"); // Is this base thing present?
				if(s != null) // This can be not-present...
					return s;
			}

			//-- No title. Can we use the menu label?
			if(ma.labelKey().length() > 0)
				return br.getString(loc, ma.labelKey()); // When present this must exist

			//-- Try the label from keyBase..
			if(ma.baseKey().length() != 0) {
				String s = br.findMessage(loc, ma.baseKey() + ".title");
				if(s != null) // This can be not-present...
					return s;
			}
		}

		//-- Try default page bundle and package bundle names.
		br = getClassBundle(clz); // Find bundle for the class
		String s = br.findMessage(loc, "label"); // Find title key
		if(s != null)
			return s;
		s = br.findMessage(loc, "title");
		if(s != null)
			return s;

		//-- Try package bundle.
		br = getPackageBundle(clz);
		String root = clz.getName();
		root = root.substring(root.lastIndexOf('.') + 1); // Class name without package
		s = br.findMessage(loc, root + ".label"); // Find title key
		if(s != null)
			return s;
		s = br.findMessage(loc, root + ".title");
		if(s != null)
			return s;

		//-- No annotation, or the annotation did not deliver data. Try the menu.
		return null;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Resource Bundle utilities.							*/
	/*--------------------------------------------------------------*/
	/**
	 * Locates the default page bundle for a page. The lookup of the bundle
	 * is as follows (first match returns):
	 * <ul>
	 *	<li>If the page has an @UIMenu annotation use it's bundleBase and bundleName to find the page bundle. It is an error to specify a nonexistent bundle here.</li>
	 *	<li>Try a bundle with the same name as the page class</li>
	 * </ul>
	 * If this fails return null.
	 *
	 * @param urlPage
	 * @return
	 */
	public static BundleRef findPageBundle(UrlPage urlPage) {
		if(urlPage == null)
			throw new NullPointerException("Page cannot be null here");

		//-- Try to locate UIMenu-based resource
		UIMenu uim = urlPage.getClass().getAnnotation(UIMenu.class);
		if(uim != null) {
			if(uim.bundleBase() != Object.class || uim.bundleName().length() != 0) {
				//-- We have a specification for the bundle- it must exist
				BundleRef br = findBundle(uim, urlPage.getClass());
				if(!br.exists())
					throw new ProgrammerErrorException("@UIMenu bundle specified (" + uim.bundleBase() + "," + uim.bundleName() + ") but does not exist on page class " + urlPage.getClass());
				return br;
			}
		}

		//-- Try page class related bundle.
		String fullname = urlPage.getClass().getName();
		int ix = fullname.lastIndexOf('.');

		String cn = fullname.substring(ix + 1); // Classname only,
		BundleRef br = BundleRef.create(urlPage.getClass(), cn); // Try to find
		if(br.exists())
			return br;

		//-- Finally: allow 'messages' bundle in this package, if present
		br = BundleRef.create(urlPage.getClass(), "messages");
		if(br.exists())
			return br;
		return null; // Failed to get bundle.
	}

	/**
	 * If the string passed starts with ~ start page resource bundle translation.
	 * @param nodeBase
	 * @param title
	 * @return
	 */
	public static String replaceTilded(NodeBase nodeBase, String txt) {
		if(txt == null) // Unset - exit
			return null;
		if(!txt.startsWith("~"))
			return txt;
		if(txt.startsWith("~~")) // Dual tilde escapes and returns a single-tilded thingy.
			return txt.substring(1);

		//-- Must do replacement
		Page p = nodeBase.getPage();
		if(p == null)
			throw new ProgrammerErrorException("Attempt to retrieve a page-bundle's key (" + txt + "), but the node (" + nodeBase + ")is not attached to a page");
		return p.getBody().$(txt);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Error message visualisation utilities.				*/
	/*--------------------------------------------------------------*/

	/**
	 * Render a text string that possibly contains some simple HTML constructs as a DomUI
	 * node set into the container passed. The code currently accepts: B, BR, I, EM, STRONG
	 * as tags.
	 */
	static public void renderHtmlString(NodeContainer d, String text) {
		if(text == null || text.length() == 0)
			return;
		StringBuilder sb = new StringBuilder(text.length()); // rll string segment buffer
		List<NodeContainer> nodestack = Collections.EMPTY_LIST; // generated html stack (embedding)

		/*
		 * Enter a scan loop. The scan loop has two sections; the first one scans the TEXT between tags and adds it
		 * to the string buffer. The second loop scans a tag and handles it properly. After that we return to scanning
		 * text etc until the string is done.
		 */
		int ix = 0;
		int len = text.length();
		NodeContainer top = d;
		for(;;) {
			//-- Text scan: scan content and add to the buffer until a possible tag start character is found.
			while(ix < len) {
				char c = text.charAt(ix);
				if(c == '<')
					break;
				sb.append(c);
				ix++;
			}

			//-- Ok; we possibly have some text in the buffer and have reached a tag or eoln.
			if(ix >= len)
				break;

			//-- Tag scan. We find the end of the tag and check if we recognise it. We currently are on the open '<'.
			int tix = ix + 1; // Get past >
			String tag = null;
			while(tix < len) {
				char c = text.charAt(tix++);
				if(c == '>') {
					//-- Found an end tag- set the tag found.
					tag = text.substring(ix, tix); // Get whole tag including <>.
					break;
				}
			}

			//-- If no tag was found (missing >) we have a literal < so copy it.
			if(tag == null) {
				//-- Literal <. Copy to text string and continue scanning text
				sb.append('<');
				ix++; // Skip over <.
			} else {
				//-- Some kind of text between <>. Scan for recognised constructs; open tags 1st
				if(tag.equalsIgnoreCase("<br>") || tag.equalsIgnoreCase("<br/>") || tag.equalsIgnoreCase("<br />")) {
					//-- Newline. Append a BR node.
					appendOptionalText(top, sb);
					top.add(new BR());
					ix = tix;
				} else if(tag.equalsIgnoreCase("<b>") || tag.equalsIgnoreCase("<strong>")) {
					appendOptionalText(top, sb);
					ix = tix;
					NodeContainer n = new Span();
					n.setCssClass("ui-txt-b");
					nodestack = appendContainer(nodestack, n);
					top.add(n);
					top = n;
				} else if(tag.equalsIgnoreCase("<i>") || tag.equalsIgnoreCase("<em>")) {
					appendOptionalText(top, sb);
					ix = tix;
					NodeContainer n = new Span();
					n.setCssClass("ui-txt-i");
					nodestack = appendContainer(nodestack, n);
					top.add(n);
					top = n;
				} else if(tag.startsWith("</")) {
					//-- Some kind of end tag.
					tag = tag.substring(2, tag.length() - 1).trim(); // Remove </ >
					if(tag.equalsIgnoreCase("b") || tag.equalsIgnoreCase("i") || tag.equalsIgnoreCase("strong") || tag.equalsIgnoreCase("em")) {
						//-- Recognised end tag: pop node stack.
						ix = tix;
						appendOptionalText(top, sb); // Append the text for this node because it ends.
						if(nodestack.size() > 0) {
							nodestack.remove(nodestack.size() - 1);
							if(nodestack.size() == 0)
								top = d;
							else
								top = nodestack.get(nodestack.size() - 1);
						}
					} else {
						//-- Unrecognised end tag: just add
						sb.append('<');
						ix++;
					}
				} else {
					//-- Unrecognised thingy: copy < verbatim and scan on.
					sb.append('<');
					ix++;
				}
			}
		}

		//-- We have reached eo$. If there is text left in the buffer render it in the last node added, then be done.
		if(sb.length() > 0)
			top.add(sb.toString());
	}

	/**
	 * This scans the input, and only copies "safe" html, which is HTML with only
	 * simple constructs. It checks to make sure the resulting document is xml-safe (well-formed),
	 * if the input is not well-formed it will add or remove tags until the result is valid.
	 *
	 * @param sb
	 * @param html
	 */
	static public void htmlRemoveUnsafe(StringBuilder outsb, String text) {
		if(text == null || text.length() == 0)
			return;
		new HtmlTextScanner().scan(outsb, text);
	}

	static public String htmlRemoveUnsafe(String html) {
		if(html == null || html.length() == 0)
			return "";
		StringBuilder sb = new StringBuilder(html.length() + 20);
		htmlRemoveUnsafe(sb, html);
		return sb.toString();
	}

	static public void htmlRemoveAll(StringBuilder outsb, String text, boolean lf) {
		if(text == null || text.length() == 0)
			return;
		new HtmlTextScanner().scanAndRemove(outsb, text, lf);
	}

	static public String htmlRemoveAll(String html, boolean lf) {
		if(html == null || html.length() == 0)
			return "";
		StringBuilder sb = new StringBuilder(html.length() + 20);
		htmlRemoveAll(sb, html, lf);
		return sb.toString();
	}


	static public List<NodeContainer> appendContainer(List<NodeContainer> stack, NodeContainer it) {
		if(stack == Collections.EMPTY_LIST)
			stack = new ArrayList<NodeContainer>();
		stack.add(it);
		return stack;
	}

	static private void appendOptionalText(NodeContainer nc, StringBuilder sb) {
		if(sb.length() == 0)
			return;
		nc.add(sb.toString());
		sb.setLength(0);
	}

	/**
	 * This scans an error messages for simple HTML and renders that as DomUI nodes. The rendered content gets added to
	 * the container passed.
	 */
	static public void renderErrorMessage(NodeContainer d, UIMessage m) {
		if(d.getCssClass() == null)
			d.setCssClass("ui-msg ui-msg-" + m.getType().name().toLowerCase());
		d.setUserObject(m);
		String text = m.getErrorLocation() != null ? "<b>" + m.getErrorLocation() + "</b>" + ": " + m.getMessage() : m.getMessage();
		renderHtmlString(d, text);
		if(m.getErrorNode() != null) {
			m.getErrorNode().addCssClass("ui-input-err");
		}
	}

	/**
	 * Obtain a parameter and convert it to a Long wrapper.
	 * @param pp
	 * @param name
	 * @param def
	 * @return
	 */
	static public Long getLongParameter(PageParameters pp, String name, Long def) {
		String s = pp.getString(name, null); // Parameter present?
		if(s == null || s.trim().length() == 0)
			return def;
		try {
			return Long.valueOf(s.trim());
		} catch(Exception x) {
			throw new UIException(Msgs.BUNDLE, Msgs.X_INVALID_PARAMETER, name);
		}
	}

	/**
	 * Convert a CSS size string like '200px' into the 200... If the size string is in any way
	 * invalid this returns -1.
	 *
	 * @param css
	 * @return
	 */
	static public int pixelSize(String css) {
		if(!css.endsWith("px"))
			return -1;
		try {
			return Integer.parseInt(css.substring(0, css.length() - 2).trim());
		} catch(Exception x) {
			return -1;
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Tree walking helpers.								*/
	/*--------------------------------------------------------------*/
	/**
	 * Functor interface to handle tree walking.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Nov 3, 2009
	 */
	static public interface IPerNode {
		/** When this object instance is returned by the before(NodeBase) method we SKIP the downwards traversal. */
		static public final Object SKIP = new Object();

		/**
		 * Called when the node is first encountered in the tree. It can return null causing the rest of the tree
		 * to be traversed; if it returns the constant IPerNode.SKIP the subtree starting at this node will not
		 * be traversed but the rest of the tree will. When you return SKIP the {@link IPerNode#after(NodeBase)} method
		 * will not be called for this node. Returning any other value will stop the node traversal process
		 * and return that value to the caller of {@link DomUtil#walkTree(NodeBase, IPerNode)}.
		 * @param n
		 * @return
		 * @throws Exception
		 */
		public Object before(NodeBase n) throws Exception;

		/**
		 * Called when all child nodes of the specified node have been traversed. When this returns a non-null
		 * value this will terminate the tree walk and return that value to the called of {@link DomUtil#walkTree(NodeBase, IPerNode)}.
		 * @param n
		 * @return
		 * @throws Exception
		 */
		public Object after(NodeBase n) throws Exception;
	}

	/**
	 * Walks a node tree, calling the handler for every node in the tree. As soon as
	 * a handler returns not-null traversing stops and that object gets returned.
	 * @param handler
	 * @return
	 * @throws Exception
	 */
	static public Object walkTree(NodeBase root, IPerNode handler) throws Exception {
		if(root == null)
			return null;
		Object v = handler.before(root);
		if(v == IPerNode.SKIP)
			return null;
		if(v != null)
			return v;
		if(root instanceof NodeContainer) {
			for(NodeBase ch : (NodeContainer) root) {
				v = walkTree(ch, handler);
				if(v != null)
					return v;
			}
		}
		return handler.after(root);
	}

	/**
	 * This clears the 'modified' flag for all nodes in the subtree that implement {@link IHasModifiedIndication}.
	 * @param root		The subtree to traverse
	 */
	static public void clearModifiedFlag(NodeBase root) {
		try {
			walkTree(root, new IPerNode() {
				public Object before(NodeBase n) throws Exception {
					if(n instanceof IHasModifiedIndication)
						((IHasModifiedIndication) n).setModified(false);
					return null;
				}

				public Object after(NodeBase n) throws Exception {
					return null;
				}
			});
		} catch(Exception x) { // Cannot happen.
			throw new RuntimeException(x);
		}
	}

	/**
	 * Walks the subtree and asks any node implementing {@link IHasModifiedIndication} whether it has been
	 * modified; return as soon as one node tells us it has been modified.
	 * @param root
	 */
	static public boolean isModified(NodeBase root) {
		try {
			Object res = walkTree(root, new IPerNode() {
				public Object before(NodeBase n) throws Exception {
					if(n instanceof IHasModifiedIndication) {
						if(((IHasModifiedIndication) n).isModified())
							return Boolean.TRUE;
					}
					if(n instanceof IUserInputModifiedFence)
						return SKIP;
					return null;
				}

				public Object after(NodeBase n) throws Exception {
					return null;
				}
			});
			return res != null;
		} catch(Exception x) { // Cannot happen.
			throw new RuntimeException(x);
		}
	}

	/**
	 * Update modified flag of node. Propagate notify signal up to final modified fence in parant tree, if any is defined.
	 * Use it to set modified flag as result of handling of user data modification.
	 * @param node
	 */
	static public void setModifiedFlag(NodeBase node) {
		NodeBase n = node;
		while(n != null) {
			boolean wasModifiedBefore = false;
			if(n instanceof IHasModifiedIndication) {
				wasModifiedBefore = ((IHasModifiedIndication) n).isModified();
				((IHasModifiedIndication) n).setModified(true);
			}
			if(n instanceof IUserInputModifiedFence) {
				if(!wasModifiedBefore) {
					((IUserInputModifiedFence) n).onModifyFlagRaised();
				}
				if(((IUserInputModifiedFence) n).isFinalUserInputModifiedFence()) {
					return;
				}
			}
			n = (NodeBase) n.getParent(IUserInputModifiedFence.class);
		}
	}

	/**
	 * Checks if string is blank.
	 * @param s String to be validated.
	 * @return true if it is blank, false otherwise.
	 */
	static public boolean isBlank(String s) {
		return s == null || s.trim().length() == 0;
	}

	static public boolean isRelativeURL(String in) {
		if(in == null)
			return false;
		if(in.startsWith("http:") || in.startsWith("https:") || in.startsWith("/"))
			return false;
		return true;
	}

	static public String createOpenWindowJS(Class< ? > targetClass, PageParameters targetParameters, WindowParameters newWindowParameters) {
		//-- We need a NEW window session. Create it,
		RequestContextImpl ctx = (RequestContextImpl) PageContext.getRequestContext();
		WindowSession cm = ctx.getSession().createWindowSession();

		//-- Send a special JAVASCRIPT open command, containing the shtuff.
		StringBuilder sb = new StringBuilder();
		sb.append("DomUI.openWindow('");
		sb.append(ctx.getRelativePath(targetClass.getName()));
		sb.append(".ui?");
		StringTool.encodeURLEncoded(sb, Constants.PARAM_CONVERSATION_ID);
		sb.append('=');
		sb.append(cm.getWindowID());
		sb.append(".x");
		if(targetParameters != null)
			DomUtil.addUrlParameters(sb, targetParameters, false);
		sb.append("','");
		sb.append(cm.getWindowID());
		sb.append("','");

		sb.append("resizable=");
		sb.append(newWindowParameters.isResizable() ? "yes" : "no");
		sb.append(",scrollbars=");
		sb.append(newWindowParameters.isShowScrollbars() ? "yes" : "no");
		sb.append(",toolbar=");
		sb.append(newWindowParameters.isShowToolbar() ? "yes" : "no");
		sb.append(",location=");
		sb.append(newWindowParameters.isShowLocation() ? "yes" : "no");
		sb.append(",directories=");
		sb.append(newWindowParameters.isShowDirectories() ? "yes" : "no");
		sb.append(",status=");
		sb.append(newWindowParameters.isShowStatus() ? "yes" : "no");
		sb.append(",menubar=");
		sb.append(newWindowParameters.isShowMenubar() ? "yes" : "no");
		sb.append(",copyhistory=");
		sb.append(newWindowParameters.isCopyhistory() ? "yes" : "no");

		if(newWindowParameters.getWidth() > 0) {
			sb.append(",width=");
			sb.append(newWindowParameters.getWidth());
		}
		if(newWindowParameters.getHeight() > 0) {
			sb.append(",height=");
			sb.append(newWindowParameters.getHeight());
		}
		sb.append("');");
		return sb.toString();
	}

	static public String createOpenWindowJS(String url, WindowParameters newWindowParameters) {
		//-- We need a NEW window session. Create it,
		RequestContextImpl ctx = (RequestContextImpl) PageContext.getRequestContext();
		WindowSession cm = ctx.getSession().createWindowSession();

		//-- Send a special JAVASCRIPT open command, containing the shtuff.
		StringBuilder sb = new StringBuilder();
		sb.append("DomUI.openWindow('");
		sb.append(url);
		sb.append("','");
		sb.append(cm.getWindowID());
		sb.append("','");

		sb.append("resizable=");
		sb.append(newWindowParameters.isResizable() ? "yes" : "no");
		sb.append(",scrollbars=");
		sb.append(newWindowParameters.isShowScrollbars() ? "yes" : "no");
		sb.append(",toolbar=");
		sb.append(newWindowParameters.isShowToolbar() ? "yes" : "no");
		sb.append(",location=");
		sb.append(newWindowParameters.isShowLocation() ? "yes" : "no");
		sb.append(",directories=");
		sb.append(newWindowParameters.isShowDirectories() ? "yes" : "no");
		sb.append(",status=");
		sb.append(newWindowParameters.isShowStatus() ? "yes" : "no");
		sb.append(",menubar=");
		sb.append(newWindowParameters.isShowMenubar() ? "yes" : "no");
		sb.append(",copyhistory=");
		sb.append(newWindowParameters.isCopyhistory() ? "yes" : "no");

		if(newWindowParameters.getWidth() > 0) {
			sb.append(",width=");
			sb.append(newWindowParameters.getWidth());
		}
		if(newWindowParameters.getHeight() > 0) {
			sb.append(",height=");
			sb.append(newWindowParameters.getHeight());
		}
		sb.append("');");
		return sb.toString();
	}

}
