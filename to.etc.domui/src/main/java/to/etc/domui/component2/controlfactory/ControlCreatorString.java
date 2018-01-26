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

import to.etc.domui.component.input.Text2;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.util.DomUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This is a fallback factory; it accepts anything and shows a String edit component OR a
 * DisplayValue component for it. It hopes that the control can convert the string input
 * value to the actual type using the registered Converters. This is also the factory
 * for regular Strings.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 2, 2009
 */
@SuppressWarnings("unchecked")
public class ControlCreatorString implements IControlCreator {
	/**
	 * Accept any type using a string.
	 */
	@Override
	public <T> int accepts(PropertyMetaModel<T> pmm, Class< ? extends IControl<T>> controlClass) {
		if(controlClass != null) {
			if(!controlClass.isAssignableFrom(Text2.class))
				return -1;
		}

		return 2;
	}

	@Override
	public <T, C extends IControl<T>> C createControl(@Nonnull PropertyMetaModel<T> pmm, @Nullable Class<C> controlClass) {
		Class<T> type = pmm.getActualType();
		Text2<T> txt;
		if(Number.class.isAssignableFrom(DomUtil.getBoxedForPrimitive(type))) {
			txt = (Text2<T>) Text2.createNumericInput((PropertyMetaModel<Double>) pmm, true);
		} else {
			txt = Text2.createText(type, pmm, true);
		}
		return (C) txt;
	}
}
