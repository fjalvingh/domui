package to.etc.domui.component2.controlfactory;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.dom.html.Checkbox;
import to.etc.domui.dom.html.IControl;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2/3/15.
 */
public class ControlCreatorBoolean implements IControlCreator {
	@Override public <T> int accepts(@NonNull PropertyMetaModel<T> pmm, @Nullable Class<? extends IControl<T>> controlClass) {
		if(pmm.getActualType() == Boolean.class || pmm.getActualType() == boolean.class) {
			if(Checkbox.TYPE_HINT.equals(pmm.getComponentTypeHint()))
				return 10;
		}
		return -1;
	}

	@NonNull @Override public <T, C extends IControl<T>> C createControl(@NonNull PropertyMetaModel<T> pmm, @Nullable Class<C> controlClass) {
		Checkbox cb = new Checkbox();
		return (C) cb;
	}
}
