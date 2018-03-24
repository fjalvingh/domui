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

import javax.annotation.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.controlfactory.*;
import to.etc.domui.component.form.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;
import to.etc.domui.themes.*;
import to.etc.webapp.query.*;

/**
 * DO NOT USE - ancient and badly written.
 *
 * Basic stuff to handle editing a simple entity.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 22, 2008
 */
@Deprecated
public abstract class BasicEditPage<T> extends BasicPage<T> {
	private IButtonBar m_buttonBar;

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
		if(!onBeforeCreateContent())
			return;

		createButtonBar();
		createButtons();
		createEditableBase();
		onAfterCreateContent();
	}

	protected boolean onBeforeCreateContent() throws Exception {
		return true;
	}

	protected void onAfterCreateContent() throws Exception {
	}

	private void createEditableBase() throws Exception {
		m_bindings = createEditable();

		if(m_formBuilder != null) {
			NodeContainer nc = m_formBuilder.finish();
			if(nc != null) {
				Panel p = new Panel();
				add(p);
				p.add(nc);
				if(m_bindings == null)
					m_bindings = m_formBuilder.getBindings();
			}
		}
		if(m_bindings != null)
			m_bindings.moveModelToControl();
	}

	protected ModelBindings createEditable() throws Exception {
		return null;
	}

	protected void createButtonBar() {
		add((ButtonBar) getButtonBar());
	}

	public IButtonBar getButtonBar() {
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
			@Override
			public void clicked(@Nonnull DefaultButton b) throws Exception {
				save();
			}
		});
	}

	protected void createCancelButton() {
		getButtonBar().addButton("!Cancel", Theme.BTN_CANCEL, new IClicked<DefaultButton>() {
			@Override
			public void clicked(@Nonnull DefaultButton b) throws Exception {
				cancel();
			}
		});
	}

	protected void createDeleteButton() {
		getButtonBar().addConfirmedButton("!Delete", "THEME/btnDelete.png", "Delete: are you sure?", new IClicked<DefaultButton>() {
			@Override
			public void clicked(@Nonnull DefaultButton b) throws Exception {
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
		onAfterSave();
	}

	protected void onAfterSave() {
		UIGoto.back();
	}


	protected boolean validate() throws Exception {
		return true;
	}

	protected void cancel() throws Exception {
		UIGoto.back();
	}

	protected void delete() throws Exception {
		if(onDelete(getInstance()))
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

	protected void onSave(@Nonnull T object) throws Exception {
		//-- Do a commit, then exit;
		QDataContext dc = getSharedContext();
		dc.startTransaction();
		saveObject(dc, object);
		dc.commit();
	}

	protected void saveObject(@Nonnull QDataContext dc, @Nonnull T object) throws Exception {
		dc.save(object);
	}

	protected boolean onDelete(@Nonnull T object) throws Exception {
		QDataContext dc = getSharedContext();
		dc.startTransaction();
		boolean res = deleteObject(dc, object);
		dc.commit();
		return res;
	}

	protected boolean deleteObject(@Nonnull QDataContext dc, @Nonnull T object) throws Exception {
		dc.delete(object);
		return true;
	}
}
