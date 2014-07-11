package to.etc.testng.launcher.misc;

import java.util.*;

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

	@Test
	public void testMergeArgumentsOnlyFile() throws Exception {
		String optionsFileName = this.getClass().getResource("optionsFile.res").getFile();
		String[] argumentsFromOptionsFile = ArgumentsUtil.getRunFromFileOptions(optionsFileName);

		List<String> list = new ArrayList<String>();

		ArgumentsUtil.mergeArguments(list, argumentsFromOptionsFile);
		Assert.assertEquals(28, list.size());
		Assert.assertTrue(list.contains("-testng.server.url"));
		int index = list.indexOf("-testng.server.url");
		Assert.assertEquals("http://localhost:8080/ITRIS_VO02/", list.get(index + 1));
	}

	@Test
	public void testMergeArgumentsMixed() throws Exception {
		String optionsFileName = this.getClass().getResource("optionsFile.res").getFile();
		String[] argumentsFromOptionsFile = ArgumentsUtil.getRunFromFileOptions(optionsFileName);

		List<String> list = new ArrayList<String>();
		list.add("-testng.server.url");
		list.add("xxx");

		ArgumentsUtil.mergeArguments(list, argumentsFromOptionsFile);
		int index = list.indexOf("-testng.server.url");
		Assert.assertEquals("xxx", list.get(index + 1));

		index = list.indexOf("-testng.username");
		Assert.assertEquals("vpc", list.get(index + 1));
	}

	@Test
	public void testMergeArgumentsNoFile() throws Exception {
		String[] argumentsFromOptionsFile = null;

		List<String> list = new ArrayList<String>();

		ArgumentsUtil.mergeArguments(list, argumentsFromOptionsFile);
		Assert.assertEquals(0, list.size());

		list.add("-testng.server.url");
		list.add("xxx");
		ArgumentsUtil.mergeArguments(list, argumentsFromOptionsFile);
		Assert.assertEquals(2, list.size());

		int index = list.indexOf("-testng.server.url");
		Assert.assertEquals("xxx", list.get(index + 1));
	}

	@Test
	public void testMergeArguments() throws Exception {
		String optionsFileName = this.getClass().getResource("optionsFile.res").getFile();

		String[] args = new String[]{ //
		"-options.file", optionsFileName, "-testng.server.url", "xxx"};

		String[] mergedArgs = ArgumentsUtil.mergeArguments("options.file", args);

		List<String> list = Arrays.asList(mergedArgs);

		int index = list.indexOf("-testng.server.url");
		Assert.assertEquals("xxx", list.get(index + 1));

		index = list.indexOf("-testng.username");
		Assert.assertEquals("vpc", list.get(index + 1));
	}

}
