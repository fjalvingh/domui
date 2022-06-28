package to.etc.domui.fontawesome;

import to.etc.domui.component.misc.Icon;
import to.etc.util.Pair;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Map.entry;
import static java.util.Objects.requireNonNull;

/**
 */
final public class IconFromCss {
	private static Map<String, String> TRANSLATIONS = Map.ofEntries(
		entry("faCc", "faClosedCaptioning"),
		entry("faEercast", "faSellcast"),
		entry("faFilesO", "faCopy"),
		entry("faFlash", "faBolt"),
		entry("faFloppyO", "faSave"),
		entry("faGlass", "faGlassMartini"),
		entry("faMeanpath", "faFontAwesome"),
		entry("faMoney", "faMoneyBillAlt"),
		entry("faPictureO", "faImage"),
		entry("faSend%", "faPaperPlane%"),
		entry("faHandO%", "faHandPoint%"),
		entry("faTripadvisor", "faSuitcase"),
		entry("faYoutubePlay", "faYoutube")
	);

	private static Map<String, String> TRANSLATIONS_SUFFIXES = Map.ofEntries(
		entry("%O", "%"),
		entry("%ODown", "%Down"),
		entry("%OLeft", "%Left"),
		entry("%ORight", "%Right"),
		entry("%OUp", "%Up"),
		entry("%Square", "%"),
		entry("%ONotch", "%Notch"),
		entry("%Thin", "%"),
		entry("%Official", "%")
	);

	private final Reader m_r;

	public IconFromCss(Reader r) {
		m_r = r;
	}

	public static void main(String[] args) throws Exception {
		File fa6AllCssFile;
		if(args.length != 1) {
			System.out.println("Add the font awesome 6 all.css file path as a parameter. Download the free resource bundle and put here path to all icons css.");
			//I.E.: /home/vmijic/Downloads/fontawesome-free-6.1.1-web/css/all.css
			System.out.println("By default we take one from resources");
			fa6AllCssFile = new File(IconFromCss.class.getResource("/generator/all.css").toURI());
		}else {
			fa6AllCssFile = new File(args[0]);
		}
		File f = new File(".").getAbsoluteFile();
		System.out.println("Path " + f);
		File src = new File(f, "domui/integrations/fontawesome6free/src/main/java/to/etc/domui/fontawesome/FaIcon.java");
		if(! src.exists()) {
			throw new IllegalStateException("Cannot find java source as " + src);
		}

		Properties props;
		try(InputStream r = IconFromCss.class.getResourceAsStream("/generator/iconFaStyles.properties")) {
			props = new Properties();
			props.load(r);
		}

		List<Pair<String, Integer>> namesAndCodes = loadNames(fa6AllCssFile);

		renderOutput(src, namesAndCodes, props);
	}

	static private final String ICON_START = "///--- BEGIN ICONS";
	static private final String ICON_END = "///--- END ICONS";
	static private final String MAP_START = "///--- BEGIN MAP";
	static private final String MAP_END = "///--- END MAP";

	enum InSection {
		COPY, ICONS, MAP
	}

	private static void renderOutput(File src, List<Pair<String, Integer>> namesAndCodes, Properties props) throws Exception {
		String outPath = "/tmp/FaIcon.java";
		try(LineNumberReader r = new LineNumberReader(new InputStreamReader(new FileInputStream(src), "utf-8"))) {
			try(OutputStreamWriter of = new OutputStreamWriter(new FileOutputStream(outPath), "utf-8")) {
				String s;

				InSection section = InSection.COPY;
				while(null != (s = r.readLine())) {
					switch(section) {
						default:
							throw new IllegalStateException(section + "??");

						case COPY:
							//-- ALWAYS copy the line
							of.write(s);
							of.write("\n");

							if(s.contains(ICON_START)) {
								section = InSection.ICONS;
								renderIcons(of, namesAndCodes, props);
							} else if(s.contains(MAP_START)) {
								section = InSection.MAP;
								renderMap(of, namesAndCodes);
							}
							break;

						case ICONS:
							if(s.contains(ICON_END)) {
								//-- ALWAYS copy the line
								of.write(s);
								of.write("\n");
								section = InSection.COPY;
							}
							break;

						case MAP:
							if(s.contains(MAP_END)) {
								//-- ALWAYS copy the line
								of.write(s);
								of.write("\n");
								section = InSection.COPY;
							}
							break;
					}
				}
			}
		}
		System.out.println("Written output as: " + outPath);
	}

	private static void renderMap(OutputStreamWriter of, List<Pair<String, Integer>> namesAndCodes) throws Exception {
		of.write("\tstatic public void initializeIcons() {\n");

		//-- Get a set of Java names from names
		Set<String> nameSet = namesAndCodes.stream()
			.map(a -> alterName(a.get1()))
			.collect(Collectors.toSet());

		for(Icon icon : Icon.values()) {
			String newName = icon.name();
			if(! nameSet.contains(newName)) {
				System.out.print("missing icon " + newName);
				newName = transformFirst(newName);
				if(!nameSet.contains(newName)) {
					newName = transformSecond(newName);
				}
				if(nameSet.contains(newName)) {
					System.out.println(", replaced with  " + newName);
				}else {
					throw new IllegalStateException("Missing icon " + icon.name());
				}
			}
			of.write("\t\tIcon.setIcon(Icon.");
			of.write(icon.name());
			of.write(", FaIcon.");
			of.write(newName);
			of.write(");\n");
		}
		of.write("\t}\n");
	}

	private static String transformFirst(final String newName) {
		String translation = TRANSLATIONS.get(newName);
		if(null != translation) {
			return translation;
		}
		String startWithKey = TRANSLATIONS.keySet().stream().filter(k -> k.endsWith("%") && newName.startsWith(k.substring(0, k.length() -1))).findFirst().orElse(null);
		if(null != startWithKey) {
			String startWithValue = TRANSLATIONS.get(startWithKey);
			return startWithValue.replace("%", newName.substring(startWithKey.length() - 1));
		}
		return newName;
	}

	private static String transformSecond(final String newName) {
		String endsWithSuffix = TRANSLATIONS_SUFFIXES.keySet().stream().filter(k -> k.startsWith("%") && newName.endsWith(k.substring(1))).findFirst().orElse(null);
		if(null != endsWithSuffix) {
			String endsWithValue = TRANSLATIONS_SUFFIXES.get(endsWithSuffix);
			return endsWithValue.replace("%", newName.substring(0, newName.length() - endsWithSuffix.length() + 1));
		}
		return newName;
	}

	private static void renderIcons(OutputStreamWriter of, List<Pair<String, Integer>> namesAndCodes, Properties props) throws Exception {
		try(InputStream faSolidIs = IconFromCss.class.getResourceAsStream("/META-INF/resources/webfonts/fa-solid-900.ttf");
			InputStream faRegularIs = IconFromCss.class.getResourceAsStream("/META-INF/resources/webfonts/fa-regular-400.ttf");
			InputStream faBrandsIs = IconFromCss.class.getResourceAsStream("/META-INF/resources/webfonts/fa-brands-400.ttf")) {
			Font fontRegular = Font.createFont(Font.TRUETYPE_FONT, faRegularIs);
			Font fontSolid = Font.createFont(Font.TRUETYPE_FONT, faSolidIs);
			Font fontBrands = Font.createFont(Font.TRUETYPE_FONT, faBrandsIs);

			for(Pair<String, Integer> nameAndCode : namesAndCodes) {
				String name = nameAndCode.get1();
				int code = requireNonNull(nameAndCode.get2()).intValue();
				String faStyle = props.getProperty(name, null);
				if(null == faStyle) {
					if(fontRegular.canDisplay(code)) {
						faStyle = "far";
					}else if (fontSolid.canDisplay(code)) {
						faStyle = "fas";
					}else if (fontBrands.canDisplay(code)) {
						faStyle = "fab";
					}else {
						faStyle = "na";
					}
				}
				String constName = alterName(name);
				//of.write("\t" + constName + "(\"" + name + "\",\"" + mainClass + "\"),\n");
				of.write("\tpublic static final FaIcon " + constName + " = register(new FaIcon(\"" + name + "\",\"" + faStyle + "\"));\n");
			}
		}
	}

	private static List<Pair<String, Integer>> loadNames(File file) throws Exception {
		try(Reader r = new InputStreamReader(new FileInputStream(file), "utf-8") ) {
			return new IconFromCss(r).render();
		}
	}

	private int next() throws Exception {
		int t = m_r.read();
		return t;
	}

	enum State {
		findDot,
		scanName,
		waitBody,
		waitContent,
		waitContentEnd,
	}

	private State m_state = State.findDot;

	private List<Pair<String, Integer>> render() throws Exception {
		int t;

		List<Pair<String, Integer>> namesAndCodes = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		String name = null;
		while((t = next()) != -1) {
			switch(m_state) {
				default:
					throw new IllegalStateException(m_state + "?");

				case findDot:
					if(t == '.') {
						m_state = State.scanName;
					}
					break;

				case scanName:
					if(t == ':') {
						m_state = State.waitBody;
						name = sb.toString();
						sb.setLength(0);
					} else if(t == '}') {
						sb.setLength(0);
						m_state = State.findDot;
					} else if(t == '.') {
						sb.setLength(0);
					} else if(! Character.isWhitespace(t)){
						sb.append((char) t);
					}
					break;

				case waitBody:
					if(t == '{') {
						m_state = State.waitContent;
					} else if(t == '}') {
						sb.setLength(0);
						m_state = State.findDot;
					}
					break;

				case waitContent:
					if(t == ':') {
						if("content".equalsIgnoreCase(sb.toString()) && name != null && name.length() > 0) {
							sb.setLength(0);
							m_state = State.waitContentEnd;
							break;
						}
						m_state = State.findDot;
						sb.setLength(0);
						name = null;
					} else if(t == '}') {
						sb.setLength(0);
						m_state = State.findDot;
					} else if(! Character.isWhitespace(t)) {
						sb.append((char) t);
					}
					break;

				case waitContentEnd:
					if(t == ';') {
						if(sb.toString().startsWith("\"\\") && sb.toString().endsWith("\"") && name != null && name.length() > 0) {
							Integer code = Integer.parseInt(sb.toString().replace("\"\\", "").replace("\"", ""), 16);
							namesAndCodes.add(new Pair<>(name, code));
						}
						m_state = State.findDot;
						sb.setLength(0);
						name = null;
					} else if(! Character.isWhitespace(t)) {
						sb.append((char) t);
					}
					break;
			}
		}

		Collections.sort(namesAndCodes, Comparator.comparing(Pair::get1));
		System.out.println("Got " + namesAndCodes.size() + " names with font codes");
		for(Pair<String, Integer> s : namesAndCodes) {
			System.out.println(alterName(s.get1()) + "(\"" + s + "\") - " + s.get2() + ",");
		}
		return namesAndCodes;
	}

	static String alterName(String faname) {
		if(! faname.startsWith("fa-"))
			faname = "fa-" + faname;

		StringBuilder sb = new StringBuilder();
		boolean uc = false;
		for(int i = 0; i < faname.length(); i++) {
			char c = faname.charAt(i);
			if(uc) {
				sb.append(Character.toUpperCase(c));
				uc = false;
			} else if(c == '-') {
				uc = true;
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
}
