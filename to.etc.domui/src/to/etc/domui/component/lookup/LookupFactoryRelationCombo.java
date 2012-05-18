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
package to.etc.domui.component.lookup;

import javax.annotation.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.util.*;

final class LookupFactoryRelationCombo implements ILookupControlFactory {
	@Override
	public <X extends IInputNode< ? >> int accepts(final @Nonnull SearchPropertyMetaModel spm, final X control) {
		final PropertyMetaModel< ? > pmm = MetaUtils.getLastProperty(spm);

		if(pmm.getRelationType() != PropertyRelationType.UP)
			return -1;
		if(control == null && Constants.COMPONENT_COMBO.equals(pmm.getComponentTypeHint()))
			return 10;
		return 2;
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public <X extends IInputNode< ? >> ILookupControlInstance createControl(final @Nonnull SearchPropertyMetaModel spm, final X control) {
		IInputNode< ? > input = control;
		if(input == null) {
			final PropertyMetaModel pmm = MetaUtils.getLastProperty(spm);
			try {
				ComboLookup< ? > co = ComboLookup.createLookup(pmm);
				co.setMandatory(false);

				//				if(pmm.isRequired()) jal 20110802 Mandatoryness in property model has no relation with search criteria of course!
				//					co.setMandatory(true);
				String s = pmm.getDefaultHint();
				if(s != null)
					co.setTitle(s);
				String hint = MetaUtils.findHintText(spm);
				if(hint != null)
					co.setTitle(hint);
				input = co;
			} catch(Exception x) {
				throw WrappedException.wrap(x); // Checked exceptions are idiocy.
			}


//			//-- We need to add a ComboBox. Do we have a combobox dataset provider?
			//			Class< ? extends IComboDataSet< ? >> set = pmm.getComboDataSet();
			//			if(set == null) {
			//				set = pmm.getClassModel().getComboDataSet();
			//				if(set == null)
			//					throw new IllegalStateException("Missing Combo dataset provider for property " + pmm);
			//			}
			//
			//			INodeContentRenderer< ? > r = MetaManager.createDefaultComboRenderer(pmm, null);
			//			final ComboLookup< ? > co = new ComboLookup(set, r);
			//			if(pmm.isRequired())
			//				co.setMandatory(true);
			//			String s = pmm.getDefaultHint();
			//			if(s != null)
			//				co.setTitle(s);
			//			String hint = MetaUtils.findHintText(spm);
			//			if(hint != null)
			//				co.setTitle(hint);
			//			input = co;
		}
		return new EqLookupControlImpl(spm.getPropertyName(), input);
	}
}
