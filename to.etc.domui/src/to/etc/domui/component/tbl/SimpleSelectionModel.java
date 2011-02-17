package to.etc.domui.component.tbl;

import java.util.*;

import javax.annotation.*;

import to.etc.webapp.*;

/**
 * Example implementation of a simple selection model, retaining only the instances PK.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 15, 2011
 */
public abstract class SimpleSelectionModel<T, P> extends AbstractSelectionModel<T> {
	final private Map<P, T> m_selectedSet = new HashMap<P, T>();

	final boolean m_retainInstancies;

	/**
	 * Constructor.
	 * @param retainInstancies Set T in case that model should collect instancies. For lightweight use, set F in case that collecting PKs is sufficient.
	 */
	public SimpleSelectionModel(boolean retainInstancies) {
		m_retainInstancies = retainInstancies;
	}

	@Override
	public boolean isMultiSelect() {
		return true;
	}

	@Override
	public boolean isSelected(@Nonnull T rowinstance) {
		if(null == rowinstance) // Should not happen.
			throw new IllegalArgumentException("null row");
		return m_selectedSet.containsKey(getPk(rowinstance));
	}

	public abstract P getPk(@Nonnull T rowinstance);

	@Override
	public int getSelectionCount() {
		return m_selectedSet.size();
	}

	public void setInstanceSelected(@Nonnull T rowinstance, boolean on) throws Exception {
		if(null == rowinstance) // Should not happen.
			throw new IllegalArgumentException("null row");

		boolean changed;
		if(on) {
			changed = !m_selectedSet.containsKey(getPk(rowinstance));
			if(changed) {
				m_selectedSet.put(getPk(rowinstance), m_retainInstancies ? rowinstance : null);
			}
		} else {
			changed = m_selectedSet.containsKey(getPk(rowinstance));
			if(changed) {
				m_selectedSet.remove(getPk(rowinstance));
			}
		}
		if(!changed)
			return;

		callChanged(rowinstance, on);
	}

	public void clearSelection() throws Exception {
		boolean changed = m_selectedSet.size() > 0;
		if(!changed)
			return;
		m_selectedSet.clear();
		callSelectionCleared();
	}

	public Collection<T> getSelectedInstancies() {
		if(!m_retainInstancies) {
			throw new ProgrammerErrorException("Selection model is not set to retain instancies!");
		}
		return m_selectedSet.values();
	}

	public Set<P> getSelectedPks() {
		return m_selectedSet.keySet();
	}
}

