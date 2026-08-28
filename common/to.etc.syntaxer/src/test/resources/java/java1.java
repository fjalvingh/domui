package to.etc.syntaxer.sample;

import java.util.List;

/**
 * A sample used by the highlighter tests. It deliberately contains the
 * constructs that are easy to get wrong: comments holding quotes, strings
 * holding comment starters, escapes, and the numeric formats.
 */
public final class Sample<T> extends Base implements Runnable {
	private static final String GREETING = "Hello \"world\", a backslash \\ and a unicode é";

	private static final String NOT_A_COMMENT = "/* this is a string */ // and so is this";

	private int m_count;			// a trailing line comment with a ' quote in it

	/* a block comment
	   spanning several lines, with a " in it */
	public Sample(int count) {
		m_count = count;
	}

	@Override
	public void run() {
		int decimal = 1_000_000;
		int hex = 0xCAFEBABE;
		int binary = 0b1010_0101;
		int octal = 0777;
		long big = 123456789L;
		double scientific = 1.5e-3d;
		char c = 'x';
		char escaped = '\n';
		boolean flag = true;
		Object nothing = null;

		var list = List.of(decimal, hex, binary, octal);
		for(var item : list) {
			if(item > 0 && flag) {
				System.out.println(GREETING + item + big + scientific + c + escaped + nothing);
			}
		}
	}
}
