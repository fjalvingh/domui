package to.etc.domui.component.layout;

import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

	void updateLabel(@Nonnull String label, @Nullable String image);

	void updateLabel(@Nonnull NodeBase label, @Nullable String image);

	void updateContent(@Nonnull NodeContainer content);
}
