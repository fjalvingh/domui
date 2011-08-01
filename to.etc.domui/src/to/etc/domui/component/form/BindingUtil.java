package to.etc.domui.component.form;

import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;

public final class BindingUtil {

	private BindingUtil() {
		super();
	}

	/**
	 * Sends a message to the control that belongs to the object and property
	 * @param bindings
	 * @param object
	 * @param property
	 * @param message
	 * @throws Exception 
	 */
	public static void sendMessage(ModelBindings bindings, Object object, String property, UIMessage message) throws Exception {
		IControl[] h = new IControl[1];
		find(h, bindings, object, property);
		if(h[0] == null) {
			throw new Exception(object.getClass().getSimpleName() + "." + property + " not found in bindings");
		}
		h[0].setMessage(message);
	}

	private static void find(IControl[] h, ModelBindings bindings, Object object, String property) throws Exception {
		for(IModelBinding mb : bindings) {
			if(mb instanceof SimpleComponentPropertyBinding) {
				SimpleComponentPropertyBinding b = (SimpleComponentPropertyBinding) mb;
				System.err.println(b.getModel());
				if(b.getModel().getValue().equals(object) && b.getPropertyMeta().getName().equals(property)) {
					h[0] = b.getControl();
					return;
				}
			} else if(mb instanceof ModelBindings) {
				ModelBindings modelBindings = (ModelBindings) mb;
				find(h, modelBindings, object, property);
			}
		}

	}


}
