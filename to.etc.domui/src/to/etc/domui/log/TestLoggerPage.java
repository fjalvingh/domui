package to.etc.domui.log;

import org.slf4j.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.dom.html.*;

public class TestLoggerPage extends UrlPage {
	private static Logger LOG = LoggerFactory.getLogger(TestLoggerPage.class);

	@Override
	public void createContent() throws Exception {
		super.createContent();
		add(new DefaultButton("Click to log line", new IClicked<DefaultButton>() {

			@Override
			public void clicked(DefaultButton clickednode) throws Exception {
				MDC.put("page", this.getClass().getSimpleName());
				LOG.debug("debug log");
				LOG.trace("trace log");
				LOG.info("info log");
				LOG.warn("warn log");
				LOG.error("error log");
				add("Added lines to log");
				add(new BR());
			}
		}));
	}

}
