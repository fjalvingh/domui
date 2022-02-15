package to.etc.util.commandinterpreter;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 15-02-22.
 */
interface IWordRecognizer {
	boolean recognize(CommandContext ctx, String word);
}
