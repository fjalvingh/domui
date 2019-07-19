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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.input.IQueryManipulator;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.PropertyRelationType;
import to.etc.domui.component.misc.UIControlUtil;
import to.etc.domui.component2.lookupinput.LookupInput2;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.server.DomApplication;
import to.etc.domui.util.Constants;
import to.etc.domui.util.IRenderInto;

/**
 * Accepts any UP (parent) relation and scores 3, preferring this above the combobox-based
 * lookup.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 2, 2009
 */
public class ControlCreatorRelationLookup implements IControlCreator {
	/**
	 * Accept any UP relation.
	 */
	@Override
	public <T> int accepts(PropertyMetaModel<T> pmm, Class< ? extends IControl<T>> controlClass) {
		if(controlClass != null && !controlClass.isAssignableFrom(LookupInput2.class))
			return -1;

		Class<T> actualType = pmm.getActualType();
		ClassMetaModel cmm = MetaManager.findClassMeta(actualType);
		if(cmm.isPersistentClass() || pmm.getRelationType() == PropertyRelationType.UP) {
			if(Constants.COMPONENT_LOOKUP.equals(pmm.getComponentTypeHint()))
				return 10;
			return 3;                                                // Prefer a lookup above a combo if unspecified
		}
		return -1;
	}

	/**
	 * Create the lookup thingy.
	 */
	@Override
	public <T, C extends IControl<T>> C createControl(@NonNull PropertyMetaModel<T> pmm, @Nullable Class<C> controlClass) {
		//-- We'll do a lookup thingy for sure.
		LookupInput2<T> li = new LookupInput2<T>(pmm.getActualType(), pmm.getValueModel());

		//-- 1. Define search fields from property, then class.lookup, then generic
// jal 2014/07/08 What to do? Fixme!
//		List<SearchPropertyMetaModel> sp = pmm.getLookupFieldSearchProperties();		// Property override?
//		if(sp.size() == 0) {
//			sp = li.getMetaModel().getSearchProperties();		// Class level?
//			if(sp.size() > 0)
//				li.setSearchProperties(sp);
//		}

		//-- 2. Define keyword search properties in the same way.
		if(pmm.getLookupSelectedRenderer() != null)
			li.setValueRenderer(DomApplication.get().createInstance(pmm.getLookupSelectedRenderer())); // Bloody stupid Java generic crap
		else {
			ClassMetaModel cmm = MetaManager.findClassMeta(pmm.getActualType()); // Get meta for type reached,
			if(cmm.getLookupSelectedRenderer() != null)
				li.setValueRenderer((IRenderInto<T>) DomApplication.get().createInstance(cmm.getLookupSelectedRenderer())); // Bloody stupid Java generic crap
		}
		UIControlUtil.configure(li, pmm);

		//-- 20110721 jal If a query manipulator is present- use it.
		IQueryManipulator<T> qm = pmm.getQueryManipulator();
		if(null == qm)
			qm = (IQueryManipulator<T>) pmm.getClassModel().getQueryManipulator();
		if(null != qm) {
			li.setQueryManipulator(qm);
		}

		return (C) li;
	}
}

