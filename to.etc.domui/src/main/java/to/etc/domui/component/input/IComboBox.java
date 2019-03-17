package to.etc.domui.component.input;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.util.IRenderInto;
import to.etc.webapp.query.QCriteria;

import java.util.List;

/**
 * Represents a control that looks up a value somehow. Examples
 * are Comboboxes and LookupInput controls.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 7-3-19.
 */
public interface IComboBox<T> extends IControl<T> {
	@Nullable
	List<T> getData();

	@NonNull
	IComboBox<T> data(@Nullable List<T> list);

	@NonNull
	IComboBox<T> query(@Nullable QCriteria<T> criteria);

	@NonNull
	IComboBox<T> renderer(@NonNull IRenderInto<T> renderer);
}
