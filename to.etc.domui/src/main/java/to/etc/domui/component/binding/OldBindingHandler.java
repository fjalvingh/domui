package to.etc.domui.component.binding;

import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.server.DomApplication;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.IValueAccessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the default binding manager.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 12-3-17.
 */
final public class OldBindingHandler {
	static public final String BINDING_ERROR = "BindingError";

	private OldBindingHandler() {}

	/**
	 * System helper method to move all bindings from control into the model (called at request start).
	 * @param root
	 * @throws Exception
	 */
	static public void controlToModel(@Nonnull NodeBase root) throws Exception {
		DomApplication.get().getBindingHandler(root).controlToModel();
	}

	/**
	 * System helper method to move all bindings from model to control (called at request end).
	 * @param root
	 * @throws Exception
	 */
	static public void modelToControl(@Nonnull NodeBase root) throws Exception {
		DomApplication.get().getBindingHandler(root).modelToControl();
	}

	/**
	 * Get a list of binding errors starting at (and including) the parameter node. Each
	 * message will contain the NodeBase control that failed inside {@link UIMessage#getErrorNode()}.
	 * @param root
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	static public List<UIMessage> getBindingErrors(@Nonnull NodeBase root) throws Exception {
		final List<UIMessage> res = new ArrayList<>();
		DomUtil.walkTreeUndelegated(root, new DomUtil.IPerNode() {
			@Override
			public Object before(NodeBase n) throws Exception {
				List<IBinding> list = n.getBindingList();
				if(null != list) {
					for(IBinding sb : list) {
						UIMessage message = sb.getBindError();
						if(null != message)
							res.add(message);
					}
				}
				return null;
			}

			@Override
			public Object after(NodeBase n) throws Exception {
				return null;
			}
		});
		return res;
	}

	/**
	 * If the specified subtree has binding errors: report them, and return TRUE if there are
	 * errors.
	 * @param root
	 * @return true if errors are present
	 * @throws Exception
	 */
	static public boolean reportBindingErrors(@Nonnull NodeBase root) throws Exception {
		final boolean[] silly = new boolean[1];					// Not having free variables is a joke.
		DomUtil.walkTreeUndelegated(root, new DomUtil.IPerNode() {
			@Override
			public Object before(NodeBase n) throws Exception {
				List<IBinding> list = n.getBindingList();
				if(null != list) {
					List<UIMessage> bindErrorList= new ArrayList<>();

					//-- Find all bindings with an error
					for(IBinding sb : list) {
						UIMessage message = sb.getBindError();
						if(null != message) {
							bindErrorList.add(message);
						}
					}

					//-- If there is an error somewhere- report the 1st one on the component
					if(bindErrorList.size() > 0) {
						UIMessage message = bindErrorList.get(0);		// Report the first error as the binding error.
						message.group(BINDING_ERROR);
						silly[0] = true;
						n.setMessage(message);
					} else {
						/*
						 * jal 20160215 This binding's component does not have a binding error now. An old
						 * comment said "should not be reset: should be done by component itself". That seems
						 * to be wrong, though. We should not just set the component error to null here, because
						 * an error can be put there by something else. But if the component is showing a binding
						 * error caused by a /previous/ run of this code then that error should be removed, because
						 * otherwise no one does! The component cannot do it because it is forbidden to play
						 * with messages during binding.
						 */
						UIMessage componentMessage = n.getMessage();
						if(componentMessage != null && BINDING_ERROR.equals(componentMessage.getGroup())) {
							n.setMessage(null);
						}
					}
				}
				return null;
			}

			@Override
			public Object after(NodeBase n) throws Exception {
				return null;
			}
		});
		return silly[0];
	}

	@Nullable
	public static ComponentPropertyBinding findBinding(NodeBase nodeBase, String string) {
		List<IBinding> list = nodeBase.getBindingList();
		if(list != null) {
			for(IBinding sb : list) {
				if(sb instanceof ComponentPropertyBinding) {
					ComponentPropertyBinding sib = (ComponentPropertyBinding) sb;
					IValueAccessor<?> property = sib.getControlProperty();
					if(property instanceof PropertyMetaModel) {
						if(string.equals(((PropertyMetaModel<?>) property).getName()))
							return sib;
					}
				}
			}
		}
		return null;
	}
}
