package to.etc.domui.component.lookup;

import to.etc.domui.dom.html.*;
import to.etc.webapp.query.*;

public interface LookupFieldQueryBuilderThingy {
	public NodeBase[] getInputControls();

	public boolean appendCriteria(QCriteria< ? > crit) throws Exception;
}
