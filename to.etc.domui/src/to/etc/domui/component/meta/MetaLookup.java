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

import to.etc.domui.component.input.*;
import to.etc.domui.util.*;

/**
 * Defines how an object is to be displayed if it is displayed in a {@link LookupInput} control, and
 * also defines a "preference" for being shown in a LookupInput control also - a property of this type,
 * when used in a form builder, will default to use a LookupInput control instead of a Combobox.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 20, 2010
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MetaLookup {
	/**
	 * Set to define your own renderer to render the <i>display size</i> of the field. In that case you
	 * must render the complete data to show inside the control's display presentation, including the
	 * button to start searching. When you use this the data in {@link #properties()} is ignored.
	 * @return
	 */

	Class< ? extends INodeContentRenderer< ? >> nodeRenderer() default UndefinedLabelStringRenderer.class;

	/**
	 * The list of properties that should be shown in the "display part" of the control (on the form). If
	 * not present this defaults to the {@link MetaObject} properties for the target datatype.
	 *
	 * @return
	 */
	MetaDisplayProperty[] displayProperties() default {};

	/**
	 * The list of properties to show in the "lookup form result table" when it is shown. If not present
	 * this defaults to the columns specified in the target table's {@link MetaObject} annotation.
	 * @return
	 */
	MetaDisplayProperty[] tableProperties() default {};

	/**
	 * The list of properties to use as "search properties" in the lookup form shown when
	 * an instance is searched for.
	 * @return
	 */
	MetaSearch searchProperties();
}
