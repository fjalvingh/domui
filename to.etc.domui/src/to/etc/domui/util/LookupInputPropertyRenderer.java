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
package to.etc.domui.util;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.domui.dom.html.*;
import to.etc.webapp.*;

/**
 * Configurable renderer for a LookupInput's "display selected value" field. This is a customizable
 * replacement for {@link SimpleLookupInputRenderer} which can define the fields to show explicitly,
 * instead of using metadata only.
 *
 * <p>This renderer represents default renderer that is used for {@link LookupInput} control.
 * It can be additionaly customized (before and after custom content) by setting provided {@link ICustomContentFactory} fields.
 * See {@link LookupInputPropertyRenderer#setBeforeContent} and {@link LookupInputPropertyRenderer#setAfterContent}.
 * Custom added content would be enveloped into separate row(s).</p>
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Feb 10, 2010
 */
public class LookupInputPropertyRenderer<T> implements INodeContentRenderer<T> {
	private INodeContentRenderer<T> m_beforeRenderer;

	private INodeContentRenderer<T> m_afterRenderer;

	/** The type that it should render. */
	@Nonnull
	final private Class<T> m_actualClass;

	@Nonnull
	final private String[] m_propertyNames;

	@Nullable
	private List<ExpandedDisplayProperty< ? >> m_xpl;

	public LookupInputPropertyRenderer(@Nonnull Class<T> clz, @Nonnull String... colset) {
		m_actualClass = clz;
		m_propertyNames = colset;
		initRenderingModel();
	}

	/**
	 * Create the rendering model from the available data.
	 */
	private void initRenderingModel() {
		ClassMetaModel cmm = MetaManager.findClassMeta(getActualClass());
		List<ExpandedDisplayProperty< ? >> xpl;
		if(m_propertyNames.length == 0) {
			//-- Has default meta?
			List<DisplayPropertyMetaModel> l = cmm.getTableDisplayProperties();
			if(l.size() == 0)
				l = cmm.getComboDisplayProperties();
			if(l.size() == 0)
				throw new ProgrammerErrorException("The class "+getActualClass()+" has no presentation metadata (@MetaObject or @MetaCombo)");

			//-- Expand the thingy: render a single line separated with BRs
			xpl = ExpandedDisplayProperty.expandDisplayProperties(l, cmm, null);
		} else {
			//-- Use the specified properties and create a list for presentation.
			xpl = new ArrayList<ExpandedDisplayProperty< ? >>();
			for(String propname : m_propertyNames) {
				xpl.add(ExpandedDisplayProperty.expandProperty(cmm, propname));
			}
		}
		m_xpl = ExpandedDisplayProperty.flatten(xpl);
	}

	@Override
	public void renderNodeContent(@Nonnull NodeBase component, @Nonnull NodeContainer node, @Nonnull T object, @Nullable Object parameters) throws Exception {
		String txt;
		TBody tbl = ((LookupInput< ? >) node).getBody();
		if(getBeforeRenderer() != null) {
			TD cell = new TD();
			getBeforeRenderer().renderNodeContent(component, cell, object, parameters);
			if(cell.getChildCount() != 0)
				tbl.addRow().add(cell);
		}

		if(object == null) {
			txt = Msgs.BUNDLE.getString(Msgs.UI_LOOKUP_EMPTY);
			TR r = new TR();
			tbl.add(r);
			TD td = new TD();
			r.add(td);
			td.setValign(TableVAlign.TOP);
			td.setCssClass("ui-lui-v");
			td.add(txt);

			//-- parameters is either the button, or null if this is a readonly version.
			if(parameters != null) {
				td = new TD();
				r.add(td);
				td.setValign(TableVAlign.TOP);
				td.setWidth("1%");
				td.add((NodeBase) parameters); // Add the button,
			}
		} else {
			tbl.setCssClass("ui-lui-v");
			int c = 0;
			int mw = 0;

			List<ExpandedDisplayProperty< ? >> xpl = m_xpl;
			if(null == xpl)
				throw new IllegalStateException("initRenderingModel was not yet called?");
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
				if(c++ == 0 && parameters != null) {
					td.add((NodeBase) parameters); // Add the button,
				}
			}
			mw += 4;
			if(mw > 40)
				mw = 40;
			tbl.setWidth(mw + "em");
		}

		if(getAfterRenderer() != null) {
			TD cell = new TD();
			getAfterRenderer().renderNodeContent(component, cell, object, parameters);
			if(cell.getChildCount() != 0)
				tbl.addRow().add(cell);
		}
	}

	/**
	 * Enables inserting of custom content that would be enveloped into additionaly added row that is inserted before rows that are part of builtin content.
	 */
	public INodeContentRenderer<T> getBeforeRenderer() {
		return m_beforeRenderer;
	}

	/**
	 * Enables inserting of custom content that would be enveloped into additionaly added row that is inserted before rows that are part of builtin content.
	 * @param afterContent
	 */
	public void setBeforeRenderer(INodeContentRenderer<T> beforeContent) {
		m_beforeRenderer = beforeContent;
	}

	/**
	 * Enables appending of custom content that would be enveloped into additionaly added row <i>after</i> the actual data.
	 */
	public INodeContentRenderer<T> getAfterRenderer() {
		return m_afterRenderer;
	}

	/**
	 * Enables appending of custom content that would be enveloped into additionaly added row <i>after</i> the actual data.
	 * @param afterContent
	 */
	public void setAfterRenderer(INodeContentRenderer<T> afterContent) {
		m_afterRenderer = afterContent;
	}

	@Nonnull
	public Class<T> getActualClass() {
		return m_actualClass;
	}

	@Nonnull
	public String[] getPropertyNames() {
		return m_propertyNames;
	}
}
