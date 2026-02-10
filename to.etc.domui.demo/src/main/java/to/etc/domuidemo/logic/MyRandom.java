package to.etc.domuidemo.logic;

import java.util.Random;

final public class MyRandom {
	@SuppressWarnings("squid:S2245")
	static private final Random m_random = new Random();

	private MyRandom() {
		//-- ignore
	}

	public static Random getRandom() {
		return m_random;
	}
}
