package to.etc.domui.databinding.list;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.databinding.*;

public interface IListValueChangeListener<E> extends IChangeListener<List<E>, ListValueChangedEvent<E>, IListValueChangeListener<E>> {
	@Override
	public void handleChange(@Nonnull ListValueChangedEvent<E> event) throws Exception;
}
