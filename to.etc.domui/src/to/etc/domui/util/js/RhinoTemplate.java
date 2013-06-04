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
package to.etc.domui.util.js;

import java.util.*;

import org.mozilla.javascript.*;

import to.etc.template.*;

/**
 * A single template which can be generated.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 26, 2010
 */
public class RhinoTemplate {
	final private String m_source;

	final private Script m_code;

	final private List<JSLocationMapping> m_locMap;

	public RhinoTemplate(String source, Script code, List<JSLocationMapping> locMap) {
		m_source = source;
		m_code = code;
		m_locMap = locMap;
	}

	final public String getSource() {
		return m_source;
	}

	/**
	 * Execute this compiled script using the specified scope.
	 * @param tc
	 * @param scope
	 * @return
	 */
	public Object execute(IJSTemplateContext tc, Scriptable scope) {
		scope.put("out", scope, tc);
		Context cx = Context.enter();
		try {
			return m_code.exec(cx, scope);
		} catch(RhinoException sx) {
			int[] res = JSTemplateCompiler.remapLocation(m_locMap, sx.lineNumber(), sx.columnNumber());
			throw new JSTemplateError(sx, sx.getMessage(), m_source, res[0], res[1]);
		} finally {
			Context.exit();
		}
	}

	private IJSTemplateContext createContext(final Appendable a) {
		return new IJSTemplateContext() {
			@Override
			public void writeValue(Object v) throws Exception {
				if(v == null)
					return;
				if(v instanceof Double) {
					String res = v.toString();
					if(res.endsWith(".0"))
						res = res.substring(0, res.length() - 2);
					a.append(res);
				} else {
					a.append(v.toString());
				}
			}

			@Override
			public void write(String text) throws Exception {
				a.append(text);
			}
		};
	}

	/**
	 * Execute this template, and leave the result in the specified appendable.
	 * @param a
	 * @param assignments
	 */
	public Object execute(final Appendable a, Scriptable scope) {
		return execute(createContext(a), scope);
	}

	/**
	 * Execute this template, and leave the result in the specified appendable.
	 * @param a
	 * @param assignments
	 */
	public Object execute(final Appendable a, IScriptScope scope) {
		Scriptable ss = scope.getAdapter(Scriptable.class);
		return execute(createContext(a), ss);
	}

	//	/**
	//	 * Execute this template, and leave the result in the specified appendable.
	//	 * @param a
	//	 * @param assignments
	//	 */
	//	public Object execute(final Appendable a, Map<String, Object> assignments) {
	//		return execute(createContext(a), assignments);
	//	}
}
