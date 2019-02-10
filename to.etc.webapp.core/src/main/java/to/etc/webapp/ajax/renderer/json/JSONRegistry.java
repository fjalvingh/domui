/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.webapp.ajax.renderer.json;

import to.etc.util.StringTool;
import to.etc.webapp.ajax.renderer.ItemRenderer;
import to.etc.webapp.ajax.renderer.RenderRegistry;

import java.lang.reflect.Array;
import java.util.Calendar;
import java.util.Date;

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
			public void render(final JSONRenderer r, final Object val) throws Exception {
				r.getWriter().print("null");
			}
		});
		addArrayRenderer(Integer.TYPE, new JSONArrayRenderer(30) {
			@Override
			public void render(final JSONRenderer r, final Object val, final int ix) throws Exception {
				int v = Array.getInt(val, ix);
				r.printInt(v);
			}
		});
		addArrayRenderer(Byte.TYPE, new JSONItemRenderer() {
			@Override
			public void render(final JSONRenderer r, final Object val) throws Exception {
				r.print((byte[]) val);
			}
		});

		ItemRenderer r = new JSONItemRenderer() {
			@Override
			public void render(final JSONRenderer rr, final Object val) throws Exception {
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
			public void render(final JSONRenderer rr, final Object val) throws Exception {
				Boolean v = (Boolean) val;
				rr.getWriter().print(v.toString());
			}
		};
		register(Boolean.TYPE, r);
		register(Boolean.class, r);

		register(String.class, new JSONItemRenderer() {
			@Override
			public void render(final JSONRenderer rd, final Object val) throws Exception {
				StringTool.strToJavascriptString(rd.getWriter(), (String) val, true);
			}
		});

		registerBase(Date.class, new JSONItemRenderer() {
			@Override
			public void render(final JSONRenderer rr, final Object val) throws Exception {
				rr.renderDate((Date) val);
			}
		});
		registerBase(Calendar.class, new JSONItemRenderer() {
			@Override
			public void render(final JSONRenderer rr, final Object val) throws Exception {
				rr.renderDate(((Calendar) val).getTime());
			}
		});

		registerBase(Enum.class, new JSONItemRenderer() {
			@Override
			public void render(final JSONRenderer rd, final Object val) throws Exception {
				Enum< ? > e = (Enum< ? >) val;
				StringTool.strToJavascriptString(rd.getWriter(), e.toString(), false);
			}
		});
	}
}
