package to.etc.domui.component.layout;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;

/**
 * Handle to a tab panel, with methods to change it.
 *
 * @author <a href="mailto:marc.mol@itris.nl">Marc Mol</a>
 * @since Dec 5, 2014
 */
public interface ITabHandle {
	/** Close this tab. This tab is unusable after. */
	void close() throws Exception;

	/** Set this tab as selected */
	void select() throws Exception;

	void updateLabel(@NonNull String label, @Nullable String image);

	void updateLabel(@NonNull NodeBase label, @Nullable String image);

	void updateContent(@NonNull NodeContainer content);
}
