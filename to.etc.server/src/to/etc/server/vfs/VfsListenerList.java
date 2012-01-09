package to.etc.server.vfs;

import java.lang.ref.*;
import java.util.*;

import to.etc.server.syslogger.*;

/**
 * Encapsulates a list of registered listeners.
 *
 * @author jal
 * Created on Dec 5, 2005
 */
final public class VfsListenerList {
	/**
	 * @uml.property  name="list"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="to.mumble.nema.vfs.VfsChangeListener"
	 */
	private List<Reference<VfsChangeListener>>	m_list	= null;

	public synchronized void addListener(VfsChangeListener l) {
		if(l == null)
			throw new NullPointerException("Listener cannot be null.");
		if(m_list == null)
			m_list = new ArrayList<Reference<VfsChangeListener>>();
		else {
			for(Reference<VfsChangeListener> r : m_list) {
				if(r.get() == l)
					return;
			}
		}
		m_list.add(new WeakReference<VfsChangeListener>(l));
	}

	public synchronized void removeListener(VfsChangeListener l) {
		if(l == null)
			throw new NullPointerException("Listener cannot be null.");
		if(m_list == null)
			return;
		for(int i = m_list.size(); --i >= 0;) {
			Reference<VfsChangeListener> ref = m_list.get(i);
			if(ref.get() == l) {
				m_list.remove(i);
				return;
			}
		}
	}

	/**
	 * Posts a change event to all registered listeners. This also cleans the
	 * table of all cleared references.
	 * @param ev
	 */
	public void post(VfsChangeEvent ev) {
		List<VfsChangeListener> list = null;
		synchronized(this) {
			int ix = m_list.size();
			while(ix > 0) {
				ix--;
				Reference<VfsChangeListener> ref = m_list.get(ix);
				VfsChangeListener l = ref.get();
				if(l == null) {
					//-- Remove from list
					m_list.remove(ix);
				} else {
					if(list == null)
						list = new ArrayList(m_list.size());
					list.add(l);
				}
			}
			if(list == null)
				return;
		}

		//-- Call all listeners out of lock.
		for(VfsChangeListener l : list) {
			try {
				l.vfsResourceChanged(ev);
			} catch(Exception x) {
				Panicker.getInstance().logUnexpected(x, "In calling vfs change listener", null);
				x.printStackTrace();
			}
		}
	}
}
