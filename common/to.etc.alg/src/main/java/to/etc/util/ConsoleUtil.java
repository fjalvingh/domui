package to.etc.util;

import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Date;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-6-19.
 */
public class ConsoleUtil {
	static private final FastDateFormat m_logFmt = FastDateFormat.getInstance("HH:mm:ss.SSS");

	static public String getLogTime() {
		return m_logFmt.format(new Date());
	}

	public static final String RESET = "\033[0m";  // Text Reset

	// Regular Colors
	public static final String BLACK = "\033[0;30m";   // BLACK
	public static final String RED = "\033[0;31m";     // RED
	public static final String GREEN = "\033[0;32m";   // GREEN
	public static final String YELLOW = "\033[0;33m";  // YELLOW
	public static final String BLUE = "\033[0;34m";    // BLUE
	public static final String PURPLE = "\033[0;35m";  // PURPLE
	public static final String CYAN = "\033[0;36m";    // CYAN
	public static final String WHITE = "\033[0;37m";   // WHITE

	// Bold
	public static final String BLACK_BOLD = "\033[1;30m";  // BLACK
	public static final String RED_BOLD = "\033[1;31m";    // RED
	public static final String GREEN_BOLD = "\033[1;32m";  // GREEN
	public static final String YELLOW_BOLD = "\033[1;33m"; // YELLOW
	public static final String BLUE_BOLD = "\033[1;34m";   // BLUE
	public static final String PURPLE_BOLD = "\033[1;35m"; // PURPLE
	public static final String CYAN_BOLD = "\033[1;36m";   // CYAN
	public static final String WHITE_BOLD = "\033[1;37m";  // WHITE

	// Underline
	public static final String BLACK_UNDERLINED = "\033[4;30m";  // BLACK
	public static final String RED_UNDERLINED = "\033[4;31m";    // RED
	public static final String GREEN_UNDERLINED = "\033[4;32m";  // GREEN
	public static final String YELLOW_UNDERLINED = "\033[4;33m"; // YELLOW
	public static final String BLUE_UNDERLINED = "\033[4;34m";   // BLUE
	public static final String PURPLE_UNDERLINED = "\033[4;35m"; // PURPLE
	public static final String CYAN_UNDERLINED = "\033[4;36m";   // CYAN
	public static final String WHITE_UNDERLINED = "\033[4;37m";  // WHITE

	// Background
	public static final String BLACK_BACKGROUND = "\033[40m";  // BLACK
	public static final String RED_BACKGROUND = "\033[41m";    // RED
	public static final String GREEN_BACKGROUND = "\033[42m";  // GREEN
	public static final String YELLOW_BACKGROUND = "\033[43m"; // YELLOW
	public static final String BLUE_BACKGROUND = "\033[44m";   // BLUE
	public static final String PURPLE_BACKGROUND = "\033[45m"; // PURPLE
	public static final String CYAN_BACKGROUND = "\033[46m";   // CYAN
	public static final String WHITE_BACKGROUND = "\033[47m";  // WHITE

	// High Intensity
	public static final String BLACK_BRIGHT = "\033[0;90m";  // BLACK
	public static final String RED_BRIGHT = "\033[0;91m";    // RED
	public static final String GREEN_BRIGHT = "\033[0;92m";  // GREEN
	public static final String YELLOW_BRIGHT = "\033[0;93m"; // YELLOW
	public static final String BLUE_BRIGHT = "\033[0;94m";   // BLUE
	public static final String PURPLE_BRIGHT = "\033[0;95m"; // PURPLE
	public static final String CYAN_BRIGHT = "\033[0;96m";   // CYAN
	public static final String WHITE_BRIGHT = "\033[0;97m";  // WHITE

	// Bold High Intensity
	public static final String BLACK_BOLD_BRIGHT = "\033[1;90m"; // BLACK
	public static final String RED_BOLD_BRIGHT = "\033[1;91m";   // RED
	public static final String GREEN_BOLD_BRIGHT = "\033[1;92m"; // GREEN
	public static final String YELLOW_BOLD_BRIGHT = "\033[1;93m";// YELLOW
	public static final String BLUE_BOLD_BRIGHT = "\033[1;94m";  // BLUE
	public static final String PURPLE_BOLD_BRIGHT = "\033[1;95m";// PURPLE
	public static final String CYAN_BOLD_BRIGHT = "\033[1;96m";  // CYAN
	public static final String WHITE_BOLD_BRIGHT = "\033[1;97m"; // WHITE

	// High Intensity backgrounds
	public static final String BLACK_BACKGROUND_BRIGHT = "\033[0;100m";// BLACK
	public static final String RED_BACKGROUND_BRIGHT = "\033[0;101m";// RED
	public static final String GREEN_BACKGROUND_BRIGHT = "\033[0;102m";// GREEN
	public static final String YELLOW_BACKGROUND_BRIGHT = "\033[0;103m";// YELLOW
	public static final String BLUE_BACKGROUND_BRIGHT = "\033[0;104m";// BLUE
	public static final String PURPLE_BACKGROUND_BRIGHT = "\033[0;105m"; // PURPLE
	public static final String CYAN_BACKGROUND_BRIGHT = "\033[0;106m";  // CYAN
	public static final String WHITE_BACKGROUND_BRIGHT = "\033[0;107m";   // WHITE

	static private final String[] ROTCOLORS = {
		GREEN, PURPLE, CYAN, GREEN_BRIGHT, PURPLE_BRIGHT
	};

	static public String getLogTimeAndThread() {
		StringBuilder sb = new StringBuilder();
		sb.append(m_logFmt.format(new Date()));
		while(sb.length() < 13)
			sb.append(' ');

		String name = Thread.currentThread().getName();
		int len = name.length();
		if(len > 10) {
			name = name.substring(0, 10);
			len = 10;
		}
		sb.append(name);
		while(len++ < 10)
			sb.append(' ');

		return  sb.toString();
	}
	static public void consoleLog(String... segments) {
		consoleLog(0, segments);
	}

	static public void consoleError(String... segments) {
		consoleLog(2, segments);
	}

	static public void consoleWarning(String... segments) {
		consoleLog(1, segments);
	}

	static public void consoleLog(int type, String... segments) {
		StringBuilder sb = new StringBuilder();
		sb.append(BLUE);
		append(sb, m_logFmt.format(new Date()), 12);
		sb.append(' ');
		sb.append(CYAN);
		String name = Thread.currentThread().getName();
		append(sb, name, 10);
		sb.append(' ');

		for(int i = 0; i < segments.length; i++) {
			String segment = segments[i];

			if(i == segments.length - 1) {
				//-- Last part: the message
				switch(type) {
					default:
						sb.append(WHITE);
						break;

					case 1:
						sb.append(YELLOW);
						break;

					case 2:
						sb.append(RED);
						break;
				}
				sb.append(" ").append(segment.replace("\n", "\n  ")).append(RESET);
			} else {
				sb.append(ROTCOLORS[i % ROTCOLORS.length]);
				append(sb, segment, 15);
			}
		}
		System.out.println(sb.toString());
	}


	private static void append(StringBuilder sb, String s, int trunc) {
		int sbl = sb.length() + trunc;
		if(s.length() > trunc) {
			sb.append(s, 0, trunc);
		} else {
			sb.append(s);
			while(sb.length() < sbl)
				sb.append(' ');
		}
	}
}
