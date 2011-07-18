package to.etc.domui.component.layout;

import javax.annotation.*;

import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

public class Window extends FloatingDiv {
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

	private IClicked<NodeBase> m_onClose;

	private Div m_topContent;

	private Div m_bottomContent;

	public Window(boolean modal, boolean resizable, int width, int height, @Nullable String title) {
		super(modal, resizable, width, height);
		if(null != title)
			setWindowTitle(title);
		init();
	}

	public Window(String title) {
		super(true, false);
		if(null != title)
			setWindowTitle(title);
		init();
	}

	private void init() {
		m_content = new Div();
		m_content.setCssClass("ui-flw-c");
		m_content.setStretchHeight(true);
		m_topContent = new Div();
		m_topContent.setCssClass("ui-flw-tc");
		m_bottomContent = new Div();
		m_bottomContent.setCssClass("ui-flw-bc");
		setErrorFence();
		delegateTo(m_content);
	}

	/**
	 * This creates the title bar frame.
	 * @see to.etc.domui.dom.html.NodeContainer#createFrame()
	 */
	@Override
	protected void createFrame() {
		m_titleBar = new Div();
		add(m_titleBar);
		createTitleBar();
		add(m_topContent);
		add(m_content);
		add(m_bottomContent);
		setErrorFence();

		//vmijic 20091125 - since z-index is dynamic value, correct value has to be used also in js.
		appendCreateJS("$('#" + getActualID() + "').draggable({" + "ghosting: false, zIndex:" + getZIndex() + ", handle: '#" + m_titleBar.getActualID() + "'});");
		delegateTo(m_content);
	}

	/**
	 * Create the title bar for the floater.
	 * Also replaces existing title bar in case that new is set.
	 * @return
	 */
	protected void createTitleBar() {
		if(m_titleBar == null)
			return;

		//-- The titlebar div must not change after creation because it is the drag handle.
		m_titleBar.removeAllChildren();
		m_titleBar.setCssClass("ui-flw-ttl");
		if(m_closable) {
			m_closeButton = new Img();
			m_closeButton.setSrc("THEME/close.png");
			m_closeButton.setFloat(FloatType.RIGHT);

			//some margin fixes have to be applied with css
			m_closeButton.setCssClass("ui-flw-btn-close");
			m_titleBar.add(m_closeButton);
			m_closeButton.setClicked(new IClicked<NodeBase>() {
				@Override
				public void clicked(NodeBase b) throws Exception {
					closePressed();
				}
			});
		}
		if(m_titleIcon != null)
			m_titleBar.add(m_titleIcon);
		m_titleBar.add(getWindowTitle());
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

	/*--------------------------------------------------------------*/
	/*	CODING:	Window events.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Close the window !AND CALL THE CLOSE HANDLER!.
	 *
	 * @throws Exception
	 */
	public void closePressed() throws Exception {
		close();
		if(m_onClose != null)
			m_onClose.clicked(Window.this);
	}

	/**
	 * Close this floater and cause it to be destroyed from the UI.
	 */
	public void close() {
		remove();
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Properties.											*/
	/*--------------------------------------------------------------*/
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
	public IClicked<NodeBase> getOnClose() {
		return m_onClose;
	}

	/**
	 * Set a Clicked handler to be called when this floater is closed by it's close button. This does <i>not</i> get
	 * called when the floater is closed programmatically (i.e. when close() is called).
	 *
	 * @param onClose
	 */
	public void setOnClose(IClicked<NodeBase> onClose) {
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

	/**
	 * Set an icon for the title bar, using the absolute path to a web resource. If the name is prefixed
	 * with THEME/ it specifies an image from the current THEME's directory.
	 * @param ico
	 */
	public void setIcon(String ico) {
		createIcon().setSrc(ico);
	}

	public Div getBottomContent() {
		return m_bottomContent;
	}

	public Div getTopContent() {
		return m_topContent;
	}
}
