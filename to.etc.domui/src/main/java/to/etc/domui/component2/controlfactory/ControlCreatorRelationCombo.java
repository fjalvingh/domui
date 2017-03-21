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
package to.etc.domui.component2.controlfactory;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.component2.combo.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.util.*;

/**
 * Accepts any property defined as an UP relation (parent) and score higher if a component type
 * hint is received.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 2, 2009
 */
@SuppressWarnings("unchecked")
public class ControlCreatorRelationCombo implements IControlCreator {
	/**
	 * Accept any UP relation; if the relation has a "comboLookup" type hint we score 10, else we score 2.
	 *
	 * @see to.etc.domui.component.controlfactory.PropertyControlFactory#accepts(to.etc.domui.component.meta.PropertyMetaModel, boolean)
	 */
	@Override
	public <T> int accepts(PropertyMetaModel<T> pmm, Class< ? extends IControl<T>> controlClass) {
		if(controlClass != null && !controlClass.isAssignableFrom(ComboLookup2.class))
			return -1;

		Class<T> actualType = pmm.getActualType();
		ClassMetaModel cmm = MetaManager.findClassMeta(actualType);
		if(cmm.isPersistentClass() || pmm.getRelationType() == PropertyRelationType.UP) {
			if(Constants.COMPONENT_COMBO.equals(pmm.getComponentTypeHint()))
				return 10;
			if(pmm.getComponentTypeHint() == null && Constants.COMPONENT_COMBO.equals(pmm.getClassModel().getComponentTypeHint()))
				return 10;
			return 2;
		}
		return -1;
	}

	@Override
	public <T, C extends IControl<T>> C createControl(@Nonnull PropertyMetaModel<T> pmm, @Nullable Class<C> controlClass) {
		try {
			ComboLookup2<T> co = ComboLookup2.createLookup(pmm);
			return (C) co;
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
	}
}
