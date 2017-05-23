package to.etc.domui.server.parts;

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
	private final DomApplication m_application;

	private final boolean m_allowExpires;

	@Nonnull
	private final LRUHashMap<Object, CachedPart> m_cache;

	public PartService(DomApplication application) {
		m_application = application;
		LRUHashMap.SizeCalculator<CachedPart> sc = new LRUHashMap.SizeCalculator<CachedPart>() {
			@Override
			public int getObjectSize(@Nullable final CachedPart item) {
				return item == null ? 4 : item.m_size + 32;
			}
		};

		m_cache = new LRUHashMap<>(sc, 16 * 1024 * 1024); 			// Accept 16MB of resources FIXME Must be parameterized
		m_allowExpires = DeveloperOptions.getBool("domui.expires", true);
	}

	public void renderUrlPart(IPartFactory part, RequestContextImpl ctx, @Nonnull String inputPath) throws Exception {
		IPartRenderer pr = createPartRenderer(part);
		if(pr == null)
			throw new ThingyNotFoundException("No renderer for " + part);
		pr.render(ctx, inputPath);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Part renderer factories.							*/
	/*--------------------------------------------------------------*/
	private final Map<String, IPartFactory> m_partByClassMap = new HashMap<>();

	/**
	 * Returns a thingy which knows how to render the part.
	 */
	@Nullable
	public synchronized IPartRenderer findPartRenderer(final String name) {
		IPartFactory factory = getPartFactoryByClassName(name);
		if(null == factory)
			return null;
		IPartRenderer pr = createPartRenderer(factory);
		return pr;
	}

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

	private IPartRenderer createPartRenderer(final IPartFactory pf) {
		if(pf instanceof IUnbufferedPartFactory) {
			return new IPartRenderer() {
				@Override
				public void render(final RequestContextImpl ctx, final String rest) throws Exception {
					IUnbufferedPartFactory upf = (IUnbufferedPartFactory) pf;
					upf.generate(getApplication(), rest, ctx);
				}
			};
		} else if(pf instanceof IBufferedPartFactory) {
			return new IPartRenderer() {
				@Override
				public void render(final RequestContextImpl ctx, final String rest) throws Exception {
					generate((IBufferedPartFactory) pf, ctx, rest); // Delegate internally
				}
			};
		} else
			throw new IllegalStateException("??Internal: don't know how to handle part factory " + pf);
	}

		/*--------------------------------------------------------------*/
	/*	CODING:	Buffered parts cache and code.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Helper which handles possible cached buffered parts.
	 * @param pf
	 * @param ctx
	 * @param url
	 * @throws Exception
	 */
	public void generate(final IBufferedPartFactory pf, final RequestContextImpl ctx, final String url) throws Exception {
		CachedPart cp = getCachedInstance(pf, ctx, url);

		//-- Generate the part
		OutputStream os = null;
		if(cp.m_cacheTime > 0 && m_allowExpires) {
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

	public CachedPart getCachedInstance(final IBufferedPartFactory pf, final RequestContextImpl ctx, final String url) throws Exception {
		//-- Convert the data to a key object, then lookup;
		Object key = pf.decodeKey(url, ctx);
		if(key == null)
			throw new ThingyNotFoundException("Cannot get resource for " + pf + " with rurl=" + url);
		return getCachedInstance(pf, key);
	}

	public CachedPart getCachedInstance(final IBufferedPartFactory pf, Object key) throws Exception {
		/*
		 * Lookup. This part *is* thread-safe but it has a race condition: it may cause multiple
		 * instances of the SAME resource to be generated at the same time and inserted at the
		 * same time. In time we must replace this with the MAKER pattern, but for now this
		 * small problem will be accepted; it will not cause problems since only the last instance
		 * will be kept and stored.
		 */
		CachedPart cp;
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
			if(cp.m_dependencies != null) {
				if(cp.m_dependencies.isModified()) {
					System.out.println("parts: part " + key + " has changed. Reloading..");
					cp = null;
				}
			}
		}
		if(cp != null)
			return cp;

		//-- We're going to (re)create the part
		ResourceDependencyList rdl = new ResourceDependencyList(); // Fix bug# 852: allow resource change checking in production also.
		ByteBufferOutputStream os = new ByteBufferOutputStream();
		PartResponse pr = new PartResponse(os);
		pf.generate(pr, m_application, key, rdl);
		String mime = pr.getMime();
		if(mime == null)
			throw new IllegalStateException("The part " + pf + " did not set a MIME type, key=" + key);
		os.close();
		cp = new CachedPart(os.getBuffers(), os.getSize(), pr.getCacheTime(), mime, rdl.createDependencies(), pr.getExtra());
		synchronized(m_cache) {
			m_cache.put(key, cp); // Store (may be done multiple times due to race condition)
		}
		return cp;
	}


	private DomApplication getApplication() {
		return m_application;
	}
}
