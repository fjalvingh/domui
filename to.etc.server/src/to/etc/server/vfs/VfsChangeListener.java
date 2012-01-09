package to.etc.server.vfs;

/**
 * A listener for VFS change events. These can be registered both globally (using the
 * VFS object) or per provider.
 *
 * @author jal
 * Created on Dec 5, 2005
 */
public interface VfsChangeListener {
	public void vfsResourceChanged(VfsChangeEvent e) throws Exception;
}
