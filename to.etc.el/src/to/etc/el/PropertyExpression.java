package to.etc.el;

import javax.servlet.jsp.el.*;

import to.etc.el.node.*;
import to.etc.util.*;

/**
 * This encapsulates a "property" expression. This is a possibly 
 * complex property reference based off some unknown object. To
 * resolve the reference one needs to pass in the object where
 * the root of the property can be resolved on.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 16, 2006
 */
public class PropertyExpression {
	static private class TmpResolver implements VariableResolver {
		private Object m_root;

		TmpResolver(Object root) {
			m_root = root;
		}

		public Object resolveVariable(String name) throws ELException {
			if(name == null)
				return m_root;
			throw new ELException("Variables not allowed in a property expression.");
		}
	};

	private NdLookup m_expr;

	PropertyExpression(NdLookup bo) {
		m_expr = bo;
	}

	public String getProperty() {
		return m_expr.getExpression();
	}

	/**
	 * Must return true if the value is read-only.
	 * @return
	 */
	public boolean isReadOnly(Object root) throws ELException {
		return m_expr.isReadOnly(new TmpResolver(root));
	}

	/**
	 * The type of the expression. 
	 * @param vr		The resolver to use when evaluating variables
	 * @return
	 */
	public Class getType(Object root) throws ELException {
		return m_expr.getType(new TmpResolver(root));
	}

	/**
	 * Must return the value of the bound expression.
	 * @param vr		The resolver to use when evaluating variables
	 * @return
	 */
	public Object getValue(Object root, Class< ? > target) throws Exception {
		Object o = m_expr.evaluate(new TmpResolver(root));
		if(o == null || target == null)
			return o;
		return RuntimeConversions.convertTo(o, target);
	}

	/**
	 * Must set the bound expression's target to a value. If the thing is
	 * not writeable this throws an exception.
	 * @param vr		The resolver to use when evaluating variables
	 * @param value
	 * @throws Exception
	 */
	public void setValue(Object root, Object value) throws Exception {
		m_expr.setValue(new TmpResolver(root), value);
	}
}
