package to.etc.domui.util.resources;

/**
 * This refers to some resource that can return it's own "last changed" timestamp. It is used
 * in dependency lists for a generated resource where if a dependency changes the generated
 * data needs to be regenerated.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 19, 2009
 */
public interface IModifyableResource {
	/**
	 * Return the <i>current</i> last modification time. This <b>must</b> return the ACTUAL modification time of the resource; the time
	 * returned by this call will be compared with the time that the resource was last used (stored somewhere else) to decide if this
	 * resource has changed in the meantime.
	 * This call <b>must</i> return -1 for a resource that does not exist - because non-existence is a valid caching criteria too!
	 * @return
	 */
	long getLastModified();
}
