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
 * TEMP indicator on how to display some property; used in lookup and combo
 * definitions. This is part of a working test for metadata, do not use
 * because it can change heavily (or be deleted alltogether).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 13, 2008
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MetaDisplayProperty {
	/**
	 * The name of the property to show. This will be replaced with a Property references once
	 * the JDK 7 team gets off it's ass and starts to bloody define something useful.
	 * @return
	 */
	public String name();

	/**
	 * When set this overrides the default label as set by the property metadata. It must be set to a <b>key</b> in
	 * the <b>current class</b>'s resource file. This is usually set when displaying a property from a parent relation
	 * property (using a dotted path) to override the label as defined on the parent relation's property.
	 * @return
	 */
	public String defaultLabel() default Constants.NO_DEFAULT_LABEL;

	public SortableType defaultSortable() default SortableType.UNKNOWN;

	public int displayLength() default -1;

	public Class< ? extends IConverter< ? >> converterClass() default DummyConverter.class;

	public String join() default Constants.NO_JOIN;

	public YesNoType readOnly() default YesNoType.UNKNOWN;

	//	public DateType				dateType() default DateType.UNKNOWN;

	public String renderHint() default Constants.NONE;
}
