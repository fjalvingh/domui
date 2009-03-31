package to.etc.domui.component.input;

import java.util.*;

import to.etc.domui.util.*;

public class ComboLookup2<T> extends ComboComponentBase<T, T> {
	public ComboLookup2() {
	}

	public ComboLookup2(IListMaker<T> maker) {
		super(maker);
	}

	public ComboLookup2(List<T> in) {
		super(in);
	}

	@Override
	protected T listToValue(T in) throws Exception {
		return in;
	}
}
