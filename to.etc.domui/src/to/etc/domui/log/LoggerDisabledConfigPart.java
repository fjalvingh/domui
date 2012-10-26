package to.etc.domui.log;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.log.data.*;
import to.etc.domui.util.*;
import to.etc.log.*;

public class LoggerDisabledConfigPart extends LoggerConfigTabelarPartBase<LoggerDefBase> {

	private Set<String> m_storedData;

	protected LoggerDisabledConfigPart() {
		super(LoggerDefBase.class);
	}

	@Override
	public void createContent() throws Exception {
		super.createContent();
	}

	private @Nonnull
	List<LoggerDefBase> convert(@Nonnull Set<String> loggerDisabledDef) {
		List<LoggerDefBase> res = new ArrayList<LoggerDefBase>(loggerDisabledDef.size());
		Iterator<String> i = loggerDisabledDef.iterator();
		while(i.hasNext()) {
			res.add(new LoggerDefBase(i.next()));
		}
		return res;
	}

	@Override
	protected List<LoggerDefBase> getData() {
		m_storedData = MyLoggerFactory.getDisabledDef();
		return convert(m_storedData);
	}

	@Override
	protected String[] getDisplayCols() {
		return new String[]{LoggerDefBase.pKEY};
	}

	@Override
	protected @Nonnull
	LoggerDefBase initializeNewInstance() {
		return new LoggerDefBase(null);
	}

	@Override
	public boolean validateChanges() {
		return true;
	}

	@Override
	public boolean saveChanges() throws Exception {
		List<LoggerDefBase> list = getModel().getItems(0, getModel().getRows());
		for(LoggerDefBase item : list) {
			if(!m_storedData.contains(item.getKey())) {
				if(!DomUtil.isBlank(item.getKey())) {
					MyLoggerFactory.setDisabled(item.getKey(), true);
				}
			} else {
				m_storedData.remove(item.getKey());
			}
		}
		Iterator<String> i = m_storedData.iterator();
		while(i.hasNext()) {
			MyLoggerFactory.setDisabled(i.next(), false);
		}
		return true;
	}

	@Override
	public String getPartTitle() {
		return "Disabled";
	}
}
