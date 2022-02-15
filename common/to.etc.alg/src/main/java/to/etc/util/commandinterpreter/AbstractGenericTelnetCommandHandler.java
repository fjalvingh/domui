package to.etc.util.commandinterpreter;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.telnet.ITelnetCommandHandler;
import to.etc.telnet.TelnetPrintWriter;
import to.etc.util.CmdStringDecoder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static to.etc.util.ClassUtil.findAnnotation;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 15-02-22.
 */
abstract public class AbstractGenericTelnetCommandHandler implements ITelnetCommandHandler {
	private List<CommandHandler> m_commandList = new ArrayList<>();

	public AbstractGenericTelnetCommandHandler() throws Exception {
		initialize();
	}

	/**
	 * Scan all methods that have a CommandPattern annotation.
	 */
	private void initialize() throws Exception {
		for(Method method : getClass().getMethods()) {
			scanMethod(method);
		}
	}

	private void scanMethod(Method method) {
		CommandPattern patternAnn = method.getAnnotation(CommandPattern.class);
		if(null == patternAnn)
			return;
		String pattern = patternAnn.value();
		if(pattern.length() == 0)
			throw new IllegalStateException("Invalid command pattern on method " + method);
		int modifiers = method.getModifiers();
		if(!Modifier.isPublic(modifiers))
			throw new IllegalStateException("Command handler must be public: " + method);
		Class<?>[] parameterTypes = method.getParameterTypes();
		Annotation[][] panns = method.getParameterAnnotations();
		if(parameterTypes.length < 1 || ! TelnetPrintWriter.class.isAssignableFrom(parameterTypes[0]))
			throw new IllegalStateException("Command handler must have 1st parameter of type TelnetPrintWriter : " + method);

		//-- Create a map of parameter name to index and type
		Map<String, ParamInfo> paramMap = new HashMap<>();
		for(int i = 1; i < parameterTypes.length; i++) {
			Class<?> parameterType = parameterTypes[i];
			Annotation[] pann = panns[i];
			CommandParam cp = findAnnotation(pann, CommandParam.class);
			if(null == cp) {
				throw new IllegalStateException("Command handler parameter " + (i + i) + " does not have a CommandParam annotation in method " + method);
			}
			String name = cp.value();
			if(! isWordValid(name))
				throw new IllegalStateException("Command handler parameter " + (i + i) + " has an invalid parameter name in method " + method);
			ParamInfo p = new ParamInfo(name, parameterType, i);

			if(null != paramMap.put(name, p))
				throw new IllegalStateException("Command handler parameter " + (i + i) + " has a duplicate parameter name '" + name + "' in method " + method);
		}

		//-- Scan the pattern into a list of literal words and value placeholder recognizers
		String[] words = pattern.split("\\s+");
		int parameterIndex = 1;
		List<IWordRecognizer> recognizerList = new ArrayList<>();
		for(int i = 0; i < words.length; i++) {
			IWordRecognizer recognizer = calculateRecognizer(method, words[i], paramMap);
			recognizerList.add(recognizer);
		}
		ParamInfo[] ar = new ParamInfo[parameterTypes.length];
		paramMap.values().forEach(param -> ar[param.getIndex()] = param);
		for(ParamInfo param : paramMap.values()) {
			if(! param.isUsed()) {
				throw new IllegalStateException("Parameter " + param.getName() + " not used in " + method);
			}
		}

		m_commandList.add(new CommandHandler(method, pattern, ar, recognizerList));
	}

	private IWordRecognizer calculateRecognizer(Method method, String word, Map<String, ParamInfo> paramMap) {
		//-- 1. If the word is in {} then it is a parameter.
		String unBraced = inBraces(word);
		if(unBraced != null) {
			return calculateParameterRecognizer(method, unBraced, paramMap);
		}
		//unBraced = inAngleBraces(word);
		//if(null != unBraced) {
		//
		//
		//
		//}

		return calculateLiteralRecognizer(method, word);
	}

	/**
	 * Calculate a recognizer for a parameter placeholder. The placeholder's {} have already been removed.
	 *
	 * The placeholder syntax is the following:
	 * <ul>
	 *     <li>placeholder := name [':' specifier]</li>
	 *     <li>specifier := valuespec ['|' valueSpec]*</li>
	 *     <li>valueSpec := ID '=' ID</li>
	 *     <li>name := ID</li>
	 * </ul>
	 */
	private IWordRecognizer calculateParameterRecognizer(Method method, String word, Map<String, ParamInfo> paramMap) {
		int ix = word.indexOf(':');			// Do we have a value spec?
		if(ix == -1) {
			//-- Nope. This is a literal value conversion to the specified parameter
			ParamInfo param = paramMap.get(word);
			if(null == param) {
				throw new IllegalStateException("Unknown parameter '" + word + "' in CommandPattern for " + method);
			}
			if(param.isUsed()) {
				throw new IllegalStateException("Parameter '" + word + "' is used more than once in CommandPattern for " + method);
			}
			param.setUsed(true);
			return new ValueRecognizer(param);
		}

		//-- Get the parameter name and the list-of-values string
		String paramName = word.substring(0, ix);
		ParamInfo param = paramMap.get(paramName);
		if(null == param) {
			throw new IllegalStateException("Unknown parameter '" + paramName + "' in CommandPattern for " + method);
		}
		if(param.isUsed()) {
			throw new IllegalStateException("Parameter '" + paramName + "' is used more than once in CommandPattern for " + method);
		}
		param.setUsed(true);

		String rest = word.substring(ix + 1);
		String[] alternatives = rest.split("\\|");
		List<ParamAlternative> altList = new ArrayList<>();
		for(String alternative : alternatives) {
			ParamAlternative alt = decodeAlternative(method, alternative);
			altList.add(alt);
		}
		return new AltRecognizer(param, altList);
	}

	private ParamAlternative decodeAlternative(Method m, String alternative) {
		String[] split = alternative.split("=");
		if(split.length == 1) {
			Pattern pattern = calculatePattern(m, split[0]);
			return new ParamAlternative(pattern, split[0]);
		}

		Pattern pattern = calculatePattern(m, split[0]);
		return new ParamAlternative(pattern, split[1]);
	}

	/**
	 * Recognizes literal text. The text can have an optional ending yyy which must be
	 * specified as "xxxx[yyy]".
	 */
	private IWordRecognizer calculateLiteralRecognizer(Method method, String word) {
		return new LiteralWordRecognizer(calculatePattern(method, word));
	}

	private Pattern calculatePattern(Method method, String word) {
		//-- Word may contain [] to make the end optional
		int ix = word.indexOf('[');
		if(ix == -1) {
			if(! isWordValid(word))
				throw new IllegalStateException("Invalid word '" + word + "' in handler " + method);

			Pattern pattern = Pattern.compile(word, Pattern.CASE_INSENSITIVE);
			return pattern;
		}

		if(! word.endsWith("]"))
			throw new IllegalStateException("Invalid word '" + word + "' (missing end ]) in handler " + method);
		String main = word.substring(0, ix);
		String rest = word.substring(ix + 1, word.length() - 1).trim();
		if(! isWordValid(main) || ! isWordValid(rest))
			throw new IllegalStateException("Invalid word '" + word + "' in handler " + method);

		String mp = main + "[" + rest + "]?";
		Pattern pattern = Pattern.compile(mp, Pattern.CASE_INSENSITIVE);
		return pattern;
	}

	private static boolean isWordValid(String w) {
		if(w.length() == 0)
			return false;
		if(! Character.isLetter(w.charAt(0)))
			return false;
		for(int i = 1; i < w.length(); i++) {
			char c = w.charAt(i);
			if(! Character.isLetterOrDigit(c))
				return false;
		}
		return true;
	}


	@Nullable
	private static String inBraces(String word) {
		word = word.trim();
		if(word.length() <= 2)
			return null;
		if(word.charAt(0) != '{' || word.charAt(word.length() - 1) != '}')
			return null;
		return word.substring(1, word.length() - 1).trim();
	}

	@Nullable
	private static String inAngleBraces(String word) {
		word = word.trim();
		if(word.length() <= 2)
			return null;
		if(word.charAt(0) != '[' || word.charAt(word.length() - 1) != ']')
			return null;
		return word.substring(1, word.length() - 1).trim();
	}

	@Override
	public boolean executeTelnetCommand(TelnetPrintWriter tpw, CmdStringDecoder commandline) throws Exception {
		String inputString = commandline.getInputString();
		String[] words = inputString.split("\\s+");
		if(words.length == 0) {
			return false;
		}

		//-- If the last word is a "?" then report all commands that match before
		if("?".equals(words[words.length - 1])) {
			renderHelp(tpw, words);
			return true;
		}

		//-- Try to find a match/partial match
		List<CommandContext> okContexts = new ArrayList<>();
		List<CommandContext> partialContexts = new ArrayList<>();
		for(CommandHandler commandHandler : m_commandList) {
			CommandContext ctx = new CommandContext(commandHandler);
			if(ctx.recognize(words)) {
				okContexts.add(ctx);
			} else if(ctx.getLongestMatch().size() > 0) {
				partialContexts.add(ctx);
			}
		}
		if(okContexts.size() == 1) {
			CommandContext cc = okContexts.get(0);
			if(cc.hasError()) {
				tpw.println("error: " + cc.getErrorMessage());
				return true;
			}

			//-- Not an error -> execute
			Method method = cc.getHandler().getMethod();
			Object[] values = cc.getParamValues();
			values[0] = tpw;
			method.invoke(this, values);
			return true;
		}

		List<CommandContext> all = new ArrayList<>();
		all.addAll(okContexts);
		all.addAll(partialContexts);
		int largest = 0;
		List<CommandContext> matchList = new ArrayList<>();
		for(CommandContext cc : all) {
			List<String> longestMatch = cc.getLongestMatch();
			if(longestMatch.size() > largest) {
				largest = longestMatch.size();
				matchList.clear();
				matchList.add(cc);
			} else if(longestMatch.size() == largest && largest > 0) {
				matchList.add(cc);
			}
		}
		if(largest > 0 && matchList.size() > 0) {
			tpw.println("Unrecognized command. Did you mean:");
			for(CommandContext cc : matchList) {
				tpw.println("- " + cc.getHandler().getHelpText());
			}
			return true;
		}
		return false;
	}

	private void renderHelp(TelnetPrintWriter tpw, String[] words) {
		List<CommandContext> okContexts = new ArrayList<>();
		List<CommandContext> partialContexts = new ArrayList<>();

		String[] help = new String[words.length - 1];
		System.arraycopy(words, 0, help, 0, words.length - 1);

		boolean helped = false;
		for(CommandHandler commandHandler : m_commandList) {
			CommandContext ctx = new CommandContext(commandHandler);
			if(ctx.renderHelp(tpw, help)) {
				helped = true;
			}
		}
		if(! helped)
			tpw.println("No help found for that sequence of words");
	}
}
