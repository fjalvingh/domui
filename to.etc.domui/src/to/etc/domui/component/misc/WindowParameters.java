package to.etc.domui.component.misc;

/**
 * UNSTABLE INTERFACE - DO NOT USE Parameters for an OpenWindow call.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 3, 2008
 */
public class WindowParameters {
	private int m_width = -1;

	private int m_height = -1;

	private String m_name;

	private boolean m_resizable;

	private boolean m_showScrollbars;

	private boolean m_showToolbar;

	private boolean m_showLocation;

	private boolean m_showDirectories;

	private boolean m_showStatus;

	private boolean m_showMenubar;

	private boolean m_copyhistory;

	public WindowParameters() {
		m_resizable = true;
		m_copyhistory = false;
		m_showDirectories = true;
		m_showLocation = true;
		m_showMenubar = true;
		m_showScrollbars = true;
		m_showStatus = true;
		m_showToolbar = true;
	}

	public WindowParameters setMaximized() {
		setWidth(-2);
		setHeight(-2);
		return this;
	}

	public WindowParameters setSize(int w, int h) {
		setWidth(w);
		setHeight(h);
		return this;
	}

	public int getWidth() {
		return m_width;
	}

	public WindowParameters setWidth(int width) {
		m_width = width;
		return this;
	}

	public int getHeight() {
		return m_height;
	}

	public WindowParameters setHeight(int height) {
		m_height = height;
		return this;
	}

	public String getName() {
		return m_name;
	}

	public WindowParameters setName(String name) {
		m_name = name;
		return this;
	}

	public boolean isResizable() {
		return m_resizable;
	}

	public WindowParameters setResizable(boolean resizable) {
		m_resizable = resizable;
		return this;
	}

	public boolean isShowScrollbars() {
		return m_showScrollbars;
	}

	public WindowParameters setShowScrollbars(boolean showScrollbars) {
		m_showScrollbars = showScrollbars;
		return this;
	}

	public boolean isShowToolbar() {
		return m_showToolbar;
	}

	public WindowParameters setShowToolbar(boolean showToolbar) {
		m_showToolbar = showToolbar;
		return this;
	}

	public boolean isShowLocation() {
		return m_showLocation;
	}

	public WindowParameters setShowLocation(boolean showLocation) {
		m_showLocation = showLocation;
		return this;
	}

	public boolean isShowDirectories() {
		return m_showDirectories;
	}

	public WindowParameters setShowDirectories(boolean showDirectories) {
		m_showDirectories = showDirectories;
		return this;
	}

	public boolean isShowStatus() {
		return m_showStatus;
	}

	public WindowParameters setShowStatus(boolean showStatus) {
		m_showStatus = showStatus;
		return this;
	}

	public boolean isShowMenubar() {
		return m_showMenubar;
	}

	public WindowParameters setShowMenubar(boolean showMenubar) {
		m_showMenubar = showMenubar;
		return this;
	}

	public boolean isCopyhistory() {
		return m_copyhistory;
	}

	public WindowParameters setCopyhistory(boolean copyhistory) {
		m_copyhistory = copyhistory;
		return this;
	}


	static public WindowParameters createFixed(int w, int h, String name) {
		WindowParameters p = new WindowParameters();
		p.setSize(w, h);
		p.setName(name);
		p.setCopyhistory(false);
		p.setResizable(false);
		p.setShowDirectories(false);
		p.setShowLocation(true);
		p.setShowMenubar(false);
		p.setShowScrollbars(false);
		p.setShowStatus(false);
		p.setShowToolbar(false);
		return p;
	}

	static public WindowParameters createResizeableScrollable(int w, int h, String name) {
		WindowParameters p = new WindowParameters();
		p.setSize(w, h);
		p.setName(name);
		p.setCopyhistory(false);
		p.setResizable(true);
		p.setShowDirectories(false);
		p.setShowLocation(true);
		p.setShowMenubar(false);
		p.setShowScrollbars(true);
		p.setShowStatus(false);
		p.setShowToolbar(false);
		return p;
	}
}
