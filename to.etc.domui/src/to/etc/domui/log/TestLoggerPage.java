package to.etc.domui.log;

import java.util.*;

import javax.annotation.*;

import org.slf4j.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.dom.html.*;
import to.etc.webapp.qsql.*;

public class TestLoggerPage extends UrlPage {
	private static Logger LOG = LoggerFactory.getLogger(TestLoggerPage.class);

	@Override
	public void createContent() throws Exception {
		super.createContent();
		add(new DefaultButton("Click to log line", new IClicked<DefaultButton>() {

			@Override
			public void clicked(@Nonnull DefaultButton clickednode) throws Exception {
				LOG.debug("debug log");
				LOG.trace("trace log");
				LOG.info("info log");
				LOG.warn("warn log");
				LOG.error("error log");
				add("Added lines to log");
				add(new BR());
			}
		}));
		add(new DefaultButton("Click to set MDC page=AAA", new IClicked<DefaultButton>() {

			@Override
			public void clicked(@Nonnull DefaultButton clickednode) throws Exception {
				MDC.put("page", "AAA");
				add("Added MDC AAA");
				add(new BR());
			}
		}));
		add(new DefaultButton("Click to set MDC page=BBB", new IClicked<DefaultButton>() {

			@Override
			public void clicked(@Nonnull DefaultButton clickednode) throws Exception {
				MDC.put("page", "BBB");
				add("Added MDC BBB");
				add(new BR());
			}
		}));
		add(new DefaultButton("Click to log formatted INFO mesages", new IClicked<DefaultButton>() {

			@Override
			public void clicked(@Nonnull DefaultButton clickednode) throws Exception {
				LOG.info("Info Log that uses format 1 part message, values: {}", "[value1]");
				LOG.info("Info Log that uses format 2 parts message, values: {} {}", "[value1]", "[value2]");
				LOG.info("Info Log that uses format 3 parts message, values: {} {} {}", new String[]{"[value1]", "[value2]", "[value3]"});
				add("Added formatted lines to log");
				add(new BR());
			}
		}));
		add(new DefaultButton("Click to log simple exception", new IClicked<DefaultButton>() {

			@Override
			public void clicked(@Nonnull DefaultButton clickednode) throws Exception {
				LOG.error("Example of simple exception logging.", new Exception("This is some exception logged."));
				try {
					Integer.parseInt("THIS IS NOT A INTEGER!");
				} catch(Exception ex) {
					LOG.error("Example of simple exception logging.", ex);
				}
				add("Added simple exception to log");
				add(new BR());
			}
		}));
		add(new DefaultButton("Click to log nested exception", new IClicked<DefaultButton>() {

			@Override
			public void clicked(@Nonnull DefaultButton clickednode) throws Exception {
				try {
					Integer.parseInt("THIS IS NOT A INTEGER!");
				} catch(Exception ex) {
					LOG.error("Example of nested exception logging.", new IllegalStateException("hah", new IllegalStateException("bah", new IllegalStateException("ah ah", new IllegalStateException("uh uh",
						new IllegalStateException("oh oh", new IllegalStateException("This is so unespected ;)", ex)))))));
				}
				add("Added nested exception to log");
				add(new BR());
			}
		}));
		add(new DefaultButton("Click to log SQL exception", new IClicked<DefaultButton>() {

			@Override
			public void clicked(@Nonnull DefaultButton clickednode) throws Exception {
				try {
					JdbcUtil.oracleSpCall(getSharedContext().getConnection(), Integer.class, "TestStoredProcedure", new Date(), Integer.valueOf(5), "param3");
				} catch(Exception ex) {
					LOG.error("Example of SQL exception logging.", ex);
				}
				add("Added SQL exception to log");
				add(new BR());
			}
		}));
	}

}
