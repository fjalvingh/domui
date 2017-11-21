package to.etc.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

final public class Diff<T> {
	static public boolean	DEBUG	= false;

	static public boolean	DEBUG2	= false;

	public enum Type {
		ADD, DELETE, SAME
	}

	final private int		m_startIndex;

	final private int		m_endIndex;

	@Nonnull
	final private List<T>	m_list;

	@Nonnull
	final private Type		m_type;

	private Diff(int startIndex, int endIndex, @Nonnull List<T> list, @Nonnull Type type) {
		m_startIndex = startIndex;
		m_endIndex = endIndex;
		m_list = list;
		m_type = type;
	}

	public Type getType() {
		return m_type;
	}

	public int getEndIndex() {
		return m_endIndex;
	}

	public int getStartIndex() {
		return m_startIndex;
	}

	@Nonnull
	public List<T> getList() {
		return m_list;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		char c;
		switch(getType()){
			default:
				throw new IllegalStateException();
			case ADD:
				c = '+';
				break;
			case DELETE:
				c = '-';
				break;
			case SAME:
				c = ' ';
				break;
		}

		sb.append(c).append(" @").append(m_startIndex).append(",").append(m_endIndex).append(" ").append("\n");
		for(T item: m_list) {
			sb.append(c).append(" ").append(item).append("\n");
		}

		return sb.toString();
	}


	private static class Item<T> {
		private int		m_index;

		final private Type	m_type;

		final private T		m_item;

		public Item(Type type, T item) {
			m_type = type;
			m_item = item;
		}

		public int getIndex() {
			return m_index;
		}

		public void setIndex(int index) {
			m_index = index;
		}

		public Type getType() {
			return m_type;
		}

		public T getItem() {
			return m_item;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			switch(getType()){
				default:
					throw new IllegalStateException(getType() + "?");

				case ADD:
					sb.append("+");
					break;

				case DELETE:
					sb.append("-");
					break;

				case SAME:
					sb.append(" ");
					break;
			}
			sb.append(" ").append(getItem()).append(" @").append(m_index);
			return sb.toString();
		}
	}

	static public <I> List<Diff<I>> diffList(@Nonnull List<I> oldl, @Nonnull List<I> newl, @Nullable Comparator<I> comparator) {
		return diffList(oldl, newl, comparator, true);
	}

	static public <I> List<Diff<I>> diffList(@Nonnull List<I> oldl, @Nonnull List<I> newl, @Nullable Comparator<I> comparator, boolean skipsame) {
		if(null == comparator) {
			comparator = new DiffComparator<>();
		}

		//-- First slice off common start and end;
		int oend = oldl.size();
		int nend = newl.size();
		int obeg = 0;
		int nbeg = 0;

		//-- Slice common beginning
		while(obeg < oend && nbeg < nend) {
			I so = oldl.get(obeg);
			I co = newl.get(nbeg);
			if(0 != comparator.compare(so, co)) {
				break;
			}
			obeg++;
			nbeg++;
		}

		//-- Slice common end
		while(oend > obeg && nend > nbeg) {
			I so = oldl.get(oend - 1);
			I co = newl.get(nend - 1);
			if(0 != comparator.compare(so, co)) {
				break;
			}
			nend--;
			oend--;
		}
		if(obeg >= oend && nbeg >= nend) {
			//-- Equal arrays- no changes.
			return Collections.EMPTY_LIST;
		}

		//-- Ouf.. We need to do the hard bits. Find the lcs and then render the edit as the delta.
		int m = (oend - obeg) + 1;
		int n = (nend - nbeg) + 1;
		int[][] car = new int[m][];
		for(int i = 0; i < m; i++) {
			car[i] = new int[n];
			car[i][0] = 0;
		}
		for(int i = 0; i < n; i++) {
			car[0][i] = 0;
		}

		for(int i = 1; i < m; i++) {
			for(int j = 1; j < n; j++) {
				I so = oldl.get(obeg + i - 1);
				I co = newl.get(nbeg + j - 1);
				if(0 == comparator.compare(so, co)) {
					car[i][j] = car[i - 1][j - 1] + 1;					// Is length of previous subsequence + 1.
				} else {
					car[i][j] = Math.max(car[i][j - 1], car[i - 1][j]);	// Is length of the so-far longest subsequence
				}
			}
		}

		//-- Now: backtrack from the end to the start to render the delta. This creates the delta in the "reverse" order.
		List<Item<I>> res = new ArrayList<Item<I>>();
		List<String> tmp = new ArrayList<String>();
		for(int xxx = oldl.size(); --xxx >= oend;) {
			Item<I> e = new Item<I>(Type.SAME, oldl.get(xxx));
			res.add(e);
			e.setIndex(xxx);
			//if(DEBUG2)
			//	tmp.add("  " + oldl.get(xxx) + " @" + xxx + " (e)");
		}

		int i = m - 1;
		int j = n - 1;
		while(j > 0 || i > 0) {
			if(i > 0 && j > 0 && 0 == comparator.compare(oldl.get(obeg + i - 1), newl.get(nbeg + j - 1))) {
				int sindex = (obeg + i - 1);
				//if(DEBUG2)
				//	tmp.add("  " + oldl.get(sindex) + " @" + sindex);
				res.add(new Item<I>(Type.SAME, oldl.get(sindex)));
				i--;
				j--;

				//-- part of lcs - no delta
			} else if(j > 0 && (i == 0 || car[i][j - 1] >= car[i - 1][j])) {
				//-- Addition
				int nindex = nbeg + j - 1;
				I nitem = newl.get(nindex);

				//if(DEBUG2)
				//	tmp.add("+ " + nitem + " @" + nindex);
				res.add(new Item<I>(Type.ADD, nitem));
				j--;
			} else if(i > 0 && (j == 0 || car[i][j - 1] < car[i - 1][j])) {
				//-- Deletion
				int oindex = obeg + i - 1;
				I oitem = oldl.get(oindex);
				//if(DEBUG2)
				//	tmp.add("- " + oitem + " @" + (oindex));
				res.add(new Item<I>(Type.DELETE, oitem));
				i--;
			}
		}

		//-- Add all unhandled @ start,
		for(i = obeg; --i >= 0;) {
			//if(DEBUG2)
			//	tmp.add("  " + oldl.get(i) + " @" + i + " (s)");
			res.add(new Item<I>(Type.SAME, oldl.get(i)));
		}
		Collections.reverse(tmp);
		Collections.reverse(res);

		//-- Calculate line #s.
		List<Diff<I>> dres = new ArrayList<Diff<I>>();
		int oindex = 0;
		int nindex = 0;
		int lastoindex = 0;
		int lastnindex = 0;
		Type currchange = Type.SAME;

		for(Item<I> item : res) {
			item.setIndex(oindex);

			//-- Is our type changing?
			if(currchange != item.getType()) {
				if(currchange != Type.SAME || !skipsame)
					addDiffItem(oldl, newl, oindex, dres, nindex, lastoindex, lastnindex, currchange);
				currchange = item.getType();
				lastoindex = oindex;
				lastnindex = nindex;
			}

			switch(item.getType()){
				default:
					throw new IllegalStateException(item.getType() + "?");
				case ADD:
					nindex++;
					break;
				case DELETE:
					oindex++;
					break;
				case SAME:
					oindex++;
					nindex++;
					break;
			}
		}

		if(currchange != Type.SAME || !skipsame)
			addDiffItem(oldl, newl, oindex, dres, nindex, lastoindex, lastnindex, currchange);
		//if(DEBUG) {
		//	for(Item<I> s : res) {
		//		System.out.println(" " + s);
		//	}
		//}
		//
		//if(DEBUG) {
		//	System.out.println("Diff: delta:");
		//	for(Diff<I> d : dres) {
		//		System.out.print(d);
		//	}
		//}
		//if(DEBUG2) {
		//	System.out.println("Debug list:");
		//	for(String s : tmp)
		//		System.out.println(" " + s);
		//}

		return dres;
	}


	private static <I> void addDiffItem(List<I> oldl, List<I> newl, int oindex, List<Diff<I>> dres, int nindex, int lastoindex, int lastnindex, Type type) {
		if(lastoindex != oindex || lastnindex != nindex) {
			switch(type){
				default:
					throw new IllegalStateException(type + "?");
				case ADD:
					dres.add(new Diff<I>(lastoindex, oindex, newl.subList(lastnindex, nindex), Type.ADD));
					break;
				case DELETE:
					dres.add(new Diff<I>(lastoindex, oindex, oldl.subList(lastoindex, oindex), Type.DELETE));
					break;
				case SAME:
					dres.add(new Diff<I>(lastoindex, oindex, oldl.subList(lastoindex, oindex), Type.SAME));
					break;
			}
		}
	}


	public static void main(String[] args) throws Exception {
		List<String> a = Arrays.asList("A", "B", "B", "A", "D", "E", "A", "D");
		List<String> b = Arrays.asList("B", "A", "A", "D", "E", "A", "D");
		Comparator<String> cs = String::compareTo;

		// abbadead: diff is -a @0 -b @1 +A @2
		// 01234567
		//  bbadead (-a @0)
		//   badead (-b @1)
		//

		diffList(a, b, cs);


	}

	private static class DiffComparator<I> implements Comparator<I> {
		@Override
		public int compare(I o, I n) {
			if(o == n)
				return 0;
			if(o == null)
				return 1;
			if(n == null)
				return -1;
			if(o instanceof Comparable) {
				Comparable<I> c = (Comparable<I>) o;
				return c.compareTo(n);
			}
			return o.toString().compareTo(n.toString());
		}
	}
}
