package to.etc.domui.logic.errors;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.logic.*;
import to.etc.domui.util.*;

/**
 * Experimental.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 1, 2014
 */
public class ModelErrorHandler {
	@Nonnull
	final private UrlPage m_rootNode;

	private Set<UIMessage> m_existingErrorSet;

	private ErrorSet m_errorSet;

	public ModelErrorHandler(@Nonnull UrlPage root) {
		m_rootNode = root;
	}

	/**
	 * Get all error fences.
	 * @return
	 */
	@Nonnull
	private Set<IErrorFence> getAllFences() throws Exception {
		final Set<IErrorFence> res = new HashSet<>();
		DomUtil.walkTree(m_rootNode, new DomUtil.IPerNode() {
			@Override
			public Object before(NodeBase n) throws Exception {
				if(n instanceof NodeContainer) {
					NodeContainer nc = (NodeContainer) n;
					IErrorFence fence = nc.getErrorFence();
					if(null != fence)
						res.add(fence);
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

	@Nonnull
	private Set<UIMessage> getAllErrorSet(@Nonnull Collection<IErrorFence> fenceList) {
		Set<UIMessage> res = new HashSet<>();
		for(IErrorFence f : fenceList)
			res.addAll(f.getMessageList());
		return res;
	}

	public void reportErrors() throws Exception {
		Set<IErrorFence> allFences = getAllFences();
		m_existingErrorSet = getAllErrorSet(allFences);

		LogiErrors errorModel = m_rootNode.lc().getErrorModel();
		m_errorSet = errorModel.getErrorSet();

		/*
		 * All errors that can be connected to an UI element should go there. By reporting "with"
		 * a component the error shows on the component itself if possible, and will also traverse
		 * to the nearest error panel. Errors that cannot be posted on a component must be shown
		 * globally.
		 *
		 * A solution would be a binding map, but that will cause trouble with gc. So we use the
		 * component tree itself to register errors.
		 */
		DomUtil.walkTree(m_rootNode, new DomUtil.IPerNode() {
			@Override
			public Object before(NodeBase n) throws Exception {
				if(n instanceof IBindable) {
					handleClaimError(n);
				}
				return null;
			}

			@Override
			public Object after(NodeBase n) throws Exception {
				return null;
			}
		});

		//-- All messages that could be claimed are claimed now. Add the rest as "global" messages
		for(LogicError le : m_errorSet) {
			m_existingErrorSet.remove(le.getMessage());
			m_rootNode.addGlobalMessage(le.getMessage());
		}

		//-- Now get rid of all that was no longer reported
		for(UIMessage old : m_existingErrorSet) {
			for(IErrorFence f : allFences) {
				f.removeMessage(old);
			}
		}
	}

	/**
	 * For the component, find all bindings on it and check each for errors to be reported. If no error
	 * is found at all the component's error state is cleared.
	 * @param errorSet
	 * @param n
	 */
	private void handleClaimError(@Nonnull NodeBase n) {
		IBindable b = (IBindable) n;
		List<SimpleBinder> bindingList = b.getBindingList();
		if(null == bindingList)
			return;

		List<LogicError> all = new ArrayList<>();
		for(SimpleBinder binding : bindingList) {
			handleComponentErrorClaim(all, n, binding);
		}

		//-- Remove these errors from "all errors" set, because they're (still) there
		IErrorFence fence = DomUtil.getMessageFence(n);
		for(LogicError le : all) {
			m_existingErrorSet.remove(le.getMessage());
			fence.addMessage(le.getMessage());
			le.getMessage().claimed(true);
		}
	}

	/**
	 * Check for errors on the specified binding.
	 * @param all
	 * @param errorSet
	 * @param n
	 * @param binding
	 */
	private void handleComponentErrorClaim(@Nonnull List<LogicError> all, @Nonnull NodeBase n, @Nonnull SimpleBinder binding) {
		Object instance = binding.getInstance();
		if(null == instance)								// Not an instance binding -> no errors here
			return;
		PropertyMetaModel< ? > property = binding.getInstanceProperty();
		Collection<LogicError> errors = m_errorSet.remove(instance, property);	// Get and remove errors for this binding
		if(null == errors)
			return;
		for(LogicError error : errors) {
			if(null == error.getMessage().getErrorLocation()) {
				error.getMessage().setErrorLocation(binding.getErrorLocation());
			}
			all.add(error);
		}
	}


}
