package to.etc.iocular.container;

import to.etc.iocular.Container;
import to.etc.iocular.def.ComponentDef;

/**
 * Reference to a container-created object. Each cached object
 * has an unique copy of this structure while it is active being
 * created or being cached. This structure handles the locking
 * and other chores associated with the component.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 27, 2007
 */
public class ContainerObjectRef {
	private ComponentDef		m_def;

	/** If an actual object has been created *and* it is valid this holds that object. */
	private Object				m_instance;

	/** If creating the object has caused a problem this holds that problem (wrapped if not a RuntimeException). */
	private RuntimeException	m_exception;

	private Container			m_allocatingContainer;

	private RefState			m_state;

	ContainerObjectRef(ComponentDef cd) {
		m_def = cd;
		m_state	= RefState.NEW;
	}

	/**
	 * If this ref holds a valid object this returns that object
	 * immediately. If not this tries to claim ownership to the
	 * object.
	 *
	 * @return
	 */
	public synchronized Object	retrieveOrOwn(Container owner) {
		long ts = -1;
		for(;;) {
			switch(m_state) {
				default:
					throw new IllegalStateException("!? Bad refstate "+m_state);
				case OKAY:
					return m_instance;
				case ERROR:
					throw m_exception;
				case NEW:
					//-- Allow the container passed to create this thingy.
					m_allocatingContainer = owner;
					m_state = RefState.ALLOCATING;
					return null;
				case ALLOCATING:
					//-- Something is already allocating; 
					break;
				
			}
			//-- Someone is allocating.. Wait for 'm to finish then loop.
			if(ts == -1)
				ts = System.currentTimeMillis() + 1000*20;			// Allow max. 20 seconds init time
			else if(ts >= System.currentTimeMillis())
				throw new IllegalStateException("Allocating took too much time!?");
			try {
				wait(20000);
			} catch(Exception x) {
			}
		}
	}

	public synchronized void releaseOwnership() {
		if(m_state == RefState.ALLOCATING) {
			m_state = RefState.NEW;
			notifyAll();
		}
	}

	public synchronized void	setObject(Object o) {
		if(m_state != RefState.ALLOCATING)
			throw new IllegalStateException("!? Internal: RefState is not 'allocating'");
		m_state = RefState.OKAY;
		m_instance = o;
		notifyAll();
	}
	
}
