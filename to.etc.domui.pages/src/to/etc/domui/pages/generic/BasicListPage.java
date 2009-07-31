package to.etc.domui.pages.generic;

import to.etc.domui.component.lookup.*;
import to.etc.domui.component.tbl.*;

/**
 * Generic page handling some cruddy stuff.
 *
 * @author vmijic
 * Created on 29 Jul 2009
 */
abstract public class BasicListPage<T> extends CustomizableSearchListPage<T> {

	public BasicListPage(Class<T> clz, String titlekey) {
		super(clz, titlekey);
	}

	@Override
	protected void customizeLookupForm(LookupForm<T> lf) {

	}

	@Override
	protected SimpleRowRenderer provideRowRenderer() {
		return new SimpleRowRenderer(getBaseClass());
	}

}
