package to.etc.domui.logic.errors;

import to.etc.domui.trouble.*;
import to.etc.domui.util.*;

import javax.annotation.*;

final public class LogicException extends ValidationException {
	@Nullable
	final private ProblemModel m_problems;

	public LogicException() {
		super(Msgs.BUNDLE, Msgs.V_LOGIC_ERROR);
		m_problems = null;
	}

	public LogicException(ProblemModel problems) {
		super(Msgs.BUNDLE, Msgs.V_LOGIC_ERROR);
		m_problems = problems;
	}

	@Override public String toString() {
		ProblemModel pm = m_problems;
		if(null == pm) {
			return super.toString();
		}
		return "LogicException:\n"+pm;
	}
}
