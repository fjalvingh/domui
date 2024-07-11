package to.etc.syntaxer.tests;

import org.junit.Assert;
import org.junit.Test;
import to.etc.syntaxer.HighlighterFactory;
import to.etc.syntaxer.IHighlighter;
import to.etc.syntaxer.LineContext;
import to.etc.util.FileTool;
import to.etc.util.LineIterator;

import java.io.File;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-05-22.
 */
public class SyntaxerTests {
	@Test
	public void testSql1() throws Exception {
		checkFile("sql1.sql");
	}

	@Test
	public void testSql2() throws Exception {
		checkFile("sql2.sql");
	}

	@Test
	public void testSql3() throws Exception {
		checkFile("sql3.sql");
	}

	private void checkFile(String file) throws Exception {
		System.out.println("Testing " + file);
		CopyRenderer renderer = new CopyRenderer();
		IHighlighter h = HighlighterFactory.getHighlighter("sql");

		String text = FileTool.readResourceAsString(getClass(), "/sql/" + file, "utf-8");
		LineContext lc = null;
		for(String line : new LineIterator(text)) {
			lc = h.highlightLine(renderer, lc, line);
		}

		String literal = renderer.getLiteral();

		Assert.assertEquals("In- and output must be the same", text, literal);

		File outputDir = new File("/tmp/syntaxer/");
		outputDir.mkdirs();
		File out = new File(outputDir, file + ".lit");
		FileTool.writeFileFromString(out, renderer.getDetailed(), "utf-8");

		String patterned;
		try {
			patterned = FileTool.readResourceAsString(getClass(), "/sql/" + file + ".lit", "utf-8");
		} catch(Exception x) {
			return;
		}
		Assert.assertEquals("Encoded output must be equal", patterned, renderer.getDetailed());
	}
}
