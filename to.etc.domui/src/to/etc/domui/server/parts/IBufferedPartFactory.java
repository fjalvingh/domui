package to.etc.domui.server.parts;

import javax.annotation.*;

import to.etc.domui.server.*;
import to.etc.domui.util.resources.*;

public interface IBufferedPartFactory extends IPartFactory {
	/**
	 * Decode the input and create a KEY for the request. This key must be hashable, and forms
	 * the key for the cache to retrieve an already generated copy.
	 *
	 * @param ctx
	 * @param rurl
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	public Object decodeKey(@Nonnull String rurl, @Nonnull IExtendedParameterInfo param) throws Exception;

	/**
	 * This must generate the output for the resource. That output will be put into the cache and re-rendered
	 * when the same resource is used <i>without</i> calling this method again.
	 *
	 * @param os		The stream to write the data to.
	 * @param da		The Application on behalf of which this resource is generated.
	 * @param key		The key, as specified by decodeKey.
	 * @param rdl		When running in development mode, each file resource used should be added
	 * 					to this list. The buffer code will use that list to check whether a source
	 * 					for this thing has changed; if so it will be re-generated. This causes runtime
	 * 					editability for parameter files of any buffered thingydoo.
	 * @return
	 * @throws Exception
	 */
	public void generate(@Nonnull PartResponse pr, @Nonnull DomApplication da, @Nonnull Object key, @Nonnull ResourceDependencyList rdl) throws Exception;
}
