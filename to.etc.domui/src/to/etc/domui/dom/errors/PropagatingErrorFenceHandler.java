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
package to.etc.domui.dom.errors;

import javax.annotation.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * Use this error fence handler in case when some UIMessage should be handled by more than one error fence.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 23 Sep 2009
 */
public class PropagatingErrorFenceHandler extends ErrorFenceHandler {

	public PropagatingErrorFenceHandler(NodeContainer container) {
		super(container);
	}

	@Override
	public void addMessage(@Nonnull UIMessage uim) {
		super.addMessage(uim);
		NodeContainer propagationContainer = (getContainer() != null && getContainer().hasParent()) ? getContainer().getParent() : null;
		if(propagationContainer != null) {
			IErrorFence fence = DomUtil.getMessageFence(propagationContainer);
			if(fence != this) {
				fence.addMessage(uim);
			}
		}
	}

	@Override
	public void removeMessage(@Nonnull UIMessage uim) {
		super.removeMessage(uim);
		NodeContainer propagationContainer = (getContainer() != null && getContainer().hasParent()) ? getContainer().getParent() : null;
		if(propagationContainer != null) {
			IErrorFence fence = DomUtil.getMessageFence(propagationContainer);
			if(fence != this) {
				fence.removeMessage(uim);
			}
		}
	}

}
