package to.etc.log.idioticjavasystemloggarbage;

import to.etc.log.EtcLogger;
import to.etc.log.EtcLoggerFactory;

import java.lang.System.Logger;

/**
 * This wraps the System.Logger garbage into an EtcLogger, so that we now also capture
 * that idiots output.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 15-02-22.
 */
public class GarbageLoggerFinder extends System.LoggerFinder { // and there we go again: an abstract class, no interface. What a bunch of morons.

	public GarbageLoggerFinder() {
		System.out.println("EtcLogger: replace the System.Logger 8-(");
	}

	@Override
	public Logger getLogger(String name, Module module) {
		EtcLogger logger = EtcLoggerFactory.getSingleton().getLogger(name);
		return new GarbageLogger(logger);
	}
}
