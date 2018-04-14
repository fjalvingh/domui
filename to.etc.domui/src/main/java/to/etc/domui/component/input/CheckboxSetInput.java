package to.etc.domui.component.input;

import org.eclipse.jdt.annotation.NonNull;

import java.util.List;

public class CheckboxSetInput<T> extends CheckboxSetInputBase<T, T> {
	public CheckboxSetInput() {}

	public CheckboxSetInput(List<T> data) {
		super(data);
	}

	@Override
	@NonNull
	protected T listToValue(@NonNull T in) throws Exception {
		return in;
	}
}
