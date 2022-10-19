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
import to.etc.domui.server.BrowserVersion;
import to.etc.domui.util.DomUtil;
import to.etc.util.StringTool;
import to.etc.util.WrappedException;
import to.etc.webapp.query.IIdentifyable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Encapsulates parameters for a page. All parameters must be presentable in URL form,
 * i.e. they must be renderable as part of a GET or POST. A page request formed by a
 * Page class and a PageParameters class is bookmarkable.
 * This is a mutable object.
 * A PageParameters object can be rendered on an URL by using {@link DomUtil#addUrlParameters(StringBuilder, IPageParameters, boolean)}.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 22, 2008
 */
public class PageParameters extends PageParameterWrapper implements IPageParameters, Serializable {
	/** When set no data can be changed */
	private boolean m_readOnly = false;

	/**
	 * Create an empty PageParameters.
	 */
	public PageParameters() {
		super(new MapParameterContainer());
	}

	public PageParameters(IPageParameters source, Predicate<String> acceptName) {
		this();
		copyFrom(source, acceptName);
	}
	public PageParameters(IPageParameters source) {
		this();
		copyFrom(source, a -> true);
	}

	@Override
	public MapParameterContainer getContainer() {
		return (MapParameterContainer)super.getContainer();
	}

	/**
	 * Create page parameters and fill with the initial set defined in the argument list. For details of
	 * what can be passed see {@link #addParameters(Object...)}.
	 */
	public PageParameters(Object... list) {
		this();
		try {
			addParameters(list);
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Reading parameters											*/
	/*----------------------------------------------------------------------*/

	@Nullable
	@Override
	public String[] getParameterValues(@NonNull String name) {
		return getContainer().getParameterValues(name);
	}

	@Nullable
	public String[] setParameterValues(@NonNull String name, @Nullable String[] val) {
		return getContainer().setParameterValues(name, val);
	}

	@Override
	public boolean hasParameter(String name) {
		return getParameterValues(name) != null;
	}

	public void setReadOnly() {
		m_readOnly = true;
	}

	private void writeable() {
		if(m_readOnly)
			throw new IllegalStateException("This object is readonly and cannot be changed.");
	}

	/**
	 * Removes all parameter values.
	 */
	public void clearParameters() {
		getContainer().clear();

	}

	public void removeByName(Predicate<String> what) {
		for(String parameterName : getContainer().getParameterNames()) {
			if(what.test(parameterName)) {
				setParameterValues(parameterName, null);
			}
		}
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

				setOneParameterValue(pk, String.valueOf(key));
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
			setOneParameterValue(k, String.valueOf(((IIdentifyable< ? >) o).getId()));
			return;
		}

		if(o instanceof String[]) {
			String[] ar = (String[]) o;
			if(ar.length > 0)
				setParameterValues(k, ar);
			else
				setParameterValues(k, null);
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
		setOneParameterValue(k, keyval);
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
		} else if(value instanceof Enum) {
			s = ((Enum<?>) value).name();
		} else if(value instanceof String[]) {
			setParameterValues(name, (String[]) value);
			return;
		} else
			throw new IllegalStateException("Cannot convert a " + value.getClass() + " to an URL parameter yet - parameter converters not implemented yet");
		setOneParameterValue(name, s);
	}

	public PageParameters parameter(String name, Object value) {
		addParameter(name, value);
		return this;
	}

	/**
	 * Removes the parameter with specified name entirely from the map.
	 *
	 * @param name the name of the parameter to be removed.
	 */
	public void removeParameter(String name) {
		writeable();
		setParameterValues(name, null);
	}

	private void setOneParameterValue(String name, @Nullable String value) {
		if(null == value) {
			setParameterValues(name, null);
		} else {
			setParameterValues(name, new String[] {value});
		}
	}

	///**
	// * Create this from an actual request. This does not add any parameter that starts with _ or $.
	// */
	//@NonNull
	//static public PageParameters createFrom(IRequestContext ctx) {
	//	PageParameters pp = new PageParameters();
	//	for(String name : ctx.getParameterNames()) {
	//		if(name.length() > 0) {
	//			char c = name.charAt(0);
	//			if(c != '_' && c != '$' && !name.startsWith("webui")) {
	//				String[] par = ctx.getParameters(name);
	//				if(par != null && par.length > 0) {
	//					if(par.length == 1)
	//						pp.setParameter(name, par[0]); // Add as single string
	//					else
	//						pp.setParameter(name, par); // Add as string[]0
	//				}
	//			}
	//		}
	//	}
	//	pp.setUrlContextString(ctx.getUrlContextString());
	//	return pp;
	//}
	//
	//static public PageParameters createFromAll(IRequestContext ctx) {
	//	PageParameters pp = new PageParameters();
	//	for(String name : ctx.getParameterNames()) {
	//		if(name.length() > 0) {
	//			char c = name.charAt(0);
	//			if(!name.startsWith("webui")) {
	//				String[] par = ctx.getParameters(name);
	//				if(par != null && par.length > 0) {
	//					if(par.length == 1)
	//						pp.setParameter(name, par[0]); // Add as single string
	//					else
	//						pp.setParameter(name, par); // Add as string[]0
	//				}
	//			}
	//		}
	//	}
	//	pp.setUrlContextString(ctx.getUrlContextString());
	//	return pp;
	//}
	//
	//static public PageParameters copyFrom(IPageParameters ctx) {
	//	PageParameters pp = new PageParameters();
	//	for(String name : ctx.getParameterNames()) {
	//		if(name.length() > 0) {
	//			char c = name.charAt(0);
	//			String[] par = ctx.getStringArray(name, null);
	//			if(par != null && par.length > 0) {
	//				if(par.length == 1)
	//					pp.setParameter(name, par[0]); // Add as single string
	//				else
	//					pp.setParameter(name, par); // Add as string[]0
	//			}
	//		}
	//	}
	//	pp.setUrlContextString(ctx.getUrlContextString());
	//	return pp;
	//}
	//
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
		if(StringTool.isBlank(paramsAsString)) {
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
					pp.setOneParameterValue(parts[0], parts[1]); 						// Add as single string
				}
				//empty params are ignored but with no exception
			}
		}
		return pp;
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
			String[] object = changes.getParameterValues(name);
			if(null != object)
				source.addParameter(name, object);
		}
	}

	@Override
	public boolean isReadOnly() {
		return m_readOnly;
	}

	public void setUrlContextString(@Nullable String str) {
		getContainer().setUrlContextString(str);
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

	public void copyFrom(IPageParameters source, Predicate<String> acceptName) {
		setUrlContextString(source.getUrlContextString());
		for(String parameterName : source.getParameterNames()) {
			if(acceptName.test(parameterName)) {
				setParameterValues(parameterName, source.getParameterValues(parameterName));
				getContainer().setRawUnsafeParameterValues(parameterName, source.getRawUnsafeParameterValues(parameterName));
			}
		}

		if(source instanceof IPageParameters) {
			IPageParameters x = (IPageParameters) source;
			browserVersion(x.getBrowserVersion());
			inputPath(x.getInputPath());
			themeName(x.getThemeName());
		}
	}

	@NonNull
	static public PageParameters createFrom(IPageParameters old) {
		PageParameters pp = new PageParameters(old, name -> {
			char c = name.charAt(0);
			return c != '_' && c != '$' && !name.startsWith("webui");
		});
		return pp;
	}

	public PageParameters inputPath(@NonNull String inputPath) {
		writeable();
		getContainer().setInputPath(inputPath);
		return this;
	}

	public PageParameters browserVersion(@NonNull BrowserVersion browserVersion) {
		writeable();
		getContainer().setBrowserVersion(browserVersion);
		return this;
	}

	public PageParameters themeName(@Nullable String themeName) {
		writeable();
		getContainer().setThemeName(themeName);
		return this;
	}
}
