package to.etc.domui.component.controlfactory;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;

/**
 * display marker
 *
 *
 * @author <a href="mailto:dennis.bekkering@itris.nl">Dennis Bekkering</a>
 * Created on Jan 30, 2013
 */
public class RelationLookupDisplayInput<T> extends LookupInput<T> implements IDisplayControl<T> {

	public RelationLookupDisplayInput(Class<T> lookupClass, ClassMetaModel metaModel) {
		super(lookupClass, metaModel);
	}


}
