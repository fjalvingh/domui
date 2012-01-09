package to.etc.server.janitor;

/**
 * The listener for database update events.
 *
 * @author jal
 * Created on Jan 23, 2005
 */
public interface UpdateListener {
	public void DatabaseUpdateEvent(UpdateEvent e) throws Exception;
}
