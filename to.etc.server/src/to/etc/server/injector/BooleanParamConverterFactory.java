package to.etc.server.injector;

import java.lang.annotation.Annotation;
import java.text.*;

import org.w3c.dom.*;

import to.etc.util.*;
import to.etc.xml.*;

public class BooleanParamConverterFactory implements InjectorConverterFactory {
	static SimpleDateFormat	m_sdf	= new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	public InjectorConverter accepts(Class totype, Class fromtype, Annotation[] anar) throws Exception {
		if(totype != Boolean.class && totype != Boolean.TYPE)
			return null;

		if(Boolean.class.isAssignableFrom(fromtype)) {
			return new InjectorConverter() {
				public Object convertValue(Object source) throws Exception {
					if(source == null)
						return null;
					if(!(source instanceof Boolean))
						throw new RuntimeConversionException("Can't convert a " + source.getClass() + " to a Boolean");
					return source;
				}
			};
		}

		if(String.class.isAssignableFrom(fromtype)) {
			return new InjectorConverter() {
				public Object convertValue(Object source) throws Exception {
					if(source == null)
						return null;
					if(!(source instanceof String))
						throw new RuntimeConversionException("Can't convert a " + source.getClass() + " to a Boolean");
					String s = (String) source;
					return Boolean.valueOf(s);
				}
			};
		}

		if(Node.class.isAssignableFrom(fromtype)) {
			return new InjectorConverter() {
				public Object convertValue(Object source) throws Exception {
					if(source == null)
						return null;
					Node n = (Node) source;
					String s = DomTools.textFrom(n);
					//					System.out.println("date : convert from '"+s+"'");
					if(s.length() == 0)
						return null;
					return Boolean.valueOf(s);
				}
			};
		}

		return null;
	}
}
