package to.etc.domui.pages.generic;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.form.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;
import to.etc.webapp.query.*;

/**
 * Basic stuff to handle editing a simple entity.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 22, 2008
 */
public abstract class BasicEditPage<T> extends BasicPage<T> implements IReadOnlyModel<T> {
	private ButtonBar m_buttonBar;

	private boolean m_deleteable;

	private T m_value;

	private boolean m_displayonly;

	private TabularFormBuilder m_formBuilder;

	private ModelBindings m_bindings;

	abstract public T initializeValue() throws Exception;

	public BasicEditPage(Class<T> valueClass) {
		this(valueClass, false);
	}

	public BasicEditPage(Class<T> valueClass, boolean deleteable) {
		super(valueClass);
		m_deleteable = deleteable;
	}

	public TabularFormBuilder getBuilder() {
		if(m_formBuilder == null)
			m_formBuilder = new TabularFormBuilder(getBaseClass(), this);
		return m_formBuilder;
	}

	@Override
	protected void afterCreateContent() throws Exception {
		super.afterCreateContent();
	}

	@Override
	final public void createContent() throws Exception {
		if(m_formBuilder != null)
			m_formBuilder.reset();
		m_buttonBar = null;

		super.createContent(); // Page title and crud
		createButtonBar();
		createButtons();
		m_bindings = createEditable();
		if(m_bindings == null) {
			if(m_formBuilder != null) {
				add(m_formBuilder.finish());
				m_bindings = m_formBuilder.getBindings();
			}
		}
		if(m_bindings == null)
			throw new IllegalStateException("The form's content is undefined: please override createEditable.");
		m_bindings.moveModelToControl();
	}

	protected ModelBindings createEditable() throws Exception {
		return null;
	}

	protected void createButtonBar() {
		add(getButtonBar());
	}

	public ButtonBar getButtonBar() {
		if(m_buttonBar == null)
			m_buttonBar = new ButtonBar();
		return m_buttonBar;
	}

	public boolean isDeleteable() {
		return m_deleteable;
	}

	protected void createButtons() {
		if(!isDisplayonly()) {
			createCommitButton();
			createCancelButton();
			if(isDeleteable())
				createDeleteButton();
		}
	}

	protected void createCommitButton() {
		getButtonBar().addButton("C!ommit", "THEME/btnSave.png", new IClicked<DefaultButton>() {
			public void clicked(DefaultButton b) throws Exception {
				save();
			}
		});
	}

	protected void createCancelButton() {
		getButtonBar().addButton("!Cancel", "THEME/btnCancel.png", new IClicked<DefaultButton>() {
			public void clicked(DefaultButton b) throws Exception {
				cancel();
			}
		});
	}

	protected void createDeleteButton() {
		getButtonBar().addButton("!Delete", "THEME/btnDelete.png", new IClicked<DefaultButton>() {
			public void clicked(DefaultButton b) throws Exception {
				delete();
			}
		});
	}

	/**
	 * By default this returns a valid "editing" [entity Meta name] text.
	 * @see to.etc.bugduster.pages.BasicPage#getPageTitle()
	 */
	@Override
	public String getPageTitle() {
		ClassMetaModel cmm = MetaManager.findClassMeta(getBaseClass());
		String name = cmm.getUserEntityName();
		if(name != null)
			return name;
		return getBaseClass().getName().substring(getBaseClass().getName().lastIndexOf('.') + 1);
	}

	protected void save() throws Exception {
		if(getBindings() != null)
			getBindings().moveControlToModel();
		if(!validate())
			return;
		onSave(getValue());
		UIGoto.back();
	}

	protected boolean validate() throws Exception {
		return true;
	}

	protected void cancel() throws Exception {
		UIGoto.back();
	}

	protected void delete() throws Exception {
		onDelete(getValue());
		UIGoto.back();
	}

	public boolean isDisplayonly() {
		return m_displayonly;
	}

	public void setDisplayonly(boolean displayonly) {
		if(m_displayonly == displayonly)
			return;
		m_displayonly = displayonly;
		forceRebuild();
	}

	public ModelBindings getBindings() {
		return m_bindings;
	}

	final public T getValue() throws Exception {
		if(m_value == null)
			m_value = initializeValue();
		return m_value;
	}

	final public void setValue(T val) {
		m_value = val;
	}

	protected void onSave(T object) throws Exception {
		//-- Do a commit, then exit;
		QDataContext dc = QContextManager.getContext(getPage());
		dc.startTransaction();
		dc.save(object);
		dc.commit();
	}

	protected void onDelete(T object) throws Exception {
		QDataContext dc = QContextManager.getContext(getPage());
		dc.startTransaction();
		dc.save(object);
		dc.commit();
	}
}
