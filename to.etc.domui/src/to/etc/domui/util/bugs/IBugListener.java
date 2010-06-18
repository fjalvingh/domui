package to.etc.domui.util.bugs;

import javax.annotation.*;

/**
 * Receives all posted bug events on a thread and has to do something to them.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 18, 2010
 */
public interface IBugListener {
	void bugSignaled(@Nonnull BugItem item);
}
