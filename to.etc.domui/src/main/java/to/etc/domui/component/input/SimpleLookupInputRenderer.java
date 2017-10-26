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

import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.webapp.*;

import javax.annotation.*;
import java.util.*;

/**
 * This renderer represents default renderer that is used for {@link LookupInput}. It can also be
 * configured to show specific properties.
 *
 * It can be customized (before and after custom content),
 * see {@link SimpleLookupInputRenderer#setBeforeRenderer(IRenderInto)} and {@link SimpleLookupInputRenderer#setAfterRenderer(IRenderInto)}.
 * Custom added content would be enveloped into separate row(s).
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Feb 10, 2010
 */
/* final */ public class SimpleLookupInputRenderer<T> implements IRenderInto<T> {
	/** The type that it should render. */
	@Nullable
	final private Class<T> m_actualClass;

	@Nullable
	final private String[] m_propertyNames;

	@Nullable
	final private List<ExpandedDisplayProperty< ? >> m_xpl;

	private IRenderInto<T> m_beforeRenderer;

	private IRenderInto<T> m_afterRenderer;

	public SimpleLookupInputRenderer() {
		m_actualClass = null;
		m_propertyNames = null;
		m_xpl = null;
	}

	public SimpleLookupInputRenderer(ClassMetaModel cmm, String... propertyNames) {
		m_actualClass = (Class<T>) cmm.getActualClass();
		m_propertyNames = propertyNames;
		m_xpl = initRenderingModel(cmm, propertyNames);
	}

	public SimpleLookupInputRenderer(@Nonnull Class<T> clz, @Nonnull String... colset) {
		m_actualClass = clz;
		m_propertyNames = colset;
		ClassMetaModel cmm = MetaManager.findClassMeta(clz);
		m_xpl = initRenderingModel(cmm, colset);
	}

	private List<ExpandedDisplayProperty<?>> initRenderingModel(ClassMetaModel cmm, @Nonnull String[] colset) {
		List<ExpandedDisplayProperty< ? >> xpl;
		if(colset.length == 0) {
			//-- Do we have a "selected properties" meta renderer?
			List<DisplayPropertyMetaModel> l = cmm.getLookupSelectedProperties();
			if(l.size() == 0)
				l = cmm.getTableDisplayProperties();
			if(l.size() == 0)
				l = cmm.getComboDisplayProperties();
			if(l.size() == 0)
				throw new ProgrammerErrorException("The class " + cmm + " has no presentation metadata (@MetaObject or @MetaCombo)");

			//-- Expand the thingy: render a single line separated with BRs
			xpl = ExpandedDisplayProperty.expandDisplayProperties(l, cmm, null);
		} else {
			//-- Use the specified properties and create a list for presentation.
			xpl = new ArrayList<>();
			for(String propname : colset) {
				xpl.add(ExpandedDisplayProperty.expandProperty(cmm, propname));
			}
		}
		return ExpandedDisplayProperty.flatten(xpl);
	}

	@Override
	public void render(@Nonnull NodeContainer node, @Nonnull T object) throws Exception {
		TBody tb = node.addTableForLayout("ui-lui-vtab");
		IRenderInto<T> beforeRenderer = getBeforeRenderer();
		if(beforeRenderer != null) {
			TD cell = tb.addRowAndCell("ui-lui-vcell");
			beforeRenderer.render(cell, object);
			if(cell.getChildCount() != 0)
				tb.addRow().add(cell);
		}

		List<ExpandedDisplayProperty<?>> xpl = m_xpl;
		if(xpl != null) {
			renderModelValue(object, tb, xpl);
		} else {
			ClassMetaModel cmm = MetaManager.findClassMeta(object.getClass());
			List<DisplayPropertyMetaModel> l = cmm.getTableDisplayProperties();
			if(l.size() == 0)
				l = cmm.getComboDisplayProperties();
			if(l.size() > 0) {
				xpl = ExpandedDisplayProperty.expandDisplayProperties(l, cmm, null);
				xpl = ExpandedDisplayProperty.flatten(xpl);
				renderModelValue(object, tb, xpl);
			} else {
				//-- Render as a text
				String txt = object.toString();
				TD td = tb.addRowAndCell("ui-lui-val-txt");
				td.add(new Span("ui-lui-val", txt));
			}
		}

		IRenderInto<T> afterRenderer = getAfterRenderer();
		if(afterRenderer != null) {
			TD cell = tb.addRowAndCell("ui-lui-vcell");
			afterRenderer.render(cell, object);
		}
	}

	private void renderModelValue(@Nonnull T object, TBody tb, List<ExpandedDisplayProperty< ? >> xpl) throws Exception {
		int c = 0;
		int mw = 0;
		for(ExpandedDisplayProperty< ? > xp : xpl) {
			String val = xp.getPresentationString(object);
			if(val != null && val.length() != 0) {
				TD td = tb.addRowAndCell("ui-lui-vcell");
				td.add(val);
				int len = val.length();
				if(len > mw) {
					mw = len;
				}
			}
		}
		mw += 4;
		if(mw > 40)
			mw = 40;
		tb.setWidth(mw + "em");
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
