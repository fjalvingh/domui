package to.etc.util.commandinterpreter;

import to.etc.util.RuntimeConversions;

import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 15-02-22.
 */
final class AltRecognizer implements IWordRecognizer {
	private final ParamInfo m_param;

	private final List<ParamAlternative> m_altList;

	public AltRecognizer(ParamInfo param, List<ParamAlternative> altList) {
		m_param = param;
		m_altList = altList;
	}

	@Override
	public boolean recognize(CommandContext ctx, String word) {
		for(ParamAlternative alt : m_altList) {
			if(alt.getPattern().matcher(word).matches()) {
				try {
					Object value = RuntimeConversions.convertTo(alt.getValue(), m_param.getType());
					ctx.setParameter(m_param.getIndex(), value);
					return true;
				} catch(Exception x) {
					ctx.setParameterError(m_param.getIndex(), x.toString());
					return true;
				}
			}
		}
		return false;
	}
}
