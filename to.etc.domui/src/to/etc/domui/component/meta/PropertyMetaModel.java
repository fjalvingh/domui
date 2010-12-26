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

import java.lang.reflect.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.form.*;
import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.domui.converter.*;
import to.etc.domui.util.*;

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
	public Class<T> getActualType();

	/**
	 * The abomination that is Java Generics requires a separate dysfunctional type system to represent
	 * generic typing, at the few places it is available. This returns the generic type information that
	 * is present on whatever type is the return type. This CAN return NULL!!!
	 * @return
	 */
	@Nullable
	public Type getGenericActualType();

	/**
	 * Return any default label (the text to use before the control that inputs this property) for this
	 * property. The default label is obtained from the resource file with the same location and name as
	 * the class file containing the property after doing NLS language replacement; the label text is
	 * looked up in that file as 'propertyname.label='. The code uses the "current" locale as set in NlsContext
	 * to lookup the proper resource file.
	 * @return	the label text, or null if unknown.
	 */
	@Nullable
	public String getDefaultLabel();

	/**
	 * Returns the default hint text (which pops up when the mouse is held over the control that inputs this
	 * item). The hint text is a single, short, line of text. Like the default label this gets looked up in
	 * the resource file for the class this is a property of. The property that is looked up is 'propertyname.hint'.
	 * @return	The hint text, or null if not known.
	 */
	@Nullable
	public String getDefaultHint();

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
	public int getLength();

	/**
	 * Return the specified precision of the numeric field. Returns -1 if not known or not numeric.
	 * @return	the precision, or -1 if unknown. The precision is the total #of digits present in the number, including scale digits.
	 */
	public int getPrecision();

	public int getScale();

	/**
	 * Returns the #chars to be displayed by default for this item. When present this overrides the length or
	 * precision as a size indicator.
	 * @return
	 */
	public int getDisplayLength();

	/**
	 * Returns the name of the property.
	 * @return
	 */
	@Nonnull
	public String getName();

	/**
	 * Returns whether the property should be <i>sortable</i> when used in a default table, and defines
	 * the initial sort direction of the property. This defaults to unsortable.
	 * @return
	 */
	@Nonnull
	public SortableType getSortable();

	//	/**
	//	 * Return an Accessor which is an object that can get or set the value of this property when the object
	//	 * instance is passed into it. This is usually just a wrapper for a single reflection Method invocation
	//	 * but can be more complex when this PropertyMetaModel actually refers to a compound property (a property
	//	 * that was synthesized from a path expression like relation.firstName).
	//	 *
	//	 * @return
	//	 */
	//	@Nonnull
	//	public IValueAccessor< ? > getAccessor();

	/**
	 * Returns the user-specified converter to use when converting this property's value to and from string. Can be null.
	 *
	 * @return
	 */
	@Nullable
	public IConverter<T> getConverter();

	/**
	 * Whether the property is defined as requiring a value.
	 * @return
	 */
	public boolean isRequired();

	/**
	 * Returns T if we know this property to be the PK.
	 * @return
	 */
	public boolean isPrimaryKey();

	/**
	 * Tells if this property represents some kind of database relation (a "parent" property referring to the master of this child record, or a property
	 * representing the list of children).
	 * @return
	 */
	@Nonnull
	public PropertyRelationType getRelationType();

	/**
	 * If the type for this property has a fixed set of domain values (like boolean or enum) this contains
	 * all possible values for this property. So this will contain the actual enum labels or the constants
	 * Boolean.TRUE and Boolean.FALSE. It returns null for other domains.
	 * @return
	 */
	@Nullable
	public Object[] getDomainValues();

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
	public String getDomainValueLabel(Locale loc, Object val);

	/**
	 * If this is defined as some Date type this further defines the domain (date only, date time etc).
	 * @return
	 */
	@Nonnull
	public TemporalPresentationType getTemporal();

	/**
	 * Used for numeric types, this returns how to present the number and defines the number's class, like
	 * a monetary amount. This gets overridden when a converter is set!
	 * @return
	 */
	@Nonnull
	public NumericPresentation getNumericPresentation();

	/**
	 * If this should be represented by a combo this can be set to represent the default combo dataset.
	 * @return
	 */
	@Nullable
	public Class< ? extends IComboDataSet< ? >> getComboDataSet();

	/**
	 * When this relation-property is presented as a single field this can contain a class to render
	 * that field as a string.
	 * @return
	 */
	@Nullable
	public Class< ? extends ILabelStringRenderer< ? >> getComboLabelRenderer();

	@Nullable
	public Class< ? extends INodeContentRenderer< ? >> getComboNodeRenderer();

	/**
	 * For a relation, this is the list of properties that should be shown. This
	 * is needed ONLY when the class metadata of the parent record does not specify
	 * a default display column or columnset.
	 * @return
	 */
	@Nonnull
	public List<DisplayPropertyMetaModel> getComboDisplayProperties();

	@Nonnull
	public List<DisplayPropertyMetaModel> getTableDisplayProperties();

	/**
	 * If this contains null the field can be seen by all users. If it has a value
	 * the first-level array is a set of ORs; the second level are ANDs. Meaning that
	 * an array in the format:
	 * <pre>
	 * { {"admin"}
	 * , {"editroles", "user"}
	 * , {"tester"}
	 * };
	 * </pre>
	 * this means that the field is visible for a user with the roles:
	 * <pre>
	 * 	"admin" OR "tester" OR ("editroles" AND "user")
	 * </pre>
	 * @return
	 */
	@Nullable
	public String[][] getViewRoles();

	/**
	 * Defines the roles that a user must have to edit this field. See the description
	 * at {@link PropertyMetaModel#getViewRoles()} for details.
	 * @return
	 */
	@Nullable
	public String[][] getEditRoles();

	@Nonnull
	public YesNoType getReadOnly();

	/** If marked as transient in the persistent class this returns true */
	public boolean isTransient();

	/**
	 * When present this gives a hint to the component factories to help with choosing a
	 * proper component to <i>select</i> a single record of the type specified by this
	 * property. This is only used when this property points to a parent in an UP relation,
	 * and the child needs to add a control to help it select one parent.
	 * @return
	 */
	@Nullable
	public String getComponentTypeHint();

	/**
	 * When this class is to be selected as a parent in an UP relation using an InputLookup
	 * control this describes the renderer to use to display the <i>currently selected</i>
	 * record in the edit page. If empty this will use the lookupFieldDisplayProperties.
	 * @return
	 */
	@Nullable
	public Class< ? extends INodeContentRenderer< ? >> getLookupFieldRenderer();

	/**
	 * When this class is to be selected as a parent in an UP relation using an InputLookup
	 * control this describes the properties to use to display the <i>currently selected</i>
	 * record in the edit page.
	 * @return
	 */
	@Nonnull
	public List<DisplayPropertyMetaModel> getLookupFieldDisplayProperties();

	/**
	 * When used in a {@link LookupInput} field, this fields are used to show the result of a Search in the DataTable.
	 * @return
	 */
	@Nonnull
	List<DisplayPropertyMetaModel> getLookupFieldTableProperties();

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

	@Nullable
	public PropertyMetaValidator[] getValidators();

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
	ControlFactory getControlFactory();
}
