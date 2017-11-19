package to.etc.domui.util.resources;

import javax.annotation.*;

/**
 * Collects dependencies.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 15, 2011
 */
public interface IResourceDependencyList {
	/**
	 * Add a resource to the dependency list. The resource should either implement {@link IIsModified}
	 * or {@link IModifyableResource}, or this will throw an IllegalArgmentException. If the ref
	 * implements {@link IModifyableResource} then it will be wrapped in a {@link ResourceTimestamp}
	 * instance which records the current modification time and implements {@link IIsModified}.
	 *
	 * @param ref
	 */
	void add(@Nonnull IResourceRef ref);

	/**
	 * Add a IIsModified instance.
	 * @param m
	 */
	void add(@Nonnull IIsModified m);

	/**
	 * Add an {@link IModifyableResource} instance.
	 * @param m
	 */
	void add(@Nonnull IModifyableResource c);

	//	/**
	//	 * Add another list of resources to this one.
	//	 * @param c
	//	 */
	//	public abstract void add(@Nonnull ResourceDependencyList c);
}
