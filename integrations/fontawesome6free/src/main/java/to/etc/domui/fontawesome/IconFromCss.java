package to.etc.domui.fontawesome;

import to.etc.domui.component.misc.Icon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Map.entry;

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
		entry("faYoutubePlay", "faPlayCircle")
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
		if(args.length != 1) {
			System.out.println("Add the .css file path as a parameter. Download the free resource bundle and put here path to all icons css.");
			//I.E.: /home/vmijic/Downloads/fontawesome-free-6.1.1-web/css/all.css
			System.exit(10);
		}
		File f = new File(".").getAbsoluteFile();
		System.out.println("Path " + f);
		File src = new File(f, "domui/integrations/fontawesome6free/src/main/java/to/etc/domui/fontawesome/FaIcon.java");
		if(! src.exists())
			throw new IllegalStateException("Cannot find java source as " + src);

		File file = new File(args[0]);
		List<String> names = loadNames(file);
		Map<String, Ren> map = loadMap(file);

		renderOutput(src, names, map);
	}

	static private final String ICON_START = "///--- BEGIN ICONS";
	static private final String ICON_END = "///--- END ICONS";
	static private final String MAP_START = "///--- BEGIN MAP";
	static private final String MAP_END = "///--- END MAP";

	enum InSection {
		COPY, ICONS, MAP
	}

	private static void renderOutput(File src, List<String> names, Map<String, Ren> map) throws Exception {
		try(LineNumberReader r = new LineNumberReader(new InputStreamReader(new FileInputStream(src), "utf-8"))) {
			try(OutputStreamWriter of = new OutputStreamWriter(new FileOutputStream(new File("/tmp/FaIcon.java")), "utf-8")) {
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
								renderIcons(of, names);
							} else if(s.contains(MAP_START)) {
								section = InSection.MAP;
								renderMap(of, names, map);
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
	}

	private static void renderMap(OutputStreamWriter of, List<String> names, Map<String, Ren> map) throws Exception {
		of.write("\tstatic public void initializeIcons() {\n");

		//-- Get a set of Java names from names
		Set<String> nameSet = names.stream()
			.map(a -> alterName(a))
			.collect(Collectors.toSet());

		//-- Now create a map of (java oldname, ren)
		Map<String, Ren> oldMap = map.values().stream()
			.collect(Collectors.toMap(a -> alterName(a.m_old), a -> a));

		for(Icon icon : Icon.values()) {
			String newName = icon.name();
			Ren ren = oldMap.get(icon.name());
			if(null != ren) {
				newName = alterName(ren.m_new);
			} else {
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

	private static void renderIcons(OutputStreamWriter of, List<String> names) throws Exception {
		for(String name : names) {
			String key = name;

			String mainClass = "far";
			//of.write("\t" + alterName(name) + "(\"" + name + "\",\"" + mainClass + "\"),\n");
			of.write("\tpublic static FaIcon " + alterName(name) + " = new FaIcon(\"" + name + "\",\"" + mainClass + "\");\n");
		}
	}

	private static List<String> loadNames(File file) throws Exception {
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
		waitContent
	}

	private State m_state = State.findDot;

	private List<String> render() throws Exception {
		int t;

		List<String> names = new ArrayList<>();
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
							names.add(name);
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
			}
		}

		Collections.sort(names);
		System.out.println("Got " + names.size() + " names");
		for(String s : names) {
			System.out.println(alterName(s) + "(\"" + s + "\"),");
		}
		return names;
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


	private static Map<String, Ren> loadMap(File f) throws Exception {
		File mapf = new File(f.getParent(), f.getName() + ".moves");

		Map<String, Ren> map = new HashMap<>();
		if(mapf.exists()) {

			try(LineNumberReader r = new LineNumberReader(new InputStreamReader(new FileInputStream(mapf), "utf-8"))) {
				String s;

				while(null != (s = r.readLine())) {
					decodeLine(map, s);
				}
			}
			System.out.println("Got " + map.size() + " moved icons");
		}
		return map;
	}

	private static void decodeLine(Map<String, Ren> map, String s) {
		s = s.trim();
		if(s.length() == 0)
			return;

		String[] frag = s.split("\\s+");
		if(frag.length != 4)
			throw new IllegalStateException("Bad fragment in line: " + s + " - must have 4 columns");
		Ren ren = new Ren("fa-" + frag[0], "fa-" + frag[1], frag[2]);
		map.put(frag[0], ren);
	}

	static private final class Ren {
		public final String m_old;
		public final String m_new;
		public final String m_prefix;

		public Ren(String old, String aNew, String prefix) {
			m_old = old;
			m_new = aNew;
			m_prefix = prefix;
		}
	}
}
