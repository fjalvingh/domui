package to.etc.domui.fontawesome;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-10-18.
 */
public class IconFromCss {
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
		renderCss(new File(args[0]));
	}

	private static void renderCss(File file) throws Exception {
		try(Reader r = new InputStreamReader(new FileInputStream(file), "utf-8") ) {
			new IconFromCss(r).render();
		}
	}

	private int next() throws Exception {
		m_t = m_r.read();
		return m_t;
	}

	private int LA() {
		return m_t;
	}

	private void accept() throws Exception {
		next();
	}

	enum State {
		findDot,
		scanName,
		waitBody,
		waitContent
	}

	private State m_state = State.findDot;

	private void render() throws Exception {
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
					if(t == '{' || t == ':') {
						m_state = State.waitBody;
						name = sb.toString();
						sb.setLength(0);
					} else if(! Character.isWhitespace(t)){
						sb.append((char) t);
					}
					break;

				case waitBody:
					if(t == '{') {
						m_state = State.waitContent;
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
					} else if(! Character.isWhitespace(t)) {
						sb.append((char) t);
					}
					break;
			}
		}

		Collections.sort(names);
		for(String s : names) {
			System.out.println(alterName(s) + "(\"" + s + "\"),");
		}
		System.out.println("Got " + names.size() + " names");
	}

	static String alterName(String faname) {
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
