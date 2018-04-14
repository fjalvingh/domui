package to.etc.domui.databinding.list;

import java.util.*;

import org.eclipse.jdt.annotation.*;

import to.etc.domui.databinding.*;

public interface IListValueChangeListener<E> extends IChangeListener<List<E>, ListValueChangeEvent<E>, IListValueChangeListener<E>> {
	@Override
	public void handleChange(@NonNull ListValueChangeEvent<E> event) throws Exception;
}
