package to.etc.util.commandinterpreter;

import java.util.regex.Pattern;

/**
 * Recognizes literal words.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 15-02-22.
 */
final class LiteralWordRecognizer implements IWordRecognizer {
	private final Pattern m_matcher;

	public LiteralWordRecognizer(Pattern matcher) {
		m_matcher = matcher;
	}

	@Override
	public boolean recognize(CommandContext ctx, String word) {
		return m_matcher.matcher(word).matches();
	}

}
