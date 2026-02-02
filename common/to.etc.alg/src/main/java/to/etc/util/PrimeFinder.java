package to.etc.util;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 14-04-21.
 */
final public class PrimeFinder {
	private PrimeFinder() {}

	/**
	 * List of primes for calculating hash bucket count, from http://compoasso.free.fr/primelistweb/page/prime/liste_online_en.php
	 */
	static private final int[] PRIMES = {
		17, 61, 103, 181, 367, 691, 997, 1721, 2281, 3169,
		3803, 4409, 5077, 6131, 7109, 8009, 9109, 10093,
		11069, 12037, 13009, 14084, 15077, 16073, 17011,
		18061, 20089, 25801, 30881, 40177, 50077, 60037,
		70051, 80071, 90073, 100003, 150001, 200029,
		300007, 400009, 500009, 600011, 700001, 800011,
		900001, 1000003, 2000003, 3000017, 4000037,
		5000011, 6000011, 8000009,
		10000019, 15000017, 20000003, 30000001, 50000017,
		80000023, 120000007, 170_000_009, 230_000_003,
		250_000_117,
		400_000_079,
		600_000_041,
		800_000_063,
		2_000_000_089,
	};

	/**
	 * Find the first prime number larger than the value specified.
	 */
	static public int findFirstPrimeLargerThan(int size) {
		for(int prime : PRIMES) {				// Find the 1st higher prime
			if(size < prime) {
				return prime;
			}
		}
		return PRIMES[PRIMES.length - 1];		// We'll abort anyway, I assume.
	}


}
