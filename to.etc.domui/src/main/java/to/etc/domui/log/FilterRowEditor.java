package to.etc.domui.log;

import to.etc.domui.component.tbl.TableModelTableBase;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.TextNode;
import to.etc.domui.log.data.Filter;
import to.etc.log.handler.LogFilterType;

import javax.annotation.Nonnull;

public class FilterRowEditor extends RowEditorBase<Filter> {

	private IControl<LogFilterType> m_typeCtl;

	private IControl<String> m_keyCtl;

	private TextNode m_holder;

	public FilterRowEditor(Filter instance, TableModelTableBase<Filter> model, String[] cols) {
		super(instance, model, cols);
	}

	@Override
	protected void addProperties(FormBuilder builder) throws Exception {
		m_typeCtl = (IControl<LogFilterType>) builder.property(getInstance(), Filter.pTYPE).control();
		m_keyCtl = (IControl<String>) builder.property(getInstance(), Filter.pKEY).control();
		builder.property(getInstance(), Filter.pVALUE).control();
	}

	@Override
	protected void afterCreateContent() throws Exception {
		// TODO Auto-generated method stub
		super.afterCreateContent();
		m_holder = new TextNode(m_keyCtl.getValueSafe());
		m_typeCtl.setOnValueChanged(new IValueChanged<NodeBase>() {
			@Override
			public void onValueChanged(@Nonnull NodeBase component) throws Exception {
				updateKeyByType(m_typeCtl, m_keyCtl, m_holder);
			}
		});
		if(LogFilterType.SESSION == m_typeCtl.getValue()) {
			updateKeyByType(m_typeCtl, m_keyCtl, m_holder);
		}
	}

	private void updateKeyByType(final IControl<LogFilterType> typeCtl, final IControl<String> keyCtl, final TextNode holder) {
		LogFilterType type = typeCtl.getValue();
		keyCtl.setDisabled(type == LogFilterType.SESSION);
		if(type == LogFilterType.SESSION) {
			holder.setText(keyCtl.getValueSafe());
			keyCtl.setValue("session");
		} else {
			keyCtl.setValue(holder.getText());
		}
	}
}
