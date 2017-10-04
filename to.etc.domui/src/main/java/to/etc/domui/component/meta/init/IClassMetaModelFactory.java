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
package to.etc.domui.component.meta.init;

import to.etc.domui.component.meta.ClassMetaModel;

import javax.annotation.Nonnull;

/**
 * This is responsible for creating the initial structure for a metamodel: the
 * ClassMetaModel implementation and its initial population of
 * PropertyMetaModels. It can use only the methods inside MetaInitializer, and
 * must be able to properly handle the ClassMetaIncompleteException by adding a
 * later task.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 16, 2008
 */
public interface IClassMetaModelFactory {
	/**
	 * Must return a value &gt; 0 when this knows how to create a metamodel for the specified thingerydoo.
	 *
	 * @param theThingy
	 * @return
	 */
	int accepts(@Nonnull Object theThingy);

	/**
	 * When accept() has returned a &gt; 0 value, this <i>must</i> create a metamodel for
	 * the thingy passed.
	 *
	 * @param theThingy
	 * @return
	 */
	@Nonnull
	ClassMetaModel createModel(@Nonnull MetaInitContext context, @Nonnull Object theThingy);
}
