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
package to.etc.domui.component.meta;

import java.lang.annotation.*;

import to.etc.domui.converter.*;
import to.etc.domui.util.*;

/**
 * Defines a display property for a combo control, to use in the {@link MetaCombo} annotation. It is functionally
 * equivalent to {@link MetaDisplayProperty} but has less parameters.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 20, 2010
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MetaComboProperty {
	/**
	 * The name of the property to show. You can also specify a dotted path to some
	 * parent entity's property here. This will be replaced with a Property reference once
	 * the JDK 7 team gets off it's ass and starts to bloody define something useful 8-(
	 * @return
	 */
	String name();

	/**
	 * The converter to use to convert this property's value to a string. Defaults to the actual property's
	 * converter when unset.
	 * @return
	 */
	Class< ? extends IConverter< ? >> converterClass() default DummyConverter.class;

	/**
	 * When set, this separator will be added between this property and the <i>next one</i> in the combo's
	 * property list, provided there is a next property value. It defaults to a single space.
	 * @return
	 */
	String join() default Constants.NO_JOIN;

	/**
	 * If this combo field should be used in an initial sort of the combobox's data this defines
	 * the sort direction. The field {@link #sortIndex()} defines the position inside the sort.
	 * @return
	 */
	SortableType sortable() default SortableType.UNKNOWN;

	/**
	 * Defines the "place" of this field in a sort statement, if applicable. This has meaning only
	 * if {@link #sortable()} is set to some sort; in that case this indes defines the location of
	 * the field inside the sort.
	 *
	 * @return
	 */
	int sortIndex() default -1;
}
