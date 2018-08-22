package to.etc.domui.util.bugs;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 22-8-18.
 */
public interface IBugListenerEx extends IBugListener {
	void reportRepeats(String bugHash, long since, int repeats) throws Exception;
}
