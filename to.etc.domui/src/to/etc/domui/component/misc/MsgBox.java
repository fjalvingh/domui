/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.component.misc;

import javax.annotation.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.input.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.themes.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.domui.util.bugs.*;

public class MsgBox extends Window {
	public interface IAnswer {
		void onAnswer(MsgBoxButton result) throws Exception;
	}
	public interface IAnswer2 {
		void onAnswer(Object result) throws Exception;
	}

	public interface IInput<T> {
		void onInput(T value) throws Exception;
	}

	public static enum Type {
		INFO, WARNING, ERROR, DIALOG, INPUT
	}

	private Img m_theImage = new Img();

	private String m_theText;

	private Div m_theButtons = new Div();

	private static final int WIDTH = 500;

	private static final int HEIGHT = 210;

	private Object m_selectedChoice;

	private IAnswer m_onAnswer;

	private IAnswer2 m_onAnswer2;

	private MsgBoxButton m_closeButtonObject;

	private IInput< ? > m_oninput;

	private Text< ? > m_inputControl;

	/**
	 * Custom dialog message text renderer.
	 */
	private INodeContentRenderer<String> m_dataRenderer;

	protected MsgBox() {
		super(true, false, WIDTH, -1, "");
		setErrorFence(null); // Do not accept handling errors!!
		m_theButtons.setCssClass("ui-mbx-bb");
		setOnClose(new IWindowClosed() {
			@Override
			public void closed(@Nonnull String closeReason) throws Exception {
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

				if(null != m_onAnswer2) {
					m_selectedChoice = m_closeButtonObject;
					try {
						m_onAnswer2.onAnswer(m_closeButtonObject);
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
		//		//vmijic 20100326 - in case of cascading floating windows, z-index higher than one from parent floating window must be set.
		//		FloatingWindow parentFloatingWindow = parent.getParent(FloatingWindow.class);
		//		if(parentFloatingWindow != null) {
		//			w.setZIndex(parentFloatingWindow.getZIndex() + 100);
		//		}
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
				icon = Theme.ICON_MBX_ERROR;
				break;
			case WARNING:
				ttl = Msgs.BUNDLE.getString(Msgs.UI_MBX_WARNING);
				icon = Theme.ICON_MBX_WARNING;
				break;
			case INFO:
				ttl = Msgs.BUNDLE.getString(Msgs.UI_MBX_INFO);
				icon = Theme.ICON_MBX_INFO;
				break;
			case DIALOG:
				ttl = Msgs.BUNDLE.getString(Msgs.UI_MBX_DIALOG);
				icon = Theme.ICON_MBX_DIALOG;
				break;
			case INPUT:
				ttl = Msgs.BUNDLE.getString(Msgs.UI_MBX_INPUT);
				icon = "mbx-question.png";
				break;
		}
		m_theImage.setSrc(icon);
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

	/**
	 * Provides interface to create INFO type messages with custom icon.
	 * @param dad
	 * @param iconSrc
	 * @param string
	 */
	public static void message(NodeBase dad, String iconSrc, String string) {
		message(dad, iconSrc, string, null);
	}

	/**
	 * Provides interface to create INFO type messages with custom icon.
	 * @param dad
	 * @param iconSrc
	 * @param string
	 * @param onAnswer
	 */
	public static void message(NodeBase dad, String iconSrc, String string, IAnswer onAnswer) {
		MsgBox box = create(dad);
		box.setType(Type.INFO);
		box.m_theImage.setSrc(iconSrc);
		box.setMessage(string);
		box.addButton(MsgBoxButton.CONTINUE);
		box.setCloseButton(MsgBoxButton.CONTINUE);
		box.setOnAnswer(onAnswer);
		box.construct();
	}

	/**
	 * Provides interface to create INFO type messages with custom title, icon, data section and optional callback.
	 * @param dad
	 * @param iconSrc
	 * @param title
	 * @param message
	 * @param onAnswer
	 * @param msgRenderer
	 */
	public static void message(NodeBase dad, String iconSrc, String title, IAnswer onAnswer, INodeContentRenderer<String> msgRenderer) {
		MsgBox box = create(dad);
		box.setType(Type.INFO);
		box.m_theImage.setSrc(iconSrc);
		box.setWindowTitle(title);
		box.addButton(MsgBoxButton.CONTINUE);
		box.setCloseButton(MsgBoxButton.CONTINUE);
		box.setOnAnswer(onAnswer);
		box.setDataRenderer(msgRenderer);
		box.construct();
	}

	public static void info(NodeBase dad, String string) {
		message(dad, Type.INFO, string);
	}

	public static void warning(NodeBase dad, String string) {
		message(dad, Type.WARNING, string);
	}

	public static void error(NodeBase dad, String string) {
		message(dad, Type.ERROR, string);
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

	public static void dialog(NodeBase dad, String title, IAnswer onAnswer, INodeContentRenderer<String> contentRenderer) {
		MsgBox box = create(dad);
		box.setType(Type.DIALOG);
		box.setWindowTitle(title);
		box.addButton(MsgBoxButton.CANCEL);
		box.setCloseButton(MsgBoxButton.CANCEL);
		box.setOnAnswer(onAnswer);
		box.setDataRenderer(contentRenderer);
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
		yesNo(dad, string, onAnswer, null);
	}

	/**
	 * Ask a yes/no confirmation, and pass either YES or NO to the onAnswer delegate. Use this if you need the NO action too, else use the IClicked variant.
	 * @param dad
	 * @param string
	 * @param onAnswer
	 * @param msgRenderer Provides custom rendering of specified string message.
	 */
	public static void yesNo(NodeBase dad, String string, IAnswer onAnswer, INodeContentRenderer<String> msgRenderer) {
		MsgBox box = create(dad);
		box.setType(Type.DIALOG);
		box.setMessage(string);
		box.addButton(MsgBoxButton.YES);
		box.addButton(MsgBoxButton.NO);
		box.setCloseButton(MsgBoxButton.NO);
		box.setOnAnswer(onAnswer);
		box.setDataRenderer(msgRenderer);
		box.construct();
	}

	/**
	 * Ask a yes/no confirmation; call the onAnswer handler if YES is selected and do nothing otherwise.
	 * @param dad
	 * @param string
	 * @param onAnswer
	 */
	public static void yesNo(NodeBase dad, String string, final IClicked<MsgBox> onAnswer) {
		yesNo(dad, MsgBox.Type.DIALOG, string, onAnswer);
	}

	/**
	 * Ask a yes/no confirmation; call the onAnswer handler if YES is selected and do nothing otherwise.
	 * @param dad
	 * @param string
	 * @param onAnswer
	 */
	public static void yesNo(NodeBase dad, Type msgtype, String string, final IClicked<MsgBox> onAnswer) {
		final MsgBox box = create(dad);
		box.setType(msgtype);
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

	public static <T> void inputString(NodeBase dad, String message, Text<T> input, IInput<T> onanswer) {
		MsgBox box = create(dad);
		box.setType(Type.INPUT);
		box.setMessage(message);
		box.addButton(MsgBoxButton.CONTINUE);
		box.addButton(MsgBoxButton.CANCEL);
		box.setCloseButton(MsgBoxButton.CANCEL);
		box.setOninput(onanswer);
		box.setInputControl(input);
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
	 *
	 * @param dad
	 * @param boxType
	 * @param message
	 * @param onAnswer
	 * @param buttonresultpairs
	 */
	public static void flexDialog(@Nonnull NodeBase dad, @Nonnull Type boxType, @Nonnull String message, @Nonnull IAnswer2 onAnswer, Object... buttonresultpairs) {
		MsgBox box = create(dad);
		box.setType(boxType);
		box.setMessage(message);

		int ix = 0;
		while(ix < buttonresultpairs.length) {
			Object o = buttonresultpairs[ix++];
			if(o instanceof MsgBoxButton) {
				MsgBoxButton b = (MsgBoxButton) o;
				box.addButton(b);
			} else if(o instanceof String) {
				String s = (String) o;					// Button title
				if(ix >= buttonresultpairs.length)
					throw new IllegalArgumentException("Illegal format: must be [button name string], [response object].");
				box.addButton(s, buttonresultpairs[ix++]);
			} else
				throw new IllegalArgumentException("Unsupported 'button' type in list: " + o + ", only supporting String:Object and MsgBoxButton");
		}
		box.setCloseButton(MsgBoxButton.NO);
		box.setOnAnswer2(onAnswer);
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

	//	/**
	//	 * Adjust dimensions in addition to inherited floater behavior.
	//	 * @see to.etc.domui.dom.html.NodeBase#createContent()
	//	 */
	//	@Override
	//	public void createContent() throws Exception {
	//		super.createContent();
	//		setDimensions(WIDTH, HEIGHT);
	//	}

	private void construct() {
		Div a = new Div();
		add(a);
		a.setCssClass("ui-mbx-top");
		a.setStretchHeight(true);
		a.setOverflow(Overflow.AUTO);
		Table t = new Table();
		a.add(t);
		TBody b = t.getBody();
		TR row = b.addRow();
		row.setVerticalAlign(VerticalAlignType.TOP);
		TD td = row.addCell();
		td.add(m_theImage);
		m_theImage.setPosition(PositionType.ABSOLUTE);
		td.setNowrap(true);
		td.setWidth("50px");

		td = row.addCell("ui-mbx-mc");
		if(getDataRenderer() != null) {
			try {
				getDataRenderer().renderNodeContent(this, td, m_theText, null);
			} catch(Exception ex) {
				Bug.bug(ex);
			}
		} else {
			DomUtil.renderHtmlString(td, m_theText); // 20091206 Allow simple markup in message
		}
		if(m_inputControl != null) {
			td = b.addRowAndCell();
			td.setCssClass("ui-mbx-input-1");
			td = b.addCell();
			td.setCssClass("ui-mbx-input");
			td.add(m_inputControl);
			m_inputControl.setFocus();
		}

		add(m_theButtons);

		//FIXME: vmijic 20090911 Set initial focus to first button. However preventing of keyboard input focus on window in background has to be resolved properly.
		if(m_inputControl == null)
			setFocusOnButton();
	}


	private void setFocusOnButton() {
		if(m_theButtons.getChildCount() > 0 && m_theButtons.getChild(0) instanceof Button) {
			((Button) m_theButtons.getChild(0)).setFocus();
		}
	}

	@Override
	public void setDimensions(int width, int height) {
		super.setDimensions(width, height);
		/*		setTop("50%");
				// center floating window horizontally on screen
				setMarginLeft("-" + width / 2 + "px");
				setMarginTop("-" + height / 2 + "px"); */
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
		if(m_onAnswer2 != null) {
			try {
				m_onAnswer2.onAnswer(m_selectedChoice);
			} catch(ValidationException ex) {
				//close message box in case of validation exception is thrown as result of answer
				close();
				throw ex;
			}
		}

		if(m_oninput != null && sel == MsgBoxButton.CONTINUE) {
			try {
				Object v = m_inputControl.getValue();
				((IInput<Object>) m_oninput).onInput(v);
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

	public IAnswer2 getOnAnswer2() {
		return m_onAnswer2;
	}

	public void setOnAnswer2(IAnswer2 onAnswer2) {
		m_onAnswer2 = onAnswer2;
	}

	public IInput< ? > getOninput() {
		return m_oninput;
	}

	public void setOninput(IInput< ? > oninput) {
		m_oninput = oninput;
	}

	public Text< ? > getInputControl() {
		return m_inputControl;
	}

	public void setInputControl(Text< ? > inputControl) {
		m_inputControl = inputControl;
	}

	protected INodeContentRenderer<String> getDataRenderer() {
		return m_dataRenderer;
	}

	protected void setDataRenderer(INodeContentRenderer<String> dataRenderer) {
		m_dataRenderer = dataRenderer;
	}
}
