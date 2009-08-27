package to.etc.server.injector;

import java.lang.annotation.*;

public class IntParamConverter implements InjectorConverter {
	static public final InjectorConverterFactory	FACTORY	= new InjectorConverterFactory() {
																public InjectorConverter accepts(Class totype, Class fromtype, Annotation[] anar) throws Exception {
																	if(totype != Integer.class && totype != Integer.TYPE)
																		return null;
																	if(fromtype != String.class)
																		return null;
																	return new IntParamConverter();
																}
															};

	public IntParamConverter() {
	}

	public Object convertValue(Object source) throws Exception {
		if(source instanceof Integer)
			return source;
		if(source instanceof Number)
			return Integer.valueOf(((Number) source).intValue());
		if(source instanceof String)
			return Integer.valueOf(((String) source).trim());
		throw new IllegalStateException("Cannot convert " + source.getClass() + " to an integer");
	}
}
