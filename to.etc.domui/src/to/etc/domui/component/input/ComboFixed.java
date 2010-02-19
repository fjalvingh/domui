package to.etc.domui.component.input;

import java.util.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * Simple combobox handling [String, Object] pairs where the string is the
 * presented label value and the Object represents the values selected.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 26, 2009
 */
public class ComboFixed<T> extends ComboComponentBase<ValueLabelPair<T>, T> {
	public ComboFixed() {
	}

	public ComboFixed(Class< ? extends IComboDataSet<ValueLabelPair<T>>> set, INodeContentRenderer<ValueLabelPair<T>> r) {
		super(set, r);
	}

	public ComboFixed(Class< ? extends IComboDataSet<ValueLabelPair<T>>> dataSetClass) {
		super(dataSetClass);
	}

	public ComboFixed(IComboDataSet<ValueLabelPair<T>> dataSet) {
		super(dataSet);
	}

	public ComboFixed(IListMaker<ValueLabelPair<T>> maker) {
		super(maker);
	}

	public ComboFixed(List<ValueLabelPair<T>> in) {
		super(in);
	}

	@Override
	protected T listToValue(ValueLabelPair<T> in) throws Exception {
		return in.getValue();
	}

	@Override
	protected void renderOptionLabel(SelectOption o, ValueLabelPair<T> object) throws Exception {
		o.add(object.getLabel());
	}
}
