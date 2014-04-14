package to.etc.domui.dom.html;

import org.junit.*;

import to.etc.domui.component.input.*;

public class NodeBaseTest {

	@Test
	public void testMessageBroadcastEnabled() {
		Text<String> str = new Text<String>(String.class);
		str.setMessageBroadcastEnabled(false);
		Assert.assertFalse("expected disabled MessageBroadcast", str.isMessageBroadcastEnabled());
		str.setMessageBroadcastEnabled(true);
		Assert.assertTrue("expected enabled MessageBroadcast", str.isMessageBroadcastEnabled());
	}
}
