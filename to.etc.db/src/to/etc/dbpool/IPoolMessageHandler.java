package to.etc.dbpool;

/**
 *
 * Created on Oct 18, 2003
 * @author jal
 */
public interface IPoolMessageHandler {
	public void sendPanic(String shortdesc, String body);

	public void sendLogUnexpected(Exception t, String s);
}
