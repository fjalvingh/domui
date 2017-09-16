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
package to.etc.domui.component2.lookupinput;

import to.etc.domui.component.input.LookupInput;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.impl.DisplayPropertyMetaModel;
import to.etc.domui.component.meta.impl.ExpandedDisplayProperty;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Span;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.util.IRenderInto;
import to.etc.domui.util.Msgs;
import to.etc.webapp.ProgrammerErrorException;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * This renderer represents default renderer that is used for {@link LookupInput} control.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10/2014
 */
@DefaultNonNull
final public class SimpleLookupInputRenderer2<T> implements IRenderInto<T> {
	final private Class<T> m_actualClass;

	final private ClassMetaModel m_classModel;

	final private String[] m_propertyNames;

	final private List<ExpandedDisplayProperty< ? >> m_xpl;

	public SimpleLookupInputRenderer2(Class<T> displayClass, String... propertyNames) {
		m_classModel = MetaManager.findClassMeta(displayClass);
		m_actualClass = displayClass;
		m_propertyNames = propertyNames;
		m_xpl = initRenderingModel();
	}

	public SimpleLookupInputRenderer2(ClassMetaModel cmm, String... propertyNames) {
		m_actualClass = (Class<T>) cmm.getActualClass();
		m_classModel = cmm;
		m_propertyNames = propertyNames;
		m_xpl = initRenderingModel();
	}

	private Class<T> getActualClass() {
		return m_actualClass;
	}

	/**
	 * Create the rendering model from the available data.
	 * @return
	 */
	@Nonnull
	private List<ExpandedDisplayProperty< ? >> initRenderingModel() {
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
		return ExpandedDisplayProperty.flatten(xpl);
	}

	@Override
	public void renderOpt(@Nonnull NodeContainer node, @Nullable T object) throws Exception {
		if(null == object) {
			String txt = Msgs.BUNDLE.getString(Msgs.UI_LOOKUP_EMPTY);
			node.setText(txt);
			return;
		}
		render(node, object);
	}

	@Override
	public void render(@Nonnull NodeContainer node, @Nonnull T object) throws Exception {
		List<ExpandedDisplayProperty<?>> xl = m_xpl;
		if(xl == null || xl.size() == 0) {
			node.add(String.valueOf(object));
			return;
		}

		int c = 0;
		int mw = 0;
		TBody tb = node.addTableForLayout();

		for(ExpandedDisplayProperty< ? > xp : xl) {
			String val = xp.getPresentationString(object);
			if(val == null || val.length() == 0)
				continue;

			Span vals = new Span();
			vals.setCssClass("ui-lui2-vals");
			tb.addRowAndCell().add(vals);
			vals.setText(val);
		}
	}
}
