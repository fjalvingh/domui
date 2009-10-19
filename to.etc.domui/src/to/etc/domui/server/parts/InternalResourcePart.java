package to.etc.domui.server.parts;

import java.io.*;
import java.util.*;

import to.etc.domui.server.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.domui.util.resources.*;
import to.etc.util.*;
import to.etc.webapp.nls.*;

public class InternalResourcePart implements IBufferedPartFactory {
	private static class ResKey {
		private Locale m_loc;

		private String m_rurl;

		public ResKey(Locale loc, String rurl) {
			m_loc = loc;
			m_rurl = rurl;
		}

		public Locale getLoc() {
			return m_loc;
		}

		public String getRURL() {
			return m_rurl;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((m_loc == null) ? 0 : m_loc.hashCode());
			result = prime * result + ((m_rurl == null) ? 0 : m_rurl.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(getClass() != obj.getClass())
				return false;
			ResKey other = (ResKey) obj;
			if(m_loc == null) {
				if(other.m_loc != null)
					return false;
			} else if(!m_loc.equals(other.m_loc))
				return false;
			if(m_rurl == null) {
				if(other.m_rurl != null)
					return false;
			} else if(!m_rurl.equals(other.m_rurl))
				return false;
			return true;
		}
	}

	public Object decodeKey(String rurl, IParameterInfo param) throws Exception {
		//-- Is this an URL containing an nls'ed resource?
		Locale loc = null;
		int pos = rurl.lastIndexOf(".nls.");
		if(-1 != pos) {
			loc = NlsContext.getLocale();
			rurl = rurl.substring(0, pos) + rurl.substring(pos + 5);
		}
		if(rurl.endsWith(".class") /* || rurl.endsWith(".java") */)
			throw new ThingyNotFoundException(rurl);

		//-- Create the key.
		return new ResKey(loc, rurl);
	}

	/**
	 * Generate the local resource. This first checks to see if the resource is "externalized" into the webapp's
	 * files; if so we use the copy from there. Otherwise we expect the file to reside as a class resource rooted
	 * by the /resources path in the classpath.
	 * Resources are usually returned with an Expires: header allowing the browser to cache the resources for up to
	 * a week. However, to allow for easy debugging, you can disable all expiry header generation using a developer
	 * flag in $HOME/.developer.properties: domui.expires=false. In addition, resources generated from the webapp do
	 * not get an expires header when the server runs in DEBUG mode.
	 *
	 * @see to.etc.domui.server.parts.IBufferedPartFactory#generate(to.etc.domui.server.parts.PartResponse, to.etc.domui.server.DomApplication, java.lang.Object, to.etc.domui.util.resources.ResourceDependencyList)
	 */
	public void generate(PartResponse pr, DomApplication da, Object inkey, ResourceDependencyList rdl) throws Exception {
		ResKey k = (ResKey) inkey;

		//-- 1. Locate the resource
		IResourceRef ires;
		if(k.getLoc() == null) {
			String rurl = k.getRURL();


			if(rurl.startsWith("$RES/")) {
				//-- Java resource
				ires = new ClassResourceRef(getClass(), rurl.substring(4));
			} else if(!rurl.startsWith("$"))
				throw new IllegalStateException("Internal: bad rurl passed, missing $");
			else {
				rurl = rurl.substring(1);

				//-- 1. Is a file-based resource available?
				File f = da.getAppFile(rurl);
				if(f.exists()) {
					ires = new WebappResourceRef(f);
					if(!da.inDevelopmentMode()) // Webapp resources are cached ONLY when in production mode.
						pr.setCacheTime(da.getDefaultExpiryTime());
				} else {
					//-- In the url, replace all '.' but the last one with /
					//				String	name = k.getRURL();
					//				int	pos	= name.lastIndexOf('.');
					//				if(pos != -1) {
					//					name = name.substring(0, pos).replace('.', '/')+name.substring(pos);
					//				}
					ires = new ClassResourceRef(getClass(), "/resources/" + rurl);
					pr.setCacheTime(da.getDefaultExpiryTime()); // Allow caching for a long time
				}
				if(rdl != null)
					rdl.add(ires);
			}

			pr.setMime(ServerTools.getExtMimeType(FileTool.getFileExtension(rurl)));
			InputStream is = ires.getInputStream();
			if(is == null)
				throw new ThingyNotFoundException(k.getRURL());
			try {
				FileTool.copyFile(pr.getOutputStream(), is);
			} finally {
				try {
					is.close();
				} catch(Exception x) {}
			}
			return;
		}
		throw new ThingyNotFoundException(k.getLoc() + "/" + k.getRURL());
	}
}
