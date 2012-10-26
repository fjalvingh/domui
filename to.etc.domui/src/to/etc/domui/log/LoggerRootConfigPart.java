package to.etc.domui.log;

import to.etc.domui.component.controlfactory.*;
import to.etc.domui.component.form.*;
import to.etc.domui.log.data.*;
import to.etc.log.*;

public class LoggerRootConfigPart extends LoggerConfigPartBase {
	private LoggerRootDef m_instance;

	private ModelBindings m_bindings;

	@Override
	public void createContent() throws Exception {
		super.createContent();
		m_instance = new LoggerRootDef(MyLoggerFactory.getDefaultLevel(), MyLoggerFactory.isDisabled(), MyLoggerFactory.getRootDir(), MyLoggerFactory.getLogDir());
		TabularFormBuilder tbl = new TabularFormBuilder(m_instance);
		tbl.addProps(LoggerRootDef.pROOTDIR, LoggerRootDef.pLOGDIR, LoggerRootDef.pLEVEL, LoggerRootDef.pDISABLED);
		add(tbl.finish());
		m_bindings = tbl.getBindings();
		m_bindings.moveModelToControl();
	}

	@Override
	public boolean validateChanges() {
		return true;
	}

	@Override
	public boolean saveChanges() throws Exception {
		m_bindings.moveControlToModel();
		if(m_instance.getLevel() != MyLoggerFactory.getDefaultLevel()) {
			MyLoggerFactory.setDefaultLevel(m_instance.getLevel());
		}
		if(m_instance.isDisabled() != MyLoggerFactory.isDisabled()) {
			MyLoggerFactory.setDisabled(m_instance.isDisabled());
		}
		return true;
	}

	@Override
	public String getPartTitle() {
		return "Root config";
	}

}
