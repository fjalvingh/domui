package to.etc.domui.webdriver.core.base;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.function.ConsumerEx;

public class Either<RIGHT, WRONG> {
	@Nullable
	private RIGHT m_right;

	@Nullable
	private WRONG m_wrong;

	private Either(@Nullable RIGHT right, @Nullable WRONG wrong) {
		if(right == null && wrong == null) {
			throw new IllegalArgumentException("Can't have both right and wrong nulls");
		}
		if(right != null && wrong != null) {
			throw new IllegalArgumentException("Can't have both right and wrong non null");
		}
		m_right = right;
		m_wrong = wrong;
	}

	public Either<RIGHT, WRONG> whenRight(ConsumerEx<RIGHT> right) throws Exception {
		if(m_right != null) {
			right.accept(m_right);
		}
		return this;
	}

	public void whenWrong(ConsumerEx<WRONG> errorConsumer) throws Exception {
		if(m_wrong != null) {
			errorConsumer.accept(m_wrong);
		}
	}

	public void whenWrongFail() {
		if(m_wrong != null) {
			throw new IllegalStateException("");
		}
	}

	public void whenWrongFail(String msg) {
		if(m_wrong != null) {
			throw new IllegalStateException(msg);
		}
	}

	public Either<RIGHT, WRONG> whenRightFail() {
		if(m_right != null) {
			throw new IllegalStateException();
		}
		return this;
	}

	public Either<RIGHT, WRONG> whenRightFail(String msg) {
		if(m_right != null) {
			throw new IllegalStateException(msg);
		}
		return this;
	}

	public static <S, A> Either<S, A> wrong(A error) {
		return new Either<S, A>(null, error);
	}

	public static <S, A> Either<S, A> right(S right) {
		return new Either<S, A>(right, null);
	}
}
