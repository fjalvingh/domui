package to.etc.binaries.cache;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import to.etc.server.*;

public class BinariesServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		long originalid = ServerTools.getLong(req, "oid");
		int w = ServerTools.getInt(req, "w", -1);
		int h = ServerTools.getInt(req, "h", -1);
		String mime = req.getParameter("mime");
		if(mime == null && w != -1 && h != -1)
			mime = "image/png";
		BinaryRef ref;
		try {
			ref = BinariesCache.getInstance().getObject(Long.valueOf(originalid), "raster", mime, w, h);
		} catch(RuntimeException rx) {
			throw rx;
		} catch(IOException iox) {
			throw iox;
		} catch(Exception x) {
			throw new ServletException(x.toString(), x);
		}
		ref.generate(res);
	}
}
