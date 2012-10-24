package to.etc.domui.util.js;

import java.io.*;
import java.util.*;

import javax.annotation.*;

import org.mozilla.javascript.*;

import to.etc.util.*;

class RhinoObjectBase implements IScriptScope {
	@Nonnull
	private ScriptableObject m_scriptable;

	private boolean m_writable;

	RhinoObjectBase(@Nonnull ScriptableObject scriptable, boolean writable) {
		m_scriptable = scriptable;
		m_writable = writable;
	}

	/**
	 * Create the local scope for this executor, inheriting the root scope containing
	 * Object, Function and other stuff.
	 *
	 * @param rootScope
	 */
	RhinoObjectBase(ScriptableObject rootScope) {
		m_writable = true;
		Context jcx = Context.enter();
		try {
			m_scriptable = (ScriptableObject) jcx.newObject(rootScope);
			m_scriptable.setPrototype(rootScope);
			m_scriptable.setParentScope(null);
		} finally {
			Context.exit();
		}
	}

	@Nullable
	protected <T> T translateValue(Class<T> targetClass, @Nullable Object val) {
		if(null == val)
			return null;
		if(val == UniqueTag.NOT_FOUND)
			return null;
		if(targetClass.isAssignableFrom(val.getClass()))			// Directly assignable?
			return (T) val;

		if(val instanceof ScriptableObject) {
			val = new RhinoScriptScope((ScriptableObject) val);
			if(targetClass.isAssignableFrom(val.getClass()))		// Directly assignable?
				return (T) val;
		}

		return RuntimeConversions.convertTo(val, targetClass);
	}

	@Override
	public void put(@Nonnull String name, @Nullable Object instance) {
		if(!m_writable)
			throw new IllegalStateException("This scope is read-only.");
		m_scriptable.put(name, m_scriptable, instance);
	}

	@Nonnull
	@Override
	public RhinoScriptScope newScope() {
		Context jcx = Context.enter();
		try {
			ScriptableObject scope = (ScriptableObject) jcx.newObject(m_scriptable);
			scope.setPrototype(m_scriptable);
			scope.setParentScope(null);
			return new RhinoScriptScope(scope, true);
		} finally {
			Context.exit();
		}
	}

	/**
	 *
	 * @see to.etc.domui.util.js.IScriptScope#getValue(java.lang.String)
	 */
	@Nullable
	@Override
	public <T> T getValue(@Nonnull Class<T> targetType, @Nonnull String name) {
		Object val = ScriptableObject.getProperty(m_scriptable, name);
		return translateValue(targetType, val);
	}

	@Nullable
	@Override
	public <T> T getAdapter(@Nonnull Class<T> clz) {
		if(clz.isAssignableFrom(Scriptable.class))
			return (T) m_scriptable;
		return null;
	}

	@Nonnull
	@Override
	public <T> List<T> getProperties(@Nonnull Class<T> filterClass) {
		Object[] ids = m_scriptable.getIds();
		List<T> res = new ArrayList<T>(ids.length);
		for(Object id : ids) {
			if(filterClass.isAssignableFrom(id.getClass()))
				res.add((T) id);
		}
		return res;
	}

	@Override
	@Nullable
	public <T> T eval(@Nonnull Class<T> targetType, @Nonnull Reader r, @Nonnull String sourceFileNameIndicator) throws Exception {
		Context jcx = Context.enter();
		try {
			Object val = jcx.evaluateReader(m_scriptable, r, sourceFileNameIndicator, 1, null);
			return translateValue(targetType, val);
		} finally {
			Context.exit();
		}
	}

	@Override
	public <T> T eval(@Nonnull Class<T> targetType, @Nonnull String expression, @Nonnull String sourceFileNameIndicator) throws Exception {
		Context jcx = Context.enter();
		try {
			Object val = jcx.evaluateString(m_scriptable, expression, sourceFileNameIndicator, 1, null);
			return translateValue(targetType, val);
		} finally {
			Context.exit();
		}
	}

	public Object toObject(Object o) {
		return Context.toObject(o, m_scriptable);
	}

	@Nonnull
	@Override
	public IScriptScope addObjectProperty(@Nonnull String name) {
		IScriptScope ns = newScope();
		put(name, ns);
		return ns;
	}


}
