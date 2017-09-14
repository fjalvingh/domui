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
import to.etc.domui.component.tbl.*;
import to.etc.domui.util.*;

/**
 * <p>This describes how an Object is to be shown when it is displayed in a (Data)Table (see {@link DataTable}
 * and {@link BasicRowRenderer}). The most important thing it does is to specify which properties of the
 * object should be shown in columns of the table; every property mentioned in the {@link #defaultColumns()} list
 * becomes a column in the Table shown (provided the columns are not joined). In addition, this can also specify
 * a default "sort" property; when set the table will be shown initially sorted on that property provided
 * the data model supports sorting.</p>
 *
 * <p>This annotation can be used on a class itself; then it defines the default wherever that class is used
 * in a table. You can also add it to some property in which case it "overrides" the definition done at "class"
 * level for tables that are shown using that single property. This has not yet been used so success is questionable...</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 28, 2008
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MetaObject {
	/**
	 * The list of properties to show in the table, and their conversion, display size etc characteristics. For a normal
	 * MetaObject instance this list should have at least 1 element.
	 * @return
	 */
	MetaDisplayProperty[] defaultColumns();

	/**
	 * Set to define your own renderer to render the <i>display size</i> of the field. In that case you
	 * must render the complete data to show inside the control's display presentation, including the
	 * button to start searching. When you use this the data in {@link #properties()} is ignored.
	 * @return
	 */
	Class< ? extends IRenderInto< ? >> selectedRenderer() default UndefinedLabelStringRenderer.class;

	/**
	 * The list of properties that should be shown in the "display part" of a control like {@link LookupInput}
	 * when a single instance is selected. If not present this defaults to the {@link MetaObject} properties
	 * for the target datatype.
	 *
	 * @return
	 */
	MetaDisplayProperty[] selectedProperties() default {};

	/**
	 * The list of properties to use as "search properties" in the lookup form and/or fast search shown when
	 * an instance is searched for.
	 * @return
	 */
	MetaSearchItem[] searchProperties() default {};

	/**
	 * Define a property to sort on by default. Defaults to NONE, meaning the data is not sorted by the table. To set
	 * the default sort direction use {@link #defaultSortOrder()}.
	 * @return
	 */
	String defaultSortColumn() default Constants.NONE;

	/**
	 * If a {@link #defaultSortColumn()} is defined, this defines the initial sort direction (ascending, descending). It defaults to ascending.
	 * @return
	 */
	SortableType defaultSortOrder() default SortableType.SORTABLE_ASC;
}
