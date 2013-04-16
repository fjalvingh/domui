/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.util.upload;

import java.util.*;

import javax.servlet.http.*;

import to.etc.util.*;

public class UploadHttpRequestWrapper extends HttpServletRequestWrapper {
	static public final String UPLOADKEY = "to.etc.domui.up$load$key";

	/** Indexed by name, contains both file and normal parameters. */
	private Map<String, String[]> m_formItemMap = new HashMap<String, String[]>();

	private Map<String, UploadItem[]> m_fileItemMap = new HashMap<String, UploadItem[]>();

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
		dfu.setSizeMax(100 * 1024 * 1024); // Max upload size

		List<UploadItem> l;
		try {
			l = dfu.parseRequest(req, req.getCharacterEncoding());
		} catch(Exception x) {
			x.printStackTrace();
			throw new WrappedException(x);
		}

		Map<String, List<String>> parammap = new HashMap<String, List<String>>();
		Map<String, List<UploadItem>> filemap = new HashMap<String, List<UploadItem>>();
		for(int i = 0; i < l.size(); i++) {
			UploadItem fi = l.get(i);
			String name = fi.getName().toLowerCase();
			if(!fi.isFile()) {
				List<String> v = parammap.get(name);
				if(v == null) {
					v = new ArrayList<String>(5);
					parammap.put(name, v);
				}
				v.add(fi.getValue());
				System.out.println("~~ form item name=" + name);
			} else {
				//-- This is some kind of FILE thingy..
				//				System.out.println("~~ file item name=" + name);
				List<UploadItem> v = filemap.get(name);
				if(v == null) {
					v = new ArrayList<UploadItem>(5);
					filemap.put(name, v);
				}
				v.add(fi);
			}
		}

		//-- Convert all ArrayLists to array
		for(Map.Entry<String, List<String>> me : parammap.entrySet()) {
			m_formItemMap.put(me.getKey(), me.getValue().toArray(new String[me.getValue().size()]));

		}
		for(Map.Entry<String, List<UploadItem>> me: filemap.entrySet()) {
			m_fileItemMap.put(me.getKey(), me.getValue().toArray(new UploadItem[me.getValue().size()]));
		}
		req.setAttribute(UPLOADKEY, this);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Overrides for form items.							*/
	/*--------------------------------------------------------------*/
	@Override
	public String getParameter(String name) {
		String[] ar = m_formItemMap.get(name.toLowerCase());
		return ar == null ? null : ar[0];
	}

	@Override
	public String[] getParameterValues(String name) {
		return m_formItemMap.get(name.toLowerCase());
	}

	@Override
	public Enumeration<String> getParameterNames() {
		Vector<String> v = new Vector<String>(m_formItemMap.keySet());
		return v.elements();
	}

	//-- Get all parameters in a genericized map
	@Override
	public Map<String, String[]> getParameterMap() {
		return m_formItemMap;
	}

	public Map<String, UploadItem[]> getFileItemMap() {
		return m_fileItemMap;
	}

	public UploadItem getFileItem(String name) {
		UploadItem[] ar = m_fileItemMap.remove(name.toLowerCase());
		if(ar == null)
			return null;
		if(ar.length > 1) {
			for(UploadItem ui : ar)
				ui.discard();
			throw new IllegalStateException("Multiple file items for name=" + name);
		}
		return ar[0];
	}

	public UploadItem[] getFileItems(String name) {
		return m_fileItemMap.remove(name.toLowerCase());
	}

	public void releaseFiles() {
		for(UploadItem[] uiar : m_fileItemMap.values()) {
			for(UploadItem ui : uiar) {
				try {
					ui.discard();
				} catch(Exception x) {}
			}
		}
	}
}
