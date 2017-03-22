/*
 * DomUI Java User Interface - shared code
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
package javax.annotation.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation can be applied to the value() element of an annotation that
 * is annotated as a TypeQualifier. This is only appropriate if the value field
 * returns a value that is an Enumeration.
 *
 * Applications of the type qualifier with different values are exclusive, and
 * the enumeration is an exhaustive list of the possible values.
 *
 * For example, the following defines a type qualifier such that if you know a
 * value is neither {@literal @Foo(Color.Red)} or {@literal @Foo(Color.Blue)},
 * then the value must be {@literal @Foo(Color.Green)}. And if you know it is
 * {@literal @Foo(Color.Green)}, you know it cannot be
 * {@literal @Foo(Color.Red)} or {@literal @Foo(Color.Blue)}
 *
 * <code>
 * TypeQualifier  @interface Foo {
 *     enum Color {RED, BLUE, GREEN};
 *     Exhaustive Color value();
 *     }
 *  </code>
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Exhaustive {

}
