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

import java.util.*;

import to.etc.domui.server.*;
import to.etc.util.*;

public class ParameterInfoImpl implements IParameterInfo {
	private Map<String, String[]> m_parameterMap = new HashMap<String, String[]>();

	public ParameterInfoImpl(String in) {
		String[] arg = in.split("&");
		if(arg == null || arg.length == 0)
			arg = new String[]{in};
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

	@Override
	public String getParameter(String name) {
		String[] v = m_parameterMap.get(name);
		if(v == null || v.length != 1)
			return null;
		return v[0];
	}

	@Override
	public String[] getParameterNames() {
		return m_parameterMap.keySet().toArray(new String[m_parameterMap.size()]);
	}

	@Override
	public String[] getParameters(String name) {
		return m_parameterMap.get(name);
	}
}
