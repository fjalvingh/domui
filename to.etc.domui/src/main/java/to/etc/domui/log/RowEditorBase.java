package to.etc.domui.log;

import to.etc.domui.component.controlfactory.ModelBindings;
import to.etc.domui.component.layout.ErrorMessageDiv;
import to.etc.domui.component.ntbl.IEditor;
import to.etc.domui.component.tbl.TableModelTableBase;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;

public class RowEditorBase<T> extends Div implements IEditor {
	private final T m_instance;

	private final TableModelTableBase<T> m_model;

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

	public T getInstance() {
		return m_instance;
	}

	@Override
	public void createContent() throws Exception {
		super.createContent();
		add(new ErrorMessageDiv(this, true));
		FormBuilder fb = new FormBuilder(this);
		addProperties(fb);
	}

	protected void addProperties(FormBuilder builder) throws Exception {
		for(String col : m_cols) {
			builder.property(getInstance(), col).control();
		}
	}
}
