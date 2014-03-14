package to.etc.domui.log;

import to.etc.domui.component.controlfactory.*;
import to.etc.domui.component.form.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.ntbl.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.html.*;

public class RowEditorBase<T> extends Div implements IEditor {
	private final T m_instance;

	private final TableModelTableBase<T> m_model;

	private HorizontalFormBuilder m_builder;

	private ModelBindings m_bindings;
	
	private final String[] m_cols;
	
	@Override
	public boolean validate(boolean isnew) throws Exception {
		m_bindings.moveControlToModel();
		return true;
	}

	public RowEditorBase(T instance, TableModelTableBase<T> model, String... cols) {
		super();
		m_instance = instance;
		m_model = model;
		m_cols = cols;
	}

	@Override
	public void createContent() throws Exception {
		super.createContent();
		add(new ErrorMessageDiv(this, true));
		m_builder = new HorizontalFormBuilder(m_instance);
		addProperties(m_builder);
		add(m_builder.finish());
		m_bindings = m_builder.getBindings();
		m_bindings.moveModelToControl();
	}

	protected void addProperties(HorizontalFormBuilder builder) {
		builder.addProps(m_cols);
	}
}
