package to.etc.util;

import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class StringToolTest {

	@Test
	public void testStripAccents() {
		var examples = List.of(
			new Pair<>("filë_name.xls", "file_name.xls"),
			new Pair<>("ćčćč", "cccc")		);
		for(var example : examples) {
			var rename = StringTool.stripAccents(example.get1());
			assertEquals(example.get2(), rename);
		}
	}
}