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
