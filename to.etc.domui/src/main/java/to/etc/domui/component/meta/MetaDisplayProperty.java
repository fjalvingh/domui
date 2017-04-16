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
 * Used to define a property to show in a table, used to define the names
 * and to override display properties for a single table.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 13, 2008
 */
@Documented // Retarded
@Retention(RetentionPolicy.RUNTIME)
public @interface MetaDisplayProperty {
	/**
	 * The name of the property to show. You can also specify a dotted path to some
	 * parent entity's property here. This will be replaced with a Property reference once
	 * the JDK 7 team gets off it's ass and starts to bloody define something useful 8-(
	 * @return
	 */
	String name();

	/**
	 * When set this overrides the default label as set by the property metadata. It must be set to a <b>key</b> in
	 * the <b>current class</b>'s resource file. This is usually set when displaying a property from a parent relation
	 * property (using a dotted path) to override the label as defined on the parent relation's property.
	 * @return
	 */
	String defaultLabel() default Constants.NO_DEFAULT_LABEL;

	/**
	 * When set, this defines this field as being a field that a table can show a "sort button" on. The first time
	 * the sort button is pressed it sorts either ascending or descending, depending on this property's value. Setting
	 * this only defines the property as "sortable"; it does not define the "initial sort" for a table. You need to do
	 * that using {@link MetaObject#defaultSortColumn()}. The "default" in this name refers to the default order (ascending or
	 * descending).
	 *
	 * @return
	 */
	SortableType defaultSortable() default SortableType.UNKNOWN;

	/**
	 * An indication of the display length to use for this field, in characters. When present it will influence
	 * the percentage widths used in the table. The default value is -1. In this case the real "length" of the
	 * property, as defined in {@link MetaProperty#length()} or any JPA Annotation like {@link Column#length()}.
	 * @return
	 */
	int displayLength() default -1;

	YesNoType noWrap() default YesNoType.UNKNOWN;

	/**
	 * Define a Converter class to use to convert the value from the property to a string. When unset the code
	 * defaults to the conversion specified on the property itself, either by an explicit {@link MetaProperty#converterClass()}
	 * setting or by the default conversions registered with the conversion factory.
	 * @return
	 */
	Class< ? extends IConverter< ? >> converterClass() default DummyConverter.class;

	/**
	 * <p>When present, this will force a join of this property <i>and the next one</i> specified in the display property
	 * list, and the string specified here will be used as a "separator" between the two values. The join means that
	 * the two (or more) properties are joined together in a <i>single</i> table column, as a single string. A typical
	 * use case for instance is to create a single visible table column for something like Address, where the address
	 * is displayed as:</p>
	 * <pre>
	 * @MetaObject(properties={
	 *   @MetaDisplayProperty(name="streetName", join=" ")
	 *  ,@MetaDisplayProperty(name="houseNumber", join=", ")
	 *  ,@MetaDisplayProperty(name="cityName")
	 * })
	 * </pre>
	 *
	 * <p>In this example the address field's columns would be shown in a single visible table column, like:
	 * <pre>
	 * Cinemadreef 96, Almere
	 * </pre>
	 * @return
	 */
	String join() default Constants.NO_JOIN;
}
