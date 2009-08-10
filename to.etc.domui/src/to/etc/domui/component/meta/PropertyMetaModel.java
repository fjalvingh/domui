package to.etc.domui.component.meta;

import java.util.*;

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
public interface PropertyMetaModel {
	public ClassMetaModel getClassModel();

	/**
	 * Returns the actual type of the property's value. This is the return type of the getter function.
	 * @return
	 */
	public Class< ? > getActualType();

	/**
	 * Return any default label (the text to use before the control that inputs this property) for this
	 * property. The default label is obtained from the resource file with the same location and name as
	 * the class file containing the property after doing NLS language replacement; the label text is
	 * looked up in that file as 'propertyname.label='. The code uses the "current" locale as set in NlsContext
	 * to lookup the proper resource file.
	 * @return	the label text, or null if unknown.
	 */
	public String getDefaultLabel();

	/**
	 * Returns the default hint text (which pops up when the mouse is held over the control that inputs this
	 * item). The hint text is a single, short, line of text. Like the default label this gets looked up in
	 * the resource file for the class this is a property of. The property that is looked up is 'propertyname.hint'.
	 * @return	The hint text, or null if not known.
	 */
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
	public String getName();

	/**
	 * Returns whether the property should be <i>sortable</i> when used in a default table, and defines
	 * the initial sort direction of the property. This defaults to unsortable.
	 * @return
	 */
	public SortableType getSortable();

	/**
	 * Return an Accessor which is an object that can get or set the value of this property when the object
	 * instance is passed into it. This is usually just a wrapper for a single reflection Method invocation
	 * but can be more complex when this PropertyMetaModel actually refers to a compound property (a property
	 * that was synthesized from a path expression like relation.firstName).
	 *
	 * @return
	 */
	public IValueAccessor< ? > getAccessor();

	/**
	 * Returns the converter to use when converting this property's value to and from string. Can be null.
	 *
	 * @return
	 */
	public Class< ? extends IConverter< ? >> getConverterClass();

	/**
	 * If known returns the best converter to use to convert this to a string value and v.v. This will
	 * return the proper (calculated or set) converter to use for numeric types.
	 * @return
	 */
	public IConverter< ? > getBestConverter();

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
	public PropertyRelationType getRelationType();

	/**
	 * If the type for this property has a fixed set of domain values (like boolean or enum) this contains
	 * all possible values for this property. So this will contain the actual enum labels or the constants
	 * Boolean.TRUE and Boolean.FALSE. It returns null for other domains.
	 * @return
	 */
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
	public String getDomainValueLabel(Locale loc, Object val);

	/**
	 * If this is defined as some Date type this further defines the domain (date only, date time etc).
	 * @return
	 */
	public TemporalPresentationType getTemporal();

	/**
	 * Used for numeric types, this returns how to present the number and defines the number's class, like
	 * a monetary amount. This gets overridden when a converter is set!
	 * @return
	 */
	public NumericPresentation getNumericPresentation();

	/**
	 * If this should be represented by a combo this can be set to represent the default combo dataset.
	 * @return
	 */
	public Class< ? extends IComboDataSet< ? >> getComboDataSet();

	/**
	 * When this relation-property is presented as a single field this can contain a class to render
	 * that field as a string.
	 * @return
	 */
	public Class< ? extends ILabelStringRenderer< ? >> getComboLabelRenderer();

	public Class< ? extends INodeContentRenderer< ? >> getComboNodeRenderer();

	/**
	 * For a relation, this is the list of properties that should be shown. This
	 * is needed ONLY when the class metadata of the parent record does not specify
	 * a default display column or columnset.
	 * @return
	 */
	public List<DisplayPropertyMetaModel> getComboDisplayProperties();

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
	public String[][] getViewRoles();

	/**
	 * Defines the roles that a user must have to edit this field. See the description
	 * at {@link PropertyMetaModel#getViewRoles()} for details.
	 * @return
	 */
	public String[][] getEditRoles();

	public YesNoType getReadOnly();

	/**
	 * When present this gives a hint to the component factories to help with choosing a
	 * proper component to <i>select</i> a single record of the type specified by this
	 * property. This is only used when this property points to a parent in an UP relation,
	 * and the child needs to add a control to help it select one parent.
	 * @return
	 */
	public String getComponentTypeHint();

	/**
	 * When this class is to be selected as a parent in an UP relation using an InputLookup
	 * control this describes the renderer to use to display the <i>currently selected</i>
	 * record in the edit page. If empty this will use the lookupFieldDisplayProperties.
	 * @return
	 */
	public Class< ? extends INodeContentRenderer< ? >> getLookupFieldRenderer();

	/**
	 * When this class is to be selected as a parent in an UP relation using an InputLookup
	 * control this describes the properties to use to display the <i>currently selected</i>
	 * record in the edit page.
	 * @return
	 */
	public List<DisplayPropertyMetaModel> getLookupFieldDisplayProperties();

	public PropertyMetaValidator[] getValidators();

}
