package to.etc.domui.component.form;

import java.util.*;

public class ModelBindings implements ModelBinding {
	private List<ModelBinding> m_bindings = new ArrayList<ModelBinding>();

	public void add(ModelBinding b) {
		m_bindings.add(b);
	}

	public void moveControlToModel() throws Exception {
		for(ModelBinding b : m_bindings)
			b.moveControlToModel();
	}

	public void moveModelToControl() throws Exception {
		for(ModelBinding b : m_bindings)
			b.moveModelToControl();
	}

	public int size() {
		return m_bindings.size();
	}

	public void setEnabled(boolean on) {
		for(ModelBinding b : m_bindings)
			b.setEnabled(on);
	}
}
