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
package to.etc.domui.state;

import java.util.*;

import to.etc.domui.component.delayed.*;

public class DelayedActivityState {
	private List<Progress> m_progressList;

	private List<DelayedActivityInfo> m_completionList;

	public static class Progress {
		private AsyncContainer m_container;

		private int m_pctComplete;

		private String m_message;

		protected Progress(AsyncContainer container, int pctComplete, String message) {
			m_container = container;
			m_pctComplete = pctComplete;
			m_message = message;
		}

		public AsyncContainer getContainer() {
			return m_container;
		}

		public int getPctComplete() {
			return m_pctComplete;
		}

		public String getMessage() {
			return m_message;
		}
	}

	protected DelayedActivityState(List<Progress> progressList, List<DelayedActivityInfo> completionList) {
		m_progressList = progressList;
		m_completionList = completionList;
	}

	public List<Progress> getProgressList() {
		return m_progressList;
	}

	public List<DelayedActivityInfo> getCompletionList() {
		return m_completionList;
	}
}
