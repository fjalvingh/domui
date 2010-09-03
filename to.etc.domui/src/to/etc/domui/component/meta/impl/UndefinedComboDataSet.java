package to.etc.domui.component.meta.impl;

import java.util.*;

import to.etc.domui.state.*;
import to.etc.domui.util.*;

public class UndefinedComboDataSet implements IComboDataSet<Object> {
	@Override
	public List<Object> getComboDataSet(ConversationContext cc, String[] parameters) throws Exception {
		throw new IllegalStateException("Do not call me"); // FIXME Implement, please
	}

}
