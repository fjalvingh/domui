package to.etc.domui.util.js;

import javax.annotation.*;

import org.mozilla.javascript.*;

/**
 * A Rhino Javascript scope wrapped to allow easy access for common tasks.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 27, 2011
 */
public class ScriptScope implements IScriptScope {
	@Nonnull
	private Scriptable m_scriptable;

	private boolean m_writable;

	public ScriptScope(@Nonnull Scriptable val, boolean writable) {
		m_scriptable = val;
		m_writable = writable;
	}

	public ScriptScope(@Nonnull Scriptable val) {
		this(val, true);
	}

	@Override
	public Object getValue(String name) {
		Object val = m_scriptable.get(name, m_scriptable);
		if(null == val)
			return null;
		if(val instanceof Scriptable) {
			return new ScriptScope((Scriptable) val);
		}

		return val;
	}

	@Override
	public void put(String name, Object instance) {
		if(!m_writable)
			throw new IllegalStateException("This scope is read-only.");
		m_scriptable.put(name, m_scriptable, instance);
	}

	@Override
	public IScriptScope newScope() {
		Context jcx = Context.enter();
		try {
			Scriptable scope = jcx.newObject(m_scriptable);
			scope.setPrototype(m_scriptable);
			scope.setParentScope(null);
			return new ScriptScope(scope, true);
		} finally {
			Context.exit();
		}
	}

	@Override
	public <T> T getAdapter(Class<T> clz) {
		if(clz.isAssignableFrom(Scriptable.class))
			return (T) m_scriptable;
		return null;
	}


}
