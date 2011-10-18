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
package to.etc.domui.pages.generic;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.form.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;
import to.etc.webapp.query.*;

/**
 * Basic stuff to handle editing a simple entity.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 22, 2008
 */
public abstract class BasicEditPage<T> extends BasicPage<T> {
	private ButtonBar m_buttonBar;

	private boolean m_deleteable;

	private boolean m_displayonly;

	private TabularFormBuilder m_formBuilder;

	private ModelBindings m_bindings;

	abstract public T getInstance() throws Exception;

	public BasicEditPage(Class<T> valueClass) {
		this(valueClass, false);
	}

	public BasicEditPage(Class<T> valueClass, boolean deleteable) {
		super(valueClass);
		m_deleteable = deleteable;
	}

	public TabularFormBuilder getBuilder() throws Exception {
		if(m_formBuilder == null)
			m_formBuilder = new TabularFormBuilder(getInstance());
		return m_formBuilder;
	}

	@Override
	final public void createContent() throws Exception {
		if(m_formBuilder != null)
			m_formBuilder.reset();
		m_buttonBar = null;

		super.createContent(); // Page title and crud
		createButtonBar();
		createButtons();
		createEditableBase();
	}

	private void createEditableBase() throws Exception {
		m_bindings = createEditable();
		if(m_bindings == null) {
			if(m_formBuilder != null) {
				NodeContainer nc = m_formBuilder.finish();
				if(nc != null) {
					add(nc);
					m_bindings = m_formBuilder.getBindings();
				}
			}
		}
		if(m_bindings == null)
			throw new IllegalStateException("The form's bindings are undefined: please override createForm.");
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
		onSave(getInstance());
		UIGoto.back();
	}

	protected boolean validate() throws Exception {
		return true;
	}

	protected void cancel() throws Exception {
		UIGoto.back();
	}

	protected void delete() throws Exception {
		onDelete(getInstance());
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

	protected void onSave(T object) throws Exception {
		//-- Do a commit, then exit;
		QDataContext dc = QContextManager.getContext(getPage());
		dc.startTransaction();
		saveObject(dc, object);
		dc.commit();
	}

	protected void saveObject(QDataContext dc, T object) throws Exception {
		dc.save(object);
	}

	protected void onDelete(T object) throws Exception {
		QDataContext dc = QContextManager.getContext(getPage());
		dc.startTransaction();
		deleteObject(dc, object);
		dc.commit();
	}

	protected void deleteObject(QDataContext dc, T object) throws Exception {
		dc.delete(object);
	}
}
