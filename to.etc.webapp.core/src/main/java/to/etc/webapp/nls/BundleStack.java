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
package to.etc.webapp.nls;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Represents a stack of message bundles that behaves as a single message bundle. Keys are looked up
 * from the top to the bottom, and the 1st matching one is returned.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 31, 2010
 */
@Immutable
public class BundleStack extends BundleBase implements IBundle {
	@Nonnull
	final private NlsMessageProvider[] m_bundleStack;

	public BundleStack(@Nonnull NlsMessageProvider[] bundleStack) {
		m_bundleStack = bundleStack;
	}

	public BundleStack(@Nonnull List< ? extends NlsMessageProvider> reflist) {
		m_bundleStack = reflist.toArray(new NlsMessageProvider[reflist.size()]);
	}

	/**
	 * Create a bundle stack for the specified class. For every class in that class's hierarchy
	 * including the class itself, it tries to find both "classname" and "messages" as bundle
	 * names inside the class' package dir. When found this is added to the list of bundles for
	 * the class. The parent lookup stops at any base class that is part of the "java." or
	 * "javax." hierarchy. It only follows classes; interfaces
	 * are not followed, and it is illegal to pass an interface as the class parameter.
	 * <p>If no bundles are located at all this returns null.</p>
	 * <p>This makes message bundles follow the same hierarchy as the classes itself, and allows
	 * classes that extend other classes to also "extend" the messages for the base class.</p>
	 *
	 * @param clz
	 * @return
	 */
	@Nullable
	static public BundleStack createStack(Class< ? > clz) {
		if(null == clz || clz.isInterface())
			throw new IllegalArgumentException(clz+" invalid - cannot be null or interface");

		Class<?>	cur = clz;
		List<BundleRef>		res = new ArrayList<BundleRef>();
		for(;;) {
			if(cur == null)
				break;
			String pn = cur.getPackage().getName();
			if(pn.startsWith("java.") || pn.startsWith("javax.")) {
				break;
			}

			//-- Consider bundles for this class
			String cn = cur.getName();
			int pos = cn.lastIndexOf('.');
			cn = cn.substring(pos + 1);
			BundleRef br = BundleRef.create(cur, cn);
			if(br.exists())
				res.add(br);

			br = BundleRef.create(clz, "messages");
			if(br.exists())
				res.add(br);

			//-- Walk the package hierarchy upward
			String pkg = clz.getPackage().getName().replace('.', '/');		// Class's package
			for(;;) {
				pos = pkg.lastIndexOf('/');
				if(pos < 0)
					pos = 0;
				pkg = pkg.substring(0, pos);			// Remove last package name

				br = BundleRef.create(clz, "/" + pkg + "/messages");
				if(br.exists())
					res.add(br);
				if(pos == 0)
					break;
			}

			cur = cur.getSuperclass();
		}
		if(res.size() == 0)
			return null;
		return new BundleStack(res);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String findMessage(@Nonnull Locale loc, @Nonnull String code) {
		for(int i = 0; i < m_bundleStack.length; i++) {
			String msg = m_bundleStack[i].findMessage(loc, code);
			if(null != msg)
				return msg;
		}
		return null;
	}
}
