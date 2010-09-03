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
