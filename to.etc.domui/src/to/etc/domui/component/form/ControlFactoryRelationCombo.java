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
package to.etc.domui.component.form;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.util.*;

/**
 * Accepts any property defined as an UP relation (parent) and score higher if a component type
 * hint is received.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 2, 2009
 */
@SuppressWarnings("unchecked")
// Hating Generics
public class ControlFactoryRelationCombo implements ControlFactory {
	/**
	 * Accept any UP relation; if the relation has a "comboLookup" type hint we score 10, else we score 2.
	 *
	 * @see to.etc.domui.component.form.ControlFactory#accepts(to.etc.domui.component.meta.PropertyMetaModel, boolean)
	 */
	@Override
	public int accepts(final PropertyMetaModel pmm, final boolean editable, Class< ? > controlClass, Object context) {
		if(controlClass != null && !controlClass.isAssignableFrom(ComboLookup.class))
			return -1;

		if(pmm.getRelationType() != PropertyRelationType.UP)
			return 0;
		if(Constants.COMPONENT_COMBO.equals(pmm.getComponentTypeHint()))
			return 10;
		return 2;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public ControlFactoryResult createControl(final IReadOnlyModel< ? > model, final PropertyMetaModel pmm, final boolean editable, Class< ? > controlClass, Object context) {
		//-- FIXME EXPERIMENTAL use a DisplayValue control to present the value instead of a horrible disabled combobox
		if(!editable && controlClass == null) {
			DisplayValue<Object> dv = new DisplayValue<Object>(Object.class); // No idea what goes in here.
			dv.defineFrom(pmm);
			if(dv.getConverter() == null && dv.getRenderer() == null) {
				INodeContentRenderer<Object> r = (INodeContentRenderer<Object>) MetaManager.createDefaultComboRenderer(pmm, null); // FIXME Needed?
				dv.setRenderer(r);
			}
			return new ControlFactoryResult(dv, model, pmm);
		}

		//		if(!editable)
		//			throw new IllegalStateException("Implementation: please implement ReadOnly combobox thingy.");

		//-- We need to add a ComboBox. Do we have a combobox dataset provider?
		Class< ? extends IComboDataSet< ? >> set = pmm.getComboDataSet();
		if(set == null) {
			set = pmm.getClassModel().getComboDataSet();
			if(set == null)
				throw new IllegalStateException("Missing Combo dataset provider for property " + pmm);
		}

		INodeContentRenderer< ? > r = MetaManager.createDefaultComboRenderer(pmm, null);
		ComboLookup< ? > co = new ComboLookup(set, r);
		if(pmm.isRequired())
			co.setMandatory(true);
		String s = pmm.getDefaultHint();
		if(s != null)
			co.setTitle(s);
		co.setDisabled(!editable);
		return new ControlFactoryResult(co, model, pmm);
	}
}
