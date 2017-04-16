package to.etc.domui.component.input;

import java.util.*;

import javax.annotation.*;

public class CheckboxSetInput<T> extends CheckboxSetInputBase<T, T> {
	public CheckboxSetInput() {}

	public CheckboxSetInput(List<T> data) {
		super(data);
	}

	@Override
	@Nonnull
	protected T listToValue(@Nonnull T in) throws Exception {
		return in;
	}
}
