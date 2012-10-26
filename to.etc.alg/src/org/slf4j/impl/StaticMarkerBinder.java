package org.slf4j.impl;

import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.spi.MarkerFactoryBinder;

import to.etc.log.*;

public class StaticMarkerBinder implements MarkerFactoryBinder {

	public static final StaticMarkerBinder	SINGLETON		= new StaticMarkerBinder();

	final IMarkerFactory					markerFactory	= new MyMarkerFactory();

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
