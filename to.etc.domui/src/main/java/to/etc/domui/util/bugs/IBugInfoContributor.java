package to.etc.domui.util.bugs;

/**
 * Implementations can add extra context information to bugs, so that
 * as much info as possible can be collected in a bug report. Instances
 * must be fully reentrant and threadsafe.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-8-18.
 */
public interface IBugInfoContributor {
	void onContribute(BugItem bug) throws Exception;
}
