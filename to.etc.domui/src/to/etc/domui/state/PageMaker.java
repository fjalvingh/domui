package to.etc.domui.state;

import java.lang.reflect.*;
import java.math.*;
import java.util.*;

import to.etc.domui.annotations.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;
import to.etc.util.*;
import to.etc.webapp.query.*;

/**
 * This static class helps with constructing pages from NodeContainer classes
 * that are marked as being usable as pages.
 * Parking class which holds the code to create a page class, including all
 * embellishments.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 23, 2008
 */
public class PageMaker {
	/**
	 * This tries to locate the specified page class in the conversation specified, and returns
	 * null if the page cannot be located. It is a helper function to allow access to components
	 * from Parts etc.
	 */
	static public Page	findPageInConversation(final RequestContext rctx, final Class<? extends UrlPage> clz, final String cid) throws Exception {
		if(cid == null)
			return null;
		String[]	cida	= DomUtil.decodeCID(cid);
		WindowSession	cm	= rctx.getSession().findWindowSession(cida[0]);
		if(cm == null)
			throw new IllegalStateException("The WindowSession with wid="+cida[0]+" has expired.");
		ConversationContext cc = cm.findConversation(cida[1]);
		if(cc == null)
			return null;

		//-- Page resides here?
		return cc.findPage(clz);		// Is this page already current in this context?
	}

	/**
	 * FIXME Move to WindowSession?
	 * @param pg
	 * @param papa
	 * @return
	 * @throws Exception
	 */
	static public boolean pageAcceptsParameters(final Page pg, final PageParameters papa) throws Exception {
		if(papa == null)
			return true;
		if(papa.equals(pg.getPageParameters()))
			return true;
		UrlPage nc = pg.getBody();
		if(nc instanceof IParameterChangeListener) {
			IParameterChangeListener pcl = (IParameterChangeListener) nc;
			pg.internalInitialize(papa, pg.getConversation());		// Update parameters
			pcl.pageParametersChanged(papa);						// Send the event to the page
			return true;
		}
		return false;
	}

	static Page	createPageWithContent(final RequestContext ctx, final Constructor<? extends UrlPage> con, final ConversationContext cc, final PageParameters pp) throws Exception {
		UrlPage	nc	= createPageContent(ctx, con, cc, pp);
		Page	pg	= new Page(nc);
		cc.internalRegisterPage(pg, pp);
		return pg;
	}

	/**
	 * FIXME Needs new name
	 * @param ctx
	 * @param con
	 * @param cc
	 * @param pp
	 * @return
	 * @throws Exception
	 */
	static private UrlPage		createPageContent(final RequestContext ctx, final Constructor<? extends UrlPage> con, final ConversationContext cc, final PageParameters pp) throws Exception {
		//-- Create the page.
		Class<?>[]	par	= con.getParameterTypes();
		Object[]	args = new Object[par.length];

		for(int i = 0; i < par.length; i++) {
			Class<?> pc = par[i];
			if(PageParameters.class.isAssignableFrom(pc))
				args[i] = pp;
			else if(ConversationContext.class.isAssignableFrom(pc))
				args[i] = cc;
			else
				throw new IllegalStateException("?? Cannot assign a value to constructor parameter ["+i+"]: "+pc+" of "+con);
		}

		UrlPage	p;
		try {
			p = con.newInstance(args);
		} catch(InvocationTargetException itx) {
			Throwable c = itx.getCause();
			if(c instanceof Exception)
				throw (Exception) c;
			else if(c instanceof Error)
				throw (Error) c;
			else
				throw itx;
		}
		return p;
	}

	static public Constructor<? extends UrlPage>	getBestPageConstructor(final Class<? extends UrlPage> clz, final boolean hasparam) {
		Constructor<? extends UrlPage>[]		car = clz.getConstructors();
		Constructor<? extends UrlPage>			bestcc = null;			// Will be set if a conversationless constructor is found
		int score = 0;
		for(Constructor<? extends UrlPage> cc : car) {
			//-- Check accessibility
			int mod = cc.getModifiers();
			if(! Modifier.isPublic(mod))
				continue;
			Class<?>[]	par = cc.getParameterTypes();		// Zhe parameters
			int sc;
			if((par == null || par.length == 0) && score < 1) {
				sc = 1;
			} else {
				sc = 3;					// Better match always
				int cnt = 0;
				int pcnt= 0;
				int nparam = 0;						// #of matched constructor parameters
				for(Class<?> pc : par) {
					if(ConversationContext.class.isAssignableFrom(pc)) {
						cnt++;
						sc += 2;
						nparam++;
					} else if(PageParameters.class.isAssignableFrom(pc)) {
						if(hasparam)
							sc++;
						else
							sc--;
						pcnt++;
						nparam++;
					}
				}
				//-- Skip silly constructors
				if(cnt > 1 || pcnt > 1) {
					WindowSession.LOG.info("Skipping silly constructor: "+cc);
					continue;
				}
				if(nparam != par.length) {
					WindowSession.LOG.info("Not all parameters can be filled-in: "+cc);
					continue;
				}
			}
			if(sc > score) {
				bestcc = cc;
				score = sc;
			}
		}

		//-- At this point we *must* have a usable constructor....
		if(bestcc == null)
			throw new IllegalStateException("The Page class "+clz+" does not have a suitable constructor.");
		return bestcc;
	}

	/**
	 * Finds the best constructor to use for the given page and the given conversation context.
	 *
	 * @param clz
	 * @param ccclz
	 * @param hasparam
	 * @return
	 */
	static public Constructor<? extends UrlPage>	getPageConstructor(final Class<? extends UrlPage> clz, final Class<? extends ConversationContext> ccclz, final boolean hasparam) {
		Constructor<? extends UrlPage>	bestcc = null;		// Will be set if a conversationless constructor is found
		int score = 0;
		for(Constructor<? extends UrlPage> cc : clz.getConstructors()) {
			//-- Check accessibility
			int mod = cc.getModifiers();
			if(! Modifier.isPublic(mod))
				continue;
			Class<?>[]	par = cc.getParameterTypes();		// Zhe parameters
			if(par == null || par.length == 0)
				continue;									// Never suitable
			boolean	acc = false;
			int	sc	= 5;									// This-thingies score: def to 5
			for(Class<?> pc : par) {
				if(PageParameters.class.isAssignableFrom(pc)) {
					if(hasparam)
						sc++;								// This is a good match
					else
						sc--;								// Not a good match
				} else if(ccclz.isAssignableFrom(pc)) {		// Can accept the specified context?
					acc = true;
				} else {									// Unknown parameter type?
					sc = -100;
					break;
				}
			}
			if(! acc)										// Conversation not accepted?
				continue;
			if(sc > score) {
				score = sc;
				bestcc = cc;
			}
		}

		//-- At this point we *must* have a usable constructor....
		if(bestcc == null)
			throw new IllegalStateException("The Page class "+clz+" does not have a suitable constructor.");
		return bestcc;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Conversation creation.								*/
	/*--------------------------------------------------------------*/
	/**
	 * Checks to see if the page specified accepts the given conversation class.
	 * @param pgclz
	 * @param ccclz
	 * @return
	 */
//	static public boolean	pageAcceptsConversation(Class<Page> pgclz, Class<? extends ConversationContext> ccclz) {
//		Constructor<Page>[]	coar = pgclz.getConstructors();
//		for(Constructor<Page> c : coar) {
//			Class<?>[]	par = c.getParameterTypes();
//			for(Class<?> pc : par) {
//				if(pc.isAssignableFrom(ccclz))					// This constructor accepts this conversation.
//					return true;
//			}
//		}
//		return false;
//	}
//
	/**
	 * From a page constructor, get the Conversation class to use.
	 *
	 * @param clz
	 * @return
	 */
	static public Class<? extends ConversationContext>	getConversationType(final Constructor<? extends UrlPage> clz) {
		Class<? extends ConversationContext>	ccclz = null;
		for(Class<?> pc: clz.getParameterTypes()) {
			if(ConversationContext.class.isAssignableFrom(pc)) {
				//-- Gotcha!! Cannot have 2,
				if(ccclz != null)
					throw new IllegalStateException(clz+": duplicate conversation contexts in constructor??");
				ccclz = (Class<? extends ConversationContext>)pc;
			}
		}
		if(ccclz == null)
			return ConversationContext.class;
		return ccclz;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Page parameters injector stack.						*/
	/*--------------------------------------------------------------*/

	/**
	 * Maps UrlPage classnames to their PageInjectors. We use names instead of the Class instances
	 * to allow for class reloading.
	 */
	static private Map<String, PageInjector>	m_injectorMap = new HashMap<String, PageInjector>();

	/**
	 *
	 */
	static private abstract class PropertyInjector {
		final private Method	m_propertySetter;

		public PropertyInjector(final Method propertySetter) {
			m_propertySetter = propertySetter;
		}
		protected Method getPropertySetter() {
			return m_propertySetter;
		}
		public abstract void	inject(UrlPage page, RequestContextImpl ctx, PageParameters pp) throws Exception;
	}

	static private final class PageInjector {
		final private List<PropertyInjector>	m_propInjectorList;
		final private Class<? extends UrlPage>	m_pageClass;

		public PageInjector(final Class< ? extends UrlPage> pageClass, final List<PropertyInjector> propInjectorList) {
			m_pageClass = pageClass;
			m_propInjectorList = propInjectorList;
		}
//		public List<PropertyInjector> getPropInjectorList() {
//			return m_propInjectorList;
//		}
		public Class<? extends UrlPage> getPageClass() {
			return m_pageClass;
		}

		/**
		 * Inject into all page properties.
		 * @param page
		 * @param ctx
		 * @param pp
		 * @throws Exception
		 */
		public void	inject(final UrlPage page, final RequestContextImpl ctx, final PageParameters pp) throws Exception {
			for(PropertyInjector pi: m_propInjectorList)
				pi.inject(page, ctx, pp);
		}
	}

	/**
	 * Checks all properties of a page and returns a list of Injectors to use to inject values into
	 * those properties, if needed.
	 *
	 * @param page
	 * @return
	 */
	static private List<PropertyInjector>	calculateInjectorList(final Class<? extends UrlPage> page) {
		List<PropertyInfo>		pilist = ClassUtil.getProperties(page);
		List<PropertyInjector>	ilist = Collections.EMPTY_LIST;
		for(PropertyInfo pi: pilist) {
			PropertyInjector pij = calculateInjector(pi);
			if(pij != null) {
				if(ilist.size() == 0)
					ilist = new ArrayList<PropertyInjector>();
				ilist.add(pij);
			}
		}
		return ilist;
	}

	static private Set<String>		UCS	= new HashSet<String>();
	static {
		UCS.add(String.class.toString());
		UCS.add(Byte.class.toString());
		UCS.add(Byte.TYPE.getName());
		UCS.add(Character.class.toString());
		UCS.add(Character.TYPE.getName());
		UCS.add(Short.class.toString());
		UCS.add(Short.TYPE.getName());
		UCS.add(Integer.class.toString());
		UCS.add(Integer.TYPE.getName());
		UCS.add(Long.class.toString());
		UCS.add(Long.TYPE.getName());
		UCS.add(Float.class.toString());
		UCS.add(Float.TYPE.getName());
		UCS.add(Double.class.toString());
		UCS.add(Double.TYPE.getName());
		UCS.add(Date.class.toString());
		UCS.add(BigDecimal.class.toString());
		UCS.add(BigInteger.class.toString());
//		UCS.add(Byte.class.toString());
//		UCS.add(Byte.class.toString());
//		UCS.add(Byte.class.toString());
//		UCS.add(Byte.class.toString());
//		UCS.add(Byte.class.toString());
//		UCS.add(Byte.class.toString());
//
	}


	/**
	 * Tries to find an injector to inject a value for the specified property.
	 *
	 * @param pi
	 * @return
	 */
	static private PropertyInjector	calculateInjector(final PropertyInfo pi) {
		if(pi.getSetter() == null)						// Read-only property?
			return null;								// Be gone;
		Method	m = pi.getGetter();
		if(m == null)
			m = pi.getSetter();

		//-- Check annotation.
		UIUrlParameter	upp = m.getAnnotation(UIUrlParameter.class);

		if(upp != null) {
			String name = upp.name() == Constants.NONE ? pi.getName() : upp.name();
			Class<?>	ent = upp.entity();
			if(ent == Object.class) {
				//-- Use getter's type.
				ent = pi.getGetter().getReturnType();
			}

			/*
			 * Entity auto-discovery: if entity is specified we're always certain we have an entity. If not,
			 * we check the property type; if that is in a supported conversion class we assume a normal value.
			 */
			if(upp.entity() == Object.class) {
				//-- Can be entity or literal.
				if(upp.name() == Constants.NONE || UCS.contains(ent.getName()))		// If no name is set this is NEVER an entity,
					return new UrlParameterInjector(pi.getSetter(), name, upp.mandatory());
			}

			//-- Entity lookup.
			return new UrlEntityInjector(pi.getSetter(), name, upp.mandatory(), ent);
		}
		return null;
	}

	/**
	 * Fully recalculates the page injectors to use for the specified page. This explicitly does not
	 * use the injector cache.
	 * @param page
	 * @return
	 */
	static private PageInjector	calculatePageInjector(final Class<? extends UrlPage> page) {
		List<PropertyInjector>	pil = calculateInjectorList(page);
		return new PageInjector(page, pil);
	}

	/**
	 * Find the page injectors to use for the page. This uses the cache.
	 * @param page
	 * @return
	 */
	static private synchronized PageInjector	findPageInjector(final Class<? extends UrlPage> page) {
		String	cn = page.getClass().getCanonicalName();
		PageInjector	pij	= m_injectorMap.get(cn);
		if(pij != null) {
			//-- Hit on name; is the class instance the same? If not this is a reload.
			if((Class<?>)pij.getPageClass() == page.getClass())		// Idiotic generics. If the class changed we have a reload of the class and need to recalculate.
				return pij;
		}

		pij	= calculatePageInjector(page);
		m_injectorMap.put(cn, pij);
		return pij;
	}

	/**
	 * This scans the page for properties that are to be injected. It scans for properties on the Page's UrlPage class
	 * and injects any stuff it finds.
	 *
	 * @param page
	 * @param ctx
	 * @param papa
	 * @throws Exception
	 */
	static public void		injectPageValues(final UrlPage page, final RequestContextImpl ctx, final PageParameters papa) throws Exception {
		PageInjector	pij	= findPageInjector(page.getClass());
		pij.inject(page, ctx, papa);
	}

	/**
	 * This property injector contains the name of an URL parameter plus the property to set from it. At
	 * injection time it uses the name to get the string value of the URL parameter. This parameter is
	 * then converted using the URL converters registered in the ConverterRegistry to the proper value
	 * type of the setter.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Dec 19, 2008
	 */
	static private final class UrlParameterInjector extends PropertyInjector {
		final private String		m_name;
		final private boolean		m_mandatory;

		public UrlParameterInjector(final Method propertySetter, final String name, final boolean mandatory) {
			super(propertySetter);
			m_name = name;
			m_mandatory = mandatory;
		}

		/**
		 * Effects the actual injection of an URL parameter to a value.
		 * @see to.etc.domui.state.PageMaker.PropertyInjector#inject(to.etc.domui.server.RequestContextImpl, to.etc.domui.state.PageParameters)
		 */
		@Override
		public void inject(final UrlPage page, final RequestContextImpl ctx, final PageParameters papa) throws Exception {
			//-- 1. Get the URL parameter's value.
			String	pv = papa.getString(m_name);
			if(pv == null) {
				if(m_mandatory)
					throw new IllegalArgumentException("The page "+page.getClass()+" REQUIRES the URL parameter "+m_name);
				return;
			}

			//-- 2. Convert the thing to the appropriate type.
			Class<?>	type = getPropertySetter().getReturnType();
			Object	value;
			try {
				value = ConverterRegistry.convertURLStringToValue(type, pv);
			} catch(Exception x) {
				throw new RuntimeException("Cannot convert the string '"+pv+"' to type="+type+", for URL parameter="+m_name+" of page="+page.getClass()+": "+x, x);
			}

			//-- 3. Insert the value.
			try {
				getPropertySetter().invoke(page, value);
			} catch(Exception x) {
				throw new RuntimeException("Cannot SET the value '"+value+"' converted from the string '"+pv+"' to type="+type+", for URL parameter="+m_name+" of page="+page.getClass()+": "+x, x);
			}
		}
	}

	/**
	 * This property injector takes the named URL parameter as a string. It does a lookup of the entity specified
	 * in the MetaData and locates it's ID property. The URL parameter string is then converted to the type of that
	 * primary key using the ConverterRegistry's URL converters. Finally it issues a LOOKUP of the entity using that
	 * PK. This converter accepts the special value "NEW"; when that is present it constructs a new instance of the
	 * entity.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Dec 19, 2008
	 */
	static private final class UrlEntityInjector extends PropertyInjector {
		final private String		m_name;
		final private boolean		m_mandatory;
		final private Class<?>		m_entityClass;

		public UrlEntityInjector(final Method propertySetter, final String name, final boolean mandatory, final Class< ? > enityClass) {
			super(propertySetter);
			m_name = name;
			m_mandatory = mandatory;
			m_entityClass = enityClass;
		}

		@Override
		public void inject(final UrlPage page, final RequestContextImpl ctx, final PageParameters papa) throws Exception {
			//-- 1. Get the URL parameter's value.
			String	pv = papa.getString(m_name);
			if(pv == null) {
				if(m_mandatory)
					throw new IllegalArgumentException("The page "+page.getClass()+" REQUIRES the URL parameter "+m_name);
				return;
			}

			//-- 2. Handle the constant 'NEW'.
			Object	value;
			if("NEW".equals(pv)) {
				//-- Construct a new instance
				try {
					value = m_entityClass.newInstance();
				} catch(Exception x) {
					throw new RuntimeException("Cannot create an instance of entity class '"+m_entityClass+"' for URL parameter="+m_name+" of page="+page.getClass()+": "+x, x);
				}
			} else {
				//-- Try to find the PK for this entity
				ClassMetaModel	cmm	= MetaManager.findClassMeta(m_entityClass);	// Locatish
				PropertyMetaModel pmm	= cmm.getPrimaryKey();					// Find it's PK;
				if(pmm == null)
					throw new RuntimeException("Cannot find the primary key property for entity class '"+m_entityClass+"' for URL parameter="+m_name+" of page="+page.getClass()+": ");

				//-- Convert the URL's value to the TYPE of the primary key, using URL converters.
				Object	pk = ConverterRegistry.convertURLStringToValue(pmm.getActualType(), pv);
				if(pk == null)
					throw new RuntimeException("URL parameter value='"+pv+"' converted to Null primary key value for entity class '"+m_entityClass+"' for URL parameter="+m_name+" of page="+page.getClass()+": ");

				//-- Load the entity using the page's context
				value = QContextManager.getContext(page.getPage()).find(m_entityClass, pk);
			}

			//-- 3. Insert the value.
			try {
				getPropertySetter().invoke(page, value);
			} catch(Exception x) {
				throw new RuntimeException("Cannot SET the entity '"+value+"' for URL parameter="+m_name+" of page="+page.getClass()+": "+x, x);
			}
		}
	}
}
