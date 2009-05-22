package to.etc.domui.ajax;

import java.lang.annotation.*;

import to.etc.server.ajax.*;

public interface IParameterProvider {
	static public final String	NO_VALUE = "$*$novalue";

	/**
	 * Retrieve the specified parameter's value from whatever input is provided.
	 * @param parameterType
	 * @param annotations
	 * @param paramIndex
	 * @param apm
	 * @return
	 * @throws Exception
	 */
	Object findParameterValue(Class< ? > parameterType, Annotation[] annotations, int paramIndex, AjaxParam apm) throws Exception;
}
