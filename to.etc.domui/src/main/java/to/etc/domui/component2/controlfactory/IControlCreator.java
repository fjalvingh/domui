package to.etc.domui.component2.controlfactory;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.dom.html.IControl;

public interface IControlCreator {
	/**
	 * This must return a +ve value when this factory accepts the specified property; the returned value
	 * is an eagerness score. The factory returning the highest eagerness wins.
	 * @param controlClass When set the control factory *must* be able to return a component which is assignment-compatible with this class type. If it cannot it MUST refuse to create the control.
	 * @return
	 */
	<T> int accepts(@NonNull PropertyMetaModel<T> pmm, @Nullable Class<? extends IControl<T>> controlClass);

	/**
	 * This MUST create all nodes necessary for a control to edit the specified item. The nodes must be added
	 * to the container; this <i>must</i> return a ModelBinding to bind and unbind a value to the control
	 * created.
	 * @param pmm
	 * @param controlClass	When set the control factory *must* return a component which is assignment-compatible with this
	 * 						class type. When this method is called it has already (by it's accept method) told us it can, so
	 * 						not creating the proper type is not an option.
	 * @param container
	 * @return
	 */
	@NonNull <T, C extends IControl<T>> C createControl(@NonNull PropertyMetaModel<T> pmm, @Nullable Class<C> controlClass);
}
