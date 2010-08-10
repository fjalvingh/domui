package to.etc.domui.component.form;

import java.util.*;

import javax.annotation.*;

public class ModelBindings implements IModelBinding, Iterable<IModelBinding> {
	@Nonnull
	final private List<IModelBinding> m_bindings = new ArrayList<IModelBinding>();

	public void add(@Nonnull IModelBinding b) {
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

	@Override
	@Nonnull
	public Iterator<IModelBinding> iterator() {
		return m_bindings.iterator();
	}

	@Nonnull
	public IModelBinding get(int ix) {
		return m_bindings.get(ix);
	}
}
