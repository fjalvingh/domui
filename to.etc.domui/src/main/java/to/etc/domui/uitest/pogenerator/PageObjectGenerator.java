package to.etc.domui.uitest.pogenerator;

import to.etc.domui.dom.html.UrlPage;
import to.etc.util.FileTool;

import java.io.File;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-12-21.
 */
public class PageObjectGenerator {
	static public final String PROXYPACKAGE = "to.etc.domui.webdriver.poproxies";

	private final PoGeneratorContext m_context;

	public PageObjectGenerator(UrlPage page) {
		m_context = new PoGeneratorContext(page);
	}

	public String generateAll(boolean asFiles) throws Exception {
		List<IPoProxyGenerator> generators = m_context.createGenerators(m_context.getPage());
		for(IPoProxyGenerator generator : generators) {
			generator.prepare(m_context);
		}
		for(IPoProxyGenerator generator : generators) {
			generator.generateCode(m_context);
		}

		PoClassWriter cw = new PoClassWriter();
		List<PoClass> classList = m_context.getClassList();
		for(PoClass poClass : classList) {
			poClass.visit(cw);
		}

		StringBuilder res = new StringBuilder();
		if(asFiles) {
			File tmpDir = new File(FileTool.getTmpDir(), "pageobjects");
			tmpDir.mkdirs();

			res.append("Files are generated at ").append(tmpDir).append("\n\n");

			if(tmpDir.exists()) {
				FileTool.dirEmpty(tmpDir);

				for(PoClass poClass : classList) {
					generateClassFile(tmpDir, poClass);
				}
			}
		}

		res.append(cw.getResult());
		return res.toString();
	}

	public PoGeneratorContext getContext() {
		return m_context;
	}

	private void generateClassFile(File tmpDir, PoClass poClass) throws Exception {
		String packageName = poClass.getPackageName();
		File targetDir = new File(tmpDir, packageName.replace('.', '/'));
		targetDir.mkdirs();
		PoClassWriter cw = new PoClassWriter();
		poClass.visit(cw);
		File classFile = new File(targetDir, poClass.getClassName() + ".java");
		FileTool.writeFileFromString(classFile, cw.getResult(), "UTF-8");
	}
}
