package to.etc.domui.util;

import javax.annotation.*;

import to.etc.domui.dom.html.*;

public interface IDragHandler {
	/**
	 * This must return a "type name" for the thing being dragged. This typename gets passed to
	 * any "drop target" and allows that to indicate whether that type is acceptable for that
	 * drop target.
	 * @return a non-null string.
	 */
	public @Nonnull String		getTypeName(@Nonnull NodeBase source);

	/**
	 * Called when the dragged node has been dropped on a DropTarget which has accepted the
	 * node. This should then remove the source to prevent it from being reused.
	 */
	public void			onDropped(DropEvent context)throws Exception;
}
