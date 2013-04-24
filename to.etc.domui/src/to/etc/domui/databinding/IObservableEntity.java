package to.etc.domui.databinding;

import javax.annotation.*;

public interface IObservableEntity {
	@Nonnull
	public IObservableValue< ? > observableProperty(@Nonnull String property);
}
