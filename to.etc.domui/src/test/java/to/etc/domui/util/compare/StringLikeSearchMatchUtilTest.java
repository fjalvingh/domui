package to.etc.domui.util.compare;

import org.junit.*;

/**
 * Created by vmijic on 21.8.15..
 */
public class StringLikeSearchMatchUtilTest {

	@Test
	public void testWhenExpectedMatchMatchIsFound() throws Exception {
		StringLikeSearchMatchUtil util = new StringLikeSearchMatchUtil();
		Assert.assertTrue("Successful match", util.compareLike("Abcd", "Abcd"));
		Assert.assertTrue("Successful match", util.compareLike("Abcd", "A%d"));
		Assert.assertTrue("Successful match", util.compareLike("Abcd", "Abcd%"));
		Assert.assertTrue("Successful match", util.compareLike("Abcd", "%Ab%cd%"));
		Assert.assertTrue("Successful match", util.compareLike("AbcAbeAbcd", "%Abcd%"));
	}

	@Test
	public void testWhenNotExpectedMatchMatchIsNotFound() throws Exception {
		StringLikeSearchMatchUtil util = new StringLikeSearchMatchUtil();
		Assert.assertFalse("Unsuccessful match", util.compareLike("bcd", "A%d"));
		Assert.assertFalse("Unsuccessful match", util.compareLike("abcd", "bcd%"));
		Assert.assertFalse("Unsuccessful match", util.compareLike("bcd", "%Ab%cd%"));
		Assert.assertFalse("Unsuccessful match", util.compareLike("bcAbeAbcd", "abcd%"));
	}
}
