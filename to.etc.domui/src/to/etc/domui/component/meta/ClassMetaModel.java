package to.etc.domui.component.meta;

import java.util.*;

import to.etc.domui.component.meta.impl.*;
import to.etc.domui.util.*;

public interface ClassMetaModel {
	public Class< ? > getActualClass();

	public List<PropertyMetaModel> getProperties();

	/**
	 * Returns the named property on <i>this</i> class. This does not allow
	 * a property path (dotted names).
	 * @param name
	 * @return
	 */
	public PropertyMetaModel findSimpleProperty(String name);

	/**
	 * Returns a property reference to the specified property by following the dotted path
	 * starting at this class. This returns a synthetic PropertyMetaModel which has all of
	 * the values for the last part in the path (the actual property reached), but with
	 * accessors that reach that property by following all intermediary objects.
	 *
	 * @param name
	 * @return
	 */
	public PropertyMetaModel findProperty(String name);

	public boolean isPersistentClass();

	public PropertyMetaModel getPrimaryKey();

	/**
	 * If this class is an enum or represents some enumerated value, this returns the possible value objects. If
	 * this is not a domain type this MUST return null.
	 * @return
	 */
	public Object[] getDomainValues();

	/**
	 * For a Domain type (Enum, Boolean) this returns a label for a given domain value. When called for
	 * a non-domain type this will throw an exception.
	 * @param loc
	 * @param value
	 * @return
	 */
	public String getDomainLabel(Locale loc, Object value);

	/**
	 * Returns the name of this entity in user terms; the returned name is singular.
	 * @return
	 */
	public String getUserEntityName();

	/**
	 * Returns the name of this entity in user terms; the returned name is plural.
	 * @return
	 */
	public String getUserEntityNamePlural();

	/**
	 * If this class is the UP in a relation this specifies that it must
	 * be shown as a COMBOBOX containing choices. It contains a generator
	 * for the values to show. This is a default for all relations in which
	 * this class is the parent; it can be overridden in individual relations.
	 *
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
	 * If this object is shown in a combobox it needs to show the following
	 * properties as the display value.
	 * @return
	 */
	public List<DisplayPropertyMetaModel> getComboDisplayProperties();

	/**
	 * If this object is shown in a Table it needs to show the following
	 * properties there.
	 * @return
	 */
	public List<DisplayPropertyMetaModel> getTableDisplayProperties();

	/**
	 * Returns the SORTED list of search properties defined on this class.
	 * @return
	 */
	public List<SearchPropertyMetaModel> getSearchProperties();

	public String getDefaultSortProperty();

	public SortableType getDefaultSortDirection();

	/**
	 * When present this gives a hint to the component factories to help with choosing a
	 * proper component to <i>select</i> a single record of this type. This is only used
	 * when this class is the parent in an UP relation, and the child needs to add a
	 * control to help it select one parent.
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
}
