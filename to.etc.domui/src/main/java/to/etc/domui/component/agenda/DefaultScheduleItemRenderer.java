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
package to.etc.domui.component.agenda;

import to.etc.domui.component.agenda.WeekAgendaComponent.IItemRenderer;
import to.etc.domui.dom.html.BR;
import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Span;

/**
 * WARNING: This class needs a separate <i>instance</i> for every thing it renders for! It has
 * locals!!
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 26, 2008
 */
public class DefaultScheduleItemRenderer<T extends ScheduleItem> implements IItemRenderer<T> {
	private StringBuilder m_sb = new StringBuilder();

	@Override public void render(WeekAgendaComponent<T> age, NodeContainer root, T si) throws Exception {
		if(si.getImageURL() != null) {
			Img i = new Img();
			i.setBorder(0);
			i.setCssClass("ui-wa-img");
			i.setAlt(si.getImageURL());
			i.setSrc(si.getImageURL());
			root.add(i);
		}
		if(si.getType() != null) {
			//-- add a span describing the type
			Span sp = new Span();
			root.add(sp);
			sp.setCssClass("ui-wa-ity");
			sp.setText(si.getType());
		}
		if(si.getName() != null) {
			Span sp = new Span();
			root.add(sp);
			sp.setCssClass("ui-wa-nm");
			sp.setText(si.getName());
		}
		if(true) {
			Span sp = new Span();
			root.add(sp);
			sp.setCssClass("ui-wa-tm");
			m_sb.setLength(0);
			m_sb.append(age.getDateFormat().format(si.getStart()));
			long duration = si.getEnd().getTime() - si.getStart().getTime();
			age.appendDuration(m_sb, duration);
			sp.setText(m_sb.toString());
		}
		if(si.getDetails() != null) {
			if(si.getName() != null) { // If we have a name too go to the next line
				root.add(new BR());
			}
			Span sp = new Span();
			root.add(sp);
			sp.setCssClass("ui-wa-dt");
			sp.setText(si.getDetails());
		}
	}
}
