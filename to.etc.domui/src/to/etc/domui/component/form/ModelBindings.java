package to.etc.domui.component.form;

import java.util.*;

public class ModelBindings implements ModelBinding {
	private List<ModelBinding> m_bindings = new ArrayList<ModelBinding>();

	public void add(ModelBinding b) {
		m_bindings.add(b);
	}

	public void moveControlToModel() throws Exception {
		Exception cx = null;
		for(ModelBinding b : m_bindings) {
			try {
				b.moveControlToModel();
			} catch(Exception x) {
				if(cx == null)
					cx = x;
			}
		}
		if(cx != null)
			throw cx;
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
