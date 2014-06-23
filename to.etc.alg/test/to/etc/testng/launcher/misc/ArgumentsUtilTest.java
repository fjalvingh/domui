package to.etc.testng.launcher.misc;

import org.junit.*;

public class ArgumentsUtilTest {

	@Test
	public void testParseAsRunOptions() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("-opt1 /bla/bla/bla\n");
		sb.append("-opt2 /bla/bla/bla /bla/bla/bla\n");
		sb.append("-opt3 \"/bla/bla/bla /bla/bla/bla\"\n");
		sb.append("-opt4 \n");
		sb.append("\n");
		sb.append("\n");
		sb.append("-opt5 \"/bla/bla/bla /bla/bla/bla\" /bla/bla/bla");

		String[] args = ArgumentsUtil.parseAsRunOptions(sb.toString());

		Assert.assertEquals(11, args.length);
		Assert.assertEquals("-opt1", args[0]);
		Assert.assertEquals("/bla/bla/bla", args[1]);
		Assert.assertEquals("/bla/bla/bla /bla/bla/bla", args[6]);
		Assert.assertEquals("-opt5", args[8]);
		Assert.assertEquals("/bla/bla/bla /bla/bla/bla", args[9]);
		Assert.assertEquals("/bla/bla/bla", args[10]);
	}

}
