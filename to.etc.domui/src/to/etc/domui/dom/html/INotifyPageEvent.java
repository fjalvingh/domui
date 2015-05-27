package to.etc.domui.dom.html;

import javax.annotation.*;

/**
 * Interface used to execute events between pages which are not aware of their surroundings.
 *
 * @author <a href="mailto:avisekruna@execom.eu">Andjelko Visekruna</a>
 * Created on May 27, 2015
 */
public interface INotifyPageEvent {

	void execute(@Nullable String command) throws Exception;
}
