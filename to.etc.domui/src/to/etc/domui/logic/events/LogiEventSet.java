package to.etc.domui.logic.events;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.webapp.eventmanager.*;

/**
 * A builder class which is used to create the logic events set. It builds a single event structure which
 * contains all of the changes made between source and copy instances.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 29, 2012
 */
public class LogiEventSet {
	private StringBuilder m_path_sb = new StringBuilder();

	private int[] m_pathIndices_ar = new int[128];

	private int m_pathIndex;

	@Nonnull
	final private List<LogiEventBase> m_allEvents = new ArrayList<LogiEventBase>();

	@Nonnull
	final private Map<Object, LogiEventInstanceChange> m_instanceEventMap = new HashMap<Object, LogiEventInstanceChange>();

	public void addRootInstanceRemoved(@Nonnull Object root, @Nonnull Object rootCopy) {
	}

	public void addRootInstanceAdded(@Nonnull Object root) {
		getInstance(ChangeType.ADDED, root);
	}

	@Nonnull
	private LogiEventInstanceChange getInstance(@Nonnull ChangeType ct, @Nonnull Object inst) {
		LogiEventInstanceChange ic = m_instanceEventMap.get(inst);
		if(null == ic) {
			ic = new LogiEventInstanceChange(ct, m_path_sb.toString(), inst);
			m_instanceEventMap.put(inst, ic);
			m_allEvents.add(ic);
		}
		return ic;
	}

	/**
	 * Add a property change event for some property on some instance.
	 * @param pmm
	 * @param source
	 * @param copy
	 * @param sourceval
	 * @param copyval
	 */
	public <T, P> void propertyChange(@Nonnull PropertyMetaModel<P> pmm, @Nonnull T source, @Nullable T copy, @Nullable P sourceval, @Nullable P copyval) {
		LogiEventInstanceChange ic = getInstance(ChangeType.MODIFIED, source);
		enter();
		appendPath(pmm.getName());
		LogiEventPropertyChange<P> pc = new LogiEventPropertyChange<P>(m_path_sb.toString(), pmm, copyval, sourceval);
		ic.addChange(pc);
		m_allEvents.add(pc);
		leave();
	}

	public <T, P> void addCollectionClear(@Nonnull PropertyMetaModel<P> pmm, @Nonnull T source, @Nullable T copy, @Nullable P sourceval, @Nullable P copyval) {
		enter();
		appendPath(pmm.getName());
		LogiEventListDelta<T, P, Object> ld = new LogiEventListDelta<T, P, Object>(m_path_sb.toString(), source, pmm, -1, null, ListDeltaType.CLEAR);
		m_allEvents.add(ld);
		leave();
	}

	public <T, P> void addCollectionDelete(@Nonnull PropertyMetaModel<P> pmm, @Nonnull T source, @Nullable T copy, int collectionIndex, @Nullable Object sourceCollectionInstanceEntry) {
		enter();
		appendPath(pmm.getName());
		LogiEventListDelta<T, P, Object> ld = new LogiEventListDelta<T, P, Object>(m_path_sb.toString(), source, pmm, collectionIndex, sourceCollectionInstanceEntry, ListDeltaType.DELETE);
		m_allEvents.add(ld);
		leave();
	}

	public <T, P> void addCollectionAdd(@Nonnull PropertyMetaModel<P> pmm, @Nonnull T source, @Nullable T copy, int collectionIndex, @Nullable Object sourceCollectionInstanceEntry) {
		enter();
		appendPath(pmm.getName());
		LogiEventListDelta<T, P, Object> ld = new LogiEventListDelta<T, P, Object>(m_path_sb.toString(), source, pmm, collectionIndex, sourceCollectionInstanceEntry, ListDeltaType.INSERT);
		m_allEvents.add(ld);
		leave();
	}

	public void enterRoot(int rix) {
		enter();
		appendPath("root[" + rix + "]");
	}

	private void enter() {
		m_pathIndex++;
		if(m_pathIndex >= m_pathIndices_ar.length) {
			int[] nw = new int[m_pathIndex + 128];
			System.arraycopy(m_pathIndices_ar, 0, nw, 0, m_pathIndices_ar.length);
			m_pathIndices_ar = nw;
		}
		m_pathIndices_ar[m_pathIndex] = m_path_sb.length();
	}

	private void appendPath(String s) {
		m_path_sb.setLength(m_pathIndices_ar[m_pathIndex]);			// Reset to start location
		if(m_pathIndex > 1)
			m_path_sb.append("/");
		m_path_sb.append(s);
	}

	private void leave() {
		m_pathIndex--;
		if(m_pathIndex < 0)
			throw new IllegalStateException("Underflow??");
		m_path_sb.setLength(m_pathIndices_ar[m_pathIndex]);
	}

	public void exitRoot(int rix) {
		leave();
	}

	protected void dump(@Nonnull Appendable a) throws Exception {
		a.append("eventSet[\n");
		for(LogiEventBase lb : m_allEvents) {
			lb.dump(a);
		}
		a.append("]");
	}

	@Nonnull
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		try {
			dump(sb);
			return sb.toString();
		} catch(Exception x) {
			return x.toString();
		}
	}

	/**
	 * Create the real event from this builder.
	 * @return
	 */
	public LogiEvent createEvent() {
		return new LogiEvent(m_allEvents, m_instanceEventMap);
	}
}
