package to.etc.domui.util;

import java.math.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.util.*;
import to.etc.webapp.query.*;

public class SessionStorageUtil {

	public static class LoadDataResult {
		public static class ManualUpdatePair {
			private final IControl< ? > m_control;

			private final Object m_value;

			ManualUpdatePair(IControl< ? > control, Object value) {
				super();
				m_control = control;
				m_value = value;
			}

			public IControl< ? > getControl() {
				return m_control;
			}

			public Object getValue() {
				return m_value;
			}

		}

		private final List<IControl< ? >> m_updated = new ArrayList<IControl< ? >>();

		private final List<ManualUpdatePair> m_notUpdated = new ArrayList<ManualUpdatePair>();

		LoadDataResult() {}

		public List<IControl< ? >> getUpdated() {
			return m_updated;
		}

		public List<ManualUpdatePair> getNotUpdated() {
			return m_notUpdated;
		}
	}

	public static void storeData(@Nonnull IRequestContext ctx, @Nonnull NodeContainer containerNode, @Nonnull String dataPrefix, String... ignoreControlKeys) {
		AppSession ses = ctx.getSession();
		for(IControl< ? > control : containerNode.getDeepChildren(IControl.class)) {
			String controlKey = control.getErrorLocation();
			if(!StringTool.isBlank(controlKey) && !isIgnoredControl(DomUtil.nullChecked(controlKey), ignoreControlKeys)) {
				UIMessage existingMessage = control.getMessage();
				if(null == existingMessage) {
					Object value = control.getValueSafe();
					control.clearMessage();
					if(null != value) {
						String key = dataPrefix + "|" + control.getErrorLocation();
						if(value instanceof IIdentifyable< ? >) {
							ClassMetaModel cmm = MetaManager.findClassMeta(value.getClass());
							if(null != cmm) {
								Object id = ((IIdentifyable< ? >) value).getId();
								ses.setAttribute(key + "|type", cmm.getActualClass());
								ses.setAttribute(key + "|id", id);
								System.out.println(key + "|type > " + cmm.getActualClass().getName());
								System.out.println(key + "|id > " + String.valueOf(id));
							}
						} else {
							ses.setAttribute(key, value);
							System.out.println(key + " > " + value);
						}
					}
				}
			}
		}
	}

	private static boolean isIgnoredControl(@Nonnull String controlKey, String... ignoreControlKeys) {
		if(null == ignoreControlKeys || ignoreControlKeys.length == 0) {
			return false;
		}
		for(String key : ignoreControlKeys) {
			if(key.equals(controlKey)) {
				return true;
			}
		}
		return false;
	}

	public static LoadDataResult loadData(@Nonnull QDataContext dc, @Nonnull IRequestContext ctx, @Nonnull NodeContainer containerNode, @Nonnull String dataPrefix) throws Exception {
		AppSession ses = ctx.getSession();
		LoadDataResult res = new LoadDataResult();
		for(IControl< ? > control : containerNode.getDeepChildren(IControl.class)) {
			UIMessage existingMessage = control.getMessage();
			Object existingValue = control.getValueSafe();
			if(null == existingMessage) {
				control.clearMessage();
			} else if(!existingMessage.equals(control.getMessage())) {
				control.setMessage(existingMessage);
			}

			String key = dataPrefix + "|" + control.getErrorLocation();
			Object sesValue = ses.getAttribute(key);
			if(null != sesValue) {
				if(!MetaManager.areObjectsEqual(sesValue, existingValue)) {
					boolean handled = setNonIdentifiableTypeValue(control, sesValue);
					if(handled) {
						res.getUpdated().add(control);
					} else {
						res.getNotUpdated().add(new LoadDataResult.ManualUpdatePair(control, sesValue));
					}
				}
			} else {
				Class< ? > type = (Class< ? >) ses.getAttribute(key + "|type");
				if(null != type) {
					Object id = ses.getAttribute(key + "|id");
					if(null != id) {
						if(setIdentifiableValue(dc, control, type, id, existingValue)) {
							res.getUpdated().add(control);
						}
					}
				}
			}
		}
		return res;
	}

	private static boolean setIdentifiableValue(@Nonnull QDataContext dc, @Nonnull IControl< ? > control, @Nonnull Class< ? > type, @Nonnull Object id, @Nullable Object existingValue) throws Exception {
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

	private static boolean setNonIdentifiableTypeValue(@Nonnull IControl< ? > control, @Nonnull Object value) {
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
		} else if(value instanceof String) {
			((IControl<String>) control).setValue((String) value);
		} else {
			recognized = false;
		}
		return recognized;
	}

}
