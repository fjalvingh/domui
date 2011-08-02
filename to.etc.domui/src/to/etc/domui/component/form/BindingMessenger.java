package to.etc.domui.component.form;

import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.webapp.nls.*;

public final class BindingMessenger {

	ModelBindings m_bindings;

	BundleRef m_bundleRef;

	public BindingMessenger(ModelBindings bindings, BundleRef bundleRef) {
		super();
		m_bindings = bindings;
		m_bundleRef = bundleRef;
	}

	/**
	 * Sends a message to the control that belongs to the object and property
	 * @param object
	 * @param property
	 * @param message
	 * @throws Exception 
	 */
	public void error(Object object, String property, String message, Object... param) throws Exception {
		IControl[] h = new IControl[1];
		find(m_bindings, h, object, property);
		if(h[0] == null) {
			throw new Exception(object.getClass().getSimpleName() + "." + property + " not found in bindings");
		}
		h[0].setMessage(UIMessage.error(m_bundleRef, message, param));
	}

	private void find(ModelBindings bindings, IControl[] h, Object object, String property) throws Exception {
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
				find(modelBindings, h, object, property);
			}
		}

	}


}
