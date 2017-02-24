package org.slf4j.impl;


import org.slf4j.*;
import org.slf4j.spi.*;

import to.etc.log.EtcLoggerFactory;


/**
 * Required implementation needed by slf4j to bound custom logger implementation.  
 * 
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Oct 31, 2012
 */
public class StaticLoggerBinder implements LoggerFactoryBinder {

	/** 
	 * The unique instance of this class. 
	 */
	private static final StaticLoggerBinder	SINGLETON	= new StaticLoggerBinder();

	/** 
	 * Return the singleton of this class. 
	 * 
	 * @return the StaticLoggerBinder singleton 
	 */
	public static final StaticLoggerBinder getSingleton() {
		return SINGLETON;
	}


	/** 
	 * Declare the version of the SLF4J API this implementation is 
	 * compiled against. The value of this field is usually modified 
	 * with each release. 
	 */
	// To avoid constant folding by the compiler,  
	// this field must *not* be final  
	public static String			REQUESTED_API_VERSION	= "1.7.23";						// !final

	private static final String		loggerFactoryClassStr	= EtcLoggerFactory.class.getName();

	/** 
	 * The ILoggerFactory instance returned by the 
	 * {@link #getLoggerFactory} method should always be the same 
	 * object. 
	 */
	private final ILoggerFactory	loggerFactory;

	private StaticLoggerBinder() {
		loggerFactory = EtcLoggerFactory.getSingleton();
	}

	@Override
	public ILoggerFactory getLoggerFactory() {
		return loggerFactory;
	}

	@Override
	public String getLoggerFactoryClassStr() {
		return loggerFactoryClassStr;
	}
}
