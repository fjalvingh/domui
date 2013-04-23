package to.etc.domui.databinding;

import javax.annotation.*;

public interface IChangeListener<V, E extends IChangeEvent<V, E, L>, L extends IChangeListener<V, E, L>> {
	public void handleChange(@Nonnull E event) throws Exception;
}
