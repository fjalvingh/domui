package to.etc.domui.component.tbl;

import javax.annotation.*;

/**
 * When defined in {@link InstanceSelectionModel}, this can accept or refuse items that are selected.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 18, 2012
 */
public interface IAcceptable<T> {
	/**
	 * Return T to allow an instance to become selected.
	 * @param value
	 * @return
	 */
	boolean acceptable(@Nonnull T value);
}