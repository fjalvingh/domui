package org.slf4j.impl;

import to.etc.log.*;

/**
 * Required implementation needed by slf4j to bound MDC implementation.  
 * This implementation is bound to {@link EtcMDCAdapter}.
 */
public class StaticMDCBinder {

	/**
	 * The unique instance of this class.
	 */
	public static final StaticMDCBinder	SINGLETON	= new StaticMDCBinder();

	private StaticMDCBinder() {
	}

	/**
	 * Currently this method always returns an instance of 
	 * {@link StaticMDCBinder}.
	 */
	public org.slf4j.spi.MDCAdapter getMDCA() {
		return new EtcMDCAdapter();
	}

	public String getMDCAdapterClassStr() {
		return EtcMDCAdapter.class.getName();
	}
}
