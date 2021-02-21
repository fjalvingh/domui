package to.etc.domui.state;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.spi.SpiContainer;
import to.etc.webapp.query.IQContextContainer;

/**
 * The conversatino context attached to a Subpage. This contains all of the state management
 * of subpages.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-11-18.
 */
public class SubConversationContext extends AbstractConversationContext implements IQContextContainer {
	@Nullable
	private SpiContainer m_shelvedIn;

	/**
	 * The container that has this conversation shelved. When set this means that the container
	 * is in charge of the life cycle, not the page.
	 */
	@Nullable public SpiContainer getShelvedIn() {
		return m_shelvedIn;
	}

	public void setShelvedIn(@Nullable SpiContainer shelvedIn) {
		m_shelvedIn = shelvedIn;
	}
}
