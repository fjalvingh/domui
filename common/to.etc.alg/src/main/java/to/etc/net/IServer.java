package to.etc.net;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-12-17.
 */
public interface IServer {
	String getPassword();

	@Nullable
	String getHostname();

	@NonNull
	String getDisplayName();

	String getLoginId();

	String getSshKey();

	default int getPortNumber() {
		return 22;
	}
}
