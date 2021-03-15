package to.etc.domui.spi;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.NodeBase;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-02-21.
 */
public interface ISpiShelvedEntry {
	@Nullable
	String getName();

	@Nullable
	String getTitle();


	@Nullable
	default NodeBase getIcon() {
		return null;
	}

	/**
	 * Will be called when the entry gets discarded from the shelf.
	 */
	void discard();

	/**
	 * Will be called by the container when this item needs to present itself
	 * in the UI.
	 */
	void activate(@NonNull SpiContainer container) throws Exception;

	/**
	 * Called by breadcrumb components to tell "this needs to become the current thingy".
	 */
	void select() throws Exception;
}
