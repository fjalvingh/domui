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

import java.io.*;

/**
 * This is a resource reference to something on the classpath used only in debug mode. This
 * version allows reloading of classpath resources when they change while the server is
 * running.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 15, 2010
 */
public class ReloadingClassResourceRef implements IResourceRef, IModifyableResource {
	/** When running in debug mode AND if a source for this resource can be found- this contains a ref to it. */
	private IModifyableResource m_source;

	private Class< ? > m_base;

	private String m_name;

	/**
	 * Create a root-based class resource ref.
	 * @param mr
	 * @param name
	 */
	public ReloadingClassResourceRef(IModifyableResource mr, String name) {
		m_source = mr;
		m_base = ReloadingClassResourceRef.class;
		if(!name.startsWith("/"))
			throw new IllegalStateException("The root-based resource reference " + name + " must start with a '/'");
		m_name = name;
	}

	//	public ClassResourceRef(Class< ? > base, String name) {
	//		m_base = base;
	//		m_name = name;
	//	}

	/**
	 * This is a funny one.... When a class resource is loaded from a .jar file Java will cache that data. This means that even
	 * though the underlying .jar file has changed the resource as read originally will be returned, defeating the purpose of
	 * DomUI debug mode. To fix this we need to ensure that a //different// instance is returned every time the class is accessed...
	 */
	@Override
	public InputStream getInputStream() throws Exception {
		if(m_source == null || !(m_source instanceof JarredFileRef))
			return m_base.getResourceAsStream(m_name);

		//-- This is a JAR reference: ask it to return the resource to prevent URL caching in the JDK
		JarredFileRef jref = (JarredFileRef) m_source;
		return jref.getResource();
	}

	@Override
	public boolean exists() {
		return getLastModified() != -1;
	}

	/**
	 * @see to.etc.domui.util.resources.IModifyableResource#getLastModified()
	 */
	@Override
	public long getLastModified() {
		return m_source == null ? -1 : m_source.getLastModified();
	}

	@Override
	public String toString() {
		return "ClassResourceRef[" + m_name + "]";
	}
}
