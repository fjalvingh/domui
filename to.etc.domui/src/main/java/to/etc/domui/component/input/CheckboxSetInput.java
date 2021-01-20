package to.etc.domui.component.input;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.util.DomUtil;

import java.util.ArrayList;
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

	@Override
	public CheckboxSetInput<T> css(String... css) {
		super.css(css);
		return this;
	}

	@Override
	public CheckboxSetInput<T> asButtons() {
		super.asButtons();
		return this;
	}

	static public <T extends Enum<T>> CheckboxSetInput<T> create(Class<T> clz, T... exceptions) {
		T[] ar = clz.getEnumConstants();
		List<T> l = new ArrayList<>(ar.length);
		for(T v : ar) {
			if(!DomUtil.contains(exceptions, v)) {
				l.add(v);
			}
		}
		return new CheckboxSetInput<T>(l);
	}
}
