package to.etc.server.servlet.parts;

import java.io.*;
import java.util.*;

import javax.servlet.*;

import to.etc.server.cache.*;
import to.etc.server.servlet.*;
import to.etc.server.vfs.*;
import to.etc.util.*;

/**
 * Singleton containing all part factories.
 *
 * @author jal
 * Created on Jan 23, 2006
 */
final public class PartsRegistry {
	static private PartsRegistry	m_instance	= new PartsRegistry();

	/**
	 * Private structure to keep all data pertaining to a generated part together.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Nov 7, 2006
	 */
	static private class CachedPart {
		public byte[][]	m_data;

		public int		m_size;

		public String	m_contentType;

		CachedPart() {
		}
	}

	/**
	 * This maps parts factories from their identifier string.
	 */
	private Map<String, PartFactory>	m_map	= new Hashtable<String, PartFactory>();

	private PartsRegistry() {
		register("resource", new ResourcePartGenerator());
		register("SystemResource", new OldResourcePartGenerator());
		register("Button", new ButtonPartGenerator());
	}

	static public final PartsRegistry getInstance() {
		return m_instance;
	}

	public synchronized void register(String name, PartFactory pf) {
		m_map.put(name, pf);
		System.out.println("PartsFactory: registered factory " + name);
	}

	public synchronized PartFactory getFactory(String name) throws PartFactoryNotFoundException {
		if(name == null)
			throw new NullPointerException("The part name cannot be null");
		PartFactory pf = m_map.get(name);
		if(pf == null)
			throw new PartFactoryNotFoundException(name);
		return pf;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Parts generator code.								*/
	/*--------------------------------------------------------------*/

	/**
	 * Main entrypoint to generate a part from a servlet request structure.
	 */
	public void generatePart(VfsPathResolver pr, RequestContext info) throws Exception {
		String in = info.getSiteInfo().getInputPath(); // Get input part,
		String factname = null;
		String rurl = null;
		if(in.endsWith(".part")) // Is this an "extension" mode URL?
		{
			/*
			 * The factory name is the name just before the .part. The part before
			 * the factory name becomes the rurl.
			 */
			int pos = in.lastIndexOf('/');
			if(pos == -1) {
				rurl = ""; // No parts before the name
				factname = in.substring(0, in.length() - 5); // All but .part
			} else {
				rurl = in.substring(0, pos); // All before the last segment,
				factname = in.substring(pos + 1, in.length() - 5); // The last segment excluding the .part part.
			}
		} else {
			/*
			 * URL mode. The factory name is the 1st segment; the rest is an url.
			 */
			in = info.getRequest().getPathInfo();
			if(in == null || in.length() == 0)
				throw new ServletException("Missing segment after url.");
			if(in.startsWith("/"))
				in = in.substring(1);
			int pos = in.indexOf('/');
			if(pos == -1) {
				factname = in;
				rurl = "";
			} else {
				factname = in.substring(0, pos);
				rurl = in.substring(pos + 1);
			}
		}

		generatePart(pr, info, factname, rurl);
	}

	/**
	 *
	 */
	public void generatePart(VfsPathResolver resolver, RequestContext info, String partname, String rurl) throws Exception {
		PartFactory pf = getFactory(partname);
		if(pf instanceof UnbufferedPartFactory) {
			//-- Unbuffered: generate immediately
			UnbufferedPartFactory f = (UnbufferedPartFactory) pf;
			f.generate(info, rurl, resolver);
			return;
		}

		/*
		 * Buffered part. Ask the generator for a key to decode the URL, then use the key to
		 * generate the part; finally cache it.
		 */
		BufferedPartFactory f = (BufferedPartFactory) pf;
		Object key = f.decodeKey(resolver, info, rurl); // Let factory decode the request into a key
		ResourceCache rc = ResourceCache.getInstance(); // Get the resource cache thing

		ResourceRef ref = rc.findResource(resolver, COF, key, f, info.getServlet().getServletContext(), null);
		CachedPart cp = (CachedPart) ref.getObject(); // This retrieves the object and if necessary creates it atomically.

		//-- Generate the part
		OutputStream os = null;
		info.getResponse().setContentType(cp.m_contentType);
		info.getResponse().setContentLength(cp.m_size);
		try {
			os = info.getResponse().getOutputStream();
			for(byte[] data : cp.m_data)
				os.write(data);
		} finally {
			try {
				if(os != null)
					os.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * CacheObjectFactory implementation which takes a BufferedPartFactory as
	 * a parameter and generates the resulting data.
	 */
	static private final CacheObjectFactory	COF	= new CacheObjectFactory() {
													public Object makeObject(ResourceRef ref, VfsPathResolver vr, Object pk, DependencySet depset, Object p1, Object p2, Object p3) throws Exception {
														BufferedPartFactory f = (BufferedPartFactory) p1;
														CachedPart cp = new CachedPart();
														ByteBufferOutputStream os = new ByteBufferOutputStream();
														cp.m_contentType = f.generate(os, pk, depset, vr, (ServletContext) p2);
														os.close();
														cp.m_size = os.getSize();
														cp.m_data = os.getBuffers();
														ref.registerMemorySize(cp.m_size);
														return cp;
													}

													public CacheStats makeStatistics() {
														return new CacheStats();
													}
												};
}
