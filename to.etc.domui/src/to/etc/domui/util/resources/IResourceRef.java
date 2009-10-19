package to.etc.domui.util.resources;

import java.io.*;

/**
 * A reference to some stream resource which can be read to create something else, and which is
 * changeable somehow. This gets used where generated resources need to be regenerated if one of
 * their dependencies have changed.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 19, 2009
 */
public interface IResourceRef extends IModifyableResource {
	public InputStream getInputStream() throws Exception;
}
