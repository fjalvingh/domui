package to.etc.domui.server.parts;

import to.etc.domui.parts.ParameterInfoProxy;
import to.etc.domui.server.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.domui.util.LRUHashMap;
import to.etc.domui.util.resources.*;
import to.etc.util.*;

import javax.annotation.*;
import java.io.*;
import java.util.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 23-5-17.
 */
@DefaultNonNull
public class PartService {

	public static final String PART_SUFFIX = ".part";

	private final DomApplication m_application;

	private final boolean m_allowExpires;

	@Nonnull
	private final LRUHashMap<Object, PartData> m_cache;

	/**
	 * Registers URL matchers connected to parts.
	 */
	private static class MatcherFactoryPair {
		private final IPartFactory m_factory;

		private final IUrlMatcher m_matcher;

		public MatcherFactoryPair(IPartFactory factory, IUrlMatcher matcher) {
			m_factory = factory;
			m_matcher = matcher;
		}

		public IPartFactory getFactory() {
			return m_factory;
		}

		public IUrlMatcher getMatcher() {
			return m_matcher;
		}
	}

	private List<MatcherFactoryPair> m_urlMatcherList = new ArrayList<>();

	private static class PartExecutionReference {
		private final IPartFactory m_factory;

		private final IExtendedParameterInfo m_info;

		public PartExecutionReference(IPartFactory factory, IExtendedParameterInfo info) {
			m_factory = factory;
			m_info = info;
		}

		public IPartFactory getFactory() {
			return m_factory;
		}

		public IExtendedParameterInfo getInfo() {
			return m_info;
		}
	}

	public PartService(DomApplication application) {
		m_application = application;

		m_cache = new LRUHashMap<>(item -> item == null ? 4 : item.getSize() + 32, 16 * 1024 * 1024); 			// Accept 16MB of resources FIXME Must be parameterized
		m_allowExpires = DeveloperOptions.getBool("domui.expires", true);
	}

	/**
	 * Register a part which gets called when the specified matcher matches.
	 * @param matcher
	 * @param factory
	 */
	public synchronized void registerPart(IUrlMatcher matcher, IPartFactory factory) {
		m_urlMatcherList = new ArrayList<>(m_urlMatcherList);
		m_urlMatcherList.add(new MatcherFactoryPair(factory, matcher));
	}

	private synchronized List<MatcherFactoryPair> getMatcherList() {
		return m_urlMatcherList;
	}

	/**
	 * Detect whether this is an URL part, and if so render it.
	 * @param ctx
	 */
	public boolean render(RequestContextImpl ctx) throws Exception {
		PartExecutionReference executionReference = findPart(ctx);
		if(executionReference == null)
			return false;

		IPartFactory factory = executionReference.getFactory();
		if(factory instanceof IUnbufferedPartFactory) {
			IUnbufferedPartFactory upf = (IUnbufferedPartFactory) factory;
			upf.generate(getApplication(), executionReference.getInfo().getInputPath(), ctx);
		} else if(factory instanceof IBufferedPartFactory) {
			generate((IBufferedPartFactory<?>) factory, ctx, executionReference.getInfo());
		} else
			throw new IllegalStateException("??Internal: don't know how to handle part factory " + factory);
		return true;
	}

	/**
	 * Get the data for a part identified by the specified parameters. If the part
	 * has not yet been generated it will be generated and then cached.
	 */
	public PartData getData(IExtendedParameterInfo parameters) throws Exception {
		PartExecutionReference executionReference = findPart(parameters);
		if(executionReference == null)
			throw new ThingyNotFoundException("No part found for " + parameters);

		IPartFactory factory = executionReference.getFactory();
		if(factory instanceof IBufferedPartFactory) {
			IBufferedPartFactory<?> bf = (IBufferedPartFactory<?>) factory;
			PartData cp = getCachedInstance2(bf, parameters);
			return cp;
		} else {
			throw new UnsupportedOperationException("You can only get data for a Buffered part, and " + factory + " is not buffered");
		}
	}

	@Nullable
	private PartExecutionReference findPart(IExtendedParameterInfo parameters) {
		PartExecutionReference ref = checkClassBasedPart(parameters);
		if(null != ref)
			return ref;

		return checkUrlPart(parameters);
	}

	@Nullable
	private PartExecutionReference checkUrlPart(IExtendedParameterInfo parameter) {
		for(MatcherFactoryPair pair : getMatcherList()) {
			if(pair.getMatcher().accepts(parameter)) {
				return new PartExecutionReference(pair.getFactory(), parameter);
			}
		}

		return null;							// No matches
	}

	/**
	 * Checks whether the request is for a classname-based part, and if so returns the
	 * salient details.
	 * A class based part request has a classname followed by .part in the first section
	 * of the URL. The class must exist and also implement {@link IPartFactory} or
	 * a 404 exception is thrown.
	 */
	@Nullable
	private PartExecutionReference checkClassBasedPart(IParameterInfo parameters) {
		String in = parameters.getInputPath();
		String rest;
		String segment;
		int pos = in.indexOf('/');
		if(pos < 0) {
			segment = in;
			rest = "";
		} else {
			segment = in.substring(0, pos);
			rest = in.substring(pos + 1);
		}

		//-- Is this an actual .part?
		if(! segment.endsWith(PART_SUFFIX))				// If it's not ending in .part we're done
			return null;
		segment = segment.substring(0, segment.length() - PART_SUFFIX.length());	// Remove .part to get the class name

		//-- We are sure that this is a part, so the class must exist and be valid.
		IPartFactory factory = getPartFactoryByClassName(segment);
		if(null == factory) {
			throw new ThingyNotFoundException("The part factory '" + segment + "' cannot be located.");
		}

		IExtendedParameterInfo infoProxy = new ParameterInfoProxy(parameters) {
			@Nonnull @Override public String getInputPath() {
				return rest;
			}
		};

		return new PartExecutionReference(factory, infoProxy);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Part renderer factories.							*/
	/*--------------------------------------------------------------*/
	private final Map<String, IPartFactory> m_partByClassMap = new HashMap<>();

	/**
	 * Returns a thingy which knows how to render the part.
	 */
	@Nullable
	public synchronized IPartFactory getPartFactoryByClassName(final String name) {
		IPartFactory factory = m_partByClassMap.get(name);
		if(null == factory) {
			//-- Try to locate the factory class passed,
			Class< ? > fc = DomUtil.findClass(getClass().getClassLoader(), name);
			if(fc == null)
				return null;
			if(!IPartFactory.class.isAssignableFrom(fc))
				throw new IllegalArgumentException("The class '" + name
					+ "' does not implement the 'PartFactory' interface (it is not a part, I guess. WHAT ARE YOU DOING!? Access logged to administrator)");

			//-- Create the appropriate renderers depending on the factory type.
			factory = makePartInst(fc);
			m_partByClassMap.put(name, factory);
		}
		return factory;
	}

	static private final IPartFactory makePartInst(final Class< ? > fc) {
		try {
			return (IPartFactory) fc.newInstance();
		} catch(Exception x) {
			throw new IllegalStateException("Cannot instantiate PartFactory '" + fc + "': " + x, x);
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Buffered parts cache and code.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Helper which handles possible cached buffered parts.
	 * @param pf
	 * @param ctx
	 * @throws Exception
	 */
	private <K> void generate(IBufferedPartFactory<K> pf, RequestContextImpl ctx, IExtendedParameterInfo parameters) throws Exception {
		PartData cp = getCachedInstance2(pf, parameters);

		//-- Generate the part
		OutputStream os = null;
		if(cp.getCacheTime() > 0 && m_allowExpires) {
			ctx.getRequestResponse().setExpiry(cp.getCacheTime());
		}
		try {
			os = ctx.getRequestResponse().getOutputStream(cp.getContentType(), null, cp.getSize());
			for(byte[] data : cp.getData())
				os.write(data);
		} finally {
			try {
				if(os != null)
					os.close();
			} catch(Exception x) {}
		}
	}

	private <K> PartData getCachedInstance2(final IBufferedPartFactory<K> pf, final IExtendedParameterInfo parameters) throws Exception {
		//-- Convert the data to a key object, then lookup;
		K key = pf.decodeKey(m_application, parameters);
		if(key == null)
			throw new ThingyNotFoundException("Cannot get resource for " + pf + " with rurl=" + parameters.getInputPath());
		return getCachedInstance(pf, key);
	}

	public <K> PartData getCachedInstance(final IBufferedPartFactory<K> pf, K key) throws Exception {
		/*
		 * Lookup. This part *is* thread-safe but it has a race condition: it may cause multiple
		 * instances of the SAME resource to be generated at the same time and inserted at the
		 * same time. In time we must replace this with the MAKER pattern, but for now this
		 * small problem will be accepted; it will not cause problems since only the last instance
		 * will be kept and stored.
		 */
		PartData cp;
		synchronized(m_cache) {
			cp = m_cache.get(key); // Already exists here?
		}

		/*
		 * jal 20100901 Always check for updated parts, even when in production mode. Part factories themselves will
		 * decide whether they are reloadable if their source changes, and they will decide whether that is the case
		 * in development only OR also in production. This should fix VP call 27223: menu colors do not change when
		 * VP colors are changed.
		 */
		if(cp != null /* && m_application.inDevelopmentMode() */) {
			if(cp.getDependencies() != null) {
				if(cp.getDependencies().isModified()) {
					System.out.println("parts: part " + key + " has changed. Reloading..");
					cp = null;
				}
			}
		}
		if(cp != null)
			return cp;

		//-- We're going to (re)create the part
		ResourceDependencyList rdl = new ResourceDependencyList();	// Fix bug# 852: allow resource change checking in production also.
		ByteBufferOutputStream os = new ByteBufferOutputStream();
		PartResponse pr = new PartResponse(os);
		pf.generate(pr, m_application, key, rdl);
		String mime = pr.getMime();
		if(mime == null) {
			System.err.println("The part " + pf + " did not set a MIME type, key=" + key + ", using octet-stream");
			mime = "application/octet-stream";
		}
		os.close();
		cp = new PartData(os.getBuffers(), os.getSize(), pr.getCacheTime(), mime, rdl.createDependencies(), pr.getExtra());
		synchronized(m_cache) {
			m_cache.put(key, cp); 								// Store (may be done multiple times due to race condition)
		}
		return cp;
	}


	private DomApplication getApplication() {
		return m_application;
	}
}
