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

import javax.annotation.*;

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
	final private List<Progress> m_subProgress = new ArrayList<Progress>();

	/** The amount of work in the parent(!) progress' units. */
	private double m_parentsWorkForSub;

	/** The total amount of work in *this* thing. */
	private double m_totalWork;

	/** The amount of work done. */
	private double m_currentWork;

	private double m_workReportedToParent;

	/** T if a cancel request is received. */
	private boolean m_cancelled;

	private boolean m_parallel;

	/** If F, then this cannot be cancelled, and any attempt to do that is ignored. */
	private boolean					m_cancelable	= true;

	/** A short-as-possible name for the current activity */
	@Nullable
	private String m_name;

	@Nullable
	private String					m_extra;

	@Nonnull
	private List<IProgressListener>	m_listeners	= Collections.emptyList();

	static public class Info {
		final private String m_path;

		final private int m_percentage;

		public Info(String path, int percentage) {
			m_path = path;
			m_percentage = percentage;
		}

		public String getPath() {
			return m_path;
		}

		public int getPercentage() {
			return m_percentage;
		}
	}


	/**
	 * Top-level progress indicator for a given task.
	 */
	public Progress(@Nullable String name) {
		m_root = this;
		m_name = name;
	}

	private Progress(@Nonnull Progress parent, @Nullable String name) {
		m_parent = parent;
		m_root = parent.m_root;
		m_name = name;
	}

	/**
	 * Return all parallel-running progress parts, starting with the root one.
	 * @param level
	 * @return
	 */
	@Nonnull
	public List<Info> getParallels(int level) {
		List<Info> prl = new ArrayList<Info>();
		synchronized(m_root) {
			if(m_parent != null)
				throw new IllegalStateException("Only callable on root entity");

			StringBuilder sb = new StringBuilder();
			Progress split = findSplitPoint(sb, this, level);

			//-- Got 1st split point. Add this-items's progress
			prl.add(new Info(sb.toString(), getPercentage()));

			if(split != null) {
				sb.setLength(0);
				handleSplit(sb, prl, split);
			}
		}
		return prl;
	}

	private void handleSplit(@Nonnull StringBuilder sb, @Nonnull List<Info> prl, @Nonnull Progress split) {
		int len = sb.length();

		for(Progress sub : split.m_subProgress) {
			sb.setLength(len);
			Progress nsplit = findSplitPoint(sb, sub, 3);
			if(nsplit == null) {
				prl.add(new Info(sb.toString(), sub.getPercentage()));
			} else {
				handleSplit(sb, prl, nsplit);
			}
		}
		sb.setLength(len);
	}

	private Progress findSplitPoint(@Nonnull StringBuilder sb, @Nonnull Progress progress, int level) {
		while(progress != null) {
			String name = progress.m_name;
			if(name != null) {
				if(sb.length() > 0)
					sb.append('>');
				sb.append(name);
				name = progress.m_extra;
				if(name != null)
					sb.append(' ').append(name);
			}

			if(--level == 0)
				return null;
			if(m_subProgress.size() == 0) {
				return null;
			} else if(m_subProgress.size() == 1) {
				progress = m_subProgress.get(0);
			} else {
				return progress;
			}
		}
		return null;
	}

	/**
	 * Return the root progress handler; this NEVER returns null - it returns itself if it is the root.
	 * @return
	 */
	@Nonnull
	public Progress getRoot() {
		return m_root;
	}

	@Nullable
	public Progress getParent() {
		return m_parent;
	}

	public boolean isCancelable() {
		return getRoot().m_cancelable;
	}

	@Nullable
	public String getName() {
		synchronized(m_root) {
			if(m_extra == null)
				return m_name;
			return m_name + m_extra;
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

	/**
	 * Get the path until the first parallel subprocess or the specified level
	 */
	@Nonnull
	public String getActionPath(int levels) {
		StringBuilder sb = new StringBuilder(80);

		synchronized(m_root) {
			Progress p = this;
			while(p != null && levels > 0) {
				if(p.m_name != null) {
					if(sb.length() != 0)
						sb.append(">");
					sb.append(p.m_name);
					if(p.m_extra != null)
						sb.append(p.m_extra);
					levels--;
				}
				if(p.m_subProgress.size() != 1)
					p = null;
				else
					p = p.m_subProgress.get(0);
			}
		}
		return sb.toString();
	}

	/**
	 * Request the action to be cancelled.
	 */
	public void	cancel() {
		synchronized(m_root) {
			if(!m_cancelled && m_root.m_cancelable) {
				m_cancelled = true;
				if(m_parent != null)
					m_parent.cancel();		// Pass upwards.
				updateTree();
			}
		}
	}

	public void setCancelable(boolean yes) {
		synchronized(m_root) {
			m_root.m_cancelable = yes;
		}
	}

	public boolean isCancelled() {
		synchronized(m_root) {
			return m_root.m_cancelled;		// Do not use getter, please 8-(
		}
	}

	/**
	 * Set the current amount of work.
	 * @param work
	 * @param name
	 */
	public void setTotalWork(double work) {
		setTotalWork(work, null);
	}

	/**
	 * Set the current amount of work.
	 * @param work
	 * @param extra
	 */
	public void setTotalWork(double work, @Nullable String extra) {
		synchronized(m_root) {
			checkCancelled();
			if(m_totalWork != 0) {
				m_currentWork = 0;
			}
			m_totalWork = work;
			m_extra = extra;
		}
	}

	private void checkCancelled() {
		synchronized(m_root) {
			if(m_root.m_cancelled && m_root.m_cancelable)
				throw new CancelledException();
		}
	}

	/**
	 * Set the amount of work completed. It can only be set to a valid value, and it can
	 * only advance, not go back.
	 * @param now
	 */
	public void setCompleted(double now) {
		checkCancelled();
		setCompleted(now, null);
	}

	/**
	 * Set the amount of work completed. It can only be set to a valid value, and it can
	 * only advance, not go back.
	 * @param now
	 */
	private void internalSetCompleted(double now) {
		synchronized(m_root) {
			checkCancelled();
			if(now <= m_currentWork)
				return;
			if(now >= m_totalWork)
				now = m_totalWork;
			m_currentWork = now;

			//-- If I have a parent: increment it's work done.
			Progress dad = m_parent;
			if(null != dad) {
				double parentwork = getFraction() * m_parentsWorkForSub;		// Amount of work done if parent's units'
				double toreport = parentwork - m_workReportedToParent;
				if(toreport > 0) {
					double dadwork = dad.m_currentWork + toreport;
					dad.internalSetCompleted(dadwork);
					m_workReportedToParent = parentwork;
				}
			}
			updated();
			updateTree();
		}
	}

	public synchronized void setCompleted(double now, @Nullable String extra) {
		m_extra = extra;
		internalSetCompleted(now);
	}

	public void complete() {
		synchronized(m_root) {
			clearSubProgress();
			if(m_totalWork == 0) {
				m_totalWork = 100.0;
				m_currentWork = 100.0;
			} else if(m_currentWork < m_totalWork) {
				m_currentWork = m_totalWork;
			}

			//-- If I am part of a parallel: clear myself from my parent and update parent progress
			Progress dad = m_parent;
			if(null != dad) {
				if(dad.m_subProgress.remove(this)) {			// Remove from my parent as I'm done
					double toreport = m_parentsWorkForSub - m_workReportedToParent;
					if(toreport > 0) {
						dad.internalSetCompleted(m_parent.m_currentWork + toreport);
					}
				}
			}

			updateTree();
		}
	}

	public void increment(double inc) {
		checkCancelled();
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
			m_parallel = false;
			checkCancelled();
			if(m_subProgress.size() == 0)
				return;

			double totalWork = 0.0;
			while(m_subProgress.size() > 0)
				m_subProgress.get(0).complete();
			updateTree();
		}
	}

	/**
	 * Create a single (non-parallel) sub-progress indicator for the specified portion of work.
	 * @return
	 */
	@Nonnull
	public Progress createSubProgress(@Nullable String name, double work) {
		synchronized(m_root) {
			clearSubProgress();
			checkCancelled();
			if(m_currentWork + work > m_totalWork) { 			// Truncate if amount == too big
				work = m_totalWork - m_currentWork;				// How much is possible?
				if(work < 0) 									// If we think we're already done- just ignore..
					work = 0;
			}
			Progress sub = new Progress(this, name);
			sub.m_parentsWorkForSub = work;						// The max amount of work this subprocess can complete, in our units.
			m_subProgress.add(sub);
			return sub;
		}
	}

	@Nonnull
	public Progress createParallelProgress(@Nullable String name, double work) {
		checkCancelled();
		if(!m_parallel) {
			clearSubProgress();
			m_parallel = true;
		}

		if(m_currentWork + work > m_totalWork) { 				// Truncate if amount == too big
			work = m_totalWork - m_currentWork; 				// How much is possible?
			if(work < 0) 										// If we think we're already done- just ignore..
				work = 0;
		}

		Progress sub = new Progress(this, name);
		sub.m_parentsWorkForSub = work;							// The max amount of work this subprocess can complete, in our units.
		m_subProgress.add(sub);
		return sub;
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
	}

	protected void updated() {

	}

	public synchronized void addListener(@Nonnull IProgressListener l) {
		m_listeners = new ArrayList<IProgressListener>(m_listeners);
		m_listeners.add(l);
	}

	public synchronized void removeListener(@Nonnull IProgressListener l) {
		m_listeners = new ArrayList<IProgressListener>(m_listeners);
		m_listeners.remove(l);
	}

	@Nonnull
	synchronized private List<IProgressListener> getListeners() {
		return m_listeners;
	}
}
