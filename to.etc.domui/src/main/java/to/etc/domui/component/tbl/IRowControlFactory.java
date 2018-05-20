package to.etc.domui.component.tbl;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.html.IControl;

/**
 * Create a control, possibly influenced by a PropertyMetaModel for the control's controlling property.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 3, 2014
 */
public interface IRowControlFactory<R> {
	@NonNull
	IControl<?> createControl(@NonNull R rowInstance) throws Exception;
}
