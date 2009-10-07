package to.etc.domui.component.meta;


public interface SearchPropertyMetaModel {
	/**
	 * When T (default) the search is done in a case-independent way provided we are looking
	 * for some string value.
	 * @return
	 */
	boolean isIgnoreCase();

	/**
	 * The order of this search item in the total list of items. This is only used to
	 * set the display order of the items; they will be ordered by ascending [Order;Name].
	 * @return
	 */
	int getOrder();

	/**
	 * To prevent searching over the entire database you can specify a minimum number
	 * of characters that must be present before the search is allowed on this field. This
	 * would prevent huge searches when only a single letter is entered.
	 * @return
	 */
	int getMinLength();

	String getPropertyName();

	String getLookupLabel();
}
