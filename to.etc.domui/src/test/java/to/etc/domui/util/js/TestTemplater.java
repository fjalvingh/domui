package to.etc.domui.util.js;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestTemplater {
	@Test
	public void testCompile1() throws Exception {
		RhinoTemplateCompiler tc = new RhinoTemplateCompiler();
		tc.compile(TestTemplater.class, "template1.txt", "utf-8");
		System.out.println("JS:\n" + tc.getTranslation());

	}

	@Test
	public void testTemplate1() throws Exception {
		String res = run("template1.txt");
		System.out.println("res=" + res);
	}

	@Test
	public void testTemplate2() throws Exception {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("family", "Verdana");

		String res = run("template2.txt", "font", m);
		System.out.println("res=" + res);
	}

	private String run(String res, Object... assign) throws Exception {
		StringBuilder sb = new StringBuilder();
		RhinoTemplateCompiler tc = new RhinoTemplateCompiler();
		tc.execute(sb, TestTemplater.class, res, assign);
		//		System.out.println("CALL RES: " + xx);
		return sb.toString();
	}
}
