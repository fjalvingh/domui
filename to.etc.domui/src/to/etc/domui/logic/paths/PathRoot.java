package to.etc.domui.logic.paths;

import javax.annotation.*;

public class PathRoot<T> implements IPropertyPathElement<T> {
	@Nullable
	private T m_instance;

	@Override
	@Nonnull
	public T getInstance() {
		return m_instance;
	}

	@Override
	public void appendPath(@Nonnull StringBuilder sb) {}
}
