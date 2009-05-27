package to.etc.domui.ajax;

import java.lang.annotation.*;
import java.util.*;

import to.etc.domui.annotations.*;
import to.etc.util.*;

public class JsonParameterProvider implements IParameterProvider {
	private final Map<Object, Object>		m_dataMap;

	public JsonParameterProvider(final Map<Object, Object> dataMap) {
		m_dataMap = dataMap;
	}

	public Object findParameterValue(final Class< ? > parameterType, final Annotation[] annotations, final int paramIndex, final AjaxParam apm) throws Exception {
		Object	val = m_dataMap.get(apm.value());
		if(val == null)
			return null;

		//-- FIXME Should move to generic converter.
		if(java.util.Date.class.isAssignableFrom(parameterType) && val instanceof Long) {
			return new Date( ((Long)val).longValue());
		}

		return RuntimeConversions.convertTo(val, parameterType);
	}
}
