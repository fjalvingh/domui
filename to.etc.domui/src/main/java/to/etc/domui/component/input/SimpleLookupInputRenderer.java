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
package to.etc.domui.component.input;

import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.impl.DisplayPropertyMetaModel;
import to.etc.domui.component.meta.impl.ExpandedDisplayProperty;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Span;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;
import to.etc.domui.dom.html.TR;
import to.etc.domui.dom.html.TableVAlign;
import to.etc.domui.util.IRenderInto;
import to.etc.domui.util.Msgs;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * This renderer represents default renderer that is used for {@link LookupInput} control.
 *
 * It can be additionaly customized (before and after custom content),
 * see {@link SimpleLookupInputRenderer#setBeforeRenderer(IRenderInto)} and {@link SimpleLookupInputRenderer#setAfterRenderer(IRenderInto)}.
 * Custom added content would be enveloped into separate row(s).
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Feb 10, 2010
 */
public class SimpleLookupInputRenderer<T> implements IRenderInto<T> {
	public SimpleLookupInputRenderer() {}

	private IRenderInto<T> m_beforeRenderer;

	private IRenderInto<T> m_afterRenderer;

	@Override
	public void render(@Nonnull NodeContainer node, @Nonnull T object) throws Exception {
		String txt;
		TBody tbl = ((LookupInput< ? >) node).getBody();
		if(getBeforeRenderer() != null) {
			TD cell = new TD();
			getBeforeRenderer().render(cell, object);
			if(cell.getChildCount() != 0)
				tbl.addRow().add(cell);
		}

		if(object != null) {
			ClassMetaModel cmm = MetaManager.findClassMeta(object.getClass());
			if(cmm != null) {
				//-- Has default meta?
				List<DisplayPropertyMetaModel> l = cmm.getTableDisplayProperties();
				if(l.size() == 0)
					l = cmm.getComboDisplayProperties();
				if(l.size() > 0) {
					//-- Expand the thingy: render a single line separated with BRs
					List<ExpandedDisplayProperty< ? >> xpl = ExpandedDisplayProperty.expandDisplayProperties(l, cmm, null);
					xpl = ExpandedDisplayProperty.flatten(xpl);
						//						node.add(tbl);
					tbl.setCssClass("ui-lui-v");
					int c = 0;
					int mw = 0;
					for(ExpandedDisplayProperty< ? > xp : xpl) {
						String val = xp.getPresentationString(object);
						if(val == null || val.length() == 0)
							continue;
						TR tr = new TR();
						tbl.add(tr);
						TD td = new TD(); // Value thingy.
						tr.add(td);
						td.setCssClass("ui-lui-vcell");
						td.setValign(TableVAlign.TOP);
						td.add(val);
						int len = val.length();
						if(len > mw)
							mw = len;
							td = new TD();
						tr.add(td);
						td.setValign(TableVAlign.TOP);
						td.setCssClass("ui-lui-btncell");
						td.setWidth("1%");
						//if(c++ == 0 && parameters != null) {
						//	td.add((NodeBase) parameters); // Add the button,
						//}
					}
					mw += 4;
					if(mw > 40)
						mw = 40;
					tbl.setWidth(mw + "em");
					return;
				}
			}
			txt = object.toString();
		} else
			txt = Msgs.BUNDLE.getString(Msgs.UI_LOOKUP_EMPTY);
		TR r = new TR();
		tbl.add(r);
		TD td = new TD();
		r.add(td);
		td.setValign(TableVAlign.TOP);					// FIXUI Should not be here but in CSS
		td.setCssClass("ui-lui-v");
		td.add(new Span("ui-lui-val-txt", txt));

		////-- parameters is either the button, or null if this is a readonly version.
		//if(parameters != null) {
		//	td = new TD();
		//	r.add(td);
		//	td.setValign(TableVAlign.TOP);
		//	td.setWidth("1%");
		//	td.add((NodeBase) parameters); // Add the button,
		//}
		//
		//if(getAfterRenderer() != null) {
		//	TD cell = new TD();
		//	getAfterRenderer().renderNodeContent(component, cell, object, parameters);
		//	if(cell.getChildCount() != 0)
		//		tbl.addRow().add(cell);
		//}

	}

	/**
	 * Enables inserting of custom content that would be enveloped into additionaly added row that is inserted before rows that are part of builtin content.
	 */
	public IRenderInto<T> getBeforeRenderer() {
		return m_beforeRenderer;
	}

	/**
	 * Enables inserting of custom content that would be enveloped into additionaly added row that is inserted before rows that are part of builtin content.
	 */
	public void setBeforeRenderer(IRenderInto<T> beforeContent) {
		m_beforeRenderer = beforeContent;
	}

	/**
	 * Enables appending of custom content that would be enveloped into additionaly added row <i>after</i> the actual data.
	 */
	public IRenderInto<T> getAfterRenderer() {
		return m_afterRenderer;
	}

	/**
	 * Enables appending of custom content that would be enveloped into additionaly added row <i>after</i> the actual data.
	 */
	public void setAfterRenderer(IRenderInto<T> afterContent) {
		m_afterRenderer = afterContent;
	}
}
