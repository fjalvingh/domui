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
package to.etc.domui.parts;

import to.etc.domui.server.*;
import to.etc.util.*;

import javax.annotation.*;
import java.util.*;

/**
 * This represents the parameters for simple web-like requests that would be sufficient to run most Parts.
 */
@DefaultNonNull
public class ParameterInfoImpl implements IParameterInfo {
	final private Map<String, String[]> m_parameterMap = new HashMap<String, String[]>();

	final private String m_rurl;

	/**
	 *
	 * @param rurl			A relative URL, if applicable, or the empty string if not.
	 * @param queryString   A string in URL encoded format, which will be split into parameters
	 */
	public ParameterInfoImpl(String rurl, String queryString) {
		m_rurl = rurl;
		String[] arg = queryString.split("&");
		if(arg == null || arg.length == 0)
			arg = new String[]{queryString};
		for(String s : arg) {
			int pos = s.indexOf('=');
			if(pos != -1) {
				String name = s.substring(0, pos);
				String value = s.substring(pos + 1);
				name = StringTool.decodeURLEncoded(name);
				value = StringTool.decodeURLEncoded(value);
				add(name, value);
			}
		}
	}

	public ParameterInfoImpl(String in) {
		this("", in);
	}

	private void add(String name, String value) {
		String[] v = m_parameterMap.get(name);
		if(v == null) {
			v = new String[]{value};
		} else {
			String[] nw = new String[v.length + 1];
			System.arraycopy(v, 0, nw, 0, v.length);
			nw[v.length] = value;
			v = nw;
		}
		m_parameterMap.put(name, v);
	}

	@Nullable
	@Override
	public String getParameter(@Nonnull String name) {
		String[] v = m_parameterMap.get(name);
		if(v == null || v.length != 1)
			return null;
		return v[0];
	}

	@Override
	@Nonnull
	public String[] getParameterNames() {
		return m_parameterMap.keySet().toArray(new String[m_parameterMap.size()]);
	}

	@Override
	@Nonnull
	public String[] getParameters(@Nonnull String name) {
		String[] res = m_parameterMap.get(name);
		return res == null ? new String[0] : res;
	}

	@Nonnull @Override public String getInputPath() {
		return m_rurl;
	}
}
