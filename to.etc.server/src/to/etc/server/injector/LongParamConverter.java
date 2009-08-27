package to.etc.server.injector;

import java.lang.annotation.*;

public class LongParamConverter implements InjectorConverter {
	static public final InjectorConverterFactory	FACTORY	= new InjectorConverterFactory() {
																public InjectorConverter accepts(Class totype, Class fromtype, Annotation[] anar) throws Exception {
																	if(totype != Long.class && totype != Long.TYPE)
																		return null;
																	if(fromtype != String.class)
																		return null;
																	return new LongParamConverter();
																}
															};

	public LongParamConverter() {
	}

	public Object convertValue(Object source) throws Exception {
		if(source instanceof Number)
			return Long.valueOf(((Number) source).longValue());
		return Long.valueOf(((String) source).trim());
	}
}
