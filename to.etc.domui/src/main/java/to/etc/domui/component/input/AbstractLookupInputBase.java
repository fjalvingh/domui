package to.etc.domui.component.input;

import to.etc.domui.component.binding.OldBindingHandler;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.SearchPropertyMetaModel;
import to.etc.domui.component.meta.impl.SearchPropertyMetaModelImpl;
import to.etc.domui.component.misc.FaIcon;
import to.etc.domui.component.tbl.IQueryHandler;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IActionControl;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.IForTarget;
import to.etc.domui.dom.html.IHasModifiedIndication;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Span;
import to.etc.domui.trouble.ValidationException;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.IRenderInto;
import to.etc.domui.util.Msgs;
import to.etc.util.StringTool;
import to.etc.util.WrappedException;
import to.etc.webapp.query.QCriteria;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Absolute base class of all LookupInput classes, sharing all common code.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 18-11-17.
 */
@DefaultNonNull
abstract public class AbstractLookupInputBase<QT, OT> extends Div implements IControl<OT>, ITypedControl<OT>, IHasModifiedIndication, IQueryManipulator<QT>, IForTarget {
	protected enum RebuildCause {
		CLEAR, SELECT
	}

	@Nullable
	private OT m_value;

	/**
	 * The query class/type. For Java classes this usually also defines the metamodel to use; for generic meta this should
	 * be the value record class type.
	 */
	@Nonnull
	final private Class<QT> m_queryClass;

	@Nonnull
	final private Class<OT> m_outputClass;

	/**
	 * The metamodel to use to handle the query data in this class. For Javabean data classes this is automatically
	 * obtained using MetaManager; for meta-based data models this gets passed as a constructor argument.
	 */
	@Nonnull
	final private ClassMetaModel m_queryMetaModel;

	/**
	 * The metamodel for output (display) objects.
	 */
	@Nonnull
	final private ClassMetaModel m_outputMetaModel;

	@Nonnull
	final private DefaultButton m_selButton;

	@Nonnull
	final private DefaultButton m_clearButton;

	@Nullable
	private QCriteria<QT> m_rootCriteria;

	private boolean m_mandatory;

	private boolean m_readOnly;

	private boolean m_disabled;

	@Nullable
	private String m_disabledBecause;

	@Nullable
	private IQueryManipulator<QT> m_queryManipulator;

	@Nullable
	private IQueryHandler<QT> m_queryHandler;

	/**
	 * The content renderer to use to render the current value.
	 */
	@Nullable
	private IRenderInto<OT> m_valueRenderer;

	/** Contains manually added quicksearch properties. Is null if none are added. */
	@Nullable
	private List<SearchPropertyMetaModel> m_keywordLookupPropertyList;

	/**
	 * By default set to true.
	 * Set to false in cases when keyword search functionality should be disabled regardless if metadata for this feature is defined or not.
	 */
	private boolean m_allowKeyWordSearch = true;

	@Nullable
	private String m_keySearchHint;

	/** Indication if the contents of this thing has been altered by the user. This merely compares any incoming value with the present value and goes "true" when those are not equal. */
	private boolean m_modifiedByUser;

	/**
	 * When we trigger forceRebuild, we can specify reason for this, and use this later to resolve focus after content is re-rendered.
	 */
	@Nullable
	private RebuildCause m_rebuildCause;

	private boolean m_doFocus;

	@Nullable
	private IValueChanged< ? > m_onValueChanged;

	@Nullable
	private String m_keyWordSearchCssClass;

	@Nullable
	private String m_selectionCssClass;

	abstract protected void clearKeySearch();

	protected abstract void openPopupWithClick() throws Exception;

	protected abstract boolean isPopupShown();

	/**
	 * Returns the current value of the KeySearch control, or null if there is no
	 * such control or no value.
	 */
	@Nullable
	abstract protected String getKeySearchValue();

	protected abstract void renderKeyWordSearch();

	protected abstract boolean isKeyWordSearchDefined();

	@Nullable
	protected abstract NodeBase getKeySearch();

	public AbstractLookupInputBase(@Nullable QCriteria<QT> rootCriteria, @Nonnull Class<QT> queryClass, @Nonnull Class<OT> resultClass, @Nullable ClassMetaModel queryMetaModel, @Nullable ClassMetaModel outputMetaModel) {
		m_rootCriteria = rootCriteria;
		m_queryClass = queryClass;
		m_outputClass = resultClass;
		m_queryMetaModel = queryMetaModel != null ? queryMetaModel : MetaManager.findClassMeta(queryClass);
		m_outputMetaModel = outputMetaModel != null ? outputMetaModel : MetaManager.findClassMeta(resultClass);
		setCssClass("ui-lui ctl-has-addons ui-control");

		m_selButton = new DefaultButton("", FaIcon.faSearch, b12 -> openPopupWithClick());
		//b.addCssClass("ui-lui-sel-btn");

		m_clearButton = new DefaultButton("", FaIcon.faWindowCloseO, b1 -> handleSetValue(null));
		//b.addCssClass("ui-lui-clear-btn");
	}

	@Override
	public void createContent() throws Exception {
		clearKeySearch();
		removeCssClass("ui-ro");

		OT value = m_value;
		if(value == null) {
			if(isAllowKeyWordSearch() && isKeyWordSearchDefined()) {
				if(isReadOnly() || isDisabled()) {
					renderEmptySelection();
					addCssClass("ui-ro");
				} else {
					renderKeyWordSearch();
				}
			} else {
				renderEmptySelection();
			}
		} else {
			Div vdiv = new Div("ui-lui-v ui-control");
			add(vdiv);
			IRenderInto<OT> r = getValueRenderer();
			if(r == null)
				r = new SimpleLookupInputRenderer<>(getOutputMetaModel());
			r.render(vdiv, value);
			handleSelectionCss();
		}
		appendLookupButtons();

		IActionControl clearButton = getClearButton();

		NodeBase keySearch = getKeySearch();
		if(m_rebuildCause == RebuildCause.CLEAR) {
			//User clicked clear button, so we can try to set focus to input search if possible.
			if(keySearch != null) {
				keySearch.setFocus();
			}
		} else if(m_rebuildCause == RebuildCause.SELECT) {
			//User did reselected value, so we can try to set focus to clear button if possible.
			if(clearButton != null && !clearButton.isDisabled()) {
				if(getPage().getFocusComponent() == null)
					clearButton.setFocus();
			}
		}
		m_rebuildCause = null;

		if(m_doFocus) {
			m_doFocus = false;
			if(keySearch != null)
				keySearch.setFocus();
			else if(m_clearButton != null)
				m_clearButton.setFocus();
		}
	}

	/**
	 * Render the presentation for empty/unselected input.
	 */
	private void renderEmptySelection() {
		Div vdiv = new Div("ui-lui-empty ui-control");
		add(vdiv);
		vdiv.add(new Span(Msgs.BUNDLE.getString(Msgs.UI_LOOKUP_EMPTY)));
	}

	private void appendLookupButtons() {
		if(isReadOnly()) {
			removeCssClass("ctl-has-addons");
			return;
		}
		add(getSelButton());
		add(getClearButton());

		getClearButton().setDisabled(m_value == null || isDisabled());
		getSelButton().setDisabled(isDisabled());
		getSelButton().setTestID(calcTestID() + "-lookup");
		getClearButton().setTestID(calcTestID() + "-clear");
	}

	private void handleSelectionCss() {
		String selectionCssClass = getSelectionCssClass();
		if (!StringTool.isBlank(selectionCssClass)) {
			//zilla 7548 -> if selected value txt is too large, we should be enabled to limit it, in some situations. So we use css for that.
			//When text is cutoff by that css, we have to show entire text in hover.
			//We use internal ui-lui-vcell style here, since it can not be provided from INodeContentRenderer itself :(
			//Since this is internal component code too, relaying on this internal details of renderer are not too bad
			getParent().appendShowOverflowTextAsTitleJs("." + selectionCssClass + " .ui-lui-v");
		}
	}

	@Nonnull
	private DefaultButton getSelButton() {
		return m_selButton;
	}

	@Nonnull
	private DefaultButton getClearButton() {
		return m_clearButton;
	}

	@Nullable
	protected String getDefaultKeySearchHint() {
		List<SearchPropertyMetaModel> kwlp = getKeywordLookupPropertyList();
		List<SearchPropertyMetaModel> spml = kwlp != null ? kwlp : getQueryMetaModel().getKeyWordSearchProperties();
		if(spml.size() <= 0)
			return null;

		StringBuilder sb = new StringBuilder(128);
		for(int i = 0; i < spml.size(); i++) {
			if(sb.length() > 0)
				sb.append(", ");
			SearchPropertyMetaModel spm = spml.get(i);
			if(null == spm)
				throw new IllegalStateException("null entry in keyword search list");

			appendHintFromProperty(sb, spm);
		}
		return sb.toString();
	}

	private void appendHintFromProperty(StringBuilder sb, SearchPropertyMetaModel spm) {
		if(spm.getLookupLabel() != null) {
			sb.append(spm.getLookupLabel());
		} else {
			//FIXME: vmijic 20110906 Scheduled for delete. We add extra tests and logging in code just to be sure if such cases can happen in production.
			//This should be removed soon after we are sure that problem is solved.
			PropertyMetaModel<?> pmm = spm.getProperty();
			sb.append(pmm.getDefaultLabel());
		}
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Focus handling												*/
	/*----------------------------------------------------------------------*/

	@Override
	public boolean isFocusable() {
		return false;
	}

	@Override
	public void setFocus() {
		NodeBase keySearch = getKeySearch();
		if(null != keySearch)
			keySearch.setFocus();
		else if(!isBuilt())
			m_doFocus = true;
	}

	/**
	 * Depending on what is present return the ID of a component that can
	 * receive focus.
	 */
	@Nullable
	@Override
	protected String getFocusID() {
		NodeBase forTarget = getForTarget();
		return forTarget == null ? null : forTarget.getActualID();
	}

	@Nullable @Override public NodeBase getForTarget() {
		NodeBase keySearch = getKeySearch();
		if(null != keySearch && keySearch.isAttached())
			return keySearch;
		DefaultButton selButton = m_selButton;
		if(selButton.isAttached())
			return selButton;
		DefaultButton clearButton = m_clearButton;
		if(clearButton.isAttached())
			return clearButton;
		return null;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IControl implementation.							*/
	/*--------------------------------------------------------------*/
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isMandatory() {
		return m_mandatory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMandatory(boolean mandatory) {
		m_mandatory = mandatory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isReadOnly() {
		return m_readOnly;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setReadOnly(boolean readOnly) {
		if(m_readOnly == readOnly)
			return;
		m_readOnly = readOnly;
		updateRoStyle();
		forceRebuild();
	}

	private void updateRoStyle() {
		if((m_disabled || m_readOnly) && m_value != null)
			addCssClass("ui-lui-selected-ro");
		else
			removeCssClass("ui-lui-selected-ro");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDisabled() {
		return m_disabled;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDisabled(boolean disabled) {
		if(m_disabled == disabled)
			return;
		m_disabled = disabled;
		updateRoStyle();
		forceRebuild();
	}

	@Nullable
	public String getDisabledBecause() {
		return m_disabledBecause;
	}

	public void setDisabledBecause(@Nullable String msg) {
		if(Objects.equals(msg, m_disabledBecause)) {
			return;
		}
		m_disabledBecause = msg;
		setOverrideTitle(msg);
		setDisabled(msg != null);
	}


	@Nullable
	public OT getBindValue() {
		if(m_value == null && isMandatory()) {
			throw new ValidationException(Msgs.MANDATORY);
		}
		return m_value;
	}

	public void setBindValue(@Nullable OT value) {
		setValue(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Nullable
	@Override
	public OT getValue() {
		if(m_value == null && isMandatory()) {
			setMessage(UIMessage.error(Msgs.BUNDLE, Msgs.MANDATORY));
			throw new ValidationException(Msgs.MANDATORY);
		}
		return m_value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Nullable
	public OT getValueSafe() {
		return DomUtil.getValueSafe(this);
	}

	@Override
	public void setValue(@Nullable OT v) {
		String ksValue = getKeySearchValue();
		if(DomUtil.isEqual(m_value, v) && ksValue == null)
			return;
		m_value = v;
		if(v != null) {
			getClearButton().setDisabled(false);
			clearMessage();
			addCssClass("ui-lui-selected");
			String selectionCss = getSelectionCssClass();
			if (!StringTool.isBlank(selectionCss)){
				addCssClass(DomUtil.nullChecked(selectionCss));
			}
		} else {
			getClearButton().setDisabled(true);
			removeCssClass("ui-lui-selected");
		}
		updateRoStyle();
		forceRebuild();
	}

	/**
	 * In case that new value is different than one previously selected, set modified flag, selected value and trigger onValueChange event if defined.
	 * @param value
	 * @throws Exception
	 */
	protected void handleSetValue(@Nullable OT value) throws Exception {
		if(!MetaManager.areObjectsEqual(value, m_value, null)) {
			DomUtil.setModifiedFlag(this);
			setValue(value);

			try {
				OldBindingHandler.controlToModel(this);
			} catch(Exception x) {
				throw WrappedException.wrap(x);
			}

			//-- Handle onValueChanged
			IValueChanged< ? > onValueChanged = getOnValueChanged();
			if(onValueChanged != null) {
				((IValueChanged<NodeBase>) onValueChanged).onValueChanged(this);
			}
		}
		m_rebuildCause = value == null ? RebuildCause.CLEAR : RebuildCause.SELECT;
	}

	@Override
	@Nullable
	public QCriteria<QT> adjustQuery(@Nonnull QCriteria<QT> enteredCriteria) {
		IQueryManipulator<QT> qm = getQueryManipulator();
		QCriteria<QT> result = enteredCriteria;
		if(qm != null) {
			result = qm.adjustQuery(enteredCriteria);
			if(result == null) {
				//in case of cancelled search by query manipulator return
				return null;
			}
		}

		//-- Join any root criteria, if applicable
		QCriteria<QT> root = m_rootCriteria;
		if(null != root) {
			//-- We merge the "root" criteria inside the "child" criteria. We do that by a complete "and", as follows:
			//-- result = (root criteria) AND (entered criteria), and we ignore any "other" part of the root criterion.
			result.mergeCriteria(root);
		}
		return result;
	}




	/*--------------------------------------------------------------*/
	/*	CODING:	IHasModifiedIndication impl							*/
	/*--------------------------------------------------------------*/
	/**
	 * Returns the modified-by-user flag.
	 * @see to.etc.domui.dom.html.IHasModifiedIndication#isModified()
	 */
	@Override
	public boolean isModified() {
		return m_modifiedByUser;
	}

	/**
	 * Set or clear the modified by user flag.
	 * @see to.etc.domui.dom.html.IHasModifiedIndication#setModified(boolean)
	 */
	@Override
	public void setModified(boolean as) {
		m_modifiedByUser = as;
	}


	/**
	 * The value without any consequences
	 */
	@Nullable
	public OT getWorkValue() {
		OT valueSafe = getValueSafe();
		clearMessage();
		return valueSafe;
	}

	@Override
	public boolean hasError() {
		getValueSafe();
		return super.hasError();
	}


	/**
	 * The content renderer to use to render the current value.
	 */
	@Nullable
	public IRenderInto<OT> getValueRenderer() {
		return m_valueRenderer;
	}

	public void setValueRenderer(@Nullable IRenderInto<OT> contentRenderer) {
		m_valueRenderer = contentRenderer;
	}

	@Override
	@Nullable
	public IValueChanged< ? > getOnValueChanged() {
		if(isPopupShown()) {
			//Fix for FF: prevent onchange event to be propagate on control when return key is pressed and popup is opened.
			//This does not happen on IE. Be sure that it is executed after popup is already closed.
			return null;
		}
		return m_onValueChanged;
	}

	@Override
	public void setOnValueChanged(@Nullable IValueChanged< ? > onValueChanged) {
		m_onValueChanged = onValueChanged;
	}


	/**
	 * When set the specified manipulator will be called before a query is sent to the database. The query
	 * can be altered to add extra restrictions for instance.
	 * @return
	 */
	@Nullable
	public IQueryManipulator<QT> getQueryManipulator() {
		return m_queryManipulator;
	}

	/**
	 * When set the specified manipulator will be called before a query is sent to the database. The query
	 * can be altered to add extra restrictions for instance.
	 *
	 * @param queryManipulator
	 */
	public void setQueryManipulator(@Nullable IQueryManipulator<QT> queryManipulator) {
		m_queryManipulator = queryManipulator;
	}

	/**
	 * The query handler to use, if a special one is needed. The default query handler will use the
	 * normal conversation-associated DataContext to issue the query.
	 * @return
	 */
	@Nullable
	public IQueryHandler<QT> getQueryHandler() {
		return m_queryHandler;
	}

	public void setQueryHandler(@Nullable IQueryHandler<QT> queryHandler) {
		m_queryHandler = queryHandler;
	}

	public boolean isAllowKeyWordSearch() {
		return m_allowKeyWordSearch;
	}

	public void setAllowKeyWordSearch(boolean allowKeyWordSearch) {
		m_allowKeyWordSearch = allowKeyWordSearch;
	}


	/**
	 * REMOVED: There is no need to use this: add any css class on the control itself and use CSS to address the
	 * inner control.
	 */
	@Deprecated
	@Nullable
	public String getKeyWordSearchCssClass() {
		return m_keyWordSearchCssClass;
	}

	/**
	 * REMOVED: There is no need to use this: add any css class on the control itself and use CSS to address the
	 * inner control.
	 * Set custom css that would be applied only in case that component is rendering keyWordSearch.
	 * Used for example in row inline rendering, where width and min-width should be additionaly customized.
	 * @param cssClass
	 */
	@Deprecated
	public void setKeyWordSearchCssClass(@Nullable String cssClass) {
		m_keyWordSearchCssClass = cssClass;
	}

	@Nullable
	public String getSelectionCssClass() {
		return m_selectionCssClass;
	}

	/**
	 * Set a hint text for this control, for some reason only on the select button??
	 */
	public void setHint(@Nonnull String text) {
		if(m_selButton != null)
			m_selButton.setTitle(text);
	}

	/**
	 * Set custom css that would be applied only in case that component is rendering selected value.
	 * Used for example where max-width should be additionally customized.
	 * @param cssClass
	 */
	public void setSelectionCssClass(@Nullable String cssClass) {
		m_selectionCssClass = cssClass;
	}

	@Nullable
	public String getKeySearchHint() {
		return m_keySearchHint;
	}

	/**
	 * Set hint to keyword search input. Usually says how search condition is resolved.
	 */
	public void setKeySearchHint(@Nullable String keySearchHint) {
		m_keySearchHint = keySearchHint;
	}

	@Nonnull
	public Class<OT> getOutputClass() {
		return m_outputClass;
	}

	@Nonnull @Override public Class<OT> getActualType() {
		return m_outputClass;
	}

	@Nonnull
	public Class<QT> getQueryClass() {
		return m_queryClass;
	}

	@Nonnull
	public ClassMetaModel getOutputMetaModel() {
		return m_outputMetaModel;
	}

	@Nonnull
	public ClassMetaModel getQueryMetaModel() {
		return m_queryMetaModel;
	}

	@Nullable public QCriteria<QT> getRootCriteria() {
		return m_rootCriteria;
	}

	/**
	 * Define a property to use for quick search. When used this overrides any metadata-defined
	 * properties.
	 */
	public void addKeywordProperty(@Nonnull String name, int minlen) {
		if(m_keywordLookupPropertyList == null)
			m_keywordLookupPropertyList = new ArrayList<>();
		PropertyMetaModel<?> pmm = getQueryMetaModel().getProperty(name);
		SearchPropertyMetaModelImpl si = new SearchPropertyMetaModelImpl(getQueryMetaModel(), pmm);
		if(minlen > 0)
			si.setMinLength(minlen);
		si.setIgnoreCase(true);
		DomUtil.nullChecked(m_keywordLookupPropertyList).add(si);
	}

	/**
	 * Define a property to use for quick search. When used this overrides any metadata-defined
	 * properties.
	 */
	public void addKeywordProperty(@Nonnull String name) {
		addKeywordProperty(name, -1);
	}

	/**
	 * Not normally used; use {@link #addKeywordProperty(String, int)} instead.
	 */
	public void setKeywordSearchProperties(@Nonnull List<SearchPropertyMetaModel> keywordLookupPropertyList) {
		m_keywordLookupPropertyList = keywordLookupPropertyList;
	}

	@Nullable
	public List<SearchPropertyMetaModel> getKeywordLookupPropertyList() {
		return m_keywordLookupPropertyList;
	}

}
