package to.etc.domui.component2.controlfactory;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;

import javax.annotation.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2/3/15.
 */
public class ControlCreatorBoolean implements IControlCreator {
	@Override public <T> int accepts(@Nonnull PropertyMetaModel<T> pmm, @Nullable Class<? extends IControl<T>> controlClass) {
		if(pmm.getActualType() == Boolean.class || pmm.getActualType() == boolean.class) {
			if(Checkbox.TYPE_HINT.equals(pmm.getComponentTypeHint()))
				return 10;
		}
		return -1;
	}

	@Nonnull @Override public <T, C extends IControl<T>> C createControl(@Nonnull PropertyMetaModel<T> pmm, @Nullable Class<C> controlClass) {
		Checkbox cb = new Checkbox();
		return (C) cb;
	}
}
