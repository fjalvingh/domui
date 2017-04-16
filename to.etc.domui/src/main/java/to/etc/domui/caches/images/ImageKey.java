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
package to.etc.domui.caches.images;

import javax.annotation.*;

import to.etc.domui.util.images.*;

/**
 * Represents the full key to some original image source. It consists of the <i>retriever</i> which is used to
 * instantiate images, and the key used by that retriever to identify the instance.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 30, 2009
 */
final public class ImageKey {
	private IImageRetriever m_retriever;

	private String m_instanceKey;

	public ImageKey(@Nonnull IImageRetriever retriever, @Nonnull String instanceKey) {
		if(retriever == null || instanceKey == null)
			throw new IllegalArgumentException("Parameters cannot be null");
		m_retriever = retriever;
		m_instanceKey = instanceKey;

		//-- Make sure instanceKey is valid..
		for(int i = instanceKey.length(); --i >= 0;) {
			char c = instanceKey.charAt(i);
			if(!Character.isLetterOrDigit(c) && c != '$' && c != '_' && c != '-')
				throw new IllegalArgumentException("The image key '" + instanceKey + "' contains invalid character(s): " + c + ". Allowed are letters, digits, -$_");
		}
	}

	@Nonnull
	public IImageRetriever getRetriever() {
		return m_retriever;
	}

	@Nonnull
	public String getInstanceKey() {
		return m_instanceKey;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + m_instanceKey.hashCode();
		result = prime * result + m_retriever.getClass().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		ImageKey o = (ImageKey) obj;
		if(!m_instanceKey.equals(o.m_instanceKey))
			return false;
		return m_retriever == o.m_retriever;
	}
}
