package to.etc.util;

import java.util.*;

import javax.annotation.*;

import org.junit.*;

public class TestDiff {
	@BeforeClass
	static public void before() {
		//		Diff.DEBUG = true;
		//		Diff.DEBUG2 = true;
	}

	@Test
	public void test1() throws Exception {
		//-- Adding stuff: single ranges.
		diff("abcdef", "abcxdef", "+@3:x");
		diff("abcdef", "abcxyzdef", "+@3:xyz");
		diff("abcdef", "abcdef");							// Same

		diff("abcdef", "abcdefghij", "+@6:ghij");			// Added @ end
		diff("abcdef", "ghijabcdef", "+@0:ghij");			// Added @ start
	}

	@Test
	public void test2() throws Exception {
		//-- Deleting stuff: single ranges
		diff("abcdefghij", "abcdghij", "-@4:ef");
		diff("abcdefghij", "defghij", "-@0:abc");			// @start
		diff("abcdefghij", "abcdef", "-@6:ghij");
	}

	@Test
	public void test3() throws Exception {
		//-- Multiple ranges deleted in the set
		diff("abcdefuvwxyz", "abefuvyz", "-@2:cd", "-@8:wx");	// Remove cd and wx, and make sure report is correct from old string indices.
		diff("abcdefuvwxyz", "defuvw", "-@0:abc", "-@9:xyz");	// Front and back removed.
		diff("abcdefghijklmnopqrstuvwxyz", "bcdefghijlnopqrstuvw", "-@0:a", "-@10:k", "-@12:m", "-@23:xyz");
	}

	@Test
	public void test4() throws Exception {
		//-- Multiple ranges added in the set.
		diff("abcdef", "123abc456def", "+@0:123", "+@3:456");
		diff("abcdef", "ab123cde456fghij", "+@2:123", "+@5:456", "+@6:ghij");
	}

	@Test
	public void test5() throws Exception {
		diff("abcdefghijklm", "123defghijklm", "-@0:abc", "+@3:123");					// replace abc with 123
	}


	/**
	 * Use the diff engine to create a comparison between letter strings.
	 * @param string
	 * @param string2
	 */
	private void diff(String olds, String news, String... changes) throws Exception {
		//-- Create lists
		List<Character> oldl = createList(olds);
		List<Character> newl = createList(news);
		List<Diff<Character>> dl = Diff.diffList(oldl, newl, null);

		//-- The #of changes must be the same
		Assert.assertEquals(changes.length, dl.size());

		StringBuilder sb = new StringBuilder();
		int resix = 0;
		for(String c : changes) {
			Diff<Character> de = dl.get(resix++);
			sb.setLength(0);
			switch(de.getType()){
				default:
					throw new IllegalStateException(de.getType() + " not expected");
				case ADD:
					sb.append("+");
					break;
				case DELETE:
					sb.append("-");
					break;
			}
			sb.append('@');
			sb.append(de.getStartIndex());
			sb.append(':');

			for(Character dch : de.getList()) {
				sb.append(dch.charValue());
			}
			String diffchange = sb.toString();
			Assert.assertEquals(c, diffchange);
		}
	}

	@Nonnull
	private List<Character> createList(@Nonnull String s) {
		List<Character> res = new ArrayList<Character>(s.length());
		for(int i = 0; i < s.length(); i++) {
			res.add(Character.valueOf(s.charAt(i)));
		}
		return res;
	}


}
