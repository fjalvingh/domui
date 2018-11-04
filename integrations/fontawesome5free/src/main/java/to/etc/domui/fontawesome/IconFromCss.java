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

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-10-18.
 */
final public class IconFromCss {
	private final Reader m_r;

	private int m_t;

	public IconFromCss(Reader r) {
		m_r = r;
	}

	public static void main(String[] args) throws Exception {
		if(args.length != 1) {
			System.out.println("Add the .css file path as a parameter");
			System.exit(10);
		}
		File f = new File(".").getAbsoluteFile();
		System.out.println("Path " + f);
		File src = new File(f, "integrations/fontawesome5free/src/main/java/to/etc/domui/fontawesome/FaIcon.java");
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
								renderIcons(of, names, map);
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
				if(! nameSet.contains(newName))
					throw new IllegalStateException("Missing icon " + icon.name());
			}
			of.write("\t\tIcon.setIcon(Icon.");
			of.write(icon.name());
			of.write(", FaIcon.");
			of.write(newName);
			of.write(");\n");
		}
		of.write("\t}\n");
	}

	private static void renderIcons(OutputStreamWriter of, List<String> names, Map<String, Ren> map) throws Exception {
		Map<String, Ren> by = new HashMap<>();
		map.forEach((key, ren) -> by.put(ren.m_new, ren));

		for(String name : names) {
			String key = name;

			String mainClass = "fa";
			Ren ren = by.get(key);
			if(null != ren) {
				//System.out.println("Got name");
				if(!"fa".equals(ren.m_prefix) && !"fas".equals(ren.m_prefix))
					mainClass = ren.m_prefix;
			}

			of.write("\t" + alterName(name) + "(\"" + name + "\",\"" + mainClass + "\"),\n");
		}
	}

	private static List<String> loadNames(File file) throws Exception {
		try(Reader r = new InputStreamReader(new FileInputStream(file), "utf-8") ) {
			return new IconFromCss(r).render();
		}
	}

	private int next() throws Exception {
		m_t = m_r.read();
		return m_t;
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
		try(LineNumberReader r = new LineNumberReader(new InputStreamReader(new FileInputStream(mapf), "utf-8")) ) {
			String s;

			while(null != (s = r.readLine())) {
				decodeLine(map, s);
			}
		}
		System.out.println("Got " + map.size() + " moved icons");
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
