package to.etc.domui.log;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.component.ntbl.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.log.data.*;
import to.etc.domui.util.*;
import to.etc.log.*;

public class LoggerLevelsConfigPart extends LoggerConfigTabelarPartBase<LoggerLevelsDef> {

	private Map<String, MyLogger.Level> m_storedData;

	protected LoggerLevelsConfigPart() {
		super(LoggerLevelsDef.class);
	}

	@Override
	public void createContent() throws Exception {
		super.createContent();
	}

	private @Nonnull
	List<LoggerLevelsDef> convert(@Nonnull Map<String, MyLogger.Level> loggerLevelsDef) {
		List<LoggerLevelsDef> res = new ArrayList<LoggerLevelsDef>(loggerLevelsDef.size());
		for(String key : loggerLevelsDef.keySet()) {
			res.add(new LoggerLevelsDef(key, loggerLevelsDef.get(key)));
		}
		return res;
	}

	@Override
	protected List<LoggerLevelsDef> getData() {
		m_storedData = MyLoggerFactory.getLoggerLevelsDef();
		return convert(m_storedData);
	}

	@Override
	protected String[] getDisplayCols() {
		return new String[]{LoggerDefBase.pKEY, LoggerLevelsDef.pLEVEL};
	}

	@Override
	protected @Nonnull
	LoggerLevelsDef initializeNewInstance() {
		return new LoggerLevelsDef(null, MyLogger.Level.DEBUG);
	}

	@Override
	public boolean validateChanges() {
		return true;
	}

	@Override
	public boolean saveChanges() throws Exception {
		List<LoggerLevelsDef> list = getModel().getItems(0, getModel().getRows());
		for(LoggerLevelsDef item : list) {
			if(!m_storedData.containsKey(item.getKey())) {
				if(item.getLevel() != null) {
					MyLoggerFactory.setLevel(item.getKey(), item.getLevel());
				}
			} else {
				MyLogger.Level stored = m_storedData.get(item.getKey());
				m_storedData.remove(item.getKey());
				if(!stored.equals(item.getLevel()) && item.getLevel() != null) {
					MyLoggerFactory.setLevel(item.getKey(), item.getLevel());
				}
			}
		}
		for(String key : m_storedData.keySet()) {
			MyLoggerFactory.clearLevel(key);
		}
		return true;
	}

	@Override
	public String getPartTitle() {
		return "Levels";
	}
}
