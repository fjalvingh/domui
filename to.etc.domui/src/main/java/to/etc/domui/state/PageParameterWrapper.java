package to.etc.domui.state;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.trouble.MissingParameterException;
import to.etc.domui.trouble.MultipleParameterException;
import to.etc.domui.trouble.UnusableParameterException;
import to.etc.domui.util.DomUtil;
import to.etc.util.StringTool;
import to.etc.util.WrappedException;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-10-19.
 */
@NonNullByDefault
public class PageParameterWrapper implements IPageParameters {
	private IBasicParameterContainer m_container;

	public PageParameterWrapper(IBasicParameterContainer container) {
		m_container = container;
	}

	public IBasicParameterContainer getContainer() {
		return m_container;
	}

	/**
	 * Gets the value for the specified parameter name as untyped value.
	 * It is used internally for generic copying of params form one PageParameter to another.
	 */
	@Override
	@Nullable
	public Object getObject(@NonNull String name) {
		return m_container.getObject(name);
	}

	/**
	 * Return the number of parameter (names) in this instance.
	 */
	@Override
	public int size() {
		return m_container.size();
	}

	@Override
	public int getDataLength() {
		return getContainer().getDataLength();
	}

	/**
	 * Returns a single value for a parameter. The parameter must either be a single
	 * string, or must be a 1-size array.
	 */
	@Nullable
	protected String	getOne(String name) {
		Object v = getObject(name);
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
	protected String getOneNotNull(String name) {
		String v = getOne(name);
		if(null == v)
			throw new MissingParameterException(name);
		return v;
	}

	@Override
	public boolean hasParameter(String name) {
		return getObject(name) != null;
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

	@NonNull
	@Override
	public Long getLongW(String name, long df) {
		return Objects.requireNonNull(getLongW(name, Long.valueOf(df)));
	}

	@Nullable
	@Override
	public Long getLongW(String name, @Nullable Long df) {
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
	public String getString(String name, @Nullable String df) {
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
		Object var = getObject(name);
		if(null != var) {
			if(var instanceof String)
				return new String[]{(String) var};
			String[] ar = (String[]) var;
			if(ar.length >= 0)
				return ar;
		}
		return deflt;
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
			List<String> names = new ArrayList<String>(m_container.getParameterNames());		// Dup all keys
			Collections.sort(names);										// Sort alphabetically
			for(String name : names) {
				Object val = m_container.getObject(name);
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
	public boolean isReadOnly() {
		return true;
	}

	@NonNull
	@Override
	public Set<String> getParameterNames() {
		return getContainer().getParameterNames();
	}

	@NonNull
	@Override
	public PageParameters getUnlockedCopy() {
		PageParameters clone = new PageParameters(this);
		return clone;
	}

	@Nullable
	@Override
	public String getUrlContextString() {
		return getContainer().getUrlContextString();
	}

	@Override
	public String toString() {
		//-- Must render explicitly now because array toString method does not print members
		StringBuilder sb = new StringBuilder();
		for(String parameterName : getParameterNames()) {
			Object value = getObject(parameterName);
			if(value instanceof String) {
				if(sb.length() > 0)
					sb.append("&");
				sb.append(parameterName).append('=').append(value);
			} else if(value instanceof String[]) {
				String[] vals = (String[]) value;
				for(String s : vals) {
					if(sb.length() > 0)
						sb.append("&");
					sb.append(parameterName).append('=').append(s);
				}
			} else {
				throw new IllegalStateException("Unexpected object in parameter map: " + value);
			}
		}
		return "Parameters: " + sb.toString();
	}

	/**
	 * Convert the parameters to a properly escaped URL string.
	 */
	public String toEscapedURL() {
		StringBuilder sb = new StringBuilder();
		for(String name : getParameterNames()) {
			Object value = getObject(name);
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

	@Override
	public boolean equals(@Nullable Object obj) {
		if(! (obj instanceof IPageParameters))
			return false;

		IPageParameters a = (IPageParameters) obj;
		if(a.size() != size())
			return false;

		Set<String> parameterNames = getParameterNames();
		parameterNames.addAll(a.getParameterNames());
		for(String name : parameterNames) {
			Object oval = a.getObject(name);
			Object val = getObject(name);
			if(!compValues(oval, val))
				return false;
		}
		return Objects.equals(getUrlContextString(), a.getUrlContextString());
	}

	private boolean compValues(@Nullable Object oval, @Nullable Object val) {
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

}
