/*
 * DomUI Java User Interface library
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
package to.etc.domui.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.annotations.UIMenu;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.misc.WindowParameters;
import to.etc.domui.dom.errors.IErrorFence;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.BR;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.IHasModifiedIndication;
import to.etc.domui.dom.html.IUserInputModifiedFence;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Page;
import to.etc.domui.dom.html.Span;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;
import to.etc.domui.dom.html.THead;
import to.etc.domui.dom.html.TR;
import to.etc.domui.dom.html.Table;
import to.etc.domui.dom.html.TextNode;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.IRequestContext;
import to.etc.domui.server.RequestContextImpl;
import to.etc.domui.server.WrappedHttpServetResponse;
import to.etc.domui.state.AppSession;
import to.etc.domui.state.IPageParameters;
import to.etc.domui.state.PageParameters;
import to.etc.domui.state.UIContext;
import to.etc.domui.state.UIGoto;
import to.etc.domui.state.WindowSession;
import to.etc.domui.trouble.Trouble;
import to.etc.domui.trouble.UIException;
import to.etc.domui.trouble.ValidationException;
import to.etc.util.ByteArrayUtil;
import to.etc.util.ExceptionClassifier;
import to.etc.util.FileTool;
import to.etc.util.HtmlScanner;
import to.etc.util.LineIterator;
import to.etc.util.StringTool;
import to.etc.webapp.ProgrammerErrorException;
import to.etc.webapp.nls.BundleRef;
import to.etc.webapp.nls.NlsContext;
import to.etc.webapp.query.IIdentifyable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

final public class DomUtil {
	static public final Logger USERLOG = LoggerFactory.getLogger("to.etc.domui.userAction");

	static public final String DOCROOT = "https://etc.to/confluence/";

	static private int m_guidSeed;

	/** Map value types of primitive type to their boxed (wrapped) types. */
	final static private Map<Class<?>, Class<?>> BOXINGDISASTER = new HashMap<>();

	static {
		BOXINGDISASTER.put(long.class, Long.class);
		BOXINGDISASTER.put(int.class, Integer.class);
		BOXINGDISASTER.put(short.class, Short.class);
		BOXINGDISASTER.put(char.class, Character.class);
		BOXINGDISASTER.put(double.class, Double.class);
		BOXINGDISASTER.put(float.class, Float.class);
		BOXINGDISASTER.put(boolean.class, Boolean.class);
		BOXINGDISASTER.put(byte.class, Byte.class);

		long val = System.currentTimeMillis() / 1000 / 60;
		m_guidSeed = (int) val;
	}

	private DomUtil() {
	}

	/**
	 * Map value types of primitive type to their boxed (wrapped) types.
	 * @param clz
	 * @return
	 */
	@Nonnull
	static public Class<?> getBoxedForPrimitive(@Nonnull Class<?> clz) {
		Class<?> newClass = BOXINGDISASTER.get(clz);
		return newClass != null ? newClass : clz;
	}

	/**
	 * Use Objects.requireNonNull.
	 *
	 * NULL CHECKING BELONGS IN THE LANGUAGE, NOT IN ANNOTATIONS, damnit! This fine idiocy is needed to
	 * handle null checking because the pathetic losers that make up the Java JSR board are so incredible
	 * stupid it boggles the mind. Java == cobol 8-(
	 *
	 * @param in
	 * @return
	 */
	@Deprecated
	@Nonnull
	static public <T> T nullChecked(@Nullable T in) {
		if(null == in)
			throw new IllegalStateException("Unexpected thingy is null: " + in);
		return in;
	}

	/**
	 * Does explicit non null check for parameter passed in as nullable but expected to be non null.
	 * In case of null, it throws IllegalStateException with provided exceptionMsg.
	 *
	 * @param in
	 * @param exceptionMsg
	 * @return
	 * @deprecated Use Objects.requireNonNull.
	 */
	@Deprecated
	@Nonnull
	static public <T> T nullChecked(@Nullable T in, @Nonnull String exceptionMsg) {
		if(null == in)
			throw new IllegalStateException(exceptionMsg);
		return in;
	}

	/**
	 * Don not use.
	 * We are not sure that it's checked before. This is a potential null pointer access.
	 * Adding this mean that we didn't want to introduce risk but we are marking this as a bad part of code.
	 *
	 * @param in
	 * @return
	 */
	@Deprecated
	@Nonnull
	static public <T> T badCheck(T in) {
		return in;
	}

	/**
	 * Define (or clear) the x-ua-compatible value sent for this page. When not called
	 * this defaults to the value defined by the ms-emulation property in web.xml.
	 * @param comp
	 * @throws IOException
	 */
	static public final void setPageCompatibility(@Nonnull HttpServletResponse req, @Nullable String comp) throws IOException {
		if(!(req instanceof WrappedHttpServetResponse))
			return;
		WrappedHttpServetResponse wsr = (WrappedHttpServetResponse) req;
		wsr.setIeEmulationMode(comp);
	}

	/**
	 * FIXME REMOVE!??!
	 * @param req
	 * @throws IOException
	 */
	@Deprecated
	static public final void ie8Capable(HttpServletResponse req) throws IOException {
	}

	static public final boolean isEqualOLD(final Object a, final Object b) {
		if(a == b)
			return true;
		if(a == null || b == null)
			return false;
		return a.equals(b);
	}

	static public final boolean isEqual(final Object a, final Object b) {
		if(a == b)
			return true;
		if(a == null || b == null)
			return false;
		if(a instanceof Date && b instanceof Date) {
			//Dates needs special handling
			return ((Date) a).compareTo((Date) b) == 0;
		}
		if(a instanceof BigDecimal && b instanceof BigDecimal) {
			//BigDecimals needs special handling
			return ((BigDecimal) a).compareTo((BigDecimal) b) == 0;
		}
		if(a.getClass() != b.getClass())
			return false;
		if(a.getClass().isArray() && b.getClass().isArray())
			return Arrays.equals((Object[]) a, (Object[]) b);
		return a.equals(b);
	}

	/**
	 * Use {@link StringTool#isEqualIgnoreCase(String, String)} please.
	 * @param a
	 * @param b
	 * @return
	 */
	@Deprecated
	static public final boolean isEqualIgnoreCase(@Nullable String a, @Nullable String b) {
		return StringTool.isEqualIgnoreCase(a, b);
	}

	static public final boolean isEqual(final Object... ar) {
		if(ar.length < 2)
			throw new IllegalStateException("Silly.");
		Object a = ar[0];
		for(int i = ar.length; --i >= 1; ) {
			if(!isEqual(a, ar[i]))
				return false;
		}
		return true;
	}

	@Nullable
	static public <T> T getValueSafe(@Nonnull IControl<T> node) {
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
	static public boolean classResourceExists(final Class<?> clz, final String name) {
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

	static public final Class<?> findClass(@Nonnull final ClassLoader cl, @Nonnull final String name) {
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
	static public boolean isIntegerType(Class<?> clz) {
		return clz == int.class || clz == Integer.class || clz == long.class || clz == Long.class || clz == Short.class || clz == short.class;
	}

	static public boolean isDoubleOrWrapper(Class<?> clz) {
		return clz == Double.class || clz == double.class;
	}

	static public boolean isFloatOrWrapper(Class<?> clz) {
		return clz == Float.class || clz == float.class;
	}

	static public boolean isIntegerOrWrapper(Class<?> clz) {
		return clz == Integer.class || clz == int.class;
	}

	static public boolean isShortOrWrapper(Class<?> clz) {
		return clz == Short.class || clz == short.class;
	}

	static public boolean isByteOrWrapper(Class<?> clz) {
		return clz == Byte.class || clz == byte.class;
	}

	static public boolean isLongOrWrapper(Class<?> clz) {
		return clz == Long.class || clz == long.class;
	}

	static public boolean isBooleanOrWrapper(Class<?> clz) {
		return clz == Boolean.class || clz == boolean.class;
	}

	static public boolean isNumber(Class<?> clz) {
		return Number.class.isAssignableFrom(getBoxedForPrimitive(clz));
	}

	/**
	 * Return T if the class represents a real (double or float) type.
	 * @param clz
	 * @return
	 */
	static public boolean isRealType(Class<?> clz) {
		return clz == float.class || clz == Float.class || clz == Double.class || clz == double.class;
	}

	/**
	 * Returns T if the type is some numeric type that can have a fraction.
	 * @param clz
	 * @return
	 */
	static public boolean isScaledType(Class<?> clz) {
		return isRealType(clz) || clz == BigDecimal.class;
	}

	/**
	 * Returns T if this is one of the basic types: any numeric including BigDecimal and BigInteger; string, or date.
	 * @param t
	 * @return
	 */
	static public boolean isBasicType(Class<?> t) {
		if(t.isPrimitive())
			return true;
		return isIntegerOrWrapper(t) || isLongOrWrapper(t) || isShortOrWrapper(t) || isByteOrWrapper(t) || isDoubleOrWrapper(t) || isFloatOrWrapper(t) || isBooleanOrWrapper(t) || t == String.class
			|| t == BigDecimal.class || t == BigInteger.class || t == Date.class || Enum.class.isAssignableFrom(t);
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
		Class<?> clz = inst.getClass();
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


	/**
	 * Returns T if the topclass or one of its base classes (above the base class) has overridden
	 * the specified method.
	 * @param topClass
	 * @param baseClass
	 * @param method
	 * @param parameters
	 * @return
	 */
	static public boolean hasOverridden(Class<?> topClass, Class<?> baseClass, String method, Class<?>... parameters) {
		Class<?> currentClass = topClass;

		while(currentClass != null && currentClass != Object.class && currentClass != baseClass) {
			try {
				currentClass.getDeclaredMethod(method, parameters);        // Throws exception if not here.
				return true;
			} catch(NoSuchMethodException nmx) {
				//-- Not here, so continue walking upwards.
			}
			currentClass = currentClass.getSuperclass();
		}
		return false;
	}

	static public String createRandomColor() {
		int value = (int) (0xffffff * Math.random());
		return "#" + StringTool.intToStr(value, 16, 6);
	}

	@Nonnull
	static public IErrorFence getMessageFence(@Nonnull NodeBase in) {
		NodeBase start = in;

		//-- If we're delegated then test the delegate 1st
		if(in instanceof NodeContainer) {
			NodeContainer nc = (NodeContainer) in;
			if(nc.getDelegate() != null) {
				IErrorFence ef = getMessageFence(nc.getDelegate());
				if(null != ef)
					return ef;
			}
		}

		for(; ; ) {
			if(start == null) {
				//-- Collect the path we followed for the error message
				StringBuilder sb = new StringBuilder();
				sb.append("Cannot locate error fence. Did you call an error routine on an unattached Node?\nThe path followed upwards was: ");
				start = in;
				for(; ; ) {
					if(start != in)
						sb.append(" -> ");
					sb.append(start.toString());
					if(!start.hasParent())
						break;
					start = start.getParent();
				}

				throw new IllegalStateException(sb.toString());
			}
			if(start instanceof NodeContainer) {
				NodeContainer nc = (NodeContainer) start;
				IErrorFence errorFence = nc.getErrorFence();
				if(errorFence != null)
					return errorFence;
			}
			//			if(start.getParent() == null) {
			//				return start.getPage().getErrorFence();	// Use the generic page's fence.
			//			}
			if(start.hasParent())
				start = start.getParent();
			else
				start = null;
		}
	}

	//fix for call 28547: $ cant be used in window names in javascript function window.openWindow in IE7, so we have to use something else...
	static private final char[] BASE64MAP = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_0".toCharArray();

	/**
	 * Generate an unique identifier with reasonable expectations that it will be globally unique. This
	 * does not use the known GUID format but shortens the string by encoding into base64-like encoding.
	 * @return
	 */
	@Nonnull
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
			String[] parameters = ctx.getParameters(name);
			if(null == parameters)
				continue;
			for(String value : parameters) {
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

	static public void addUrlParameters(@Nonnull final StringBuilder sb, @Nonnull final IPageParameters ctx, boolean first) {
		addUrlParameters(sb, ctx, first, Collections.EMPTY_SET);
	}

	static public void addUrlParameters(@Nonnull final StringBuilder sb, @Nonnull final IPageParameters ctx, boolean first, @Nonnull Set<String> skipset) {
		if(ctx == null)
			return;
		for(String name : ctx.getParameterNames()) {
			if(name.equals(Constants.PARAM_CONVERSATION_ID))
				continue;
			if(skipset.contains(name))
				continue;
			String[] values = ctx.getStringArray(name);
			for(String value : values) {
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

	/**
	 * Returns application url part from current request.
	 * Call depends on existing of request, so it can't be used within backend threads.
	 * @return
	 */
	static public String getApplicationURL() {
		return UIContext.getRequestContext().getRequestResponse().getApplicationURL();
	}

	/**
	 * Returns application context part from current request. Returns the webapp
	 * context as either an empty string for the ROOT context or a string starting
	 * without a slash and always ending in one, like "viewpoint/".
	 * Call depends on existing of request, so it can't be used within backend threads.
	 * @return
	 */
	static public String getApplicationContext() {
		return UIContext.getRequestContext().getRequestResponse().getWebappContext();
	}

	/**
	 * Returns relative path for specified resource (without host name, like '/APP_CONTEXT/resource').
	 * Call depends on existing of request, so it can't be used within backend threads.
	 * @param resource
	 * @return
	 */
	static public String getRelativeApplicationResourceURL(String resource) {
		return "/" + getApplicationContext() + "/" + resource;
	}

	/**
	 * IMPORTANT: This method MUST be used only within UI threads, when UIContext.getRequestContext() != null!
	 * In all other, usually background running threads, other alternatives that are using stored appURL must be used!
	 * @param clz
	 * @param pp
	 * @return
	 */
	static public String createPageURL(Class<? extends UrlPage> clz, IPageParameters pp) {
		StringBuilder sb = new StringBuilder();
		sb.append(UIContext.getRequestContext().getRelativePath(clz.getName()));
		sb.append('.');
		sb.append(DomApplication.get().getUrlExtension());
		if(pp != null)
			addUrlParameters(sb, pp, true);
		return sb.toString();
	}

	/**
	 * Create a relative URL for the specified page (an URL that is relative to the application's context, i.e. without
	 * hostname nor webapp context).
	 * @param clz
	 * @param pp
	 * @return
	 */
	@Nonnull
	static public String createPageRURL(@Nonnull Class<? extends UrlPage> clz, @Nullable IPageParameters pp) {
		StringBuilder sb = new StringBuilder();
		sb.append(clz.getName());
		sb.append('.');
		sb.append(DomApplication.get().getUrlExtension());
		if(pp != null)
			addUrlParameters(sb, pp, true);
		return sb.toString();
	}

	/**
	 * IMPORTANT: This method MUST be used for non UI threads, when UIContext.getRequestContext() == null!
	 * In all other, usually UI running threads, use other alternatives that is using appURL from UIContext.getRequestContext()!
	 *
	 * @param webAppUrl web app url, must be ended with '/'
	 * @param clz
	 * @param pp
	 * @return
	 */
	static public String createPageURL(@Nonnull String webAppUrl, @Nonnull Class<? extends UrlPage> clz, @Nullable IPageParameters pp) {
		StringBuilder sb = new StringBuilder();
		sb.append(webAppUrl);
		sb.append(clz.getName());
		sb.append('.');
		sb.append(DomApplication.get().getUrlExtension());
		if(pp != null)
			addUrlParameters(sb, pp, true);
		return sb.toString();
	}

	/**
	 * Generate an URL to some page with parameters.
	 *
	 * @param rurl    The absolute or relative URL to whatever resource.
	 * @param pageParameters
	 * @return
	 */
	public static String createPageURL(String rurl, IPageParameters pageParameters) {
		StringBuilder sb = new StringBuilder();
		if(DomUtil.isRelativeURL(rurl)) {
			RequestContextImpl ctx = (RequestContextImpl) UIContext.getRequestContext();
			sb.append(ctx.getRelativePath(rurl));
		} else
			sb.append(rurl);
		if(pageParameters != null) {
			addUrlParameters(sb, pageParameters, true);
		}
		return sb.toString();
	}

	@Nonnull
	public static String getAdjustedPageUrl(@Nonnull Page page, @Nullable IPageParameters pp) {
		PageParameters newpp = mergePageParameters(pp);
		StringBuilder sb = getPageContextURL(page);
		DomUtil.addUrlParameters(sb, newpp, false);
		return sb.toString();
	}

	@Nonnull
	public static String getAdjustedComponentUrl(@Nonnull NodeBase component, @Nonnull String command, @Nullable IPageParameters pp) {
		PageParameters newpp = mergePageParameters(pp);

		//-- Add the action code.
		newpp.addParameter("webuia", command);
		newpp.addParameter("webuic", component.getActualID());

		StringBuilder sb = getPageContextURL(component.getPage());
		DomUtil.addUrlParameters(sb, newpp, false);
		return sb.toString();
	}

	@Nonnull
	private static PageParameters mergePageParameters(@Nullable IPageParameters pp) {
		PageParameters newpp = PageParameters.createFrom(UIContext.getRequestContext());
		if(null != pp) {
			for(String name : pp.getParameterNames()) {
				String value = pp.getString(name);
				newpp.addParameter(name, value);
			}
		}
		return newpp;
	}

	@Nonnull
	private static StringBuilder getPageContextURL(@Nonnull Page page) {
		StringBuilder sb = new StringBuilder();
		sb.append(UIContext.getRequestContext().getRelativePath(page.getBody().getClass().getName()));
		sb.append(".").append(DomApplication.get().getUrlExtension());
		sb.append("?");
		sb.append(Constants.PARAM_CONVERSATION_ID);
		sb.append("=");
		StringTool.encodeURLEncoded(sb, page.getConversation().getFullId());
		return sb;
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
		if(rurl.startsWith("www."))
			return "http://" + rurl;
		if(rurl.startsWith("/"))
			return rurl;

		//-- Append context.
		return ci.getRelativePath(rurl);
	}

//	@Nonnull
//	static public String[] decodeCID(@Nonnull final String param) {
//		if(param == null)
//			throw new IllegalStateException("$cid cannot be null");
//		int pos = param.indexOf('.');
//		if(pos == -1)
//			throw new IllegalStateException("Missing '.' in $CID parameter");
//		String[] res = new String[]{param.substring(0, pos), param.substring(pos + 1)};
//		return res;
//	}

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
		if(label == null || label.length() == 0)
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
							if(tr.getChildCount() == 0) {
								//--??? Childless row?! Cannot do anything with this...
								System.out.println("?? Silly empty row in table");
								//								throw new IllegalStateException("Table has a row without any TD's in it.");
							} else {
								TD td = (TD) tr.getChild(tr.getChildCount() - 1);
								td.setColspan(maxcol - count + 1); // Adjust colspan
							}
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
		for(; ; ) {
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

	/**
	 * This method will first determine whether the exception is considered severe.
	 * Based on that outcome it will dump it or not.
	 * @param x
	 */
	static public void dumpExceptionIfSevere(@Nonnull final Exception x) {
		if(ExceptionClassifier.getInstance().isSevereException(x)) {
			dumpException(x);
		}
	}


	static public void dumpException(@Nonnull StringBuilder sb, final Throwable x) {
		StringTool.strStacktrace(sb, x);

		Throwable next = null;
		for(Throwable curr = x; curr != null; curr = next) {
			next = curr.getCause();
			if(next == curr)
				next = null;

			if(curr instanceof SQLException) {
				SQLException sx = (SQLException) curr;
				while(sx.getNextException() != null) {
					sx = sx.getNextException();
					sb.append("SQL NextException: " + sx).append("\n");
				}
			}
		}
	}


	static public void dumpRequest(HttpServletRequest req) {
		System.out.println("---- request parameter dump ----");
		for(Enumeration<String> en = req.getParameterNames(); en.hasMoreElements(); ) {
			String name = en.nextElement();
			String val = req.getParameter(name);
			System.out.println(name + ": " + val);
		}
		System.out.println("---- end request parameter dump ----");
	}

	static public String getJavaResourceRURL(final Class<?> resourceBase, final String name) {
		if(name.startsWith("/")) {
			//-- Absolute resource name.
			return Constants.RESOURCE_PREFIX + name.substring(1);
		}

		String rb = resourceBase.getName();
		int pos = rb.lastIndexOf('.');
		if(pos == -1)
			throw new IllegalStateException("??");
		return Constants.RESOURCE_PREFIX + rb.substring(0, pos + 1).replace('.', '/') + name;
	}

	@Nonnull
	static public String convertToID(@Nonnull String id) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0, len = id.length(); i < len; i++) {
			char c = id.charAt(i);
			if(c == ' ')
				sb.append('_');
			else if(Character.isLetterOrDigit(c) || c == '_' || c == '-')
				sb.append(c);
		}

		return sb.toString();
	}

	/**
	 * Returns T if the specified resource exists.
	 * @param clz
	 * @param cn
	 * @return
	 */
	public static boolean hasResource(final Class<? extends UrlPage> clz, final String cn) {
		InputStream is = null;
		try {
			is = clz.getResourceAsStream(cn);
			return is != null;
		} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception x) {
			}
		}
	}

	static public String getClassNameOnly(final Class<?> clz) {
		String cn = clz.getName();
		return cn.substring(cn.lastIndexOf('.') + 1);
	}

	/**
	 *
	 * @param ma
	 * @param clz
	 * @return
	 */
	static public BundleRef findBundle(final UIMenu ma, final Class<?> clz) {
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
	static public BundleRef getClassBundle(final Class<?> clz) {
		String s = clz.getName();
		s = s.substring(s.lastIndexOf('.') + 1); // Get to base class name (no path)
		return BundleRef.create(clz, s); // Get ref to this bundle;
	}

	static public BundleRef getPackageBundle(final Class<?> base) {
		return BundleRef.create(base, "messages"); // Default package bundle is messages[nls].properties
	}

	/**
	 * Lookup a page Title bar text.. FIXME Bad logic, bad name; should have a version passing in class instance.
	 * @param clz
	 * @return
	 */
	@Nonnull
	static public String calcPageTitle(final Class<?> clz) {
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
	static public String calcPageLabel(final Class<?> clz) {
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

	//	/**
	//	 * If the string passed starts with ~ start page resource bundle translation.
	//	 * @param nodeBase
	//	 * @param title
	//	 * @return
	//	 */
	//	public static String replaceTilded(NodeBase nodeBase, String txt) {
	//		if(txt == null) // Unset - exit
	//			return null;
	//		if(!txt.startsWith("~"))
	//			return txt;
	//		if(txt.startsWith("~~")) // Dual tilde escapes and returns a single-tilded thingy.
	//			return txt.substring(1);
	//
	//		//-- Must do replacement
	//		Page p = nodeBase.getPage();
	//		if(p == null)
	//			throw new ProgrammerErrorException("Attempt to retrieve a page-bundle's key (" + txt + "), but the node (" + nodeBase + ")is not attached to a page");
	//		return p.getBody().$(txt);
	//	}

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
		for(; ; ) {
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

	static public void renderLines(@Nonnull NodeContainer nc, @Nullable String text) {
		renderLines(nc, text, null);
	}

	/**
	 * Render a text with crlf line endings into a node.
	 * @param nc
	 * @param text
	 */
	static public void renderLines(@Nonnull NodeContainer nc, @Nullable String text, @Nullable Function<String, String> lineFixer) {
		if(text == null)
			return;
		text = text.trim();
		if(text == null || text.length() == 0)					// Extra nullity test is because ecj is nuts
			return;
		for(String line : new LineIterator(text)) {
			Div d = new Div("ui-nl-line");
			nc.add(d);
			if(lineFixer != null)
				line = lineFixer.apply(line);
			d.add(line);
		}
	}

 	/**
	 * This scans the input, and only copies "safe" html, which is HTML with only
	 * simple constructs. It checks to make sure the resulting document is xml-safe (well-formed),
	 * if the input is not well-formed it will add or remove tags until the result is valid.
	 *
	 * @param outsb
	 * @param text
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
		String text = m.getErrorLocation() != null ? "<b>" + m.getErrorLocation() + ":</b> " + m.getMessage() : m.getMessage();
		renderHtmlString(d, text);
		NodeBase errorNode = m.getErrorNode();
		if(errorNode != null) {
			errorNode.addCssClass("ui-input-err");
		}
	}

	/**
	 * Should use {@link PageParameters#getLongW(String, Long) instead.
	 * Obtain a parameter and convert it to a Long wrapper.
	 * @param pp
	 * @param name
	 * @param def
	 * @return
	 */
	@Deprecated
	static public Long getLongParameter(IPageParameters pp, String name, Long def) {
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
		return pixelSize(css, -1);
	}

	/**
	 * Convert a CSS size string like '200px' into the 200... If the size string is in any way
	 * invalid this returns specified defaultVal.
	 *
	 * @param css
	 * @param defaultVal
	 * @return
	 */
	static public int pixelSize(String css, int defaultVal) {
		if(css == null || !css.endsWith("px"))
			return defaultVal;
		try {
			return Integer.parseInt(css.substring(0, css.length() - 2).trim());
		} catch(Exception x) {
			return defaultVal;
		}
	}

	/**
	 * Convert a CSS percentage size string like '90%' into the 90... If the size string is in any way
	 * invalid this returns -1.
	 *
	 * @param css
	 * @return
	 */
	static public int percentSize(String css) {
		if(css == null || !css.endsWith("%"))
			return -1;
		try {
			return Integer.parseInt(css.substring(0, css.length() - 1).trim());
		} catch(Exception x) {
			return -1;
		}
	}

	/**
	 * Checks whether the icon name specified is not a resource (.png, .gif et al) but an icon name like
	 * an FontAwesome icon name.
	 */
	public static boolean isIconName(String iconUrl) {
		if(iconUrl.contains("."))
			return false;
		if(iconUrl.contains("/"))
			return false;
		return iconUrl.startsWith("fa-");
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Handle cookies.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Find a cookie if it exists, return null otherwise.
	 * @param name
	 * @return
	 */
	@Nullable
	static public Cookie findCookie(@Nonnull String name) {
		IRequestContext rci = UIContext.getRequestContext();
		Cookie[] car = rci.getRequestResponse().getCookies();
		if(car == null || car.length == 0)
			return null;

		for(Cookie c : car) {
			if(c.getName().equals(name)) {
				return c;
			}
		}
		return null;
	}

	@Nullable
	static public String findCookieValue(@Nonnull String name) {
		Cookie c = findCookie(name);
		return c == null ? null : c.getValue();
	}

	/**
	 * Set a new or overwrite an existing cookie.
	 *
	 * @param name
	 * @param value
	 * @param maxage	Max age, in seconds.
	 */
	static public void setCookie(@Nonnull String name, String value, int maxage) {
		IRequestContext rci = UIContext.getRequestContext();
		Cookie k = new Cookie(name, value);
		k.setMaxAge(maxage);
		k.setPath("/" + rci.getRequestResponse().getWebappContext());
		rci.getRequestResponse().addCookie(k);
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
	public interface IPerNode {
		/** When this object instance is returned by the before(NodeBase) method we SKIP the downwards traversal. */
		Object SKIP = new Object();

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
		Object before(@Nonnull NodeBase n) throws Exception;

		/**
		 * Called when all child nodes of the specified node have been traversed. When this returns a non-null
		 * value this will terminate the tree walk and return that value to the called of {@link DomUtil#walkTree(NodeBase, IPerNode)}.
		 * @param n
		 * @return
		 * @throws Exception
		 */
		Object after(@Nonnull NodeBase n) throws Exception;
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
			NodeContainer nc = (NodeContainer) root;
			for(int i = 0, len = nc.getChildCount(); i < len; i++) {
				NodeBase ch = nc.getChild(i);
				v = walkTree(ch, handler);
				if(v != null)
					return v;
			}
		}
		return handler.after(root);
	}

	/**
	 * This walks the tree, but ignores delegation to make sure that all nodes
	 * are reached.
	 */
	static public Object walkTreeUndelegated(NodeBase root, IPerNode handler) throws Exception {
		if(root == null)
			return null;
		Object v = handler.before(root);
		if(v == IPerNode.SKIP)
			return null;
		if(v != null)
			return v;
		if(root instanceof NodeContainer) {
			NodeContainer nc = (NodeContainer) root;
			for(NodeBase ch : new ArrayList<>(nc.internalGetChildren())) {
				//System.out.println(" >>> child " + ch);
				v = walkTreeUndelegated(ch, handler);
				if(v != null)
					return v;
			}
		}
		return handler.after(root);
	}

	/**
	 * This clears the 'modified' flag for all nodes in the subtree that implement {@link IHasModifiedIndication}.
	 * @param root        The subtree to traverse
	 */
	static public void clearModifiedFlag(NodeBase root) {
		try {
			walkTree(root, new IPerNode() {
				@Override
				public Object before(NodeBase n) throws Exception {
					if(n instanceof IHasModifiedIndication)
						((IHasModifiedIndication) n).setModified(false);
					return null;
				}

				@Override
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
				@Override
				public Object before(NodeBase n) throws Exception {
					if(n instanceof IHasModifiedIndication) {
						if(((IHasModifiedIndication) n).isModified())
							return Boolean.TRUE;
					}
					if(n instanceof IUserInputModifiedFence)
						return SKIP;
					return null;
				}

				@Override
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
			n = (NodeBase) n.findParent(IUserInputModifiedFence.class);
		}
	}

	/**
	 * Use StringTool.isBlank(String s) instead.
	 * <pre>
	 * Checks if string is blank.
	 * </pre>
	 * @param s String to be validated.
	 * @return true if it is blank, false otherwise.
	 *
	 */
	@Deprecated
	static public boolean isBlank(String s) {
		return s == null || s.trim().length() == 0;
	}

	static public boolean isRelativeURL(String in) {
		if(in == null)
			return false;
		return !in.startsWith("http:") && !in.startsWith("https:") && !in.startsWith("/");
	}

	/**
	 * EXPENSIVE - USE WITH CARE
	 * Check if first primitive type paramater is equal to some from others.
	 * Use only for primitive types and enums, for other complex types use {@link MetaManager#areObjectsEqual(Object, Object, ClassMetaModel)}.
	 *
	 * @param value
	 * @param values
	 * @return
	 */
	static public <T> boolean isIn(T value, T... values) {
		for(T item : values) {
			if(item.equals(value)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This opens a new DomUI page, immediately creating a session for it.
	 * @param targetClass
	 * @param targetParameters
	 * @param newWindowParameters
	 * @return
	 */
	@Nonnull
	static public String createOpenWindowJS(@Nonnull Class<?> targetClass, @Nullable IPageParameters targetParameters, @Nullable WindowParameters newWindowParameters) {
		//-- We need a NEW window session. Create it,
		RequestContextImpl ctx = (RequestContextImpl) UIContext.getRequestContext();
		WindowSession cm = ctx.getSession().createWindowSession();

		//-- Send a special JAVASCRIPT open command, containing the shtuff.
		StringBuilder sb = new StringBuilder();
		sb.append(ctx.getRelativePath(targetClass.getName()));
		sb.append(".ui?");
		StringTool.encodeURLEncoded(sb, Constants.PARAM_CONVERSATION_ID);
		sb.append('=');
		sb.append(cm.getWindowID());
		sb.append(".x");
		if(targetParameters != null)
			DomUtil.addUrlParameters(sb, targetParameters, false);
		return createOpenWindowJS(sb.toString(), newWindowParameters);
	}

	@Nonnull
	/**
	 * create a postUrlJS command where all pageparameters are put in a json collection.
	 */
	static public String createPostURLJS(@Nonnull String url, @Nonnull IPageParameters pageParameters) {
		StringBuilder sb = new StringBuilder();
		sb.append("DomUI.postURL('");
		sb.append(url);
		sb.append("','");
		sb.append("window").append(DomUtil.generateGUID());
		sb.append("', {");
		int i = 0;
		for(String parameterName : pageParameters.getParameterNames()) {
			if(i++ > 0) {
				sb.append(',');
			}
			sb.append(parameterName).append(":");
			//om narigheid te voorkomen
			StringTool.strToJavascriptString(sb, pageParameters.getString(parameterName), true);
		}
		sb.append("});");
		return sb.toString();
	}

	/**
	 * Create a postUrlJS command where all pageparameters are put in a json collection.
	 * Special treatment is done for parameter that defines array of values.
	 *
	 * @param url
	 * @param pageParameters
	 * @param arrayParamName  Name of parameter that is used for string array value, it is always single one.
	 * @param useSingleQuotes When T we use single quotes to escape, otherwise we use double quotes.
	 * @param useBlankTarget  When T, we do POST on _blank target, otherwise it navigates the same screen.
	 * @return
	 */
	@Nonnull
	static public String createPostWithArrayURLJS(@Nonnull String url, @Nonnull IPageParameters pageParameters, @Nonnull String arrayParamName, boolean useSingleQuotes, boolean useBlankTarget) {
		char quotes = useSingleQuotes ? '\'' : '"';
		StringBuilder sb = new StringBuilder();
		sb.append("DomUI.postURL(").append(quotes);
		sb.append(url);
		sb.append(quotes).append(",").append(quotes);
		sb.append("window").append(DomUtil.generateGUID());
		sb.append(quotes).append(", {");
		int i = 0;
		for(String parameterName : pageParameters.getParameterNames()) {
			if(!parameterName.equals(arrayParamName)) {
				if(i++ > 0) {
					sb.append(',');
				}
				sb.append(parameterName).append(":");
				StringTool.strToJavascriptString(sb, pageParameters.getString(parameterName), !useSingleQuotes);
			}
		}
		sb.append(',');
		StringBuilder values = new StringBuilder();
		for(String value : pageParameters.getStringArray(arrayParamName)) {
			if(values.length() > 0) {
				values.append(",");
			}
			StringTool.strToJavascriptString(values, value, !useSingleQuotes);
		}
		sb.append(arrayParamName).append(":").append("[").append(values).append("]");
		sb.append("} ");
		if(useBlankTarget) {
			sb.append(", ").append(quotes).append("_blank").append(quotes);
		}
		sb.append(");");
		return sb.toString();
	}

	@Nonnull
	static public String createOpenWindowJS(@Nonnull String url, @Nullable WindowParameters newWindowParameters, boolean useSingleQuotes) {
		char quotes = useSingleQuotes ? '\'' : '"';
		//-- Send a special JAVASCRIPT open command, containing the stuff.
		StringBuilder sb = new StringBuilder();
		sb.append("DomUI.openWindow(").append(quotes);
		sb.append(url);
		sb.append(quotes).append(",").append(quotes);
		String name = null;
		if(newWindowParameters != null)
			name = newWindowParameters.getName();
		if(isBlank(name)) {
			name = "window" + DomUtil.generateGUID();
		}
		sb.append(name);
		sb.append(quotes).append(",").append(quotes);

		if(newWindowParameters == null) {
			// separator must be comma otherwise it wont work in IE (call 27348)
			sb.append("resizable=yes,scrollbars=yes,toolbar=no,location=no,directories=no,status=yes,menubar=yes,copyhistory=no");
		} else {
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
		}
		sb.append(quotes).append(");");
		return sb.toString();
	}

	@Nonnull
	static public String createOpenWindowJS(@Nonnull String url, @Nullable WindowParameters newWindowParameters) {
		return createOpenWindowJS(url, newWindowParameters, true);
	}

	public static boolean isWhitespace(char c) {
		return c == '\u00a0' || Character.isWhitespace(c);

	}

	public static StackTraceElement[] getTracepoint() {
		try {
			throw new Exception();
		} catch(Exception x) {
			return x.getStackTrace();
		}
	}


	static private String m_lorem;

	/**
	 * Return a large string containing lorum ipsum text, for testing purposes.
	 * @return
	 * @throws Exception
	 */
	static public String getLorem() throws Exception {
		if(null == m_lorem) {
			InputStream is = DomUtil.class.getResourceAsStream("lorem.txt");
			try {
				m_lorem = FileTool.readStreamAsString(is, "utf-8");
			} finally {
				try {
					is.close();
				} catch(Exception x) {
				}
			}
		}
		return m_lorem;
	}

	/**
	 * Util can be used to check if list contains item that has equal Long Id as specified one, while instanies itself does not need to be equal.
	 * @param <T>
	 * @param set
	 * @param lookingFor
	 * @return
	 */
	public static <V, T extends IIdentifyable<V>> boolean containsLongIdentifyable(@Nonnull Collection<T> set, @Nonnull T lookingFor) {
		V id = lookingFor.getId();
		if(null == id)
			throw new IllegalStateException(lookingFor + ": id is null");
		for(T member : set) {
			V mid = member.getId();
			if(null != mid && mid.equals(id))
				return true;
		}
		return false;
	}

	/**
	 * Util that returns index of member in specified list that has same Long Id as specified <I>member</I>.
	 * @param <T>
	 * @param list
	 * @param member
	 * @return -1 if <I>member</I> object Long Id is not found in specified <I>list</I>, otherwise returns found index.
	 */
	public static <V, T extends IIdentifyable<V>> int indexOfLongIdentifyable(@Nonnull List<T> list, @Nonnull T lookingFor) {
		if(list == null)                    // jal 20120424 Bad, should be removed.
			return -1;

		V id = lookingFor.getId();
		if(null == id)
			throw new IllegalStateException(lookingFor + ": id is null");
		for(int i = list.size(); --i >= 0; ) {
			V mid = list.get(i).getId();
			if(null != mid && mid.equals(id))
				return i;
		}
		return -1;
	}

	/**
	 * Add item to mergeSource if it is not already contained.
	 * @param <T>
	 * @param mergeSource
	 * @param item
	 * @return (not)appended mergeSource
	 */
	@Nonnull
	public static <V, T extends IIdentifyable<V>> List<T> merge(@Nonnull List<T> mergeSource, @Nonnull T item) {
		if(!containsLongIdentifyable(mergeSource, item)) {
			if(mergeSource == Collections.EMPTY_LIST) {
				mergeSource = new ArrayList<T>();
			}
			mergeSource.add(item);
		}
		return mergeSource;
	}

	/**
	 * Appends non contained items from toJoinItems into mergeSource.
	 * Uses linear search, not suitable for large lists.
	 * @param <T>
	 * @param mergeSource
	 * @param toJoinItems
	 * @return (not)appended mergeSource
	 */
	public static <V, T extends IIdentifyable<V>> List<T> merge(@Nonnull List<T> mergeSource, @Nonnull List<T> toJoinItems) {
		for(@Nonnull T item : toJoinItems) {
			mergeSource = merge(mergeSource, item);
		}
		return mergeSource;
	}

	/**
	 * Util that returns T if <I>lookingFor</I> object is contained in specified <I>array</I>
	 *
	 * @param <T>
	 * @param array
	 * @param lookingFor
	 * @return
	 */
	public static <T> boolean contains(T[] array, T lookingFor) {
		return indexOf(array, lookingFor) != -1;
	}

	/**
	 * Util that returns index of <I>lookingFor</I> object inside specified <I>array</I>
	 *
	 * @param <T>
	 * @param array
	 * @param lookingFor
	 * @return -1 if <I>lookingFor</I> object is not found in specified <I>array</I>, otherwise returns its index
	 */
	public static <T> int indexOf(T[] array, T lookingFor) {
		if(array == null) {
			return -1;
		}
		for(int i = 0; i < array.length; i++) {
			if(isEqual(array[i], lookingFor)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns specified session attribute value if exists.
	 * If specified, it clears value to support easier 'one time purpose actions'.
	 * Must be called within valid request UI context.
	 *
	 * @param attribute
	 * @param doReset if T attribute value is set to null after reading.
	 * @return
	 */
	@Nullable
	public static Object getSessionAttribute(@Nonnull String attribute, boolean doReset) {
		IRequestContext ctx = UIContext.getRequestContext();
		AppSession ses = ctx.getSession();
		Object val = ses.getAttribute(attribute);
		if(doReset) {
			ses.setAttribute(attribute, null);
		}
		return val;
	}

	/**
	 * Set specified session attribute value. Attribute would be accessible until session expires.
	 * Must be called within valid request UI context.
	 *
	 * @param attribute
	 * @param value
	 */
	public static void setSessionAttribute(@Nonnull String attribute, @Nullable Object value) {
		IRequestContext ctx = UIContext.getRequestContext();
		AppSession ses = ctx.getSession();
		ses.setAttribute(attribute, value);
	}

	/**
	 * Decode a {@link PropertyMetaModel#getComponentTypeHint()} hint.
	 * @param componentTypeHint
	 * @param string
	 * @return
	 */
	@Nullable
	public static String getHintValue(String componentTypeHint, String name) {
		if(null == componentTypeHint)
			return null;

		name = name.toLowerCase();
		int l = name.length();
		String[] frags = componentTypeHint.split(";");
		for(String frag : frags) {
			if(frag.toLowerCase().startsWith(name)) {
				if(frag.length() == l) { // It is just name, without value
					return ""; // Value is empty string, it does exist
				}
				char c = frag.charAt(l);
				if(c == '=') {
					return frag.substring(l + 1);
				}
			}
		}
		return null;
	}

	@Nonnull
	public static String getComponentDetails(NodeBase n) {
		if(null == n)
			return "null";
		return n.getComponentInfo();
	}

	public static void main(final String[] args) {
		String html = "<p>This is <i>just</i> some html with<br>a new line <b>and a bold tag</b>";
		String uns = htmlRemoveUnsafe(html);
		System.out.println("uns=" + uns);
	}

	public static @Nonnull
	List<UIMessage> addSingleShotMessage(@Nonnull NodeBase node, @Nonnull UIMessage message) {
		WindowSession ws = node.getPage().getConversation().getWindowSession();
		List<UIMessage> msgl = null;
		Object stored = ws.getAttribute(UIGoto.SINGLESHOT_MESSAGE);
		if(stored != null && stored instanceof List<?>) {
			msgl = (List<UIMessage>) stored;
		} else {
			msgl = new ArrayList<UIMessage>(1);
		}

		msgl.add(message);
		ws.setAttribute(UIGoto.SINGLESHOT_MESSAGE, msgl);
		return msgl;
	}

	/**
	 * Try to get some content text from this node, for displaying what the node "is".
	 * @param nc
	 * @return
	 */
	public static String calcNodeText(@Nonnull NodeContainer nc) {
		StringBuilder sb = new StringBuilder();
		calcNodeText(sb, nc);
		return sb.toString();
	}

	private static void calcNodeText(@Nonnull StringBuilder sb, @Nonnull NodeContainer nc) {
		for(NodeBase nb : nc) {
			if(nb instanceof TextNode) {
				String text = ((TextNode) nb).getText();
				if(text != null && !appendPartial(sb, text))
					return;
			} else {
				if(nb instanceof NodeContainer) {
					calcNodeText(sb, (NodeContainer) nb);
				}
			}
		}
	}

	public static void time(@Nonnull String what, @Nonnull IExecute exec) throws Exception {
		long ts = System.nanoTime();
		exec.execute();
		ts = System.nanoTime() - ts;
		System.out.println(what + " took " + StringTool.strNanoTime(ts));
	}

	private static boolean appendPartial(@Nonnull StringBuilder sb, @Nonnull String text) {
		int todo = 400 - sb.length();
		if(todo >= text.length()) {
			sb.append(text);
			return true;
		} else if(todo > 0) {
			sb.append(text.substring(0, todo));
		}
		return false;
	}

	/**
	 * Compares two nullable objects. If one is null and other is not, returns 1 or -1.
	 * If both are null returns 0, if both are non null, and compare function is set, it returns result of compare, otherwise it returns 0.
	 *
	 * @param o1
	 * @param o2
	 * @return
	 */
	public static <T> int compareNullable(@Nullable T o1, @Nullable T o2, @Nullable BiFunction<T, T, Integer> compare) {
		if(o1 == null) {
			return o2 == null ? 0 : 1;
		} else if(o2 == null) {
			return -1;
		}
		if(null == compare) {
			return 0;
		}
		return compare.apply(o1, o2).intValue();
	}

	/**
	 * Compares chain of specified first level functions (usually properties) of two nullable objects, while also properties are nullable ;)
	 * Limitation of this method is that all functions are of same result, usually getters of same field type.
	 * In case that compare fallback needs to be done on other type property, simply use fallback as separate call to this method with different arguments.
	 * If one specified objects 01 or 02 is null and other is not, returns 1 or -1.
	 * If both are null returns 0, if both are non null, function applies to both to get level 1 function results in chain, and then compareNullable is applied on both results until we find any of different values.
	 * At the end it returns null if whole chain of functions returns same results.
	 * In order to revers compare logic (desc to asc), just reverse o1 and o2 arguments ;)
	 *
	 * @param o1
	 * @param o2
	 * @param functions
	 * @param compare
	 * @param <T>
	 * @param <P>
	 * @return
	 */
	@SafeVarargs
	public static <T, P> int compareNullableOnFunctions(@Nullable T o1, @Nullable T o2, @Nonnull BiFunction<P, P, Integer> compare, @Nonnull Function<T, P>... functions) {
		if(o1 == null) {
			return o2 == null ? 0 : 1;
		} else if(o2 == null) {
			return -1;
		}
		for(Function<T, P> function : functions) {
			P prop1 = function.apply(o1);
			P prop2 = function.apply(o2);
			int result = compareNullable(prop1, prop2, compare);
			if(result != 0) {
				return result;
			}
		}
		return 0;
	}

	/**
	 * Set a Javascript action on the button which copies the text specified
	 * to the button when clicked. WARNING: copying text to the clipboard
	 * only works when directly invoked from a user action, which is
	 * why this is added as a Javascript onclick action.
	 */
	public static final void clipboardCopy(NodeBase button, String text) {
		StringBuilder sb = new StringBuilder();
		sb.append("WebUI.copyTextToClipboard(");
		StringTool.strToJavascriptString(sb, text, true);
		sb.append("); return false;");
		button.setOnClickJS(sb.toString());
	}
}
