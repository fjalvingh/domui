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

import javax.annotation.Nonnull;
import java.util.Comparator;

public interface SearchPropertyMetaModel {
	Comparator<SearchPropertyMetaModel> BY_ORDER = (a, b) -> a.getOrder() - b.getOrder();

	/**
	 * When T (default) the search is done in a case-independent way provided we are looking
	 * for some string value.
	 */
	boolean isIgnoreCase();

	/**
	 * The order of this search item in the total list of items. This is only used to
	 * set the display order of the items; they will be ordered by ascending [Order;Name].
	 */
	int getOrder();

	/**
	 * To prevent searching over the entire database you can specify a minimum number
	 * of characters that must be present before the search is allowed on this field. This
	 * would prevent huge searches when only a single letter is entered.
	 */
	int getMinLength();

	@Nonnull
	PropertyMetaModel<?> getProperty();

	//String getPropertyName();
	//
	//List<PropertyMetaModel< ? >> getPropertyPath();

	/**
	 * Returns the text to use as the control label, if defined in the metadata.
	 */
	String getLookupLabel();

	/**
	 * Returns the text to use as the control label, if defined in the metadata.
	 */

	String getLookupHint();

	/**
	 * When T, and when search field is resolved as LookupInput/LookupInput2 type of control, lookup popup is shown with performed initial search automatically.
	 */
	boolean isPopupSearchImmediately();

	/**
	 * When T, and when search field is resolved as LookupInput/LookupInput2 type of control, lookup popup is shown with initially collapsed search options on it's LookupForm.
	 */
	boolean isPopupInitiallyCollapsed();
}
