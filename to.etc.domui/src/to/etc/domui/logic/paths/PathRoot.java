package to.etc.domui.logic.paths;

import javax.annotation.*;

public class PathRoot<T> implements IPropertyPathElement<T> {
	@Nonnull
	private T m_instance;

	public PathRoot(@Nonnull T instance) {
		m_instance = instance;
	}

	@Override
	@Nonnull
	public T getInstance() {
		return m_instance;
	}

	@Override
	public void appendPath(@Nonnull StringBuilder sb) {}
}
