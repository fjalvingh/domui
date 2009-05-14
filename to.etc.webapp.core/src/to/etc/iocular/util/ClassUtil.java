package to.etc.iocular.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ClassUtil {
	private ClassUtil() {}

	static public <T> Class<T>		byName(Class<T> cls, String name) {
		try {
			Class<?> c = Class.forName(name);
			if(cls.isAssignableFrom(c))
				return (Class<T>) c;
			throw new IllegalStateException("The class '"+name+"' is not of type "+cls);
		} catch(ClassNotFoundException x) {
			x.printStackTrace();
			return null;
		}
	}

	static public <T> T		instanceByName(Class<T> cls, String name) {
		try {
			Class<?> c = Class.forName(name);
			if(! cls.isAssignableFrom(c))
				throw new IllegalStateException("The class '"+name+"' is not of type "+cls);
			return ((Class<T>)c).newInstance();
		} catch(Exception x) {
			x.printStackTrace();
			return null;
		}
	}

	static public <T> Method[]	findMethod(Class<T> cls, String method) {
		List<Method> res = new ArrayList<Method>();
		Method[] mar = cls.getMethods();
		for(Method m : mar) {
			if(m.getName().equals(method))
				res.add(m);
		}
		return res.toArray(new Method[res.size()]);
	}

}
