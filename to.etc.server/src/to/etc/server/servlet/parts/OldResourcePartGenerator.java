package to.etc.server.servlet.parts;

import java.io.*;

import javax.servlet.*;

import to.etc.server.*;
import to.etc.server.cache.*;
import to.etc.server.servlet.*;
import to.etc.server.vfs.*;
import to.etc.util.*;

public class OldResourcePartGenerator implements BufferedPartFactory {
	static private final String	PREFIX	= "$respg";

	/**
	 * Decodes the request into a resource to generate. This simply returns
	 * the resource name preceded with a prefix to make it unique.
	 *
	 * @see to.etc.server.servlet.parts.BufferedPartFactory#decodeKey(to.etc.server.vfs.VfsPathResolver, to.etc.server.servlet.RequestContext, java.lang.String)
	 */
	public Object decodeKey(VfsPathResolver vpr, RequestContext ctx, String rurl) throws Exception {
		String k = ctx.getRequest().getParameter("img");
		if(k == null)
			k = ctx.getRequest().getParameter("key");
		if(k == null)
			throw new IllegalStateException("Missing 'key=' parameter identifying the resource.");
		return PREFIX + k;
	}

	public String generate(OutputStream os, Object key, DependencySet depset, VfsPathResolver vpr, ServletContext sctx) throws Exception {
		depset.add(key);
		String rurl = ((String) key).substring(PREFIX.length());
		String ext = FileTool.getFileExtension(rurl);
		String mime = ServerTools.getExtMimeType(ext);
		if(mime == null)
			throw new IllegalStateException("Unknown mime type for extension " + ext);
		InputStream is = null;
		try {
			String rp = sctx.getRealPath("WEB-INF/resources/" + rurl);
			File f = new File(rp);
			try {
				is = new FileInputStream(f);
			} catch(Exception x) {
				//-- File failed- try a resource
				is = OldResourcePartGenerator.class.getResourceAsStream("/resources/" + rurl);
				if(is == null)
					throw new IllegalStateException("Resource " + rurl + " not found.");
			}

			FileTool.copyFile(os, is);
			return mime;
		} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception x) {}
		}
	}
}
