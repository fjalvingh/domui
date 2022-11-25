package to.etc.syntaxer.tests;

import org.junit.Assert;
import org.junit.Test;
import to.etc.syntaxer.HighlighterFactory;
import to.etc.syntaxer.IHighlighter;
import to.etc.syntaxer.LineContext;
import to.etc.util.FileTool;
import to.etc.util.LineIterator;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-05-22.
 */
public class SyntaxerTests {
	@Test
	public void testSqlSyntaxes() throws Exception {
		String allFiles = FileTool.readResourceAsString(getClass(), "/sql/all.txt", "utf-8");

		for(String file : new LineIterator(allFiles)) {
			checkFile(file);
		}
	}

	private void checkFile(String file) throws Exception {
		System.out.println("Testing " + file);
		StringBuilder sb = new StringBuilder();
		IHighlighter h = HighlighterFactory.getHighlighter("sql", new CopyRenderer(sb));

		String text = FileTool.readResourceAsString(getClass(), file, "utf-8");
		LineContext lc = null;
		for(String line : new LineIterator(text)) {
			lc = h.highlightLine(lc, line);
		}

		Assert.assertEquals("In- and output must be the same", text, sb.toString());
	}
}
