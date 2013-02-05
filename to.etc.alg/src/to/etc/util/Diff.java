package to.etc.util;

import java.util.*;

final public class Diff<T> {
	private enum Type {
		ADD, DELETE, SAME
	}

	final private int		m_startIndex;

	final private int		m_endIndex;

	final private List<T>	m_list;

	final private Type		m_type;

	private Diff(int startIndex, int endIndex, List<T> list, Type type) {
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

	static public <I> List<Diff<I>> diffList(List<I> sourcel, List<I> copyl, Comparator<I> comparator) throws Exception {
		//-- First slice off common start and end;
		int send = sourcel.size();
		int cend = copyl.size();
		int sbeg = 0;
		int cbeg = 0;

		//-- Slice common beginning
		while(sbeg < send && cbeg < cend) {
			I so = sourcel.get(sbeg);
			I co = copyl.get(cbeg);
			if(0 != comparator.compare(so, co)) {
				break;
			}
			sbeg++;
			cbeg++;
		}

		//-- Slice common end
		while(send > sbeg && cend > cbeg) {
			I so = sourcel.get(send - 1);
			I co = copyl.get(cend - 1);
			if(0 != comparator.compare(so, co)) {
				break;
			}
			cend--;
			send--;
		}
		if(sbeg >= send && cbeg >= cend) {
			//-- Equal arrays- no changes.
			return Collections.EMPTY_LIST;
		}

		//-- Ouf.. We need to do the hard bits. Find the lcs and then render the edit as the delta.
		int m = (send - sbeg) + 1;
		int n = (cend - cbeg) + 1;
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
				I so = sourcel.get(sbeg + i - 1);
				I co = copyl.get(cbeg + j - 1);
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
		for(int xxx = sourcel.size(); --xxx >= send;) {
			Item<I> e = new Item<I>(Type.SAME, sourcel.get(xxx));
			res.add(e);
			e.setIndex(xxx);
			tmp.add("  " + sourcel.get(xxx) + " @" + xxx + " (e)");
		}

		int i = m - 1;
		int j = n - 1;
		int sindex = i;
		while(j > 0 || i > 0) {
			if(i > 0 && j > 0 && 0 == comparator.compare(sourcel.get(sbeg + i - 1), copyl.get(cbeg + j - 1))) {
				tmp.add("  " + sourcel.get(sbeg + i - 1) + " @" + (sindex));
				res.add(new Item<I>(Type.SAME, sourcel.get(sbeg + i - 1)));
				sindex--;
				i--;
				j--;

				//-- part of lcs - no delta
			} else if(j > 0 && (i == 0 || car[i][j - 1] >= car[i - 1][j])) {
				//-- Addition
				tmp.add("+ " + copyl.get(cbeg + j - 1) + " @" + sindex);
				res.add(new Item<I>(Type.ADD, copyl.get(cbeg + j - 1)));
				j--;
			} else if(i > 0 && (j == 0 || car[i][j - 1] < car[i - 1][j])) {
				//-- Deletion
				tmp.add("- " + sourcel.get(sbeg + i - 1) + " @" + sindex);
				res.add(new Item<I>(Type.DELETE, sourcel.get(sbeg + i - 1)));
				i--;
			}
		}

		//-- Add all unhandled @ start,
		for(i = sbeg; --i >= 0;) {
			tmp.add("  " + sourcel.get(i) + " @" + i);
			res.add(new Item<I>(Type.SAME, sourcel.get(i)));
		}
		Collections.reverse(tmp);
		Collections.reverse(res);

		//-- Calculate line #s.
		List<Diff<I>> dres = new ArrayList<Diff<I>>();
		sindex = 0;
		int dindex = 0;
		int lastsindex = 0;
		int lastdindex = 0;
		Type currchange = Type.SAME;

		for(Item<I> item : res) {
			item.setIndex(sindex);

			//-- Is our type changing?
			if(currchange != item.getType()) {
				addDiffItem(sourcel, copyl, sindex, dres, dindex, lastsindex, lastdindex, currchange);
				currchange = item.getType();
				lastsindex = sindex;
				lastdindex = dindex;
			}

			switch(item.getType()){
				case ADD:
					dindex++;
					break;
				case DELETE:
					sindex++;
					break;
				case SAME:
					sindex++;
					dindex++;
					break;
			}
		}
		addDiffItem(sourcel, copyl, sindex, dres, dindex, lastsindex, lastdindex, currchange);
		for(Item<I> s : res) {
			System.out.println(" " + s);
		}

		System.out.println("Difftree:");
		for(Diff<I> d : dres) {
			System.out.print(d);
		}


		//		for(String s : tmp)
		//			System.out.println(" " + s);

		return Collections.EMPTY_LIST;
	}


	private static <I> void addDiffItem(List<I> sourcel, List<I> copyl, int sindex, List<Diff<I>> dres, int dindex, int lastsindex, int lastdindex, Type type) {
		if(lastsindex != sindex || lastdindex != dindex) {
			switch(type){
				case ADD:
					dres.add(new Diff<I>(lastsindex, sindex, copyl.subList(lastdindex, dindex), Type.ADD));
					break;
				case DELETE:
					dres.add(new Diff<I>(lastsindex, sindex, sourcel.subList(lastsindex, sindex), Type.DELETE));
					break;
				case SAME:
					dres.add(new Diff<I>(lastsindex, sindex, sourcel.subList(lastsindex, sindex), Type.SAME));
					break;
			}
		}
	}


	public static void main(String[] args) throws Exception {
		List<String> a = Arrays.asList("A", "B", "B", "A", "D", "E", "A", "D");
		List<String> b = Arrays.asList("B", "A", "A", "D", "E", "A", "D");
		Comparator<String> cs = new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		};

		// abbadead: diff is -a @0 -b @1 +A @2
		// 01234567
		//  bbadead (-a @0)
		//   badead (-b @1)
		//

		diffList(a, b, cs);


	}
}
