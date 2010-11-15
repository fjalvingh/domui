package to.etc.domui.component.input;

import to.etc.domui.dom.header.*;
import to.etc.domui.dom.html.*;

/**
 * We use javascript to wrap addiotnal client side controls around ComboFixedClientFilter, for that we use jquery plugin:
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
