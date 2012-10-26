package to.etc.domui.log;

import java.util.*;

import to.etc.domui.component.ntbl.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.html.*;
import to.etc.log.*;
import to.etc.log.data.*;

public class LoggerConfigPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		super.createContent();
		List<LoggerOutputDef> logOutList = MyLoggerFactory.getLoggerOutputDef();
		ITableModel<LoggerOutputDef> logOutModel = new DefaultTableModel<LoggerOutputDef>(logOutList);
		ExpandingEditTable<LoggerOutputDef> table = new ExpandingEditTable<LoggerOutputDef>(logOutModel, new BasicRowRenderer<LoggerOutputDef>(LoggerOutputDef.class, "key", "output"));
		add(table);
	}

}
