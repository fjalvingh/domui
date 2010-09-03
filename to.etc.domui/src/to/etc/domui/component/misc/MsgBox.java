package to.etc.domui.component.misc;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;

public class MsgBox extends FloatingWindow {
	public interface IAnswer {
		void onAnswer(MsgBoxButton result) throws Exception;
	}

	public static enum Type {
		INFO, WARNING, ERROR, DIALOG
	}

	private Img m_theImage = new Img();

	private String m_theText;

	private Div m_theButtons = new Div();

	private static final int WIDTH = 500;

	private static final int HEIGHT = 200;

	Object m_selectedChoice;

	IAnswer m_onAnswer;

	MsgBoxButton m_closeButtonObject;

	protected MsgBox() {
		super(true, "");
		setErrorFence(null); // Do not accept handling errors!!
		m_theButtons.setCssClass("ui-mbx-bb");
		m_theImage.setCssClass("ui-mbx-img");
		setOnClose(new IClicked<FloatingWindow>() {
			@Override
			public void clicked(FloatingWindow b) throws Exception {
				if(null != m_onAnswer) {
					m_selectedChoice = m_closeButtonObject;
					try {
						m_onAnswer.onAnswer(m_closeButtonObject);
					} catch(ValidationException ex) {
						//close message box in case of validation exception is thrown as result of answer. Other exceptions do not close.
						close();
						throw ex;
					}
				}
			}
		});
	}

	static public MsgBox create(NodeBase parent) {
		MsgBox w = new MsgBox(); // Create instance
		//vmijic 20100326 - in case of cascading floating windows, z-index higher than one from parent floating window must be set.
		FloatingWindow parentFloatingWindow = parent.getParent(FloatingWindow.class);
		if(parentFloatingWindow != null) {
			w.setZIndex(parentFloatingWindow.getZIndex() + 100);
		}
		UrlPage body = parent.getPage().getBody();
		body.add(w);
		return w;
	}

	protected void setType(Type type) {
		String ttl;
		String icon;
		switch(type){
			default:
				throw new IllegalStateException(type + " ??");
			case ERROR:
				ttl = Msgs.BUNDLE.getString(Msgs.UI_MBX_ERROR);
				icon = "mbx-error.png";
				break;
			case WARNING:
				ttl = Msgs.BUNDLE.getString(Msgs.UI_MBX_WARNING);
				icon = "mbx-warning.png";
				break;
			case INFO:
				ttl = Msgs.BUNDLE.getString(Msgs.UI_MBX_INFO);
				icon = "mbx-info.png";
				break;
			case DIALOG:
				ttl = Msgs.BUNDLE.getString(Msgs.UI_MBX_DIALOG);
				icon = "mbx-question.png";
				break;
		}
		m_theImage.setSrc("THEME/" + icon);
		setWindowTitle(ttl);
		setTestID("msgBox");
	}

	protected void setMessage(String txt) {
		m_theText = txt;
	}

	private void setCloseButton(MsgBoxButton val) {
		m_closeButtonObject = val;
	}

	MsgBoxButton getCloseObject() {
		return m_closeButtonObject;
	}

	public static void message(NodeBase dad, Type mt, String string) {
		if(mt == Type.DIALOG) {
			throw new IllegalArgumentException("Please use one of the predefined button calls for MsgType.DIALOG type MsgBox!");
		}
		MsgBox box = create(dad);
		box.setType(mt);
		box.setMessage(string);
		box.addButton(MsgBoxButton.CONTINUE);
		box.setCloseButton(MsgBoxButton.CONTINUE);
		box.construct();
	}

	public static void message(NodeBase dad, Type mt, String string, IAnswer onAnswer) {
		if(mt == Type.DIALOG) {
			throw new IllegalArgumentException("Please use one of the predefined button calls for MsgType.DIALOG type MsgBox!");
		}
		MsgBox box = create(dad);
		box.setType(mt);
		box.setMessage(string);
		box.addButton(MsgBoxButton.CONTINUE);
		box.setCloseButton(MsgBoxButton.CONTINUE);
		box.setOnAnswer(onAnswer);
		box.construct();
	}

	public static void yesNoCancel(NodeBase dad, String string, IAnswer onAnswer) {
		MsgBox box = create(dad);
		box.setType(Type.DIALOG);
		box.setMessage(string);
		box.addButton(MsgBoxButton.YES);
		box.addButton(MsgBoxButton.NO);
		box.addButton(MsgBoxButton.CANCEL);
		box.setCloseButton(MsgBoxButton.CANCEL);
		box.setOnAnswer(onAnswer);
		box.construct();
	}

	/**
	 * Ask a yes/no confirmation, and pass either YES or NO to the onAnswer delegate. Use this if you need the NO action too, else use the IClicked variant.
	 * @param dad
	 * @param string
	 * @param onAnswer
	 */
	public static void yesNo(NodeBase dad, String string, IAnswer onAnswer) {
		MsgBox box = create(dad);
		box.setType(Type.DIALOG);
		box.setMessage(string);
		box.addButton(MsgBoxButton.YES);
		box.addButton(MsgBoxButton.NO);
		box.setCloseButton(MsgBoxButton.NO);
		box.setOnAnswer(onAnswer);
		box.construct();
	}

	/**
	 * Ask a yes/no confirmation; call the onAnswer handler if YES is selected and do nothing otherwise.
	 * @param dad
	 * @param string
	 * @param onAnswer
	 */
	public static void yesNo(NodeBase dad, String string, final IClicked<MsgBox> onAnswer) {
		final MsgBox box = create(dad);
		box.setType(Type.DIALOG);
		box.setMessage(string);
		box.addButton(MsgBoxButton.YES);
		box.addButton(MsgBoxButton.NO);
		box.setCloseButton(MsgBoxButton.NO);
		box.setOnAnswer(new IAnswer() {
			@Override
			public void onAnswer(MsgBoxButton result) throws Exception {
				if(result == MsgBoxButton.YES)
					onAnswer.clicked(box);
			}
		});
		box.construct();
	}

	/**
	 * Show message of specified type, and provide details (More...) button. Usually used to show some error details if user wants to see it.
	 * @param dad
	 * @param type
	 * @param string
	 * @param onAnswer
	 */
	public static void okMore(NodeBase dad, Type type, String string, IAnswer onAnswer) {
		final MsgBox box = create(dad);
		box.setType(type);
		box.setMessage(string);
		box.addButton(MsgBoxButton.OK);
		box.addButton(MsgBoxButton.MORE);
		box.setCloseButton(MsgBoxButton.OK);
		box.setOnAnswer(onAnswer);
		box.construct();
	}

	/**
	 * Ask a continue/cancel confirmation. This passes either choice to the handler.
	 * @param dad
	 * @param string
	 * @param onAnswer
	 */
	public static void continueCancel(NodeBase dad, String string, IAnswer onAnswer) {
		MsgBox box = create(dad);
		box.setType(Type.DIALOG);
		box.setMessage(string);
		box.addButton(MsgBoxButton.CONTINUE);
		box.addButton(MsgBoxButton.CANCEL);
		box.setCloseButton(MsgBoxButton.CANCEL);
		box.setOnAnswer(onAnswer);
		box.construct();
	}

	/**
	 * Ask a continue/cancel confirmation, and call the IClicked handler for CONTINUE only.
	 * @param dad
	 * @param string
	 * @param onAnswer
	 */
	public static void continueCancel(NodeBase dad, String string, final IClicked<MsgBox> onAnswer) {
		final MsgBox box = create(dad);
		box.setType(Type.DIALOG);
		box.setMessage(string);
		box.addButton(MsgBoxButton.CONTINUE);
		box.addButton(MsgBoxButton.CANCEL);
		box.setCloseButton(MsgBoxButton.CANCEL);
		box.setOnAnswer(new IAnswer() {
			@Override
			public void onAnswer(MsgBoxButton result) throws Exception {
				if(result == MsgBoxButton.CONTINUE)
					onAnswer.clicked(box);
			}
		});
		box.construct();
	}

	/**
	 * Create a button which will show an "are you sure" yes/no dialog with a specified text. Only if the user
	 * presses the "yes" button will the clicked handler be executed.
	 * @param icon
	 * @param text		The button's text.
	 * @param message	The message to show in the are you sure popup
	 * @param ch		The delegate to call when the user is sure.
	 * @return
	 */
	public static DefaultButton areYouSureButton(String text, String icon, final String message, final IClicked<DefaultButton> ch) {
		final DefaultButton btn = new DefaultButton(text, icon);
		IClicked<DefaultButton> bch = new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton b) throws Exception {
				yesNo(b, message, new IClicked<MsgBox>() {
					@Override
					public void clicked(MsgBox bx) throws Exception {
						ch.clicked(btn);
					}
				});
			}
		};
		btn.setClicked(bch);
		return btn;
	}

	/**
	 * Create a button which will show an "are you sure" yes/no dialog with a specified text. Only if the user
	 * presses the "yes" button will the clicked handler be executed.
	 *
	 * @param text		The button's text.
	 * @param message	The message to show in the are you sure popup
	 * @param ch		The delegate to call when the user is sure.
	 * @return
	 */
	public static DefaultButton areYouSureButton(String text, final String message, final IClicked<DefaultButton> ch) {
		return areYouSureButton(text, null, message, ch);
	}

	/**
	 * Create a LinkButton which will show an "are you sure" yes/no dialog with a specified text. Only if the user
	 * presses the "yes" button will the clicked handler be executed.
	 * @param icon
	 * @param text		The button's text.
	 * @param message	The message to show in the are you sure popup
	 * @param ch		The delegate to call when the user is sure.
	 * @return
	 */
	public static LinkButton areYouSureLinkButton(String text, String icon, final String message, final IClicked<LinkButton> ch) {
		final LinkButton btn = new LinkButton(text, icon);
		IClicked<LinkButton> bch = new IClicked<LinkButton>() {
			@Override
			public void clicked(LinkButton b) throws Exception {
				yesNo(b, message, new IClicked<MsgBox>() {
					@Override
					public void clicked(MsgBox bx) throws Exception {
						ch.clicked(btn);
					}
				});
			}
		};
		btn.setClicked(bch);
		return btn;
	}

	/**
	 * Create a button which will show an "are you sure" yes/no dialog with a specified text. Only if the user
	 * presses the "yes" button will the clicked handler be executed.
	 *
	 * @param text		The button's text.
	 * @param message	The message to show in the are you sure popup
	 * @param ch		The delegate to call when the user is sure.
	 * @return
	 */
	public static LinkButton areYouSureLinkButton(String text, final String message, final IClicked<LinkButton> ch) {
		return areYouSureLinkButton(text, null, message, ch);
	}

	/**
	 * Adjust dimensions in addition to inherited floater behavior.
	 * @see to.etc.domui.dom.html.NodeBase#createContent()
	 */
	@Override
	public void createContent() throws Exception {
		super.createContent();
		setDimensions(WIDTH, HEIGHT);
	}

	private void construct() {
		Div a = new Div();
		add(a);
		a.setCssClass("ui-mbx-top");
		Table t = new Table();
		a.add(t);
		TBody b = t.getBody();
		TD td = b.addRowAndCell();
		td.setCssClass("ui-mbx-ic");
		td.add(m_theImage);
		td.setNowrap(true);
		td.setWidth("1%");

		td = b.addCell("ui-mbx-mc");
		DomUtil.renderHtmlString(td, m_theText); // 20091206 Allow simple markup in message
		add(m_theButtons);
		//FIXME: vmijic 20090911 Set initial focus to first button. However preventing of keyboard input focus on window in background has to be resolved properly.
		setFocusOnButton();
	}


	private void setFocusOnButton() {
		if(m_theButtons.getChildCount() > 0 && m_theButtons.getChild(0) instanceof Button) {
			((Button) m_theButtons.getChild(0)).setFocus();
		}
	}

	private void setDimensions(int width, int height) {
		setTop("50%");
		setWidth(width + "px");
		setHeight(height + "px");
		// center floating window horizontally on screen
		setMarginLeft("-" + width / 2 + "px");
		setMarginTop("-" + height / 2 + "px");
	}

	void setSelectedChoice(Object selectedChoice) {
		m_selectedChoice = selectedChoice;
	}

	protected void answer(Object sel) throws Exception {
		m_selectedChoice = sel;
		if(m_onAnswer != null) {
			try {
				m_onAnswer.onAnswer((MsgBoxButton) m_selectedChoice);
			} catch(ValidationException ex) {
				//close message box in case of validation exception is thrown as result of answer
				close();
				throw ex;
			}
		}
		close();
	}

	/**
	 * Add a default kind of button.
	 * @param mbb
	 */
	protected void addButton(final MsgBoxButton mbb) {
		if(mbb == null)
			throw new NullPointerException("A message button cannot be null, dufus");
		String lbl = MetaManager.findEnumLabel(mbb);
		if(lbl == null)
			lbl = mbb.name();
		DefaultButton btn = new DefaultButton(lbl, new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton b) throws Exception {
				answer(mbb);
			}
		});
		btn.setTestID(mbb.name());
		m_theButtons.add(btn);
	}

	protected void addButton(final String lbl, final Object selval) {
		m_theButtons.add(new DefaultButton(lbl, new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton b) throws Exception {
				answer(selval);
			}
		}));
	}

	protected IAnswer getOnAnswer() {
		return m_onAnswer;
	}

	protected void setOnAnswer(IAnswer onAnswer) {
		m_onAnswer = onAnswer;
	}
}
