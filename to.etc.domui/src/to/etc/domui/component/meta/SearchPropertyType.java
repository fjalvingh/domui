package to.etc.domui.component.meta;

/**
 * Represents a specification how search item defined with {@link SearchProperty} would be used.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 15 Jan 2010
 */
public enum SearchPropertyType {
	/**
	 *	Search metadata would be used to generate search field. 
	 */
	SEARCH_FIELD,
	/**
	 *	Search metadata would be used to generate built in key word search conditions. 
	 */
	KEYWORD,
	/**
	 *	Search metadata would be used both to generate both search fields and key word search conditions. 
	 */
	BOTH
}
