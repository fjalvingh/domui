package to.etc.domui.util;

import java.util.*;

import to.etc.domui.state.*;

/**
 * Some kind of combobox dataset. This returns a list of thingies for a combobox, depending
 * on the current conversation context.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 11, 2008
 */
public interface IComboDataSet<T> {
	public List<T> getComboDataSet(ConversationContext cc, String[] parameters) throws Exception;
}
