package to.etc.domui.ajax;

import to.etc.domui.server.*;
import to.etc.server.ajax.*;

public class AjaxRequestContext {
	private final RequestContextImpl		m_rctx;
	private final AjaxRequestHandler		m_rh;

	public AjaxRequestContext(final AjaxRequestHandler ajaxRequestHandler, final RequestContextImpl ctx) {
		m_rh = ajaxRequestHandler;
		m_rctx = ctx;
	}

	public RequestContextImpl getRctx() {
		return m_rctx;
	}
	public void	execute() throws Exception {
		try {
			String rurl = m_rctx.getInputPath();
			if(rurl == null || rurl.length() == 0)
				throw new AjaxException("Missing url segment");
			int sx = 0;
			int ex = rurl.length();
			if(rurl.startsWith("/"))
				sx = 1;
			if(rurl.endsWith("/"))
				ex = ex - 1;
			int pos = rurl.lastIndexOf('.'); // Remove the suffix (.ajax usually)
			if(pos != -1)
				ex = pos;
			rurl = rurl.substring(sx, ex); // Base name contains class and method.
			if(rurl.equals("bulk")) {
				executeBulkRequest();
				return;
			}

			//-- If a format override is present get it,
			String s = m_rctx.getParameter("_format"); // Format override present in request?
			ResponseFormat rf = null;
			if(s != null)
				rf = ResponseFormat.valueOf(s);
//			List<Class< ? extends Object>> sourceList = new ArrayList<Class< ? extends Object>>(m_ajax.getRequestCaller().getSourceClassesList());
//			sourceList.add(0, getClass());
			m_rh.executeSingleCall(this, rurl, rf);
			
//			m_ajax.getRequestCaller().executeSingleCall(this, sourceList, rurl, rf);
//		} catch(ServiceException sx) {
//			sx.setContext(this);
//			throw sx;
//		} catch(Exception x) {
//			throw new ServiceException(this, x.toString(), x);
		} finally {
//			releaseSources();
		}
	}

	/**
	 * Handles a bulk request using either JSON or XML. A bulk request
	 * is a set of calls executed in sequence. The bulk request must
	 * specify either a parameter containing the bulk request's data, or
	 * the input must be a stream containing it [not implemented yet].
	 */
	private void executeBulkRequest() throws Exception {
//		String json = getRequest().getParameter("json");
//		String xml = getRequest().getParameter("xml");
//		if(xml != null && json != null)
//			;
//		else if(json != null) {
//			m_ajax.getBulkCaller().executeBulkJSON(this, json);
//			return;
//		}
//		else if(xml != null) {
//			throw new IllegalStateException("xml bulk call not implemented yet");
//		}
		throw new IllegalStateException(
				"Bulk requests must be called using json= or xml= as parameter, or with an appropriate mime type and the call data in the request input stream (body)");
	}


}
