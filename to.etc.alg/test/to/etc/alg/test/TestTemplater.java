package to.etc.alg.test;

import org.junit.*;

import to.etc.template.*;

public class TestTemplater {
	@Test
	public void testCompile1() throws Exception {
		JSTemplateCompiler tc = new JSTemplateCompiler();
		tc.compile(TestTemplater.class, "template1.txt", "utf-8");
		System.out.println("JS:\n" + tc.getTranslation());

	}

	@Test
	public void testTemplate1() throws Exception {
		String res = run("template1.txt");
		System.out.println("res=" + res);
	}

	private String run(String res, Object... assign) throws Exception {
		StringBuilder sb = new StringBuilder();
		JSTemplateCompiler tc = new JSTemplateCompiler();
		tc.execute(sb, TestTemplater.class, res, assign);
		return sb.toString();
	}


}
