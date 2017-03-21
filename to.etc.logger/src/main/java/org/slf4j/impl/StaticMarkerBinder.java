package org.slf4j.impl;

import org.slf4j.*;
import org.slf4j.helpers.*;
import org.slf4j.spi.*;

/**
 * Required implementation needed by slf4j to bound custom marker implementation.  
 * 
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Oct 31, 2012
 */
public class StaticMarkerBinder implements MarkerFactoryBinder {

	public static final StaticMarkerBinder	SINGLETON		= new StaticMarkerBinder();

	final IMarkerFactory					markerFactory	= new to.etc.log.EtcMarkerFactory();

	private StaticMarkerBinder() {
	}

	@Override
	public IMarkerFactory getMarkerFactory() {
		return markerFactory;
	}

	@Override
	public String getMarkerFactoryClassStr() {
		return BasicMarkerFactory.class.getName();
	}

}
