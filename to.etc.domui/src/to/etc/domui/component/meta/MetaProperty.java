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

/**
 * Annotation to add metadata to a property.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 18, 2008
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MetaProperty {
	/**
	 * Defines whether the property should be <i>sortable</i> when used in a default table, and defines
	 * the initial sort direction of the property. This defaults to unsortable.
	 * @return
	 */
	public SortableType defaultSortable() default SortableType.UNKNOWN;

	/**
	 * The size, in characters, that should be used as the display size. This is not the max size of the input but only of the
	 * part that is seen in one go.
	 * @return
	 */
	public int displaySize() default -1;

	/**
	 * The actual maximal input length (for a string) or the precision to use (for a number).
	 * @return
	 */
	public int length() default -1;

	/**
	 * Whether the value is a required value. This sadly cannot use boolean because I need a value
	 * representing "not set here but somewhere else" - and annotations cannot use null ([irony]because that
	 * would be helpful and in conformance with the rest of the language[/irony]).
	 * @return
	 */
	public YesNoType required() default YesNoType.UNKNOWN;

	/**
	 * If you have special non-default string-object and object-string conversion rules for this
	 * property you can specify your own converter class doing those conversions here.
	 * @return
	 */
	public Class< ? extends IConverter< ? >> converterClass() default DummyConverter.class;

	/**
	 * Defines this as a readonly (displayonly) property (by default). It defaults to
	 * "unset" meaning that other metadata defines this property. If no other provider sets it
	 * the property is NOT readonly by default. Cannot use "boolean" - see {@link MetaProperty#required()}.
	 * @return
	 */
	public YesNoType readOnly() default YesNoType.UNKNOWN;

	/**
	 * If this is defined as some Date type this further defines the domain (date only, date time etc). It defaults
	 * to UNKNOWN, which is treated much the same as date-time.
	 * @return
	 */
	public TemporalPresentationType temporal() default TemporalPresentationType.UNKNOWN;

	//	public DateType				dateType() default DateType.UNKNOWN;

	/**
	 * A set of strings that indicate the roles a user must
	 * have to view this field. The permissions
	 * @return
	 */
	public String[] viewpermissions() default {};

	public String[] editpermissions() default {};

	/**
	 * This is a hint string which can help a component factory decide how to render a component. The content
	 * is fully free, and individual factories decide which hints they listen to.
	 * @return
	 */
	public String componentTypeHint() default "";

	/**
	 * Defines one or more parameterless validators for this field.
	 * @return
	 */
	public Class< ? extends IValueValidator< ? >>[] validator() default {};

	public MetaValueValidator[] parameterizedValidator() default {};

	/**
	 * A regular expression defining what text input must look like to be accepted. Can be
	 * used to define a default regexp when a Text control is used for this property; it is
	 * ignored on other input controls. If validation fails the error generated will be a
	 * validation error showing a 'pattern'; this pattern can be set using {@link #regexpUserString()}.
	 * @return
	 */
	public String regexpValidation() default "";

	public String regexpUserString() default "";

	/**
	 * Defines the number class AND it's presentation format for numeric values.
	 * @return
	 */
	public NumericPresentation numericPresentation() default NumericPresentation.UNKNOWN;
}
