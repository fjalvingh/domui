package to.etc.server.ajax.renderer.json;

import java.lang.reflect.*;
import java.util.*;

import to.etc.server.ajax.renderer.*;
import to.etc.util.*;

/**
 * Renders an AJAX object tree as an XML document that can eb easily used
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 7, 2006
 */
public class JSONRegistry extends RenderRegistry {
	public JSONRegistry() {
		register(null, new JSONItemRenderer() {
			@Override
			public void render(JSONRenderer r, Object val) throws Exception {
				r.getWriter().print("null");
			}
		});
		addArrayRenderer(Integer.TYPE, new JSONArrayRenderer(30) {
			@Override
			public void render(JSONRenderer r, Object val, int ix) throws Exception {
				int v = Array.getInt(val, ix);
				r.printInt(v);
			}
		});
		addArrayRenderer(Byte.TYPE, new JSONItemRenderer() {
			@Override
			public void render(JSONRenderer r, Object val) throws Exception {
				r.print((byte[]) val);
			}
		});

		ItemRenderer r = new JSONItemRenderer() {
			@Override
			public void render(JSONRenderer rr, Object val) throws Exception {
				Number n = (Number) val;
				rr.getWriter().print(n.toString());
			}
		};
		register(Integer.class, r);
		register(Integer.TYPE, r);
		register(Long.class, r);
		register(Long.TYPE, r);
		register(Double.class, r);
		register(Double.TYPE, r);


		r = new JSONItemRenderer() {
			@Override
			public void render(JSONRenderer rr, Object val) throws Exception {
				Boolean v = (Boolean) val;
				rr.getWriter().print(v.toString());
			}
		};
		register(Boolean.TYPE, r);
		register(Boolean.class, r);

		register(String.class, new JSONItemRenderer() {
			@Override
			public void render(JSONRenderer rd, Object val) throws Exception {
				StringTool.strToJavascriptString(rd.getWriter(), (String) val, false);
			}
		});

		registerBase(Date.class, new JSONItemRenderer() {
			@Override
			public void render(JSONRenderer rr, Object val) throws Exception {
				rr.renderDate((Date) val);
			}
		});
		registerBase(Calendar.class, new JSONItemRenderer() {
			@Override
			public void render(JSONRenderer rr, Object val) throws Exception {
				rr.renderDate(((Calendar) val).getTime());
			}
		});

		registerBase(Enum.class, new JSONItemRenderer() {
			@Override
			public void render(JSONRenderer rd, Object val) throws Exception {
				Enum e = (Enum) val;
				StringTool.strToJavascriptString(rd.getWriter(), e.toString(), false);
			}
		});
	}
}
