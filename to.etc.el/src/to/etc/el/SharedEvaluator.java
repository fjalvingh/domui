/*
 * DomUI Java User Interface - shared code
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
package to.etc.el;

import java.util.*;

import javax.servlet.jsp.el.*;

import to.etc.el.node.*;

/**
 * Shared singleton instance of the evaluator which contains the cached shit.
 *
 * Created on May 18, 2005
 * @author jal
 */
public class SharedEvaluator {
	static private SharedEvaluator m_instance = new SharedEvaluator();

	private Map<String, ElExpressionImpl> m_cache_map = new Hashtable<String, ElExpressionImpl>();

	private SharedEvaluator() {}

	static public SharedEvaluator getInstance() {
		return m_instance;
	}

	/**
	 * Return the cached expression or make a new one.
	 * @param expression
	 * @param expectedType
	 * @param mapper
	 * @return
	 * @throws ELException
	 */
	public ElExpressionImpl parseExpression(String expression, Class< ? > expectedType, FunctionMapper mapper, String expressionsource) throws ELException {
		ElExpressionImpl xi = null;
		ElExpressionImpl xic = null;
		String key = null;
		synchronized(m_cache_map) {
			if(expectedType != null && expectedType != Object.class) // Must we convert?
			{
				key = expression + "." + expectedType.getClass(); // Then make a new key
				xic = m_cache_map.get(key);
				if(xic != null)
					return xic;
			}

			//-- No need for conversion OR the expr with conversion was not found....
			xi = m_cache_map.get(expression); // Find expression sans conversion
			if(xi != null) {
				if(key != null) // Do we need the converted one?
				{
					NdBase b = new NdConvert(xi.getNode(), expectedType);
					xic = new ElExpressionImpl(b, expressionsource);
					m_cache_map.put(key, xic);
					return xic; // Return the converted expr
				}

				//-- We need the literal one.
				return xi;
			}
		}

		//-- We need to parse the expression and store it
		CoreElEvaluator e = new CoreElEvaluator();
		NdBase base = e.evaluate(expression, mapper); // Get a base node for the expression
		xi = new ElExpressionImpl(base, expressionsource);
		synchronized(m_cache_map) {
			m_cache_map.put(expression, xi);
			if(key != null) {
				NdBase b = new NdConvert(xi.getNode(), expectedType);
				xic = new ElExpressionImpl(b, expressionsource);
				m_cache_map.put(key, xic);
				return xic; // Return the converted expr
			}
		}
		return xi;
	}

	/**
	 * Return the cached expression or make a new one.
	 * @param expression
	 * @param expectedType
	 * @return
	 * @throws ELException
	 */
	public PropertyExpression parsePropertyExpression(String expression) throws ELException {
		CoreElEvaluator e = new CoreElEvaluator();
		return e.evaluatePropertyExpression(expression, null);
	}
}
