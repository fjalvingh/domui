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
	static private final INodeContentRenderer<ValueLabelPair<Object>> STATICRENDERER = new INodeContentRenderer<ValueLabelPair<Object>>() {
		@Override
		public void renderNodeContent(NodeBase component, NodeContainer node, ValueLabelPair<Object> object, Object parameters) throws Exception {
			node.setText(object.getLabel());
		}
	};

	public ComboFixed() {
		initRenderer();
	}

	public ComboFixed(Class< ? extends IComboDataSet<ValueLabelPair<T>>> set, INodeContentRenderer<ValueLabelPair<T>> r) {
		super(set, r);
	}

	public ComboFixed(Class< ? extends IComboDataSet<ValueLabelPair<T>>> dataSetClass) {
		super(dataSetClass);
		initRenderer();
	}

	public ComboFixed(IComboDataSet<ValueLabelPair<T>> dataSet) {
		super(dataSet);
		initRenderer();
	}

	public ComboFixed(IListMaker<ValueLabelPair<T>> maker) {
		super(maker);
		initRenderer();
	}

	public ComboFixed(List<ValueLabelPair<T>> in) {
		super(in);
		initRenderer();
	}

	@Override
	protected T listToValue(ValueLabelPair<T> in) throws Exception {
		return in.getValue();
	}

	@SuppressWarnings("unchecked")
	private void initRenderer() {
		setContentRenderer((INodeContentRenderer) STATICRENDERER); // Another generics fuckup again: you cannot cast this proper, appearently.
	}
	// 20100502 jal Horrible bug! This prevents setting customized option rendering from working!!
	//	@Override
	//	protected void renderOptionLabel(SelectOption o, ValueLabelPair<T> object) throws Exception {
	//		o.add(object.getLabel());
	//	}
}
