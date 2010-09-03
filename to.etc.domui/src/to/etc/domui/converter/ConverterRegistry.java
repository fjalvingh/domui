package to.etc.domui.converter;

import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.util.*;
import to.etc.webapp.nls.*;

/**
 * A repository of Converter instances.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 12, 2008
 */
public class ConverterRegistry {
	/** All converter instances for staticly accessed IConverters */
	static private Map<Class<? extends IConverter<?>>, IConverter<?>> m_converterMap = new HashMap<Class<? extends IConverter<?>>, IConverter<?>>();

	/** The list of registered factories. */
	static private List<IConverterFactory> m_factoryList = new ArrayList<IConverterFactory>();

	//	static private Map<Class< ? >, List<IConverterFactory>> m_factoryMap = new HashMap<Class< ? >, List<IConverterFactory>>();

	static private IConverterFactory m_defaultConverterFactory;

	static {
		setDefaultFactory(new DefaultConverterFactory());
		register(new DomainListConverterFactory()); // Accepts anything having domain value list
		register(new DateConverterFactory());
		register(new MoneyConverterFactory());
		register(new DoubleFactory()); // Low-level Double converters (numeric only)
		register(new EnumFactory()); // last-resort: Accepts generic enums without propertyMeta;
		register(new BooleanConverterFactory()); // last-resort: Accepts generic boolean without metadata (yes, no texts only)
	}

	/**
	 * Get an instance of a given converter type. Instances are cached and reused.
	 * @param clz
	 * @return
	 */
	static public synchronized <X, T extends IConverter<X>> T getConverterInstance(Class<T> clz) {
		T c = (T) m_converterMap.get(clz);
		if(c == null) {
			try {
				c = clz.newInstance();
			} catch(Exception x) {
				throw new IllegalStateException("Cannot instantiate converter " + clz + ": " + x, x);
			}
			m_converterMap.put(clz, c);
		}
		return c;
	}

	/**
	 * Convert a String value to some object, using the specified converter.
	 * @param clz
	 * @param loc
	 * @param in
	 * @return
	 * @throws Exception
	 */
	static public <X, T extends IConverter<X>> X convertStringToValue(Class<T> clz, Locale loc, String in) throws Exception {
		IConverter<X> c = getConverterInstance(clz);
		return c.convertStringToObject(loc, in);
	}

	/**
	 * Convert some object to a String value, using the specified converter.
	 * @param clz
	 * @param loc
	 * @param in
	 * @return
	 * @throws Exception
	 */
	static public <X, T extends IConverter<X>> String convertValueToString(Class<T> clz, Locale loc, X in) throws Exception {
		IConverter<X> c = getConverterInstance(clz);
		return c.convertObjectToString(loc, in);
	}

	/**
	 * Convert a String value to some object, using the specified converter. This uses the "current" locale.
	 *
	 * @param clz
	 * @param in
	 * @return
	 * @throws Exception
	 */
	static public <X, T extends IConverter<X>> X convertStringToValue(Class<T> clz, String in) throws Exception {
		T c = getConverterInstance(clz);
		Locale loc = NlsContext.getLocale();
		return c.convertStringToObject(loc, in);
	}

	/**
	 * Convert some object to a String value, using the specified converter. This uses the "current" locale.
	 *
	 * @param clz
	 * @param in
	 * @return
	 * @throws Exception
	 */
	static public <X, T extends IConverter<X>> String convertValueToString(Class<T> clz, X in) throws Exception {
		if(clz == null)
			return in == null ? "" : in.toString();
		T c = getConverterInstance(clz);
		Locale loc = NlsContext.getLocale();
		return c.convertObjectToString(loc, in);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Default conversions per type.						*/
	/*--------------------------------------------------------------*/
	/*--------------------------------------------------------------*/
	/*	CODING:	URL Converters.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Maps a class to the converter for the class.
	 */
	static private Map<Class<?>, IConverter<?>> m_urlConverterMap = new HashMap<Class<?>, IConverter<?>>();

	/**
	 * Register an URL converter for the specified class.
	 *
	 * @param totype
	 * @param c
	 */
	static public synchronized <X> void registerURLConverter(Class<X> totype, IConverter<X> c) {
		if(null != m_urlConverterMap.put(totype, c))
			throw new IllegalStateException("Duplicate URLConverter registered for target type=" + totype);
	}

	/**
	 * Finds an URL converter for the specified target type. This walks the target type's hierarchy to
	 * allow for supertypes and interfaces to be registered and found.
	 *
	 * @param totype
	 * @return
	 */
	static private synchronized <X> IConverter<X> calculateURLConverter(Class<X> totype) {
		Class< ? > ctype = totype;
		for(;;) { // Walk to the hierarchy's parent (Object.class)
			IConverter<X> c = (IConverter<X>) m_urlConverterMap.get(ctype);
			if(c != null)
				return c;

			//-- Scan all interfaces and superinterfaces for this type
			c = (IConverter<X>) scanInterfaces(ctype);
			if(c != null)
				return c;
			ctype = ctype.getSuperclass();
			if(ctype == null || Object.class == ctype)
				return null;
		}
	}

	static private synchronized IConverter<?> scanInterfaces(Class<?> ctype) {
		//-- Direct interfaces are preferred
		Class< ? >[] intar = ctype.getInterfaces();
		for(Class< ? > iclz : intar) {
			IConverter<?> c = m_urlConverterMap.get(iclz);
			if(c != null)
				return c;
		}

		//-- Try all parents.
		for(Class< ? > iclz : intar) {
			Class< ? > pint = iclz.getSuperclass();

			while(pint != null && pint != Object.class) {
				IConverter<?> c = m_urlConverterMap.get(pint);
				if(c != null)
					return c;
				pint = pint.getSuperclass();
			}
		}
		return null;
	}

	/**
	 * Find an URL converter to convert to the given type.
	 * @param totype
	 * @return
	 */
	static public synchronized <X> IConverter<X> findURLConverter(Class<X> totype) {
		IConverter<X> c = (IConverter<X>) m_urlConverterMap.get(totype);
		if(c != null)
			return c;
		c = calculateURLConverter(totype);
		m_urlConverterMap.put(totype, c);
		return c;
	}

	/**
	 * Convert the URL string passed to some object value.
	 * @param toType
	 * @param svalue
	 * @return
	 * @throws Exception
	 */
	static public <X> X convertURLStringToValue(Class<X> toType, String svalue) throws Exception {
		IConverter<X> c = findURLConverter(toType);
		if(c == null)
			return RuntimeConversions.convertTo(svalue, toType);
		return c.convertStringToObject(NlsContext.getLocale(), svalue);
	}


	/*----------------------------------- NEW, ACCEPTED CODE ------------------------*/
	/*
	 * Everything above this part is due for refactoring because the converter framework is messy. Refactored code
	 * goes below this line.
	 */

	/*--------------------------------------------------------------*/
	/*	CODING:	Define factories.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Registers the specified converter factory.
	 */
	static synchronized public void register(IConverterFactory cf) {
		if(m_factoryList.contains(cf))
			return;
		m_factoryList = new ArrayList<IConverterFactory>(m_factoryList); // Dup the original list,
		m_factoryList.add(cf);
	}

	/**
	 * Return a thread-safe copy of the factory list.
	 * @return
	 */
	static synchronized private List<IConverterFactory> getFactoryList() {
		return m_factoryList;
	}

	/**
	 * Returns the "default" converter factory, which returns the "factory of last resort"; this one converts
	 * everything by using toString(), and does not convert anything back.
	 *
	 * @return
	 */
	static synchronized IConverterFactory getDefaultFactory() {
		return m_defaultConverterFactory;
	}

	/**
	 * Replaces the default factory (converter of last resort) - USE WITH CARE, OR BETTER YET - DO NOT USE AT ALL!!
	 * @param f
	 */
	static synchronized void setDefaultFactory(IConverterFactory f) {
		if(f == null)
			throw new NullPointerException("Default converter CANNOT BE NULL!!");
		m_defaultConverterFactory = f;
		m_converterMap.clear();
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Find factories for conversions.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Finds the best factory to use. Returns null if no factory was found.
	 * <p>jal 20091118 Per-class factory cache removed because more than just class is used to determine the factory to use.</p>
	 * @param clz
	 * @param pmm
	 * @return
	 */
	static private IConverterFactory findFactory(Class< ? > clz, PropertyMetaModel pmm) {
		synchronized(ConverterRegistry.class) {
			//-- Scan teh full list, and build a list-of-factories-accepting-this-class during it,
			List<IConverterFactory> flist = new ArrayList<IConverterFactory>();
			IConverterFactory best = null;
			int bestscore = 0;
			for(IConverterFactory cf : getFactoryList()) {
				int score = cf.accept(clz, pmm);
				if(score < 0)
					continue;
				flist.add(cf); // Factory at least accepts the class- add to per-type list,

				if(score > bestscore) { // > 0 (!) and > highscore
					best = cf;
					bestscore = score;
				}
			}
			return best;
		}
	}

	/**
	 * Like {@link #findFactory(Class, PropertyMetaModel), but never returns null; this returns the default converter
	 * factory if no specific one is found.
	 * @param clz
	 * @param pmm
	 * @return
	 */
	static private IConverterFactory getFactory(Class< ? > clz, PropertyMetaModel pmm) {
		IConverterFactory cf = findFactory(clz, pmm);
		return cf == null ? getDefaultFactory() : cf;
	}

	/**
	 * Finds the best converter to convert a value of the specified type (and the optionally specified metadata) to a string. This walks
	 * the converter factory list and finds the best converter to use. If no factory accepts the type this returns null. To get a valid
	 * converter all of the time use getConverter(); this returns the "default converter" if no specific converter could be found.
	 *
	 * @param clz	The class type of the value to convert
	 * @param pmm	The metadata for the property, or null if unknown.
	 * @return A converter instance, or null if no factory claimed the type.
	 */
	static public <X> IConverter<X> findConverter(Class<X> clz, PropertyMetaModel pmm) {
		IConverterFactory cf = findFactory(clz, pmm);
		if(cf == null)
			return null;
		return cf.createConverter(clz, pmm);
	}

	/**
	 * Finds the best converter to convert a value of the specified type to a string. This walks
	 * the converter factory list and finds the best converter to use. If no factory accepts the
	 * type this returns null. To get a valid converter all of the time use getConverter(); this
	 * returns the "default converter" if no specific converter could be found.
	 *
	 * @param clz	The class type of the value to convert
	 */
	static public <X> IConverter<X> findConverter(Class<X> clz) {
		return findConverter(clz, null);
	}

	/**
	 * Gets the best converter to convert a value of the specified type (and the optionally specified metadata) to a string. This walks
	 * the converter factory list and finds the best converter to use. If no factory accepts the type this returns the default converter.
	 *
	 * @param clz	The class type of the value to convert
	 * @param pmm	The metadata for the property, or null if unknown.
	 * @return A converter instance.
	 */
	static public <X> IConverter<X> getConverter(Class<X> clz, PropertyMetaModel pmm) {
		IConverterFactory cf = getFactory(clz, pmm);
		return cf.createConverter(clz, pmm);
	}

	/**
	 * Convert the value which is for a given property to a presentation string.
	 * @param <X>
	 * @param pmm
	 * @param value
	 * @return
	 */
	static public <X> String convertToString(PropertyMetaModel pmm, X value) {
		IConverter<X> conv = (IConverter<X>) getConverter(pmm.getActualType(), pmm);
		return conv.convertObjectToString(NlsContext.getLocale(), value);
	}

	/**
	 * Obtain the very best presentation converter we can find for the specified property.
	 * @param pmm
	 * @return
	 */
	static public IConverter< ? > findBestConverter(PropertyMetaModel pmm) {
		//-- User specified converters always override anything else.
		if(pmm.getConverter() != null) // User-specified converters always override all else.
			return pmm.getConverter();

		//-- Ask the converter registry for a converter for this
		return findConverter(pmm.getActualType(), pmm);
	}
}
