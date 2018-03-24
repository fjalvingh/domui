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
package to.etc.domui.component.input;

import to.etc.domui.dom.header.*;
import to.etc.domui.dom.html.*;

/**
 * We use javascript to wrap additional client side controls around ComboFixedClientFilter, for that we use jquery plugin:
 * jquery.clientOptionFilter.js - client side filtering options select, author Vladimir Mijic (vmijic@execom.eu)
 *
 * Intended to be used for larger select boxes that need client side filtering by substring.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 12 nov 2010
 */

public class ComboFixedClientFilter<T> extends ComboFixed<T> {

	public ComboFixedClientFilter() {
		super();
	}

	@Override
	public void createContent() throws Exception {
		super.createContent();
		getActualID();
		appendJavascript("$(document).ready(function() {" + getInitializeJavascriptCall() + "});");
	}

	@Override
	protected void onUnshelve() throws Exception {
		//Since real filter option UI is actually created on browser side, we have to recreate it when page is unshelved.
		appendJavascript(getInitializeJavascriptCall());
	}

	private String getInitializeJavascriptCall() {
		return "ClientOptionFilter.initialize('" + getActualID() + "');";
	}

	/**
	 * Force the javascript to load when this panel is used.
	 * @see to.etc.domui.dom.html.NodeBase#onAddedToPage(to.etc.domui.dom.html.Page)
	 */
	@Override
	public void onAddedToPage(Page p) {
		getPage().addHeaderContributor(HeaderContributor.loadJavascript("$js/jquery.clientOptionFilter.js"), 100);
	}
}
