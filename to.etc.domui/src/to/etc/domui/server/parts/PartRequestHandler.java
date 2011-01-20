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
package to.etc.domui.server.parts;

import java.io.*;
import java.util.*;

import to.etc.domui.server.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.domui.util.LRUHashMap;
import to.etc.domui.util.resources.*;
import to.etc.util.*;

public class PartRequestHandler implements IFilterRequestHandler {
	private final DomApplication m_application;

	private final boolean m_allowExpires;

	private List<IUrlPart> m_urlFactories = new ArrayList<IUrlPart>();

	/**
	 * Contains a cached instance of some part rendering.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Jun 4, 2008
	 */
	static private class CachedPart {
		public byte[][] m_data;

		public int m_size;

		public ResourceDependencies m_dependencies;

		public String m_contentType;

		//		public String		m_key;

		/** The time a response may be cached locally, in seconds */
		public int m_cacheTime;

		CachedPart() {}
	}


	public PartRequestHandler(final DomApplication application) {
		m_application = application;

		LRUHashMap.SizeCalculator<CachedPart> sc = new LRUHashMap.SizeCalculator<CachedPart>() {
			@Override
			public int getObjectSize(final CachedPart item) {
				return item == null ? 4 : item.m_size + 32;
			}
		};

		m_cache = new LRUHashMap<Object, CachedPart>(sc, 16 * 1024 * 1024); // Accept 16MB of resources FIXME Must be parameterized

		m_allowExpires = DeveloperOptions.getBool("domui.expires", true);
	}

	DomApplication getApplication() {
		return m_application;
	}

	public void registerUrlPart(IUrlPart p) {
		synchronized(m_urlFactories) {
			m_urlFactories.add(p);
		}
	}

	//	static private void dumpHeaders(RequestContextImpl ctx) {
	//		for(Enumeration<String> e = ctx.getRequest().getHeaderNames(); e.hasMoreElements();) {
	//			String name = e.nextElement();
	//			System.out.println("  hdr " + name + ": " + ctx.getRequest().getHeader(name));
	//		}
	//	}

	public boolean acceptURL(final String in) {
		if(in.endsWith(".part"))
			return true;
		int pos = in.indexOf('/'); // First component
		if(pos < 0)
			return false;
		String seg = in.substring(0, pos);
		if(seg.endsWith(".part"))
			return true;

		// FIXME Needs to be faster, needs to be done only once.
		if(null != findFactory(in))
			return true;
		return false;
	}

	private IUrlPart findFactory(String rurl) {
		synchronized(m_urlFactories) {
			for(IUrlPart p : m_urlFactories) {
				if(p.accepts(rurl))
					return p;
			}
		}
		return null;
	}

	@Override
	public void handleRequest(final RequestContextImpl ctx) throws Exception {
		String input = ctx.getInputPath();
		//		dumpHeaders(ctx);
		boolean part = false;
		if(input.endsWith(".part")) {
			input = input.substring(0, input.length() - 5); // Strip ".part" off the name
			part = true;
		}
		int pos = input.indexOf('/'); // First path component is the factory name,
		String fname, rest;
		if(pos == -1) {
			fname = input;
			rest = "";
		} else {
			fname = input.substring(0, pos);
			rest = input.substring(pos + 1);
		}
		if(fname.endsWith(".part")) {
			fname = fname.substring(0, fname.length() - 5);
			part = true;
		}

		IPartRenderer pr;
		if(part) {
			//-- Obtain the factory class, then ask it to execute.
			pr = findPartRenderer(fname);
		} else {
			//-- FIXME Do this faster.
			rest = input;
			IUrlPart p = findFactory(input);
			if(p == null)
				throw new IllegalStateException("No factory for " + ctx + " but we have accepted!?");
			pr = createPartRenderer(p);
		}
		if(pr == null)
			throw new ThingyNotFoundException("The part factory '" + fname + "' cannot be located.");
		pr.render(ctx, rest);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Part renderer factories.							*/
	/*--------------------------------------------------------------*/
	/** All part renderer thingies currently known to the system. */
	private final Map<String, IPartRenderer> m_partMap = new HashMap<String, IPartRenderer>();

	static private final IPartFactory makePartInst(final Class< ? > fc) {
		try {
			return (IPartFactory) fc.newInstance();
		} catch(Exception x) {
			throw new IllegalStateException("Cannot instantiate PartFactory '" + fc + "': " + x, x);
		}
	}

	/**
	 * Returns a thingy which knows how to render the part.
	 */
	public synchronized IPartRenderer findPartRenderer(final String name) {
		IPartRenderer pr = m_partMap.get(name);
		if(pr != null)
			return pr;

		//-- Try to locate the factory class passed,
		Class< ? > fc = DomUtil.findClass(getClass().getClassLoader(), name);
		if(fc == null)
			return null;
		if(!IPartFactory.class.isAssignableFrom(fc))
			throw new IllegalArgumentException("The class '" + name
				+ "' does not implement the 'PartFactory' interface (it is not a part, I guess. WHAT ARE YOU DOING!? Access logged to administrator)");

		//-- Create the appropriate renderers depending on the factory type.
		final IPartFactory pf = makePartInst(fc); // Instantiate
		if(pf instanceof IUnbufferedPartFactory) {
			pr = new IPartRenderer() {
				@Override
				public void render(final RequestContextImpl ctx, final String rest) throws Exception {
					IUnbufferedPartFactory upf = (IUnbufferedPartFactory) pf;
					upf.generate(getApplication(), rest, ctx);
				}
			};
		} else if(pf instanceof IBufferedPartFactory) {
			pr = new IPartRenderer() {
				@Override
				public void render(final RequestContextImpl ctx, final String rest) throws Exception {
					generate((IBufferedPartFactory) pf, ctx, rest); // Delegate internally
				}
			};
		} else
			throw new IllegalStateException("??Internal: don't know how to handle part factory " + fc);

		m_partMap.put(name, pr);
		return pr;
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
	private final LRUHashMap<Object, CachedPart> m_cache;

	/**
	 * Helper which handles possible cached buffered parts.
	 * @param pf
	 * @param ctx
	 * @param url
	 * @throws Exception
	 */
	public void generate(final IBufferedPartFactory pf, final RequestContextImpl ctx, final String url) throws Exception {
		//-- Convert the data to a key object, then lookup;
		Object key = pf.decodeKey(url, ctx);
		if(key == null)
			throw new ThingyNotFoundException("Cannot get resource for " + pf + " with rurl=" + url);

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

		if(cp == null) {
			//-- We're going to create the part
			cp = new CachedPart(); // New one to be done,
			ResourceDependencyList rdl = new ResourceDependencyList(); // Fix bug# 852: allow resource change checking in production also.
			ByteBufferOutputStream os = new ByteBufferOutputStream();
			PartResponse pr = new PartResponse(os);
			pf.generate(pr, m_application, key, rdl);
			cp.m_contentType = pr.getMime();
			if(cp.m_contentType == null)
				throw new IllegalStateException("The part " + pf + " did not set a MIME type, rurl=" + url);
			os.close();
			cp.m_size = os.getSize();
			cp.m_data = os.getBuffers();
			cp.m_dependencies = rdl.createDependencies();
			cp.m_cacheTime = pr.getCacheTime();
			synchronized(m_cache) {
				m_cache.put(key, cp); // Store (may be done multiple times due to race condition)
			}
		}

		//-- Generate the part
		OutputStream os = null;
		if(cp.m_cacheTime > 0 && m_allowExpires) {
			ServerTools.generateExpiryHeader(ctx.getResponse(), cp.m_cacheTime); // Allow browser-local caching.
		}
		ctx.getResponse().setContentType(cp.m_contentType);
		ctx.getResponse().setContentLength(cp.m_size);

		try {
			os = ctx.getResponse().getOutputStream();
			for(byte[] data : cp.m_data)
				os.write(data);
		} finally {
			try {
				if(os != null)
					os.close();
			} catch(Exception x) {}
		}
	}
}
