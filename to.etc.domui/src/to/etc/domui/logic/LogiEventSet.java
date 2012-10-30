package to.etc.domui.logic;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;

/**
 * A single event structure which contains all of the changes made between source and copy
 * instances.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 29, 2012
 */
public class LogiEventSet {
	private StringBuilder m_path_sb = new StringBuilder();

	private int[] m_pathIndices_ar = new int[128];

	private int m_pathIndex;

	@Nonnull
	final private Map<Object, LogiEventInstanceChange> m_instanceEventMap = new HashMap<Object, LogiEventInstanceChange>();

	public void addRootInstanceRemoved(@Nonnull Object root, @Nonnull Object rootCopy) {
	}

	public void addRootInstanceAdded(@Nonnull Object root) {
		// TODO Auto-generated method stub

	}

	@Nonnull
	private LogiEventInstanceChange getInstance(@Nonnull Object inst) {
		LogiEventInstanceChange ic = m_instanceEventMap.get(inst);
		if(null == ic) {
			ic = new LogiEventInstanceChange(m_path_sb.toString(), inst);
			m_instanceEventMap.put(inst, ic);
		}
		return ic;
	}

	public <T, P> void propertyChange(@Nonnull PropertyMetaModel<P> pmm, @Nonnull T source, @Nonnull T copy, @Nullable P sourceval, @Nullable P copyval) {
		LogiEventInstanceChange ic = getInstance(source);
		enter();
		appendPath("#" + pmm.getName());
		LogiEventPropertyChange<P> pc = new LogiEventPropertyChange<P>(m_path_sb.toString(), pmm, copyval, sourceval);
		ic.addChange(pc);
		leave();
	}

	public <T, P> void addCollectionClear(@Nonnull PropertyMetaModel<P> pmm, @Nonnull T source, @Nonnull T copy, @Nullable P sourceval, @Nullable P copyval) {
		enter();
		appendPath("#" + pmm.getName());
		//-- Log

		leave();
	}

	public <T, P> void addCollectionDelete(@Nonnull PropertyMetaModel<P> pmm, @Nonnull T source, @Nonnull T copy, int collectionIndex, @Nullable Object sourceCollectionInstanceEntry) {
		enter();
		appendPath("#" + pmm.getName());
		//-- Log

		leave();
	}

	public <T, P> void addCollectionAdd(@Nonnull PropertyMetaModel<P> pmm, @Nonnull T source, @Nonnull T copy, int collectionIndex, @Nullable Object sourceCollectionInstanceEntry) {
		enter();
		appendPath("#" + pmm.getName());
		//-- Log

		leave();
	}


	public void enterRoot(int rix) {
		enter();
		appendPath("root[" + rix + "]");

		// TODO Auto-generated method stub

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


}
