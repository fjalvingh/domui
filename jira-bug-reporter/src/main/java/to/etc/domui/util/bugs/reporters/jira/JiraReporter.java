package to.etc.domui.util.bugs.reporters.jira;

import to.etc.domui.util.bugs.BugItem;
import to.etc.domui.util.bugs.BugSeverity;
import to.etc.domui.util.bugs.IBugListener;

/**
 * This reporter is used to report bugs reported by the Bug framework to a Jira
 * instance. The data for the bug is collected, a hash for the bug is calculated
 * and a search is initiated for a user field that would contain hashes for bugs.
 *
 * If a bug with the same hash is found the message is added to the existing bug,
 * else we create a new one.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-8-18.
 */
public class JiraReporter implements IBugListener {





	@Override public void bugSignaled(BugItem item) {
		if(! accept(item))
			return;








	}

	/**
	 * By default only report PANIC items.
	 */
	protected boolean accept(BugItem item) {
		return item.getSeverity() == BugSeverity.PANIC;
	}
}
