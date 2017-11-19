package to.etc.domui.component.misc;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.IWindowClosed;
import to.etc.domui.component.layout.Window;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.dom.css.Overflow;
import to.etc.domui.dom.css.VerticalAlignType;
import to.etc.domui.dom.html.Button;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.Label;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;
import to.etc.domui.dom.html.TR;
import to.etc.domui.dom.html.Table;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.themes.Theme;
import to.etc.domui.trouble.ValidationException;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.IRenderInto;
import to.etc.domui.util.Msgs;
import to.etc.domui.util.bugs.Bug;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Easier to use MsgBox using Builder pattern.
 *
 * Created by jal on 4/3/14.
 */
final public class MsgBox2 extends Window {
	private IClicked<MsgBox2> m_clicked;

	private MsgBoxButton m_clickedButton;

	public interface IAnswer {
		void onAnswer(@Nonnull MsgBoxButton result) throws Exception;
	}
	public interface IAnswer2 {
		void onAnswer(Object result) throws Exception;
	}

	public interface IInput<T> {
		void onInput(T value) throws Exception;
	}

	private final class InputPair {
		@Nullable
		private final NodeBase	m_label;

		@Nullable
		private final NodeBase m_input;

		public InputPair(@Nullable NodeBase label, @Nullable NodeBase input) {
			if(null == label && null == input)
				throw new IllegalStateException("Both nodes cannot be null");
			m_label = label;
			m_input = input;
		}

		@Nullable
		public NodeBase getLabel() {
			return m_label;
		}

		@Nullable
		public NodeBase getInput() {
			return m_input;
		}
	}

	public enum Type {
		INFO, WARNING, ERROR, DIALOG, INPUT
	}

	/** Autoclose behavior. */
	private Boolean m_autoClose;

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

	/**
	 * Custom dialog message text renderer.
	 */
	private IRenderInto<String> m_dataRenderer;

	private NodeContainer m_content;

	private MsgBoxButton	m_assumedOkButton;

	private MsgBoxButton 	m_assumedCancelButton = MsgBoxButton.CANCEL;

	@Nonnull
	private List<InputPair> m_inputList = new ArrayList<>();

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
		/*
		 * Autoclose: default to autoclose for message/question; do not for inputs.
		 */
		Boolean autoClose = m_autoClose;
		if(null == autoClose) {
			autoClose = Boolean.valueOf(m_inputList.size() == 0);
		}
		setAutoClose(autoClose.booleanValue());

		//-- If no buttons added: just add continue.
		if(m_theButtons.size() == 0) {
			button(MsgBoxButton.CONTINUE);

			//-- If we have inputs then also add CANCEL
			if(m_inputList.size() > 0) {
				button(MsgBoxButton.CANCEL);
			}
		}

		if(m_closeButtonObject == null) {
			m_closeButtonObject = m_assumedCancelButton;
		}
		if(! m_typeSet) {
			if(m_inputList.size() > 0) {
				type(Type.DIALOG);
			} else if(m_theButtons.size() < 2)
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
		IRenderInto<String> renderer = m_dataRenderer;
		if(null != renderer) {
			try {
				renderer.renderOpt(td, m_theText);
			} catch(Exception ex) {
				Bug.bug(ex);
			}
		} else if(content != null) {
			td.add(content);
		} else {
			DomUtil.renderHtmlString(td, m_theText);
		}

		//-- Start adding inputs, if applicable
		boolean unfocused = true;
		if(m_inputList.size() > 0) {
			unfocused = renderInputs(td);
		}

		Div bd = m_buttonDiv = new Div();
		add(bd);
		bd.addCssClass("ui-mbx-btns");
		for(Button btn: m_theButtons) {
			bd.add(btn);
		}

		if(unfocused)
			setFocusOnButton();
	}

	private boolean renderInputs(@Nonnull NodeContainer nc) {
		boolean unfocused = true;
		Div area = new Div();
		nc.add(area);
		area.setCssClass("ui-mbx2-input-area");
		TBody tb = area.addTable();
		for(InputPair ip: m_inputList) {
			NodeBase input = ip.getInput();
			NodeBase label = ip.getLabel();

			if(input == null) {
				if(label != null) {
					//-- Only label node: show label only
					TD td = tb.addRowAndCell();
					td.setColspan(2);
					td.add(label);
					td.setCssClass("ui-mbx2-in-lbl");
				}
			} else {
				if(label == null) {
					//-- No label: create an input that takes all of the area
					TD td = tb.addRowAndCell();
					td.setColspan(2);
					td.add(input);
					td.setCssClass("ui-mbx2-in-ctl");
				} else {
					//-- Both present
					TD td = tb.addRowAndCell();
					td.add(label);
					td.setCssClass("ui-mbx2-in-lbl");
					td = tb.addCell();
					td.add(input);
					td.setCssClass("ui-mbx2-in-ctl");
				}

				if(unfocused) {
					input.setFocus();
					unfocused = false;
				}
			}
		}
		return unfocused;
	}

	private void setFocusOnButton() {
		if(m_buttonDiv.getChildCount() > 0 && m_buttonDiv.getChild(0) instanceof Button) {
			m_buttonDiv.getChild(0).setFocus();
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
		if(getWindowTitle() == null)
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
		return button(lbl, null, selval);
	}

	@Nonnull
	public MsgBox2 button(final String lbl, final String icon, final Object selval) {
		m_theButtons.add(new DefaultButton(lbl, icon, new IClicked<DefaultButton>() {
			@Override
			public void clicked(@Nonnull DefaultButton b) throws Exception {
				answer(selval);
			}
		}));
		return this;
	}

	@Nonnull
	public MsgBox2 renderer(IRenderInto<String> cr) {
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

	public MsgBox2 autoClose(boolean auto) {
		m_autoClose = Boolean.valueOf(auto);
		return this;
	}

	private void setCloseButton(MsgBoxButton val) {
		m_closeButtonObject = val;
	}

	private void answer(Object sel) throws Exception {
		m_selectedChoice = sel;

		try {
			if(m_onAnswer != null) {
				m_onAnswer.onAnswer((MsgBoxButton) m_selectedChoice);
			}
			if(m_onAnswer2 != null) {
				m_onAnswer2.onAnswer(m_selectedChoice);
			}
			IClicked<MsgBox2> clicked = m_clicked;
			if(clicked != null) {
				if(sel == m_assumedOkButton) {
					clicked.clicked(this);
				}
			}

			IInput<?> oninput = m_oninput;
			if(oninput != null && sel == MsgBoxButton.CONTINUE) {
				if(m_inputList.size() != 1)
					throw new IllegalStateException("Internal: bad input count??");
				InputPair inputPair = m_inputList.get(0);
				IControl<Object> ic = (IControl<Object>) inputPair.getInput();
				if(null == ic)
					throw new IllegalStateException("No IControl<T> as part of IInput<> handling");
				Object value = ic.getValue();
				((IInput<Object>) oninput).onInput(value);
			}
			close();
		} catch(ValidationException vx) {
			throw vx;
		} catch(Exception x) {
			close();
			throw x;
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Pattern: adding inputs.								*/
	/*--------------------------------------------------------------*/

	/**
	 * Add an input line with label and control.
	 * @param label
	 * @param control
	 * @return
	 */
	@Nonnull
	public MsgBox2 input(@Nonnull String label, @Nonnull NodeBase control) {
		Label l = new Label(control, label);
		input(l, control);
		return this;
	}

	/**
	 * Add an input line with label and control.
	 * @param label
	 * @param control
	 * @return
	 */
	@Nonnull
	public MsgBox2 input(@Nonnull Label label, @Nonnull NodeBase control) {
		_input(label, control);
		return this;
	}

	private void _input(@Nullable NodeBase label, @Nullable NodeBase control) {
		if(m_oninput != null)
			throw new IllegalStateException("You cannot combine an IInput-based answer with a list-of-controls");

		InputPair p = new InputPair(label, control);
		m_inputList.add(p);
	}

	@Nonnull
	public <T> MsgBox2 input(@Nonnull String label, @Nonnull IControl<T> control, @Nonnull IInput<T> onanswer) {
		//-- Only allowed with input list empty
		if(m_inputList.size() != 0)
			throw new IllegalStateException("You cannot combine this with other input controls as there's only one answer.");
		_input(new Label((NodeBase) control, label), (NodeBase) control);
		m_oninput = onanswer;					// Ordered
		return this;
	}

	@Nonnull
	public <T> MsgBox2 input(@Nonnull IControl<T> control, @Nonnull IInput<T> onanswer) {
		if(m_oninput != null)
			throw new IllegalStateException("Duplicate IInput<> set");

		//-- Only allowed with input list empty
		if(m_inputList.size() != 0)
			throw new IllegalStateException("You cannot combine this with other input controls as there's only one answer.");
		_input(null, (NodeBase) control);
		m_oninput = onanswer;					// Ordered
		return this;
	}

	public <T> MsgBox2 icon(@Nonnull String icon){
		m_theImage.setSrc(icon);
		return this;
	}
}
