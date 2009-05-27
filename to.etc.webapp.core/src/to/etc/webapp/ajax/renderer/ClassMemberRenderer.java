package to.etc.webapp.ajax.renderer;

import java.lang.reflect.*;

import to.etc.util.*;

public class ClassMemberRenderer {
	static public final String INVALID = "(invalid)";

	/** The method to call on the object to retrieve it's value */
	private final Method m_method;

	/** The name of the property represented by this value, as obtained from the method name. */
	private final String m_name;

	ClassMemberRenderer(final Method m, final String name) {
		m_method = m;
		m_name = name;
	}

	public Method getMethod() {
		return m_method;
	}

	public String getName() {
		return m_name;
	}

	public Object getMemberValue(final Object val) throws RenderMethodException {
		try {
			return m_method.invoke(val, (Object[]) null); // Call the getter
		} catch(Exception x) {
			if(x instanceof InvocationTargetException) {
				if(x.getCause() instanceof Exception)
					x = (Exception) x.getCause();
			}
			throw new RenderMethodException(m_method, "Class member getter call '" + m_method.toString() + "' failed with " + StringTool.getExceptionMessage(x), x);
		}
	}

	public int render(final ObjectRenderer or, final Object val, final int count) throws Exception {
		Object retval = getMemberValue(val);
		//		System.out.println("renderthingy: get "+m_name+" returned "+retval);
		if(retval == INVALID)
			return count;
		if(or.isKnownObject(retval))
			return count;
		or.renderObjectBeforeItem(count, val, m_name, m_method.getReturnType());
		or.renderObjectMember(retval, m_name, m_method.getReturnType());
		or.renderObjectAfterItem(count, val, m_name, m_method.getReturnType());
		return count + 1;
	}
}
