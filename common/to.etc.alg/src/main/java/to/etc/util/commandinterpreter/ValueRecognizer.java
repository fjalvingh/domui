package to.etc.util.commandinterpreter;

import to.etc.util.RuntimeConversions;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 15-02-22.
 */
final class ValueRecognizer implements IWordRecognizer {
	private final ParamInfo m_param;

	public ValueRecognizer(ParamInfo param) {
		m_param = param;
	}

	@Override
	public boolean recognize(CommandContext ctx, String word) {
		try {
			Object o = RuntimeConversions.convertTo(word, m_param.getType());
			ctx.setParameter(m_param.getIndex(), o);
			return true;
		} catch(Exception x) {
			ctx.setParameterError(m_param.getIndex(), x.getMessage());
		}
		return false;
	}
}
