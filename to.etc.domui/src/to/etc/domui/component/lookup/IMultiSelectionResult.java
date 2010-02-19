package to.etc.domui.component.lookup;

import java.util.*;

/**
 * Listener for event that returns result list from multiple selection input control.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 27 Oct 2009
 */
public interface IMultiSelectionResult<T> {
	public void onReturnResult(List<T> result) throws Exception;
}
