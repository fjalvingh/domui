package to.etc.alg.test;

import java.util.*;

import javax.script.*;

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

	@Test
	public void testTemplate2() throws Exception {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("family", "Verdana");

		String res = run("template2.txt", "font", m);
		System.out.println("res=" + res);
	}

	private String run(String res, Object... assign) throws Exception {
		StringBuilder sb = new StringBuilder();
		JSTemplateCompiler tc = new JSTemplateCompiler();
		tc.execute(sb, TestTemplater.class, res, assign);
		//		System.out.println("CALL RES: " + xx);
		return sb.toString();
	}

	@Test
	public void testOne() throws Exception {
		ScriptEngineManager m = new ScriptEngineManager();
		ScriptEngine se = m.getEngineByName("js");
		Object v = se.eval("[1, 2]");
		//		Object v = se.eval("{a: 12, b: 'hello'};");
		System.out.println("val=" + v);
	}

	@Test
	public void testBindings() throws Exception {
		ScriptEngineManager m = new ScriptEngineManager();
		ScriptEngine se = m.getEngineByName("JavaScript");
		if(null == se) {
			se = m.getEngineByName("nashorn");
			if(null == se)
				throw new IllegalStateException("NO JAVASCRIPT");
		}

		Bindings b = se.getBindings(ScriptContext.ENGINE_SCOPE);
		for(String name : b.keySet()) {
			System.out.println("before k=" + name + ", value=" + b.get(name));
		}

		Object v = se.eval("var a = 12; var b = 100;");

		b = se.getBindings(ScriptContext.ENGINE_SCOPE);
		for(String name : b.keySet()) {
			System.out.println("after k=" + name + ", value=" + b.get(name));
		}

	}


}
