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

	public void setMaximized() {
		setWidth(-2);
		setHeight(-2);
	}

	public void setSize(int w, int h) {
		setWidth(w);
		setHeight(h);
	}

	public int getWidth() {
		return m_width;
	}

	public void setWidth(int width) {
		m_width = width;
	}

	public int getHeight() {
		return m_height;
	}

	public void setHeight(int height) {
		m_height = height;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	public boolean isResizable() {
		return m_resizable;
	}

	public void setResizable(boolean resizable) {
		m_resizable = resizable;
	}

	public boolean isShowScrollbars() {
		return m_showScrollbars;
	}

	public void setShowScrollbars(boolean showScrollbars) {
		m_showScrollbars = showScrollbars;
	}

	public boolean isShowToolbar() {
		return m_showToolbar;
	}

	public void setShowToolbar(boolean showToolbar) {
		m_showToolbar = showToolbar;
	}

	public boolean isShowLocation() {
		return m_showLocation;
	}

	public void setShowLocation(boolean showLocation) {
		m_showLocation = showLocation;
	}

	public boolean isShowDirectories() {
		return m_showDirectories;
	}

	public void setShowDirectories(boolean showDirectories) {
		m_showDirectories = showDirectories;
	}

	public boolean isShowStatus() {
		return m_showStatus;
	}

	public void setShowStatus(boolean showStatus) {
		m_showStatus = showStatus;
	}

	public boolean isShowMenubar() {
		return m_showMenubar;
	}

	public void setShowMenubar(boolean showMenubar) {
		m_showMenubar = showMenubar;
	}

	public boolean isCopyhistory() {
		return m_copyhistory;
	}

	public void setCopyhistory(boolean copyhistory) {
		m_copyhistory = copyhistory;
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
}
