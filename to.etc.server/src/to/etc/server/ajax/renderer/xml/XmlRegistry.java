package to.etc.server.ajax.renderer.xml;

import java.math.*;
import java.util.*;

import to.etc.server.ajax.renderer.*;
import to.etc.xml.*;

/**
 * Renders an AJAX object tree as an XML document that can eb easily used
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 7, 2006
 */
public class XmlRegistry extends RenderRegistry {
	/**
	 * Maps Java classes to a default type specification.
	 */
	private Map<Class, String>	m_xmlTypeMap	= new HashMap<Class, String>();

	public XmlRegistry() {
		register(null, new XmlItemRenderer() {
			@Override
			public void render(XmlRenderer r, Object val) throws Exception {
				r.xw().tagonly("null");
			}
		});

		ItemRenderer r = new TostringRenderer();
		register(Integer.class, r);
		register(Integer.TYPE, r);
		register(String.class, r);
		register(Long.class, r);
		register(Long.TYPE, r);
		register(Double.class, r);
		register(Double.TYPE, r);
		register(Float.class, r);
		register(Float.TYPE, r);
		register(Short.class, r);
		register(Short.TYPE, r);
		register(Byte.class, r);
		register(Byte.TYPE, r);
		register(Boolean.class, r);
		register(Boolean.TYPE, r);
		registerBase(Date.class, new XmlItemRenderer() {
			@Override
			public void render(XmlRenderer rr, Object val) throws Exception {
				rr.xw().cdata(DomTools.dateEncode((Date) val));
			}
		});

		registerType(Boolean.class, "xsi:boolean");
		registerType(Boolean.TYPE, "xsi:boolean");
		registerType(Integer.class, "xsi:int");
		registerType(Integer.TYPE, "xsi:int");
		registerType(Long.class, "xsi:int");
		registerType(Long.TYPE, "xsi:int");
		registerType(Short.class, "xsi:int");
		registerType(Short.TYPE, "xsi:int");
		registerType(Double.class, "xsi:double");
		registerType(Double.TYPE, "xsi:double");
		registerType(Float.class, "xsi:float");
		registerType(Float.TYPE, "xsi:float");
		registerType(BigDecimal.class, "xsi:decimal");
		registerType(BigInteger.class, "xsi:decimal");
		registerType(BigDecimal.class, "xsi:decimal");
		registerType(String.class, "xsi:string");
	}

	public synchronized void registerType(Class cl, String name) {
		m_xmlTypeMap.put(cl, name);
	}

	/**
	 * Walks the entire object's class structure and returns T if a type
	 * can be found.
	 * @param cl
	 * @return
	 */
	public synchronized String findType(Class cl) {
		String type;
		Class[] ifes = cl.getInterfaces();
		if(ifes.length > 0) {
			for(Class ifa : ifes) {
				type = m_xmlTypeMap.get(ifa);
				if(type != null)
					return type;
			}
		}
		cl = cl.getSuperclass();
		while(cl != null) {
			type = m_xmlTypeMap.get(cl);
			if(type != null)
				return type;
			cl = cl.getSuperclass();
		}
		return null;
	}

	public synchronized String getType(Class cl) {
		String type = m_xmlTypeMap.get(cl);
		if(type != null)
			return type;
		type = findType(cl); // Walk structure to get a type
		if(type == null) {
			if(cl.isArray())
				type = "xsi:list";
			else if(Collection.class.isAssignableFrom(cl))
				type = "xsi:list";
			else if(Map.class.isAssignableFrom(cl))
				type = "xsi:map";
		}
		if(type == null) {
			type = cl.getCanonicalName();
			int pos = type.lastIndexOf('.');
			if(pos != -1)
				type = type.substring(pos + 1);
			type = "java:" + type;
			m_xmlTypeMap.put(cl, type);
		}
		return type;
	}
}
