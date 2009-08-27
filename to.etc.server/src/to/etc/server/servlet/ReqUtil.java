package to.etc.server.servlet;

import javax.servlet.*;
import javax.servlet.jsp.*;

public class ReqUtil {
	private ReqUtil() {
	}

	static public long getLong(ServletRequest req, String name) throws Exception {
		String n = req.getParameter(name);
		if(n != null) {
			n = n.trim();
			try {
				return Long.parseLong(n);
			} catch(Exception x) {}
		}
		throw new IllegalStateException("Missing or invalid mandatory URL parameter '" + name + "'");
	}

	static public long getLong(ServletRequest req, String name, long deflt) throws Exception {
		String n = req.getParameter(name);
		if(n == null)
			return deflt;
		n = n.trim();
		try {
			return Long.parseLong(n);
		} catch(Exception x) {}
		throw new IllegalStateException("Invalid 'long' URL parameter '" + name + "': " + n);
	}

	static public int getInt(ServletRequest req, String name) throws Exception {
		String n = req.getParameter(name);
		if(n != null) {
			n = n.trim();
			try {
				return Integer.parseInt(n);
			} catch(Exception x) {}
		}
		throw new IllegalStateException("Missing or invalid mandatory URL parameter '" + name + "'");
	}

	static public int getInt(ServletRequest req, String name, int deflt) throws Exception {
		String n = req.getParameter(name);
		if(n == null)
			return deflt;
		n = n.trim();
		try {
			return Integer.parseInt(n);
		} catch(Exception x) {}
		throw new IllegalStateException("Invalid 'int' URL parameter '" + name + "': " + n);
	}

	static public int getInt(PageContext pc, String name) throws Exception {
		return getInt(pc.getRequest(), name);
	}

	static public int getInt(PageContext pc, String name, int deflt) throws Exception {
		return getInt(pc.getRequest(), name, deflt);
	}

	static public long getLong(PageContext pc, String name) throws Exception {
		return getLong(pc.getRequest(), name);
	}

	static public long getLong(PageContext pc, String name, long deflt) throws Exception {
		return getLong(pc.getRequest(), name, deflt);
	}
}
