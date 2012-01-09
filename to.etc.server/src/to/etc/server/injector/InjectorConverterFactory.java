package to.etc.server.injector;

import java.lang.annotation.*;

public interface InjectorConverterFactory {
	/**
	 * Must return TRUE if this provider accepts a method parameter of
	 * the specified type with the specified annotations.
	 *
	 * @param type		The type of the parameter, never null
	 * @param anar		The list of annotations to consider for this parameter, never null but can be [0].
	 * @return			null if not accepted or a parameter provider if it is.
	 */
	public InjectorConverter accepts(Class totype, Class fromtype, Annotation[] anar) throws Exception;
}
