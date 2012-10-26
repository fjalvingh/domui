package to.etc.domui.log;

import to.etc.domui.component.controlfactory.*;
import to.etc.domui.component.form.*;
import to.etc.domui.component.input.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.ntbl.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.log.data.*;

public class LoggerRowEditor<T extends LoggerDefBase> extends Div implements IEditor {
	private LoggerDefBase m_instance;

	private TableModelTableBase<T> m_model;
	
	private HorizontalFormBuilder m_builder; 

	private ModelBindings m_bindings; 

	private String[] m_cols; 
	
	@Override
	public boolean validate(boolean isnew) throws Exception {
		m_bindings.moveControlToModel();
		return true;
	}

	public LoggerRowEditor(T instance, TableModelTableBase<T> model, String... cols) {
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
		m_builder.addProps(m_cols);
		add(m_builder.finish());
		m_bindings = m_builder.getBindings();
		m_bindings.moveModelToControl();
	}
}
