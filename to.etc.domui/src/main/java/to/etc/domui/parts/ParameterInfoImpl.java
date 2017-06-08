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
import java.util.function.Predicate;

/**
 * This represents the parameters for simple web-like requests that would be sufficient to run most Parts.
 */
@DefaultNonNull
public class ParameterInfoImpl implements IParameterInfo {
	final private Map<String, List<String>> m_parameterMap = new HashMap<>();

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

	/**
	 * Copy parameters from another.
	 * @param param
	 */
	public ParameterInfoImpl(IParameterInfo param, Predicate<String> copyPredicate) {
		m_rurl = param.getInputPath();
		for(String name : param.getParameterNames()) {
			if(copyPredicate.test(name)) {
				List<String> parameters = new ArrayList<>(Arrays.asList(param.getParameters(name)));
				m_parameterMap.put(name, parameters);
			}
		}
	}
	public ParameterInfoImpl(IParameterInfo param) {
		m_rurl = param.getInputPath();
		for(String name : param.getParameterNames()) {
			List<String> parameters = new ArrayList<>(Arrays.asList(param.getParameters(name)));
			m_parameterMap.put(name, parameters);
		}
	}

	private void add(String name, String value) {
		List<String> v = m_parameterMap.get(name);
		if(v == null) {
			v = new ArrayList<>();
			m_parameterMap.put(name, v);
		}
		v.add(value);
	}

	@Nullable
	@Override
	public String getParameter(@Nonnull String name) {
		List<String> v = m_parameterMap.get(name);
		if(v == null || v.size() != 1)
			return null;
		return v.get(0);
	}

	@Override
	@Nonnull
	public String[] getParameterNames() {
		return m_parameterMap.keySet().toArray(new String[m_parameterMap.size()]);
	}

	@Override
	@Nonnull
	public String[] getParameters(@Nonnull String name) {
		List<String> res = m_parameterMap.get(name);
		return res == null ? new String[0] : res.toArray(new String[res.size()]);
	}

	@Nonnull @Override public String getInputPath() {
		return m_rurl;
	}

	@Override public boolean equals(@Nullable Object o) {
		if(this == o)
			return true;
		if(o == null || getClass() != o.getClass())
			return false;
		ParameterInfoImpl that = (ParameterInfoImpl) o;
		return Objects.equals(m_parameterMap, that.m_parameterMap) &&
			Objects.equals(m_rurl, that.m_rurl);
	}

	@Override public int hashCode() {
		return Objects.hash(m_parameterMap, m_rurl);
	}
}
