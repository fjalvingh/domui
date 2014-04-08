package to.etc.domui.component.misc;

import java.util.*;

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

/**
 * Easier to use MsgBox using Builder pattern.
 *
 * Created by jal on 4/3/14.
 */
final public class MsgBox2 extends Window {
	private IClicked<MsgBox2> m_clicked;

	private MsgBoxButton m_clickedButton;

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

	private boolean m_typeSet;

	private String m_theText;

	private Div m_buttonDiv = new Div();

	private List<Button> m_theButtons = new ArrayList<>();

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

	private NodeContainer m_content;

	private MsgBoxButton	m_assumedOkButton;

	private MsgBoxButton 	m_assumedCancelButton = MsgBoxButton.CANCEL;

	private MsgBox2() {
		super(true, false, WIDTH, -1, "");
		setErrorFence(null); // Do not accept handling errors!!
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

	@Override public void createContent() throws Exception {
		//-- If no buttons added: just add continue.
		if(m_theButtons.size() == 0) {
			button(MsgBoxButton.CONTINUE);
		}

		if(m_closeButtonObject == null) {
			m_closeButtonObject = m_assumedCancelButton;
		}
		if(! m_typeSet) {
			if(m_theButtons.size() < 2)
				type(Type.INFO);
			else
				type(Type.DIALOG);
		}

		//-- Initialize all that was left empty....
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
		td.setVerticalAlign(VerticalAlignType.TOP);
		td.add(m_theImage);
		td.setNowrap(true);
		td.setWidth("50px");

		td = row.addCell("ui-mbx-mc");
		NodeContainer content = m_content;
		INodeContentRenderer<String> renderer = m_dataRenderer;
		if(null != renderer) {
			try {
				renderer.renderNodeContent(this, td, m_theText, null);
			} catch(Exception ex) {
				Bug.bug(ex);
			}
		} else if(content != null) {
			td.add(content);
		} else {
			DomUtil.renderHtmlString(td, m_theText);
		}
		if(m_inputControl != null) {
			td = b.addRowAndCell();
			td.setCssClass("ui-mbx-input-1");
			td = b.addCell();
			td.setCssClass("ui-mbx-input");
			td.add(m_inputControl);
			m_inputControl.setFocus();
		}

		Div bd = m_buttonDiv = new Div();
		add(bd);
		bd.addCssClass("ui-bb-middle");
		for(Button btn: m_theButtons) {
			bd.add(btn);
		}

		if(m_inputControl == null)
			setFocusOnButton();
	}

	private void setFocusOnButton() {
		if(m_buttonDiv.getChildCount() > 0 && m_buttonDiv.getChild(0) instanceof Button) {
			((Button) m_buttonDiv.getChild(0)).setFocus();
		}
	}

	/**
	 * Create a message box and link it to the page.
	 * @param parent
	 * @return
	 */
	@Nonnull
	static public MsgBox2 on(@Nonnull NodeBase parent) {
		MsgBox2 b = new MsgBox2();
		UrlPage body = parent.getPage().getBody();
		body.undelegatedAdd(0, b);
		return b;
	}

	@Nonnull
	@Override
	public MsgBox2 title(String set) {
		super.title(set);
		return this;
	}

	@Nonnull
	public MsgBox2 type(@Nonnull Type type) {
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
				icon = Theme.ICON_MBX_DIALOG;
				break;
		}
		m_theImage.setSrc(icon);
		setWindowTitle(ttl);
		setTestID("msgBox");
		m_typeSet = true;
		return this;
	}

	@Nonnull
	public MsgBox2 info() {
		type(Type.INFO);
		return this;
	}

	@Nonnull
	public MsgBox2 warning() {
		type(Type.WARNING);
		return this;
	}

	@Nonnull
	public MsgBox2 error() {
		type(Type.ERROR);
		return this;
	}

	@Nonnull
	public MsgBox2 info(@Nonnull String message) {
		type(Type.INFO);
		text(message);
		return this;
	}

	@Nonnull
	public MsgBox2 warning(@Nonnull String message) {
		type(Type.WARNING);
		text(message);
		return this;
	}

	@Nonnull
	public MsgBox2 error(@Nonnull String message) {
		type(Type.ERROR);
		text(message);
		return this;
	}

	@Nonnull
	public MsgBox2 question() {
		type(Type.DIALOG);
		return this;
	}

	/**
	 * Set the message box's content text. Alternatively call {@link #content(to.etc.domui.dom.html.NodeContainer)}.
	 * @param txt
	 * @return
	 */
	@Nonnull
	public MsgBox2 text(@Nonnull String txt) {
		m_theText = txt;
		return this;
	}

	/**
	 * Set the box's content.
	 * @param content
	 * @return
	 */
	@Nonnull
	public MsgBox2 content(@Nonnull NodeContainer content) {
		m_content = content;
		return this;
	}

	@Override
	@Nonnull
	public MsgBox2 size(int w, int h) {
		setDimensions(w, h);
		return this;
	}

	/**
	 * Add a default kind of button.
	 * @param mbb
	 */
	@Nonnull
	public MsgBox2  button(@Nonnull final MsgBoxButton mbb) {
		if(mbb == null)
			throw new NullPointerException("A message button cannot be null");
		String lbl = MetaManager.findEnumLabel(mbb);
		if(lbl == null)
			lbl = mbb.name();
		DefaultButton btn = new DefaultButton(lbl, new IClicked<DefaultButton>() {
			@Override
			public void clicked(@Nonnull DefaultButton b) throws Exception {
				answer(mbb);
			}
		});
		btn.setTestID(mbb.name());
		if(m_theButtons.size() == 0) {
			m_assumedOkButton = mbb;
		} else {
			m_assumedCancelButton = mbb;
		}
		m_theButtons.add(btn);
		return this;
	}

	@Nonnull
	public MsgBox2 continueCancel() {
		button(MsgBoxButton.CONTINUE);
		button(MsgBoxButton.CANCEL);
		return this;
	}

	@Nonnull
	public MsgBox2 yesNo() {
		button(MsgBoxButton.YES);
		button(MsgBoxButton.NO);
		return this;
	}

	@Nonnull
	public MsgBox2 button(final String lbl, final Object selval) {
		m_theButtons.add(new DefaultButton(lbl, new IClicked<DefaultButton>() {
			@Override
			public void clicked(@Nonnull DefaultButton b) throws Exception {
				answer(selval);
			}
		}));
		return this;
	}

	@Nonnull
	public MsgBox2 renderer(INodeContentRenderer<String> cr) {
		m_dataRenderer = cr;
		return this;
	}

	@Nonnull
	public MsgBox2 onAnswer(@Nonnull IAnswer onAnswer) {
		m_onAnswer = onAnswer;
		return this;
	}

	@Nonnull
	public MsgBox2 onAnswer(@Nonnull IAnswer2 onAnswer) {
		m_onAnswer2 = onAnswer;
		return this;
	}

	@Nonnull
	public MsgBox2 onAnswer(@Nonnull IClicked<MsgBox2> clicked) {
		m_clicked = clicked;
		return this;
	}

	private void setCloseButton(MsgBoxButton val) {
		m_closeButtonObject = val;
	}

	private void answer(Object sel) throws Exception {
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
		IClicked<MsgBox2> clicked = m_clicked;
		if(clicked != null) {
			if(sel == m_assumedOkButton) {
				clicked.clicked(this);
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
}
