package to.etc.server.injector;

import java.lang.annotation.*;

import org.w3c.dom.*;

import to.etc.xml.*;

public class XMLNodeInjectorConverter implements InjectorConverterFactory {
	public InjectorConverter accepts(Class totype, Class fromtype, Annotation[] anar) throws Exception {
		if(!Node.class.isAssignableFrom(fromtype)) // Source must be a node
			return null;
		if(totype == Long.class || totype == Long.TYPE) {
			return new InjectorConverter() {
				public Object convertValue(Object source) throws Exception {
					Node n = (Node) source;
					return Long.decode(DomTools.textFrom(n));
				}
			};
		}

		if(totype == Integer.class || totype == Integer.TYPE) {
			return new InjectorConverter() {
				public Object convertValue(Object source) throws Exception {
					Node n = (Node) source;
					return Integer.decode(DomTools.textFrom(n));
				}
			};
		}

		if(totype == Boolean.class || totype == Boolean.TYPE) {
			return new InjectorConverter() {
				public Object convertValue(Object source) throws Exception {
					Node n = (Node) source;
					return Boolean.valueOf(DomTools.decodeBoolStr(DomTools.textFrom(n), "undef"));
				}
			};
		}
		if(totype == String.class) {
			return new InjectorConverter() {
				public Object convertValue(Object source) throws Exception {
					Node n = (Node) source;
					return DomTools.textFrom_untrimmed(n);
				}
			};
		}
		return null;
	}
}
