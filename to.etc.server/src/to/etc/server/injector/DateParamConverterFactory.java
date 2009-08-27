package to.etc.server.injector;

import java.lang.annotation.Annotation;
import java.text.*;
import java.util.*;

import org.w3c.dom.*;

import to.etc.xml.*;

public class DateParamConverterFactory implements InjectorConverterFactory {
	static SimpleDateFormat	m_sdf	= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public InjectorConverter accepts(Class totype, Class fromtype, Annotation[] anar) throws Exception {
		if(totype != java.util.Date.class)
			return null;
		if(Calendar.class.isAssignableFrom(fromtype))
			return new InjectorConverter() {
				public Object convertValue(Object source) throws Exception {
					if(source == null)
						return null;
					return ((Calendar) source).getTime();
				}
			};

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
					return m_sdf.parseObject(s);
				}
			};
		}

		return new InjectorConverter() {
			public Object convertValue(Object source) throws Exception {
				if(source == null)
					return null;
				if(source instanceof String)
					source = Long.decode((String) source);
				if(source instanceof Long)
					return new Date(((Long) source).longValue());
				throw new IllegalArgumentException("The object '" + source + "' could not be converted to a Date");
			}
		};
	}
}
