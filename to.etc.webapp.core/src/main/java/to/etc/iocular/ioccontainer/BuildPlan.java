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
package to.etc.iocular.ioccontainer;

import java.io.*;

import to.etc.iocular.def.*;
import to.etc.util.*;

/**
 * Encapsulates a method of building an instance of a given object
 * from a given container.
 *
 * @author jal
 * Created on Mar 28, 2007
 */
public interface BuildPlan {
	ComponentRef[] EMPTY_PLANS = new ComponentRef[0];

	Object getObject(BasicContainer c) throws Exception;

	void dump(IndentWriter iw) throws IOException;

	/**
	 * When T this component has a static (one-time only) initialization requirement.
	 * @return
	 */
	boolean needsStaticInitialization();

	/**
	 * When this has a static initializer this should execute it. This gets called before an actual object
	 * is created from this definition.
	 * @param c
	 * @throws Exception
	 */
	void staticStart(BasicContainer c) throws Exception;

	/**
	 * Call the after-construction methods specified for this object (start methods). When present these are
	 * called after construction of the object, with the instance of the object as a possible parameter.
	 * @param bc
	 * @param self
	 * @throws Exception
	 */
	void start(BasicContainer bc, Object self) throws Exception;

	/**
	 * Call the before-destruction methods specified for this object.
	 * @param bc
	 * @param self
	 * @throws Exception
	 */
	void destroy(BasicContainer bc, Object self);

	boolean hasDestructors();
}
