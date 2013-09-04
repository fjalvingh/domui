package to.etc.domui.databinding;

import java.util.*;

import javax.annotation.*;

public class IListChangeEvent<T> {
	@Nonnull
	final private List<ListChange<T>> m_changeList;

	public IListChangeEvent(@Nonnull List<ListChange<T>> changeList) {
		m_changeList = changeList;
	}

	@Nonnull
	public List<ListChange<T>> getChanges() {
		return m_changeList;
	}
}
