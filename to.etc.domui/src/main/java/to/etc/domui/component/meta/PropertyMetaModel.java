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

import to.etc.domui.component.controlfactory.PropertyControlFactory;
import to.etc.domui.component.input.IQueryManipulator;
import to.etc.domui.component.input.LookupInput;
import to.etc.domui.component.meta.impl.DisplayPropertyMetaModel;
import to.etc.domui.converter.IConverter;
import to.etc.domui.util.IComboDataSet;
import to.etc.domui.util.ILabelStringRenderer;
import to.etc.domui.util.IRenderInto;
import to.etc.domui.util.IValueAccessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;

/**
 * Contains the metadata that is known for a field (property). Since
 * the future of first-class Java properties is unclear (they would
 * be way too helpful so naturally they get little attention) this
 * uses class/string encoding to represent a property.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 16, 2008
 */
public interface PropertyMetaModel<T> extends IValueAccessor<T> {
	PropertyMetaValidator[] NO_VALIDATORS = new PropertyMetaValidator[0];

	/**
	 * The ClassModel that this property is a property of.
	 * @return
	 */
	@Nonnull
	ClassMetaModel getClassModel();

	/**
	 * If applicable, the value type's class model. Can be asked for explicitly to allow
	 * for non-class-based metamodels. It will return null for all primitive and basic types.
	 * @return
	 */
	@Nullable
	ClassMetaModel getValueModel();


	/**
	 * Returns the actual type of the property's value. This is the return type of the getter function.
	 * @return
	 */
	@Nonnull
	Class<T> getActualType();

	/**
	 * The abomination that is Java Generics requires a separate dysfunctional type system to represent
	 * generic typing, at the few places it is available. This returns the generic type information that
	 * is present on whatever type is the return type. This CAN return NULL!!!
	 * @return
	 */
	@Nullable
	Type getGenericActualType();

	/**
	 * Return any default label (the text to use before the control that inputs this property) for this
	 * property. The default label is obtained from the resource file with the same location and name as
	 * the class file containing the property after doing NLS language replacement; the label text is
	 * looked up in that file as 'propertyname.label='. The code uses the "current" locale as set in NlsContext
	 * to lookup the proper resource file. If there is no label this returns the property's name.
	 * @return	the label text,
	 */
	@Nonnull
	String getDefaultLabel();

	/**
	 * Returns the default hint text (which pops up when the mouse is held over the control that inputs this
	 * item). The hint text is a single, short, line of text. Like the default label this gets looked up in
	 * the resource file for the class this is a property of. The property that is looked up is 'propertyname.hint'.
	 * @return	The hint text, or null if not known.
	 */
	@Nullable
	String getDefaultHint();

	/**
	 * Return the defined length for the item PROVIDED IT WAS SET - THIS SUFFERS FROM AN UTTER FUCKUP IN THE JPA "STANDARD".
	 * This is valid for string-type fields mostly, and should hold the size defined in the database. This field is usually
	 * set from the @Column annotation specified. But since that annotation was written by an idiot with a brain the size of
	 * a pea the "default" value for the length field in that annotation is set to 255. Since 255 is a valid length value this
	 * makes it impossible to determine if the actual length IS 255, or that the user did not provide a value and we need to
	 * calculate one ourselves.
	 *
	 * @return	The size in characters of this item, or -1 if unknown.
	 */
	int getLength();

	/**
	 * Return the specified precision of the numeric field. Returns -1 if not known or not numeric.
	 * @return	the precision, or -1 if unknown. The precision is the total #of digits present in the number, including scale digits.
	 */
	int getPrecision();

	/**
	 * For numeric types, this returns any defined scale. If undefined this returns -1.
	 * @return
	 */
	int getScale();

	/**
	 * Returns the #chars to be displayed by default for this item. When present this overrides the length or
	 * precision as a size indicator.
	 * @return
	 */
	int getDisplayLength();

	/**
	 * Returns the name of the property.
	 * @return
	 */
	@Nonnull
	String getName();

	/**
	 * Returns whether the property should be <i>sortable</i> when used in a default table, and defines
	 * the initial sort direction of the property. This defaults to unsortable.
	 * @return
	 */
	@Nonnull
	SortableType getSortable();

	/**
	 * Returns the user-specified converter to use when converting this property's value to and from string. Can be null.
	 *
	 * @return
	 */
	@Nullable
	IConverter<T> getConverter();

	/**
	 * Whether the property is defined as requiring a value.
	 * @return
	 */
	boolean isRequired();

	/**
	 * Returns T if we know this property to be the PK.
	 * @return
	 */
	boolean isPrimaryKey();

	/**
	 * Tells if this property represents some kind of database relation (a "parent" property referring to the master of this child record, or a property
	 * representing the list of children).
	 * @return
	 */
	@Nonnull
	PropertyRelationType getRelationType();

	/**
	 * If the type for this property has a fixed set of domain values (like boolean or enum) this contains
	 * all possible values for this property. So this will contain the actual enum labels or the constants
	 * Boolean.TRUE and Boolean.FALSE. It returns null for other domains.
	 * @return
	 */
	@Nullable
	Object[] getDomainValues();

	/**
	 * Retrieves the properly localized string representing a domain value in a type which has a fixed set
	 * of domain values (like enum or boolean). So for instance passing in an english locale and the constant
	 * Boolean.TRUE this could return "yes". The translated value is obtained by first looking up a translation
	 * in the base class's properties file; if it is undefined there it will be looked up in the enum's property
	 * file. If a proper representation cannot be found this will return a toString on the value.
	 *
	 * @param loc
	 * @param val
	 * @return
	 */
	@Nullable
	String getDomainValueLabel(Locale loc, Object val);

	/**
	 * If this is defined as some Date type this further defines the domain (date only, date time etc).
	 * @return
	 */
	@Nonnull
	TemporalPresentationType getTemporal();

	/**
	 * Used for numeric types, this returns how to present the number and defines the number's class, like
	 * a monetary amount. This gets overridden when a converter is set!
	 * @return
	 */
	@Nonnull
	NumericPresentation getNumericPresentation();

	/**
	 * If this should be represented by a combo this can be set to represent the default combo dataset.
	 * FIXME Must become instance, not class.
	 * @return
	 */
	@Nullable
	Class< ? extends IComboDataSet< ? >> getComboDataSet();

	/**
	 * When this relation-property is presented as a single field this can contain a class to render
	 * that field as a string.
	 * FIXME Must become instance, not class.
	 * @return
	 */
	@Nullable
	Class< ? extends ILabelStringRenderer< ? >> getComboLabelRenderer();

	/**
	 * When set this renderer should be used to render the nodes in the combobox.
	 * FIXME Must become instance, not class.
	 * @return
	 */
	@Nullable
	Class< ? extends IRenderInto<T>> getComboNodeRenderer();

	/**
	 * For a relation, this is the list of properties that should be shown. This
	 * is needed ONLY when the class metadata of the parent record does not specify
	 * a default display column or columnset.
	 * @return
	 */
	@Nonnull
	List<DisplayPropertyMetaModel> getComboDisplayProperties();

	/**
	 * Reports whether a property is readonly. For Java classes a property is defined as readOnly when it
	 * has no "setter" method.
	 * @return
	 */
	@Nonnull
	YesNoType getReadOnly();

	/** If marked as transient in the persistent class this returns true */
	boolean isTransient();

	/**
	 * When present this gives a hint to the component factories to help with choosing a
	 * proper component to <i>select</i> a single record of the type specified by this
	 * property. This is only used when this property points to a parent in an UP relation,
	 * and the child needs to add a control to help it select one parent.
	 * @return
	 */
	@Nullable
	String getComponentTypeHint();

	/**
	 * When this class is to be selected as a parent in an UP relation using an InputLookup
	 * control this describes the renderer to use to display the <i>currently selected</i>
	 * record in the edit page. If empty this will use the lookupFieldDisplayProperties.
	 *
	 * FIXME Must become instance, not class.
	 * @return
	 */
	@Nullable
	Class< ? extends IRenderInto<T>> getLookupSelectedRenderer();

	/**
	 * When this class is to be selected as a parent in an UP relation using an InputLookup
	 * control this describes the properties to use to display the <i>currently selected</i>
	 * record in the edit page.
	 * @return
	 */
	@Nonnull
	List<DisplayPropertyMetaModel> getLookupSelectedProperties();

	/**
	 * When used in a {@link LookupInput} field, this fields are used to show the result of a Search in the DataTable.
	 * @return
	 */
	@Nonnull
	List<DisplayPropertyMetaModel> getLookupTableProperties();

	/**
	 * When used in a {@link LookupInput} field, this fields are used to create the search inputs.
	 *
	 * @return
	 */
	@Nonnull
	List<SearchPropertyMetaModel> getLookupFieldSearchProperties();

	/**
	 * When used in a {@link LookupInput} field, this fields are used to create the keyword search inputs.
	 *
	 * @return
	 */
	@Nonnull
	List<SearchPropertyMetaModel> getLookupFieldKeySearchProperties();

	/**
	 * Get all validators to run on this property's input after conversion.
	 * @return
	 */
	@Nonnull
	PropertyMetaValidator[] getValidators();

	/**
	 * Returns the regexp to use to validate input.
	 * @return
	 */
	@Nullable
	String getRegexpValidator();

	/**
	 * Use the string to use as the pattern indicator in regexp-validator error messages.
	 * @return
	 */
	@Nullable
	String getRegexpUserString();

	/**
	 * If a specific control factory is to be used to create controls for this item this returns that factory.
	 * @return
	 */
	@Nullable
	PropertyControlFactory getControlFactory();

	/**
	 * If the property has some kind of "annotation" (which in here does not need to be a Java annotation, but
	 * which can also be some other java class containing data) this returns it.
	 * @param <A>
	 * @param annclass
	 * @return
	 */
	@Nullable
	<A> A getAnnotation(@Nonnull Class<A> annclass);

	/**
	 * If the property has some kind of "annotations" (which in here does not need to be a Java annotation, but
	 * which can also be some other java class containing data) this returns all of them.
	 *
	 * @return
	 */
	@Nonnull
	List<Object> getAnnotations();

	/**
	 * Return the column name(s) for this property <b>if</b> this is a persisted column in a persistent class.
	 */
	String[] getColumnNames();

	/**
	 * For Lookup and Combo fields, this can return a QueryManipulator instance that will alter the base
	 * query for the thing to show.
	 * @return
	 */
	@Nullable
	IQueryManipulator<T> getQueryManipulator();

	@Nonnull
	YesNoType getNowrap();
}
