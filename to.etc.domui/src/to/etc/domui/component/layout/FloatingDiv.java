package to.etc.domui.component.layout;

import javax.annotation.*;

import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * The base class for all floating thingeries (new style).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 18, 2011
 */
public class FloatingDiv extends Div implements IAddToBody {
	final private boolean m_modal;

	final private boolean m_resizable;

	/** If this is a modal window it will have a "hider" div to make it modal, and that div will be placed in here by the Page when the div is shown. */
	@Nullable
	private Div m_hider;

	public FloatingDiv(boolean modal) {
		this(modal, false, 640, 400);
	}

	public FloatingDiv(boolean modal, boolean resizable) {
		this(modal, resizable, 640, 400);
	}

	public FloatingDiv(boolean modal, boolean resizable, int widthinpx, int heightinpx) {
		m_modal = modal;
		m_resizable = resizable;
		setDimensions(widthinpx, heightinpx);
	}

	/**
	 * Change the width and height for the dialog - only valid before it has been
	 * built!! The minimum size is 250x200 pixels.
	 * @param width
	 * @param height
	 */
	public void setDimensions(int width, int height) {
		if(width < 250 || height < 200)
			throw new IllegalArgumentException("The width=" + width + " or height=" + height + " is invalid: it cannot be smaller than 250x200.");
		if(isBuilt())
			throw new IllegalStateException("The initial size can only be changed before the component " + getClass() + " is built.");
		setWidth(width + "px");
		setHeight(height + "px");
	}

	/**
	 * Returns T if this is a MODAL window, obscuring windows it is on top of.
	 */
	public boolean isModal() {
		return m_modal;
	}

	public boolean isResizable() {
		return m_resizable;
	}

	public Div internalGetHider() {
		return m_hider;
	}

	public void internalSetHider(@Nullable Div hider) {
		m_hider = hider;
	}

	/**
	 * Overridden to tell the floating thing handler to remove this floater from
	 * the stack.
	 * @see to.etc.domui.dom.html.NodeBase#onRemoveFromPage(to.etc.domui.dom.html.Page)
	 */
	@Override
	public void onRemoveFromPage(Page p) {
		super.onRemoveFromPage(p);
		p.internalRemoveFloater(this);
	}

	@Override
	protected void beforeCreateContent() {
		super.beforeCreateContent();
		setCssClass("ui-flw");

		if(getWidth() == null) // Should not be possible.
			setWidth("640px");
		if(getHeight() == null) // Should not be possible
			setHeight("400px");
		if(getZIndex() <= 0) { // Should not be possible.
			setZIndex(100);
		}
		if(getTestID() == null) {
			setTestID("popup_" + getZIndex());
		}
		setPosition(PositionType.FIXED);
		int width = DomUtil.pixelSize(getWidth());
		if(-1 == width)
			throw new IllegalStateException("Bad width!");

		// center floating window horizontally on screen
		setMarginLeft("-" + width / 2 + "px");

		//-- If this is resizable add the resizable() thing to the create javascript.
		if(isResizable())
			appendCreateJS("$('#" + getActualID() + "').resizable({minHeight: 256, minWidth: 256, resize: WebUI.floatingDivResize });");
	}
}
