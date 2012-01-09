package to.etc.server.servlet.parts;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import to.etc.server.servlet.*;
import to.etc.server.vfs.*;
import to.etc.util.*;

/**
 * This servlet is used to generate all "content parts". It uses the parts framework
 * which caches part recipes and generated parts if needed.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 18, 2006
 */
public class PartsServlet extends ContextServletBase {
	private VfsPathResolver	m_resolver;

	public PartsServlet() {
		super(false);
	}

	public VfsPathResolver getResolver() {
		return m_resolver;
	}

	@Override
	public void init() throws ServletException {
		super.init();
		try {
			String rp = getServletContext().getRealPath("/");
			m_resolver = VFS.getInstance().makeFilesystemResolver(new File(rp), "utf-8");
		} catch(RuntimeException x) {
			throw x;
		} catch(Exception x) {
			throw new WrappedException(x);
		}
	}

	@Override
	public ContextServletContext makeContext(HttpServletRequest req, HttpServletResponse res, boolean ispost) {
		return new ContextServletContextBase(this, req, res, ispost) {
			@Override
			public void execute() throws Exception {
				PartsRegistry.getInstance().generatePart(getResolver(), this);
			}
		};
	}
}
