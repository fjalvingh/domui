package to.etc.domui.logic;

import javax.annotation.*;

public interface IPropertyPathElement<T> {
	@Nonnull
	public T getInstance();

	public void appendPath(@Nonnull StringBuilder sb);
}
