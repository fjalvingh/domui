package to.etc.util;

import javax.annotation.*;

/**
 * Listens for Progress changes at a given level.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 19, 2011
 */
public interface IProgressListener {
	void progressed(@Nonnull Progress level) throws Exception;
}
