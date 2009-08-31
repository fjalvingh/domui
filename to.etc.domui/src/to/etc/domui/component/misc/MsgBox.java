package to.etc.domui.component.misc;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
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
		m_theButtons.setCssClass("ui-mbx-bb");
		m_theImage.setCssClass("ui-mbx-img");
		setOnClose(new IClicked<FloatingWindow>() {
			public void clicked(FloatingWindow b) throws Exception {
				if(null != m_onAnswer) {
					m_selectedChoice = m_closeButtonObject;
					m_onAnswer.onAnswer(m_closeButtonObject);
				}
			}
		});
	}

	static public MsgBox create(NodeBase parent) {
		UrlPage body = parent.getPage().getBody();
		MsgBox w = new MsgBox(); // Create instance
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
			public void onAnswer(MsgBoxButton result) throws Exception {
				if(result == MsgBoxButton.YES)
					onAnswer.clicked(box);
			}
		});
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
			public void clicked(DefaultButton b) throws Exception {
				yesNo(b, message, new IClicked<MsgBox>() {
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
		td.add(m_theText);
		add(m_theButtons);
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

	protected void close(Object sel) throws Exception {
		m_selectedChoice = sel;
		close();
		if(m_onAnswer != null) {
			m_onAnswer.onAnswer((MsgBoxButton) m_selectedChoice);
		}
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
		m_theButtons.add(new DefaultButton(lbl, new IClicked<DefaultButton>() {
			public void clicked(DefaultButton b) throws Exception {
				close(mbb);
			}
		}));
	}

	protected void addButton(final String lbl, final Object selval) {
		m_theButtons.add(new DefaultButton(lbl, new IClicked<DefaultButton>() {
			public void clicked(DefaultButton b) throws Exception {
				close(selval);
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
