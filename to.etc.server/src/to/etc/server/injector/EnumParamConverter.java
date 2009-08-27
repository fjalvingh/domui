package to.etc.server.injector;

import java.lang.annotation.*;

public class EnumParamConverter implements InjectorConverter {
	private Class									m_typeClass;

	static public final InjectorConverterFactory	FACTORY	= new InjectorConverterFactory() {
																public InjectorConverter accepts(Class totype, Class fromtype, Annotation[] anar) throws Exception {
																	if(!Enum.class.isAssignableFrom(totype))
																		return null;
																	if(fromtype != String.class)
																		return null;
																	return new EnumParamConverter(totype);
																}
															};

	public EnumParamConverter(Class to) {
		m_typeClass = to;
	}

	public Object convertValue(Object source) throws Exception {
		String value = (String) source;
		value = value.trim();
		Class<Enum> cl = /* (Class<Enum>) */m_typeClass;
		Enum[] ar = cl.getEnumConstants();
		if(ar == null)
			throw new IllegalStateException("!? No enum constants for enum class " + m_typeClass.getCanonicalName());
		for(Enum en : ar) {
			if(en.name().equalsIgnoreCase(value))
				return en;
		}
		throw new IllegalStateException("The value '" + value + "' is not a valid enum name for the enum '" + m_typeClass.getCanonicalName());
	}
}
