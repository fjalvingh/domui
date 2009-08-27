package to.etc.server.servlet;

import java.util.*;

import javax.servlet.http.*;

import to.etc.server.upload.*;
import to.etc.util.*;

public class UploadHttpRequestWrapper extends HttpServletRequestWrapper {
	static public final String	UPLOADKEY		= "to.mumble.nema.up$load$key";

	/** Indexed by name, contains both file and normal parameters. */
	private Hashtable			m_formItemMap	= new Hashtable();

	private Hashtable			m_fileItemMap	= new Hashtable();

	public UploadHttpRequestWrapper(HttpServletRequest req) {
		super(req);
		init(req);
	}

	static public UploadHttpRequestWrapper findWrapper(HttpServletRequest req) {
		if(req instanceof UploadHttpRequestWrapper)
			return (UploadHttpRequestWrapper) req;
		return (UploadHttpRequestWrapper) req.getAttribute(UPLOADKEY);
	}

	private void init(HttpServletRequest req) {
		if(!UploadParser.isMultipartContent(req))
			throw new IllegalStateException("Cannot wrap a non-multipart request!");
		UploadParser dfu = new UploadParser();
		dfu.setSizeMax(20 * 1024 * 1024); // Max upload size
		dfu.setFenceSize(100 * 1024); // max 100k in memory.

		ArrayList formnames = new ArrayList();
		ArrayList filenames = new ArrayList();
		List l;
		try {
			l = dfu.parseRequest(req, req.getCharacterEncoding());
		} catch(Exception x) {
			x.printStackTrace();
			throw new WrappedException(x);
		}
		for(int i = 0; i < l.size(); i++) {
			UploadItem fi = (UploadItem) l.get(i);
			String name = fi.getName().toLowerCase();
			if(!fi.isFileItem()) {
				Object v = m_formItemMap.get(name);
				if(v == null)
					m_formItemMap.put(name, new String[]{fi.getValue()});
				else if(v instanceof ArrayList)
					((ArrayList) v).add(fi.getValue());
				else {
					ArrayList a = new ArrayList(5);
					a.add(((String[]) v)[0]);
					a.add(fi.getValue());
					m_formItemMap.put(name, a);
					formnames.add(name);
				}
				System.out.println("~~ form item name=" + name);
			} else {
				//-- This is some kind of FILE thingy..
				System.out.println("~~ file item name=" + name);
				Object v = m_fileItemMap.get(name);
				if(v == null)
					m_fileItemMap.put(name, new UploadItem[]{fi});
				else if(v instanceof ArrayList)
					((ArrayList) v).add(fi);
				else {
					ArrayList a = new ArrayList(5);
					a.add(((UploadItem[]) v)[0]);
					a.add(fi);
					m_formItemMap.put(name, a);
					filenames.add(name);
				}
			}
		}

		//-- Convert all ArrayLists to array
		for(int i = formnames.size(); --i >= 0;) {
			String name = (String) formnames.get(i);
			ArrayList a = (ArrayList) m_formItemMap.get(name);
			m_formItemMap.put(name, a.toArray(new String[a.size()]));
		}
		for(int i = filenames.size(); --i >= 0;) {
			String name = (String) filenames.get(i);
			ArrayList a = (ArrayList) m_fileItemMap.get(name);
			m_fileItemMap.put(name, a.toArray(new UploadItem[a.size()]));
		}
		req.setAttribute(UPLOADKEY, this);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Overrides for form items.							*/
	/*--------------------------------------------------------------*/
	@Override
	public String getParameter(String name) {
		String[] ar = (String[]) m_formItemMap.get(name.toLowerCase());
		return ar == null ? null : ar[0];
	}

	@Override
	public String[] getParameterValues(String name) {
		return (String[]) m_formItemMap.get(name.toLowerCase());
	}

	@Override
	public Enumeration getParameterNames() {
		return m_formItemMap.keys();
	}

	//-- Get all parameters in a genericized map
	@Override
	public Map getParameterMap() {
		return m_formItemMap;
	}

	public Hashtable getFileItemMap() {
		return m_fileItemMap;
	}

	public UploadItem getFileItem(String name) {
		UploadItem[] ar = (UploadItem[]) m_fileItemMap.get(name.toLowerCase());
		return ar == null ? null : ar[0];
	}

	public UploadItem[] getFileItems(String name) {
		return (UploadItem[]) m_fileItemMap.get(name.toLowerCase());
	}
}
