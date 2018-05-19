package to.etc.util;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Listens for Progress changes at a given level.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 19, 2011
 */
public interface IProgressListener {
	void progressed(@NonNull Progress level) throws Exception;
}
