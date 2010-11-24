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

import to.etc.domui.component.meta.impl.*;
import to.etc.domui.util.*;

/**
 * TEMP Specifies that a parent relation is set using a default
 * combobox. This is part of a working test for metadata, do not use
 * because it can change heavily (or be deleted alltogether).
 *
 * This annotation can also be added to a PARENT class, in which case it will
 * define the defaults would that class be used in an "Up" relation combobox.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 13, 2008
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MetaCombo {
	/**
	 * The thingy which is resposible for returning the set-of-objects to select a value
	 * from. The default will use the data type and create a generic full-dataset query.
	 *
	 * @return
	 */
	Class< ? extends IComboDataSet< ? >> dataSet() default UndefinedComboDataSet.class;

	Class< ? extends ILabelStringRenderer< ? >> labelRenderer() default UndefinedLabelStringRenderer.class;

	Class< ? extends INodeContentRenderer< ? >> nodeRenderer() default UndefinedLabelStringRenderer.class;

	/**
	 * The list of properties that should be shown. This is needed ONLY when the class metadata of the
	 * parent record does not specify a default display column or columnset.
	 * @return
	 */
	public MetaDisplayProperty[] properties() default {};

	/**
	 * Allow no value to be selected here. Used when there's a need to override the default.
	 * @return
	 */
	public ComboOptionalType optional() default ComboOptionalType.INHERITED;
}
