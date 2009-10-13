package to.etc.domui.component.form;

import java.util.*;

public class ModelBindings implements IModelBinding {
	private List<IModelBinding> m_bindings = new ArrayList<IModelBinding>();

	public void add(IModelBinding b) {
		m_bindings.add(b);
	}

	public void moveControlToModel() throws Exception {
		Exception cx = null;
		for(IModelBinding b : m_bindings) {
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
		for(IModelBinding b : m_bindings)
			b.moveModelToControl();
	}

	public int size() {
		return m_bindings.size();
	}

	public void setControlsEnabled(boolean on) {
		for(IModelBinding b : m_bindings)
			b.setControlsEnabled(on);
	}
}
