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

import to.etc.domui.component.input.ComboLookup;
import to.etc.domui.component.meta.MetaUtils;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.PropertyRelationType;
import to.etc.domui.component.meta.SearchPropertyMetaModel;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.util.Constants;
import to.etc.util.WrappedException;

import javax.annotation.Nonnull;

@Deprecated
final class LookupFactoryRelationCombo implements ILookupControlFactory {
	@Override
	public <T, X extends IControl<T>> int accepts(final @Nonnull SearchPropertyMetaModel spm, final X control) {
		final PropertyMetaModel< ? > pmm = spm.getProperty();

		if(pmm.getRelationType() != PropertyRelationType.UP)
			return -1;
		if(control == null && Constants.COMPONENT_COMBO.equals(pmm.getComponentTypeHint()))
			return 10;
		return 2;
	}

	@Override
	@SuppressWarnings({"unchecked"})
	public <T, X extends IControl<T>> ILookupControlInstance<?> createControl(final @Nonnull SearchPropertyMetaModel spm, final X control) {
		IControl< ? > input = control;
		if(input == null) {
			final PropertyMetaModel< ? > pmm = spm.getProperty();
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
		return new EqLookupControlImpl<>(spm.getProperty().getName(), input);
	}
}
