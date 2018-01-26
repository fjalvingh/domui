/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.server.parts;

import javax.annotation.*;

import to.etc.domui.server.*;
import to.etc.domui.util.resources.*;

public interface IBufferedPartFactory<K> extends IPartFactory {
	/**
	 * Decode the input and create a KEY for the request. This key must be hashable, and forms
	 * the key for the cache to retrieve an already generated copy.
	 *
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	K decodeKey(@Nonnull DomApplication application, @Nonnull IExtendedParameterInfo param) throws Exception;

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
	void generate(@Nonnull PartResponse pr, @Nonnull DomApplication da, @Nonnull K key, @Nonnull IResourceDependencyList rdl) throws Exception;
}
