package to.etc.domui;

import org.junit.*;
import to.etc.domui.util.*;

import javax.annotation.*;
import java.util.*;
import java.util.stream.*;

/**
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 5.12.17..
 */
public class DomUtilTest {
	@Test
	public void testStripHtml_whenHasMetaTag_itIsNotInStrippedPlainText() throws Exception {
		String html = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"><html><head></head><body>bla<br/><div>bla2</div>bla3</body></html>";
		StringBuilder sb = new StringBuilder();
		DomUtil.stripHtml(sb, html);
		Assert.assertEquals("stripped text as expected", "blabla2bla3", sb.toString());
	}

	@Nonnull
	private List<Integer> asList(int... items){
		List<Integer> list = new ArrayList<>(items.length);
		for (int item : items){
			list.add(Integer.valueOf(item));
		}
		return list;
	}

	@Test
	public void testBatches() throws Exception {
		Assert.assertEquals("expect split batches",
			Arrays.asList(asList(1, 2, 3), asList(4, 5, 6), asList(7, 8)),
			DomUtil.batches(asList(1, 2, 3, 4, 5, 6, 7, 8), 3).collect(Collectors.toList()));

		Assert.assertEquals("expect single batch",
			Arrays.asList(asList(1, 2, 3)),
			DomUtil.batches(asList(1, 2, 3), 4).collect(Collectors.toList()));

		Assert.assertEquals("expect empty list", new ArrayList<>(), DomUtil.batches(new ArrayList<>(), 3).collect(Collectors.toList()));
	}
}
