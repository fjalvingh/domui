package to.etc.server.ajax;

import java.io.*;
import java.util.*;

import to.etc.server.ajax.renderer.json.*;
import to.etc.server.ajax.renderer.xml.*;
import to.etc.xml.*;

public class TestRender {
	static public class Test1 {
		private List		m_list;

		private Object[]	m_array;

		Test1(List l, Object[] ar) {
			m_list = l;
			m_array = ar;
		}

		public Object[] getArray() {
			return m_array;
		}

		public List getList() {
			return m_list;
		}
	}

	static private Object makeTestObject1() {
		List l = new ArrayList();
		l.add(java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getLocation());
		l.add(java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getLocation());
		l.add(java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getLocation());
		//		l.add(System.getProperties());
		//		l.add(Locale.ITALY);
		return new Test1(l, l.toArray());
	}

	static public void main(String[] args) {
		try {
			XmlWriter w = new XmlWriter(new FileWriter(new File("/tmp/test.out")));
			JSONRenderer jr = new JSONRenderer(new JSONRegistry(), w, true);
			//			Object o = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getLocation();
			//			Object o = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
			Object o = makeTestObject1();
			jr.render(o);
			w.print("\n\n--------- xml --------------\n");
			XmlRenderer xr = new XmlRenderer(new XmlRegistry(), w);
			xr.render(o);
			w.flush();
			w.close();
		} catch(Exception x) {
			x.printStackTrace();
		}
	}
}
