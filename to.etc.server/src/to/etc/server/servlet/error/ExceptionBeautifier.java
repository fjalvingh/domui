package to.etc.server.servlet.error;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import to.etc.net.*;
import to.etc.server.exceptiontemplates.*;
import to.etc.server.servlet.parts.*;
import to.etc.util.*;

/**
 * This is a helper class which "beautifies" exceptions for
 * server tasks.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 17, 2006
 */
public class ExceptionBeautifier {
	static private boolean		m_initialized	= false;

	static private File			m_copyRoot;

	static private File			m_overRoot;

	static private String		m_rootTemplateRURL;

	/**
	 * This is the location within WEB-INF which contains the scratch files copies
	 * from resources.
	 */
	static private final String	COPYROOT		= "/WEB-INF/.toetcserver/";

	static private final String	OVERRIDEROOT	= "/WEB-INF/exception-templates/";

	static private final String	ROOTTEMPLATE	= "exceptionFrame.jsp";

	static private final String	ROOTSOURCE		= "__root";

	/**
	 * Locates a resource by name.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Apr 18, 2006
	 */
	static private interface SourceLocation {
		public String getPath();

		public Source findSource(String name);

		public boolean needsCopy();
	}

	/**
	 * An opened source of stream data.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Apr 18, 2006
	 */
	static public final class Source {
		final private String		m_name;

		final private String		m_encoding;

		final private InputStream	m_is;

		private final String		m_path;

		private boolean				m_copy;

		public Source(InputStream is, String name, String path, String encoding, boolean copy) {
			m_name = name;
			m_is = is;
			m_path = path;
			m_copy = copy;
			m_encoding = encoding;
		}

		public boolean needsCopy() {
			return m_copy;
		}

		public InputStream getInput() {
			return m_is;
		}

		public String getName() {
			return m_name;
		}

		public String getPath() {
			return m_path;
		}

		public String getEncoding() {
			return m_encoding;
		}
	}

	/**
	 * Return class which contains the actual exception to
	 * render AND the source to use as a render template.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Apr 18, 2006
	 */
	static private class ExceptionSource {
		private Source		m_source;

		private Throwable	m_exception;

		public ExceptionSource(Source s, Throwable t) {
			m_source = s;
			m_exception = t;
		}

		public Throwable getException() {
			return m_exception;
		}

		public Source getSource() {
			return m_source;
		}
	}

	static private List<SourceLocation>	m_sourceList	= new ArrayList<SourceLocation>();

	static private class ClassSource implements SourceLocation {
		Class			m_clazz;

		String			m_rel;

		private String	m_path;

		public ClassSource(Class cl, String r) {
			m_clazz = cl;
			if(r != null && r.trim().length() == 0)
				r = null;
			m_rel = r == null ? "" : r + "/";
			StringBuilder sb = new StringBuilder();
			sb.append(m_clazz.getName());
			if(r != null) {
				sb.append('/');
				sb.append(r);
			}
			m_path = sb.toString();
		}

		public Source findSource(String name) {
			//			System.out.println("Trying "+m_clazz.getCanonicalName()+":"+m_rel+"/"+name);
			InputStream is = m_clazz.getResourceAsStream(m_rel + name);
			if(is == null)
				return null;
			return new Source(is, name, getPath(), "utf-8", true);
		}

		public String getPath() {
			return m_path;
		}

		public boolean needsCopy() {
			return true;
		}
	}

	//	static private class OverrideSource implements SourceLocation
	//	{
	//		public InputStream findSource(String name)
	//		{
	//			File	f	= new File(m_overRoot, name);
	//			if(! f.exists())
	//				return null;
	//			try
	//			{
	//				return new FileInputStream(f);
	//			}
	//			catch(Exception x)
	//			{
	//				x.printStackTrace();
	//			}
	//			return null;
	//		}
	//		public boolean needsCopy()
	//		{
	//			return false;
	//		}
	//		public String getPath()
	//		{
	//			return "__over";
	//		}
	//	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Exception template source handling					*/
	/*--------------------------------------------------------------*/

	static public void addSource(Class cl, String rest) {
		m_sourceList.add(new ClassSource(cl, rest));
	}

	/**
	 * Tries to find a source for the spec'd exception.
	 * @param name
	 * @return
	 */
	static private ExceptionSource findSource(String name, Throwable t) {
		if(m_overRoot != null) {
			File f = new File(m_overRoot, name);
			if(f.exists()) {
				try {
					InputStream is = new FileInputStream(f);
					Source s = new Source(is, name, ROOTSOURCE, "utf-8", false);
					return new ExceptionSource(s, t);
				} catch(Exception x) {
					x.printStackTrace(); // Should not really happen
				}
			}
		}

		for(SourceLocation o : m_sourceList) {
			Source s = o.findSource(name);
			if(s != null)
				return new ExceptionSource(s, t);
		}
		return null;
	}

	/**
	 * Retrieves a resource from a specified source.
	 * @param sourcename
	 * @param rurl
	 * @return
	 */
	static public Source findSourceByName(String sourcename, String rurl) throws Exception {
		if(sourcename.equals(ROOTSOURCE)) {
			//-- Use override location OR my location.
			if(m_overRoot != null) {
				File f = new File(m_overRoot, rurl);
				if(f.exists())
					return new Source(new FileInputStream(f), rurl, ROOTSOURCE, "utf-8", false);
			}
			//-- Try my own location
			return new Source(DummyRootThingy.class.getResourceAsStream(rurl), rurl, ROOTSOURCE, "utf-8", false);
		}

		//-- Find the appropriate registered resource
		for(SourceLocation o : m_sourceList) {
			if(o.getPath().equals(sourcename))
				return o.findSource(rurl);
		}
		return null;
	}

	/**
	 * Tries to find the most appropriate source for an exception.
	 * @param t
	 * @return
	 */
	static public ExceptionSource findExceptionSource(Throwable t) {
		//-- The "done" set skips stuff we've already handled.
		Throwable orgt;
		do {
			orgt = t;
			if(t instanceof ServletException && ((ServletException) t).getRootCause() != null)
				t = ((ServletException) t).getRootCause();
			if(t instanceof WrappedException)
				t = ((WrappedException) t).getCause();
		} while(t != orgt);
		Set<Class> doneset = new HashSet<Class>();
		return findExceptionSource(t, doneset, 0);
	}

	static private ExceptionSource findExceptionSource(Throwable t, Set<Class> set, int lvl) {
		if(lvl > 20)
			return null;
		Throwable cause = t.getCause();
		if(cause == null) {
			if(t instanceof ServletException) {
				cause = ((ServletException) t).getRootCause();
			}
		}

		if(cause != null && cause != t) {
			ExceptionSource is = findExceptionSource(cause, set, lvl + 1);
			if(is != null)
				return is;
		}

		//-- We must try to resolve this level.
		return findExceptionSourceHier(t, set);
	}

	static private ExceptionSource findExceptionSourceHier(Throwable t, Set<Class> set) {
		Class cl = t.getClass();
		while(cl != null) {
			if(set.contains(cl))
				return null;
			set.add(cl);
			String name = cl.getCanonicalName();
			if(name != null) {
				name = name + ".jsp";
				ExceptionSource s = findSource(name, t);
				if(s != null)
					return s;
			}
			cl = cl.getSuperclass();
		}
		return null;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Generate the source thingy.							*/
	/*--------------------------------------------------------------*/
	static private synchronized void init(HttpServlet slet) throws Exception {
		if(m_initialized)
			return;

		//-- 1. Create the copydir and check if the override dir exists.
		String rp = slet.getServletContext().getRealPath(COPYROOT);
		m_copyRoot = new File(rp);
		m_copyRoot.mkdirs();
		FileTool.dirEmpty(m_copyRoot);

		//-- 2. Check if an override dir exists.
		rp = slet.getServletContext().getRealPath(OVERRIDEROOT);
		File f = new File(rp);
		m_overRoot = f;

		if(m_overRoot != null) {
			f = new File(m_overRoot, ROOTTEMPLATE);
			if(f.exists())
				m_rootTemplateRURL = OVERRIDEROOT + ROOTTEMPLATE;
		}
		if(m_rootTemplateRURL == null) {
			//-- Copy from core resources.
			InputStream is = null;
			OutputStream os = null;
			try {
				is = DummyRootThingy.class.getResourceAsStream(ROOTTEMPLATE);
				if(is == null)
					throw new IllegalStateException(ROOTTEMPLATE);
				os = new FileOutputStream(new File(m_copyRoot, ROOTTEMPLATE));
				FileTool.copyFile(os, is);
			} finally {
				try {
					if(is != null)
						is.close();
				} catch(Exception x) {}
				try {
					if(os != null)
						os.close();
				} catch(Exception x) {}
			}
			m_rootTemplateRURL = COPYROOT + ROOTTEMPLATE;
		}
		m_initialized = true;
	}

	/**
	 * Tries to generate a "beautified" thingy for an exception. This locates a source and,
	 * if found, copies the data to a .jsp file in the WEB-INF directory; then it forwards
	 * to this jsp page.
	 */
	static public boolean generateException(HttpServlet slet, HttpServletRequest req, HttpServletResponse res, Throwable t) throws Exception {
		try {
			init(slet); // Make sure we have initialized.

			//-- Find an exception source as a file
			ExceptionSource xs = findExceptionSource(t);
			if(xs == null) {
				System.out.println("exceptionBeautifier: can't find a template for a " + t.getClass().getCanonicalName());
				return false; // Not found: nothing generated
			}
			Source s = xs.getSource();

			String pageurl = null;
			if(!s.needsCopy()) {
				pageurl = OVERRIDEROOT + s.getName();
			} else {
				File f = new File(m_copyRoot, s.getName());
				//				if(! f.exists())
				{
					OutputStream os = null;
					try {
						f.getParentFile().mkdirs();
						os = new FileOutputStream(f);
						FileTool.copyFile(os, s.getInput());
					} finally {
						try {
							if(os != null)
								os.close();
						} catch(Exception x) {}
					}
				}
				s.getInput().close();
				pageurl = COPYROOT + s.getName();
			}

			//-- Forward to the item
			req.setAttribute("x", xs.getException());
			String hn = NetTools.getApplicationURL(req);

			req.setAttribute("base", hn + "part/ExceptionBeautifier/" + s.getPath() + "/");
			req.setAttribute("include", pageurl);
			RequestDispatcher rd = req.getRequestDispatcher(m_rootTemplateRURL);
			res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			rd.forward(req, res);
			res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

			return true;
		} catch(Exception x) {
			System.out.println("internal: Exception during exception template processing: " + x);
			x.printStackTrace();
			throw x;
		}
	}

	static {
		addSource(DummyRootThingy.class, null);
		PartsRegistry.getInstance().register("ExceptionBeautifier", new ExceptionBeautifierPart());
	}
}
