package org.hibernate.impl;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 10-6-16.
 */
public interface XXDebugData {
	Exception getAllocationPoint();
	Exception getClosePoint();
	int getId();
}
