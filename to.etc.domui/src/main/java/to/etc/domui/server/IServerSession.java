package to.etc.domui.server;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Encapsulates HttpSession functionality required by DomUI. The wrapper is used
 * so that testing is possible without HttpSession needing to be implemented.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 9, 2013
 */
public interface IServerSession {
	@NonNull String getId();

	@Nullable Object getAttribute(@NonNull String name);

	void setAttribute(@NonNull String name, @Nullable Object value);

	void invalidate();

	boolean isNew();
}
