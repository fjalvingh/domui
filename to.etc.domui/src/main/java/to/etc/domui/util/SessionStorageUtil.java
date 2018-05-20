package to.etc.domui.util;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.event.ICheckCallback;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.server.IRequestContext;
import to.etc.domui.state.AppSession;
import to.etc.util.StringTool;
import to.etc.webapp.query.IIdentifyable;
import to.etc.webapp.query.QDataContext;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Util for storing data into Session scope storage.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Aug 30, 2013
 */
public class SessionStorageUtil {

	private static final String PART_TIME = "time";

	private static final String PART_TYPE = "type";

	private static final String PART_ID = "id";

	private static final String EMPTY_VALUE = "#NULL#";

	/**
	 * Defines data that can be stored/load from session storage.
	 *
	 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
	 * Created on Aug 30, 2013
	 */
	public interface ISessionStorage {
		/**
		 * Unique storage data id. Identifies group of data.
		 *
		 * @return
		 */
		@NonNull
		default String getStorageId(){
			return this.getClass().getSimpleName();
		}

		/**
		 * Container that contains data that is stored into session. It means that its nested controls would save current data into session storage.
		 *
		 * @return
		 */
		@NonNull
		default NodeContainer getNodeToStore(){
			if (this instanceof NodeContainer){
				return (NodeContainer) this;
			}
			throw new IllegalStateException("Must be instance of NodeContainer or provide correct node container");
		}

		/**
		 * Defines keys of controls that should be excluded from session store actions.
		 *
		 * @return
		 */
		@NonNull
		default List<String> getIgnoredControlKeys(){
			return Collections.EMPTY_LIST;
		}

		/**
		 * Callback that does custom loading of stored values into custom logic controls.
		 *
		 * @return
		 */
		@Nullable
		default ICheckCallback<ControlValuePair> getCustomLoadCallback(){
			return null;
		}
	}

	/**
	 * DTO - pair of control and value
	 *
	 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
	 * Created on Aug 30, 2013
	 */
	public static class ControlValuePair {
		@NonNull
		private final IControl< ? > m_control;

		@NonNull
		private final Object m_value;

		ControlValuePair(@NonNull IControl< ? > control, @NonNull Object value) {
			m_control = control;
			m_value = value;
		}

		public @NonNull
		IControl< ? > getControl() {
			return m_control;
		}

		public @NonNull
		Object getValue() {
			return m_value;
		}
	}

	/**
	 * Returns if storage currenlty has data stored into session.
	 *
	 * @param ctx
	 * @param storableData
	 * @return
	 */
	public static boolean hasStoredData(@NonNull IRequestContext ctx, @NonNull ISessionStorage storableData) {
		AppSession ses = ctx.getSession();
		return null != ses.getAttribute(storableData.getStorageId() + "|" + PART_TIME);
	}

	/**
	 * Stores storable data into session.
	 *
	 * @param ctx
	 * @param storableData
	 */
	public static void storeData(@NonNull IRequestContext ctx, @NonNull ISessionStorage storableData) {
		AppSession ses = ctx.getSession();
		ses.setAttribute(storableData.getStorageId() + "|" + PART_TIME, new Date());

		for(IControl< ? > control : storableData.getNodeToStore().getDeepChildren(IControl.class)) {
			String controlKey = control.getErrorLocation();
			if(!StringTool.isBlank(controlKey) && !isIgnoredControl(DomUtil.nullChecked(controlKey), storableData.getIgnoredControlKeys())) {
				UIMessage existingMessage = control.getMessage();
				if(null == existingMessage) {
					Object value = control.getValueSafe();
					control.clearMessage();
					String key = storableData.getStorageId() + "|" + controlKey;
					if(null == value) {
						value = EMPTY_VALUE;
						ses.setAttribute(key, value);
					} else {
						if(value instanceof IIdentifyable< ? >) {
							ClassMetaModel cmm = MetaManager.findClassMeta(value.getClass());
							if(null != cmm) {
								Object id = ((IIdentifyable< ? >) value).getId();
								ses.setAttribute(key + "|" + PART_TYPE, cmm.getActualClass());
								ses.setAttribute(key + "|" + PART_ID, id);
							}
						} else {
							ses.setAttribute(key, value);
						}
					}
				}
			}
		}
	}

	private static boolean isIgnoredControl(@NonNull String controlKey, @NonNull List<String> ignoredControlKeys) {
		return ignoredControlKeys.contains(controlKey);
	}

	/**
	 * Loads data previously persisted into session.
	 *
	 * @param dc
	 * @param ctx
	 * @param storableData
	 * @throws Exception
	 */
	public static void loadData(@NonNull QDataContext dc, @NonNull IRequestContext ctx, @NonNull ISessionStorage storableData)
		throws Exception {
		AppSession ses = ctx.getSession();
		for(IControl< ? > control : storableData.getNodeToStore().getDeepChildren(IControl.class)) {
			UIMessage existingMessage = control.getMessage();
			Object existingValue = control.getValueSafe();
			if(null == existingMessage) {
				control.clearMessage();
			} else if(!existingMessage.equals(control.getMessage())) {
				control.setMessage(existingMessage);
			}

			String key = storableData.getStorageId() + "|" + control.getErrorLocation();
			Object sesValue = ses.getAttribute(key);
			ICheckCallback<ControlValuePair> callback = storableData.getCustomLoadCallback();
			if(null != sesValue) {
				if(EMPTY_VALUE.equals(sesValue)) {
					if(!MetaManager.areObjectsEqual(null, existingValue)) {
						control.setValue(null);
						fireValueChanged(control);
					}
				}
 else if(!MetaManager.areObjectsEqual(sesValue, existingValue)) {
					boolean handled = setNonIdentifiableTypeValue(control, sesValue);
					if(handled) {
						fireValueChanged(control);
					} else {
						if(callback != null && callback.check(new ControlValuePair(control, sesValue))) {
							fireValueChanged(control);
						}
					}
				}
			} else {
				Class< ? > type = (Class< ? >) ses.getAttribute(key + "|" + PART_TYPE);
				if(null != type) {
					Object id = ses.getAttribute(key + "|" + PART_ID);
					if(null != id) {
						if(setIdentifiableValue(dc, control, type, id, existingValue)) {
							fireValueChanged(control);
						}
					}
				}
			}
		}
	}

	private static void fireValueChanged(@NonNull IControl< ? > control) throws Exception {
		if(control instanceof NodeBase) {
			IValueChanged<NodeBase> valueChangedListener = (IValueChanged<NodeBase>) control.getOnValueChanged();
			if(valueChangedListener != null) {
				valueChangedListener.onValueChanged((NodeBase) control);
			}
		} else {
			throw new IllegalStateException("Unexpected type for control[" + control.getClass() + "]. Has to be assignable from " + NodeBase.class);
		}
	}

	private static boolean setIdentifiableValue(@NonNull QDataContext dc, @NonNull IControl< ? > control, @NonNull Class< ? > type, @NonNull Object id, @Nullable Object existingValue) throws Exception {
		if(id instanceof Long) {
			Long longId = (Long) id;
			IIdentifyable<Long> val = (IIdentifyable<Long>) dc.find(type, longId);
			if(!MetaManager.areObjectsEqual(val, existingValue)) {
				((IControl<IIdentifyable<Long>>) control).setValue(val);
				return true;
			} else {
				return false;
			}
		} else {
			throw new IllegalArgumentException("Id argument type not supported [" + id + "], Id argument of type:" + id.getClass());
		}
	}

	private static boolean setNonIdentifiableTypeValue(@NonNull IControl< ? > control, @NonNull Object value) {
		boolean recognized = true;
		if(value instanceof Integer) {
			((IControl<Integer>) control).setValue((Integer) value);
		} else if(value instanceof BigDecimal) {
			((IControl<BigDecimal>) control).setValue((BigDecimal) value);
		} else if(value instanceof Double) {
			((IControl<Double>) control).setValue((Double) value);
		} else if(value instanceof Long) {
			((IControl<Long>) control).setValue((Long) value);
		} else if(value instanceof Date) {
			((IControl<Date>) control).setValue((Date) value);
		} else if(value instanceof Boolean) {
			((IControl<Boolean>) control).setValue((Boolean) value);
		} else if(value instanceof String) {
			((IControl<String>) control).setValue((String) value);
		} else if(Enum.class.isAssignableFrom(value.getClass())) {
			((IControl<Enum< ? >>) control).setValue((Enum< ? >) value);
		} else {
			recognized = false;
		}
		return recognized;
	}

}
