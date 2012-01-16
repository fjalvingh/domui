/*
 * DomUI Java User Interface - shared code
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
package to.etc.util;

import java.util.*;

/**
 * A progress reporter utility. A single instance defines progress in user-specified
 * numeric terms (for instance record 12 of 1012312), and gets translated to a
 * percentage. Progress instances can obtain sub-progress parts that are Progress
 * entries all by themselves and can define their work in their terms; they get
 * mapped to the defined part of work in their parent(s).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 16, 2009
 */
public class Progress {
	/** The top-level progress entry, */
	private Progress m_root;

	/** The parent of this progress entry */
	private Progress m_parent;

	/** If a subprogress is in action this contains it. */
	private Progress m_subProgress;

	/** The amount of work in this-progress' units. */
	private double m_subTotalWork;

	/** The "current" amount of work units that was set when the sub work started. */
	private double m_subStartWork;

	/** The total amount of work. */
	private double m_totalWork;

	/** The amount of work done. */
	private double m_currentWork;

	/** T if a cancel request is received. */
	private boolean m_cancelled;

	/** A short-as-possible name for the current activity */
	private String m_name;

	private List<IProgressListener>	m_listeners	= Collections.emptyList();

	/**
	 * Top-level progress indicator for a given task.
	 */
	public Progress(String name) {
		m_root = this;
		m_name = name;
	}

	private Progress(Progress parent, String name) {
		m_parent = parent;
		m_root = parent.m_root;
		m_name = name;
	}

	/**
	 * Return the root progress handler; this NEVER returns null - it returns itself if it is the root.
	 * @return
	 */
	public Progress getRoot() {
		return m_root;
	}

	public Progress getParent() {
		return m_parent;
	}

	public String getName() {
		synchronized(m_root) {
			return m_name;
		}
	}

	public double getTotalWork() {
		synchronized(m_root) {
			return m_totalWork;
		}
	}

	public double getCompletedWork() {
		synchronized(m_root) {
			return m_currentWork;
		}
	}

	public int getPercentage() {
		synchronized(m_root) {
			if(m_totalWork <= 0)
				return 0;
			double pct = m_currentWork * 100.0d / m_totalWork;
			return (int) Math.round(pct);
		}
	}

	public double getFraction() {
		synchronized(m_root) {
			if(m_totalWork <= 0)
				return 0.0d;
			return m_currentWork / m_totalWork;
		}
	}

	public String getActionPath(int levels) {
		StringBuilder sb = new StringBuilder(80);

		synchronized(m_root) {
			Progress p = this;
			while(p != null && levels > 0) {
				if(p.m_name != null) {
					if(sb.length() != 0)
						sb.append(">");
					sb.append(p.m_name);
					levels--;
				}
				p = p.m_subProgress;
			}
		}
		return sb.toString();
	}

	/**
	 * Request the action to be cancelled.
	 */
	public void	cancel() {
		synchronized(m_root) {
			if(! m_cancelled) {
				m_cancelled = true;
				if(m_parent != null)
					m_parent.cancel();		// Pass upwards.
				updateTree();
			}
		}
	}

	public boolean isCancelled() {
		synchronized(m_root) {
			return m_root.isCancelled();
		}
	}

	/**
	 * Set the current amount of work.
	 * @param work
	 * @param name
	 */
	public void setTotalWork(double work) {
		synchronized(m_root) {
			if(m_totalWork != 0)
				throw new IllegalStateException("You cannot change the work-to-do after it has been set");
			m_totalWork = work;
			updateTree();
		}
	}

	/**
	 * Set the amount of work completed. It can only be set to a valid value, and it can
	 * only advance, not go back.
	 * @param now
	 */
	public void setCompleted(double now) {
		synchronized(m_root) {
			clearSubProgress();
			if(now <= m_currentWork)
				return;
			if(now >= m_totalWork)
				now = m_totalWork;
			m_currentWork = now;
			updateTree();
		}
	}

	public void complete() {
		synchronized(m_root) {
			clearSubProgress();
			if(m_totalWork == 0) {
				m_totalWork = 100.0;
				m_currentWork = 100.0;
				updateTree();
			} else if(m_currentWork < m_totalWork) {
				m_currentWork = m_totalWork;
				updateTree();
			}
		}
	}

	public void increment(double inc) {
		if(inc <= 0.0d)
			return;
		synchronized(m_root) {
			setCompleted(m_currentWork + inc);
		}
	}

	/**
	 * If a sub-progress indicator is present here complete it, and discard. This
	 * adds the amount of work for the subprogress item to this item (treating it
	 * as completed).
	 */
	private void clearSubProgress() {
		synchronized(m_root) {
			if(m_subProgress == null || m_subProgress.m_parent == null) // Nothing active?
				return;
			m_currentWork = m_subStartWork + m_subTotalWork; // Finish off the sub.
			m_subProgress.m_parent = null;
			m_subProgress = null;
			updateTree();
		}
	}

	/**
	 * Create a sub-progress indicator for the specified portion of work.
	 * @return
	 */
	public Progress createSubProgress(String name, double work) {
		synchronized(m_root) {
			clearSubProgress();
			if(m_currentWork + work > m_totalWork) { // Truncate if amount == too big
				work = m_totalWork - m_currentWork; // How much is possible?
				if(work < 0) // If we think we're already done- just ignore..
					work = 0;
			}
			m_subTotalWork = work; // The max amount of work this subprocess can complete, in our units.
			m_subStartWork = m_currentWork; // Save the base value to be able to do a full recalculate (prevent rounding trouble)

			m_subProgress = new Progress(this, name);
			return m_subProgress;
		}
	}

	/**
	 * Called when a thingy has updated; this passes the change to all parents. The current
	 * instance has already changed; use those values to recalculate values for all parents.
	 */
	private void updateTree() {
		//-- Call all listeners @ this level
		for(IProgressListener l : getListeners()) {
			try {
				l.progressed(this);
			} catch(Exception x) {
				throw WrappedException.wrap(x);		// Bad interfaces: I hate checked exceptions.
			}
		}
		updated();
		synchronized(m_root) {
			Progress p = m_parent;
			while(p != null) {
				if(!p.updateFromSub())
					return;
				p = p.m_parent;
			}
		}
	}

	protected void updated() {

	}

	protected boolean updateFromSub() {
		synchronized(m_root) {
			if(m_subProgress == null)
				throw new IllegalStateException("?? Unexpected: no sub active?");

			//** Calculate the fraction of work done, then adjust
			double frac = m_subProgress.getFraction();
			double amount = frac * m_subTotalWork + m_subStartWork;
			if(m_currentWork >= amount)
				return false;
			if(amount > m_subStartWork+m_subTotalWork)
				throw new IllegalStateException("?? Sub adjustment causes overflow: "+amount+", start="+m_subStartWork+", max-sub="+m_subTotalWork);
			m_currentWork = amount;
			updated();
			return true;
		}
	}

	public synchronized void addListener(IProgressListener l) {
		m_listeners = new ArrayList<IProgressListener>(m_listeners);
		m_listeners.add(l);
	}

	public synchronized void removeListener(IProgressListener l) {
		m_listeners = new ArrayList<IProgressListener>(m_listeners);
		m_listeners.remove(l);
	}

	synchronized private List<IProgressListener> getListeners() {
		return m_listeners;
	}
}
