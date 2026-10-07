package to.etc.domui.themes;

import to.etc.domui.themes.sass.SassThemeFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Compiles every variant of the winter theme and writes, to the directory given as the only
 * argument:
 * <ul>
 *	<li><code>[variant].css</code> for each variant of {@link SassThemeFactory#INSTANCE}, the
 *		compiled sheets, for diffing against an earlier run;</li>
 *	<li><code>same-colours.txt</code>, every colour declaration that the dark sheet has exactly as
 *		the light sheet has it - a light colour left on a dark page, unless it is something like
 *		<code>transparent</code> that is the same in both by nature.</li>
 * </ul>
 *
 * <p>Run it from the to.etc.domui module directory, e.g. with
 * <code>mvn21 -q exec:java -Dexec.classpathScope=test -Dexec.mainClass=to.etc.domui.themes.ThemeColorReport -Dexec.args=/tmp/x</code>.</p>
 */
final public class ThemeColorReport {
	static private final Pattern COLOUR = Pattern.compile("#[0-9a-fA-F]{3,8}\\b|rgba?\\(|hsla?\\(|\\b(white|black|red|yellow|goldenrod|navy|green|blue|grey|gray|silver|orange|cyan|magenta|purple)\\b");

	static private final Pattern NEUTRAL = Pattern.compile("^(transparent|inherit|none|currentColor)( !important)?$");

	private ThemeColorReport() {
	}

	public static void main(String[] args) throws Exception {
		if(args.length != 1)
			throw new IllegalArgumentException("usage: ThemeColorReport <output directory>");
		File out = new File(args[0]);
		out.mkdirs();

		Map<String, String> sheets = new LinkedHashMap<>();
		try(ThemeVariantCompiler c = new ThemeVariantCompiler(ThemeVariantCompiler.findThemeDir())) {
			for(IThemeVariant variant : SassThemeFactory.INSTANCE.getVariants()) {
				String name = variant.getVariantName();
				String css = c.compile(name);
				sheets.put(name, css);
				Files.writeString(new File(out, name + ".css").toPath(), css, StandardCharsets.UTF_8);
			}
		}
		String light = sheets.get(DefaultThemeVariant.INSTANCE.getVariantName());
		String dark = sheets.get(DarkThemeVariant.INSTANCE.getVariantName());

		Map<String, String> l = declarations(light);
		Map<String, String> d = declarations(dark);
		List<String> same = new ArrayList<>();
		int colours = 0;
		for(Map.Entry<String, String> e : l.entrySet()) {
			String v = e.getValue();
			if(!COLOUR.matcher(v).find() || v.contains("url("))
				continue;
			colours++;
			if(v.equals(d.get(e.getKey())) && !NEUTRAL.matcher(v).matches())
				same.add(e.getKey() + "\t" + v);
		}
		StringBuilder sb = new StringBuilder();
		sb.append("# ").append(colours).append(" colour declarations in the light sheet, ").append(same.size())
			.append(" of them the same in the dark sheet\n");
		for(String s : same)
			sb.append(s).append('\n');
		Files.writeString(new File(out, "same-colours.txt").toPath(), sb.toString(), StandardCharsets.UTF_8);
		System.out.println(colours + " colour declarations, " + same.size() + " the same in dark; written to " + out);
	}

	/**
	 * Every declaration of a compiled sheet, keyed by the selectors it is nested in plus the
	 * property; a property declared twice in one rule gets a #n suffix from the second on.
	 */
	static Map<String, String> declarations(String css) {
		css = css.replaceAll("(?s)/\\*.*?\\*/", "");
		Map<String, String> result = new LinkedHashMap<>();
		Deque<String> stack = new ArrayDeque<>();
		StringBuilder buf = new StringBuilder();
		for(char ch : css.toCharArray()) {
			switch(ch) {
				case '{' -> {
					stack.addLast(buf.toString().trim().replaceAll("\\s+", " "));
					buf.setLength(0);
				}
				case '}' -> {
					stack.pollLast();
					buf.setLength(0);
				}
				case ';' -> {
					int ix = buf.indexOf(":");
					if(ix > 0 && !stack.isEmpty()) {
						String key = String.join(" | ", stack) + "\t" + buf.substring(0, ix).trim();
						String k = key;
						for(int n = 1; result.containsKey(k); n++)
							k = key + "#" + n;
						result.put(k, buf.substring(ix + 1).trim());
					}
					buf.setLength(0);
				}
				default -> buf.append(ch);
			}
		}
		return result;
	}
}
