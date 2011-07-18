package to.etc.domui.component.layout;

import javax.annotation.*;

import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * Mostly a more modern duplication of the {@link FloatingWindow} class, this is a different
 * way of handling floating windows by explicitly defining a content area that is responsible
 * for it's own construction.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 18, 2011
 */
public class Dialog extends FloatingDiv {
	/** The container holding this dialog's title bar. This is also the drag source. */
	private NodeContainer m_titleBar;

	/** The container holding the content area for this dialog. */
	private Div m_content;

	/** The title in the title bar. */
	@Nullable
	private String m_windowTitle;

	private boolean m_closable = true;

	/** The close button in the title bar. */
	private Img m_closeButton;

	/** If present, an image to use as the icon inside the title bar. */
	private Img m_titleIcon;

	IClicked<Dialog> m_onClose;

	private static final int WIDTH = 640;

	private static final int HEIGHT = 400;

	public Dialog(boolean modal, boolean resizable, String title) {
		super(modal, resizable);
		if(title != null)
			setWindowTitle(title);
		init();
	}

	public Dialog(String title) {
		this(true, title);
	}

	/**
	 * Create a floating window with the specified title in the title bar.
	 * @param title
	 */
	public Dialog(boolean modal, @Nullable String title) {
		this(modal, false, title);
	}

	/**
	 * Create the floater.
	 * @see to.etc.domui.dom.html.NodeBase#createContent()
	 */
	@Override
	public void createContent() throws Exception {
		delegateTo(null);
		setCssClass("ui-fw");

		if(getWidth() == null)
			setWidth(WIDTH + "px");
		if(getHeight() == null)
			setHeight(HEIGHT + "px");
		if(getZIndex() <= 0) {
			setZIndex(100);
		}
		if(getTestID() == null) {
			setTestID("popup_" + getZIndex());
		}
		setPosition(PositionType.FIXED);

		int width = DomUtil.pixelSize(getWidth());
		if(width == -1)
			width = WIDTH;

		// center floating window horizontally on screen
		setMarginLeft("-" + width / 2 + "px");

		//-- Construct the title bar
		createTitleBar();
		super.add(1, m_content);
		delegateTo(m_content);

		//vmijic 20091125 - since z-index is dynamic value, correct value has to be used also in js.
		appendCreateJS("$('#" + getActualID() + "').draggable({" + "ghosting: false, zIndex:" + getZIndex() + ", handle: '#" + m_titleBar.getActualID() + "'});");
	}

	private void init() {
		m_content = new Div();
		m_content.setCssClass("ui-fw-c");
		setErrorFence();
		delegateTo(m_content);
	}

	/**
	 * Set an icon for the title bar, using the absolute path to a web resource. If the name is prefixed
	 * with THEME/ it specifies an image from the current THEME's directory.
	 * @param ico
	 */
	public void setIcon(String ico) {
		createIcon().setSrc(ico);
	}

	private Img createIcon() {
		if(m_titleIcon == null) {
			m_titleIcon = new Img();
			m_titleIcon.setBorder(0);
			if(m_titleBar != null) {
				//Since IE has bug that floater object is rendered under previous sibling, close button must be rendered before any other element in title bar.
				if(m_closeButton != null && m_titleBar.getChildCount() > 0 && m_titleBar.getChild(0) == m_closeButton) {
					m_titleBar.add(1, m_titleIcon);
				} else {
					m_titleBar.add(0, m_titleIcon);
				}
			}
		}
		return m_titleIcon;
	}

	/**
	 * Create the title bar for the floater.
	 * Also replaces existing title bar in case that new is set.
	 * @return
	 */
	protected NodeContainer createTitleBar() {
		Div ttl = new Div();
		if(m_titleBar != null) {
			//if old title bar exists
			NodeContainer parent = m_titleBar.getParent();
			if(parent != null) {
				//replace old title bar if already added to page
				m_titleBar.replaceWith(ttl);
			}
		}
		m_titleBar = ttl;
		if(null == ttl.getParent()) {
			//in case that title bar is still not added to page, add it as first item
			super.add(0, ttl);
		}
		ttl.setCssClass("ui-fw-ttl");
		if(m_closable) {
			m_closeButton = new Img();
			m_closeButton.setSrc("THEME/close.png");
			m_closeButton.setFloat(FloatType.RIGHT);
			//some margin fixes have to be applied with css
			m_closeButton.setCssClass("ui-fw-btn-close");
			ttl.add(m_closeButton);
			m_closeButton.setClicked(new IClicked<NodeBase>() {
				@Override
				public void clicked(NodeBase b) throws Exception {
					closePressed();
				}
			});
		}
		if(m_titleIcon != null)
			ttl.add(m_titleIcon);
		ttl.add(getWindowTitle());
		return ttl;
	}

	/**
	 * Close the window !AND CALL THE CLOSE HANDLER!.
	 *
	 * @throws Exception
	 */
	public void closePressed() throws Exception {
		close();
		if(m_onClose != null)
			m_onClose.clicked(Dialog.this);
	}

	/**
	 * Close this floater and cause it to be destroyed from the UI.
	 */
	public void close() {
		remove();
	}

	/**
	 * Returns T if the window can be closed using a close button on the title bar.
	 * @return
	 */
	public boolean isClosable() {
		return m_closable;
	}

	/**
	 * When set to TRUE, the floater will display a close button on it's title bar, and will close
	 * if that thingy is pressed.
	 * @param closable
	 */
	public void setClosable(boolean closable) {
		if(m_closable == closable)
			return;
		m_closable = closable;
	}

	/**
	 * Get the current "onClose" handler.
	 * @return
	 */
	public IClicked<Dialog> getOnClose() {
		return m_onClose;
	}

	/**
	 * Set a Clicked handler to be called when this floater is closed by it's close button. This does <i>not</i> get
	 * called when the floater is closed programmatically (i.e. when close() is called).
	 *
	 * @param onClose
	 */
	public void setOnClose(IClicked<Dialog> onClose) {
		m_onClose = onClose;
	}

	/**
	 * Return the floater's title bar title string.
	 * @return
	 */
	public String getWindowTitle() {
		return m_windowTitle;
	}

	/**
	 * Set the floater's title bar string.
	 * @param windowTitle
	 */
	public void setWindowTitle(String windowTitle) {
		if(DomUtil.isEqual(windowTitle, m_windowTitle))
			return;
		m_windowTitle = windowTitle;
		if(m_titleBar != null)
			createTitleBar();
	}
}
