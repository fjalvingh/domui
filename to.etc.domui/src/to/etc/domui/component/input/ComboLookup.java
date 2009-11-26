package to.etc.domui.component.input;

import java.util.*;

import to.etc.domui.util.*;

public class ComboLookup<T> extends ComboComponentBase<T, T> {
	public ComboLookup() {}

	public ComboLookup(IListMaker<T> maker) {
		super(maker);
	}

	public ComboLookup(List<T> in) {
		super(in);
	}

	public ComboLookup(Class< ? extends IComboDataSet<T>> set, INodeContentRenderer<T> r) {
		super(set, r);
	}

	@Override
	protected T listToValue(T in) throws Exception {
		return in;
	}
}
