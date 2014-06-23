package to.etc.testng.launcher.misc;

import java.util.*;

import javax.annotation.*;

/**
 * Util library for working with specific command line arguments.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Jul 31, 2013
 */
public class ArgumentsUtil {
	private final @Nonnull
	Map<String, List<String>>	m_arguments	= new HashMap<String, List<String>>();

	private static final @Nonnull
	String						HELP		= "\nPlease see doc using -help";

	public boolean isEmptyArgumentValues(@Nonnull List<String> values) {
		return null == values || values.isEmpty();
	}

	public String getSingleArgumentValue(@Nonnull String argumentKey) {
		List<String> argValues = m_arguments.get(argumentKey);
		if(isEmptyArgumentValues(argValues)) {
			throw new IllegalArgumentException("Missing argument value -" + argumentKey + HELP);
		}
		if(argValues.size() > 1) {
			throw new IllegalArgumentException("Multiple argument values are not permitted -" + argumentKey + HELP);
		}
		return argValues.get(0);
	}

	public String getSingleArgumentValue(@Nonnull String argumentKey, @Nonnull String defaultValue) {
		List<String> argValues = m_arguments.get(argumentKey);
		if(isEmptyArgumentValues(argValues)) {
			return defaultValue;
		}
		if(argValues.size() > 1) {
			throw new IllegalArgumentException("Multiple argument values are not permitted -" + argumentKey + HELP);
		}
		return argValues.get(0);
	}

	public ArgumentsUtil(@Nonnull String[] args) {
		String key = null;
		List<String> values = null;
		for(String arg : args) {
			if(arg.startsWith("-")) {
				key = arg.substring(1);
				values = m_arguments.get(key);
				if(null == values) {
					values = new ArrayList<String>();
					m_arguments.put(key, values);
				}
			} else {
				if(null == key) {
					throw new IllegalArgumentException("Unrecognized argument value: " + arg + ". Plase prefix with proper argument name that starts with -." + HELP);
				}
				values.add(arg);
			}
		}
	}

	public @Nonnull
	List<String> getMandatory(@Nonnull String argKey) {
		List<String> values = m_arguments.get(argKey);
		if(isEmptyArgumentValues(values)) {
			throw new IllegalArgumentException("Missing argument value -" + argKey + HELP);
		}
		return values;
	}

	public @Nonnull
	String getOptionalSingle(@Nonnull String argKey) {
		List<String> values = m_arguments.get(argKey);
		if(!isEmptyArgumentValues(values)) {
			if(values.size() > 1) {
				throw new IllegalArgumentException("Multiple argument values are not allowed -" + argKey + HELP);
			}
			return values.get(0);
		}
		return null;
	}

	public @Nullable
	List<String> getOptional(@Nonnull String argKey) {
		return m_arguments.get(argKey);
	}

	public @Nonnull
	List<String> getOptionalNonNull(@Nonnull String argKey) {
		List<String> res = m_arguments.get(argKey);
		if(null == res) {
			return Collections.EMPTY_LIST;
		}
		return res;
	}

	public int getIntParam(@Nonnull String argkey, int defaultVal) {
		List<String> values = m_arguments.get(argkey);
		if(isEmptyArgumentValues(values)) {
			return defaultVal;
		}
		try {
			String val = getSingleArgumentValue(argkey);
			return Integer.parseInt(val);
		} catch(Exception ex) {
			ex.printStackTrace();
			//in case of bad argument just return default value
			return defaultVal;
		}
	}

	@Nonnull
	public static String[] parseAsRunOptions(@Nonnull String fileOptionsAsStr) {
		fileOptionsAsStr = fileOptionsAsStr.replaceAll("\r\n", " ");
		fileOptionsAsStr = fileOptionsAsStr.replaceAll("\n", " ");
		List<String> result = new ArrayList<String>();
		boolean insideDoubleQuotes = false;
		StringBuilder current = new StringBuilder();
		for(int i = 0; i < fileOptionsAsStr.length(); i++) {
			char nextChar = fileOptionsAsStr.charAt(i);
			if(insideDoubleQuotes) {
				if(nextChar == '"') {
					insideDoubleQuotes = false;
					result.add(current.toString().trim());
					current.setLength(0);
				} else {
					current.append(nextChar);
				}
			} else {
				if(nextChar == ' ') {
					if(current.length() > 0) {
						result.add(current.toString().trim());
						current.setLength(0);
					}
				} else if(nextChar == '"' && current.length() == 0) {
					insideDoubleQuotes = true;
				} else {
					current.append(nextChar);
				}
			}
		}
		if(current.length() > 0) {
			result.add(current.toString().trim());
		}
		return result.toArray(new String[result.size()]);
	}
}
