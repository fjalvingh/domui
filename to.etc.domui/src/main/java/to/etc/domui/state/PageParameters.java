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
package to.etc.domui.state;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.converter.CompoundKeyConverter;
import to.etc.domui.converter.ConverterRegistry;
import to.etc.domui.converter.IConverter;
import to.etc.domui.server.IRequestContext;
import to.etc.domui.trouble.MissingParameterException;
import to.etc.domui.trouble.MultipleParameterException;
import to.etc.domui.trouble.UnusableParameterException;
import to.etc.domui.util.DomUtil;
import to.etc.util.StringTool;
import to.etc.util.WrappedException;
import to.etc.webapp.query.IIdentifyable;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Encapsulates parameters for a page. All parameters must be presentable in URL form,
 * i.e. they must be renderable as part of a GET or POST. A page request formed by a
 * Page class and a PageParameters class is bookmarkable.
 * This is a mutable object.
 * A PageParameters object can be rendered on an URL by using {@link DomUtil#addUrlParameters(StringBuilder, PageParameters, boolean)}.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 22, 2008
 */
public class PageParameters implements IPageParameters, Serializable {
	/**
	 * Contains either String or String[], maps parameter name to either one or an array of values of that parameter.
	 */
	private Map<String, Object> m_map = new HashMap<String, Object>();

	/** When set no data can be changed */
	private boolean m_readOnly = false;

	/** The approximate length of this parameters instance when rendered on an URL. */
	private int m_dataLength;

	@NonNull
	private String m_urlContextString = "";

	/**
	 * Create an empty PageParameters.
	 */
	public PageParameters() {}

	/**
	 * Create page parameters and fill with the initial set defined in the argument list. For details of
	 * what can be passed see {@link #addParameters(Object...)}.
	 */
	public PageParameters(Object... list) {
		try {
			addParameters(list);
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
		//
		//
		//		if((list.length & 0x1) != 0)
		//			throw new IllegalStateException("Incorrect parameter count: must be an even number of objects, each [string], [object]");
		//		for(int i = 0; i < list.length; i += 2) {
		//			Object a = list[i];
		//			if(!(a instanceof String))
		//				throw new IllegalStateException("Expecting a 'String' as parameter " + i + ", but got a '" + a + "'");
		//			Object b = list[i + 1];
		//			addParameter((String) a, b);
		//		}
	}

	@NonNull
	@Override
	public PageParameters getUnlockedCopy() {
		PageParameters clone = new PageParameters();
		clone.m_urlContextString = m_urlContextString;
		for(Map.Entry<String, Object> entry : m_map.entrySet()) {
			clone.addParameter(entry.getKey(), entry.getValue());
		}
		return clone;
	}

	public void setReadOnly() {
		m_readOnly = true;
	}

	private void writeable() {
		if(m_readOnly)
			throw new IllegalStateException("This object is readonly and cannot be changed.");
	}

	/**
	 * Primitive, only allowing String value.
	 */
	private void setParameter(String name, String value) {
		increaseLength(value);
		Object o = m_map.put(name, value);
		decreaseLength(o);
	}

	private void decreaseLength(@Nullable Object o) {
		if(o instanceof String) {
			m_dataLength -= ((String) o).length() + 2;
		} else if(o instanceof String[]) {
			for(String s : (String[]) o) {
				decreaseLength(s);
			}
		}
	}

	private void increaseLength(@Nullable String value) {
		if(null == value)
			return;
		m_dataLength += (value.length() + 2);
	}

	/**
	 * Primitive, only allowing string array.
	 */
	private void setParameter(String name, String[] values) {
		if(null != values) {
			for(String s : values)
				increaseLength(s);
		}
		Object o = m_map.put(name, values);
		decreaseLength(o);
	}

	/**
	 * Returns a single value for a parameter. The parameter must either be a single
	 * string, or must be a 1-size array.
	 */
	@Nullable
	private String	getOne(String name) {
		Object v = m_map.get(name);
		if(null == v)
			return null;
		if(v instanceof String)
			return (String) v;
		String[] ar = (String[]) v;
		if(ar.length == 0)		// Questionable: allow 0-size array and treat as empty; rationale: this parameter would not actually occur on the url.
			return null;
		if(ar.length == 1)
			return ar[0];
		throw new MultipleParameterException(name); // There can be only oneeeeee.. </highlander>
	}

	/**
	 * Throws MissingParameterException when the parameter can not be found.
	 */
	@NonNull
	private String getOneNotNull(String name) {
		String v = getOne(name);
		if(null == v)
			throw new MissingParameterException(name);
		return v;
	}

	/**
	 * Add parameters. This accepts multiple formats that can all be mixed. Each actual parameter always is a
	 * name, value pair. The simplest way to use this is to specify a list of strings in pairs where the first
	 * string in the pair is the key and the second one is the value. You can also substitute an object instance
	 * for the value; if this object instance represents some persistent entity or implements {@link IIdentifyable<Long>}
	 * the primary key for the object will be rendered as the value, otherwise it will be rendered as a tostring.
	 * You can also specify a single object in the location for the next key; in this case both key and value will
	 * be determined from this object; it must be some persistent object which knows it's key field.
	 */
	public void addParameters(Object... plist) throws Exception {
		writeable();
		int ix = 0;
		int len = plist.length;
		while(ix < len) {
			Object k = plist[ix++]; // Consume the key
			if(k instanceof String) {
				//-- Must be [key, value] pair. Append it. The value can be array.
				if(ix >= len)
					throw new IllegalStateException("Missing value for key string '" + k + "'");
				internalAdd((String) k, plist[ix++]);
			} else if(k != null) {
				//-- Non-string @ key position: this must be a persistent class.
				ClassMetaModel cmm = MetaManager.findClassMeta(k.getClass());
				if(! cmm.isPersistentClass())
					throw new IllegalStateException("Instance of "+k.getClass()+" is not a persistent class");
				//-- Get the PK attribute of the persistent class;
				PropertyMetaModel< ? > pkpm = cmm.getPrimaryKey();
				if(pkpm == null)
					throw new IllegalStateException("The instance of " + k.getClass() + " passed has no primary key defined");
				Object key = pkpm.getValue(k);
				if(key == null)
					throw new IllegalStateException("The instance of " + k.getClass() + " passed has a null primary key");

				String	pk = cmm.getClassBundle().getString("pk.name");
				if(pk == null) {
					pk = k.getClass().getName();
					pk = pk.substring(pk.lastIndexOf('.') + 1);
				}
				setParameter(pk, String.valueOf(key));
			}
		}
	}

	/**
	 * Add string, value pair. Existing parameter is overwritten.
	 */
	private void internalAdd(String k, Object o) throws Exception {
		writeable();
		if(o == null)
			return;

		if(o instanceof IIdentifyable< ? >) {
			setParameter(k, String.valueOf(((IIdentifyable< ? >) o).getId()));
			return;
		}

		if(o instanceof String[]) {
			String[] ar = (String[]) o;
			if(ar.length > 0)
				setParameter(k, ar);
			return;
		}

		String keyval = null;
		ClassMetaModel cmm = MetaManager.findClassMeta(o.getClass());
		if(cmm.isPersistentClass()) {
			//-- Get the PK attribute of the persistent class;
			PropertyMetaModel< ? > pkpm = cmm.getPrimaryKey();
			if(pkpm != null) {
				Object key = pkpm.getValue(o);
				if(key == null)
					throw new IllegalStateException("The instance of " + o.getClass() + " passed has a null primary key");
				keyval = CompoundKeyConverter.INSTANCE.marshal(key);
			}
		}else if (o instanceof Date){
			//-- Special handling for Date url parameters
			IConverter<Date> dateConv = ConverterRegistry.findURLConverter(Date.class);
			if (null != dateConv){
				keyval = dateConv.convertObjectToString(Locale.getDefault(), (Date) o);
			}
		}

		if(keyval == null)
			keyval = String.valueOf(o);
		setParameter(k, keyval);
	}

	/**
	 * Adds a parameter with the specified name. When a parameter with the same name already exists,
	 * the existing value will be overwritten with the specified value.
	 *
	 * @param name, the parameter name.
	 * @param value, the (new) value.
	 */
	public void addParameter(String name, Object value) {
		writeable();

		//-- Convert the value to a string;
		String s;
		if(value == null)
			s = "null";
		else if(value instanceof String)
			s = (String) value;
		else if(value instanceof Number) {
			s = value.toString();
		} else if(value instanceof Boolean) {
			s = value.toString();
		} else if(value instanceof String[]) {
			setParameter(name, (String[]) value);
			return;
		} else
			throw new IllegalStateException("Cannot convert a " + value.getClass() + " to an URL parameter yet - parameter converters not implemented yet");
		setParameter(name, s);
	}

	/**
	 * Removes the parameter with specified name entirely from the map.
	 *
	 * @param name the name of the parameter to be removed.
	 */
	public void removeParameter(String name) {
		writeable();
		Object v = m_map.remove(name);
		decreaseLength(v);
	}

	@Override
	public boolean hasParameter(String name) {
		return m_map.containsKey(name);
	}

	/**
	 * Return the number of parameter (names) in this instance.
	 */
	@Override
	public int size() {
		return m_map.size();
	}

	@Override
	public int getInt(String name) {
		String v = getOneNotNull(name);
		try {
			return Integer.parseInt(v);
		} catch(Exception x) {
		}
		throw new UnusableParameterException(name, "int", v);
	}

	@Override
	public int getInt(String name, int df) {
		String v = getOne(name);
		if(null != v && (v = v.trim()).length() > 0) {
			try {
				return Integer.parseInt(v);
			} catch(Exception x) {
				throw new UnusableParameterException(name, "int", v);
			}
		}
		return df;
	}

	@Override
	public long getLong(String name) {
		String v = getOneNotNull(name);
		try {
			return Long.parseLong(v);
		} catch(Exception x) {
		}
		throw new UnusableParameterException(name, "long", v);
	}

	@Override
	public long getLong(String name, long df) {
		String v = getOne(name);
		if(null != v && (v = v.trim()).length() > 0) {
			try {
				return Long.parseLong(v);
			} catch(Exception x) {
				throw new UnusableParameterException(name, "long", v);
			}
		}
		return df;
	}

	@Override
	public boolean getBoolean(String name) {
		String v = getOneNotNull(name);
		try {
			return Boolean.parseBoolean(v);
		} catch(Exception x) {}
		throw new UnusableParameterException(name, "boolean", v);
	}

	@Override
	public boolean getBoolean(String name, boolean df) {
		String v = getOne(name);
		if(null != v && (v = v.trim()).length() > 0) {
			try {
				v = v.toLowerCase();
				if(v.startsWith("y"))
					return true;
				else if(v.startsWith("n"))
					return false;

				return Boolean.parseBoolean(v);
			} catch(Exception x) {
				throw new UnusableParameterException(name, "boolean", v);
			}
		}
		return df;
	}

	@Override
	public Long getLongW(String name) {
		String v = getOneNotNull(name);
		try {
			return Long.decode(v);
		} catch(Exception x) {
		}
		throw new UnusableParameterException(name, "long", v);
	}

	@Override
	public Long getLongW(String name, long df) {
		return getLongW(name, Long.valueOf(df));
	}

	@Override
	public Long getLongW(String name, Long df) {
		String v = getOne(name);
		if(null != v && (v = v.trim()).length() > 0) {
			try {
				return Long.decode(v);
			} catch(Exception x) {
				throw new UnusableParameterException(name, "long", v);
			}
		}
		return df;
	}

	@Override
	@NonNull
	public String getString(String name) {
		return getOneNotNull(name);
	}

	@Override
	@Nullable
	public String getString(String name, String df) {
		String v = getOne(name);
		return v == null ? df : v;
	}

	@Override
	@NonNull
	public String[] getStringArray(@NonNull String name) {
		String[] arr = getStringArray(name, null);
		if(null == arr)
			throw new MissingParameterException(name);
		return arr;
	}

	@Override
	@Nullable
	public String[] getStringArray(@NonNull String name, @Nullable String[] deflt) {
		Object var = m_map.get(name);
		if(null != var) {
			if(var instanceof String)
				return new String[]{(String) var};
			String[] ar = (String[]) var;
			if(ar.length >= 0)
				return ar;
		}
		return deflt;
	}

	/**
	 * Gets the value for the specified parameter name as untyped value.
	 * It is used internally for generic copying of params form one PageParameter to another.
	 */
	@Nullable
	public Object getObject(String name) {
		return m_map.get(name);
	}

	public void putObject(@NonNull String name, @Nullable Object value) {
		if(null == value)
			m_map.remove(name);
		else
			m_map.put(name, value);
	}

	@NonNull
	@Override public String getUrlContextString() {
		return m_urlContextString;
	}

	public void setUrlContextString(@Nullable String urlContextString) {
		m_urlContextString = urlContextString == null ? "" : urlContextString;
	}

	/**
	 * Create this from an actual request. This does not add any parameter that starts with _ or $.
	 */
	@NonNull
	static public PageParameters createFrom(IRequestContext ctx) {
		PageParameters pp = new PageParameters();
		for(String name : ctx.getParameterNames()) {
			if(name.length() > 0) {
				char c = name.charAt(0);
				if(c != '_' && c != '$' && !name.startsWith("webui")) {
					String[] par = ctx.getParameters(name);
					if(par != null && par.length > 0) {
						if(par.length == 1)
							pp.setParameter(name, par[0]); // Add as single string
						else
							pp.setParameter(name, par); // Add as string[]0
					}
				}
			}
		}
		pp.setUrlContextString(ctx.getUrlContextString());
		return pp;
	}

	static public PageParameters createFromAll(IRequestContext ctx) {
		PageParameters pp = new PageParameters();
		for(String name : ctx.getParameterNames()) {
			if(name.length() > 0) {
				char c = name.charAt(0);
				if(!name.startsWith("webui")) {
					String[] par = ctx.getParameters(name);
					if(par != null && par.length > 0) {
						if(par.length == 1)
							pp.setParameter(name, par[0]); // Add as single string
						else
							pp.setParameter(name, par); // Add as string[]0
					}
				}
			}
		}
		pp.setUrlContextString(ctx.getUrlContextString());
		return pp;
	}

	static public PageParameters copyFrom(IPageParameters ctx) {
		PageParameters pp = new PageParameters();
		for(String name : ctx.getParameterNames()) {
			if(name.length() > 0) {
				char c = name.charAt(0);
				String[] par = ctx.getStringArray(name, null);
				if(par != null && par.length > 0) {
					if(par.length == 1)
						pp.setParameter(name, par[0]); // Add as single string
					else
						pp.setParameter(name, par); // Add as string[]0
				}
			}
		}
		pp.setUrlContextString(ctx.getUrlContextString());
		return pp;
	}

	/**
	 * Create this from an string representation of params. This is used as utility for manipulation of data that stores params as strings.
	 */
	@NonNull
	static public PageParameters createFromEncodedUrlString(@NonNull String paramsAsString) {
		PageParameters pp = new PageParameters();
		if(DomUtil.isBlank(paramsAsString)) {
			return pp;
		}
		paramsAsString = paramsAsString.trim();
		//happens that params starts with ?, and it should be removed
		if(paramsAsString.startsWith("?")) {
			paramsAsString = paramsAsString.substring(1);
		}
		if(DomUtil.isBlank(paramsAsString)) {
			return pp;
		}
		String asDecoded = StringTool.decodeURLEncoded(paramsAsString);
		String[] splits = asDecoded.split("&");
		for(String nameValue : splits) {
			char c = nameValue.charAt(0);
			if(c != '_' && c != '$' && !nameValue.startsWith("webui")) {
				String[] parts = nameValue.split("=");
				if(parts.length > 2 || parts.length == 0) {
					throw new IllegalArgumentException("Expected name=value pair, but found:" + nameValue);
				}
				if(parts.length == 2) {
					pp.m_map.put(parts[0], parts[1]); // Add as single string
				}
				//empty params are ignored but with no exception
			}
		}
		return pp;
	}

	@Override
	public String toString() {
		//-- Must render explicitly now because array toString method does not print members
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, Object> me : m_map.entrySet()) {
			if(me.getValue() instanceof String) {
				if(sb.length() > 0)
					sb.append("&");
				sb.append(me.getKey()).append('=').append(me.getValue());
			} else {
				String[] vals = (String[]) me.getValue();
				for(String s : vals) {
					if(sb.length() > 0)
						sb.append("&");
					sb.append(me.getKey()).append('=').append(s);
				}
			}
		}
		return "Parameters: " + sb.toString();
	}

	@Override
	@NonNull
	public String[] getParameterNames() {
		return m_map.keySet().toArray(new String[m_map.size()]);
	}

	/**
	 * @see to.etc.domui.state.IPageParameters#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(! (obj instanceof PageParameters))
			return false;

		PageParameters a = (PageParameters) obj;
		if(a.m_map.size() != m_map.size()) // Maps differ -> done
			return false;

		for(String key : m_map.keySet()) {
			Object oval = a.m_map.get(key);
			Object val = m_map.get(key);
			if(!compValues(oval, val))
				return false;
		}
		return Objects.equals(getUrlContextString(), a.getUrlContextString());
	}

	private boolean compValues(Object oval, Object val) {
		if(oval instanceof String && val instanceof String) {
			return oval.equals(val);
		}
		if(oval instanceof String[] && val instanceof String[]) {
			String[] a = (String[]) oval;
			String[] b = (String[]) val;
			if(a.length != b.length)
				return false;
			//-- walk through the entire array, same order of members is not necessary to be equal
			for(String av : a) {
				boolean found = false;
				for(String bv : b) {
					if(DomUtil.isEqual(av, bv)) {
						found = true;
						break;
					}
				}
				if(!found)
					return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		throw new IllegalStateException("missing");
	}

	/**
	 * Apply changes to source.
	 * New values found in changes would be added to source, changed values found in changes would replace values in source.
	 * Params that are not found in changes would remian unchanged in source. So, this utility can not be used to remove items from source.
	 */
	public static void applyChanges(PageParameters source, PageParameters changes) {

		for(String name : changes.getParameterNames()) {
			if(source.hasParameter(name)) {
				source.removeParameter(name);
			}
			Object object = changes.getObject(name);
			if(null != object)
				source.addParameter(name, object);
		}
	}

	@Override
	@NonNull
	public String calculateHashString() {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch(NoSuchAlgorithmException x) {
			throw new RuntimeException("MISSING MANDATORY SECURITY DIGEST PROVIDER MD5: " + x.getMessage());
		}

		//-- Sort all names.
		try {
			List<String> names = new ArrayList<String>(m_map.keySet());		// Dup all keys
			Collections.sort(names);										// Sort alphabetically
			for(String name : names) {
				Object val = m_map.get(name);
				if(null != val) {
					if(val instanceof String[]) {
						String[] allv = (String[]) val;
						Arrays.sort(allv);									// Sort all values alphabetically.
						for(String s : allv) {
							md.update(s.getBytes("utf-8"));
							md.update((byte) 0xa);
						}
					} else {
						md.update(val.toString().getBytes("utf-8"));
						md.update((byte) 0xa);
					}
				}
			}
			String cxs = getUrlContextString();
			if(null != cxs)
				md.update(cxs.getBytes("utf-8"));
		} catch(UnsupportedEncodingException x) {
			throw WrappedException.wrap(x);									// Cannot happen.
		}
		return StringTool.toHex(md.digest());
	}

	@Override
	public int getDataLength() {
		return m_dataLength;
	}

	@Override
	public boolean isReadOnly() {
		return m_readOnly;
	}

	/**
	 * Decode a http query string into a PageParameters instance.
	 */
	@NonNull
	static public PageParameters decodeParameters(@Nullable String query) {
		if(null == query)
			return new PageParameters();
		String[] indiar = query.split("&");
		Map<String, List<String>> map = new HashMap<>();
		for(String frag : indiar) {
			int pos = frag.indexOf('=');
			if(pos >= 0) {
				String name = frag.substring(0, pos).toLowerCase();
				String value = frag.substring(pos + 1);
				name = StringTool.decodeURLEncoded(name);
				value = StringTool.decodeURLEncoded(value);

				map.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
			}
		}
	
		PageParameters pp = new PageParameters();
		for(Map.Entry<String, List<String>> me : map.entrySet()) {
			if(me.getValue().size() == 1) {
				pp.addParameter(me.getKey(), me.getValue().get(0));
			} else {
				pp.addParameter(me.getKey(), me.getValue().toArray(new String[0]));
			}
		}
		return pp;
	}

	/**
	 * Convert the parameters to a properly escaped URL string.
	 */
	public String toEscapedURL() {
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, Object> en : m_map.entrySet()) {
			String name = en.getKey();
			Object value = en.getValue();
			if(value instanceof List) {
				List<String> list = (List<String>) value;
				for(String s : list) {
					if(sb.length() > 0)
						sb.append('&');
					sb.append(StringTool.encodeURLEncoded(name));
					sb.append('=');
					if(null != s)
						sb.append(StringTool.encodeURLEncoded((String) s));
				}
			} else {
				if(sb.length() > 0)
					sb.append('&');
				sb.append(StringTool.encodeURLEncoded(name));
				sb.append('=');
				if(null != value)
					sb.append(StringTool.encodeURLEncoded((String) value));
			}
		}
		return sb.toString();
	}

}
