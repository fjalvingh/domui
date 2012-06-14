package to.etc.domuidemo.pages.overview.tree;

import java.io.*;
import java.util.*;

import to.etc.domui.component.tree.*;

/**
 * This implements a lazily-loaded file system tree model. It returns the content of the file
 * system as a tree.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 18, 2010
 */
public class DemoTreeModel implements ITreeModel<File> {
	private class Link {
		private File		m_base;
		private Link[]		m_children;
		public Link(File base) {
			m_base = base;
		}
		public Link[] children() {
			if(m_children == null) {
				File[]	ar = m_base.listFiles();
				if(ar == null) {
					m_children = new Link[0];
				} else {
					Arrays.sort(ar, new Comparator<File>() {
						@Override
						public int compare(File o1, File o2) {
							if(o1.isDirectory() == o2.isDirectory()) {
								return o1.getName().compareTo(o2.getName());
							}
							return o1.isDirectory() ? -1 : 1;
						}
					});

					m_children = new Link[ar.length];
					for(int i = ar.length; --i >= 0;) {
						m_children[i] = new Link(ar[i]);
						m_linkMap.put(ar[i], m_children[i]);
					}
				}
			}
			return m_children;
		}
		public File getBase() {
			return m_base;
		}
	}

	private Link		m_root;

	Map<File, Link>		m_linkMap = new HashMap<File, Link>();

	public DemoTreeModel(File root) {
		m_root = new Link(root);
		m_linkMap.put(root, m_root);
	}

	private Link	getLink(File f) {
		Link l = m_linkMap.get(f);
		if(l == null)
			throw new IllegalStateException("File not located in link thing");
		return l;
	}

	@Override
	public void addChangeListener(ITreeModelChangedListener l) {
	}

	@Override
	public File getChild(File parent, int index) throws Exception {
//		System.out.println("Entered getChild");
//		Link	l = getLink(parent);
//		System.out.println(">> link = "+l);
//		Link[]	children = l.children();
//		System.out.println("Children="+children);
//		l	= children[index];
//		System.out.println("child link="+l);
//		return l.getBase();
		return getLink(parent).children()[index].getBase();
	}
	@Override
	public File getParent(File child) throws Exception {
		if(child == m_root.getBase())
			return null;
		return getLink(child.getParentFile()).getBase();
	}

	@Override
	public int getChildCount(File item) throws Exception {
		return getLink(item).children().length;
	}

	@Override
	public File getRoot() throws Exception {
		return m_root.getBase();
	}

	@Override
	public boolean hasChildren(File item) throws Exception {
		return getChildCount(item) != 0;
	}

	@Override
	public void removeChangeListener(ITreeModelChangedListener l) {
	}

	@Override
	public void collapseChildren(File item) throws Exception {
	// TODO Auto-generated method stub

	}

	@Override
	public void expandChildren(File item) throws Exception {
	// TODO Auto-generated method stub

	}
}
