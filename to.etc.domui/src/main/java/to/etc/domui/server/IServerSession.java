package to.etc.domui.server;

import javax.annotation.*;

/**
 * Encapsulates HttpSession functionality required by DomUI. The wrapper is used
 * so that testing is possible without HttpSession needing to be implemented.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 9, 2013
 */
public interface IServerSession {
	@Nonnull String getId();

	@Nullable Object getAttribute(@Nonnull String name);

	void setAttribute(@Nonnull String name, @Nullable Object value);

	void invalidate();

	boolean isNew();
}
