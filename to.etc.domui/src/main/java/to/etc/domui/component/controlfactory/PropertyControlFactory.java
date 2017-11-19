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
package to.etc.domui.component.controlfactory;

import javax.annotation.*;

import to.etc.domui.component.meta.*;

/**
 * A factory which creates the correct EDITING control to edit a property, specified by the property's
 * PropertyMetaModel. The DomApplication will contain a list of ControlFactories. When an edit control
 * is needed this list is obtained and each ControlFactory in it has it's accepts() method called. This
 * returns a "score" for each control factory. The first factory with the highest score (which must be
 * > 0) will be used to create the control. If no factory returns a &gt; 0 score a control cannot be
 * created which usually results in an exception.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 8, 2008
 */
public interface PropertyControlFactory {
	PropertyControlFactory TEXTAREA_CF = new ControlFactoryTextArea();

	PropertyControlFactory OLD_STRING_CF = new ControlFactoryStringOld();

	/**
	 * This is a fallback factory; it accepts anything and shows a String edit component for it. It
	 * hopes that the Text<?> control can convert the string input value to the actual type using the
	 * registered Converters. This is also the factory for regular Strings.
	 */
	PropertyControlFactory STRING_CF = new ControlFactoryString();

	PropertyControlFactory BOOLEAN_AND_ENUM_CF = new ControlFactoryEnumAndBool();

	PropertyControlFactory DATE_CF = new ControlFactoryDate();

	/**
	 * Factory for UP relations. This creates a combobox input if the property is an
	 * UP relation and has combobox properties set.
	 */
	PropertyControlFactory RELATION_COMBOBOX_CF = new ControlFactoryRelationCombo();

	PropertyControlFactory RELATION_LOOKUP_CF = new ControlFactoryRelationLookup();

	/**
	 * This must return a +ve value when this factory accepts the specified property; the returned value
	 * is an eagerness score. The factory returning the highest eagerness wins.
	 * @param pmm
	 * @param editable
	 * @param controlClass When set the control factory *must* be able to return a component which is assignment-compatible with this class type. If it cannot it MUST refuse to create the control.
	 * @return
	 */
	int accepts(@Nonnull PropertyMetaModel< ? > pmm, boolean editable, @Nullable Class< ? > controlClass);

	/**
	 * This MUST create all nodes necessary for a control to edit the specified item. The nodes must be added
	 * to the container; this <i>must</i> return a ModelBinding to bind and unbind a value to the control
	 * created.
	 * @param pmm
	 * @param editable
	 * @param controlClass	When set the control factory *must* return a component which is assignment-compatible with this
	 * 						class type. When this method is called it has already (by it's accept method) told us it can, so
	 * 						not creating the proper type is not an option.
	 * @param container
	 * @return
	 */
	@Nonnull
	<T> ControlFactoryResult createControl(@Nonnull PropertyMetaModel<T> pmm, boolean editable, @Nullable Class< ? > controlClass);
}
