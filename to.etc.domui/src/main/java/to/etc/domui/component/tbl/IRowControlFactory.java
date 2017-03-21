package to.etc.domui.component.tbl;

import javax.annotation.*;

import to.etc.domui.dom.html.*;

/**
 * Create a control, possibly influenced by a PropertyMetaModel for the control's controlling property.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 3, 2014
 */
public interface IRowControlFactory<R> {
	@Nonnull
	IControl<?> createControl(@Nonnull R rowInstance) throws Exception;
}
