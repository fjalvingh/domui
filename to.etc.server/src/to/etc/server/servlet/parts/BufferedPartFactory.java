package to.etc.server.servlet.parts;

import java.io.*;

import javax.servlet.*;

import to.etc.server.cache.*;
import to.etc.server.servlet.*;
import to.etc.server.vfs.*;

public interface BufferedPartFactory extends PartFactory {
	/**
	 * Decode the input and create a KEY for the request. This key must be hashable, and forms
	 * the key for the cache to retrieve an already generated copy.
	 *
	 * @param ctx
	 * @param rurl
	 * @return
	 * @throws Exception
	 */
	public Object decodeKey(VfsPathResolver vpr, RequestContext ctx, String rurl) throws Exception;

	public String generate(OutputStream os, Object key, DependencySet depset, VfsPathResolver vpr, ServletContext sctx) throws Exception;
}
