package to.etc.domui.component2.form4;

import kotlin.reflect.KProperty0;
import kotlin.reflect.KProperty1;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.binding.BindReference;
import to.etc.domui.component.binding.IBidiBindingConverter;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.impl.PropertyMetaModelWrapper;
import to.etc.domui.component.misc.IIconRef;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.component2.controlfactory.ControlCreatorRegistry;
import to.etc.domui.dom.html.BindingBuilderBidi;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.Label;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.server.DomApplication;
import to.etc.domui.util.DomUtil;
import to.etc.webapp.ProgrammerErrorException;
import to.etc.webapp.annotations.GProperty;
import to.etc.webapp.nls.IBundleCode;
import to.etc.webapp.query.QField;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;

/**
 * Yet another attempt at a generic form builder, using the Builder pattern. The builder
 * starts in vertical mode - call horizontal() to move horizontally.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 17, 2014
 */
final public class FormBuilder {

	/**
	 * Handle adding nodes generated by the form builder to the page.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Jun 13, 2012
	 */
	interface IAppender {
		void add(@NonNull NodeBase formNode);
	}

	@NonNull
	final private IAppender m_appender;

	private IFormLayouter m_layouter;

	private boolean m_horizontal;

	private boolean m_append;

	private boolean m_currentDirection;

	private static boolean m_defaultShowHintAsIcon;

	private Boolean m_showHintAsIcon;

	@NonNull
	static private BiConsumer<NodeContainer, String> m_defaultHintRenderer = (node, text) -> {
		if(null == text)
			return;

		IIconRef ir = Icon.faInfoCircle.css("ui-f4-hinticon");
		NodeBase irNode = ir.createNode();
		node.add(irNode);

		irNode.setClicked(a -> {
			Div content = new Div();
			DomUtil.renderHtmlString(content, text);
			MsgBox.info(node, content);
		});
	};

	@Nullable
	private BiConsumer<NodeContainer, String> m_hintRenderer;

	/** While set, all controls added will have their readOnly property bound to this reference unless otherwise specified */
	@Nullable
	private BindReference<?, Boolean> m_readOnlyGlobal;

	@Nullable
	private BindReference<?, String> m_disabledMessageGlobal;

	@Nullable
	private BindReference<?, Boolean> m_disabledGlobal;

	@Nullable
	private BuilderData<?> m_currentBuilder;

	public FormBuilder(@NonNull IAppender appender) {
		m_appender = appender;
		m_layouter = new ResponsiveFormLayouter(appender);
//		m_layouter = new TableFormLayouter(appender);
	}

	public FormBuilder(@NonNull IFormLayouter layout) {
		m_appender = layout;
		m_layouter = layout;
	}

	public FormBuilder(@NonNull final NodeContainer nb) {
		this(nb::add);
	}

	@NonNull
	public FormBuilder append() {
		m_append = true;
		return this;
	}


	public FormBuilder nl() {
		m_layouter.clear();
		return this;
	}

	@NonNull
	public FormBuilder horizontal() {
		m_horizontal = true;
		m_layouter.setHorizontal(true);
		return this;
	}

	@NonNull
	public FormBuilder vertical() {
		m_horizontal = false;
		m_layouter.setHorizontal(false);
		return this;
	}

	static private <I, V> BindReference<I, V> createRef(@NonNull I instance, @NonNull String property, @NonNull Class<V> type) {
		if(null == instance)
			throw new ProgrammerErrorException("The instance for a formbuilder property cannot be null");
		PropertyMetaModel<?> pmm = MetaManager.getPropertyMeta(instance.getClass(), property);
		if(DomUtil.getBoxedForPrimitive(pmm.getActualType()) != DomUtil.getBoxedForPrimitive(type)) {
			throw new ProgrammerErrorException(pmm + " must be of type " + type.getName());
		}
		return new BindReference<>(instance, (PropertyMetaModel<V>) pmm);

	}

	static private <I, V> BindReference<I, V> createRef(@NonNull I instance, @NonNull QField<I, V> property) {
		if(null == instance)
			throw new ProgrammerErrorException("The instance for a formbuilder property cannot be null");
		PropertyMetaModel<V> pmm = MetaManager.getPropertyMeta(instance.getClass(), property);
		//if(DomUtil.getBoxedForPrimitive(pmm.getActualType()) != DomUtil.getBoxedForPrimitive(property.)) {
		//	throw new ProgrammerErrorException(pmm + " must be of type " + type.getName());
		//}
		return new BindReference<>(instance, pmm);
	}
	public FormBuilder hintAsIcon(boolean yes) {
		m_showHintAsIcon = yes;
		return this;
	}

	@Nullable
	public Boolean getShowHintAsIcon() {
		return m_showHintAsIcon;
	}

	/**
	 * By default bind all next components' readOnly property to the specified Boolean property. This binding
	 * takes effect except if a more detailed readOnly binding is specified.
	 */
	@NonNull
	public <I> FormBuilder readOnlyAll(@NonNull I instance, @NonNull String property) {
		m_readOnlyGlobal = createRef(instance, property, Boolean.class);
		return this;
	}

	/**
	 * Clear the global "read only" binding as set by {@link #readOnlyAll(Object, String)}, so that components
	 * after this are no longer bound to the previously set property.
	 */
	@NonNull
	public FormBuilder readOnlyAllClear() {
		m_readOnlyGlobal = null;
		return this;
	}

	/**
	 * By default bind all next components' disabled property to the specified Boolean property. This binding
	 * takes effect except if a more detailed binding is specified.
	 */
	@NonNull
	public <I> FormBuilder disabledAll(@NonNull I instance, @NonNull String property) {
		m_disabledGlobal = createRef(instance, property, Boolean.class);
		return this;
	}

	/**
	 * Clear the global "disabled" binding as set by {@link #disabledAll(Object, String)}, so that components
	 * after this are no longer bound to the previously set property.
	 */
	@NonNull
	public FormBuilder disabledAllClear() {
		m_disabledGlobal = null;
		return this;
	}

	@NonNull
	public <I> FormBuilder disabledBecauseAll(@NonNull I instance, @NonNull String property) {
		m_disabledMessageGlobal = createRef(instance, property, String.class);
		return this;
	}

	@NonNull
	public FormBuilder disabledBecauseClear() {
		m_disabledMessageGlobal = null;
		return this;
	}

	/*--------------------------------------------------------------*/
	/*	CODING: defining (manually created) controls.				*/
	/*--------------------------------------------------------------*/

	@NonNull
	public <T> UntypedControlBuilder<T> property(@NonNull T instance, @GProperty String property) {
		if(null == instance)
			throw new ProgrammerErrorException("The instance for a formbuilder property cannot be null");
		check();
		UntypedControlBuilder<T> currentBuilder = new UntypedControlBuilder<>(instance, MetaManager.getPropertyMeta(instance.getClass(), property));
		m_currentBuilder = currentBuilder;
		return currentBuilder;
	}

	private void check() {
		if(null != m_currentBuilder)
			throw new IllegalStateException("You need to end the builder pattern with a call to 'control()'");
	}

	@NonNull
	public <T, V> TypedControlBuilder<T, V> property(@NonNull T instance, QField<?, V> property) {
		if(null == instance)
			throw new ProgrammerErrorException("The instance for a formbuilder property cannot be null");
		check();
		TypedControlBuilder<T, V> builder = new TypedControlBuilderWithInstance<>(instance, MetaManager.getPropertyMeta(instance.getClass(), property));
		m_currentBuilder = builder;
		return builder;
	}

	/**
	 * Kotlin specific entrypoint. It sucks because it does not define a type parameter for the receiver.
	 *
	 * jal 20200520: currently very hard to implement. Because the receiver is missing we do not know
	 * the class this comes from, and that makes it very difficult to derive the class-based metadata
	 * for the property. The engine calculating these (MetaInitializer) expects to have a normal class
	 * model and the appropriate ways to discover base class and annotations. While at least annotations
	 * are available on the KProperty0 they are not in the correct form, so we would need to largely
	 * re write the annotation discovery logic.
	 */
	@NonNull
	public <V> TypedControlBuilder<?, V> property(@NonNull KProperty0<V> propertyRef) {
		check();

		TypedControlBuilder<?, V> builder = new TypedControlBuilder<>(MetaManager.getPropertyMeta(propertyRef));
		m_currentBuilder = builder;
		return builder;
	}

	/**
	 * Reference to a Kotlin property itself, i.e. ClassName::property.
	 */
	@NonNull
	public <T, V> TypedControlBuilder<T, V> property(@NonNull T instance, @NonNull KProperty1<T, V> propertyRef) {
		if(null == instance)
			throw new ProgrammerErrorException("The instance for a formbuilder property cannot be null");
		check();

		TypedControlBuilder<T, V> builder = new TypedControlBuilderWithInstance<>(instance, MetaManager.getPropertyMeta(instance.getClass(), propertyRef));
		m_currentBuilder = builder;
		return builder;
	}


	//@NonNull
	//public <T, V, C> ControlBuilder<T, V, C> property(@NonNull T instance, QField<?, V> property, IBidiBindingConverter<C, V> converter) {
	//	if(null != m_currentBuilder)
	//		throw new IllegalStateException("You need to end the builder pattern with a call to 'control()'");
	//	ControlBuilder<T, V, C> builder = new ControlBuilder<>(instance, MetaManager.getPropertyMeta(instance.getClass(), property), converter);
	//	m_currentBuilder = builder;
	//	return builder;
	//}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Propertyless items.											*/
	/*----------------------------------------------------------------------*/

	public ItemBuilder label(IBundleCode code, Object... param) {
		return label(code.format(param));
	}

	public ItemBuilder label(@Nullable String label) {
		check();
		ItemBuilder b = new ItemBuilder().label(label);
		m_currentBuilder = b;
		return b;
	}

	public ItemBuilder label(@Nullable NodeContainer label) {
		check();
		ItemBuilder b = new ItemBuilder().label(label);
		m_currentBuilder = b;
		return b;
	}

	public void item(@NonNull NodeBase item) throws Exception {
		addControl(new ItemBuilder(), item, null);
		resetBuilder();
	}

	public void control(@NonNull NodeBase item) throws Exception {
		addControl(new ItemBuilder(), item, null);
		resetBuilder();
	}

	private void resetBuilder() {
		m_append = false;
		m_currentBuilder = null;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Form building code.									*/
	/*--------------------------------------------------------------*/

	/**
	 * @param <I>		The record instance type.
	 * @param <MV>		The property (model) value.
	 * @param <UI>		The control's type, as a construct of all of the above.
	 */
	@NonNull
	private <I, MV, CV, UI extends IControl<CV>> UI controlMain(BuilderData<MV> cb, @Nullable Class<UI> controlClass) throws Exception {
		ControlCreatorRegistry builder = DomApplication.get().getControlCreatorRegistry();
		PropertyMetaModel<MV> pmm = cb.m_propertyMetaModel;
		PropertyMetaModel<CV> cpmm = null;
		IBidiBindingConverter<?, ?> converter = cb.m_converter;
		if(null != converter) {
			//-- Determine the control type from the converter instance.
			Type[] giAr = converter.getClass().getGenericInterfaces();
			for(Type iface : giAr) {
				if(iface instanceof ParameterizedType) {
					ParameterizedType pt = (ParameterizedType) iface;
					if(pt.getRawType().getTypeName().equals(IBidiBindingConverter.class.getName())) {
						Type[] tp = pt.getActualTypeArguments();
						if(tp != null && tp.length >= 1) {
							Class<?> controlType = (Class<?>) tp[0];

							//-- Now: create a wrapper around the meta model to alter the type
							cpmm = new PropertyMetaModelWrapper<CV>((PropertyMetaModel<CV>) pmm) {
								@NonNull
								@Override
								public ClassMetaModel getClassModel() {
									return getWrappedModel().getClassModel();
								}

								@NonNull
								@Override
								public Class<CV> getActualType() {
									return (Class<CV>) controlType;
								}
							};
						}
					}
				}
			}
		}
		if(cpmm == null)
			cpmm = (PropertyMetaModel<CV>) pmm;

		UI control = builder.createControl(cpmm, controlClass);
		addControl(cb, (NodeBase) control, converter);
		resetBuilder();
		return control;
	}

	private <I, V> void addControl(BuilderData<V> builder, @NonNull NodeBase control, @Nullable IBidiBindingConverter<?, ?> conv) throws Exception {
		PropertyMetaModel<?> pmm = builder.m_propertyMetaModel;

		if (control.getClass().getSimpleName().contains("TextArea")
			&& builder.m_labelCss == null) {
			builder.m_labelCss = "ui-f4-ta";
		}

		NodeContainer lbl = builder.determineLabel();
		resetDirection();

		String hintText = builder.m_hintText;
		if(null == hintText && pmm != null) {
			hintText = pmm.getDefaultHint();
		}
		boolean hintAsIcon = m_defaultShowHintAsIcon;
		Boolean b = m_showHintAsIcon;
		if(null != b)
			hintAsIcon = b;

		m_layouter.addControl(control, lbl, hintAsIcon ? hintText : null, builder.m_controlCss, builder.m_labelCss, m_append, getHintRenderer());

		String testid = builder.m_testid;
		if(null != testid)
			control.setTestID(testid);
		else if(control.getTestID() == null) {
			if(pmm != null)
				control.setTestID(pmm.getName());
		}

		if(control instanceof IControl) {
			IControl< ? > ctl = (IControl< ? >) control;

			if(!hintAsIcon) {
				ctl.setHint(hintText);
			}
			if(null != pmm) {
				if(builder instanceof IHasInstance) {
					Object instance = ((IHasInstance<?>) builder).getInstance();
					if(null != instance) {
						//IBidiBindingConverter<Object, Object> conv = (IBidiBindingConverter<Object, Object>) builder.m_converter;
						if(null == conv) {
							control.bind().to(instance, pmm);
						} else {
							BindingBuilderBidi<?> bind = control.bind();
							((BindingBuilderBidi<Object>) bind).to(instance, (PropertyMetaModel<Object>) pmm, (IBidiBindingConverter<Object, Object>) conv);
						}
					}
				}

				//FIXME Handle Kotlin KProperty0
			}

			//-- Do all the readOnly chores
			Boolean readOnly = builder.m_readOnly;
			BindReference<?, Boolean> roOnce = builder.m_readOnlyOnce;
			BindReference<?, Boolean> roGlob = m_readOnlyGlobal;
			if(null != readOnly) {
				ctl.setReadOnly(readOnly.booleanValue());
			} else if(roOnce != null) {
				control.bind("readOnly").to(roOnce);
			} else if(roGlob != null) {
				control.bind("readOnly").to(roGlob);
			}

			//-- Same for disabled - prefer message above the boolean disabled.
			String diMsg = builder.m_disabledMessage;
			BindReference<?, String> diMsgOnce = builder.m_disabledMessageOnce;
			BindReference<?, String> diMsgGlob = m_disabledMessageGlobal;
			Boolean di = builder.m_disabled;
			BindReference<?, Boolean> diOnce = builder.m_disabledOnce;
			BindReference<?, Boolean> diGlob = m_disabledGlobal;

			if(diMsg != null) {
				//ctl.setDisabledBecause(diMsg);			// FIXME
				ctl.setDisabled(true);
			} else if(diMsgOnce != null) {
				control.bind("disabledBecause").to(diMsgOnce);
			} else if(diMsgGlob != null) {
				control.bind("disabledBecause").to(diMsgGlob);
			} else if(di != null) {
				ctl.setDisabled(di.booleanValue());
			} else if(diOnce != null) {
				control.bind("disabled").to(diOnce);
			} else if(diGlob != null) {
				control.bind("disabled").to(diGlob);
			}

			if(builder.isMandatory()) {
				ctl.setMandatory(true);
			}
		}

		String label = builder.labelTextCalculated();
		if (null != builder.m_errorLocation){
			control.setErrorLocation(builder.m_errorLocation);
		} else {
			if(null != label) {
				control.setErrorLocation(label);
			}
		}
		if(null != label)
			control.setCalculcatedId(label.toLowerCase());
	}

	private void resetDirection() {
		if(m_horizontal == m_currentDirection)
			return;
		m_layouter.clear();
		m_currentDirection = m_horizontal;
	}


	public void appendAfterControl(@NonNull NodeBase what) {
		m_layouter.appendAfterControl(what);
	}

	private class BuilderWithInstanceData<I, V> extends BuilderData<V> implements IHasInstance<I> {
		private final I m_instance;

		public BuilderWithInstanceData(I instance, PropertyMetaModel<V> propertyMetaModel) {
			super(propertyMetaModel);
			m_instance = instance;
		}

		@Override
		public I getInstance() {
			return m_instance;
		}
	}

	private interface IHasInstance<I> {
		@Nullable
		I getInstance();
	}

	private class BuilderData<V> {
		protected final PropertyMetaModel<V> m_propertyMetaModel;

		protected String m_errorLocation;

		protected String m_nextLabel;

		@Nullable
		protected String m_hintText;

		protected NodeContainer m_nextLabelControl;

		protected Boolean m_mandatory;

		@Nullable
		protected String m_controlCss;

		@Nullable
		protected String m_labelCss;

		@Nullable
		protected String m_testid;

		/** ReadOnly as set directly in the Builder */
		protected Boolean m_readOnly;

		/** When set, the next control's readOnly property will be bound to this reference, after which it will be cleared */
		@Nullable
		protected BindReference<?, Boolean> m_readOnlyOnce;

		/** disabled as set directly in the Builder */
		protected Boolean m_disabled;

		@Nullable
		protected BindReference<?, Boolean> m_disabledOnce;

		/** When set, disable the next component with the specified message. */
		@Nullable
		protected String m_disabledMessage;

		@Nullable
		protected BindReference<?, String> m_disabledMessageOnce;

		@Nullable
		protected IBidiBindingConverter<?, ?> m_converter;

		public BuilderData(PropertyMetaModel<V> propertyMetaModel) {
			m_propertyMetaModel = propertyMetaModel;
		}

		@Nullable public NodeContainer determineLabel() {
			NodeContainer res = null;
			String txt = m_nextLabel;
			if(null != txt) {
				//m_nextLabel = null;
				if(!txt.isEmpty())					// Not "unlabeled"?
					res = new Label(txt);
			} else {
				res = m_nextLabelControl;
				if(res == null) {
					//-- Property known?
					PropertyMetaModel< ? > pmm = m_propertyMetaModel;
					if(null != pmm) {
						txt = pmm.getDefaultLabel();
						if(txt != null && !txt.isEmpty())
							res = new Label(txt);
					}
				}
			}
			if(res != null && calculateMandatory() && !isReadOnly()) {
				res.addCssClass("ui-f4-mandatory");
			}

			return res;
		}

		private boolean calculateMandatory() {
			Boolean m = m_mandatory;
			if(null != m)
				return m.booleanValue();						// If explicitly set: obey that
			PropertyMetaModel<?> pmm = m_propertyMetaModel;
			if(null != pmm) {
				return pmm.isRequired();
			}
			return false;
		}

		@Nullable
		private String labelTextCalculated() {
			String txt = m_nextLabel;
			if(null != txt) {
				if(!txt.isEmpty())					// Not "unlabeled"?
					return txt;
				return null;
			} else {
				NodeContainer res = m_nextLabelControl;
				if(res != null) {
					return res.getTextContents();
				} else {
					//-- Property known?
					PropertyMetaModel< ? > pmm = m_propertyMetaModel;
					if(null != pmm) {
						txt = pmm.getDefaultLabel();
						if(txt != null && !txt.isEmpty())
							return txt;
					}
				}
			}
			return null;
		}

		private boolean isMandatory() {
			Boolean man = m_mandatory;
			if(null != man) {
				return man.booleanValue();
			}
			return false;
		}

		private boolean isReadOnly() {
			Boolean ro = m_readOnly;
			if(null != ro) {
				return ro.booleanValue();
			}
			return false;
		}

		protected void copyFrom(ItemBuilder o) {
			this.m_controlCss = o.m_controlCss;
			this.m_disabled = o.m_disabled;
			this.m_disabledMessage = o.m_disabledMessage;
			this.m_disabledMessageOnce = o.m_disabledMessageOnce;
			this.m_disabledOnce = o.m_disabledOnce;
			this.m_errorLocation = o.m_errorLocation;
			this.m_labelCss = o.m_labelCss;
			this.m_mandatory = o.m_mandatory;
			this.m_nextLabel = o.m_nextLabel;
			this.m_nextLabelControl = o.m_nextLabelControl;
			this.m_readOnly = o.m_readOnly;
			this.m_readOnlyOnce = o.m_readOnlyOnce;
			this.m_testid = o.m_testid;
			this.m_hintText = o.m_hintText;
		}
	}

	/**
	 * This builder is for propertyless items, and hence does not contain type information.
	 */
	final public class ItemBuilder extends BuilderData<Void>{
		public ItemBuilder() {
			super(null);
		}

		/*--------------------------------------------------------------*/
		/*	CODING:	Label control.										*/
		/*--------------------------------------------------------------*/
		@NonNull
		public ItemBuilder label(@Nullable String label) {
			if(null != m_nextLabelControl)
				throw new IllegalStateException("You already set a Label instance");
			m_nextLabel = label;
			return this;
		}

		@NonNull
		public ItemBuilder label(@Nullable NodeContainer label) {
			if(null != m_nextLabel)
				throw new IllegalStateException("You already set a String label instance");
			m_nextLabelControl = label;
			return this;
		}

		public ItemBuilder hint(@Nullable String s) {
			m_hintText = s;
			return this;
		}

		public ItemBuilder hint(@Nullable IBundleCode code) {
			m_hintText = code == null ? null : code.getString();
			return this;
		}

		@NonNull
		public ItemBuilder unlabeled() {
			label("");
			return this;
		}

		@NonNull
		public ItemBuilder mandatory() {
			m_mandatory = Boolean.TRUE;
			return this;
		}

		@NonNull
		public ItemBuilder mandatory(boolean yes) {
			m_mandatory = Boolean.valueOf(yes);
			return this;
		}

		@NonNull
		public ItemBuilder readOnly() {
			m_readOnly = Boolean.TRUE;
			return this;
		}

		/**
		 * Force the next component to have the specified value for readOnly.
		 */
		@NonNull
		public ItemBuilder readOnly(boolean ro) {
			m_readOnly = Boolean.valueOf(ro);
			return this;
		}

		/**
		 * Bind only the next component to the specified boolean property. See
		 */
		@NonNull
		public <X> ItemBuilder readOnly(@NonNull X instance, @NonNull String property) {
			m_readOnlyOnce = createRef(instance, property, Boolean.class);
			return this;
		}

		/**
		 * Bind only the next component to the specified boolean property. See
		 */
		@NonNull
		public <X> ItemBuilder readOnly(@NonNull X instance, @NonNull QField<X, Boolean> property) {
			m_readOnlyOnce = createRef(instance, property);
			return this;
		}

		public void item(@NonNull NodeBase item) throws Exception {
			addControl(this, item, null);
			resetBuilder();
		}

		public void control(@NonNull NodeBase item) throws Exception {
			addControl(this, item, null);
			resetBuilder();
		}

		@NonNull
		public <T> UntypedControlBuilder<T> property(@NonNull T instance, @GProperty String property) {
			UntypedControlBuilder<T> currentBuilder = new UntypedControlBuilder<>(instance, MetaManager.getPropertyMeta(instance.getClass(), property));
			m_currentBuilder = currentBuilder;				// This is now current

			//-- Copy all fields.
			currentBuilder.copyFrom(this);
			return currentBuilder;
		}
	}

	/**
	 * A builder that will end in a control, with full types - used with QFields.
	 *
	 * @param <I>	The instance.
	 * @param <V>	The value type of the property of the instance.
	 */
	public class TypedControlBuilder<I, V> extends BuilderData<V> {
		public TypedControlBuilder(PropertyMetaModel<V> propertyMeta) {
			super(propertyMeta);
		}

		/*--------------------------------------------------------------*/
		/*	CODING:	Label control.										*/
		/*--------------------------------------------------------------*/
		@NonNull
		public TypedControlBuilder<I, V> label(@NonNull String label) {
			if(null != m_nextLabelControl)
				throw new IllegalStateException("You already set a Label instance");
			m_nextLabel = label;
			return this;
		}

		@NonNull
		public TypedControlBuilder<I, V> label(@NonNull IBundleCode code) {
			if(null != m_nextLabelControl)
				throw new IllegalStateException("You already set a Label instance");
			m_nextLabel = code.getString();
			return this;
		}


		@NonNull
		public TypedControlBuilder<I, V> label(@NonNull NodeContainer label) {
			if(null != m_nextLabel)
				throw new IllegalStateException("You already set a String label instance");
			m_nextLabelControl = label;
			return this;
		}

		public TypedControlBuilder<I, V> hint(@Nullable String s) {
			m_hintText = s;
			return this;
		}

		public TypedControlBuilder<I, V> hint(@Nullable IBundleCode s) {
			m_hintText = s == null ? null : s.getString();
			return this;
		}

		@NonNull
		public TypedControlBuilder<I, V> unlabeled() {
			label("");
			return this;
		}

		@NonNull
		public TypedControlBuilder<I, V> mandatory() {
			m_mandatory = Boolean.TRUE;
			return this;
		}

		@NonNull
		public TypedControlBuilder<I, V> mandatory(boolean yes) {
			m_mandatory = Boolean.valueOf(yes);
			return this;
		}

		@NonNull
		public TypedControlBuilder<I, V> errorLocation(@NonNull String errorLocation) {
			m_errorLocation = errorLocation;
			return this;
		}

		/*--------------------------------------------------------------*/
		/*	CODING:	Readonly, mandatory, disabled.						*/
		/*--------------------------------------------------------------*/
		@NonNull
		public TypedControlBuilder<I, V> readOnly() {
			m_readOnly = Boolean.TRUE;
			return this;
		}

		/**
		 * Force the next component to have the specified value for readOnly.
		 */
		@NonNull
		public TypedControlBuilder<I, V> readOnly(boolean ro) {
			m_readOnly = Boolean.valueOf(ro);
			return this;
		}

		/**
		 * Bind only the next component to the specified boolean property. See
		 */
		@NonNull
		public <X> TypedControlBuilder<I, V> readOnly(@NonNull X instance, @NonNull String property) {
			m_readOnlyOnce = createRef(instance, property, Boolean.class);
			return this;
		}

		/**
		 * Bind only the next component to the specified boolean property. See
		 */
		@NonNull
		public <X> TypedControlBuilder<I, V> readOnly(@NonNull X instance, @NonNull QField<X, Boolean> property) {
			m_readOnlyOnce = createRef(instance, property);
			return this;
		}

		@NonNull
		public TypedControlBuilder<I, V> disabled() {
			m_disabled = Boolean.TRUE;
			return this;
		}

		/**
		 * Force the next component to have the specified value for disabled.
		 */
		@NonNull
		public TypedControlBuilder<I, V> disabled(boolean ro) {
			m_disabled = Boolean.valueOf(ro);
			return this;
		}
		@NonNull
		public TypedControlBuilder<I, V> testId(String id) {
			m_testid = id;
			return this;
		}

		@NonNull
		public <X> TypedControlBuilder<I, V> disabled(@NonNull X instance, @NonNull String property) {
			m_disabledOnce = createRef(instance, property, Boolean.class);
			return this;
		}

		@NonNull
		public <X> TypedControlBuilder<I, V> disabled(@NonNull X instance, @NonNull QField<X, Boolean> property) {
			m_disabledOnce = createRef(instance, property);
			return this;
		}

		/**
		 * Disables the next component with the specified disable message.
		 */
		@NonNull
		public TypedControlBuilder<I, V> disabledBecause(@Nullable String message) {
			m_disabledMessage = message;
			return this;
		}

		@NonNull
		public <X> TypedControlBuilder<I, V> disabledBecause(@NonNull X instance, @NonNull String property) {
			m_disabledMessageOnce = createRef(instance, property, String.class);
			return this;
		}

		@NonNull
		public <X> TypedControlBuilder<I, V> disabledBecause(@NonNull X instance, @NonNull QField<X, String> property) {
			m_disabledMessageOnce = createRef(instance, property);
			return this;
		}

		/**
		 * Adds the specified css class to the control cell.
		 */
		@NonNull
		public TypedControlBuilder<I, V> cssControl(@NonNull String cssClass) {
			m_controlCss = cssClass;
			return this;
		}

		/**
		 * Adds the specified css class to the label cell.
		 */
		@NonNull
		public TypedControlBuilder<I, V> cssLabel(@NonNull String cssClass) {
			m_labelCss = cssClass;
			return this;
		}

		/**
		 * Add the specified control. Since the control is manually created this code assumes that the
		 * control is <b>properly configured</b> for it's task! This means that this code will not
		 * make any changes to the control! Specifically: if the form item is marked as "mandatory"
		 * but the control here is not then the control stays optional.
		 * The reverse however is not true: if the control passed in is marked as mandatory then the
		 * form item will be marked as such too.
		 */
		public void control(@NonNull IControl<V> control) throws Exception {
			if(control.isMandatory()) {
				m_mandatory = Boolean.TRUE;
			}
			addControl(this, (NodeBase) control, null);
			resetBuilder();
		}

		public <CV> void control(@NonNull IControl<CV> control, IBidiBindingConverter<CV, V> converter) throws Exception {
			if(control.isMandatory()) {
				m_mandatory = Boolean.TRUE;
			}
			addControl(this, (NodeBase) control, converter);
			resetBuilder();
		}

		@NonNull
		public IControl<V> control() throws Exception {
			return controlMain(this, null);
		}

		@NonNull
		public <C extends IControl<V>> C control(@Nullable Class<C> controlClass) throws Exception {
			return controlMain(this, controlClass);
		}
	}

	public class TypedControlBuilderWithInstance<I, V> extends TypedControlBuilder<I, V> implements IHasInstance<I> {
		protected I m_instance;

		public TypedControlBuilderWithInstance(I instance, PropertyMetaModel<V> propertyMeta) {
			super(propertyMeta);
			m_instance = instance;
		}

		@Nullable
		@Override
		public I getInstance() {
			return m_instance;
		}
	}

	final public class UntypedControlBuilder<I> extends BuilderWithInstanceData<I, Object> {
		public UntypedControlBuilder(I instance, PropertyMetaModel<?> propertyMeta) {
			super(instance, (PropertyMetaModel<Object>) propertyMeta);
		}

		/*--------------------------------------------------------------*/
		/*	CODING:	Label control.										*/
		/*--------------------------------------------------------------*/
		@NonNull
		public UntypedControlBuilder<I> label(@NonNull String label) {
			if(null != m_nextLabelControl)
				throw new IllegalStateException("You already set a Label instance");
			m_nextLabel = label;
			return this;
		}

		@NonNull
		public UntypedControlBuilder<I> label(@NonNull IBundleCode label, Object... param) {
			if(null != m_nextLabelControl)
				throw new IllegalStateException("You already set a Label instance");
			m_nextLabel = label.format(param);
			return this;
		}

		@NonNull
		public UntypedControlBuilder<I> label(@NonNull NodeContainer label) {
			if(null != m_nextLabel)
				throw new IllegalStateException("You already set a String label instance");
			m_nextLabelControl = label;
			return this;
		}

		public UntypedControlBuilder<I>  hint(@Nullable String s) {
			m_hintText = s;
			return this;
		}

		public UntypedControlBuilder<I>  hint(@Nullable IBundleCode s) {
			m_hintText = s == null ? null : s.getString();
			return this;
		}

		@NonNull
		public UntypedControlBuilder<I> unlabeled() {
			label("");
			return this;
		}

		@NonNull
		public UntypedControlBuilder<I> mandatory() {
			m_mandatory = Boolean.TRUE;
			return this;
		}

		@NonNull
		public UntypedControlBuilder<I> mandatory(boolean yes) {
			m_mandatory = Boolean.valueOf(yes);
			return this;
		}

		@NonNull
		public UntypedControlBuilder<I> errorLocation(@NonNull String errorLocation) {
			m_errorLocation = errorLocation;
			return this;
		}

		/*--------------------------------------------------------------*/
		/*	CODING:	Readonly, mandatory, disabled.						*/
		/*--------------------------------------------------------------*/
		@NonNull
		public UntypedControlBuilder<I> readOnly() {
			m_readOnly = Boolean.TRUE;
			return this;
		}

		/**
		 * Force the next component to have the specified value for readOnly.
		 */
		@NonNull
		public UntypedControlBuilder<I> readOnly(boolean ro) {
			m_readOnly = Boolean.valueOf(ro);
			return this;
		}

		/**
		 * Bind only the next component to the specified boolean property. See
		 */
		@NonNull
		public <X> UntypedControlBuilder<I> readOnly(@NonNull X instance, @NonNull String property) {
			m_readOnlyOnce = createRef(instance, property, Boolean.class);
			return this;
		}

		/**
		 * Bind only the next component to the specified boolean property. See
		 */
		@NonNull
		public <X> UntypedControlBuilder<I> readOnly(@NonNull X instance, @NonNull QField<X, Boolean> property) {
			m_readOnlyOnce = createRef(instance, property);
			return this;
		}

		@NonNull
		public UntypedControlBuilder<I> disabled() {
			m_disabled = Boolean.TRUE;
			return this;
		}

		/**
		 * Force the next component to have the specified value for disabled.
		 */
		@NonNull
		public UntypedControlBuilder<I> disabled(boolean ro) {
			m_disabled = Boolean.valueOf(ro);
			return this;
		}
		@NonNull
		public UntypedControlBuilder<I> testId(String id) {
			m_testid = id;
			return this;
		}

		@NonNull
		public <X> UntypedControlBuilder<I> disabled(@NonNull X instance, @NonNull String property) {
			m_disabledOnce = createRef(instance, property, Boolean.class);
			return this;
		}

		@NonNull
		public <X> UntypedControlBuilder<I> disabled(@NonNull X instance, @NonNull QField<X, Boolean> property) {
			m_disabledOnce = createRef(instance, property);
			return this;
		}

		/**
		 * Disables the next component with the specified disable message.
		 */
		@NonNull
		public UntypedControlBuilder<I> disabledBecause(@Nullable String message) {
			m_disabledMessage = message;
			return this;
		}

		@NonNull
		public <X> UntypedControlBuilder<I> disabledBecause(@NonNull X instance, @NonNull String property) {
			m_disabledMessageOnce = createRef(instance, property, String.class);
			return this;
		}

		@NonNull
		public <X> UntypedControlBuilder<I> disabledBecause(@NonNull X instance, @NonNull QField<X, String> property) {
			m_disabledMessageOnce = createRef(instance, property);
			return this;
		}

		/**
		 * Adds the specified css class to the control cell.
		 */
		@NonNull
		public UntypedControlBuilder<I> cssControl(@NonNull String cssClass) {
			m_controlCss = cssClass;
			return this;
		}

		/**
		 * Adds the specified css class to the label cell.
		 */
		@NonNull
		public UntypedControlBuilder<I> cssLabel(@NonNull String cssClass) {
			m_labelCss = cssClass;
			return this;
		}

		@NonNull
		public UntypedControlBuilder<I> converter(IBidiBindingConverter<?, ?> converter) {
			m_converter = converter;
			return this;
		}

		/**
		 * Add the specified control. Since the control is manually created this code assumes that the
		 * control is <b>properly configured</b> for it's task! This means that this code will not
		 * make any changes to the control! Specifically: if the form item is marked as "mandatory"
		 * but the control here is not then the control stays optional.
		 * The reverse however is not true: if the control passed in is marked as mandatory then the
		 * form item will be marked as such too.
		 */
		public void control(@NonNull IControl<?> control) throws Exception {
			if(control.isMandatory()) {
				m_mandatory = Boolean.TRUE;
			}
			addControl(this, (NodeBase) control, m_converter);
			resetBuilder();
		}

		public <CV> void control(@NonNull IControl<?> control, IBidiBindingConverter<?, ?> converter) throws Exception {
			if(control.isMandatory()) {
				m_mandatory = Boolean.TRUE;
			}
			addControl(this, (NodeBase) control, converter);
			resetBuilder();
		}

		@NonNull
		public IControl<?> control() throws Exception {
			return controlMain(this, null);
		}

		@NonNull
		public <C extends IControl<?>> C control(@Nullable Class<C> controlClass) throws Exception {
			ControlCreatorRegistry builder = DomApplication.get().getControlCreatorRegistry();
			PropertyMetaModel<Object> pmm = m_propertyMetaModel;
			C control = (C) builder.createControl(pmm, (Class<IControl<Object>>) controlClass);
			addControl(this, (NodeBase) control, null);
			resetBuilder();
			return control;
		}
	}


	/**
	 * When set, the form builder will add (i) icons after each label for controls that have a
	 * hint text. If you set this you should also set {@link DomApplication#isDefaultHintsOnControl()} to false.
	 */
	public static boolean isDefaultShowHintAsIcon() {
		return m_defaultShowHintAsIcon;
	}

	public static void setDefaultShowHintAsIcon(boolean defaultShowHintAsIcon) {
		m_defaultShowHintAsIcon = defaultShowHintAsIcon;
	}

	public static void setDefaultHintRenderer(BiConsumer<NodeContainer, String> defaultHintRenderer) {
		m_defaultHintRenderer = defaultHintRenderer;
	}

	public FormBuilder hintRenderer(BiConsumer<NodeContainer, String> hintRenderer) {
		m_hintRenderer = hintRenderer;
		return this;
	}

	@Nullable
	public BiConsumer<NodeContainer, String> getHintRenderer() {
		BiConsumer<NodeContainer, String> hintRenderer = m_hintRenderer;
		if(null == hintRenderer) {
			hintRenderer = m_defaultHintRenderer;
		}
		return hintRenderer;
	}
}
