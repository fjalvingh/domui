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
package javax.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.annotation.meta.TypeQualifier;
import javax.annotation.meta.When;

/**
 * This annotation a value that is of a particular syntax, such as Java syntax
 * or regular expression syntax. This can be used to provide syntax checking of
 * constant values at compile time, run time checking at runtime, and can assist
 * IDEs in deciding how to interpret String constants (e.g., should a
 * refactoring that renames method x() to y() update the String constant "x()").
 *
 *
 */
@Documented
@TypeQualifier(applicableTo = String.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface Syntax {
    /**
     * Value indicating the particular syntax denoted by this annotation.
     * Different tools will recognize different syntaxes, but some proposed
     * canonical values are:
     * <ul>
     * <li> "Java"
     * <li> "RegEx"
     * <li> "JavaScript"
     * <li> "Ruby"
     * <li> "Groovy"
     * <li> "SQL"
     * <li> "FormatString"
     * </ul>
     *
     * Syntax names can be followed by a colon and a list of key value pairs,
     * separated by commas. For example, "SQL:dialect=Oracle,version=2.3". Tools
     * should ignore any keys they don't recognize.
     */
    String value();

    When when() default When.ALWAYS;
}
